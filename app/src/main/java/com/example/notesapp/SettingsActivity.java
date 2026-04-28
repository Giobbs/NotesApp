package com.example.notesapp;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "app_settings";
    public static final String KEY_THEME = "theme";
    public static final String KEY_AGGREGATION = "aggregation";
    public static final String KEY_DATE_RANGE = "date_range";
    public static final String KEY_PIN = "user_pin";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // =========================
        // THEME
        // =========================
        RadioGroup themeGroup = findViewById(R.id.radioTheme);
        RadioButton system = findViewById(R.id.themeSystem);
        RadioButton light = findViewById(R.id.themeLight);
        RadioButton dark = findViewById(R.id.themeDark);

        String currentTheme = prefs.getString(KEY_THEME, "system");

        switch (currentTheme) {
            case "light":
                light.setChecked(true);
                break;
            case "dark":
                dark.setChecked(true);
                break;
            default:
                system.setChecked(true);
                break;
        }

        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {

            String value = "system";

            if (checkedId == R.id.themeLight) value = "light";
            else if (checkedId == R.id.themeDark) value = "dark";

            prefs.edit()
                    .putString(KEY_THEME, value)
                    .apply();

            applyTheme(prefs);
            recreate();
        });

        // =========================
        // AGGREGATION
        // =========================
        RadioGroup aggGroup = findViewById(R.id.radioAggregation);

        RadioButton aggNone = findViewById(R.id.aggNone);
        RadioButton aggTag = findViewById(R.id.aggTag);
        RadioButton aggDate = findViewById(R.id.aggDate);

        String agg = prefs.getString(KEY_AGGREGATION, "none");

        switch (agg) {
            case "tag":
                aggTag.setChecked(true);
                break;
            case "date":
                aggDate.setChecked(true);
                break;
            default:
                aggNone.setChecked(true);
                break;
        }

        aggGroup.setOnCheckedChangeListener((group, checkedId) -> {

            String value = "none";

            if (checkedId == R.id.aggTag) value = "tag";
            else if (checkedId == R.id.aggDate) value = "date";

            prefs.edit()
                    .putString(KEY_AGGREGATION, value)
                    .apply();
        });

        // =========================
        // DATE RANGE
        // =========================
        RadioGroup dateGroup = findViewById(R.id.radioDateRange);

        RadioButton d7 = findViewById(R.id.date7);
        RadioButton d30 = findViewById(R.id.date30);
        RadioButton d365 = findViewById(R.id.date365);

        String range = prefs.getString(KEY_DATE_RANGE, "7");

        switch (range) {
            case "30":
                d30.setChecked(true);
                break;
            case "365":
                d365.setChecked(true);
                break;
            default:
                d7.setChecked(true);
                break;
        }

        dateGroup.setOnCheckedChangeListener((group, checkedId) -> {

            String value = "7";

            if (checkedId == R.id.date30) value = "30";
            else if (checkedId == R.id.date365) value = "365";

            prefs.edit()
                    .putString(KEY_DATE_RANGE, value)
                    .apply();
        });

        android.widget.EditText editPin = findViewById(R.id.editPin);
        android.widget.Button btnSavePin = findViewById(R.id.btnSavePin);

        String savedPin = prefs.getString(KEY_PIN, "");
        editPin.setText(savedPin);

        btnSavePin.setOnClickListener(v -> {

            String pin = editPin.getText().toString().trim();

            if (pin.length() < 4) {
                android.widget.Toast.makeText(this,
                        "PIN troppo corto",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                    .putString(KEY_PIN, pin)
                    .apply();

            android.widget.Toast.makeText(this,
                    "PIN salvato",
                    android.widget.Toast.LENGTH_SHORT).show();
        });
    }
    public static void applyTheme(SharedPreferences prefs) {

        String theme = prefs.getString(KEY_THEME, "system");

        if ("light".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if ("dark".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }}