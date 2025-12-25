package com.example.tamagotchiproject.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.tamagotchiproject.model.*;
import com.example.tamagotchiproject.repository.GameRepository;

import java.util.Timer;
import java.util.TimerTask;

public class GameViewModel extends AndroidViewModel {
    private GameRepository repository;
    private Timer gameTimer;
    private Timer statTimer;

    // Коэффициенты уменьшения для разных показателей
    private final float HUNGER_DECREASE_RATE = 1.2f;
    private final float HAPPINESS_DECREASE_RATE = 1.0f;
    private final float CLEANLINESS_DECREASE_RATE = 0.8f;
    private final float ENERGY_DECREASE_RATE = 0.9f;

    private int decreaseCounter = 0;
    private int difficultyLevel = 1;
    private boolean isNewGame = true;

    private MutableLiveData<PetStats> petStats = new MutableLiveData<>();
    private MutableLiveData<GameSettings> gameSettings = new MutableLiveData<>();
    private MutableLiveData<GameState> gameState = new MutableLiveData<>();
    private MutableLiveData<String> timerText = new MutableLiveData<>();
    private MutableLiveData<Boolean> isGameOver = new MutableLiveData<>(false);
    private MutableLiveData<Integer> difficultyLevelLive = new MutableLiveData<>(1);

    public GameViewModel(Application application) {
        super(application);
        repository = new GameRepository(application);
        loadGameState();
    }

    private void loadGameState() {
        GameState loadedState = repository.loadGameState(isNewGame);
        gameState.setValue(loadedState);
        petStats.setValue(loadedState.getPetStats());
        gameSettings.setValue(loadedState.getGameSettings());

        // Обновляем локальные переменные
        difficultyLevel = loadedState.getDifficultyLevel();
        difficultyLevelLive.setValue(difficultyLevel);
        isNewGame = loadedState.isNewGame();

        startTimers();
    }

    private void startTimers() {
        stopTimers();

        // Таймер обновления времени
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                GameState currentState = gameState.getValue();
                if (currentState != null && !currentState.isPaused() && !currentState.isGameOver()) {
                    currentState.updateElapsedTime();

                    // Обновляем уровень сложности
                    updateDifficultyLevel(currentState.getElapsedTime());

                    String formattedTime = formatTime(currentState.getElapsedTime());
                    timerText.postValue(formattedTime);

                    // Проверяем конец игры
                    if (currentState.isAnyStatCritical()) {
                        // ВАЖНО: Сохраняем лучшее время перед окончанием
                        saveBestTime();

                        isGameOver.postValue(true);
                        stopTimers();
                        repository.markGameOver();
                    }
                }
            }
        }, 0, 1000);

        // Таймер уменьшения характеристик
        statTimer = new Timer();
        statTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                GameState currentState = gameState.getValue();
                if (currentState != null && !currentState.isPaused() && !currentState.isGameOver()) {
                    decreaseStats(currentState);
                    gameState.postValue(currentState);
                    petStats.postValue(currentState.getPetStats());
                }
            }
        }, 1000, 1000);
    }

    private void decreaseStats(GameState gameState) {
        GameSettings settings = gameState.getGameSettings();
        PetStats stats = gameState.getPetStats();

        decreaseCounter++;
        int interval = (settings.getGameSpeed() == 0) ? 30 : 1;

        if (decreaseCounter >= interval) {
            decreaseCounter = 0;

            int baseDecrease = difficultyLevel;

            int hungerDecrease = Math.max(1, Math.round(baseDecrease * HUNGER_DECREASE_RATE));
            int happinessDecrease = Math.max(1, Math.round(baseDecrease * HAPPINESS_DECREASE_RATE));
            int cleanlinessDecrease = Math.max(1, Math.round(baseDecrease * CLEANLINESS_DECREASE_RATE));
            int energyDecrease = Math.max(1, Math.round(baseDecrease * ENERGY_DECREASE_RATE));

            // Случайные увеличения как в оригинале
            if (Math.random() > 0.7) hungerDecrease++;
            if (Math.random() > 0.7) happinessDecrease++;
            if (Math.random() > 0.5) cleanlinessDecrease++;
            if (Math.random() > 0.6) energyDecrease++;

            // Уменьшаем характеристики
            stats.setHunger(stats.getHunger() - hungerDecrease);
            stats.setHappiness(stats.getHappiness() - happinessDecrease);
            stats.setCleanliness(stats.getCleanliness() - cleanlinessDecrease);
            stats.setEnergy(stats.getEnergy() - energyDecrease);
        }
    }

    private void updateDifficultyLevel(long elapsedTime) {
        long minutes = elapsedTime / 60000;
        int newLevel;

        if (minutes < 1) {
            newLevel = 1;
        } else if (minutes < 2) {
            newLevel = 2;
        } else if (minutes < 5) {
            newLevel = 3;
        } else if (minutes < 10) {
            newLevel = 4;
        } else {
            newLevel = 5;
        }

        if (newLevel != difficultyLevel) {
            difficultyLevel = newLevel;
            difficultyLevelLive.postValue(difficultyLevel);
        }
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void feedPet() {
        updateStats(stats -> {
            stats.setHunger(Math.min(100, stats.getHunger() + 20));
            stats.setEnergy(Math.min(100, stats.getEnergy() + 5));
            return stats;
        });
    }

    public void washPet() {
        updateStats(stats -> {
            stats.setCleanliness(Math.min(100, stats.getCleanliness() + 25));
            stats.setHappiness(Math.min(100, stats.getHappiness() + 5));
            return stats;
        });
    }
    public void saveBestTimeNow() {
        saveBestTime();
    }

    public void playWithPet() {
        updateStats(stats -> {
            stats.setHappiness(Math.min(100, stats.getHappiness() + 20));
            stats.setEnergy(Math.max(0, stats.getEnergy() - 10));
            return stats;
        });
    }

    public void restPet() {
        updateStats(stats -> {
            stats.setEnergy(Math.min(100, stats.getEnergy() + 30));
            stats.setHunger(Math.max(0, stats.getHunger() - 5));
            return stats;
        });
    }

    private void updateStats(StatsUpdater updater) {
        PetStats currentStats = petStats.getValue();
        if (currentStats != null) {
            PetStats newStats = updater.update(currentStats);
            petStats.setValue(newStats);

            // Обновляем GameState
            GameState currentState = gameState.getValue();
            if (currentState != null) {
                currentState.setPetStats(newStats);
                gameState.setValue(currentState);
            }
        }
    }

    public void pauseGame() {
        GameState currentState = gameState.getValue();
        if (currentState != null) {
            currentState.pauseGame();
            gameState.setValue(currentState);
            // Сохраняем состояние при паузе
            saveGame();
        }
    }

    public void resumeGame() {
        GameState currentState = gameState.getValue();
        if (currentState != null) {
            currentState.resumeGame();
            gameState.setValue(currentState);
        }
    }

    public void resetGame() {
        GameState currentState = gameState.getValue();
        if (currentState != null) {
            currentState.resetGame();
            gameState.setValue(currentState);
            petStats.setValue(currentState.getPetStats());
            isGameOver.setValue(false);
            isNewGame = true;
            decreaseCounter = 0;
            difficultyLevel = 1;
            difficultyLevelLive.setValue(1);
            repository.resetGameState();
            stopTimers();
            startTimers();
        }
    }

    public void saveBestTime() {
        GameState currentState = gameState.getValue();
        GameSettings settings = gameSettings.getValue();
        if (currentState != null && settings != null) {
            // Добавим логи для отладки
            android.util.Log.d("GameViewModel",
                    "saveBestTime: elapsedTime=" + currentState.getElapsedTime() +
                            ", gameSpeed=" + settings.getGameSpeed());

            // Сохраняем лучшее время для текущего режима
            repository.saveBestTime(
                    currentState.getElapsedTime(),
                    settings.getGameSpeed()
            );
        } else {
            android.util.Log.e("GameViewModel", "Не удалось сохранить лучшее время: данные null");
        }
    }

    public void saveGame() {
        GameState currentState = gameState.getValue();
        if (currentState != null) {
            repository.saveGameState(currentState);
        }
    }

    private void stopTimers() {
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }
        if (statTimer != null) {
            statTimer.cancel();
            statTimer = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        saveGame();
        stopTimers();
    }

    // LiveData геттеры
    public LiveData<PetStats> getPetStats() { return petStats; }
    public LiveData<GameSettings> getGameSettings() { return gameSettings; }
    public LiveData<GameState> getGameState() { return gameState; }
    public LiveData<String> getTimerText() { return timerText; }
    public LiveData<Boolean> getIsGameOver() { return isGameOver; }
    public LiveData<Integer> getDifficultyLevel() { return difficultyLevelLive; }

    interface StatsUpdater {
        PetStats update(PetStats stats);
    }
}