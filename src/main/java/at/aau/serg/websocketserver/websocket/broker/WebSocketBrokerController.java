package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.messaging.dtos.OutputMessage;
import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import at.aau.serg.websocketdemoserver.session.Job;
import at.aau.serg.websocketdemoserver.session.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
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

    @MessageMapping("/move") // z.B. vom Client: /app/move
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

    /**
     * Diese Methode wird aufgerufen, wenn das Frontend eine Nachricht an "/app/job/request" sendet.
     * Sie holt einen zufälligen, noch nicht vergebenen Job, markiert ihn als vergeben und sendet
     * die Job-Informationen an das Topic "/topic/job".
     */
    @MessageMapping("/job/request")
    @SendTo("/topic/job")
    public OutputMessage handleJobRequest(StompMessage message) {
        Optional<Job> jobOpt = jobRepository.getRandomJob();
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            String jobInfo = "Job assigned: " + job.getBezeichnung();
            return new OutputMessage(message.getPlayerName(), jobInfo, LocalDateTime.now().toString());
        } else {
            return new OutputMessage(message.getPlayerName(), "Kein Job verfügbar", LocalDateTime.now().toString());
        }
    }
}
