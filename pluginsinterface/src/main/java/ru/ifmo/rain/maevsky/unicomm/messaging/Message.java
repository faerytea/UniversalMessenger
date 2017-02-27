package ru.ifmo.rain.maevsky.unicomm.messaging;

import android.graphics.Bitmap;

/**
 * Created by faerytea on 21.09.16.
 */
public class Message {
    private final Chat addressee;
    private final Chat sender;
    private final String text;
    private final Bitmap[] pictures;

    public Message(Chat addressee, Chat sender, String text, Bitmap... pictures) {
        this.addressee = addressee;
        this.sender = sender;
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

    public Chat getAddressee() {
        return addressee;
    }

    public Chat getSender() {
        return sender;
    }
}
