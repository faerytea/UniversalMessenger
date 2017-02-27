package ru.ifmo.rain.maevsky.unicomm.plugins;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

abstract public class StatelessPreprocessor implements Preprocessor {
    private static StatelessPreprocessor I;

    public StatelessPreprocessor() {
        I = this;
    }

    @Override
    public void restorePreviousState() {
    }

    @Override
    public void saveGlobalSettings(SharedPreferences preferences) {
    }

    @Override
    public void restoreGlobalSettings(SharedPreferences preferences) {
    }

    @Override
    public void saveLocalSettings(SharedPreferences preferences) {
    }

    @Override
    public void restoreLocalSettings(SharedPreferences preferences) {
    }

    @Nullable
    @Override
    public ComponentName getSettingsComponentName() {
        return null;
    }

    @Override
    public void onSettingsActivityResult(Bundle b) {
    }

    @NonNull
    @Override
    public Preprocessor getInstance() {
        return I;
    }
}
