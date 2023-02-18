package io.github.imashtak.echo.spring;

import io.github.imashtak.echo.core.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Handler
@Component
public class TestEventHandler {
    public static final AtomicInteger handles = new AtomicInteger(0);

    private final Bus bus;

    public TestEventHandler(@Lazy Bus bus) {
        this.bus = bus;
    }

    @Handles(TestSimpleFirstEvent.class)
    public void handles(TestSimpleFirstEvent event) {
        handles.incrementAndGet();
    }

    @Handles(TestSimpleSecondEvent.class)
    public void handles(TestSimpleSecondEvent event) {
        handles.incrementAndGet();
        handles.getAndIncrement();
        throw new RuntimeException();
    }

    @HandlesExceptionsOf({TestSimpleFirstEvent.class, TestSimpleSecondEvent.class})
    public void onException(Event event, Throwable ex) {
        handles.incrementAndGet();
    }
}
