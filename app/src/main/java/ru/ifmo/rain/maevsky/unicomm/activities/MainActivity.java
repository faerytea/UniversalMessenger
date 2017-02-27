package ru.ifmo.rain.maevsky.unicomm.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import ru.ifmo.rain.maevsky.unicomm.R;
import ru.ifmo.rain.maevsky.unicomm.plugins.Plugin;
import ru.ifmo.rain.maevsky.unicomm.plugins.PluginsStack;
import ru.ifmo.rain.maevsky.unicomm.plugins.Preprocessor;
import ru.ifmo.rain.maevsky.unicomm.service.CommManager;
import ru.ifmo.rain.maevsky.unicomm.service.Keeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static ru.ifmo.rain.maevsky.unicomm.utils.Constants.LOG_TAG;

public class MainActivity extends AppCompatActivity {
    private static final int MESSENGER_REQUEST_CODE = 91;
    private static final int STACK_REQUEST_CODE = 53;
    private static final int SETTINGS_REQUEST_CODE = 107;
    private CommManager.Channel channel;
    private ListView list;
    private ArrayAdapter adapter;
    private FloatingActionButton fab;
    private List<PluginsStack> stacks;

    // Captures for settings
    private Plugin currentlyLaunched = null;
    private Iterator<? extends Plugin> shouldBeLaunchedSettings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.list = (ListView) findViewById(R.id.stacks);
        this.fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(MainActivity.this, Select.class)
                                .putExtra(Select.IS_MESSENGER, true),
                        MESSENGER_REQUEST_CODE);
            }
        });
        restart();
    }

    private void fill() {
        stacks = Keeper.getInstance(this).stacks;
        list.setAdapter(adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stacks));
        list.setOnItemClickListener((parent, view, position, id) -> {
            startActivity(new Intent(this, Chats.class).putExtra(Chats.PLUGIN_STACK_NO, position));
        });
    }

    private void restart() {
        startService(new Intent(this, CommManager.class));
        bindService(new Intent(this, CommManager.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                channel = (CommManager.Channel) service;
                fill();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                channel = null;
                Log.e(LOG_TAG, "Service disconnected");
                restart();
            }
        }, Context.BIND_IMPORTANT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MESSENGER_REQUEST_CODE && resultCode == RESULT_OK) {
            String msgClassName = data.getStringExtra(Select.RESULT);
            if (msgClassName == null) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivityForResult(
                    new Intent(this, PluginsStackCreation.class)
                            .putExtra(PluginsStackCreation.MESSENGER_KEY, msgClassName),
                    STACK_REQUEST_CODE);
        }
        boolean previusSettingsSkipped = false;
        if (requestCode == STACK_REQUEST_CODE && resultCode == RESULT_OK) {
            String name = data.getStringExtra(PluginsStackCreation.STACK_NAME);
            String msgClassName = data.getStringExtra(PluginsStackCreation.MESSENGER_KEY);
            ArrayList<String> ppCNs = data.getStringArrayListExtra(PluginsStackCreation.PREPROCESSORS_KEY);
            if (name == null || msgClassName == null || ppCNs == null) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                return;
            }
            PluginsStack stack;
            stack = Keeper.getInstance(this).createStack(name, msgClassName, ppCNs);
            ArrayList<Preprocessor> plugins = new ArrayList<>(stack.preprocessors.length);
            Collections.addAll(plugins, stack.preprocessors);
            shouldBeLaunchedSettings = plugins.iterator();
            previusSettingsSkipped = !launchSettings(stack.messenger, Plugin.LOCAL);
        }
        if (requestCode == SETTINGS_REQUEST_CODE || previusSettingsSkipped) {
            if (currentlyLaunched != null)
                currentlyLaunched.onSettingsActivityResult(data.getExtras());
            previusSettingsSkipped = true;
            if (shouldBeLaunchedSettings.hasNext()) {
                while (shouldBeLaunchedSettings.hasNext() && previusSettingsSkipped) {
                    previusSettingsSkipped =
                            !launchSettings(
                                    currentlyLaunched = shouldBeLaunchedSettings.next(),
                                    Plugin.LOCAL);
                }
            }
            if (!shouldBeLaunchedSettings.hasNext() && previusSettingsSkipped) {
                adapter.notifyDataSetChanged();
                shouldBeLaunchedSettings = null;
            }
        }
    }

    private boolean launchSettings(Plugin p, String typeOfSettings) {
        ComponentName settings = p.getSettingsComponentName();
        if (settings == null) return false;
        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setComponent(settings);
        intent.putExtra(Plugin.SETTINGS_KEY, typeOfSettings);
        startActivityForResult(intent, SETTINGS_REQUEST_CODE);
        return true;
    }
}
