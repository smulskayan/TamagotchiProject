package com.example.tamagotchiproject.model;

public class GameSettings {
    private String petName;
    private int character; // 1 - панда, 2 - кот, 3 - лягушка
    private int gameSpeed; // 0 - средний, 1 - быстрый
    private long elapsedTime;

    public GameSettings() {
        this.petName = "";
        this.character = 1;
        this.gameSpeed = 0;
        this.elapsedTime = 0;
    }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public int getCharacter() { return character; }
    public void setCharacter(int character) { this.character = character; }

    public int getGameSpeed() { return gameSpeed; }
    public void setGameSpeed(int gameSpeed) { this.gameSpeed = gameSpeed; }

    public long getElapsedTime() { return elapsedTime; }
    public void setElapsedTime(long elapsedTime) { this.elapsedTime = elapsedTime; }
}