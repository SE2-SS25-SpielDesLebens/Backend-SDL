package at.aau.serg.websocketdemoserver.game;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Player {

    private String name;
    private int id;
    private int money;
    private int investments;
    private int salary;
    private int children;
    private String education;
    private String relationship;

    public Player(String name, int id, int money, int investments, int salary, int children, String education, String relationship) {
        this.name = name;
        this.id = id;
        this.money = money;
        this.investments = investments;
        this.salary = salary;
        this.children = children;
        this.education = education;
        this.relationship = relationship;
    }

    @JsonProperty("id") public int getId() { return id; }
    @JsonProperty("name") public String getName() { return name; }
    @JsonProperty("money") public int getMoney() { return money; }
    @JsonProperty("investments") public int getInvestments() { return investments; }
    @JsonProperty("salary") public int getSalary() { return salary; }
    @JsonProperty("children") public int getChildren() { return children; }
    @JsonProperty("education") public String getEducation() { return education; }
    @JsonProperty("relationship") public String getRelationship() { return relationship; }


}
