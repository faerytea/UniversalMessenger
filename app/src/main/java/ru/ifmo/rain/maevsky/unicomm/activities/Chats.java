package ru.ifmo.rain.maevsky.unicomm.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import ru.ifmo.rain.maevsky.unicomm.R;
import ru.ifmo.rain.maevsky.unicomm.messaging.Chat;
import ru.ifmo.rain.maevsky.unicomm.plugins.PluginsStack;
import ru.ifmo.rain.maevsky.unicomm.service.Keeper;

import java.util.ArrayList;
import java.util.List;

public class Chats extends AppCompatActivity {
    public static final String PLUGIN_STACK_NO = "Plugin stack #";
    public static final String CHATS_KEY = "chats AL";
    private int psNo;
    private ArrayList<String> chats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        psNo = savedInstanceState == null
                ? getIntent().getIntExtra(PLUGIN_STACK_NO, -1)
                : savedInstanceState.getInt(PLUGIN_STACK_NO, -1);
        if (savedInstanceState == null) {
            chats = new ArrayList<>();
            new AsyncTask<PluginsStack, Void, List<? extends Chat>>() {
                @Override
                protected void onPostExecute(List<? extends Chat> chats) {
                    Chats.this.chats.ensureCapacity(chats.size());
                    for (Chat c : chats)
                        Chats.this.chats.add(c.getName());
                    fill();
                }

                @Override
                protected List<? extends Chat> doInBackground(PluginsStack... params) {
                    params[0].refreshChatList();
                    return params[0].getListOfChats();
                }
            }.execute(Keeper.getInstance(Chats.this).stacks.get(psNo));
        } else {
            chats = savedInstanceState.getStringArrayList(CHATS_KEY);
            fill();
        }
    }

    private void fill() {
        ListView list = (ListView) findViewById(R.id.list_of_chats);
        ProgressBar bar = (ProgressBar) findViewById(R.id.chats_progress_bar);
        bar.setVisibility(View.GONE);
        list.setAdapter(new ArrayAdapter<>(
                Chats.this,
                android.R.layout.simple_list_item_1,
                Chats.this.chats));
        list.setVisibility(View.VISIBLE);
        list.setOnItemClickListener((parent, view, position, id) ->
                startActivity(
                        new Intent(this, ChatActivity.class)
                                .putExtra(PLUGIN_STACK_NO, psNo)));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(PLUGIN_STACK_NO, psNo);
        outState.putStringArrayList(CHATS_KEY, chats);
        super.onSaveInstanceState(outState);
    }
}
