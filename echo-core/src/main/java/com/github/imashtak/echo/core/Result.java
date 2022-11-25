package com.github.imashtak.echo.core;

import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class Result extends Event {

    private final UUID taskId;

    protected Result(Task<?, ?> task) {
        super(task);
        this.taskId = task.id();
    }

    public boolean isSuccess() {
        return Success.class.isAssignableFrom(this.getClass());
    }

    public boolean isFailure() {
        return Failure.class.isAssignableFrom(this.getClass());
    }
}
