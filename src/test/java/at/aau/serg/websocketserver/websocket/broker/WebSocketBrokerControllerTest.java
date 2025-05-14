package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.board.BoardService;
import at.aau.serg.websocketserver.board.Field;
import at.aau.serg.websocketserver.messaging.dtos.*;
import at.aau.serg.websocketserver.session.Job;
import at.aau.serg.websocketserver.session.JobRepository;
import at.aau.serg.websocketserver.session.JobService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketBrokerControllerTest {

    @Mock private JobService jobService;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private JobRepository repo;
    @Mock private BoardService boardService;

    private WebSocketBrokerController controller;

    @Before
    public void setUp() {
        // immer dieselbe Repo zurückliefern
        when(jobService.getOrCreateRepository(anyInt())).thenReturn(repo);
        controller = new WebSocketBrokerController(jobService, messagingTemplate, boardService);
    }


    // --- handleChat tests -------------------------------------------------

    @Test
    public void testHandleChat() {
        StompMessage msg = new StompMessage();
        msg.setPlayerName("Bob");
        msg.setMessageText("Hallo zusammen!");

        controller.handleChat(msg);

        ArgumentCaptor<OutputMessage> cap = ArgumentCaptor.forClass(OutputMessage.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/chat"), cap.capture());

        OutputMessage out = cap.getValue();
        assertEquals("Bob", out.getPlayerName());
        assertEquals("Hallo zusammen!", out.getContent());
        assertNotNull(out.getTimestamp());
    }

    // --- handleLobby tests ------------------------------------------------

    @Test
    public void testHandleLobby_CreateLobby() {
        StompMessage msg = new StompMessage();
        msg.setPlayerName("Alice");
        msg.setAction("createLobby");
        msg.setGameId("42");

        controller.handleLobby(msg);

        ArgumentCaptor<OutputMessage> cap = ArgumentCaptor.forClass(OutputMessage.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/lobby"), cap.capture());

        OutputMessage out = cap.getValue();
        assertEquals("Alice", out.getPlayerName());
        assertEquals("🆕 Lobby [42] von Alice erstellt.", out.getContent());
        assertNotNull(out.getTimestamp());
    }

    @Test
    public void testHandleLobby_JoinLobby() {
        StompMessage msg = new StompMessage();
        msg.setPlayerName("Bob");
        msg.setAction("joinLobby");
        msg.setGameId("7");

        controller.handleLobby(msg);

        ArgumentCaptor<OutputMessage> cap = ArgumentCaptor.forClass(OutputMessage.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/lobby"), cap.capture());

        OutputMessage out = cap.getValue();
        assertEquals("Bob", out.getPlayerName());
        assertEquals("✅ Bob ist Lobby [7] beigetreten.", out.getContent());
        assertNotNull(out.getTimestamp());
    }

    // --- handleGameStart tests --------------------------------------------

    @Test
    public void testHandleGameStart_CreatesOrLoadsRepositoryOnly() {
        int gameId = 3;
        controller.handleGameStart(gameId);

        verify(jobService, times(1)).getOrCreateRepository(gameId);
        verifyNoInteractions(messagingTemplate);
    }

    // --- handleJobRequest tests -------------------------------------------

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

    // --- handleJobSelection tests -----------------------------------------

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
