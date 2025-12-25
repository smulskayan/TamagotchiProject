package com.example.tamagotchiproject.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.tamagotchiproject.model.*;
import com.example.tamagotchiproject.repository.GameRepository;

public class SettingsViewModel extends AndroidViewModel {
    private GameRepository repository;
    private MutableLiveData<GameSettings> gameSettings = new MutableLiveData<>();

    public SettingsViewModel(Application application) {
        super(application);
        repository = new GameRepository(application);
        loadSettings();
    }

    private void loadSettings() {
        // Загружаем настройки (false означает не новая игра для SettingsActivity)
        GameState gameState = repository.loadGameState(false);
        gameSettings.setValue(gameState.getGameSettings());
    }

    public void updatePetName(String name) {
        GameSettings settings = gameSettings.getValue();
        if (settings != null) {
            settings.setPetName(name);
            gameSettings.setValue(settings);
        }
    }

    public void updateSelectedPet(int characterNumber) {
        GameSettings settings = gameSettings.getValue();
        if (settings != null) {
            settings.setCharacter(characterNumber);
            gameSettings.setValue(settings);
        }
    }

    public void updateGameSpeed(int gameSpeed) {
        GameSettings settings = gameSettings.getValue();
        if (settings != null) {
            settings.setGameSpeed(gameSpeed);
            gameSettings.setValue(settings);
        }
    }

    public void saveSettings() {
        GameSettings settings = gameSettings.getValue();
        if (settings != null) {
            repository.saveSettings(settings);
        }
    }

    public LiveData<GameSettings> getGameSettings() {
        return gameSettings;
    }
}