package com.github.imashtak.echo.core;

import lombok.Getter;
import lombok.SneakyThrows;

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

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public Task(SerializedEvent x) {
        super(x);
        this.failureType = (Class<TFailure>) Class.forName(x.get("failureType", String.class));
        this.successType = (Class<TSuccess>) Class.forName(x.get("successType", String.class));
    }

}
