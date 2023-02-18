package io.github.imashtak.echo.spring;

import io.github.imashtak.echo.core.*;

import java.util.concurrent.atomic.AtomicInteger;

@Handler
public class TestStaticEventHandler {

    public static final AtomicInteger handles = new AtomicInteger(0);

    @Handles(TestSimpleFirstEvent.class)
    public static void handles(TestSimpleFirstEvent event, Bus bus) {
        handles.incrementAndGet();
    }

    @HandlesExceptionsOf({TestSimpleFirstEvent.class})
    public static void onException(Event event, Throwable ex, Bus bus) {

    }
}
