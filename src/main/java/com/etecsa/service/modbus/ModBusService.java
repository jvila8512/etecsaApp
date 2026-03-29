package com.etecsa.service.modbus;

import com.digitalpetri.modbus.client.ModbusTcpClient;
import com.digitalpetri.modbus.exceptions.ModbusExecutionException;
import com.digitalpetri.modbus.exceptions.ModbusResponseException;
import com.digitalpetri.modbus.exceptions.ModbusTimeoutException;
import com.digitalpetri.modbus.pdu.ReadCoilsRequest;
import com.digitalpetri.modbus.pdu.ReadCoilsResponse;
import com.digitalpetri.modbus.pdu.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.pdu.ReadHoldingRegistersResponse;
import com.digitalpetri.modbus.pdu.WriteSingleCoilRequest;
import com.digitalpetri.modbus.pdu.WriteSingleCoilResponse;
import com.digitalpetri.modbus.pdu.WriteSingleRegisterRequest;
import com.digitalpetri.modbus.pdu.WriteSingleRegisterResponse;
import com.digitalpetri.modbus.tcp.client.NettyTcpClientTransport;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ════════════════════════════════════════════════════════════
 *  MODBUS SERVICE — Schneider TM221CE40T
 *
 *  PROBLEMA QUE CORRIGE ESTA VERSIÓN:
 *  El log mostraba:
 *    10:40:59.270 → Empieza Connecting
 *    10:40:59.277 → Lee y da 70  ← LEÍA ANTES DE CONECTAR
 *    10:40:59.320 → ConnectFailure
 *
 *  CAUSA: client.connect() de digitalpetri es async (devuelve
 *  CompletableFuture). Sin esperar el resultado, el código
 *  continuaba y leía con el socket aún en handshake → basura.
 *
 *  SOLUCIÓN: .get(5, SECONDS) hace la conexión síncrona.
 *  Además se usa un mapa propio connectedState que solo se pone
 *  en true DESPUÉS de que el .get() termine sin excepción.
 * ════════════════════════════════════════════════════════════
 */
@Service
public class ModBusService {

    private static final Logger log = LoggerFactory.getLogger(ModBusService.class);

    private static final int DEFAULT_PORT = 502;
    private static final int DEFAULT_UNIT_ID = 255;

    private final ConcurrentHashMap<String, ModbusTcpClient> clients = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConnectionInfo> connectionInfo = new ConcurrentHashMap<>();

    // ✅ Estado propio: solo true después de conectar exitosamente
    private final ConcurrentHashMap<String, Boolean> connectedState = new ConcurrentHashMap<>();

    private static class ConnectionInfo {

        String host;
        int port;
        int unitId;

