package com.github.imashtak.echo.core;

public interface SelfHandler<E extends Event> {
    void handleSelf(Bus bus);
}
