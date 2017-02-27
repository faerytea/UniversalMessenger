package ru.ifmo.rain.maevsky.unicomm.plugins;

/**
 * Created by faerytea on 21.09.16.
 */

/**
 * For exceptions in {@link Preprocessor}s.
 * Please ensure that {@link PreprocessorException#getMessage()} will return
 * something useful for users.
 */
public class PreprocessorException extends PluginException {
    public PreprocessorException(String detailMessage) {
        super(detailMessage);
    }

    public PreprocessorException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PreprocessorException(Throwable throwable) {
        super(throwable);
    }
}
