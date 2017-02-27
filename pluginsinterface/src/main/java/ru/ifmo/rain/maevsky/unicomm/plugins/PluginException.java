package ru.ifmo.rain.maevsky.unicomm.plugins;

/**
 * Created by faerytea on 21.09.16.
 */

/**
 * For exceptions in plugins.
 * Use {@link MessengerException} or {@link PreprocessorException} instead of
 * {@code PluginException} in your plugins.
 */
public class PluginException extends Exception {
    public PluginException(String detailMessage) {
        super(detailMessage);
    }

    public PluginException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PluginException(Throwable throwable) {
        super(throwable);
    }
}
