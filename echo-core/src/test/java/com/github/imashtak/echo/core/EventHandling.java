package com.github.imashtak.echo.core;

import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class EventHandling {

    @Getter
    private static class TestTask
        extends Task<TestTask.TestFailure, TestTask.TestSuccess>
        implements SelfHandler
    {
        private static final AtomicInteger x = new AtomicInteger();

        public TestTask() {
            super(TestTask.TestFailure.class, TestTask.TestSuccess.class);
        }

        @Override
        public void handleSelf(Bus bus) {
            x.incrementAndGet();
            bus.publish(new TestTask.TestSuccess(this));
        }

        public static class TestSuccess extends Success {
            protected TestSuccess(TestTask task) {
                super(task);
            }
        }

        public static class TestFailure extends Failure {
            protected TestFailure(TestTask task, Throwable cause) {
                super(task, cause);
            }
        }

    }

    static Bus bus;

    @BeforeAll
    static void setup() {
        bus = new Bus();
        bus.subscribe(TestTask.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {2_000_000, 10, 100, 1_000, 10_000, 100_000, 1_000_000})
    @SneakyThrows
    void benchmark(int count) {
        var workBuilder = Work.builder();
        for (var i = 0; i < count; ++i) {
            workBuilder.task(new TestTask());
        }
        var work = workBuilder.build();
        var start = Instant.now();
        var results = bus.publishAndAwait(work);

        var resultsCount = results.toStream().count();
        Assertions.assertEquals(count, resultsCount);
        System.out.println(Duration.between(start, Instant.now()).toMillis());
    }

}
