package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.messaging.dtos.OutputMessage;
import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class WebSocketBrokerController {

    @MessageMapping("/move") // z.B. von Client: /app/move
    @SendTo("/topic/game")
    public OutputMessage handleMove(StompMessage message) {
        System.out.println("[MOVE] " + message.getPlayerName() + ": " + message.getAction());
        return new OutputMessage(
                message.getPlayerName(),
                message.getAction(),
                LocalDateTime.now().toString()
        );
    }

    @MessageMapping("/lobby")
    @SendTo("/topic/lobby")
    public OutputMessage handleLobby(StompMessage message) {
        String action = message.getAction();
        String content;

        if (action == null) {
            content = "❌ Keine Aktion angegeben.";
        } else {
            switch (action) {
                case "createLobby":
                    content = "Lobby von " + message.getPlayerName() + " erstellt.";
                    break;
                case "joinLobby":
                    content = message.getPlayerName() + " ist der Lobby beigetreten.";
                    break;
                default:
                    content = "Unbekannte Lobby-Aktion.";
                    break;
            }
        }

        System.out.println("[LOBBY] " + message.getPlayerName() + ": " + content);

        return new OutputMessage(
                message.getPlayerName(),
                content,
                LocalDateTime.now().toString()
        );
    }

    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public OutputMessage handleChat(StompMessage message) {
        System.out.println("[CHAT] " + message.getPlayerName() + ": " + message.getMessageText());
        return new OutputMessage(
                message.getPlayerName(),
                message.getMessageText(),
                LocalDateTime.now().toString()
        );
    }
}
