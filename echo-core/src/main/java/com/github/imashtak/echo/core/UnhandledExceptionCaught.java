package com.github.imashtak.echo.core;

import lombok.Getter;

@Getter
public final class UnhandledExceptionCaught extends Event {
    private final Throwable exception;

    UnhandledExceptionCaught(Throwable exception) {
        this.exception = exception;
    }
}
