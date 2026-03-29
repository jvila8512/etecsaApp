package com.etecsa.service.modbus;

import com.etecsa.AppsupervisorApp;
import com.etecsa.domain.Equipo;
import com.etecsa.domain.EventoEquipo;
import com.etecsa.domain.enumeration.TipoDato;
import com.etecsa.domain.enumeration.TipoRegistro;
import com.etecsa.repository.EventoEquipoRepository;
import com.etecsa.service.AlarmaService;
import com.etecsa.service.dto.AlarmaDeteccionDTO;
import com.etecsa.service.dto.DashboardEquipoDTO;
import com.etecsa.service.dto.EventoResumenDTO;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Servicio de polling que lee valores de los equipos via Modbus.
 * Soporta intervalo de lectura variable por cada EventoEquipo.
 */
@Service
public class PollingService {

    private static final Logger LOG = LoggerFactory.getLogger(AppsupervisorApp.class);

    private final ModBusService modBusService;
    private final EventoEquipoRepository eventoEquipoRepository;
    private final AlarmaService alarmaService;

    // Mapa para trackear última vez que se envió cada variable por WebSocket
    // Key: eventoId, Value: última vez enviada (epoch millis)
    private final ConcurrentHashMap<Long, Long> lastSentTime = new ConcurrentHashMap<>();

    // Mapa para evitar detecciones duplicadas (Key: eventoId, Value: timestamp)
    private final ConcurrentHashMap<Long, Long> lastAlarmCheck = new ConcurrentHashMap<>();

    public PollingService(ModBusService modBusService, EventoEquipoRepository eventoEquipoRepository, AlarmaService alarmaService) {
        this.modBusService = modBusService;
        this.eventoEquipoRepository = eventoEquipoRepository;
        this.alarmaService = alarmaService;
    }

    /**
     * Determina el intervalo de lectura en segundos para una variable.
     *
     * OPTIMIZADO para alarmas (200+ equipos × 20 variables):
     * 1. Booleanos con umbral → 500ms (INSTANTÁNEO para alarmas críticas)
     * 2. Numéricos con umbral → 2 segundos
     * 3. Equipos críticos → 3 segundos
     * 4. Intervalo específico configurado → ese valor
     * 5. Intervalo base del equipo → ese valor
     * 6. Por defecto → 10 segundos
     */
    private int getIntervaloLectura(EventoEquipo ev, Equipo equipo) {
        // 1. Booleanos con umbral = INSTANTÁNEO (500ms)
        if (ev.getUmbralAlerta() != null && ev.getTipoRegistro() == com.etecsa.domain.enumeration.TipoRegistro.BIT_LOGICO_M) {
            return 1; // 1 segundo (el polling base es cada 500ms)
        }

        // 2. Numéricos con umbral = 2 segundos
        if (ev.getUmbralAlerta() != null) {
            return 2;
        }

        // 3. Equipos críticos = 3 segundos
        if (Boolean.TRUE.equals(equipo.getCritico())) {
            return 3;
        }

        // 4. Intervalo específico de la variable
        if (ev.getIntervaloLectura() != null && ev.getIntervaloLectura() > 0) {
            return ev.getIntervaloLectura();
        }

        // 5. Intervalo base del equipo
        if (equipo.getIntervaloBase() != null && equipo.getIntervaloBase() > 0) {
            return equipo.getIntervaloBase();
        }

        // 6. Por defecto
        return 10;
    }

    /**
     * Determina si una variable debe ser enviada por WebSocket según su intervalo.
     */
    private boolean debeEnviarPorWebSocket(Long eventoId, int intervaloSegundos) {
        long now = System.currentTimeMillis();
        long lastSent = lastSentTime.getOrDefault(eventoId, 0L);
        long intervalMs = intervaloSegundos * 1000L;

        return (now - lastSent) >= intervalMs;
    }

    /**
     * Marca una variable como enviada por WebSocket.
     */
    private void marcarEnviada(Long eventoId) {
        lastSentTime.put(eventoId, System.currentTimeMillis());
    }

