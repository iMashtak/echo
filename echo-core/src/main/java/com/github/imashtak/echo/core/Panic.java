package com.github.imashtak.echo.core;

import lombok.Getter;

@Getter
public final class Panic extends Event {
    private final Throwable exception;

    Panic(Throwable exception) {
        super();
        this.exception = exception;
    }

    Panic(Event parent, Throwable exception) {
        super(parent);
        this.exception = exception;
    }
}
