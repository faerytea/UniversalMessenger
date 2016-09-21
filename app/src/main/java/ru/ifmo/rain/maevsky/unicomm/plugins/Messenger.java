package ru.ifmo.rain.maevsky.unicomm.plugins;

import ru.ifmo.rain.maevsky.unicomm.Message;

/**
 * Created by faerytea on 21.09.16.
 */
public interface Messenger extends Plugin {
    /**
     * Send message using API
     *
     * @param msg message for sending
     * @return true if message was sent successfully, false otherwise
     * @throws MessengerException when something went wrong
     */
    boolean send(Message msg) throws MessengerException;

    /**
     * Starts {@code Messenger} plugin
     *
     * @return string with errors or {@code null}, if everything is ok.
     */
    String start();

    /**
     * Shutting down plugin.
     * This method may be interrupted, but
     * only if shutting down is very slow (i.e. about minute)
     *
     * @throws InterruptedException for avoiding warnings
     */
    void stop() throws InterruptedException;

    // TODO: 21.09.16 something useful
}
