package com.github.imashtak.echo.example.example;

import com.github.imashtak.echo.core.Bus;
import com.github.imashtak.echo.core.Handler;
import com.github.imashtak.echo.core.Task;

public class ExampleTask extends Task<ExampleFailure, ExampleSuccess> implements Handler<ExampleTask> {
    protected ExampleTask() {
        super(ExampleFailure.class, ExampleSuccess.class);
    }

    @Override
    public void handle(ExampleTask event, Bus bus) {
        System.out.println("--- EXAMPLE ---");
    }
}
