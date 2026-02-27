package com.etecsa.config;

import com.etecsa.security.AuthoritiesConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebsocketSecurityConfiguration extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            // 🔹 Conexión inicial requiere autenticación
            .nullDestMatcher()
            .authenticated()
            // 🔹 Admin para tracker
            .simpDestMatchers("/topic/tracker")
            .hasAuthority(AuthoritiesConstants.ADMIN)
            // matches any destination that starts with /topic/
            // (i.e. cannot send messages directly to /topic/)
            // (i.e. cannot subscribe to /topic/messages/* to get messages sent to
            // /topic/messages-user<id>)

            // 🔹 Usuarios autenticados pueden suscribirse a generadores
            .simpSubscribeDestMatchers("/topic/generadores")
            .authenticated()
            // 🔹 Usuarios autenticados pueden enviar mensajes a /app/generadores/**
            .simpDestMatchers("/app/generadores/**")
            .authenticated()
            // 🔹 Resto de topics requieren autenticación (para suscripción)
            .simpDestMatchers("/topic/**")
            .authenticated()
            // message types other than MESSAGE and SUBSCRIBE
            .simpTypeMatchers(SimpMessageType.MESSAGE, SimpMessageType.SUBSCRIBE)
            .denyAll()
            // catch all
            .anyMessage()
            .denyAll();
    }

    /**
     * Disables CSRF for Websockets.
     */
    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
