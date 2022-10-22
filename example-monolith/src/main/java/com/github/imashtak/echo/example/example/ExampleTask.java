package com.github.imashtak.echo.example.example;

import com.github.imashtak.echo.core.Bus;
import com.github.imashtak.echo.core.SelfHandler;
import com.github.imashtak.echo.core.Task;

public class ExampleTask extends Task<ExampleFailure, ExampleSuccess> implements SelfHandler<ExampleTask> {
    protected ExampleTask() {
        super(ExampleFailure.class, ExampleSuccess.class);
    }

    @Override
    public void handleSelf(Bus bus) {
        System.out.println("--- EXAMPLE ---");
    }
}
