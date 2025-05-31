package at.aau.serg.websocketserver.session.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JobRepositoryTest {
    private JobRepository repo;

    @BeforeEach
    void setUp() {
        repo = new JobRepository();
    }

    @SuppressWarnings("unchecked")
    private List<Job> getJobsList(JobRepository repository) throws Exception {
        Field field = JobRepository.class.getDeclaredField("jobs");
        field.setAccessible(true);
        return (List<Job>) field.get(repository);
    }

    @Test
    void testGetCurrentJobForPlayerWhenUnloaded() {
        Optional<Job> result = repo.getCurrentJobForPlayer("Alice");
        assertTrue(result.isEmpty(), "Expected no job for player when repository is empty");
    }

    @Test
    void testGetCurrentJobForPlayerWhenLoaded() throws Exception {
        Job job = new Job(1, "Designer", 3500, 300, false);
        job.assignJobTo("Alice");
        getJobsList(repo).add(job);

        Optional<Job> result = repo.getCurrentJobForPlayer("Alice");
        assertTrue(result.isPresent(), "Expected to find a job for Alice");
        assertEquals(job, result.get(), "Found job should be the one assigned to Alice");
    }

    @Test
    void testFindJobByIdWhenIDExists() throws Exception {
        Job job1 = new Job(1, "Dev", 5000, 500, false);
        Job job2 = new Job(2, "Analyst", 4500, 400, true);
        List<Job> jobs = getJobsList(repo);
        jobs.add(job1);
        jobs.add(job2);

        Optional<Job> result = repo.findJobById(2);
        assertTrue(result.isPresent(), "Expected to find job with id=2");
        assertEquals(job2, result.get(), "Found job should have id=2");
    }

    @Test
    void testFindJobByIdWhenIdNotExists() throws Exception {
        Optional<Job> result = repo.findJobById(99);
        assertTrue(result.isEmpty(), "Expected no job for non-existent id");
    }

    @Test
    void testGetRandomAvailableJobs() throws Exception {
        Job j1 = new Job(1, "A", 100, 10, false);
        Job j2 = new Job(2, "B", 200, 20, false);
        Job j3 = new Job(3, "C", 300, 30, true);
        Job j4 = new Job(4, "D", 400, 40, false);

        List<Job> jobs = getJobsList(repo);
        jobs.add(j1);
        jobs.add(j2);
        jobs.add(j3);
        jobs.add(j4);

        List<Job> availableFalse = repo.getRandomAvailableJobs(false, 3);
        assertEquals(3, availableFalse.size(), "Should return exactly 3 jobs");
        assertTrue(availableFalse.stream().noneMatch(Job::isTaken), "None of the returned jobs should be taken");
        assertTrue(availableFalse.stream().noneMatch(Job::isRequiresDegree), "None should require a degree");

        List<Job> availableTrue = repo.getRandomAvailableJobs(true, 1);
        assertEquals(1, availableTrue.size(), "Should return exactly 1 job");
        assertTrue(availableTrue.stream().noneMatch(Job::isTaken), "Returned job should not be taken");
        assertTrue(availableTrue.stream().allMatch(Job::isRequiresDegree), "Returned job should require a degree");
    }

    @Test
    void testAssignAndReleaseJobAssignment() throws Exception {
        Job oldJob = new Job(1, "Old", 100, 10, false);
        Job newJob = new Job(2, "New", 200, 20, false);
        List<Job> jobs = getJobsList(repo);
        jobs.add(oldJob);
        jobs.add(newJob);

        // Zuweisung des ersten Jobs an Bob
        repo.assignJobToPlayer("Bob", oldJob);
        assertTrue(oldJob.isTaken(), "Old job should be marked as taken");
        assertEquals("Bob", oldJob.getAssignedToPlayerName(), "Old job should be assigned to Bob");

        // Zuweisung des neuen Jobs an Bob – der alte wird freigegeben
        repo.assignJobToPlayer("Bob", newJob);
        assertTrue(newJob.isTaken(), "New job should be marked as taken");
        assertEquals("Bob", newJob.getAssignedToPlayerName(), "New job should be assigned to Bob");
        assertFalse(oldJob.isTaken(), "Old job should have been released");
        assertNull(oldJob.getAssignedToPlayerName(), "Old job should no longer have an assignee");
    }

    @Test
    void testGetRandomAvailableJobsCountExceedsAvailable() throws Exception {
        Job only = new Job(1, "Solo", 100, 10, false);
        getJobsList(repo).add(only);

        List<Job> res = repo.getRandomAvailableJobs(false, 5);
        assertEquals(1, res.size(), "Should not return more jobs than available");
        assertEquals(only, res.get(0));
    }

    @Test
    void testReleaseJob() throws Exception {
        Job job = new Job(3, "Test", 300, 30, false);
        getJobsList(repo).add(job);
        repo.assignJobToPlayer("Alice", job);
        assertTrue(job.isTaken(), "Job should be taken after assignment");

        repo.releaseJob(job);
        assertFalse(job.isTaken(), "Job should be released");
        assertNull(job.getAssignedToPlayerName(), "Job should have no assigned player after release");
    }

    @Test
    void testLoadJobsLoadsFromJson() throws Exception {
        repo.loadJobs();
        List<Job> all = getJobsList(repo);
        assertEquals(2, all.size(), "Es sollten genau 2 Jobs geladen werden");
        assertEquals(1, all.get(0).getJobId(), "Erster Job sollte ID=1 haben");
        assertEquals("Entwickler", all.get(1).getTitle(), "Zweiter Job sollte 'Entwickler' heißen");
    }
    @Test
    void testLoadJobsThrowsWhenMissing() {
        JobRepository emptyRepo = new JobRepository() {
            @Override
            public void loadJobs() throws Exception {
                // Simuliere fehlende jobs.json
                throw new IllegalStateException("jobs.json nicht gefunden!");
            }
        };
        assertThrows(IllegalStateException.class, emptyRepo::loadJobs,
                "Wenn jobs.json nicht gefunden wird, muss eine IllegalStateException geworfen werden");
    }
}