package com.etecsa.web.websocket;

import static com.etecsa.config.WebsocketConfiguration.IP_ADDRESS;

import com.etecsa.web.websocket.dto.ActivityDTO;
import java.security.Principal;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Controller
public class ActivityService implements ApplicationListener<SessionDisconnectEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityService.class);

    private final SimpMessageSendingOperations messagingTemplate;

    public ActivityService(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/activity")
    @SendTo("/topic/tracker")
    public ActivityDTO sendActivity(@Payload ActivityDTO activityDTO, StompHeaderAccessor stompHeaderAccessor, Principal principal) {
        try {
            LOG.info(
                "*********************TRACKER********************* - Recibida actividad de {}",
                principal != null ? principal.getName() : "unknown"
            );
            activityDTO.setUserLogin(principal != null ? principal.getName() : "unknown");
            activityDTO.setSessionId(stompHeaderAccessor.getSessionId());
            Object ipAddr = stompHeaderAccessor.getSessionAttributes().get(IP_ADDRESS);
            activityDTO.setIpAddress(ipAddr != null ? ipAddr.toString() : "unknown");
            activityDTO.setTime(Instant.now());
            LOG.info("*********************TRACKER********************* - Enviando {}", activityDTO);
            return activityDTO;
        } catch (Exception e) {
            LOG.error("*********************TRACKER********************* - Error procesando actividad", e);
            return activityDTO;
        }
    }

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setSessionId(event.getSessionId());
        activityDTO.setPage("logout");
        messagingTemplate.convertAndSend("/topic/tracker", activityDTO);
    }
}
