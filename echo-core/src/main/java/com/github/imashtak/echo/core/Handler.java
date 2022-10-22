package com.github.imashtak.echo.core;

public interface Handler<E> {
    void handle(E event, Bus bus);
}
