package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.messaging.dtos.OutputMessage;
import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class WebSocketBrokerController {

    @MessageMapping("/move")
    @SendTo("/topic/game") // optional: dynamisch mit gameId (siehe Kommentar unten)
    public OutputMessage handleMove(StompMessage message) {
        System.out.println("[MOVE] [" + message.getGameId() + "] " + message.getPlayerName() + ": " + message.getAction());
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
        String gameId = message.getGameId();

        if (action == null) {
            content = "❌ Keine Aktion angegeben.";
        } else {
            switch (action) {
                case "createLobby":
                    content = "🆕 Lobby [" + gameId + "] von " + message.getPlayerName() + " erstellt.";
                    break;
                case "joinLobby":
                    content = "✅ " + message.getPlayerName() + " ist Lobby [" + gameId + "] beigetreten.";
                    break;
                default:
                    content = "Unbekannte Lobby-Aktion.";
                    break;
            }
        }

        System.out.println("[LOBBY] [" + gameId + "] " + message.getPlayerName() + ": " + content);

        return new OutputMessage(
                message.getPlayerName(),
                content,
                LocalDateTime.now().toString()
        );
    }

    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public OutputMessage handleChat(StompMessage message) {
        System.out.println("[CHAT] [" + message.getGameId() + "] " + message.getPlayerName() + ": " + message.getMessageText());
        return new OutputMessage(
                message.getPlayerName(),
                message.getMessageText(),
                LocalDateTime.now().toString()
        );
    }
}