    @Async("modbusExecutor")
    public CompletableFuture<DashboardEquipoDTO> procesarEquipo(Equipo equipo, boolean enviarPorWebSocket, boolean detectarAlarmas) {
        DashboardEquipoDTO eqDto = new DashboardEquipoDTO();
        eqDto.setId(equipo.getId());
        eqDto.setNombre(equipo.getNombre());
        eqDto.setDireccionIp(equipo.getDireccionIp());

        String genId = equipo.getId().toString();

        // ── Conectar si es necesario ──────────────────────────────
        if (!modBusService.isConnected(genId)) {
            boolean ok = modBusService.connectToGenerator(genId, equipo.getDireccionIp());
            if (!ok) {
                eqDto.setEstado("DESCONECTADO");
                return CompletableFuture.completedFuture(eqDto);
            }
        }

        try {
            List<EventoEquipo> eventos = eventoEquipoRepository.findByEquipoIdWithPlantilla(equipo.getId());

            for (EventoEquipo ev : eventos) {
                // Determinar si la variable es de lectura o tiene función de escritura
                String funcEscritura = ev.getPlantilla().getFuncionEscritura();
                boolean esLectura = funcEscritura == null || funcEscritura.isEmpty();
                if (ev.getEsEscribible() != null && ev.getEsEscribible()) {
                    esLectura = false;
                }

                EventoResumenDTO res = new EventoResumenDTO();
                res.setId(ev.getId());
                res.setNombreVariable(ev.getNombreVariable());
                res.setUnidadMedida(ev.getPlantilla().getUnidadMedida());
                res.setEsLectura(esLectura);
                res.setUmbralAlerta(ev.getUmbralAlerta());
                res.setHabilitarAlarma(ev.getHabilitarAlarma());

                int dir = ev.getDireccionModbus();
                res.setDir(dir);

                res.setValorBooleano(null);
                res.setValorNumerico(null);

                // ══════════════════════════════════════════════════
                // LEER SEGÚN tipoRegistro + tipoDato
                // ══════════════════════════════════════════════════
                Boolean valorBooleanoLeido = null;
                Double valorNumericoLeido = null;

                switch (ev.getTipoRegistro()) {
                    case BIT_LOGICO_M: {
                        Boolean val = modBusService.readM(genId, dir);
                        if (val != null) {
                            res.setValorBooleano(val);
                            valorBooleanoLeido = val;
                            if (esLectura) {
                                ev.setValorBooleano(val);
                            }
                        } else {
                            res.setValorBooleano(ev.getValorBooleano());
                        }
                        break;
                    }
                    case PALABRA_MW: {
                        Integer raw = modBusService.readMW(genId, dir);
                        if (raw != null) {
                            Double valor = interpretarMW(raw, ev.getTipoDato());
                            Double escalado = escalar(valor, ev.getPlantilla().getScalingFactor());
                            res.setValorNumerico(escalado);
                            valorNumericoLeido = escalado;
                            if (esLectura) {
                                ev.setValorNumerico(escalado);
                            }
                        } else {
                            res.setValorNumerico(ev.getValorNumerico());
                        }
                        break;
                    }
                    case PALABRA_DOBLE_MD: {
                        Long raw = modBusService.readMD(genId, dir);
                        if (raw != null) {
                            Double valor = interpretarMD(raw, ev.getTipoDato());
                            Double escalado = escalar(valor, ev.getPlantilla().getScalingFactor());
                            res.setValorNumerico(escalado);
                            valorNumericoLeido = escalado;
                            if (esLectura) {
                                ev.setValorNumerico(escalado);
                            }
                        } else {
                            res.setValorNumerico(ev.getValorNumerico());
                        }
                        break;
                    }
                    default:
                        LOG.warn("TipoRegistro desconocido: {} en variable {}", ev.getTipoRegistro(), ev.getNombreVariable());
                        break;
                }

                // ── DETECTAR ALARMAS (solo si tiene umbral configurado y se pide) ─
                if (detectarAlarmas) {
                    LOG.info(
                        ">>> CHECK ALARMA: variable={}, umbral={}, bool={}, num={}",
                        ev.getNombreVariable(),
                        ev.getUmbralAlerta(),
                        valorBooleanoLeido,
                        valorNumericoLeido
                    );
                    detectarAlarma(ev, valorBooleanoLeido, valorNumericoLeido);
                }

                // ── SIEMPRE guardar a BD ───────────────────────────
                ev.setTimestampActualizacion(ZonedDateTime.now());
                eventoEquipoRepository.save(ev);

                // ── ¿Enviar por WebSocket? ────────────────────────
                int intervalo = getIntervaloLectura(ev, equipo);
                if (enviarPorWebSocket && debeEnviarPorWebSocket(ev.getId(), intervalo)) {
                    eqDto.getVariables().add(res);
                    marcarEnviada(ev.getId());
                }
            }

            eqDto.setEstado("OPERATIVO");
        } catch (Exception e) {
            LOG.error("Error leyendo PLC {} ({}): {}", eqDto.getNombre(), equipo.getDireccionIp(), e.getMessage());
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("Connection") || msg.contains("Timeout") || msg.contains("closed")) {
                modBusService.disconnectGenerator(genId);
                eqDto.setEstado("DESCONECTADO");
            } else {
                eqDto.setEstado("ERROR");
            }
        }

