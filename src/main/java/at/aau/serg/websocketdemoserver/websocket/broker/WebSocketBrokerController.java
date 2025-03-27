package at.aau.serg.websocketdemoserver.websocket.broker;

import at.aau.serg.websocketdemoserver.messaging.dtos.OutputMessage;
import at.aau.serg.websocketdemoserver.messaging.dtos.StompMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;


@Controller
public class WebSocketBrokerController {

    @MessageMapping("/move") // z.B. von Client: /app/move
    @SendTo("/topic/game")
    public OutputMessage handleMove(StompMessage message) {
        return new OutputMessage(
                message.getPlayerName(),
                message.getAction(),
                LocalDateTime.now().toString()
        );
    }

    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public OutputMessage handleChat(StompMessage message) {
        return new OutputMessage(
                message.getPlayerName(),
                message.getMessageText(),
                LocalDateTime.now().toString()
        );
    }
}
