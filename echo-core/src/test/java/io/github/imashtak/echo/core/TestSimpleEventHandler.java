package io.github.imashtak.echo.core;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@Handler
@RequiredArgsConstructor
public class TestSimpleEventHandler {

    public static final AtomicInteger handles = new AtomicInteger(0);

    private final Bus bus;

    @Handles(TestSimpleEvent.class)
    public void handles(TestSimpleEvent e) {
        handles.incrementAndGet();
    }

    @HandlesExceptionsOf({})
    public void onException(Event e, Throwable ex) {
        handles.incrementAndGet();
    }
}
