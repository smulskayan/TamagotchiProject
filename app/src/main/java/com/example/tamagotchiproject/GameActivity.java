package com.example.tamagotchiproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.animation.ValueAnimator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AppCompatActivity;


import com.example.tamagotchiproject.R;
import com.example.tamagotchiproject.model.GameSettings;
import com.example.tamagotchiproject.model.GameState;
import com.example.tamagotchiproject.model.PetStats;
import com.example.tamagotchiproject.repository.GameRepository;
import com.example.tamagotchiproject.viewmodel.GameViewModel;
import com.example.tamagotchiproject.viewmodel.ViewModelFactory;

public class GameActivity extends AppCompatActivity {

    private GameViewModel viewModel;

    // UI элементы
    private TextView gameTitle, timerText;
    private ProgressBar hungerBar, happinessBar, cleanlinessBar, energyBar;
    private ImageView petImage, petBackground, backButton, pauseButton;

    // ID ресурсов для изображений
    private int normalImageResId = R.drawable.panda;
    private int sadImageResId = R.drawable.panda_sad;
    private int leaveImageResId = R.drawable.panda_leave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Инициализация ViewModel
        ViewModelFactory factory = new ViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(GameViewModel.class);

        initViews();
        setupObservers();
        setupClickListeners();
    }

    private void initViews() {
        gameTitle = findViewById(R.id.gameTitle);
        timerText = findViewById(R.id.timerText);

        hungerBar = findViewById(R.id.hungerBar);
        happinessBar = findViewById(R.id.happinessBar);
        cleanlinessBar = findViewById(R.id.cleanlinessBar);
        energyBar = findViewById(R.id.energyBar);

        petImage = findViewById(R.id.petImage);
        petBackground = findViewById(R.id.petBackground);
        backButton = findViewById(R.id.backButton);
        pauseButton = findViewById(R.id.pauseButton);
    }

    private void setupObservers() {
        // Наблюдаем за текстом таймера
        viewModel.getTimerText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String time) {
                timerText.setText(time);
            }
        });

        // Наблюдаем за настройками игры
        viewModel.getGameSettings().observe(this, new Observer<GameSettings>() {
            @Override
            public void onChanged(GameSettings settings) {
                if (settings != null) {
                    // Устанавливаем имя питомца
                    String displayName = settings.getPetName().isEmpty() ?
                            "ТАМАГОЧИ" : settings.getPetName().toUpperCase();
                    setColoredText(gameTitle, displayName);

                    // Устанавливаем изображение питомца
                    setPetImage(settings.getCharacter());
                }
            }
        });

        // Наблюдаем за статистикой питомца
        viewModel.getPetStats().observe(this, new Observer<PetStats>() {
            @Override
            public void onChanged(PetStats stats) {
                if (stats != null) {
                    updateProgressBars(stats);
                    updatePetAppearance(stats);
                    updatePetBackground(stats);
                }
            }
        });

        // Наблюдаем за состоянием игры
        viewModel.getGameState().observe(this, new Observer<GameState>() {
            @Override
            public void onChanged(GameState gameState) {
                if (gameState != null) {
                    // Обновляем UI в зависимости от состояния игры
                    if (gameState.isPaused()) {
                        pauseButton.setImageResource(R.drawable.ic_play);
                    } else {
                        pauseButton.setImageResource(R.drawable.ic_pause);
                    }
                }
            }
        });

        // Наблюдаем за окончанием игры
        viewModel.getIsGameOver().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isGameOver) {
                if (isGameOver != null && isGameOver) {
                    showGameOverDialog();
                }
            }
        });
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.pauseGame();
                showExitDialog();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameState currentState = viewModel.getGameState().getValue();
                if (currentState != null) {
                    if (currentState.isPaused()) {
                        viewModel.resumeGame();
                        pauseButton.setImageResource(R.drawable.ic_pause);
                    } else {
                        viewModel.pauseGame();
                        pauseButton.setImageResource(R.drawable.ic_play);
                        showPauseDialog();
                    }
                }
            }
        });

        findViewById(R.id.feedButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.feedPet();
            }
        });

        findViewById(R.id.washButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.washPet();
            }
        });

        findViewById(R.id.playButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.playWithPet();
            }
        });

        findViewById(R.id.restButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.restPet();
            }
        });
    }

    private void updateProgressBars(PetStats stats) {
        animateStatChange(hungerBar, stats.getHunger());
        animateStatChange(happinessBar, stats.getHappiness());
        animateStatChange(cleanlinessBar, stats.getCleanliness());
        animateStatChange(energyBar, stats.getEnergy());
    }

    private void updatePetAppearance(PetStats stats) {
        boolean isSad = stats.isAnyStatLow();
        petImage.setImageResource(isSad ? sadImageResId : normalImageResId);
    }

    private void updatePetBackground(PetStats stats) {
        int hunger = stats.getHunger();
        int happiness = stats.getHappiness();
        int cleanliness = stats.getCleanliness();
        int energy = stats.getEnergy();

        if (energy < 40) {
            petBackground.setImageResource(R.drawable.circle_green);
            return;
        }

        if (hunger < 40) {
            petBackground.setImageResource(R.drawable.circle_red);
            return;
        }

        if (cleanliness < 40) {
            petBackground.setImageResource(R.drawable.circle_yellow);
            return;
        }

        if (happiness < 40) {
            petBackground.setImageResource(R.drawable.circle_blue);
            return;
        }

        petBackground.setImageResource(R.drawable.circle_pink);
    }

    private void setPetImage(int characterNumber) {
        switch (characterNumber) {
            case 1:
                normalImageResId = R.drawable.panda;
                sadImageResId = R.drawable.panda_sad;
                leaveImageResId = R.drawable.panda_leave;
                break;
            case 2:
                normalImageResId = R.drawable.cat;
                sadImageResId = R.drawable.cat_sad;
                leaveImageResId = R.drawable.cat_leave;
                break;
            case 3:
                normalImageResId = R.drawable.frog;
                sadImageResId = R.drawable.frog_sad;
                leaveImageResId = R.drawable.frog_leave;
                break;
        }

        // Обновляем текущее изображение
        PetStats stats = viewModel.getPetStats().getValue();
        if (stats != null) {
            updatePetAppearance(stats);
        }
    }

    private void animateStatChange(final ProgressBar progressBar, final int targetValue) {
        final int currentProgress = progressBar.getProgress();

        if (currentProgress == targetValue) {
            return;
        }

        final ValueAnimator animator = ValueAnimator.ofInt(currentProgress, targetValue);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                progressBar.setProgress(value);
            }
        });
        animator.start();
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

    // Диалог паузы (стилизованный как в оригинале)
    private void showPauseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle("Игра на паузе");
        builder.setMessage("Игра приостановлена.\nНажмите 'Продолжить', чтобы вернуться к игре.");

        builder.setPositiveButton("Продолжить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewModel.resumeGame();
                pauseButton.setImageResource(R.drawable.ic_pause);
                dialog.dismiss();
            }
        });

        builder.setCancelable(false);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                viewModel.resumeGame();
                pauseButton.setImageResource(R.drawable.ic_pause);
            }
        });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                AlertDialog d = (AlertDialog) dialogInterface;

                TextView titleView = d.findViewById(android.R.id.title);
                if (titleView != null) {
                    titleView.setTypeface(getResources().getFont(R.font.shantellsans_bold));
                    titleView.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.blue));
                    titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }

                TextView messageView = d.findViewById(android.R.id.message);
                if (messageView != null) {
                    messageView.setTypeface(getResources().getFont(R.font.shantellsans_regular));
                    messageView.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.black));
                    messageView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }

                Button positiveButton = d.getButton(AlertDialog.BUTTON_POSITIVE);
                if (positiveButton != null) {
                    positiveButton.setTypeface(getResources().getFont(R.font.shantellsans_bold));
                    positiveButton.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.green));
                    positiveButton.setAllCaps(false);

                    ViewGroup.LayoutParams params = positiveButton.getLayoutParams();
                    if (params instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) params).setMargins(0, 20, 0, 20);
                        positiveButton.setLayoutParams(params);
                    }
                }
            }
        });

        dialog.show();
    }

    // Диалог выхода (стилизованный как в оригинале)
    // Диалог выхода (стилизованный как в оригинале)
    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle("Выход из игры");
        builder.setMessage("Вы уверены, что хотите закончить игру?\n\nТекущий прогресс будет сохранен.");

        builder.setPositiveButton("Закончить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewModel.resumeGame(); // Возвращаем игру из паузы перед выходом

                // ВАЖНО: Сохраняем лучшее время при досрочном завершении
                saveBestTimeOnExit();

                viewModel.saveGame();
                finish();
            }
        });

        builder.setNegativeButton("Продолжить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewModel.resumeGame(); // Возвращаем игру из паузы
                dialog.dismiss();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                viewModel.resumeGame(); // Возвращаем игру из паузы
            }
        });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                AlertDialog d = (AlertDialog) dialogInterface;

                TextView titleView = d.findViewById(android.R.id.title);
                if (titleView != null) {
                    titleView.setTypeface(getResources().getFont(R.font.shantellsans_bold));
                    titleView.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.red));
                    titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }

                TextView messageView = d.findViewById(android.R.id.message);
                if (messageView != null) {
                    messageView.setTypeface(getResources().getFont(R.font.shantellsans_regular));
                    messageView.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.black));
                    messageView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }

                Button positiveButton = d.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = d.getButton(AlertDialog.BUTTON_NEGATIVE);

                if (positiveButton != null) {
                    positiveButton.setTypeface(getResources().getFont(R.font.shantellsans_bold));
                    positiveButton.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.red));
                    positiveButton.setAllCaps(false);
                }

                if (negativeButton != null) {
                    negativeButton.setTypeface(getResources().getFont(R.font.shantellsans_bold));
                    negativeButton.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.green));
                    negativeButton.setAllCaps(false);
                }

                ViewGroup buttonLayout = (ViewGroup) positiveButton.getParent();
                if (buttonLayout != null) {
                    buttonLayout.setPadding(50, 20, 50, 20);
                }
            }
        });

        dialog.show();
    }

    // Новый метод для сохранения лучшего времени при выходе
    private void saveBestTimeOnExit() {
        // Используем метод ViewModel для сохранения лучшего времени
        viewModel.saveBestTimeNow();

        // Дополнительно сохраняем через репозиторий для гарантии
        GameState gameState = viewModel.getGameState().getValue();
        GameSettings settings = viewModel.getGameSettings().getValue();

        if (gameState != null && settings != null) {
            GameRepository repository = new GameRepository(this);
            repository.saveBestTime(gameState.getElapsedTime(), settings.getGameSpeed());
        }
    }

    // Диалог завершения игры (стилизованный как в оригинале)
    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        // Получаем данные из ViewModel
        GameState gameState = viewModel.getGameState().getValue();
        GameSettings settings = viewModel.getGameSettings().getValue();

        if (gameState == null || settings == null) return;

        long elapsedTime = gameState.getElapsedTime();
        int gameSpeed = settings.getGameSpeed();
        String petName = settings.getPetName();

        // ВАЖНО: Дополнительное сохранение лучшего времени прямо здесь
        GameRepository repository = new GameRepository(this);
        repository.saveBestTime(elapsedTime, gameSpeed);

        // Получаем лучшее время для текущего режима для отображения
        long bestTimeForMode = repository.getBestTime(gameSpeed);
        String bestTimeFormatted = formatTime(bestTimeForMode);

        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;
        String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        String displayName = petName.isEmpty() ? "ТАМАГОЧИ" : petName;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, 0, 0, 0);

        ImageView leaveImageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(360, 360);
        imageParams.gravity = Gravity.CENTER;
        imageParams.setMargins(0, 0, 0, 0);
        leaveImageView.setLayoutParams(imageParams);

        leaveImageView.setImageResource(leaveImageResId);
        leaveImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        TextView messageTextView = new TextView(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(0, 0, 0, 0);
        messageTextView.setLayoutParams(textParams);

        messageTextView.setText(
                "Ваш питомец не выдержал одиночества и сбежал!\n\n" +
                        "Время игры: " + time + "\n" +
                        "Режим игры: " + (gameSpeed == 0 ? "Средний" : "Быстрый") + "\n" +
                        "Лучшее время для этого режима: " + bestTimeFormatted + "\n\n");

        messageTextView.setTypeface(getResources().getFont(R.font.shantellsans_regular));
        messageTextView.setTextColor(ContextCompat.getColor(this, R.color.text_color));
        messageTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        messageTextView.setLineSpacing(1.2f, 1.2f);

        layout.addView(leaveImageView);
        layout.addView(messageTextView);

        builder.setView(layout);
        builder.setTitle(displayName + " больше не с Вами...");

        builder.setPositiveButton("Новая игра", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewModel.resetGame();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Выйти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setCancelable(false);
        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                AlertDialog d = (AlertDialog) dialogInterface;

                TextView titleView = d.findViewById(android.R.id.title);
                if (titleView != null) {
                    titleView.setTypeface(getResources().getFont(R.font.shantellsans_bold));
                    titleView.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.blue));
                    titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }

                Button positiveButton = d.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = d.getButton(AlertDialog.BUTTON_NEGATIVE);

                if (positiveButton != null) {
                    positiveButton.setTypeface(getResources().getFont(R.font.shantellsans_bold));
                    positiveButton.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.green));
                    positiveButton.setAllCaps(false);
                }

                if (negativeButton != null) {
                    negativeButton.setTypeface(getResources().getFont(R.font.shantellsans_bold));
                    negativeButton.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.red));
                    negativeButton.setAllCaps(false);
                }

                ViewGroup buttonLayout = (ViewGroup) positiveButton.getParent();
                if (buttonLayout != null) {
                    buttonLayout.setPadding(0, 0, 0, 0);
                }
            }
        });

        dialog.show();
    }

    // Метод для форматирования времени
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

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.saveGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.saveGame();
    }
}