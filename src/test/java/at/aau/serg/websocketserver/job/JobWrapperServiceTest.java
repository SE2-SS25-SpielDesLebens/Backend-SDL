package at.aau.serg.websocketserver.job;

import at.aau.serg.websocketserver.messaging.dtos.JobMessage;
import at.aau.serg.websocketserver.session.Job;
import at.aau.serg.websocketserver.session.JobRepository;
import at.aau.serg.websocketserver.session.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JobWrapperServiceTest {
    
    @Mock
    private JobService jobService;
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    @Mock
    private JobRepository jobRepository;
    
    private JobWrapperService jobWrapperService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jobWrapperService = new JobWrapperService(jobService, messagingTemplate);
        
        // Standard Mock-Verhalten einrichten
        when(jobService.getOrCreateRepository(anyInt())).thenReturn(jobRepository);
    }
    
    @Test
    void testCreateJobRepository() {
        // Act
        JobRepository result = jobWrapperService.createJobRepository(1);
        
        // Assert
        assertNotNull(result);
        verify(jobService).getOrCreateRepository(1);
    }
    
    @Test
    void testHandleJobRequest_WithCurrentJob() {
        // Arrange
        int gameId = 1;
        String playerName = "player1";
        boolean hasDegree = true;
        
        Job currentJob = new Job(1, "Current Job", 1000, 200, false);
        Job otherJob = new Job(2, "Other Job", 1500, 300, true);
        
        when(jobRepository.getCurrentJobForPlayer(playerName)).thenReturn(Optional.of(currentJob));
        when(jobRepository.getRandomAvailableJobs(hasDegree, 1)).thenReturn(Arrays.asList(otherJob));
        
        // Act
        jobWrapperService.handleJobRequest(gameId, playerName, hasDegree);
        
        // Assert
        ArgumentCaptor<String> destCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<JobMessage>> jobsCaptor = ArgumentCaptor.forClass(List.class);
        
        verify(messagingTemplate).convertAndSend(destCaptor.capture(), jobsCaptor.capture());
        
        assertEquals("/topic/1/jobs/player1", destCaptor.getValue());
        List<JobMessage> capturedJobs = jobsCaptor.getValue();
        assertEquals(2, capturedJobs.size());
        assertEquals(1, capturedJobs.get(0).getJobId());
        assertEquals(2, capturedJobs.get(1).getJobId());
    }
    
    @Test
    void testHandleJobRequest_WithoutCurrentJob() {
        // Arrange
        int gameId = 1;
        String playerName = "player1";
        boolean hasDegree = false;
        
        Job job1 = new Job(1, "Job 1", 1000, 200, false);
        Job job2 = new Job(2, "Job 2", 1500, 300, true);
        
        when(jobRepository.getCurrentJobForPlayer(playerName)).thenReturn(Optional.empty());
        when(jobRepository.getRandomAvailableJobs(hasDegree, 2)).thenReturn(Arrays.asList(job1, job2));
        
        // Act
        jobWrapperService.handleJobRequest(gameId, playerName, hasDegree);
        
        // Assert
        ArgumentCaptor<String> destCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<JobMessage>> jobsCaptor = ArgumentCaptor.forClass(List.class);
        
        verify(messagingTemplate).convertAndSend(destCaptor.capture(), jobsCaptor.capture());
        
        assertEquals("/topic/1/jobs/player1", destCaptor.getValue());
        List<JobMessage> capturedJobs = jobsCaptor.getValue();
        assertEquals(2, capturedJobs.size());
    }
    
    @Test
    void testHandleJobSelection_NewJob() {
        // Arrange
        int gameId = 1;
        String playerName = "player1";
        int jobId = 2;
        
        Job selectedJob = new Job(jobId, "Selected Job", 1500, 300, true);
        Job currentJob = new Job(1, "Current Job", 1000, 200, false);
        
        when(jobRepository.getCurrentJobForPlayer(playerName)).thenReturn(Optional.of(currentJob));
        when(jobRepository.findJobById(jobId)).thenReturn(Optional.of(selectedJob));
        
        // Act
        jobWrapperService.handleJobSelection(gameId, playerName, jobId);
        
        // Assert
        verify(jobRepository).assignJobToPlayer(playerName, selectedJob);
    }
    
    @Test
    void testHandleJobSelection_SameJob() {
        // Arrange
        int gameId = 1;
        String playerName = "player1";
        int jobId = 1;
        
        Job currentJob = new Job(jobId, "Current Job", 1000, 200, false);
        
        when(jobRepository.getCurrentJobForPlayer(playerName)).thenReturn(Optional.of(currentJob));
        
        // Act
        jobWrapperService.handleJobSelection(gameId, playerName, jobId);
        
        // Assert
        verify(jobRepository, never()).assignJobToPlayer(anyString(), any(Job.class));
    }
}
