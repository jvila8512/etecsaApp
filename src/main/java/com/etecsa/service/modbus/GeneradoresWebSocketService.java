package com.etecsa.service.modbus;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class GeneradoresWebSocketService {

    private static final Logger log = LoggerFactory.getLogger(GeneradoresWebSocketService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ModBusService modBusService;

    // Configuración de grupos electrógenos
    private final Map<String, String> gruposConfig = new ConcurrentHashMap<>();

    // Estadísticas de envíos
    private final AtomicInteger contadorEnvios = new AtomicInteger(0);
    private final Map<String, Long> ultimaConexion = new ConcurrentHashMap<>();

    // Cache para evitar enviar datos idénticos consecutivos
    private final Map<String, Map<String, Object>> cacheDatos = new ConcurrentHashMap<>();
    // Control de conexión automática
    private boolean conexionAutomatica = false;

    public GeneradoresWebSocketService() {
        // Inicializar configuración de grupos
        inicializarConfiguracionGrupos();
    }

    /**
     * Inicializa la configuración de los grupos electrógenos
     */
    private void inicializarConfiguracionGrupos() {
        gruposConfig.put("grupo1", "192.168.1.11");
        gruposConfig.put("grupo2", "192.168.1.12");
        gruposConfig.put("grupo3", "192.168.1.13");
        gruposConfig.put("grupo4", "192.168.1.14");

        log.info("🎯 Configuración de grupos inicializada: {} grupos", gruposConfig.size());
    }

    /**
     * CONECTAR TODOS LOS GRUPOS AUTOMÁTICAMENTE al iniciar
     */
    @PostConstruct
    public void conectarGruposAutomaticamente() {
        log.info("🔌 Intentando conexión automática de grupos...");

        int conectados = 0;
        for (Map.Entry<String, String> entry : gruposConfig.entrySet()) {
            String grupoId = entry.getKey();
            String ip = entry.getValue();

            try {
                boolean conectado = modBusService.connectToGenerator(grupoId, ip);
                if (conectado) {
                    conectados++;
                    log.info("✅ {} conectado - {}", grupoId, ip);

                    // Intentar leer datos inmediatamente para verificar
                    try {
                        List<Boolean> test = modBusService.readCoils(grupoId, 0, 1);
                        log.info("   📖 {} responde correctamente", grupoId);
                    } catch (Exception e) {
                        log.warn("   ⚠️ {} conectado pero no responde: {}", grupoId, e.getMessage());
                    }
                } else {
                    log.warn("❌ {} NO conectado - {}", grupoId, ip);
                }
            } catch (Exception e) {
                log.error("💥 Error grave conectando {}: {}", grupoId, e.getMessage());
            }

            // Pequeña pausa entre conexiones
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }

        log.info("🎯 Resumen conexión: {}/{} grupos operativos", conectados, gruposConfig.size());
        this.conexionAutomatica = true;
    }

    private Map<String, Object> obtenerDatosGrupo(String grupoId) {
        Map<String, Object> datos = new HashMap<>();
        String ip = gruposConfig.get(grupoId);

        datos.put("grupoId", grupoId);
        datos.put("ip", ip);
        datos.put("timestamp", System.currentTimeMillis());

        boolean conectado = modBusService.isConnected(grupoId);
        datos.put("conectado", conectado);

        if (conectado) {
            try {
                // LEER DATOS con timeout
                List<Boolean> memorias = modBusService.readCoils(grupoId, 0, 11);
                List<Integer> registros = modBusService.readHoldingRegisters(grupoId, 0, 5);

                // Verificar que los datos no estén vacíos
                boolean datosValidos = !memorias.isEmpty() && !registros.isEmpty();

                datos.put("memorias", crearMapaMemorias(memorias));
                datos.put("registros", crearMapaRegistros(registros));
                datos.put("estado", datosValidos ? "🟢 OPERATIVO" : "🟡 SIN DATOS");
                datos.put("datosValidos", datosValidos);

                if (datosValidos) {
                    // Interpretar datos específicos
                    datos.put("voltaje", registros.get(0) + " V");
                    datos.put("frecuencia", registros.get(1) + " Hz");
                    datos.put("estadoPrincipal", memorias.get(0) ? "ENCENDIDO" : "APAGADO");
                }
            } catch (Exception e) {
                datos.put("estado", "🔴 ERROR");
                datos.put("datosValidos", false);
                datos.put("error", "Timeout o error de lectura");
                log.warn("⚠️ Error leyendo {}: {}", grupoId, e.getMessage());
            }
        } else {
            datos.put("estado", "⚫ DESCONECTADO");
            datos.put("datosValidos", false);
            datos.put("memorias", new HashMap<>());
            datos.put("registros", new HashMap<>());
        }

        return datos;
    }

    /**
     * ENVÍO AUTOMÁTICO - Datos en tiempo real cada 2 segundos
     */
    @Scheduled(fixedRate = 2000)
    public void enviarDatosTiempoReal() {
        try {
            int numeroEnvio = contadorEnvios.incrementAndGet();
            long inicio = System.currentTimeMillis();

            Map<String, Object> datosDashboard = new HashMap<>();
            datosDashboard.put("type", "TIEMPO_REAL");
            datosDashboard.put("timestamp", inicio);
            datosDashboard.put("numeroEnvio", numeroEnvio);
            datosDashboard.put("totalGrupos", gruposConfig.size());

            Map<String, Object> datosGrupos = new HashMap<>();
            int gruposConectados = 0;
            int gruposConDatos = 0;

            // Obtener datos de cada grupo
            for (String grupoId : gruposConfig.keySet()) {
                Map<String, Object> datosGrupo = obtenerDatosGrupo(grupoId);
                datosGrupos.put(grupoId, datosGrupo);

                if (Boolean.TRUE.equals(datosGrupo.get("conectado"))) {
                    gruposConectados++;
                }
                if (Boolean.TRUE.equals(datosGrupo.get("datosValidos"))) {
                    gruposConDatos++;
                }
            }

            datosDashboard.put("gruposConectados", gruposConectados);
            datosDashboard.put("gruposConDatos", gruposConDatos);
            datosDashboard.put("grupos", datosGrupos);

            // Enviar al BROKER ESPECÍFICO de generadores
            messagingTemplate.convertAndSend("/topic/generadores/tiempo-real", datosDashboard);

            long duracion = System.currentTimeMillis() - inicio;
            log.debug(
                "📤 Envío {} completado: {} grupos, {} conectados, {}ms",
                numeroEnvio,
                gruposConfig.size(),
                gruposConectados,
                duracion
            );
        } catch (Exception e) {
            log.error("❌ Error enviando datos WebSocket: {}", e.getMessage(), e);
        }
    }

    /**
     * Obtiene datos específicos de un grupo electrógeno
     */
    private Map<String, Object> obtenerDatosGrupo1(String grupoId) {
        Map<String, Object> datos = new HashMap<>();
        String ip = gruposConfig.get(grupoId);

        datos.put("grupoId", grupoId);
        datos.put("ip", ip);
        datos.put("timestamp", System.currentTimeMillis());

        try {
            boolean conectado = modBusService.isConnected(grupoId);
            datos.put("conectado", conectado);

            if (conectado) {
                // LEER DATOS DEL PLC
                Map<String, Object> datosPlc = leerDatosPlc(grupoId);
                datos.putAll(datosPlc);
                datos.put("datosValidos", true);

                // Actualizar última conexión exitosa
                ultimaConexion.put(grupoId, System.currentTimeMillis());
            } else {
                // Grupo desconectado
                datos.put("estado", "⚫ DESCONECTADO");
                datos.put("voltaje", "N/A");
                datos.put("frecuencia", "N/A");
                datos.put("temperatura", "N/A");
                datos.put("nivelCombustible", "N/A");
                datos.put("memorias", new HashMap<>());
                datos.put("registros", new HashMap<>());
                datos.put("datosValidos", false);
                datos.put("error", "No conectado al PLC");
            }
        } catch (Exception e) {
            log.error("❌ Error obteniendo datos grupo {}: {}", grupoId, e.getMessage());
            datos.put("datosValidos", false);
            datos.put("error", e.getMessage());
            datos.put("estado", "🔴 ERROR");
        }

        return datos;
    }

    /**
     * Lee datos específicos del PLC (Modbus)
     */
    private Map<String, Object> leerDatosPlc(String grupoId) {
        Map<String, Object> datosPlc = new HashMap<>();

        try {
            // LEER MEMORIAS M0-M10 (Coils)
            List<Boolean> memorias = modBusService.readCoils(grupoId, 0, 11);
            Map<String, Boolean> mapaMemorias = crearMapaMemorias(memorias);
            datosPlc.put("memorias", mapaMemorias);

            // LEER REGISTROS MW0-MW10 (Holding Registers)
            List<Integer> registros = modBusService.readHoldingRegisters(grupoId, 0, 11);
            Map<String, Integer> mapaRegistros = crearMapaRegistros(registros);
            datosPlc.put("registros", mapaRegistros);

            // INTERPRETAR DATOS PARA DASHBOARD
            datosPlc.put("estado", interpretarEstado(mapaMemorias));
            datosPlc.put("voltaje", interpretarVoltaje(mapaRegistros));
            datosPlc.put("frecuencia", interpretarFrecuencia(mapaRegistros));
            datosPlc.put("temperatura", interpretarTemperatura(mapaRegistros));
            datosPlc.put("nivelCombustible", interpretarCombustible(mapaRegistros));
            datosPlc.put("horasFuncionamiento", interpretarHoras(mapaRegistros));

            // DETECTAR ALERTAS
            List<String> alertas = detectarAlertas(grupoId, mapaMemorias, mapaRegistros);
            datosPlc.put("alertas", alertas);
            datosPlc.put("tieneAlertas", !alertas.isEmpty());
        } catch (Exception e) {
            log.error("❌ Error leyendo datos PLC grupo {}: {}", grupoId, e.getMessage());
            throw e;
        }

        return datosPlc;
    }

    /**
     * Crea mapa de memorias M0-M10
     */
    private Map<String, Boolean> crearMapaMemorias(List<Boolean> memorias) {
        Map<String, Boolean> mapa = new HashMap<>();
        for (int i = 0; i < memorias.size(); i++) {
            mapa.put("M" + i, memorias.get(i));
        }
        return mapa;
    }

    /**
     * Crea mapa de registros MW0-MW10
     */
    private Map<String, Integer> crearMapaRegistros(List<Integer> registros) {
        Map<String, Integer> mapa = new HashMap<>();
        for (int i = 0; i < registros.size(); i++) {
            mapa.put("MW" + i, registros.get(i) != null ? registros.get(i) : 0);
        }
        return mapa;
    }

    /**
     * Interpreta el estado del generador basado en las memorias
     */
    private String interpretarEstado(Map<String, Boolean> memorias) {
        if (memorias.get("M0") != null && memorias.get("M0")) {
            return "🟢 ENCENDIDO";
        } else if (memorias.get("M1") != null && memorias.get("M1")) {
            return "🟡 EN MARCHA";
        } else if (memorias.get("M2") != null && memorias.get("M2")) {
            return "🔴 EN ALARMA";
        } else {
            return "⚫ APAGADO";
        }
    }

    /**
     * Interpreta voltaje (MW0)
     */
    private String interpretarVoltaje(Map<String, Integer> registros) {
        Integer voltaje = registros.get("MW0");
        return voltaje != null ? voltaje + " V" : "N/A";
    }

    /**
     * Interpreta frecuencia (MW1)
     */
    private String interpretarFrecuencia(Map<String, Integer> registros) {
        Integer frecuencia = registros.get("MW1");
        return frecuencia != null ? frecuencia + " Hz" : "N/A";
    }

    /**
     * Interpreta temperatura (MW2)
     */
    private String interpretarTemperatura(Map<String, Integer> registros) {
        Integer temp = registros.get("MW2");
        return temp != null ? temp + " °C" : "N/A";
    }

    /**
     * Interpreta nivel de combustible (MW3)
     */
    private String interpretarCombustible(Map<String, Integer> registros) {
        Integer combustible = registros.get("MW3");
        if (combustible == null) return "N/A";

        if (combustible > 80) return "🟢 " + combustible + "%";
        else if (combustible > 30) return "🟡 " + combustible + "%";
        else return "🔴 " + combustible + "%";
    }

    /**
     * Interpreta horas de funcionamiento (MW4)
     */
    private String interpretarHoras(Map<String, Integer> registros) {
        Integer horas = registros.get("MW4");
        return horas != null ? horas + " horas" : "N/A";
    }

    /**
     * Detecta alertas basadas en los datos del PLC
     */
    private List<String> detectarAlertas(String grupoId, Map<String, Boolean> memorias, Map<String, Integer> registros) {
        List<String> alertas = new java.util.ArrayList<>();

        // Alertas de memorias (estados)
        if (memorias.get("M2") != null && memorias.get("M2")) {
            alertas.add("ALARMA ACTIVA - Revisar generador");
        }

        if (memorias.get("M3") != null && memorias.get("M3")) {
            alertas.add("FALLA DETECTADA - Mantenimiento requerido");
        }

        // Alertas de registros (valores)
        Integer voltaje = registros.get("MW0");
        if (voltaje != null && (voltaje < 200 || voltaje > 250)) {
            alertas.add("VOLTAJE FUERA DE RANGO: " + voltaje + "V");
        }

        Integer frecuencia = registros.get("MW1");
        if (frecuencia != null && (frecuencia < 55 || frecuencia > 65)) {
            alertas.add("FRECUENCIA FUERA DE RANGO: " + frecuencia + "Hz");
        }

        Integer temperatura = registros.get("MW2");
        if (temperatura != null && temperatura > 90) {
            alertas.add("TEMPERATURA ALTA: " + temperatura + "°C");
        }

        Integer combustible = registros.get("MW3");
        if (combustible != null && combustible < 20) {
            alertas.add("COMBUSTIBLE BAJO: " + combustible + "%");
        }

        // Si hay alertas, enviarlas inmediatamente
        if (!alertas.isEmpty()) {
            for (String alerta : alertas) {
                enviarAlertaInmediata(grupoId, alerta, "WARNING");
            }
        }

        return alertas;
    }

    /**
     * ENVÍA COMANDO DE ESCRITURA EJECUTADO
     */
    public void enviarComandoEscritura(String grupoId, String tipo, int direccion, Object valor, boolean exito) {
        Map<String, Object> comando = new HashMap<>();
        comando.put("type", "COMANDO_EJECUTADO");
        comando.put("grupoId", grupoId);
        comando.put("tipo", tipo);
        comando.put("direccion", direccion);
        comando.put("valor", valor);
        comando.put("exito", exito);
        comando.put("timestamp", System.currentTimeMillis());
        comando.put("descripcion", generarDescripcionComando(tipo, direccion, valor));

        // Enviar al BROKER ESPECÍFICO de generadores
        messagingTemplate.convertAndSend("/topic/generadores/comandos", comando);

        log.info("📝 Comando {} ejecutado: {} @{} = {} - {}", tipo, grupoId, direccion, valor, exito ? "✅" : "❌");
    }

    /**
     * Genera descripción legible del comando
     */
    private String generarDescripcionComando(String tipo, int direccion, Object valor) {
        switch (tipo) {
            case "COIL":
                return String.format("Memoria M%d = %s", direccion, valor.equals(true) ? "ON" : "OFF");
            case "REGISTRO":
                return String.format("Registro MW%d = %s", direccion, valor);
            case "CONEXION":
                return String.format("Conexión: %s", valor);
            default:
                return String.format("%s @%d = %s", tipo, direccion, valor);
        }
    }

    /**
     * ENVÍA ALERTA INMEDIATA
     */
    public void enviarAlerta(String grupoId, String mensaje, String tipo) {
        Map<String, Object> alerta = new HashMap<>();
        alerta.put("type", "ALERTA");
        alerta.put("grupoId", grupoId);
        alerta.put("mensaje", mensaje);
        alerta.put("tipo", tipo); // INFO, WARNING, ERROR, CRITICAL
        alerta.put("timestamp", System.currentTimeMillis());
        alerta.put("nivel", obtenerNivelAlerta(tipo));

        // Enviar al BROKER ESPECÍFICO de generadores
        messagingTemplate.convertAndSend("/topic/generadores/alertas", alerta);

        log.warn("🚨 Alerta {}: {} - {}", tipo, grupoId, mensaje);
    }

    /**
     * Envía alerta inmediata (más rápido, sin log)
     */
    private void enviarAlertaInmediata(String grupoId, String mensaje, String tipo) {
        Map<String, Object> alerta = new HashMap<>();
        alerta.put("type", "ALERTA");
        alerta.put("grupoId", grupoId);
        alerta.put("mensaje", mensaje);
        alerta.put("tipo", tipo);
        alerta.put("timestamp", System.currentTimeMillis());
        alerta.put("inmediata", true);

        messagingTemplate.convertAndSend("/topic/generadores/alertas", alerta);
    }

    /**
     * Obtiene nivel numérico de alerta
     */
    private int obtenerNivelAlerta(String tipo) {
        switch (tipo) {
            case "INFO":
                return 1;
            case "WARNING":
                return 2;
            case "ERROR":
                return 3;
            case "CRITICAL":
                return 4;
            default:
                return 1;
        }
    }

    /**
     * ENVÍA DATOS DE LECTURA INMEDIATA
     */
    public void enviarDatosInmediatos(String grupoId, Map<String, Object> datos) {
        datos.put("type", "LECTURA_INMEDIATA");
        datos.put("grupoId", grupoId);
        datos.put("timestamp", System.currentTimeMillis());
        datos.put("solicitadoPor", "usuario");

        // Enviar al BROKER ESPECÍFICO de generadores
        messagingTemplate.convertAndSend("/topic/generadores/datos-inmediatos", datos);

        log.debug("📖 Lectura inmediata enviada para grupo: {}", grupoId);
    }

    /**
     * ENVÍA ESTADO DE CONEXIÓN
     */
    public void enviarEstadoConexion(String grupoId, boolean conectado, String mensaje) {
        Map<String, Object> estado = new HashMap<>();
        estado.put("type", "ESTADO_CONEXION");
        estado.put("grupoId", grupoId);
        estado.put("conectado", conectado);
        estado.put("mensaje", mensaje);
        estado.put("timestamp", System.currentTimeMillis());
        estado.put("ip", gruposConfig.get(grupoId));

        // Enviar al BROKER ESPECÍFICO de generadores
        messagingTemplate.convertAndSend("/topic/generadores/estado-conexion", estado);

        log.info("🔌 Estado conexión {}: {} - {}", grupoId, conectado ? "CONECTADO" : "DESCONECTADO", mensaje);
    }

    /**
     * ENVÍA ESTADÍSTICAS DEL SISTEMA
     */
    @Scheduled(fixedRate = 30000) // Cada 30 segundos
    public void enviarEstadisticasSistema() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("type", "ESTADISTICAS");
        stats.put("timestamp", System.currentTimeMillis());
        stats.put("totalEnvios", contadorEnvios.get());
        stats.put("totalGrupos", gruposConfig.size());

        // Calcular grupos conectados
        long gruposConectados = gruposConfig.keySet().stream().filter(grupoId -> modBusService.isConnected(grupoId)).count();
        stats.put("gruposConectados", gruposConectados);

        // Tiempo de actividad
        stats.put("tiempoActividad", System.currentTimeMillis() - getTiempoInicio());

        messagingTemplate.convertAndSend("/topic/generadores/estadisticas", stats);

        log.debug("📈 Estadísticas enviadas: {}/{} grupos conectados", gruposConectados, gruposConfig.size());
    }

    /**
     * Obtiene tiempo de inicio (para estadísticas)
     */
    private long getTiempoInicio() {
        // En una implementación real, esto vendría de un bean de aplicación
        return System.currentTimeMillis() - (5 * 60 * 1000); // Simular 5 minutos
    }

    /**
     * AGREGAR NUEVO GRUPO DINÁMICAMENTE
     */
    public boolean agregarGrupo(String grupoId, String ip) {
        if (gruposConfig.containsKey(grupoId)) {
            log.warn("⚠️ Grupo {} ya existe", grupoId);
            return false;
        }

        gruposConfig.put(grupoId, ip);
        log.info("➕ Grupo agregado: {} - {}", grupoId, ip);

        // Enviar actualización de configuración
        enviarActualizacionConfiguracion();

        return true;
    }

    /**
     * ELIMINAR GRUPO
     */
    public boolean eliminarGrupo(String grupoId) {
        if (!gruposConfig.containsKey(grupoId)) {
            log.warn("⚠️ Grupo {} no existe", grupoId);
            return false;
        }

        // Desconectar si está conectado
        modBusService.disconnectGenerator(grupoId);

        gruposConfig.remove(grupoId);
        log.info("➖ Grupo eliminado: {}", grupoId);

        // Enviar actualización de configuración
        enviarActualizacionConfiguracion();

        return true;
    }

    /**
     * ENVÍA ACTUALIZACIÓN DE CONFIGURACIÓN
     */
    private void enviarActualizacionConfiguracion() {
        Map<String, Object> config = new HashMap<>();
        config.put("type", "CONFIGURACION_ACTUALIZADA");
        config.put("timestamp", System.currentTimeMillis());
        config.put("grupos", new HashMap<>(gruposConfig));
        config.put("totalGrupos", gruposConfig.size());

        messagingTemplate.convertAndSend("/topic/generadores/configuracion", config);

        log.info("⚙️ Configuración actualizada enviada: {} grupos", gruposConfig.size());
    }

    /**
     * OBTIENE CONFIGURACIÓN DE GRUPOS (para el controlador)
     */
    public Map<String, String> getGruposConfig() {
        return new HashMap<>(gruposConfig);
    }

    /**
     * OBTIENE ESTADÍSTICAS DEL SERVICIO
     */
    public Map<String, Object> getEstadisticasServicio() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEnvios", contadorEnvios.get());
        stats.put("totalGrupos", gruposConfig.size());
        stats.put("gruposConectados", gruposConfig.keySet().stream().filter(grupoId -> modBusService.isConnected(grupoId)).count());
        stats.put("ultimaActualizacion", System.currentTimeMillis());

        return stats;
    }

    /**
     * LIMPIAR CACHE (para testing o mantenimiento)
     */
    public void limpiarCache() {
        cacheDatos.clear();
        log.info("🧹 Cache de datos limpiado");
    }
}
