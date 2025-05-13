
import at.aau.serg.websocketserver.messaging.dtos.JobMessage;
import at.aau.serg.websocketserver.messaging.dtos.JobRequestMessage;
import at.aau.serg.websocketserver.messaging.dtos.OutputMessage;
import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import at.aau.serg.websocketserver.session.Job;
import at.aau.serg.websocketserver.session.JobRepository;
import at.aau.serg.websocketserver.session.JobService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketBrokerControllerTest {

    @Mock
    private JobService jobService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private JobRepository repo;

    private WebSocketBrokerController controller;

    @Before
    public void setUp() {
        controller = new WebSocketBrokerController(jobService, messagingTemplate);
        // Default: jede gameId liefert das gleiche Mock-Repository
        when(jobService.getOrCreateRepository(anyInt())).thenReturn(repo);
    }

    @Test
    public void testHandleMove() {
        StompMessage input = new StompMessage();
        input.setPlayerName("Alice");
        input.setAction("Würfelt eine 5");

        OutputMessage output = controller.handleMove(input);

        assertEquals("Alice", output.getPlayerName());
        assertEquals("Würfelt eine 5", output.getContent());
        assertNotNull(output.getTimestamp());
    }

    @Test
    public void testHandleChat() {
        StompMessage input = new StompMessage();
        input.setPlayerName("Bob");
        input.setMessageText("Hallo zusammen!");

        OutputMessage output = controller.handleChat(input);

        assertEquals("Bob", output.getPlayerName());
        assertEquals("Hallo zusammen!", output.getContent());
        assertNotNull(output.getTimestamp());
    }

    @Test
    public void testHandleGameStart_CreatesOrLoadsRepositoryOnly() {
        int gameId = 3;
        controller.handleGameStart(gameId);
        verify(jobService, times(1)).getOrCreateRepository(gameId);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    public void testHandleJobRequest_NoCurrentJob_SendsTwoRandomJobs() {
        int gameId = 5;
        String player = "Bob";
        JobRequestMessage msg = new JobRequestMessage();
        msg.setGameId(gameId);
        msg.setPlayerName(player);
        msg.setHasDegree(false);

        when(repo.getCurrentJobForPlayer(player)).thenReturn(Optional.empty());
        Job job1 = new Job(1, "A", 1000, 100, false);
        Job job2 = new Job(2, "B", 2000, 200, false);
        when(repo.getRandomAvailableJobs(false, 2)).thenReturn(Arrays.asList(job1, job2));

        controller.handleJobRequest(gameId, player, msg);

        String dest = String.format("/topic/%d/jobs/%s", gameId, player);
        verify(messagingTemplate).convertAndSend(eq(dest), eq(Arrays.asList(
                new JobMessage(1, "A", 1000, 100, false, false, gameId),
                new JobMessage(2, "B", 2000, 200, false, false, gameId)
        )));
    }

    @Test
    public void testHandleJobRequest_WithCurrentJob_SendsCurrentThenRandom() {
        int gameId = 2;
        String player = "Alice";
        JobRequestMessage msg = new JobRequestMessage();
        msg.setGameId(gameId);
        msg.setPlayerName(player);
        msg.setHasDegree(true);

        Job current = new Job(3, "Curr", 3000, 300, true);
        current.assignJobTo(player);
        when(repo.getCurrentJobForPlayer(player)).thenReturn(Optional.of(current));

        Job other = new Job(4, "Other", 4000, 400, true);
        when(repo.getRandomAvailableJobs(true, 1)).thenReturn(Collections.singletonList(other));

        controller.handleJobRequest(gameId, player, msg);

        String dest = String.format("/topic/%d/jobs/%s", gameId, player);
        verify(messagingTemplate).convertAndSend(eq(dest), eq(Arrays.asList(
                new JobMessage(3, "Curr", 3000, 300, true, true, gameId),
                new JobMessage(4, "Other", 4000, 400, true, false, gameId)
        )));
    }

    @Test
    public void testHandleJobSelection_SameJob_SkipsAssign() {
        int gameId = 7;
        String player = "Eve";
        Job assigned = new Job(5, "X", 5000, 500, false);
        assigned.assignJobTo(player);

        when(repo.getCurrentJobForPlayer(player)).thenReturn(Optional.of(assigned));

        JobMessage msg = new JobMessage(5, "X", 5000, 500, false, true, gameId);
        controller.handleJobSelection(gameId, player, msg);

        verify(repo, never()).findJobById(anyInt());
        verify(repo, never()).assignJobToPlayer(anyString(), any(Job.class));
    }

    @Test
    public void testHandleJobSelection_DifferentJob_PerformsAssign() {
        int gameId = 8;
        String player = "Dan";
        Job newJob = new Job(6, "Y", 6000, 600, false);

        when(repo.getCurrentJobForPlayer(player)).thenReturn(Optional.empty());
        when(repo.findJobById(6)).thenReturn(Optional.of(newJob));

        JobMessage msg = new JobMessage(6, "Y", 6000, 600, false, false, gameId);
        controller.handleJobSelection(gameId, player, msg);

        verify(repo).findJobById(6);
        verify(repo).assignJobToPlayer(player, newJob);
    }
}

