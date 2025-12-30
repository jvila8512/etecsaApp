package com.etecsa.web.rest;

import com.etecsa.service.modbus.ModBusService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generadores")
public class GeneradoresController {

    @Autowired
    private ModBusService modBusService;

    /**
     * Conectar a un grupo específico
     */
    @PostMapping("/{grupoId}/conectar")
    public Map<String, Object> conectarGrupo(
        @PathVariable String grupoId,
        @RequestParam String ip,
        @RequestParam(defaultValue = "502") int port,
        @RequestParam(defaultValue = "1") int unitId
    ) {
        Map<String, Object> response = new HashMap<>();
        boolean conectado = modBusService.connectToGenerator(grupoId, ip, port, unitId);

        response.put("success", conectado);
        response.put("message", conectado ? "✅ Conectado a " + grupoId + " - " + ip : "❌ Error conectando a " + grupoId);
        response.put("grupo", grupoId);
        response.put("ip", ip);
        response.put("port", port);
        response.put("unitId", unitId);

        return response;
    }

    /**
     * Leer coils de un grupo
     */
    @GetMapping("/{grupoId}/leer-coils/{direccion}/{cantidad}")
    public Map<String, Object> leerCoils(@PathVariable String grupoId, @PathVariable int direccion, @PathVariable int cantidad) {
        Map<String, Object> response = new HashMap<>();

        if (!modBusService.isConnected(grupoId)) {
            response.put("success", false);
            response.put("message", "❌ Grupo no conectado: " + grupoId);
            return response;
        }

        List<Boolean> coils = modBusService.readCoils(grupoId, direccion, cantidad);

        response.put("success", !coils.isEmpty());
        response.put("grupo", grupoId);
        response.put("direccion", direccion);
        response.put("cantidad", cantidad);
        response.put("valores", coils);

        return response;
    }

    /**
     * Escribir coil en grupo
     */
    @PostMapping("/{grupoId}/escribir-coil/{direccion}/{valor}")
    public Map<String, Object> escribirCoil(@PathVariable String grupoId, @PathVariable int direccion, @PathVariable boolean valor) {
        Map<String, Object> response = new HashMap<>();

        if (!modBusService.isConnected(grupoId)) {
            response.put("success", false);
            response.put("message", "❌ Grupo no conectado: " + grupoId);
            return response;
        }

        boolean exito = modBusService.writeCoil(grupoId, direccion, valor);

        response.put("success", exito);
        response.put(
            "message",
            exito ? "✅ Escrito M" + direccion + " = " + (valor ? "ON" : "OFF") + " en " + grupoId : "❌ Error escribiendo en " + grupoId
        );
        response.put("grupo", grupoId);
        response.put("direccion", direccion);
        response.put("valor", valor);

        return response;
    }

    /**
     * Estado de conexiones
     */
    @GetMapping("/estado")
    public Map<String, Object> obtenerEstado() {
        Map<String, Object> response = new HashMap<>();

        List<String> conectados = modBusService.getConnectedGenerators();
        Map<String, String> infoConexiones = new HashMap<>();

        conectados.forEach(grupoId -> {
            infoConexiones.put(grupoId, modBusService.getConnectionInfo(grupoId));
        });

        response.put("totalConectados", conectados.size());
        response.put("gruposConectados", conectados);
        response.put("infoConexiones", infoConexiones);
        response.put("estadisticas", modBusService.getServiceStats());

        return response;
    }

    /**
     * Desconectar grupo
     */
    @PostMapping("/{grupoId}/desconectar")
    public Map<String, Object> desconectarGrupo(@PathVariable String grupoId) {
        Map<String, Object> response = new HashMap<>();

        modBusService.disconnectGenerator(grupoId);

        response.put("success", true);
        response.put("message", "🔌 Desconectado grupo: " + grupoId);
        response.put("grupo", grupoId);

        return response;
    }
}
