package io.github.imashtak;

import io.github.imashtak.echo.core.Failure;
import io.github.imashtak.echo.core.Task;

public class TestFailure extends Failure {
    protected TestFailure(Task<?, ?> task, Throwable cause) {
        super(task, cause);
    }
}
