package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketdemoserver.session.Job;
import at.aau.serg.websocketserver.messaging.dtos.JobMessage;
import at.aau.serg.websocketserver.messaging.dtos.OutputMessage;
import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import at.aau.serg.websocketdemoserver.session.JobRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class WebSocketBrokerController {

    private final JobRepository jobRepository = new JobRepository();

    public WebSocketBrokerController() {
        try {
            this.jobRepository.loadJobs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/move")
    @SendTo("/topic/game")
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

    @MessageMapping("/getJob")
    @SendTo("/topic/getJob")
    public List<JobMessage> handleJobRequest(@Payload StompMessage message) {
        return jobRepository.getTwoAvailableJobMessages(message.getPlayerName());
    }

    @MessageMapping("/acceptJob")
    @SendTo("/topic/jobTaken")
    public JobMessage acceptJob(@Payload JobMessage message) {
        Optional<Job> jobOpt = jobRepository.findJobById(message.getJobId());

        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();

            if (!job.isTaken()) {
                job.assignJobTo(message.getPlayerName());

                return new JobMessage(
                        job.getJobId(),
                        job.getTitle(),
                        job.getSalary(),
                        job.getBonusSalary(),
                        job.isRequiresDegree(),
                        true,
                        job.getAssignedToPlayerName(),
                        message.getPlayerName(),
                        LocalDateTime.now().toString()
                );
            } else {
                return new JobMessage(
                        job.getJobId(),
                        job.getTitle() + " (bereits vergeben)",
                        job.getSalary(),
                        job.getBonusSalary(),
                        job.isRequiresDegree(),
                        true,
                        job.getAssignedToPlayerName(),
                        message.getPlayerName(),
                        LocalDateTime.now().toString()
                );
            }
        } else {
            return new JobMessage(
                    -1,
                    "❌ Job nicht gefunden",
                    0,
                    0,
                    false,
                    false,
                    null,
                    message.getPlayerName(),
                    LocalDateTime.now().toString()
            );
        }
    }



}
