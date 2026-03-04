package com.etecsa.web;

import com.etecsa.domain.EventoEquipo;
import com.etecsa.service.dto.ModbusCommandResponse;
import com.etecsa.service.dto.ModbusWriteCommand;
import com.etecsa.service.modbus.ModBusService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ModbusWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ModbusWebSocketController.class);

    private final ModBusService modBusService;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.etecsa.repository.EventoEquipoRepository eventoEquipoRepository;
    private final com.etecsa.service.modbus.GeneradoresWebSocketService generadoresWebSocketService;

    public ModbusWebSocketController(
        ModBusService modBusService,
        SimpMessagingTemplate messagingTemplate,
        com.etecsa.repository.EventoEquipoRepository eventoEquipoRepository,
        com.etecsa.service.modbus.GeneradoresWebSocketService generadoresWebSocketService
    ) {
        this.modBusService = modBusService;
        this.messagingTemplate = messagingTemplate;
        this.eventoEquipoRepository = eventoEquipoRepository;
        this.generadoresWebSocketService = generadoresWebSocketService;
    }

    /**
     * 📝 ESCRIBIR COIL
     * Flutter envía a: /app/generadores/writeCoil
     * Responde a: /topic/generadores
     */
    @MessageMapping("/generadores/writeCoil")
    @SendTo("/topic/generadores")
    public ModbusCommandResponse writeCoil(ModbusWriteCommand command) {
        log.info(
            "📨 Recibido comando WRITE COIL - Generador: {}, Address: {}, Value: {}",
            command.getGeneratorId(),
            command.getAddress(),
            command.getBooleanValue()
        );

        try {
            boolean success = modBusService.writeCoil(command.getGeneratorId(), command.getAddress(), command.getBooleanValue());

            // si la escritura fue exitosa, actualizamos también el valor en la tabla
            if (success) {
                try {
                    EventoEquipo ev = eventoEquipoRepository.findByEquipoIdAndDireccionModbus(
                        Long.parseLong(command.getGeneratorId()),
                        command.getAddress()
                    );
                    if (ev != null) {
                        ev.setValorBooleano(command.getBooleanValue());
                        eventoEquipoRepository.save(ev);
                    }
                } catch (Exception x) {
                    log.warn("⚠️ No pude actualizar evento_equipo tras writeCoil: {}", x.getMessage());
                }
                // enviar evento para que el front reciba confirmación
                generadoresWebSocketService.enviarComandoEscritura(
                    command.getGeneratorId(),
                    "COIL",
                    command.getAddress(),
                    command.getBooleanValue(),
                    true
                );
            }

            String message = success ? "✅ Coil escrito exitosamente" : "❌ Error escribiendo coil";

            return new ModbusCommandResponse(command.getGeneratorId(), "COIL", command.getAddress(), success, message);
        } catch (Exception e) {
            log.error("❌ Error procesando writeCoil: {}", e.getMessage());
            return new ModbusCommandResponse(command.getGeneratorId(), "COIL", command.getAddress(), false, "Error: " + e.getMessage());
        }
    }

    /**
     * 📝 ESCRIBIR REGISTER
     * Flutter envía a: /app/generadores/writeRegister
     * Responde a: /topic/generadores
     */
    @MessageMapping("/generadores/writeRegister")
    @SendTo("/topic/generadores")
    public ModbusCommandResponse writeRegister(ModbusWriteCommand command) {
        log.info(
            "📨 Recibido comando WRITE REGISTER - Generador: {}, Address: {}, Value: {}",
            command.getGeneratorId(),
            command.getAddress(),
            command.getValue()
        );

        try {
            boolean success = modBusService.writeRegister(command.getGeneratorId(), command.getAddress(), command.getValue());

            // similar al coil, actualizar DB y emitir evento
            if (success) {
                try {
                    EventoEquipo ev = eventoEquipoRepository.findByEquipoIdAndDireccionModbus(
                        Long.parseLong(command.getGeneratorId()),
                        command.getAddress()
                    );
                    if (ev != null) {
                        // el tipo en la entidad es Double, convertir explícitamente
                        ev.setValorNumerico(Double.valueOf(command.getValue()));
                        eventoEquipoRepository.save(ev);
                    }
                } catch (Exception x) {
                    log.warn("⚠️ No pude actualizar evento_equipo tras writeRegister: {}", x.getMessage());
                }
                generadoresWebSocketService.enviarComandoEscritura(
                    command.getGeneratorId(),
                    "REGISTER",
                    command.getAddress(),
                    command.getValue(),
                    true
                );
            }
            // devolver respuesta según resultado de éxito
            String message = success ? "✅ Register escrito exitosamente" : "❌ Error escribiendo register";
            return new ModbusCommandResponse(command.getGeneratorId(), "REGISTER", command.getAddress(), success, message);
        } catch (Exception e) {
            log.error("❌ Error procesando writeRegister: {}", e.getMessage());
            return new ModbusCommandResponse(command.getGeneratorId(), "REGISTER", command.getAddress(), false, "Error: " + e.getMessage());
        }
    }

    /**
     * 🔌 CONECTAR A GENERADOR
     * Flutter envía a: /app/generadores/connect
     */
    @MessageMapping("/generadores/connect")
    @SendTo("/topic/generadores")
    public ModbusCommandResponse connectGenerator(Map<String, Object> payload) {
        String generatorId = (String) payload.get("generatorId");
        String host = (String) payload.get("host");
        int port = (int) payload.getOrDefault("port", 502);
        int unitId = (int) payload.getOrDefault("unitId", 1);

        log.info("📨 Conectando a generador {} - {}:{}", generatorId, host, port);

        boolean success = modBusService.connectToGenerator(generatorId, host, port, unitId);

        return new ModbusCommandResponse(generatorId, "CONNECT", 0, success, success ? "✅ Conectado exitosamente" : "❌ Error conectando");
    }
}
