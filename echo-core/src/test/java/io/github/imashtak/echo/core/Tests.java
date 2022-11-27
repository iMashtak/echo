package io.github.imashtak.echo.core;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Tests {

    @Test
    @SneakyThrows
    public void testPublishSubscribe() {
        var ok = new AtomicBoolean(false);
        var bus = new Bus();
        bus.subscribe(TestSimpleEvent.class, e -> ok.set(true));
        bus.publish(new TestSimpleEvent());
        Thread.sleep(10);
        assertTrue(ok.get());
    }

    @Test
    @SneakyThrows
    public void testTaskAwaiting() {
        var tasks = new AtomicInteger(0);
        var successes = new AtomicInteger(0);
        var failures = new AtomicInteger(0);
        var bus = new Bus();
        bus.subscribe(TestSimpleTask.class, t -> {
            tasks.incrementAndGet();
            bus.publish(new TestSimpleSuccess(t));
        });
        bus.subscribe(TestSimpleSuccess.class, e -> successes.incrementAndGet());
        bus.subscribe(TestSimpleFailure.class, e -> failures.incrementAndGet());
        bus.publishAndAwaitSuccess(new TestSimpleTask()).block();
        assertEquals(1, tasks.get());
        assertEquals(1, successes.get());
        assertEquals(0, failures.get());
    }

    @Test
    @SneakyThrows
    public void testAnnotatedEventsHandling() {
        var ok = new AtomicBoolean(false);
        var bus = new Bus();
        bus.subscribeOnAnnotated(TestAnnotation.class, e -> ok.set(true));
        bus.publish(new TestAnnotatedEvent());
        Thread.sleep(10);
        assertTrue(ok.get());
    }

    @Test
    @SneakyThrows
    public void testExceptionDuringHandlingEvent() {
        var panics = new AtomicInteger(0);
        var bus = new Bus();
        bus.subscribe(TestSimpleEvent.class, e -> {
            throw new RuntimeException("random");
        });
        bus.subscribe(Panic.class, e -> panics.incrementAndGet());
        bus.publish(new TestSimpleEvent());
        Thread.sleep(50);
        assertEquals(1, panics.get());
    }

    @Test
    @SneakyThrows
    public void testPredicativeEventsHandling() {
        var handled = new AtomicInteger(0);
        var bus = new Bus();
        bus.subscribe(
            e -> e instanceof TestSimpleEvent || e instanceof TestSimpleTask,
            e -> handled.incrementAndGet()
        );
        bus.publish(new TestAnnotatedEvent());
        bus.publish(new TestSimpleEvent());
        bus.publish(new TestSimpleEvent());
        var task = new TestSimpleTask();
        bus.publish(task);
        bus.publish(new TestSimpleFailure(task, new RuntimeException()));
        Thread.sleep(50);
        assertEquals(3, handled.get());
    }

}
