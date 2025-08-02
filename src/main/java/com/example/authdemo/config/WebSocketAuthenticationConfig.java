package com.example.authdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketAuthenticationConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        // This configuration ensures that any connection to the message broker requires authentication.
        // It secures your WebSocket endpoints so only logged-in users can subscribe and receive messages.
        messages
                .nullDestMatcher().authenticated() // Requires authentication for messages with no destination
                .simpSubscribeDestMatchers("/user/queue/updates").authenticated() // Secures the specific user queue
                .anyMessage().authenticated(); // Requires authentication for any other message
    }

    @Override
    protected boolean sameOriginDisabled() {
        // We will handle origin protection with our CORS configuration, so we can disable this here.
        return true;
    }
}