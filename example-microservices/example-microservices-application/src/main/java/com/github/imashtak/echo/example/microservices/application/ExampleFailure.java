package com.github.imashtak.echo.example.microservices.application;

import com.github.imashtak.echo.core.Failure;
import com.github.imashtak.echo.core.Task;

public class ExampleFailure extends Failure {

    protected ExampleFailure(Task<?, ?> task, Throwable cause) {
        super(task, cause);
    }
}
