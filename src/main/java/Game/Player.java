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
    private int houseValue;
    private boolean married;
    private int childrenCount;
    private boolean retired;
    private String carColor;
    private boolean active;
    private boolean hasPoliceCard;

    private int baseSalary;
    private int salaryBonus;

    public Player(String id) {
        this.id = id;
        this.money = 0;
        this.debts = 0;
        this.lifeCards = new ArrayList<>();
        this.shareJoyCards = new ArrayList<>();
        this.married = false;
        this.childrenCount = 0;
        this.retired = false;
        this.active = false;
        this.hasPoliceCard = false;
        this.baseSalary = 0;
        this.salaryBonus = 0;
    }

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

    public int getDebts() {
        return debts;
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

    public void clearJob() {
        this.job = null;
        this.academicJob = null;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public int getHouseValue() {
        return houseValue;
    }

    public void setHouseValue(int value) {
        this.houseValue = value;
    }

    public void removeHouse() {
        this.house = null;
        this.houseValue = 0;
    }

    public boolean isMarried() {
        return married;
    }

    public void marry() {
        this.married = true;
    }

    public int getChildren() {
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
        this.active = false;
    }

    public String getCarColor() {
        return carColor;
    }

    public void setCarColor(String color) {
        this.carColor = color;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean hasPoliceCard() {
        return hasPoliceCard;
    }

    public void assignPoliceCard() {
        this.hasPoliceCard = true;
    }

    public int getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(int baseSalary) {
        this.baseSalary = baseSalary;
    }

    public int getSalaryBonus() {
        return salaryBonus;
    }

    public void addSalaryBonus(int bonus) {
        this.salaryBonus += bonus;
    }

    public int getTotalSalary() {
        return baseSalary + salaryBonus;
    }
}


