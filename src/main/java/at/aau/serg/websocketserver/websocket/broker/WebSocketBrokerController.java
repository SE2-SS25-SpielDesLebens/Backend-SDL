package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.messaging.dtos.JobMessage;
import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import at.aau.serg.websocketdemoserver.session.Job;
import at.aau.serg.websocketdemoserver.session.JobRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /**
     * Gibt dem Spieler 2 neue Jobs oder seinen aktuellen + einen neuen Job zurück.
     */
    @MessageMapping("/getJob")
    @SendTo("/topic/getJob")
    public List<JobMessage> handleJobRequest(@Payload StompMessage message) {
        List<Job> jobsToSend = jobRepository.getJobsForPlayer(message.getPlayerName());

        return jobsToSend.stream()
                .map(job -> new JobMessage(
                        job.getJobId(),
                        job.getTitle(),
                        job.getSalary(),
                        job.getBonusSalary(),
                        job.isRequiresDegree(),
                        job.isTaken(),
                        job.getAssignedToPlayerName(),
                        message.getPlayerName(),
                        LocalDateTime.now().toString()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Verarbeitet die Annahme eines Jobs und gibt nur den neuen Job zurück.
     */
    @MessageMapping("/acceptJob")
    @SendTo("/topic/getJob")
    public List<JobMessage> handleJobAcceptance(@Payload JobMessage message) {
        Optional<Job> jobOpt = jobRepository.findJobById(message.getJobId());

        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            boolean assigned = jobRepository.assignJobToPlayer(message.getPlayerName(), job);

            if (assigned) {
                return List.of(new JobMessage(
                        job.getJobId(),
                        job.getTitle(),
                        job.getSalary(),
                        job.getBonusSalary(),
                        job.isRequiresDegree(),
                        job.isTaken(),
                        job.getAssignedToPlayerName(),
                        message.getPlayerName(),
                        LocalDateTime.now().toString()
                ));
            }
        }

        return List.of(new JobMessage(
                0,
                "Job konnte nicht übernommen werden",
                0,
                0,
                false,
                false,
                null,
                message.getPlayerName(),
                LocalDateTime.now().toString()
        ));
    }
}
