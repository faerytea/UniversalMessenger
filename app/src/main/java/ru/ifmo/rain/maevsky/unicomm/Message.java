package ru.ifmo.rain.maevsky.unicomm;

import android.graphics.Bitmap;

/**
 * Created by faerytea on 21.09.16.
 */
public class Message {
    private final String text;
    private final Bitmap[] pictures;

    public Message(String text) {
        this(text, null);
    }

    public Message(String text, Bitmap[] pictures) {
        this.text = text;
        this.pictures = pictures;
    }

    public String getText() {
        return text;
    }

    public boolean hasText() {
        return text != null;
    }

    public Bitmap[] getPictures() {
        return pictures;
    }

    public boolean hasPictutes() {
        return pictures != null;
    }
}
