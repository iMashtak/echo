package com.github.imashtak.echo.core;

import lombok.Getter;

@Getter
public abstract class Failure extends Result {

    private final Throwable cause;

    protected Failure(Task<?, ?> task, Throwable cause) {
        super(task);
        this.cause = cause;
    }
}
