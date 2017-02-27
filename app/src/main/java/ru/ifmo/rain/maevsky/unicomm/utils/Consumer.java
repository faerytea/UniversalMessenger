package ru.ifmo.rain.maevsky.unicomm.utils;

/**
 * Created by faerytea on 18.12.16.
 */
public interface Consumer<T> {
    void apply(T object);
}
