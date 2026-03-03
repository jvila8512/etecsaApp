package com.etecsa.service.modbus;

import com.etecsa.AppsupervisorApp;
import com.etecsa.domain.Equipo;
import com.etecsa.domain.EventoEquipo;
import com.etecsa.domain.enumeration.TipoDato;
import com.etecsa.repository.EventoEquipoRepository;
import com.etecsa.service.dto.DashboardEquipoDTO;
import com.etecsa.service.dto.EventoResumenDTO;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PollingService {

    private static final Logger LOG = LoggerFactory.getLogger(AppsupervisorApp.class);

    private final ModBusService modBusService;
    private final EventoEquipoRepository eventoEquipoRepository;

    public PollingService(ModBusService modBusService, EventoEquipoRepository eventoEquipoRepository) {
        this.modBusService = modBusService;
        this.eventoEquipoRepository = eventoEquipoRepository;
    }

    @Async("modbusExecutor")
    public CompletableFuture<DashboardEquipoDTO> procesarEquipo(Equipo equipo) {
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
                // Solo variables de lectura
                String funcEscritura = ev.getPlantilla().getFuncionEscritura();
                if (funcEscritura != null && !funcEscritura.isEmpty()) continue;

                EventoResumenDTO res = new EventoResumenDTO();
                res.setNombreVariable(ev.getNombreVariable());
                res.setUnidadMedida(ev.getPlantilla().getUnidadMedida());
                res.setEsLectura(true);

                int dir = ev.getDireccionModbus();

                // ══════════════════════════════════════════════════
                // LEER SEGÚN tipoRegistro + tipoDato
                // ══════════════════════════════════════════════════
                switch (ev.getTipoRegistro()) {
                    // ── BIT_LOGICO_M → FC01 Read Coils ──────────
                    case BIT_LOGICO_M: {
                        Boolean val = modBusService.readM(genId, dir);
                        res.setValorBooleano(val);
                        ev.setValorBooleano(val);
                        break;
                    }
                    // ── PALABRA_MW → FC03, 1 registro 16 bits ───
                    case PALABRA_MW: {
                        Integer raw = modBusService.readMW(genId, dir);
                        if (raw != null) {
                            Double valor = interpretarMW(raw, ev.getTipoDato());
                            Double escalado = escalar(valor, ev.getPlantilla().getScalingFactor());
                            res.setValorNumerico(escalado);
                            ev.setValorNumerico(escalado);
                        }
                        break;
                    }
                    // ── PALABRA_DOBLE_MD → FC03, 2 registros 32 bits ─
                    case PALABRA_DOBLE_MD: {
                        Long raw = modBusService.readMD(genId, dir);
                        if (raw != null) {
                            Double valor = interpretarMD(raw, ev.getTipoDato());
                            Double escalado = escalar(valor, ev.getPlantilla().getScalingFactor());
                            res.setValorNumerico(escalado);
                            ev.setValorNumerico(escalado);
                        }
                        break;
                    }
                    default:
                        LOG.warn("⚠️ TipoRegistro desconocido: {} en variable {}", ev.getTipoRegistro(), ev.getNombreVariable());
                        break;
                }

                ev.setTimestampActualizacion(ZonedDateTime.now());
                eventoEquipoRepository.save(ev);
                eqDto.getVariables().add(res);
            }

            eqDto.setEstado("OPERATIVO");
        } catch (Exception e) {
            LOG.error("❌ Error leyendo PLC {} ({}): {}", eqDto.getNombre(), equipo.getDireccionIp(), e.getMessage());
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("Connection") || msg.contains("Timeout") || msg.contains("closed")) {
                modBusService.disconnectGenerator(genId); // forzar reconexión próximo ciclo
                eqDto.setEstado("DESCONECTADO");
            } else {
                eqDto.setEstado("ERROR");
            }
        }

        return CompletableFuture.completedFuture(eqDto);
    }

    // ════════════════════════════════════════════════════════════
    // HELPERS — Interpretar valor según tipoDato
    // ════════════════════════════════════════════════════════════

    /**
     * Interpreta un registro MW (16 bits unsigned) según el tipoDato configurado.
     *
     * Del scan sabemos que el TM221 manda cada BYTE en un registro separado.
     * Por eso INT16/UINT16 son el valor directo del registro.
     *
     * tipoDato:
     *   BOOLEAN → 0=false, cualquier otro=true
     *   INT16   → signed  (-32768..32767)
     *   UINT16  → unsigned (0..65535)
     */
    private Double interpretarMW(int raw, TipoDato tipoDato) {
        if (tipoDato == null) return (double) raw;
        switch (tipoDato) {
            case BOOLEAN:
                return raw != 0 ? 1.0 : 0.0;
            case INT16:
                return (double) (short) raw; // convierte a signed
            case UINT16:
                return (double) raw; // ya es unsigned por & 0xFFFF
            default:
                LOG.warn("⚠️ tipoDato {} no aplica a PALABRA_MW, se usa raw", tipoDato);
                return (double) raw;
        }
    }

    /**
     * Interpreta un registro MD (32 bits) según el tipoDato configurado.
     *
     * tipoDato:
     *   INT32   → signed  (-2147483648..2147483647)
     *   FLOAT32 → IEEE 754 float (los 32 bits son un float)
     *   UINT16  → solo la parte baja (para compatibilidad)
     */
    private Double interpretarMD(long raw, TipoDato tipoDato) {
        if (tipoDato == null) return (double) raw;
        switch (tipoDato) {
            case INT32: {
                // Convertir a signed 32 bits
                int signed = (int) (raw & 0xFFFFFFFFL);
                return (double) signed;
            }
            case FLOAT32: {
                // Los 32 bits representan un IEEE 754 float
                int bits = (int) (raw & 0xFFFFFFFFL);
                float f = Float.intBitsToFloat(bits);
                return (double) f;
            }
            default:
                LOG.warn("⚠️ tipoDato {} no aplica a PALABRA_DOBLE_MD, se usa raw", tipoDato);
                return (double) raw;
        }
    }

    /**
     * Aplica el factor de escala de la plantilla.
     * Si scalingFactor es null o 0, devuelve el valor sin escalar.
     */
    private Double escalar(Double valor, Double scalingFactor) {
        if (valor == null) return null;
        if (scalingFactor == null || scalingFactor == 0.0) return valor;
        return valor * scalingFactor;
    }
    // ... dentro de tu PollingService ...

}
