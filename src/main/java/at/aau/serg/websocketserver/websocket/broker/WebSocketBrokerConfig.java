package at.aau.serg.websocketserver.websocket.broker;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Ziel für @SendTo
        config.setApplicationDestinationPrefixes("/app"); // Prefix für @MessageMapping
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS-Fallback für Browser
        registry.addEndpoint("/websocket-broker")
                .setAllowedOriginPatterns("*")
                .withSockJS(); 
                
        // Direkter WebSocket-Endpunkt für Android
        registry.addEndpoint("/websocket-broker")
                .setAllowedOriginPatterns("*");
    }
}
