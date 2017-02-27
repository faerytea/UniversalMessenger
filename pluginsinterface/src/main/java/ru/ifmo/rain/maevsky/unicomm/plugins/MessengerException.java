package ru.ifmo.rain.maevsky.unicomm.plugins;

/**
 * Created by faerytea on 21.09.16.
 */

/**
 * For exceptions in {@link Messenger}s.
 * Please ensure that {@link MessengerException#getMessage()} will return
 * something useful for users.
 */
public class MessengerException extends PluginException {
    public MessengerException(String detailMessage) {
        super(detailMessage);
    }

    public MessengerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public MessengerException(Throwable throwable) {
        super(throwable);
    }
}
