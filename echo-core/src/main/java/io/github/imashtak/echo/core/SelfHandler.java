package io.github.imashtak.echo.core;

public interface SelfHandler {
    void handleSelf(Bus bus);

    default void onException(Bus bus, Throwable ex) {
    }
}
