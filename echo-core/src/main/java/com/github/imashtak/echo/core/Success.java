package com.github.imashtak.echo.core;

public abstract class Success extends Result {

    protected Success(Task<?, ?> task) {
        super(task);
    }
}
