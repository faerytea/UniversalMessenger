package ru.ifmo.rain.maevsky.unicomm.plugins;

import android.content.Context;
import android.support.annotation.NonNull;
import ru.ifmo.rain.maevsky.unicomm.messaging.Chat;
import ru.ifmo.rain.maevsky.unicomm.messaging.Message;

import java.util.List;

/**
 * Created by faerytea on 21.09.16.
 */
public interface Messenger extends Plugin {
    /**
     * Send message using API
     *
     * @param msg message for sending
     * @throws MessengerException when something went wrong
     */
    void send(Message msg) throws MessengerException;

    /**
     * Starts {@code Messenger} plugin. Called once or more times after
     * loading plugin. First time it should initialise network connection
     * and other stuff. Each next time it should do nothing.
     *
     * @param context context may be useful
     * @throws MessengerException with human-readable error description
     */
    void start(Context context) throws MessengerException;

    /**
     * Shutting down plugin. This is last method which may be called.
     */
    void stop();

    /**
     * This method will be called before {@link #start(Context)}. For different
     * instance of messenger there may be different {@link Notifier}s.
     * You should keep it until next {@code register(notifier)} call.
     *
     * @see Notifier
     * @param stackName unique name for plugin's stack
     * @param notifier {@link Notifier} instance which provides some callbacks.
     */
    void register(String stackName, Notifier notifier);

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    Messenger getInstance();

    Chat getYourself();

    List<? extends Chat> getAvailableChats();
    // TODO: 21.09.16 something useful
}
