package com.example.tamagotchiproject.model;

public class GameState {
    private PetStats petStats;
    private GameSettings gameSettings;
    private long startTime;
    private long pausedTime;
    private boolean isPaused;
    private boolean isGameOver;
    private boolean isNewGame;
    private int difficultyLevel = 1;

    public GameState() {
        this.petStats = new PetStats(100, 100, 100, 100);
        this.gameSettings = new GameSettings();
        this.startTime = System.currentTimeMillis();
        this.isPaused = false;
        this.isGameOver = false;
        this.isNewGame = true;
    }

    public void updateElapsedTime() {
        // Прошедшее время обновляется автоматически через getElapsedTime()
    }

    public void updateDifficultyLevel() {
        long minutes = getElapsedTime() / 60000;

        if (minutes < 1) {
            difficultyLevel = 1;
        } else if (minutes < 2) {
            difficultyLevel = 2;
        } else if (minutes < 5) {
            difficultyLevel = 3;
        } else if (minutes < 10) {
            difficultyLevel = 4;
        } else {
            difficultyLevel = 5;
        }
    }

    public void pauseGame() {
        if (!isPaused) {
            isPaused = true;
            pausedTime = System.currentTimeMillis();
        }
    }

    public void resumeGame() {
        if (isPaused) {
            long pauseDuration = System.currentTimeMillis() - pausedTime;
            startTime += pauseDuration; // Корректируем startTime на время паузы
            isPaused = false;
        }
    }

    public void resetGame() {
        petStats.reset();
        startTime = System.currentTimeMillis();
        gameSettings.setElapsedTime(0);
        isGameOver = false;
        isPaused = false;
        isNewGame = true;
        difficultyLevel = 1;
    }

    // Новый метод для установки startTime при загрузке
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    // Геттеры и сеттеры
    public PetStats getPetStats() { return petStats; }
    public void setPetStats(PetStats petStats) { this.petStats = petStats; }

    public GameSettings getGameSettings() { return gameSettings; }
    public void setGameSettings(GameSettings gameSettings) { this.gameSettings = gameSettings; }

    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { isPaused = paused; }

    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean gameOver) { isGameOver = gameOver; }

    public boolean isNewGame() { return isNewGame; }
    public void setNewGame(boolean newGame) { isNewGame = newGame; }

    public int getDifficultyLevel() { return difficultyLevel; }

    public long getElapsedTime() {
        if (isPaused) {
            return pausedTime - startTime;
        } else {
            return System.currentTimeMillis() - startTime;
        }
    }

    public boolean isAnyStatCritical() {
        return petStats.isAnyStatCritical();
    }

    public boolean isAnyStatLow() {
        return petStats.isAnyStatLow();
    }
}