package io.github.imashtak.echo.spring.example.model;

import io.github.imashtak.echo.core.Failure;
import io.github.imashtak.echo.core.Task;

public class TaskFailed extends Failure {
    protected TaskFailed(Task<?, ?> task, Throwable cause) {
        super(task, cause);
    }
}
