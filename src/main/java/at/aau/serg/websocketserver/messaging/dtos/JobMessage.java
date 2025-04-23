package at.aau.serg.websocketserver.messaging.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobMessage {
    private int jobId;
    private String title;
    private int salary;
    private int bonusSalary;
    private boolean requiresDegree;
    private boolean isTaken;
    private String assignedToPlayerName;
    private String playerName;
    private String timestamp;
}
