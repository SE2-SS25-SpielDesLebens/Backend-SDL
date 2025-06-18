package at.aau.serg.websocketserver.session.job;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class JobService {

    private final Map<String, JobRepository> repositories = new ConcurrentHashMap<>();
    private final ObjectProvider<JobRepository> jobRepoProvider;

    public JobService(ObjectProvider<JobRepository> jobRepoProvider) {
        this.jobRepoProvider = jobRepoProvider;
    }


    public JobRepository getOrCreateRepository(String gameId) {
        return repositories.computeIfAbsent(gameId, id -> {
            JobRepository repo = jobRepoProvider.getObject();
            try {
                repo.loadJobs();
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Fehler beim Laden der jobs.json für Spiel " + id, e
                );
            }
            return repo;
        });
    }

    public void removeRepository(String gameId) {
        repositories.remove(gameId);
    }
}

