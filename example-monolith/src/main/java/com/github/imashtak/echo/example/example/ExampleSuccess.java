package com.github.imashtak.echo.example.example;

import com.github.imashtak.echo.core.Bus;
import com.github.imashtak.echo.core.Success;
import com.github.imashtak.echo.core.Task;
import com.github.imashtak.echo.core.Handler;
import com.github.imashtak.echo.core.Handles;

@Handler
public class ExampleSuccess extends Success {
    protected ExampleSuccess(Task<?, ?> task) {
        super(task);
    }

    @Handles(ExampleSuccess.class)
    public static void handle(ExampleSuccess event, Bus bus) {

    }
}