        return CompletableFuture.completedFuture(eqDto);
    }

    /**
     * Version legacy para compatibilidad (envia siempre).
     */
    @Async("modbusExecutor")
    public CompletableFuture<DashboardEquipoDTO> procesarEquipo(Equipo equipo) {
        return procesarEquipo(equipo, true, true);
    }

    private Double interpretarMW(int raw, TipoDato tipoDato) {
        if (tipoDato == null) return (double) raw;
        switch (tipoDato) {
            case BOOLEAN:
                return raw != 0 ? 1.0 : 0.0;
            case INT16:
                return (double) (short) raw;
            case UINT16:
                return (double) raw;
            default:
                return (double) raw;
        }
    }

    private Double interpretarMD(long raw, TipoDato tipoDato) {
        if (tipoDato == null) return (double) raw;
        switch (tipoDato) {
            case INT32: {
                int signed = (int) (raw & 0xFFFFFFFFL);
                return (double) signed;
            }
            case FLOAT32: {
                int bits = (int) (raw & 0xFFFFFFFFL);
                float f = Float.intBitsToFloat(bits);
                return (double) f;
            }
            default:
                return (double) raw;
        }
    }

    private Double escalar(Double valor, Double scalingFactor) {
        if (valor == null) return null;
        if (scalingFactor == null || scalingFactor == 0.0) return valor;
        return valor * scalingFactor;
    }

    /**
     * Detecta alarmas durante el polling.
     *
     * Logica:
     * - Solo si habilitarAlarma = true
     * - Booleana: alarma si valor=true Y tiene umbral configurado
     * - Numerica: alarma si valor > umbral
     *
     * Evita duplicados: solo detecta cada 1 segundo por evento
     */
    private void detectarAlarma(EventoEquipo ev, Boolean valorBooleano, Double valorNumerico) {
        // Solo procesar si tiene habilitadas las alarmas
        if (ev.getHabilitarAlarma() == null || !ev.getHabilitarAlarma()) {
            return;
        }

        // Solo procesar si tiene umbral configurado
        if (ev.getUmbralAlerta() == null) {
            return;
        }

        // Evitar detecciones muy seguidas (cada 1 segundo maximo)
        long now = System.currentTimeMillis();
        Long lastCheck = lastAlarmCheck.get(ev.getId());
        if (lastCheck != null && (now - lastCheck) < 1000) {
            return;
        }
        lastAlarmCheck.put(ev.getId(), now);

        boolean esAlarma = false;

        // Boolean: alarma si true (puerta abierta, alarma activada, etc.)
        if (valorBooleano != null && valorBooleano) {
            esAlarma = true;
            LOG.info(">>> ALARMA BOOLEANA: {} en equipo {}", ev.getNombreVariable(), ev.getEquipo().getNombre());
        }
        // Numérica: alarma si valor > umbral
        else if (valorNumerico != null && valorNumerico > ev.getUmbralAlerta()) {
            esAlarma = true;
            LOG.info(">>> ALARMA NUMERICA: {}={} > umbral={}", ev.getNombreVariable(), valorNumerico, ev.getUmbralAlerta());
        }

        if (esAlarma) {
            try {
                AlarmaDeteccionDTO deteccion = new AlarmaDeteccionDTO();
                deteccion.setEventoId(ev.getId());
                deteccion.setEsAlarma(true);
                deteccion.setValorBooleano(valorBooleano);
                deteccion.setValorActual(valorNumerico);
                deteccion.setUmbral(ev.getUmbralAlerta());
                deteccion.setSeveridad(ev.getSeveridadAlerta() != null ? ev.getSeveridadAlerta().name() : "ALTA");

                alarmaService.procesarDeteccion(deteccion);
            } catch (Exception e) {
                LOG.error("Error creando alarma: {}", e.getMessage());
            }
        }
    }
}
