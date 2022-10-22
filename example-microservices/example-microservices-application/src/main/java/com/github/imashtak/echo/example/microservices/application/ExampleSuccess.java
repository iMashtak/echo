package com.github.imashtak.echo.example.microservices.application;

import com.github.imashtak.echo.core.*;
import lombok.Getter;

import java.util.Map;

@Getter
public class ExampleSuccess extends Success implements SelfHandler {

    private final String result;

    protected ExampleSuccess(Task<?, ?> task, String result) {
        super(task);
        this.result = result;
    }

    public ExampleSuccess(SerializedEvent x) {
        super(x);
        this.result = x.get("result", String.class);
    }

    @Override
    protected void serialize(Map<String, Object> x) {
        x.put("result", result);
    }

    @Override
    public void handleSelf(Bus bus) {
        System.out.println("--- ONE ---");
    }
}
