package at.aau.serg.websocketserver.Player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    @Test
    public void testPlayerCreationAndGetters() {
        // Given
        String name = "Max";
        int id = 1;
        int money = 10000;
        int investments = 5000;
        int salary = 3000;
        int children = 2;
        String education = "Bachelor";
        String relationship = "Single";
        String career = "Lehrer";
        int jobID = 10;
        int houseID = 5;

        // When
        Player player = new Player(name, id, money, investments, salary, children, education, relationship, career, jobID, houseID, 8);

        // Then
        assertEquals(name, player.getName());
        assertEquals(id, player.getId());
        assertEquals(money, player.getMoney());
        assertEquals(investments, player.getInvestments());
        assertEquals(salary, player.getSalary());
        assertEquals(children, player.getChildren());
        assertEquals(education, player.getEducation());
        assertEquals(relationship, player.getRelationship());
        assertEquals(career, player.getCareer());
        assertEquals(jobID, player.getJobId());
        assertEquals(houseID, player.getHouseId());
    }
}
