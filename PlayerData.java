package de.delayeddamage;

public class PlayerData {
    private long timerSeconds;
    private double storedDamage;

    public PlayerData() {
        this.timerSeconds = 0;
        this.storedDamage = 0.0;
    }

    public PlayerData(long timerSeconds, double storedDamage) {
        this.timerSeconds = timerSeconds;
        this.storedDamage = storedDamage;
    }

    public void incrementTimer() {
        this.timerSeconds++;
    }

    public void resetTimer() {
        this.timerSeconds = 0;
    }

    public void addDamage(double damage) {
        this.storedDamage += damage;
    }

    public void resetDamage() {
        this.storedDamage = 0.0;
    }

    // Getters
    public long getTimerSeconds() {
        return timerSeconds;
    }

    public double getStoredDamage() {
        return storedDamage;
    }

    // Setters
    public void setTimerSeconds(long timerSeconds) {
        this.timerSeconds = timerSeconds;
    }

    public void setStoredDamage(double storedDamage) {
        this.storedDamage = storedDamage;
    }
}