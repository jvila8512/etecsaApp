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
            // Mensajes sin destino (por ejemplo, handshakes internos)
            .nullDestMatcher()
            .authenticated()
            // Solo administradores pueden suscribirse a /topic/tracker
            .simpDestMatchers("/topic/tracker")
            .hasAuthority(AuthoritiesConstants.ADMIN)
            // Cualquier suscripción a /topic/** (incluye /topic/generadores/** y /topic/dashboard)
            // requiere un usuario autenticado (ROLE_USER o superior)
            .simpDestMatchers("/topic/**")
            .authenticated()
            // Permitir el envío de comandos a los endpoints de aplicación
            // /app/generadores/** se usa para escribir en el PLC y conectar equipos
            .simpDestMatchers("/app/generadores/**")
            .authenticated()
            // A partir de aquí, cualquier otro MESSAGE/SUBSCRIBE que no coincida
            // con las reglas anteriores será denegado.
            .simpTypeMatchers(SimpMessageType.MESSAGE, SimpMessageType.SUBSCRIBE)
            .denyAll()
            // catch all final
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
