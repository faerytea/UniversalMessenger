package ru.ifmo.rain.maevsky.unicomm.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.*;
import ru.ifmo.rain.maevsky.unicomm.R;
import ru.ifmo.rain.maevsky.unicomm.messaging.Message;
import ru.ifmo.rain.maevsky.unicomm.plugins.MessengerException;
import ru.ifmo.rain.maevsky.unicomm.plugins.Notifier;
import ru.ifmo.rain.maevsky.unicomm.plugins.PluginsStack;
import ru.ifmo.rain.maevsky.unicomm.service.Keeper;

import java.util.ArrayList;

import static ru.ifmo.rain.maevsky.unicomm.activities.Chats.PLUGIN_STACK_NO;

public class ChatActivity extends AppCompatActivity {
    private static final String MESSAGES_KEY = "all messages";
    private final Handler uiHandler = new Handler();
    private Button send;
    private EditText messageText;
    private ListView chat;
    private TextView infoString;
    private ArrayList<String> messages;
    private ArrayAdapter<String> adapter;
    private PluginsStack pluginsStack;
    private int psNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        restore(savedInstanceState);
    }

    private void restore(Bundle savedInstanceState) {
        send = (Button) findViewById(R.id.send_button);
        messageText = (EditText) findViewById(R.id.message_field);
        chat = (ListView) findViewById(R.id.chat);
        infoString = (TextView) findViewById(R.id.info_string);
        psNo = savedInstanceState == null
                ? getIntent().getIntExtra(PLUGIN_STACK_NO, -1)
                : savedInstanceState.getInt(PLUGIN_STACK_NO, -1);
        pluginsStack = Keeper.getInstance(this).stacks.get(psNo);
        messages = savedInstanceState != null
                ? savedInstanceState.getStringArrayList(MESSAGES_KEY)
                : new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        ((ListView) findViewById(R.id.chat)).setAdapter(adapter);
        MessengerException e = pluginsStack.start(this, new Notifier() {
            @Override
            public void statusNotification(boolean online) {
                // do nothing
            }

            @Override
            public void changeInfoString(String newInfoString) {
                uiHandler.post(() -> infoString.setText(newInfoString));
            }

            @Override
            public void messageReceived(@NonNull Message msg) {
                Message decoded;
                try {
                    decoded = pluginsStack.decode(msg);
                } catch (PluginsStack.Fail fail) {
                    uiHandler.post(() -> {
                        Toast.makeText(
                                ChatActivity.this,
                                fail.getMessage() + ": " + fail.getCause().getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                uiHandler.post(() -> addMessage(decoded.getSender().getName(), decoded.getText()));
            }
        });
        if (e != null) {
            Toast.makeText(this, "Fatal: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(PLUGIN_STACK_NO, psNo);
        outState.putStringArrayList(MESSAGES_KEY, messages);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        restore(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void addMessage(String from, String text) {
        messages.add(from + ": " + text);
        adapter.notifyDataSetChanged();
        chat.scrollTo(0, chat.getMaxScrollAmount());
    }

    public void send(View view) {
        send.setEnabled(false);
        final String text = messageText.getText().toString();
        new AsyncTask<Message, Void, PluginsStack.Fail>() {
            @Override
            protected PluginsStack.Fail doInBackground(Message... params) {
                Message msg = params[0];
                try {
                    pluginsStack.send(msg);
                } catch (PluginsStack.Fail fail) {
                    return fail;
                }
                return null;
            }

            @Override
            protected void onPostExecute(PluginsStack.Fail fail) {
                if (fail == null) {
                    addMessage(pluginsStack.getYourself().getName(), text);
                } else {
                    Toast.makeText(
                            ChatActivity.this,
                            fail.getMessage() + ": " + fail.getCause().getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                send.setEnabled(true);
            }
        }.execute(new Message(pluginsStack.getSelectedChat(), pluginsStack.getYourself(), text));
    }
}
