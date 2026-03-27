package com.etecsa.service.alarma;

import com.etecsa.service.dto.AlarmaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio que envía notificaciones de alarmas por WebSocket.
 * Los clientes suscritos a /topic/alarmas reciben estas notificaciones en tiempo real.
 */
@Service
public class AlarmaWebSocketService {

    private static final Logger log = LoggerFactory.getLogger(AlarmaWebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public AlarmaWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Envía una notificación cuando se crea una nueva alarma.
     */
    public void notificarNuevaAlarma(AlarmaDTO alarma) {
        log.info("Enviando notificación de nueva alarma por WebSocket: id={}, descripcion={}", alarma.getId(), alarma.getDescripcion());
        messagingTemplate.convertAndSend("/topic/alarmas/nueva", alarma);
    }

    /**
     * Envía una notificación cuando se reconoce una alarma.
     */
    public void notificarAlarmaReconocida(AlarmaDTO alarma) {
        log.info("Enviando notificación de alarma reconocida por WebSocket: id={}", alarma.getId());
        messagingTemplate.convertAndSend("/topic/alarmas/reconocida", alarma);
    }

    /**
     * Envía una notificación cuando se resuelve/finaliza una alarma.
     */
    public void notificarAlarmaResuelta(AlarmaDTO alarma) {
        log.info("Enviando notificación de alarma resuelta por WebSocket: id={}", alarma.getId());
        messagingTemplate.convertAndSend("/topic/alarmas/resuelta", alarma);
    }

    /**
     * Envía una actualización general de todas las alarmas activas.
     */
    public void notificarActualizacion(Object payload) {
        messagingTemplate.convertAndSend("/topic/alarmas/actualizacion", payload);
    }
}
