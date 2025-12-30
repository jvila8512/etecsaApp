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

@Service
public class ModBusService {

    private static final Logger log = LoggerFactory.getLogger(ModBusService.class);

    // Mapa para almacenar múltiples clientes (uno por grupo electrógeno)
    private ConcurrentHashMap<String, ModbusTcpClient> clients = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ConnectionInfo> connectionInfo = new ConcurrentHashMap<>();

    // Configuración por defecto
    private final int DEFAULT_PORT = 502;
    private final int DEFAULT_UNIT_ID = 1;

    // Clase para almacenar información de conexión
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

    // ========== GESTIÓN DE CONEXIONES ==========

    /**
     * CONECTAR a un dispositivo específico
     */
    public boolean connectToGenerator(String generatorId, String host, int port, int unitId) {
        try {
            // Si ya está conectado al mismo dispositivo, mantener conexión
            if (isConnected(generatorId)) {
                ConnectionInfo info = connectionInfo.get(generatorId);
                if (info != null && info.host.equals(host) && info.port == port && info.unitId == unitId) {
                    log.info("✅ Ya conectado a grupo {} - {}:{} (UnitID: {})", generatorId, host, port, unitId);
                    return true;
                }
            }

            // Cerrar conexión anterior si existe
            disconnectGenerator(generatorId);

            // Crear nueva conexión
            var transport = NettyTcpClientTransport.create(cfg -> {
                cfg.setHostname(host);
                cfg.setPort(port);
            });

            ModbusTcpClient client = ModbusTcpClient.create(transport);
            client.connect();

            // Guardar en el mapa
            clients.put(generatorId, client);
            connectionInfo.put(generatorId, new ConnectionInfo(host, port, unitId));

            log.info("✅ Conectado a grupo {} - {}:{} (UnitID: {})", generatorId, host, port, unitId);
            return true;
        } catch (Exception e) {
            log.error("❌ Error conectando a grupo {} - {}:{} - {}", generatorId, host, port, e.getMessage());
            return false;
        }
    }

    /**
     * CONECTAR con parámetros por defecto
     */
    public boolean connectToGenerator(String generatorId, String host) {
        return connectToGenerator(generatorId, host, DEFAULT_PORT, DEFAULT_UNIT_ID);
    }

    /**
     * CONECTAR con IP y puerto por parámetro
     */
    public boolean connectToGenerator(String generatorId, String host, int port) {
        return connectToGenerator(generatorId, host, port, DEFAULT_UNIT_ID);
    }

    /**
     * Verificar si un grupo específico está conectado
     */
    public boolean isConnected(String generatorId) {
        ModbusTcpClient client = clients.get(generatorId);
        return client != null && client.isConnected();
    }

    /**
     * Desconectar un grupo específico
     */
    public void disconnectGenerator(String generatorId) {
        ModbusTcpClient client = clients.get(generatorId);
        if (client != null) {
            try {
                client.disconnect();
                log.info("🔌 Desconectado grupo {}", generatorId);
            } catch (Exception e) {
                log.warn("⚠️ Error al desconectar grupo {}: {}", generatorId, e.getMessage());
            } finally {
                clients.remove(generatorId);
                connectionInfo.remove(generatorId);
            }
        }
    }

    /**
     * Desconectar todos los grupos
     */
    public void disconnectAll() {
        clients.forEach((generatorId, client) -> {
            try {
                client.disconnect();
                log.info("🔌 Desconectado grupo {}", generatorId);
            } catch (Exception e) {
                log.warn("⚠️ Error al desconectar grupo {}: {}", generatorId, e.getMessage());
            }
        });
        clients.clear();
        connectionInfo.clear();
        log.info("🔌 Todos los grupos desconectados");
    }

    /**
     * Obtener información de conexión de un grupo
     */
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

    /**
     * LEER HOLDING REGISTERS de un grupo específico
     */
    public List<Integer> readHoldingRegisters(String generatorId, int address, int quantity) {
        try {
            ModbusTcpClient client = clients.get(generatorId);
            if (client == null || !client.isConnected()) {
                log.warn("⚠️ Grupo {} no conectado", generatorId);
                return Collections.emptyList();
            }

            ConnectionInfo info = connectionInfo.get(generatorId);
            ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(address, quantity);
            ReadHoldingRegistersResponse response = client.readHoldingRegisters(info.unitId, request);

            List<Integer> registers = new ArrayList<>();
            for (int i = 0; i < quantity; i++) {
                registers.add((int) response.registers()[i]);
            }

            log.debug("📖 Grupo {} - Leídos {} registros desde @{}", generatorId, quantity, address);
            return registers;
        } catch (Exception e) {
            log.error("❌ Error leyendo registros en grupo {} @{}: {}", generatorId, address, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * LEER UN SOLO HOLDING REGISTER
     */
    public Integer readHoldingRegister(String generatorId, int address) {
        List<Integer> registers = readHoldingRegisters(generatorId, address, 1);
        return registers.isEmpty() ? null : registers.get(0);
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
}
