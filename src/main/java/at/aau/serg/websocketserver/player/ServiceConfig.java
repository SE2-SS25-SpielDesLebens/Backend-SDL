package at.aau.serg.websocketserver.player;

import at.aau.serg.websocketserver.player.PlayerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public PlayerService playerService() {
        return PlayerService.getInstance(); // Singleton
    }
}
