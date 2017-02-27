package ru.ifmo.rain.maevsky.unicomm.plugins;

import android.content.Context;
import android.content.SharedPreferences;
import ru.ifmo.rain.maevsky.unicomm.messaging.Chat;
import ru.ifmo.rain.maevsky.unicomm.messaging.Message;
import ru.ifmo.rain.maevsky.unicomm.utils.BiConsumer;

import java.util.List;
import java.util.Locale;

/**
 * Created by faerytea on 21.09.16.
 */
public class PluginsStack {
    public final Messenger messenger;
    public final Preprocessor[] preprocessors;
    private final String name;
    private List<? extends Chat> chats = null;
    private Chat selectedChat;

    public PluginsStack(String name, Messenger messenger, Preprocessor[] preprocessors) {
        this.name = name;
        this.messenger = messenger;
        this.preprocessors = preprocessors;
    }

    public void save(Context context) {
        settingsManagement(context, Plugin::saveLocalSettings);
    }

    public void load(Context context) {
        settingsManagement(context, Plugin::restoreLocalSettings);
    }

    public MessengerException start(Context context, Notifier notifier) {
        messenger.register(name, notifier);
        try {
            messenger.start(context);
        } catch (MessengerException e) {
            return e;
        }
        return null;
    }

    private void settingsManagement(Context context, BiConsumer<Plugin, SharedPreferences> method) {
        SharedPreferences preferences;
        for (int i = 0; i < preprocessors.length; ++i) {
            String className = preprocessors[i].getClass().getName();
            preferences =
                    context.getSharedPreferences(
                            String.format(Locale.FRANCE, "%s_%d_%s", name, i, className),
                            Context.MODE_PRIVATE);
            method.apply(preprocessors[i], preferences);
        }
        preferences = context.getSharedPreferences(
                String.format(Locale.FRANCE, "%s_M_%s", name, messenger.getClass().getName()),
                Context.MODE_PRIVATE);
        method.apply(messenger, preferences);
    }

    public void send(Message msg) throws Fail {
        int i = 0;
        try {
            for (; i < preprocessors.length; ++i) {
                msg = preprocessors[i].encode(msg);
            }
            messenger.send(msg);
        } catch (PluginException e) {
            String bad = preprocessors[i].getName();
            for (; i >= 0; --i) {
                preprocessors[i].restorePreviousState();
            }
            throw new Fail(bad, e);
        }
    }

    public Message decode(Message msg) throws Fail {
        int i = preprocessors.length - 1;
        try {
            for (; i >= 0; --i) {
                msg = preprocessors[i].decode(msg);
            }
        } catch (PluginException e) {
            String bad = preprocessors[i].getName();
            for (; i < preprocessors.length; ++i) {
                preprocessors[i].restorePreviousState();
            }
            throw new Fail(bad, e);
        }
        return msg;
    }

    public Chat getSelectedChat() {
        return selectedChat;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public Chat getYourself() {
        return messenger.getYourself();
    }

    public List<? extends Chat> getListOfChats() {
        if (chats == null)
            refreshChatList();
        return chats;
    }

    public void refreshChatList() {
        chats = messenger.getAvailableChats();
    }

    public void select(int chatNo) {
        selectedChat = chats.get(chatNo);
    }

    public static final class Fail extends PluginException {
        private Fail(String nameOfPlugin, PluginException pluginException) {
            super(nameOfPlugin, pluginException);
        }
    }
}
