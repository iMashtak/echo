package com.github.imashtak.echo.example.microservices.application;

import com.github.imashtak.echo.core.Bus;
import com.github.imashtak.echo.core.SelfHandler;
import com.github.imashtak.echo.core.SerializedEvent;
import com.github.imashtak.echo.core.Task;
import lombok.Getter;

import java.util.Map;

@Getter
public class ExampleTask
    extends Task<ExampleFailure, ExampleSuccess>
    implements SelfHandler
{
    private final String one;
    private final String two;

    public ExampleTask(String one, String two) {
        super(ExampleFailure.class, ExampleSuccess.class);
        this.one = one;
        this.two = two;
    }

    @Override
    public void handleSelf(Bus bus) {
        if (one.equals("error")) bus.publish(new ExampleFailure(this, new RuntimeException("this error")));
        else bus.publish(new ExampleSuccess(this, two));
    }

    public ExampleTask(SerializedEvent x) {
        super(x);
        this.one = x.get("one", String.class);
        this.two = x.get("two", String.class);
    }

    protected void serialize(Map<String, Object> x){
        x.put("one", one);
        x.put("two", two);
    }

}
