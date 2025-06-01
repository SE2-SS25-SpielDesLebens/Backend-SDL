package at.aau.serg.websocketserver.player;

import at.aau.serg.websocketserver.session.job.Job;
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
    private Map<Integer, Integer> houseID;
    @Getter
    private boolean isMarried;
    private int childrenCount;
    @Getter
    private boolean isRetired;
    @Getter
    private String carColor;
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
    @Getter
    private int autoPassengers = 0; // Max 5 weitere erlaubt

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

    //💼Job&Einkommen
    public void assignJob(Job newJob) {
        job = newJob;
    }

    public void clearJob() {
        this.job = null;
    }

    public boolean hasJob() {
        return this.job != null;
    }

    //💰Geld
    public void addMoney(int amount) {
        money += amount;
    }

    public void removeMoney(int amount) {
        money -= amount;
    }

    //💳Schulden
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
        int costPerLoan = 0;
        if (debts <= 0 || money < costPerLoan) return;
        removeMoney(costPerLoan);
        debts--;
    }

    //👶Familie
    public void marry() {
        if (isMarried) throw new IllegalStateException("Spieler ist bereits verheiratet.");
        this.isMarried = true;
    }

    public void addChild() {
        this.childrenCount++;
    }

    public boolean canHaveMoreChildren(int count) {
        return !canAddPassengers(count) && (childrenCount + count <= 4);
    }

    public void addChildrenWithCarCheck(int count) {
        if (!canHaveMoreChildren(count)) {
            throw new IllegalStateException("Nicht genug Platz für mehr Kinder.");
        }
        this.childrenCount += count;
        addPassenger(count);
    }

    /**
     *Führt eine Investition durch – Spieler zahlt einen Betrag und erhält ein Investment-Slot.
     *Beispiel: investMoney(50000);
     */
    public void investMoney(int amount) {
        if (investments > 0) {
            throw new IllegalStateException("❗ Spieler hat bereits investiert.");
        }
        if (money < amount) {
            throw new IllegalStateException("❌ Nicht genug Geld für eine Investition.");
        }

        removeMoney(amount);

        /**
         * Diese Zufallszahl ist rein für Spielzwecke, daher ist ThreadLocalRandom hier ausreichend.
         */
        int chosenNumber = 1 + java.util.concurrent.ThreadLocalRandom.current().nextInt(10);
        //zufällige Zahl zwischen 1–10
        this.investments = chosenNumber;
        this.investmentPayout = 0;

        System.out.println("💸 Spieler " + id + " investiert " + amount + "auf Zahl" + chosenNumber);
    }


    //🐾Freund,Haustier,Zwilling
    public void addPassengerWithLimit(String type, int count) {
        if (canAddPassengers(count)) {
            throw new IllegalStateException("🚗Kein Platz mehr im Auto für: " + type);
        }
        addPassenger(count);
    }

    //🚘Auto
    public boolean canAddPassengers(int count) {
        return autoPassengers + count > 5;
    }

    public void addPassenger(int count) {
        this.autoPassengers += count;
    }

    //🏠Häuser
    public void addHouse(int houseId, int houseValue) {
        this.houseID.put(houseId, houseValue);
    }

    public void removeHouse(int houseId) {
        this.houseID.remove(houseId);
    }

    //🎓Studium
    public boolean hasDegree() {
        return this.university;
    }

    public void setDegree(boolean value) {
        this.university = value;
    }

    public boolean mustRepeatExam() {
        return mustRepeatExam;
    }

    //🧓Rente
    public void retire() {
        this.isRetired = true;
        this.isActive = false;
    }

    //Ereignisse
    public void handleEvent(String eventType) {
        switch (eventType.toLowerCase()) {
            case "heirat":
                marry();
                System.out.println("💍 Spieler " + id + " hat geheiratet.");
                break;

            case "kind":
                addChildrenWithCarCheck(1);
                System.out.println("👶 Spieler " + id + " hat ein Kind. Plätze im Auto: " + autoPassengers);
                break;

            case "zwilling":
                addChildrenWithCarCheck(2);
                System.out.println("👶👶 Spieler " + id + " hat Zwillinge. Plätze im Auto: " + autoPassengers);
                break;

            case "freund":
                addPassengerWithLimit("Freund", 1);
                System.out.println("🤝 Spieler " + id + " hat einen Freund. Plätze im Auto: " + autoPassengers);
                break;

            case "tier":
                addPassengerWithLimit("Haustier", 1);
                System.out.println("🐶 Spieler " + id + " hat ein Haustier. Plätze im Auto: " + autoPassengers);
                break;

            default:
                throw new IllegalArgumentException("❌ Unbekanntes Ereignis: " + eventType);
        }
    }

    //✅JSON-Properties zb. für WebSocket oder REST-Ausgabe)
    @JsonProperty("id") public String getId() { return id; }
    @JsonProperty("money") public int getMoney() { return money; }
    @JsonProperty("investments") public int getInvestments() { return investments; }
    @JsonProperty("salary") public int getSalary() { return salary; }
    @JsonProperty("children") public int getChildren() { return childrenCount; }
    @JsonProperty("education") public boolean getEducation() { return university; }
    @JsonProperty("relationship") public boolean getRelationship() { return isMarried; }
    @JsonProperty("jobId") public Job getJobId() { return job; }
    @JsonProperty("houseId") public Map<Integer, Integer> getHouseId() { return houseID; }
    @JsonProperty("fieldId") public int getFieldID() { return fieldId; }
}
