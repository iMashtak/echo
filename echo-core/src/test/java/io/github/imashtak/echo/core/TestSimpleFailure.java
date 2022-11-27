package io.github.imashtak.echo.core;

public class TestSimpleFailure extends Failure{
    protected TestSimpleFailure(Task<?, ?> task, Throwable cause) {
        super(task, cause);
    }
}
