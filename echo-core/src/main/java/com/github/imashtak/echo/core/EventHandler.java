package com.github.imashtak.echo.core;

public interface EventHandler<T> {
    String name();
    void handle(T event);
}
