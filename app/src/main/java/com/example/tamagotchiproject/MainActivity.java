package com.example.tamagotchiproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.tamagotchiproject.R;
import com.example.tamagotchiproject.model.GameSettings;
import com.example.tamagotchiproject.viewmodel.MainViewModel;
import com.example.tamagotchiproject.viewmodel.ViewModelFactory;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;

    private TextView titleTextView;
    private ImageView mainPetImage;
    private TextView bestTimeMediumText;
    private TextView bestTimeFastText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация ViewModel
        ViewModelFactory factory = new ViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        initViews();
        setupObservers();
        setupClickListeners();

        // Загружаем данные при создании
        viewModel.loadData();
    }

    private void initViews() {
        titleTextView = findViewById(R.id.title);
        mainPetImage = findViewById(R.id.image);
        bestTimeMediumText = findViewById(R.id.best_time_medium);
        bestTimeFastText = findViewById(R.id.best_time_fast);
    }

    private void setupObservers() {
        // Наблюдаем за настройками игры
        viewModel.getGameSettings().observe(this, new Observer<GameSettings>() {
            @Override
            public void onChanged(GameSettings settings) {
                if (settings != null) {
                    // Устанавливаем имя питомца
                    String petName = settings.getPetName();
                    if (!petName.isEmpty()) {
                        setColoredText(titleTextView, petName.toUpperCase());
                    } else {
                        setColoredText(titleTextView, "ТАМАГОЧИ");
                    }

                    // Устанавливаем изображение питомца
                    setPetImage(settings.getCharacter());
                }
            }
        });

        // Наблюдаем за лучшим временем (средний режим)
        viewModel.getBestTimeMedium().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String bestTime) {
                if (bestTime != null) {
                    bestTimeMediumText.setText(bestTime);
                    // Устанавливаем шрифт и цвет
                    bestTimeMediumText.setTypeface(getResources().getFont(R.font.shantellsans_bold));
                    bestTimeMediumText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.text_color));
                }
            }
        });

        // Наблюдаем за лучшим временем (быстрый режим)
        viewModel.getBestTimeFast().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String bestTime) {
                if (bestTime != null) {
                    bestTimeFastText.setText(bestTime);
                    // Устанавливаем шрифт и цвет
                    bestTimeFastText.setTypeface(getResources().getFont(R.font.shantellsans_bold));
                    bestTimeFastText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.text_color));
                }
            }
        });
    }

    private void setupClickListeners() {
        // Обработка кнопки "начать игру"
        TextView startButton = findViewById(R.id.button1);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(gameIntent);
            }
        });

        // Обработка кнопки "настройки"
        TextView settingsButton = findViewById(R.id.button2);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении из настроек обновляем данные
        viewModel.loadData();
    }

    private void setColoredText(TextView textView, String word) {
        SpannableString spannableString = new SpannableString(word);

        int[] colorResources = {
                R.color.yellow,
                R.color.green,
                R.color.blue,
                R.color.pink
        };

        for (int i = 0; i < word.length(); i++) {
            int colorIndex = i % colorResources.length;
            int color = ContextCompat.getColor(this, colorResources[colorIndex]);
            ForegroundColorSpan span = new ForegroundColorSpan(color);
            spannableString.setSpan(span, i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(spannableString);
    }

    // Метод для установки картинки питомца
    private void setPetImage(int characterNumber) {
        switch (characterNumber) {
            case 1: // Панда
                mainPetImage.setImageResource(R.drawable.panda);
                break;
            case 2: // Кот
                mainPetImage.setImageResource(R.drawable.cat);
                break;
            case 3: // Лягушка
                mainPetImage.setImageResource(R.drawable.frog);
                break;
            default: // По умолчанию панда
                mainPetImage.setImageResource(R.drawable.panda);
                break;
        }
    }
}