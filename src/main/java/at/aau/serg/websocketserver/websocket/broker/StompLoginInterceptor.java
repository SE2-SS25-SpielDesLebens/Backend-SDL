package at.aau.serg.websocketserver.websocket.broker;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import org.springframework.stereotype.Component;

@Component
public class StompLoginInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            accessor.getCommand();
            if(command != null) {
                switch (command) {
                    case CONNECT:
                        String login = accessor.getFirstNativeHeader("login");
                        if (login == null || login.isBlank()) {
                            login = "Anonymous-" + System.currentTimeMillis();
                        }

                        // Setzt das Principal für diese STOMP-Session
                        String finalLogin = login;
                        accessor.setUser(() -> finalLogin);
                        break;
                    default:
                        break;
                }
            }
        }

        return message;
    }
}
