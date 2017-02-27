package ru.ifmo.rain.maevsky.unicomm.plugins;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Base class for all plugins.
 * <p>Plugin will be loaded when app starts if and only if plugin
 * is already added in app's settings. Every stack of plugins
 * will run not in UI thread so you can use some slow actions
 * without {@link android.os.AsyncTask}, but some very long tasks,
 * like working with network, should be performed in another
 * thread.</p>
 * <p>Please ensure that your class have a constructor without
 * parameters which constructs instance for constructing
 * instances using {@link #getInstance()} method.</p>
 */
public interface Plugin {
    String SETTINGS_KEY = "unicomm settings";
    String GLOBAL = "unicomm global";
    String LOCAL = "unicomm local";

    void saveGlobalSettings(SharedPreferences preferences);

    void restoreGlobalSettings(SharedPreferences preferences);

    void saveLocalSettings(SharedPreferences preferences);

    void restoreLocalSettings(SharedPreferences preferences);

    @Nullable
    ComponentName getSettingsComponentName();

    void onSettingsActivityResult(Bundle b);

    @NonNull
    Plugin getInstance();

    @NonNull
    String getName();

    @NonNull
    String getDescription();
}