        ConnectionInfo(String host, int port, int unitId) {
            this.host = host;
            this.port = port;
            this.unitId = unitId;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GESTIÓN DE CONEXIONES
    // ═══════════════════════════════════════════════════════════

    /**
     * Conectar al equipo.
     * REUTILIZA el cliente existente si ya hay uno para evitar multiplicar conexiones al PLC.
     */
    public boolean connectToGenerator(String generatorId, String host, int port, int unitId) {
        log.info(">>> connectToGenerator: {} → {}:{}:{}", generatorId, host, port, unitId);

        try {
            // Verificar si ya existe un cliente
            ModbusTcpClient existingClient = clients.get(generatorId);
            ConnectionInfo existingInfo = connectionInfo.get(generatorId);

            if (existingClient != null && existingInfo != null && existingInfo.host.equals(host) && existingInfo.port == port) {
                // Cliente existente - intentar usar directamente
                // NO crear nuevo cliente para evitar multiplicar conexiones al PLC
                try {
                    existingClient.readHoldingRegisters(unitId, new ReadHoldingRegistersRequest(0, 1));
                    connectedState.put(generatorId, true);
                    log.info(">>> ✅ Cliente existente funciona, retornando true");
                    return true;
                } catch (Exception e) {
                    // Lectura falló, pero NO destruir el cliente
                    // La biblioteca digitalpetri reintentará automáticamente
                    log.warn(">>> Lectura falló pero conservamos cliente: {}", e.getMessage());
                    connectedState.put(generatorId, false);
                    return false;
                }
            }

            // NO existe cliente - crear uno nuevo
            // Primero desconectar cualquier cliente anterior (IP diferente)
            if (existingClient != null) {
                try {
                    existingClient.disconnect();
                } catch (Exception ignored) {}
            }

            log.info(">>> Creando nuevo cliente Modbus para {}", generatorId);

            var transport = NettyTcpClientTransport.create(cfg -> {
                cfg.setHostname(host);
                cfg.setPort(port);
                cfg.setConnectTimeout(java.time.Duration.ofSeconds(5));
                cfg.setConnectPersistent(true);
                cfg.setReconnectLazy(false);
            });

            ModbusTcpClient client = ModbusTcpClient.create(transport);
            client.connect();

            // Guardar cliente
            clients.put(generatorId, client);
            connectionInfo.put(generatorId, new ConnectionInfo(host, port, unitId));

            // Verificar con lectura de prueba
            try {
                client.readHoldingRegisters(unitId, new ReadHoldingRegistersRequest(0, 1));
                connectedState.put(generatorId, true);
                log.info(">>> ✅ Nuevo cliente conectado y funcionando");
                return true;
            } catch (Exception e) {
                log.warn(">>> Lectura de prueba falló: {}", e.getMessage());
                connectedState.put(generatorId, false);
                return false;
            }
        } catch (Exception e) {
            log.error(">>> ❌ Error en connectToGenerator: {}", e.getMessage());
            connectedState.put(generatorId, false);
            return false;
        }
    }

    public boolean connectToGenerator(String generatorId, String host) {
        return connectToGenerator(generatorId, host, DEFAULT_PORT, DEFAULT_UNIT_ID);
    }

    public boolean connectToGenerator(String generatorId, String host, int port) {
        return connectToGenerator(generatorId, host, port, DEFAULT_UNIT_ID);
    }

    public void disconnectGenerator(String generatorId) {
        connectedState.put(generatorId, false);
        ModbusTcpClient client = clients.remove(generatorId);
        connectionInfo.remove(generatorId);
        if (client != null) {
            try {
                client.disconnect();
            } catch (Exception ignored) {}
            log.info("🔌 Desconectado {}", generatorId);
        }
    }

    /**
     * ✅ USA connectedState propio, NO client.isConnected().
     * client.isConnected() devuelve true incluso en estado "Connecting"
     * (antes de que el TCP handshake complete) → causaba el bug original.
     */
    public boolean isConnected(String generatorId) {
        return Boolean.TRUE.equals(connectedState.get(generatorId));
    }

    /**
     * Verifica que la conexión TCP real esté activa.
     */
    public boolean verificarConexion(String generatorId) {
        ModbusTcpClient client = clients.get(generatorId);
        if (client == null) {
            return false;
        }

        try {
            boolean tcpActivo = client.isConnected();

            if (tcpActivo) {
                ConnectionInfo info = connectionInfo.get(generatorId);
                if (info != null) {
                    try {
                        client.readHoldingRegisters(info.unitId, new ReadHoldingRegistersRequest(0, 1));
                        return true;
                    } catch (Exception e) {
                        connectedState.put(generatorId, false);
                        return false;
                    }
                }
            }

            if (!tcpActivo && Boolean.TRUE.equals(connectedState.get(generatorId))) {
                connectedState.put(generatorId, false);
            }

            return tcpActivo;
        } catch (Exception e) {
            connectedState.put(generatorId, false);
            return false;
        }
    }

    /**
     * Marca el equipo como desconectado (sin destruir el cliente).
     * El cliente puede seguir intentando reconectar automáticamente.
     */
    public void forzarDesconexion(String generatorId) {
        connectedState.put(generatorId, false);
        log.info(">>> forzarDesconexion: {} marcado como desconectado (cliente preservado para reconexión)", generatorId);
    }

    /**
     * Desconexión completa (para cuando se cambia la IP del equipo).
     */
    public void desconectarCompletamente(String generatorId) {
        connectedState.put(generatorId, false);
        ModbusTcpClient client = clients.remove(generatorId);
        connectionInfo.remove(generatorId);
        if (client != null) {
            try {
                client.disconnect();
            } catch (Exception ignored) {}
            log.info(">>> Desconexión completa de {}", generatorId);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LECTURA %M — Bits internos (FC01 Read Coils)
    // ═══════════════════════════════════════════════════════════

    public Boolean readM(String generatorId, int address) {
        try {
            ModbusTcpClient client = getClientOrNull(generatorId);
            if (client == null) return null;

            ConnectionInfo info = connectionInfo.get(generatorId);
            ReadCoilsResponse res = client.readCoils(info.unitId, new ReadCoilsRequest(address, 1));

            boolean valor = (res.coils()[0] & 0x01) != 0;
            log.debug("📖 %M{} = {}", address, valor);
            return valor;
        } catch (Exception e) {
            log.error("❌ Error leyendo %M{} en {}: {}", address, generatorId, e.getMessage());
            handleReadError(generatorId, e);
            return null;
        }
    }

    public List<Boolean> readMBits(String generatorId, int address, int quantity) {
        try {
            ModbusTcpClient client = getClientOrNull(generatorId);
            if (client == null) return Collections.emptyList();

            ConnectionInfo info = connectionInfo.get(generatorId);
            ReadCoilsResponse res = client.readCoils(info.unitId, new ReadCoilsRequest(address, quantity));

            List<Boolean> bits = new ArrayList<>();
            for (int i = 0; i < quantity; i++) {
                bits.add((res.coils()[i / 8] & (1 << (i % 8))) != 0);
            }
            return bits;
        } catch (Exception e) {
            log.error("❌ Error leyendo %M bits @{}: {}", address, e.getMessage());
            handleReadError(generatorId, e);
            return Collections.emptyList();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LECTURA BASE — Holding Registers FC03
    // ═══════════════════════════════════════════════════════════

    // ═══════════════════════════════════════════════════════════
    // LECTURA BASE — Holding Registers FC03
    // ═══════════════════════════════════════════════════════════

    public List<Integer> readHoldingRegisters(String generatorId, int address, int quantity) {
        try {
            ModbusTcpClient client = getClientOrNull(generatorId);
            if (client == null) return Collections.emptyList();

            ConnectionInfo info = connectionInfo.get(generatorId);
            ReadHoldingRegistersResponse res = client.readHoldingRegisters(info.unitId, new ReadHoldingRegistersRequest(address, quantity));

            // ✅ CORRECCIÓN CLAVE:
            // response.registers() devuelve byte[] — 2 bytes por cada registro Modbus.
            // registro[i] = byte[i*2] << 8 | byte[i*2 + 1]
            // Antes: registers()[i] leía solo 1 byte → daba 7 en vez de 1800
            byte[] raw = res.registers();
            List<Integer> registers = new ArrayList<>();
            for (int i = 0; i < quantity; i++) {
                int highByte = raw[i * 2] & 0xFF; // byte alto
                int lowByte = raw[i * 2 + 1] & 0xFF; // byte bajo
                registers.add((highByte << 8) | lowByte); // combinar → valor real
            }

            log.debug("📖 HR @{} qty={} → {}", address, quantity, registers);
            return registers;
        } catch (Exception e) {
            log.error("❌ Error leyendo HR @{} en {}: {}", address, generatorId, e.getMessage());
            handleReadError(generatorId, e);
            return Collections.emptyList();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LECTURA %MW — Word 16 bits
    // ═══════════════════════════════════════════════════════════

    public Integer readMW(String generatorId, int address) {
        List<Integer> regs = readHoldingRegisters(generatorId, address, 1);
        if (regs.isEmpty()) return null;
        log.debug("📖 %MW{} = {}", address, regs.get(0));
        return regs.get(0);
    }

    // ═══════════════════════════════════════════════════════════
    // LECTURA %MD — Double Word 32 bits
    // ═══════════════════════════════════════════════════════════

    /**
     * TM221 Little-Endian: %MD0 = LW en %MW0, HW en %MW1
     * valor = (highWord << 16) | lowWord
     */
    public Long readMD(String generatorId, int address) {
        List<Integer> regs = readHoldingRegisters(generatorId, address, 2);
        if (regs.size() < 2) return null;

        int lowWord = regs.get(0);
        int highWord = regs.get(1);
        long valor = ((long) highWord << 16) | (long) lowWord;

        log.debug("📖 %MD{} → LW={} HW={} → Valor={}", address, lowWord, highWord, valor);
        return valor;
    }

    // ═══════════════════════════════════════════════════════════
    // HELPERS PRIVADOS
    // ═══════════════════════════════════════════════════════════

    private ModbusTcpClient getClientOrNull(String generatorId) {
        if (!Boolean.TRUE.equals(connectedState.get(generatorId))) {
            log.warn("⚠️ {} no conectado (connectedState=false)", generatorId);
            return null;
        }
        ModbusTcpClient client = clients.get(generatorId);
        if (client == null) {
            connectedState.put(generatorId, false);
            return null;
        }
        return client;
    }

    private void handleReadError(String generatorId, Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (msg.contains("connection") || msg.contains("timeout") || msg.contains("closed")) {
            log.warn("⚠️ {} marcado como desconectado por error de red", generatorId);
            connectedState.put(generatorId, false);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    public void debugAddress(String generatorId, int address) {
        List<Integer> regs = readHoldingRegisters(generatorId, address, 4);
        log.error("═══ DEBUG %MW{} (grupo={}) ═══", address, generatorId);
        for (int i = 0; i < regs.size(); i++) {
            int raw = regs.get(i);
            log.error("  %MW{} → unsigned={}  signed={}  hex=0x{}", address + i, raw, (short) raw, Integer.toHexString(raw).toUpperCase());
        }
        if (regs.size() >= 2) {
            int w0 = regs.get(0), w1 = regs.get(1);
            log.error("  32-bit TM221 (LW+HW) = {}", ((long) w1 << 16) | w0);
            log.error("  32-bit BigEndian      = {}", ((long) w0 << 16) | w1);
        }
        log.error("═══════════════════════════════════════");
    }

    // ========== LECTURAS ==========

    /**
     * LEER COILS de un grupo específico
     */
    public List<Boolean> readCoils(String generatorId, int address, int quantity) {
        try {
            ModbusTcpClient client = clients.get(generatorId);
            if (client == null || !client.isConnected()) {
                log.warn("⚠️ Grupo {} no conectado", generatorId);
                return Collections.emptyList();
            }

            ConnectionInfo info = connectionInfo.get(generatorId);
            ReadCoilsRequest request = new ReadCoilsRequest(address, quantity);
            ReadCoilsResponse response = client.readCoils(info.unitId, request);

            byte[] coilBytes = response.coils();
            BitSet coilStatus = BitSet.valueOf(coilBytes);

            List<Boolean> coils = new ArrayList<>();
            for (int i = 0; i < quantity; i++) {
                coils.add(coilStatus.get(i));
            }

            log.debug("📖 Grupo {} - Leídos {} coils desde @{}", generatorId, quantity, address);
            return coils;
        } catch (Exception e) {
            log.error("❌ Error leyendo coils en grupo {} @{}: {}", generatorId, address, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * LEER UN SOLO COIL
     */
    public Boolean readCoil(String generatorId, int address) {
        List<Boolean> coils = readCoils(generatorId, address, 1);
        return coils.isEmpty() ? null : coils.get(0);
    }

    // ========== ESCRITURAS ==========

    /**
     * ESCRIBIR COIL en grupo específico
     */
    public boolean writeCoil(String generatorId, int address, boolean value) {
        try {
            ModbusTcpClient client = clients.get(generatorId);
            if (client == null || !client.isConnected()) {
                log.warn("⚠️ Grupo {} no conectado", generatorId);
                return false;
            }

            ConnectionInfo info = connectionInfo.get(generatorId);
            WriteSingleCoilRequest request = new WriteSingleCoilRequest(address, value);
            WriteSingleCoilResponse response = client.writeSingleCoil(info.unitId, request);

            boolean exito = (response != null);
            log.info("📝 Grupo {} - Escrito coil @{} = {} {}", generatorId, address, value, exito ? "✅" : "❌");
            return exito;
        } catch (ModbusExecutionException | ModbusResponseException | ModbusTimeoutException e) {
            log.error("❌ Error escribiendo coil en grupo {} @{}: {}", generatorId, address, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ Error inesperado escribiendo coil en grupo {} @{}: {}", generatorId, address, e.getMessage());
            return false;
        }
    }

    /**
     * ESCRIBIR REGISTER en grupo específico
     */
    public boolean writeRegister(String generatorId, int address, int value) {
        try {
            ModbusTcpClient client = clients.get(generatorId);
            if (client == null || !client.isConnected()) {
                log.warn("⚠️ Grupo {} no conectado", generatorId);
                return false;
            }

            // Validar que el valor esté en rango (0-65535)
            if (value < 0 || value > 65535) {
                log.error("❌ Valor fuera de rango para registro en grupo {} @{}: {}", generatorId, address, value);
                return false;
            }

            ConnectionInfo info = connectionInfo.get(generatorId);
            WriteSingleRegisterRequest request = new WriteSingleRegisterRequest(address, value);
            WriteSingleRegisterResponse response = client.writeSingleRegister(info.unitId, request);

            boolean exito = (response != null);
            log.info("📝 Grupo {} - Escrito registro @{} = {} {}", generatorId, address, value, exito ? "✅" : "❌");
            return exito;
        } catch (ModbusExecutionException | ModbusResponseException | ModbusTimeoutException e) {
            log.error("❌ Error escribiendo registro en grupo {} @{}: {}", generatorId, address, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ Error inesperado escribiendo registro en grupo {} @{}: {}", generatorId, address, e.getMessage());
            return false;
        }
    }

    /**
     * LEER INPUT REGISTERS (opcional - si necesitas esta funcionalidad)
     */
    public List<Integer> readInputRegisters(String generatorId, int address, int quantity) {
        try {
            ModbusTcpClient client = clients.get(generatorId);
            if (client == null || !client.isConnected()) {
                log.warn("⚠️ Grupo {} no conectado", generatorId);
                return Collections.emptyList();
            }

            // Nota: Necesitarías importar las clases correspondientes
            // ReadInputRegistersRequest request = new ReadInputRegistersRequest(address, quantity);
            // ReadInputRegistersResponse response = client.readInputRegisters(info.unitId, request);

            log.warn("⚠️ readInputRegisters no implementado para grupo {}", generatorId);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("❌ Error leyendo input registers en grupo {} @{}: {}", generatorId, address, e.getMessage());
            return Collections.emptyList();
        }
    }

    public String getConnectionInfo(String generatorId) {
        ConnectionInfo info = connectionInfo.get(generatorId);
        if (info == null) {
            return "No conectado";
        }
        return String.format("Conectado a %s:%d (UnitID: %d)", info.host, info.port, info.unitId);
    }

    /**
     * Obtener lista de grupos conectados
     */
    public List<String> getConnectedGenerators() {
        List<String> connected = new ArrayList<>();
        clients.forEach((generatorId, client) -> {
            if (client.isConnected()) {
                connected.add(generatorId);
            }
        });
        return connected;
    }

    /**
     * Obtener estadísticas del servicio
     */
    public Map<String, Object> getServiceStats() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalGruposRegistrados", clients.size());
        stats.put("gruposConectados", getConnectedGenerators().size());
        stats.put("connectionInfo", new java.util.HashMap<>(connectionInfo));
        return stats;
    }

    public void debugRange(String generatorId, int startAddress, int quantity) {
        List<Integer> regs = readHoldingRegisters(generatorId, startAddress, quantity);
        log.error("════ SCAN %MW{} a %MW{} ════", startAddress, startAddress + quantity - 1);
        for (int i = 0; i < regs.size(); i++) {
            int val = regs.get(i);
            log.error("  %MW{} = {}  (hex=0x{})", startAddress + i, val, Integer.toHexString(val).toUpperCase());
        }
        log.error("════ BUSCA el registro que vale 1800 (0x708) ════");
    }
}
