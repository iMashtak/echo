package com.github.imashtak.echo.example.example;

import com.github.imashtak.echo.core.Bus;
import com.github.imashtak.echo.core.Handler;
import com.github.imashtak.echo.core.Success;
import com.github.imashtak.echo.core.Task;
import com.github.imashtak.echo.spring.Handles;

public class ExampleSuccess extends Success {
    protected ExampleSuccess(Task<?, ?> task) {
        super(task);
    }

    public static class Internal implements Handler<ExampleSuccess> {

        @Handles(ExampleSuccess.class)
        @Override
        public void handle(ExampleSuccess event, Bus bus) {

        }
    }
}
