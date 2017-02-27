package ru.ifmo.rain.maevsky.unicomm.plugins;

import android.support.annotation.NonNull;
import ru.ifmo.rain.maevsky.unicomm.messaging.Message;

/**
 * Created by faerytea on 21.09.16.
 */
public interface Preprocessor extends Plugin {
    Message encode(Message msg) throws PreprocessorException;

    Message decode(Message msg) throws PreprocessorException;

    void restorePreviousState();

    @NonNull
    @Override
    Preprocessor getInstance();
}
