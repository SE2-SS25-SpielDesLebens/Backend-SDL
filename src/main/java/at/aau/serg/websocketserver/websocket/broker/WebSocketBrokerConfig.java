package at.aau.serg.websocketserver.websocket.broker;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); // Ziel für @SendTo
        config.setApplicationDestinationPrefixes("/app"); // Prefix für @MessageMapping
        config.setUserDestinationPrefix("/user"); //Prefix für User-spezifische Kommunikation
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket-broker")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new CustomHandshakeHandler())
                .withSockJS(); // SockJS-Fallback aktivieren
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new StompLoginInterceptor());
    }

    private static class CustomHandshakeHandler extends DefaultHandshakeHandler {
        @Override
        protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                          Map<String, Object> attributes) {

            // Holt den "login"-Header vom Client
            String login = getNativeHeader(request, "login");
            return () -> login != null ? login : UUID.randomUUID().toString();
        }

        private String getNativeHeader(ServerHttpRequest request, String name) {
            List<String> headers = request.getHeaders().get(name);
            return (headers != null && !headers.isEmpty()) ? headers.get(0) : null;
        }
    }
}
