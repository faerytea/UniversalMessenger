package ru.ifmo.rain.maevsky.unicomm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import ru.ifmo.rain.maevsky.unicomm.R;
import ru.ifmo.rain.maevsky.unicomm.plugins.Preprocessor;
import ru.ifmo.rain.maevsky.unicomm.service.Keeper;

import java.util.ArrayList;
import java.util.HashMap;

public class PluginsStackCreation extends AppCompatActivity {
    static final String PREPROCESSORS_KEY = "preprocessors";
    static final String MESSENGER_KEY = "msgrs";
    static final String STACK_NAME = "stack name";
    private static final int REQUEST_CODE = 17;
    private EditText stackName;
    private ArrayAdapter adapter;
    private ArrayList<String> chosenPreprocessors;
    private ArrayList<String> preprocessorsNames;
    private HashMap<String, Preprocessor> allPreprocessors;
    private String messenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugins_stack_creation);
        this.messenger = savedInstanceState == null
                ? getIntent().getStringExtra(MESSENGER_KEY)
                : savedInstanceState.getString(MESSENGER_KEY);
        this.chosenPreprocessors = savedInstanceState != null
                ? savedInstanceState.getStringArrayList(PREPROCESSORS_KEY)
                : new ArrayList<>();
        this.stackName = (EditText) findViewById(R.id.stackName);
        this.stackName.setText(savedInstanceState != null
                ? savedInstanceState.getString(STACK_NAME, "")
                : "");
        ListView list = (ListView) findViewById(R.id.stack);
        allPreprocessors = Keeper.getInstance(this).preprocessors;
        preprocessorsNames = new ArrayList<>(allPreprocessors.size());
        for (String i : chosenPreprocessors) {
            preprocessorsNames.add(allPreprocessors.get(i).getName());
        }
        list.setAdapter(adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                preprocessorsNames));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STACK_NAME, stackName.getText().toString());
        outState.putString(MESSENGER_KEY, messenger);
        outState.putStringArrayList(PREPROCESSORS_KEY, chosenPreprocessors);
        super.onSaveInstanceState(outState);
    }

    public void done(View view) {
        Intent intent = new Intent();
        intent.putExtra(STACK_NAME, stackName.getText().toString());
        intent.putExtra(MESSENGER_KEY, messenger);
        intent.putStringArrayListExtra(PREPROCESSORS_KEY, chosenPreprocessors);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void addPreprocessor(View view) {
        Bundle b = new Bundle(1);
        b.putBoolean(Select.IS_MESSENGER, false);
        startActivityForResult(new Intent(this, Select.class), REQUEST_CODE, b);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE || resultCode != RESULT_OK) return;
        ArrayList<String> extra = data.getStringArrayListExtra(Select.RESULT);
        chosenPreprocessors.addAll(extra);
        for (String i : extra) {
            preprocessorsNames.add(allPreprocessors.get(i).getName());
        }
        adapter.notifyDataSetChanged();
    }
}
