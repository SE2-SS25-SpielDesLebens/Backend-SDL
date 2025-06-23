package at.aau.serg.websocketserver.websocket.broker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StompLoginInterceptorTest {

    private final StompLoginInterceptor interceptor = new StompLoginInterceptor();

    @Test
    void preSend_shouldHandleNullAccessor() {
        Message<?> message = MessageBuilder.withPayload("test").build();
        MessageChannel channel = mock(MessageChannel.class);

        Message<?> result = interceptor.preSend(message, channel);

        assertSame(message, result);
    }

    @Test
    void preSend_shouldHandleNullCommand() {
        // Direkter Headeraufbau ohne StompCommand
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(
                MessageBuilder.withPayload("test").build()
        );
        // Kein expliziter StompCommand gesetzt

        Message<?> message = MessageBuilder.withPayload("test")
                .setHeaders(accessor)
                .build();
        MessageChannel channel = mock(MessageChannel.class);

        Message<?> result = interceptor.preSend(message, channel);

        assertSame(message, result); // Erwartet: Unverändertes Verhalten
    }



    @Test
    void preSend_shouldSetUserFromLoginHeader() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("login", "testUser");
        accessor.setLeaveMutable(true); // wichtig!

        Message<?> message = MessageBuilder.createMessage("test", accessor.getMessageHeaders());
        MessageChannel channel = mock(MessageChannel.class);

        Message<?> result = interceptor.preSend(message, channel);

        StompHeaderAccessor resultAccessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertNotNull(resultAccessor);
        assertNotNull(resultAccessor.getUser());
        assertEquals("testUser", resultAccessor.getUser().getName());
    }


    @Test
    void preSend_shouldHandleBlankLoginHeader() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("login", "   ");
        accessor.setLeaveMutable(true); // notwendig!

        Message<?> message = MessageBuilder.createMessage("test", accessor.getMessageHeaders());
        MessageChannel channel = mock(MessageChannel.class);

        Message<?> result = interceptor.preSend(message, channel);

        StompHeaderAccessor resultAccessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertNotNull(resultAccessor);
        assertNotNull(resultAccessor.getUser());
        assertTrue(resultAccessor.getUser().getName().startsWith("Anonymous-"));
    }


    @Test
    void preSend_shouldIgnoreNonConnectCommands() {
        for (StompCommand command : StompCommand.values()) {
            if (command != StompCommand.CONNECT) {
                StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
                Message<?> message = MessageBuilder.withPayload("test")
                        .setHeaders(accessor)
                        .build();
                MessageChannel channel = mock(MessageChannel.class);

                Message<?> result = interceptor.preSend(message, channel);

                assertSame(message, result);
                if (MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class) != null) {
                    assertNull(MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class).getUser());
                }
            }
        }
    }

    @Test
    void preSend_shouldPreserveOriginalMessageProperties() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("login", "testUser");
        accessor.setHeader("customHeader", "value");
        accessor.setLeaveMutable(true); // ⬅ wichtig!

        Message<?> originalMessage = MessageBuilder.createMessage("test", accessor.getMessageHeaders());
        MessageChannel channel = mock(MessageChannel.class);

        Message<?> result = interceptor.preSend(originalMessage, channel);

        StompHeaderAccessor resultAccessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertNotNull(resultAccessor);
        assertEquals("value", resultAccessor.getHeader("customHeader"));
        assertEquals("test", result.getPayload());
    }

}