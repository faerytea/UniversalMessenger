package ru.ifmo.rain.maevsky.unicomm.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import ru.ifmo.rain.maevsky.unicomm.R;

public class SettingsActivity extends AppCompatActivity {
    private static final String PLUGIN_STACK_NO = "plugin stack n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}
