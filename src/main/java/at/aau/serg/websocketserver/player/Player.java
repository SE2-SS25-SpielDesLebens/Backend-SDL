package at.aau.serg.websocketserver.player;

import at.aau.serg.websocketserver.session.Job;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;


@Setter

public class Player {
    private final String id;
    private int money;
    @Getter
    private int debts;
    private int salary;
    private int investments;
    private Job job;
    private Map<Integer,Integer> houseID;//Vorläufige houseID wird ersetzt wenn richtige houseID implementierung da ist
    @Getter
    private boolean isMarried;
    private int childrenCount;
    @Getter
    private boolean isRetired;
    @Getter
    private String carColor;//eventuell auf enum ändern
    @Getter
    private boolean isActive;
    @Getter
    private boolean isHost;
    private int fieldId;
    private boolean university;
    @Getter
    private int investmentPayout;
    @Setter
    private boolean mustRepeatExam = false;





    public Player(String id) {
        this.id = id;
        this.money = 0;
        this.debts = 0;
        this.university = false;
        this.isMarried = false;
        this.childrenCount = 0;
        this.isRetired = false;
        this.isActive = false;
        this.salary = 0;
        this.houseID = new HashMap<>();
    }




    public void assignJob(Job newJob){
        job = newJob;
    }

    public void addMoney(int amount) {
        money += amount;
    }

    public void removeMoney(int amount) {
        money -= amount;
    }



    public void addDebt() {
        debts += 1;
    }

    public void resetDebts() {
        this.debts = 0;
    }

    public void takeLoan() {
        addDebt();
        addMoney(20000);
    }

    public void repayLoan() {
        if (debts > 0 && money >= 25000) {
            removeMoney(25000);
            debts--;
        }
    }

    public boolean mustRepeatExam() {
        return mustRepeatExam;
    }

    public void addHouse(int houseId, int houseValue) {
        this.houseID.put(houseId, houseValue);  // Fügt das Haus zur Map hinzu
    }



    public void clearJob() {
        this.job = null;
    }



    public void removeHouse(int houseId) {
        this.houseID.remove(houseId);
    }



    public void marry() {
        this.isMarried = true;
    }



    public void addChild() {
        this.childrenCount++;
    }



    public void retire() {
        this.isRetired = true;
        this.isActive = false;
    }

    @JsonProperty("id") public String getId() { return id; }
    @JsonProperty("money") public int getMoney() { return money; }
    @JsonProperty("investments") public int getInvestments() { return investments; }
    @JsonProperty("salary") public int getSalary() { return salary; }
    @JsonProperty("children") public int getChildren() { return childrenCount; }
    @JsonProperty("education") public boolean getEducation() { return university; }
    @JsonProperty("relationship") public boolean getRelationship() { return isMarried; }
    @JsonProperty("jobId") public Job getJobId() { return job; }
    @JsonProperty("houseId") public Map<Integer,Integer> getHouseId() { return houseID; }
    @JsonProperty("fieldId") public int getFieldID(){return fieldId;}
}


