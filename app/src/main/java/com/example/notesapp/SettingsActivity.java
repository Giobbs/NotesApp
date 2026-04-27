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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        RadioGroup radioGroup = findViewById(R.id.radioTheme);
        RadioButton system = findViewById(R.id.themeSystem);
        RadioButton light = findViewById(R.id.themeLight);
        RadioButton dark = findViewById(R.id.themeDark);

        // stato iniziale
        String current = prefs.getString(KEY_THEME, "system");
        switch (current) {
            case "light": light.setChecked(true); break;
            case "dark": dark.setChecked(true); break;
            default: system.setChecked(true); break;
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String value = "system";

            if (checkedId == R.id.themeLight) value = "light";
            else if (checkedId == R.id.themeDark) value = "dark";

            prefs.edit().putString(KEY_THEME, value).apply();

            applyTheme(prefs);

             recreate();
        });
    }

    public static void applyTheme(SharedPreferences prefs) {
        String theme = prefs.getString(KEY_THEME, "system");

        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}