package com.example.tamagotchiproject.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.tamagotchiproject.model.*;
import com.example.tamagotchiproject.repository.GameRepository;

import java.util.Map;

public class MainViewModel extends AndroidViewModel {
    private GameRepository repository;
    private MutableLiveData<GameSettings> gameSettings = new MutableLiveData<>();
    private MutableLiveData<String> bestTimeMedium = new MutableLiveData<>();
    private MutableLiveData<String> bestTimeFast = new MutableLiveData<>();

    public MainViewModel(Application application) {
        super(application);
        repository = new GameRepository(application);
    }

    public void loadData() {
        GameState gameState = repository.loadGameState(true);
        if (gameState != null) {
            this.gameSettings.setValue(gameState.getGameSettings());
        }

        updateBestTimes();
    }
    public void updateBestTimes() {
        long mediumTime = repository.getBestTime(0);
        long fastTime = repository.getBestTime(1);

        bestTimeMedium.setValue("Рекорд (средний): " + formatTime(mediumTime));
        bestTimeFast.setValue("Рекорд (быстрый): " + formatTime(fastTime));
    }

    private String formatTime(long milliseconds) {
        if (milliseconds <= 0) {
            return "00:00:00";
        }

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public LiveData<GameSettings> getGameSettings() {
        return gameSettings;
    }

    public LiveData<String> getBestTimeMedium() {
        return bestTimeMedium;
    }

    public LiveData<String> getBestTimeFast() {
        return bestTimeFast;
    }
}