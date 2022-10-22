package com.github.imashtak.echo.core;

import lombok.Getter;

@Getter
public abstract class Task<TFailure extends Failure, TSuccess extends Success> extends Event {

    private final Class<TFailure> failureType;
    private final Class<TSuccess> successType;

    protected Task(Class<TFailure> failureType, Class<TSuccess> successType) {
        super();
        this.failureType = failureType;
        this.successType = successType;
    }

    protected Task(Event parent, Class<TFailure> failureType, Class<TSuccess> successType) {
        super(parent);
        this.failureType = failureType;
        this.successType = successType;
    }
}
