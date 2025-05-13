package at.aau.serg.websocketserver.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobServiceTest {
    private JobService service;
    private ObjectProvider<JobRepository> provider;

    /**
     * Ein Repository, das loadJobs() überspringt, damit wir in den meisten Tests
     * keine echte JSON-Datei laden müssen.
     */
    private static class TestJobRepository extends JobRepository {
        @Override
        public void loadJobs() {
            // no-op
        }
    }

    @BeforeEach
    void setUp() {
        provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(provider.getObject())
                .thenAnswer(inv -> new TestJobRepository());
        service = new JobService(provider);
    }

    @Test
    void getOrCreateRepository_DifferentGameIds_CreateDistinctAndSameIdSameInstance() {
        JobRepository repo1 = service.getOrCreateRepository(1);
        JobRepository repo2 = service.getOrCreateRepository(2);
        assertNotNull(repo1);
        assertNotNull(repo2);
        assertNotSame(repo1, repo2, "Repositories für verschiedene gameIds müssen verschieden sein");

        // derselbe gameId liefert dieselbe Instanz
        JobRepository again1 = service.getOrCreateRepository(1);
        assertSame(repo1, again1, "Dasselbe gameId muss dieselbe Repository-Instanz zurückgeben");
    }

    @Test
    void removeRepository_AllowsRecreation() {
        JobRepository repo1 = service.getOrCreateRepository(1);
        service.removeRepository(1);
        JobRepository repo1New = service.getOrCreateRepository(1);
        assertNotSame(repo1, repo1New, "Nach removeRepository sollte ein neues Repo angelegt werden");
    }


    @Test
    void loadJobs_populatesRepositoryFromJson() throws Exception {
        // Arrange: JobRepository lädt aus src/test/resources/jobs.json
        JobRepository repo = new JobRepository();

        // Act
        repo.loadJobs();

        // Assert: genau die beiden Einträge aus der JSON sind da
        assertTrue(repo.findJobById(1).isPresent(), "JobId=1 sollte geladen werden");
        assertTrue(repo.findJobById(2).isPresent(), "JobId=2 sollte geladen werden");

        // Ein nicht in der JSON enthaltener Job darf nicht gefunden werden
        assertTrue(repo.findJobById(999).isEmpty(), "JobId=999 darf nicht geladen sein");
    }

    @Test
    void loadJobsInstancesAreIndependent_AssignmentInOneDoesNotAffectOther() throws Exception {
        // Zwei frische Repositories, die jeweils aus derselben JSON laden
        JobRepository repo1 = new JobRepository();
        JobRepository repo2 = new JobRepository();
        repo1.loadJobs();
        repo2.loadJobs();

        // Hole jeweils den Job mit ID=1 aus beiden Repos
        Job job1Repo1 = repo1.findJobById(1)
                .orElseThrow(() -> new AssertionError("JobId=1 in repo1 nicht gefunden"));
        Job job1Repo2 = repo2.findJobById(1)
                .orElseThrow(() -> new AssertionError("JobId=1 in repo2 nicht gefunden"));

        // Stelle sicher, dass es sich um zwei unterschiedliche Instanzen handelt
        assertNotSame(job1Repo1, job1Repo2, "Die Job-Instanzen müssen voneinander unabhängig sein");

        // Weise nur im ersten Repository den Spieler zu
        repo1.assignJobToPlayer("Alice", job1Repo1);

        // Prüfe, dass nur das erste Repo betroffen ist
        assertTrue(job1Repo1.isTaken(), "Job in repo1 sollte vergeben sein");
        assertEquals("Alice", job1Repo1.getAssignedToPlayerName(), "repo1 muss Alice zugewiesen sein");

        // Im zweiten Repo darf sich nichts ändern
        assertFalse(job1Repo2.isTaken(), "Job in repo2 darf nicht betroffen sein");
        assertNull(job1Repo2.getAssignedToPlayerName(), "repo2 darf keinen Spieler haben");
    }


}
