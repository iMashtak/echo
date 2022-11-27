package io.github.imashtak.echo.spring;

import io.github.imashtak.echo.core.Bus;

import java.util.concurrent.atomic.AtomicInteger;

@Handler
public class TestEventHandler {
    public static final AtomicInteger handles = new AtomicInteger(0);

    @Handles(TestSimpleFirstEvent.class)
    public static void handles(TestSimpleFirstEvent event, Bus bus) {
        handles.incrementAndGet();
    }

    @Handles(TestSimpleSecondEvent.class)
    public static void handles(TestSimpleSecondEvent event, Bus bus) {
        handles.incrementAndGet();
        handles.getAndIncrement();
    }
}
