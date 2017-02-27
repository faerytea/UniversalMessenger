package ru.ifmo.rain.maevsky.unicomm.plugins;

import android.support.annotation.NonNull;
import ru.ifmo.rain.maevsky.unicomm.messaging.Message;

/**
 * Created by faerytea on 21.09.16.
 */
public interface Notifier {
    /**
     * This will show/hide online indicator.
     *
     * @param online status for chat, initially false
     */
    void statusNotification(boolean online); // TODO: 20.10.16 Bitmap instead boolean

    /**
     * This will change info string. Should be used for
     * something like "typing ..." or "last seen 15 minutes ago"
     *
     * @param newInfoString text which should be placed to info
     *                      string, or null for hide it.
     */
    void changeInfoString(String newInfoString);

    /**
     * Should be called when user receives message.
     *
     * @param msg received message
     */
    void messageReceived(@NonNull Message msg);
}
