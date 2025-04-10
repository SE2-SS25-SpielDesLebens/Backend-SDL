package at.aau.serg.websocketserver.websocket.broker;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Player {

    private final String name;
    private final int id;
    private final int money;
    private final int investments;
    private final int salary;
    private final int children;
    private final String education;
    private final String relationship;
    private final String career;

    public Player(String name, int id, int money, int investments, int salary, int children, String education, String relationship, String career) {
        this.name = name;
        this.id = id;
        this.money = money;
        this.investments = investments;
        this.salary = salary;
        this.children = children;
        this.education = education;
        this.relationship = relationship;
        this.career = career;
    }

    @JsonProperty("id") public int getId() { return id; }
    @JsonProperty("name") public String getName() { return name; }
    @JsonProperty("money") public int getMoney() { return money; }
    @JsonProperty("investments") public int getInvestments() { return investments; }
    @JsonProperty("salary") public int getSalary() { return salary; }
    @JsonProperty("children") public int getChildren() { return children; }
    @JsonProperty("education") public String getEducation() { return education; }
    @JsonProperty("relationship") public String getRelationship() { return relationship; }
    @JsonProperty("career") public String getCareer() { return career; }
}
