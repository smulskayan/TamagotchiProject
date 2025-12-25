package com.example.tamagotchiproject.model;

public class PetStats {
    private int hunger;
    private int happiness;
    private int cleanliness;
    private int energy;

    public PetStats() {
        this(100, 100, 100, 100);
    }

    public PetStats(int hunger, int happiness, int cleanliness, int energy) {
        setHunger(hunger);
        setHappiness(happiness);
        setCleanliness(cleanliness);
        setEnergy(energy);
    }

    public int getHunger() { return hunger; }
    public void setHunger(int hunger) {
        this.hunger = Math.max(0, Math.min(100, hunger));
    }

    public int getHappiness() { return happiness; }
    public void setHappiness(int happiness) {
        this.happiness = Math.max(0, Math.min(100, happiness));
    }

    public int getCleanliness() { return cleanliness; }
    public void setCleanliness(int cleanliness) {
        this.cleanliness = Math.max(0, Math.min(100, cleanliness));
    }

    public int getEnergy() { return energy; }
    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(100, energy));
    }

    public boolean isAnyStatCritical() {
        return hunger <= 0 || happiness <= 0 || cleanliness <= 0 || energy <= 0;
    }

    public boolean isAnyStatLow() {
        return hunger < 40 || happiness < 40 || cleanliness < 40 || energy < 40;
    }

    public void decreaseStats(int difficultyLevel, int gameSpeed) {
        float HUNGER_DECREASE_RATE = 1.2f;
        float HAPPINESS_DECREASE_RATE = 1.0f;
        float CLEANLINESS_DECREASE_RATE = 0.8f;
        float ENERGY_DECREASE_RATE = 0.9f;

        // Для быстрого режима уменьшаем в 30 раз чаще
        int speedMultiplier = (gameSpeed == 1) ? 30 : 1;

        hunger = Math.max(0, hunger - (int)(difficultyLevel * HUNGER_DECREASE_RATE / speedMultiplier));
        happiness = Math.max(0, happiness - (int)(difficultyLevel * HAPPINESS_DECREASE_RATE / speedMultiplier));
        cleanliness = Math.max(0, cleanliness - (int)(difficultyLevel * CLEANLINESS_DECREASE_RATE / speedMultiplier));
        energy = Math.max(0, energy - (int)(difficultyLevel * ENERGY_DECREASE_RATE / speedMultiplier));

        // Случайные увеличения уменьшения
        if (Math.random() > 0.7) hunger = Math.max(0, hunger - 1);
        if (Math.random() > 0.7) happiness = Math.max(0, happiness - 1);
        if (Math.random() > 0.5) cleanliness = Math.max(0, cleanliness - 1);
        if (Math.random() > 0.6) energy = Math.max(0, energy - 1);
    }

    public void feed() {
        setHunger(hunger + 20);
        setEnergy(energy + 5);
    }

    public void wash() {
        setCleanliness(cleanliness + 25);
        setHappiness(happiness + 5);
    }

    public void play() {
        setHappiness(happiness + 20);
        setEnergy(energy - 10);
    }

    public void rest() {
        setEnergy(energy + 30);
        setHunger(hunger - 5);
    }

    public void reset() {
        setHunger(100);
        setHappiness(100);
        setCleanliness(100);
        setEnergy(100);
    }
}