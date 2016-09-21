package ru.ifmo.rain.maevsky.unicomm.plugins;

import ru.ifmo.rain.maevsky.unicomm.Message;

/**
 * Created by faerytea on 21.09.16.
 */
public interface Preprocessor extends Plugin {
    Message encode(Message msg);

    Message decode(Message msg);
}
