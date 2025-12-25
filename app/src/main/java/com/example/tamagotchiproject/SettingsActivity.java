package com.example.tamagotchiproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.tamagotchiproject.R;
import com.example.tamagotchiproject.model.GameSettings;
import com.example.tamagotchiproject.viewmodel.SettingsViewModel;
import com.example.tamagotchiproject.viewmodel.ViewModelFactory;

public class SettingsActivity extends AppCompatActivity {

    private SettingsViewModel viewModel;

    private EditText petNameEditText;
    private RadioGroup speedRadioGroup;
    private TextView settingsTitle;
    private int selectedCharacter = 1;

    private View character1Background, character2Background, character3Background;
    private TextView character1Text, character2Text, character3Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Инициализация ViewModel
        ViewModelFactory factory = new ViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);

        // Устанавливаем разноцветный заголовок
        settingsTitle = findViewById(R.id.settingsTitle);
        setColoredText(settingsTitle, "НАСТРОЙКИ ИГРЫ");

        initViews();
        setupObservers();
        setupListeners();
    }

    private void initViews() {
        petNameEditText = findViewById(R.id.petNameEditText);
        speedRadioGroup = findViewById(R.id.speedRadioGroup);

        character1Background = findViewById(R.id.character1Background);
        character2Background = findViewById(R.id.character2Background);
        character3Background = findViewById(R.id.character3Background);

        character1Text = findViewById(R.id.character1Text);
        character2Text = findViewById(R.id.character2Text);
        character3Text = findViewById(R.id.character3Text);

        configurePetNameEditText();
    }

    private void setupObservers() {
        // Наблюдаем за настройками игры
        viewModel.getGameSettings().observe(this, new Observer<GameSettings>() {
            @Override
            public void onChanged(GameSettings settings) {
                if (settings != null) {
                    // Устанавливаем имя питомца
                    petNameEditText.setText(settings.getPetName());

                    // Устанавливаем скорость игры
                    if (settings.getGameSpeed() == 0) {
                        speedRadioGroup.check(R.id.mediumRadio);
                    } else {
                        speedRadioGroup.check(R.id.fastRadio);
                    }

                    // Устанавливаем выбранного персонажа
                    selectedCharacter = settings.getCharacter();
                    selectCharacterUI(selectedCharacter);
                }
            }
        });
    }

    private void configurePetNameEditText() {
        // Фильтр: запрет символов переноса строки
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char currentChar = source.charAt(i);
                    // Запрещаем символы новой строки, возврата каретки и табуляции
                    if (currentChar == '\n' || currentChar == '\r' || currentChar == '\t') {
                        return "";
                    }
                }
                return null;
            }
        };

        // Устанавливаем фильтр
        petNameEditText.setFilters(new InputFilter[]{filter});

        // Настраиваем действие по нажатию Done/Enter
        petNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                                event.getAction() == KeyEvent.ACTION_DOWN)) {
                    // Скрываем клавиатуру
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(petNameEditText.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        // Ограничиваем максимальную длину имени (например, 15 символов)
        InputFilter[] filters = petNameEditText.getFilters();
        InputFilter[] newFilters = new InputFilter[filters.length + 1];
        System.arraycopy(filters, 0, newFilters, 0, filters.length);
        newFilters[filters.length] = new InputFilter.LengthFilter(15);
        petNameEditText.setFilters(newFilters);
    }

    private void setColoredText(TextView textView, String text) {
        SpannableString spannableString = new SpannableString(text);

        int[] colorResources = {
                R.color.yellow,
                R.color.green,
                R.color.blue,
                R.color.pink
        };

        for (int i = 0; i < text.length(); i++) {
            int colorIndex = i % colorResources.length;
            int color = ContextCompat.getColor(this, colorResources[colorIndex]);
            ForegroundColorSpan span = new ForegroundColorSpan(color);
            spannableString.setSpan(span, i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(spannableString);
    }

    private void setupListeners() {
        // Выбор персонажа 1 (Панда)
        findViewById(R.id.character1Layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedCharacter = 1;
                selectCharacterUI(1);
                viewModel.updateSelectedPet(1);
            }
        });

        // Выбор персонажа 2 (Кот)
        findViewById(R.id.character2Layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedCharacter = 2;
                selectCharacterUI(2);
                viewModel.updateSelectedPet(2);
            }
        });

        // Выбор персонажа 3 (Лягушка)
        findViewById(R.id.character3Layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedCharacter = 3;
                selectCharacterUI(3);
                viewModel.updateSelectedPet(3);
            }
        });

        // Выбор скорости игры
        speedRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.mediumRadio) {
                    viewModel.updateGameSpeed(0);
                } else if (checkedId == R.id.fastRadio) {
                    viewModel.updateGameSpeed(1);
                }
            }
        });

        // Сохранение настроек
        findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Сохраняем имя питомца
                String petName = petNameEditText.getText().toString().trim();
                viewModel.updatePetName(petName);

                // Сохраняем все настройки
                viewModel.saveSettings();

                Toast.makeText(SettingsActivity.this, "Настройки сохранены!", Toast.LENGTH_SHORT).show();

                // Отправляем результат обратно в MainActivity
                setResult(RESULT_OK);
                finish();
            }
        });

        // Отмена изменений
        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void selectCharacterUI(int characterNumber) {
        hideAllBackgrounds();
        resetAllFonts();
        showSelectedBackground(characterNumber);
        setSelectedFont(characterNumber);
    }

    private void hideAllBackgrounds() {
        character1Background.setVisibility(View.INVISIBLE);
        character2Background.setVisibility(View.INVISIBLE);
        character3Background.setVisibility(View.INVISIBLE);
    }

    private void resetAllFonts() {
        character1Text.setTypeface(getResources().getFont(R.font.shantellsans_regular));
        character2Text.setTypeface(getResources().getFont(R.font.shantellsans_regular));
        character3Text.setTypeface(getResources().getFont(R.font.shantellsans_regular));
    }

    private void showSelectedBackground(int characterNumber) {
        switch (characterNumber) {
            case 1:
                character1Background.setVisibility(View.VISIBLE);
                break;
            case 2:
                character2Background.setVisibility(View.VISIBLE);
                break;
            case 3:
                character3Background.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setSelectedFont(int characterNumber) {
        switch (characterNumber) {
            case 1:
                character1Text.setTypeface(getResources().getFont(R.font.shantellsans_bold));
                break;
            case 2:
                character2Text.setTypeface(getResources().getFont(R.font.shantellsans_bold));
                break;
            case 3:
                character3Text.setTypeface(getResources().getFont(R.font.shantellsans_bold));
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}