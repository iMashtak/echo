package com.github.imashtak.echo.core;

import lombok.Getter;

import java.util.Optional;

@Getter
public abstract class Task<TFailure extends Failure, TSuccess extends Success> extends Event {

    private final Class<TFailure> failureType;
    private final Class<TSuccess> successType;

    protected Task(Class<TFailure> failureType, Class<TSuccess> successType) {
        super(Optional.empty());
        this.failureType = failureType;
        this.successType = successType;
    }
}
