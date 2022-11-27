package io.github.imashtak.echo.spring;

import io.github.imashtak.echo.core.Bus;
import io.github.imashtak.echo.core.Event;
import io.github.imashtak.echo.core.SelfHandler;

import java.util.concurrent.atomic.AtomicInteger;

public class TestSelfHandlerEvent extends Event implements SelfHandler {

    public static final AtomicInteger handles = new AtomicInteger(0);

    @Override
    public void handleSelf(Bus bus) {
        handles.incrementAndGet();
    }
}
