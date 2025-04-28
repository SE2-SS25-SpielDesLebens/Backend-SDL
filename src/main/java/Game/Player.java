package Game;

import java.util.*;

public class Player {
    private final String id;
    private int money;
    private int debts;
    private final List<String> lifeCards;
    private final List<String> shareJoyCards;
    private String job;
    private String academicJob;
    private String house;
    private boolean married;
    private int childrenCount;
    private boolean retired;

    public Player(String id) {
        this.id = id;
        this.money = 10000; // Startkapital
        this.debts = 0;
        this.lifeCards = new ArrayList<>();
        this.shareJoyCards = new ArrayList<>();
        this.married = false;
        this.childrenCount = 0;
        this.retired = false;
    }

    // Getter und Setter Methoden
    public String getId() {
        return id;
    }
    public int getMoney() {
        return money;
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
    public int getDebts() {
        return debts;
    }
    public List<String> getLifeCards() {
        return lifeCards;
    }
    public void addLifeCard(String card) {
        lifeCards.add(card);
    }
    public List<String> getShareJoyCards() {
        return shareJoyCards;
    }
    public void addShareJoyCard(String card) {
        shareJoyCards.add(card);
    }
    public String getJob() {
        return job;
    }
    public void setJob(String job) {
        this.job = job;
    }
    public String getAcademicJob() {
        return academicJob;
    }
    public void setAcademicJob(String academicJob) {
        this.academicJob = academicJob;
    }
    public String getHouse() {
        return house;
    }
    public void setHouse(String house) {
        this.house = house;
    }
    public boolean isMarried() {
        return married;
    }
    public void marry() {
        this.married = true;
    }
    public int getChildrenCount() {
        return childrenCount;
    }
    public void addChild() {
        this.childrenCount++;
    }
    public boolean isRetired() {
        return retired;
    }
    public void retire() {
        this.retired = true;
    }
}
