package io.github.imashtak.echo.core;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class Tests {

    @Test
    @SneakyThrows
    public void testPublishSubscribe() {
        var ok = new AtomicBoolean(false);
        var bus = new Bus();
        bus.subscribeOn(TestSimpleEvent.class, e -> ok.set(true), (e, ex) -> {
        });
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
        bus.subscribeOn(TestSimpleTask.class, t -> {
            tasks.incrementAndGet();
            bus.publish(new TestSimpleSuccess(t));
        }, (e, ex) -> {
        });
        bus.subscribeOn(TestSimpleSuccess.class, e -> successes.incrementAndGet(), (e, ex) -> {
        });
        bus.subscribeOn(TestSimpleFailure.class, e -> failures.incrementAndGet(), (e, ex) -> {
        });
        bus.suspend(new TestSimpleTask()).block();
        assertEquals(1, tasks.get());
        assertEquals(1, successes.get());
        assertEquals(0, failures.get());
    }

    @Test
    @SneakyThrows
    public void testTaskAwaitingSuccess() {
        var tasks = new AtomicInteger(0);
        var successes = new AtomicInteger(0);
        var failures = new AtomicInteger(0);
        var bus = new Bus();
        bus.subscribeOn(TestSimpleTask.class, t -> {
            tasks.incrementAndGet();
            bus.publish(new TestSimpleSuccess(t));
        }, (e, ex) -> {
        });
        bus.subscribeOn(TestSimpleSuccess.class, e -> successes.incrementAndGet(), (e, ex) -> {
        });
        bus.subscribeOn(TestSimpleFailure.class, e -> failures.incrementAndGet(), (e, ex) -> {
        });
        bus.suspendForSuccess(new TestSimpleTask()).block();
        assertEquals(1, tasks.get());
        assertEquals(1, successes.get());
        assertEquals(0, failures.get());
    }

    @Test
    @SneakyThrows
    public void testTaskAwaitingFailureWhileItIsSuccess() {
        var tasks = new AtomicInteger(0);
        var successes = new AtomicInteger(0);
        var failures = new AtomicInteger(0);
        var bus = new Bus();
        bus.subscribeOn(TestSimpleTask.class, t -> {
            tasks.incrementAndGet();
            bus.publish(new TestSimpleSuccess(t));
        }, (e, ex) -> {
        });
        bus.subscribeOn(TestSimpleSuccess.class, e -> successes.incrementAndGet(), (e, ex) -> {
        });
        bus.subscribeOn(TestSimpleFailure.class, e -> failures.incrementAndGet(), (e, ex) -> {
        });
        Assertions.assertThrows(IllegalStateException.class, () -> bus.suspendForFailure(new TestSimpleTask()).block());
    }

    @Test
    @SneakyThrows
    public void testAnnotatedEventsHandling() {
        var ok = new AtomicBoolean(false);
        var bus = new Bus();
        bus.subscribeOnAnnotated(TestAnnotation.class, e -> ok.set(true), (e, ex) -> {
        });
        bus.publish(new TestAnnotatedEvent());
        Thread.sleep(10);
        assertTrue(ok.get());
    }

    @Test
    @SneakyThrows
    public void testExceptionDuringHandlingEvent() {
        var exceptions = new AtomicInteger(0);
        var bus = new Bus();
        bus.subscribeOn(TestSimpleEvent.class, e -> {
            throw new RuntimeException("random");
        }, (e, ex) -> {
            exceptions.getAndIncrement();
        });
        bus.publish(new TestSimpleEvent());
        Thread.sleep(50);
        assertEquals(1, exceptions.get());
    }

    @Test
    @SneakyThrows
    public void testPredicativeEventsHandling() {
        var handled = new AtomicInteger(0);
        var bus = new Bus();
        bus.subscribeOn(
            e -> e instanceof TestSimpleEvent || e instanceof TestSimpleTask,
            e -> handled.incrementAndGet()
            , (e, ex) -> {
            });
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
