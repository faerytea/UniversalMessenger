package ru.ifmo.rain.maevsky.unicomm.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import dalvik.system.PathClassLoader;
import ru.ifmo.rain.maevsky.unicomm.R;
import ru.ifmo.rain.maevsky.unicomm.plugins.Plugin;
import ru.ifmo.rain.maevsky.unicomm.service.CommManager;
import ru.ifmo.rain.maevsky.unicomm.service.Keeper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static ru.ifmo.rain.maevsky.unicomm.utils.Constants.LOG_TAG;
import static ru.ifmo.rain.maevsky.unicomm.utils.Constants.PACK_DESCRIPTOR;

public class Select extends AppCompatActivity {
    public static final String RESULT = "result";
    public static final String IS_MESSENGER = "ismsg";
    private ListView list;
    private List<String> fullPluginsNames;
    private boolean msg;
    private CommManager.Channel channel;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh && list.isEnabled()) {
            list.setEnabled(false);
            findViewById(R.id.next).setEnabled(false);
            findViewById(R.id.select_progress_bar).setVisibility(View.VISIBLE);
            new AsyncTask<Void, Void, Void>() {
                PackageManager pm;

                @Override
                protected void onPreExecute() {
                    pm = getPackageManager();
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    list.setEnabled(true);
                    findViewById(R.id.next).setEnabled(true);
                    findViewById(R.id.select_progress_bar).setVisibility(View.GONE);
                    fill();
                }

                @SuppressLint("NewApi")
                @Override
                protected Void doInBackground(Void... params) {
                    List<ApplicationInfo> packages = new ArrayList<>(10);
                    List<ApplicationInfo> apps = pm.getInstalledApplications(0);
                    for (ApplicationInfo app : apps) {
                        if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                            ClassLoader cl = new PathClassLoader(
                                    app.sourceDir,
                                    app.nativeLibraryDir,
                                    getClassLoader());
                            try {
                                Class<?> descriptor = cl.loadClass(app.packageName + PACK_DESCRIPTOR);
                                Method preprocessorsGetter = descriptor.getMethod("getPreprocessors");
                                Method messengersGetter = descriptor.getMethod("getMessengers");
                                if (preprocessorsGetter != null && messengersGetter != null) // block optimisation
                                    packages.add(app);
                            } catch (ClassNotFoundException e) {
                                Log.v(LOG_TAG, e.getMessage());
                                // skip
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    File f = new File(Select.this.getFilesDir(), "packages");
                    try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) {
                        for (ApplicationInfo pack : packages) {
                            w.write(pack.packageName);
                            w.write(" ");
                            w.write(pack.sourceDir);
                            w.write(" ");
                            w.write(pack.nativeLibraryDir);
                            w.newLine();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    channel.reloadKeeper();
                    return null;
                }
            }.execute();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_plugins);
        msg = savedInstanceState != null
                && savedInstanceState.getBoolean(IS_MESSENGER, false);
        findViewById(R.id.select_progress_bar).setVisibility(View.GONE);
        bindService(new Intent(this, CommManager.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                channel = (CommManager.Channel) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_IMPORTANT);
        this.list = (ListView) findViewById(R.id.plugins_list);
        msg |= getIntent().getBooleanExtra(IS_MESSENGER, false);
        fill();
    }

    private void fill() {
        this.list.clearChoices();
        HashMap<String, ? extends Plugin> pluginsHashMap =
                msg
                        ? Keeper.getInstance(this).messengers
                        : Keeper.getInstance(this).preprocessors;
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> packs = new ArrayList<>();
        for (HashMap.Entry<String, ? extends Plugin> e : pluginsHashMap.entrySet()) {
            names.add(e.getValue().getName());
            packs.add(e.getKey());
        }
        this.fullPluginsNames = packs;
        this.list.setAdapter(new ArrayAdapter<String>(
                this,
                msg
                        ? android.R.layout.simple_list_item_single_choice
                        : android.R.layout.simple_list_item_multiple_choice,
                names) {
            @Override
            public boolean hasStableIds() {
                return true;  // because my array is effectively immutable for each adapter
            }
        });
        list.setChoiceMode(
                msg
                        ? ListView.CHOICE_MODE_SINGLE
                        : ListView.CHOICE_MODE_MULTIPLE);
//        list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        this.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                parent.setSelection(position);
//                view.setBackgroundColor(Color.rgb(115, 146, 250));
//            }
//        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_MESSENGER, msg);
        super.onSaveInstanceState(outState);
    }

    public void next(View view) {
        long[] ids = this.list.getCheckedItemIds();
        if (ids.length > 0) {
            Intent intent = new Intent();
            if (msg) {
                intent.putExtra(RESULT, fullPluginsNames.get((int) ids[0]));
            } else {
                ArrayList<String> arrayList = new ArrayList<>(ids.length);
                for (long id : ids) {
                    arrayList.add(fullPluginsNames.get((int) id));
                }
                intent.putStringArrayListExtra(RESULT, arrayList);
            }
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
