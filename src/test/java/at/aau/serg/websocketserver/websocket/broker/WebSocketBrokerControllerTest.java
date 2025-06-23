package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.lobby.Lobby;
import at.aau.serg.websocketserver.lobby.LobbyService;
import at.aau.serg.websocketserver.messaging.dtos.*;
import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.player.PlayerService;
import at.aau.serg.websocketserver.session.board.BoardService;
import at.aau.serg.websocketserver.session.board.Field;
import at.aau.serg.websocketserver.session.board.FieldType;
import at.aau.serg.websocketserver.session.house.HouseService;
import at.aau.serg.websocketserver.session.job.Job;
import at.aau.serg.websocketserver.session.job.JobRepository;
import at.aau.serg.websocketserver.session.job.JobService;
import at.aau.serg.websocketserver.game.GameLogic;
import at.aau.serg.websocketserver.game.PlayerTurnManager;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class WebSocketBrokerControllerTest {

    @Mock JobService jobService;
    @Mock PlayerService playerService;
    @Mock LobbyService lobbyService;
    @Mock BoardService boardService;
    @Mock HouseService houseService;
    @Mock SimpMessagingTemplate messagingTemplate;

    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    private WebSocketBrokerController createController() {
        try (MockedStatic<PlayerService> playerStatic = mockStatic(PlayerService.class);
             MockedStatic<LobbyService> lobbyStatic = mockStatic(LobbyService.class)) {
            playerStatic.when(PlayerService::getInstance).thenReturn(playerService);
            lobbyStatic.when(LobbyService::getInstance).thenReturn(lobbyService);
            return new WebSocketBrokerController(jobService, messagingTemplate, boardService, houseService);
        }
    }

    @Test
    void testHandleBoardDataRequest() {
        when(boardService.getBoard()).thenReturn(Collections.emptyList());
        createController().handleBoardDataRequest();
        verify(messagingTemplate).convertAndSend(eq("/topic/board/data"), any(BoardDataMessage.class));
    }

    @Test
    void testHandleJobRepoCreation() {
        createController().handleJobRepoCreation(1);
        verify(jobService).getOrCreateRepository(1);
        verify(messagingTemplate).convertAndSend(contains("/topic/game/1/status"), any(OutputMessage.class));
    }

    @Test
    void testHandleLobbyCreate() {
        LobbyRequestMessage req = new LobbyRequestMessage();
        req.setPlayerName("testPlayer");
        Player player = new Player("testPlayer");
        Lobby lobby = new Lobby("1", player);

        when(playerService.createPlayerIfNotExists("testPlayer")).thenReturn(player);
        when(lobbyService.createLobby(player)).thenReturn(lobby);

        createController().handleLobbyCreate(req);
        verify(messagingTemplate).convertAndSendToUser(eq("testPlayer"), eq("/queue/lobby/created"), any());
    }

    @Test
    void testHandlePlayerExistenceCheck() {
        StompMessage msg = new StompMessage();
        msg.setPlayerName("tester");
        when(playerService.isPlayerRegistered("tester")).thenReturn(true);
        createController().handlePlayerExistenceCheck(msg);
        verify(messagingTemplate).convertAndSendToUser(eq("tester"), eq("/queue/players/check"), any(OutputMessage.class));
    }

    @Test
    void testHandleChat() {
        StompMessage msg = new StompMessage();
        msg.setPlayerName("user");
        msg.setMessageText("hello");
        createController().handleChat(msg);
        verify(messagingTemplate).convertAndSend(eq("/topic/chat"), any(OutputMessage.class));
    }

    @Test
    void testHandleJobRequest_withExistingJob() {
        int gameId = 1;
        String playerName = "Alice";
        JobRepository repo = mock(JobRepository.class);
        Job job = new Job(1, "Dev", 1000, 200, false);
        when(jobService.getOrCreateRepository(gameId)).thenReturn(repo);
        when(repo.getCurrentJobForPlayer(playerName)).thenReturn(Optional.of(job));
        when(repo.getRandomAvailableJobs(false, 1)).thenReturn(new ArrayList<>(List.of(job)));

        createController().handleJobRequest(gameId, playerName, new JobRequestMessage());
        verify(messagingTemplate).convertAndSend(
                eq("/topic/1/jobs/Alice"),
                ArgumentMatchers.<List<JobMessage>>any()
        );
    }

    @Test
    void testHandleJobSelection() {
        int gameId = 1;
        String playerName = "Bob";
        JobRepository repo = mock(JobRepository.class);
        Job job = new Job(2, "Designer", 900, 50, false);
        JobMessage msg = new JobMessage(2, "Designer", 900, 50, false, false, gameId);
        when(jobService.getOrCreateRepository(gameId)).thenReturn(repo);
        when(repo.getCurrentJobForPlayer(playerName)).thenReturn(Optional.empty());
        when(repo.findJobById(2)).thenReturn(Optional.of(job));

        createController().handleJobSelection(gameId, playerName, msg);
        verify(repo).assignJobToPlayer(playerName, job);
    }

    @Test
    void testSendLobbyUpdates_withPlayers() {
        Lobby lobby = mock(Lobby.class);
        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);

        when(p1.getId()).thenReturn("p1");
        when(p2.getId()).thenReturn("p2");
        when(lobby.getPlayers()).thenReturn(List.of(p1, p2));
        when(lobby.isStarted()).thenReturn(true);
        when(lobbyService.getLobby("abc")).thenReturn(lobby);

        createController().sendLobbyUpdates("abc");
        verify(messagingTemplate).convertAndSend(eq("/topic/abc"), any(LobbyUpdateMessage.class));
    }

    @Test
    void testHandlePlayerLeave() {
        Lobby lobby = mock(Lobby.class);
        Player p = new Player("removeMe");
        LobbyRequestMessage msg = new LobbyRequestMessage();
        msg.setPlayerName("removeMe");
        when(lobbyService.getLobby("L1")).thenReturn(lobby);
        when(playerService.getPlayerById("removeMe")).thenReturn(p);

        createController().handlePlayerLeave("L1", msg);
        verify(lobby).removePlayer(p);
        verify(messagingTemplate).convertAndSend(contains("/topic/L1"), any(LobbyUpdateMessage.class));
    }

    @Test
    void testHandleHouseRepoCreation() {
        createController().handleHouseRepoCreation(1);
        verify(houseService).getOrCreateRepository(1);
        verify(messagingTemplate).convertAndSend(contains("/topic/game/1/status"), any(OutputMessage.class));
    }

    @Test
    void testHandlePlayerPositionsRequest() {
        when(boardService.getAllPlayerPositions()).thenReturn(new HashMap<>());
        createController().handlePlayerPositionsRequest();
        verify(messagingTemplate).convertAndSend(eq("/topic/players/positions"), any(PlayerPositionsMessage.class));
    }

    @Test
    void handleLegacyMove_WhenJoinAction_ShouldAddPlayerAndSendMoveMessage() {
        // Arrange
        StompMessage msg = new StompMessage();
        msg.setPlayerName("1");
        msg.setAction("join:5");

        Field startField = new Field(5, 0.0, 0.0, List.of(6), FieldType.STARTNORMAL);
        Field nextField = new Field(6, 1.0, 0.0, List.of(7), FieldType.ANLAGE);

        // Mock the board service behavior
        doNothing().when(boardService).addPlayer(1, 5);
        when(boardService.getPlayerField(1)).thenReturn(startField);
        when(boardService.getValidNextFields(1)).thenReturn(List.of(nextField));

        // Act
        createController().handleLegacyMove(msg);

        // Assert
        verify(boardService).addPlayer(1, 5);
        verify(boardService).getPlayerField(1);
        verify(boardService).getValidNextFields(1);
        verify(messagingTemplate).convertAndSend(eq("/topic/game"), any(MoveMessage.class));

        verifyNoMoreInteractions(boardService, messagingTemplate);
    }

    @Test
    void handleLegacyMove_WhenMoveToActionField_ShouldHandleCorrectly() {
        // Arrange
        StompMessage msg = new StompMessage();
        msg.setPlayerName("1");
        msg.setAction("move:8");

        Field actionField = new Field(8, 2.0, 0.0, List.of(9), FieldType.AKTION);
        Field nextField = new Field(9, 3.0, 0.0, List.of(10), FieldType.ANLAGE);

        // Mock the board service behavior
        when(boardService.movePlayerToField("1", 8)).thenReturn(true);
        when(boardService.getPlayerField(1)).thenReturn(actionField);
        when(boardService.getValidNextFields(1)).thenReturn(List.of(nextField));

        // Act
        createController().handleLegacyMove(msg);

        // Assert
        verify(boardService).movePlayerToField("1", 8);
        verify(boardService).getPlayerField(1);
        verify(boardService).getValidNextFields(1);
        verify(messagingTemplate).convertAndSend(eq("/topic/game"), any(MoveMessage.class));

        verifyNoMoreInteractions(boardService, messagingTemplate);
    }

    @Test
    void handleLegacyMove_WhenMoveToDecisionField_ShouldHandleCorrectly() {
        // Arrange
        StompMessage msg = new StompMessage();
        msg.setPlayerName("1");
        msg.setAction("move:15");

        Field decisionField = new Field(15, 4.0, 0.0, List.of(16, 20), FieldType.HEIRAT);
        Field option1Field = new Field(16, 5.0, 0.0, List.of(17), FieldType.HEIRAT_JA);
        Field option2Field = new Field(20, 5.0, 1.0, List.of(21), FieldType.HEIRAT_NEIN);

        // Mock the board service behavior
        when(boardService.movePlayerToField("1", 15)).thenReturn(true);
        when(boardService.getPlayerField(1)).thenReturn(decisionField);
        when(boardService.getValidNextFields(1)).thenReturn(List.of(option1Field, option2Field));

        // Act
        createController().handleLegacyMove(msg);

        // Assert
        verify(boardService).movePlayerToField("1", 15);
        verify(boardService).getPlayerField(1);
        verify(boardService).getValidNextFields(1);
        verify(messagingTemplate).convertAndSend(eq("/topic/game"), any(MoveMessage.class));

        verifyNoMoreInteractions(boardService, messagingTemplate);
    }


    @Test
    void handleLegacyMove_WhenMoveToEndGameField_ShouldHandleCorrectly() {
        // Arrange
        StompMessage msg = new StompMessage();
        msg.setPlayerName("1");
        msg.setAction("move:50");

        Field endField = new Field(50, 10.0, 0.0, Collections.emptyList(), FieldType.RUHESTAND);

        // Mock the board service behavior
        when(boardService.movePlayerToField("1", 50)).thenReturn(true);
        when(boardService.getPlayerField(1)).thenReturn(endField);
        when(boardService.getValidNextFields(1)).thenReturn(Collections.emptyList());

        // Act
        createController().handleLegacyMove(msg);

        // Assert
        verify(boardService).movePlayerToField("1", 50);
        verify(boardService).getPlayerField(1);
        verify(boardService).getValidNextFields(1);
        verify(messagingTemplate).convertAndSend(eq("/topic/game"), any(MoveMessage.class));

        verifyNoMoreInteractions(boardService, messagingTemplate);
    }

    @Test
    void testHandleLegacyMove_invalidPlayerId() {
        StompMessage msg = new StompMessage();
        msg.setPlayerName("invalid");
        msg.setAction("move:5");

        createController().handleLegacyMove(msg);

        verify(messagingTemplate).convertAndSend(eq("/topic/game"), any(OutputMessage.class));
    }

    @Test
    void testHandleLobby_createAction() {
        StompMessage msg = new StompMessage();
        msg.setAction("createLobby");
        msg.setGameId("123");
        msg.setPlayerName("testPlayer");

        createController().handleLobby(msg);

        verify(messagingTemplate).convertAndSend(eq("/topic/lobby"), any(OutputMessage.class));
    }

    @Test
    void testHandleLobby_joinAction() {
        StompMessage msg = new StompMessage();
        msg.setAction("joinLobby");
        msg.setGameId("123");
        msg.setPlayerName("testPlayer");

        createController().handleLobby(msg);

        verify(messagingTemplate).convertAndSend(eq("/topic/lobby"), any(OutputMessage.class));
    }

    @Test
    void testHandlePlayerJoin_success() {
        LobbyRequestMessage req = new LobbyRequestMessage();
        req.setPlayerName("newPlayer");

        Player player = new Player("newPlayer");
        Lobby lobby = mock(Lobby.class); // 🔧 Das ist jetzt ein echtes Mock

        when(playerService.createPlayerIfNotExists("newPlayer")).thenReturn(player);
        when(lobbyService.getLobby("123")).thenReturn(lobby);
        when(lobby.getPlayers()).thenReturn(new ArrayList<>()); // Falls notwendig

        createController().handlePlayerJoin("123", req);

        verify(messagingTemplate).convertAndSend(eq("/topic/123"), any(LobbyResponseMessage.class));
        verify(lobby).addPlayer(player); // ✅ Jetzt funktioniert das!
    }


    @Test
    void testHandlePlayerJoin_lobbyNotFound() {
        LobbyRequestMessage req = new LobbyRequestMessage();
        req.setPlayerName("newPlayer");

        when(lobbyService.getLobby("123")).thenReturn(null);

        createController().handlePlayerJoin("123", req);

        verify(messagingTemplate).convertAndSend(eq("/topic/123"), any(LobbyResponseMessage.class));
    }



    @Test
    void testHandleGameStart_alreadyStarted() {
        Lobby lobby = mock(Lobby.class);
        when(lobby.isStarted()).thenReturn(true);

        when(lobbyService.getLobby("1")).thenReturn(lobby);

        createController().handleGameStart(1);

        verify(lobby, never()).setStarted(true);
    }

    @Test
    void testHandleGameEnd_success() {
        Lobby lobby = mock(Lobby.class);
        GameLogic gameLogic = mock(GameLogic.class);

        when(lobby.isStarted()).thenReturn(true);
        when(lobby.getGameLogic()).thenReturn(gameLogic);
        when(lobbyService.getLobby("1")).thenReturn(lobby);

        try (MockedStatic<LobbyService> lobbyStatic = mockStatic(LobbyService.class);
             MockedStatic<PlayerService> playerStatic = mockStatic(PlayerService.class)) {
            lobbyStatic.when(LobbyService::getInstance).thenReturn(lobbyService);
            playerStatic.when(PlayerService::getInstance).thenReturn(playerService);

            WebSocketBrokerController controller = new WebSocketBrokerController(
                    jobService, messagingTemplate, boardService, houseService
            );

            controller.handleGameEnd("1");

            verify(gameLogic).endGame();
            verify(lobby).setStarted(false);
        }
    }


    @Test
    void testHandleGameEnd_notStarted() {
        Lobby lobby = mock(Lobby.class);
        when(lobby.isStarted()).thenReturn(false);
        when(lobbyService.getLobby("1")).thenReturn(lobby);

        OutputMessage result = createController().handleGameEnd("1");

        assertTrue(result.getContent().contains("nicht gestartet"));
    }

    @Test
    void testHandleHouseAction() {
        HouseBuyElseSellMessage msg = new HouseBuyElseSellMessage();
        msg.setBuyElseSell(true);

        when(houseService.handleHouseAction(1, "player1", true))
                .thenReturn(List.of(new HouseMessage()));

        createController().handleHouseAction(1, "player1", msg);

        verify(messagingTemplate).convertAndSend(contains("/topic/1/houses/player1/options"), anyList());
    }

    @Test
    void testFinalizeHouseAction() {
        HouseMessage msg = new HouseMessage();

        when(houseService.finalizeHouseAction(1, "player1", msg))
                .thenReturn(msg);

        createController().finalizeHouseAction(1, "player1", msg);

        verify(messagingTemplate).convertAndSend(contains("/topic/1/houses/player1/confirmation"), any(HouseMessage.class));
    }

    @Test
    void testHandleStartCareer() {
        Lobby lobby = mock(Lobby.class);
        GameLogic gameLogic = mock(GameLogic.class);
        PlayerTurnManager turnManager = mock(PlayerTurnManager.class);

        when(lobby.isStarted()).thenReturn(true);
        when(lobby.getGameLogic()).thenReturn(gameLogic);
        when(gameLogic.getTurnManager()).thenReturn(turnManager);
        when(lobbyService.getLobby("1")).thenReturn(lobby);

        try (MockedStatic<PlayerService> ps = mockStatic(PlayerService.class);
             MockedStatic<LobbyService> ls = mockStatic(LobbyService.class)) {
            ps.when(PlayerService::getInstance).thenReturn(playerService);
            ls.when(LobbyService::getInstance).thenReturn(lobbyService);

            WebSocketBrokerController controller = new WebSocketBrokerController(
                    jobService, messagingTemplate, boardService, houseService
            );

            controller.handleStartCareer(1, "player1");

            verify(turnManager).startWithCareer("player1", 1);
            verify(messagingTemplate).convertAndSend(contains("/topic/game/1/status"), contains("beginnt mit einer Karriere"));
        }
    }


    @Test
    void testHandleStartUniversity() {
        Lobby lobby = mock(Lobby.class);
        GameLogic gameLogic = mock(GameLogic.class);
        PlayerTurnManager turnManager = mock(PlayerTurnManager.class);

        when(lobby.isStarted()).thenReturn(true);
        when(lobby.getGameLogic()).thenReturn(gameLogic);
        when(gameLogic.getTurnManager()).thenReturn(turnManager);

        // Hier fehlt das entscheidende Mocking!
        when(lobbyService.getLobby("1")).thenReturn(lobby);

        try (MockedStatic<LobbyService> lobbyStatic = mockStatic(LobbyService.class);
             MockedStatic<PlayerService> playerStatic = mockStatic(PlayerService.class)) {

            lobbyStatic.when(LobbyService::getInstance).thenReturn(lobbyService);
            playerStatic.when(PlayerService::getInstance).thenReturn(playerService);

            WebSocketBrokerController controller = new WebSocketBrokerController(
                    jobService, messagingTemplate, boardService, houseService
            );

            controller.handleStartUniversity(1, "player1");

            verify(turnManager).startWithUniversity("player1", 1);
        }
    }


    @Test
    void testHandleGameJoin_success() {
        Lobby lobby = mock(Lobby.class);
        GameLogic gameLogic = mock(GameLogic.class);
        Player player = new Player("newPlayer");

        when(lobby.getPlayers()).thenReturn(new ArrayList<>());
        when(lobby.isFull()).thenReturn(false);
        when(lobby.isStarted()).thenReturn(true);
        when(lobby.getGameLogic()).thenReturn(gameLogic);
        when(gameLogic.registerPlayer("newPlayer")).thenReturn(true);

        when(playerService.createPlayerIfNotExists("newPlayer")).thenReturn(player);
        when(lobbyService.getLobby("1")).thenReturn(lobby);

        StompMessage msg = new StompMessage();
        msg.setPlayerName("newPlayer");

        try (MockedStatic<LobbyService> lobbyStatic = mockStatic(LobbyService.class);
             MockedStatic<PlayerService> playerStatic = mockStatic(PlayerService.class)) {

            lobbyStatic.when(LobbyService::getInstance).thenReturn(lobbyService);
            playerStatic.when(PlayerService::getInstance).thenReturn(playerService);

            WebSocketBrokerController controller = new WebSocketBrokerController(
                    jobService, messagingTemplate, boardService, houseService
            );

            controller.handleGameJoin("1", msg);

            verify(messagingTemplate).convertAndSend(
                    contains("/topic/game/1/status"),
                    anyString()
            );

            verify(messagingTemplate).convertAndSendToUser(
                    eq("newPlayer"), contains("/confirm"), any()
            );
        }
    }



    @Test
    void testGetPlayerIdSafe() {
        WebSocketBrokerController controller = createController();
        Lobby lobby = mock(Lobby.class);
        Player player = new Player("testPlayer");

        // Case 1: Lobby is null
        assertEquals("", controller.getPlayerIdSafe(null, 0));

        // Case 2: Players list is null
        when(lobby.getPlayers()).thenReturn(null);
        assertEquals("", controller.getPlayerIdSafe(lobby, 0));

        // Case 3: Index out of bounds
        when(lobby.getPlayers()).thenReturn(List.of(player));
        assertEquals("", controller.getPlayerIdSafe(lobby, 1));

        // Case 4: Valid case
        assertEquals("testPlayer", controller.getPlayerIdSafe(lobby, 0));
    }
}