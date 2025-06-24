package at.aau.serg.websocketserver.session.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobServiceTest {

    private ObjectProvider<JobRepository> provider;
    private JobService service;
    private JobRepository repo1;
    private JobRepository repo2;

    @BeforeEach
    void setUp() {
        provider = mock(ObjectProvider.class);
        repo1 = mock(JobRepository.class);
        repo2 = mock(JobRepository.class);

        // Standard: provider.getObject() liefert beim ersten Aufruf repo1, dann repo2
        when(provider.getObject())
                .thenReturn(repo1)
                .thenReturn(repo2);

        service = new JobService(provider);
    }

    @Test
    void getOrCreateRepository_firstTime_loadsAndReturns() throws Exception {
        // repo1.loadJobs() wirft keine Exception
        JobRepository result = service.getOrCreateRepository(42);

        // Rückgabe ist repo1
        assertThat(result).isSameAs(repo1);

        // loadJobs wurde genau einmal aufgerufen
        verify(repo1, times(1)).loadJobs();
        // provider.getObject() wurde genau einmal aufgerufen
        verify(provider, times(1)).getObject();
    }

    @Test
    void getOrCreateRepository_secondTime_sameInstanceNoReload() throws Exception {
        // Erstaufruf
        JobRepository first = service.getOrCreateRepository(7);
        // Zweitaufruf
        JobRepository second = service.getOrCreateRepository(7);

        assertThat(second).isSameAs(first);

        // provider und loadJobs nur beim ersten Aufruf
        verify(provider, times(1)).getObject();
        verify(first, times(1)).loadJobs();
    }

    @Test
    void removeRepository_allowsRecreation() throws Exception {
        // Erstaufruf mit repo1
        JobRepository first = service.getOrCreateRepository(1);
        assertThat(first).isSameAs(repo1);
        verify(provider, times(1)).getObject();
        verify(repo1, times(1)).loadJobs();

        // Repository entfernen
        service.removeRepository(1);

        // Nächster Aufruf für dieselbe gameId liefert neues repo
        JobRepository second = service.getOrCreateRepository(1);
        assertThat(second).isSameAs(repo2);
        verify(provider, times(2)).getObject();
        verify(repo2, times(1)).loadJobs();
    }

    @Test
    void getOrCreateRepository_loadThrows_wrappsInIllegalStateException() throws Exception {
        // Stub: repo1.loadJobs() wirft Exception
        doThrow(new RuntimeException("I/O error")).when(repo1).loadJobs();

        Throwable thrown = catchThrowable(() -> service.getOrCreateRepository(99));

        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Fehler beim Laden der jobs.json für Spiel 99")
                .hasCauseInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("I/O error");

        // provider.getObject() wurde aufgerufen, loadJobs auch
        verify(provider, times(1)).getObject();
        verify(repo1, times(1)).loadJobs();
    }

    @Test
    void removeRepository_nonExistingDoesNothing() {
        // Kein vorheriges Hinzufügen, remove darf nicht throwen
        service.removeRepository(123);
        // Nach Entfernen muss ein neuer Aufruf wieder ein Repo anlegen
        when(provider.getObject()).thenReturn(repo2);
        JobRepository r = service.getOrCreateRepository(123);
        assertThat(r).isSameAs(repo2);
    }
}
