package ru.ifmo.rain.maevsky.unicomm.utils;

/**
 * Created by faerytea on 18.12.16.
 */
public interface Function<T, R> {
    R apply(T arg);
}
