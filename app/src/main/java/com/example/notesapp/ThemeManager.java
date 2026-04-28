package com.example.notesapp;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    public static void apply(SharedPreferences prefs) {

        String theme = prefs.getString(SettingsActivity.KEY_THEME, THEME_SYSTEM);

        switch (theme) {

            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;

            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;

            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}