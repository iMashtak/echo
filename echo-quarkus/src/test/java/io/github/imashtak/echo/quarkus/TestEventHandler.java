package io.github.imashtak.echo.quarkus;

import io.github.imashtak.echo.core.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
@Handler
public class TestEventHandler {
    public static final AtomicInteger handles = new AtomicInteger(0);

    private final Bus bus;

    @Inject
    public TestEventHandler(Bus bus) {
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
