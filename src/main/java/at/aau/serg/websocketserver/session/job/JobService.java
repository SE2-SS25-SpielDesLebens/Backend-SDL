package at.aau.serg.websocketserver.session.job;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet pro Spiel‐ID (int) genau eine JobRepository‐Instanz.
 */
@Service
public class JobService {

    private final Map<Integer, JobRepository> repositories = new ConcurrentHashMap<>();
    private final ObjectProvider<JobRepository> jobRepoProvider;

    public JobService(ObjectProvider<JobRepository> jobRepoProvider) {
        this.jobRepoProvider = jobRepoProvider;
    }

    /**
     * Holt das vorhandene Repository zur gameId oder legt ein neues an.
     */
    public JobRepository getOrCreateRepository(int gameId) {
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

    /**
     * Entfernt das Repository, z. B. beim Beenden einer Spiel‐Session.
     */
    public void removeRepository(int gameId) {
        repositories.remove(gameId);
    }
}

