package at.aau.serg.websocketserver.Player;

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
    private final int jobID;
    private final int houseID;
    private final int fieldID;

    public Player(String name, int id, int money, int investments, int salary, int children, String education, String relationship, String career, int jobID, int HouseID, int fieldID) {
        this.name = name;
        this.id = id;
        this.money = money;
        this.investments = investments;
        this.salary = salary;
        this.children = children;
        this.education = education;
        this.relationship = relationship;
        this.career = career;
        this.jobID = jobID;
        this.houseID = HouseID;
        this.fieldID = fieldID;
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
    @JsonProperty("jobId") public int getJobId() { return jobID; }
    @JsonProperty("houseId") public int getHouseId() { return houseID; }
    @JsonProperty("fieldId") public int getFieldID(){return fieldID;}
}
