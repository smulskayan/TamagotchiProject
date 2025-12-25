package com.example.tamagotchiproject.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.tamagotchiproject.model.*;

public class GameRepository {
    private static final String PREFS_NAME = "game_settings";
    private SharedPreferences prefs;

    public GameRepository(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public GameState loadGameState(boolean isNewGame) {
        GameState gameState = new GameState();

        // Проверяем, была ли игра закончена
        boolean wasGameOver = prefs.getBoolean("was_game_over", false);
        if (wasGameOver) {
            isNewGame = true;
        }

        // Загрузка статистики
        PetStats stats;
        if (isNewGame) {
            stats = new PetStats(100, 100, 100, 100);
        } else {
            stats = new PetStats(
                    prefs.getInt("hunger", 100),
                    prefs.getInt("happiness", 100),
                    prefs.getInt("cleanliness", 100),
                    prefs.getInt("energy", 100)
            );
        }
        gameState.setPetStats(stats);

        // Загрузка настроек
        GameSettings settings = new GameSettings();
        settings.setPetName(prefs.getString("pet_name", ""));
        settings.setGameSpeed(prefs.getInt("game_speed", 0));
        settings.setCharacter(prefs.getInt("character", 1));

        long elapsedTime = prefs.getLong("elapsed_time", 0);
        if (isNewGame) {
            elapsedTime = 0;
        }
        settings.setElapsedTime(elapsedTime);
        gameState.setGameSettings(settings);

        // Устанавливаем флаги
        gameState.setNewGame(isNewGame);
        gameState.setGameOver(wasGameOver);

        // Если игра не новая, устанавливаем startTime
        if (!isNewGame && elapsedTime > 0) {
            // Восстанавливаем startTime для корректного расчета elapsedTime
            gameState.setStartTime(System.currentTimeMillis() - elapsedTime);
            gameState.updateDifficultyLevel();
        }

        return gameState;
    }

    public void saveGameState(GameState gameState) {
        SharedPreferences.Editor editor = prefs.edit();
        PetStats stats = gameState.getPetStats();
        GameSettings settings = gameState.getGameSettings();

        editor.putInt("hunger", stats.getHunger());
        editor.putInt("happiness", stats.getHappiness());
        editor.putInt("cleanliness", stats.getCleanliness());
        editor.putInt("energy", stats.getEnergy());

        // Сохраняем прошедшее время
        long elapsedTime = gameState.getElapsedTime();
        editor.putLong("elapsed_time", elapsedTime);

        editor.putInt("game_speed", settings.getGameSpeed());
        editor.putBoolean("was_game_over", false);

        editor.apply();
    }

    public void saveSettings(GameSettings settings) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("pet_name", settings.getPetName());
        editor.putInt("character", settings.getCharacter());
        editor.putInt("game_speed", settings.getGameSpeed());
        editor.apply();
    }

    public void saveBestTime(long elapsedTime, int gameSpeed) {
        // Используем ключ "best_time_0" или "best_time_1"
        String bestTimeKey = "best_time_" + gameSpeed;
        long currentBestTime = prefs.getLong(bestTimeKey, 0);

        if (elapsedTime > currentBestTime) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(bestTimeKey, elapsedTime);
            editor.apply();
        }
    }

    public long getBestTime(int gameSpeed) {
        String key = "best_time_" + gameSpeed;
        return prefs.getLong(key, 0);
    }

    public void markGameOver() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("was_game_over", true);
        editor.apply();
    }

    public void resetGameState() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("hunger");
        editor.remove("happiness");
        editor.remove("cleanliness");
        editor.remove("energy");
        editor.remove("elapsed_time");
        editor.putBoolean("was_game_over", false);
        editor.apply();
    }

    public void clearGameOverFlag() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("was_game_over", false);
        editor.apply();
    }
}