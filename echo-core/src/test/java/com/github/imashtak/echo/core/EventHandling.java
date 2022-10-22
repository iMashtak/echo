package com.github.imashtak.echo.core;

import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class EventHandling {

    @Getter
    private static class TestTask
        extends Task<TestTask.TestFailure, TestTask.TestSuccess>
        implements Handler<TestTask>
    {
        private static final AtomicInteger x = new AtomicInteger();

        public TestTask() {
            super(TestTask.TestFailure.class, TestTask.TestSuccess.class);
        }

        @Override
        public void handle(TestTask event, Bus bus) {
            x.incrementAndGet();
            bus.publish(new TestTask.TestSuccess(event));
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
    @ValueSource(ints = {10_000, 100_000, 1_000_000})
    @SneakyThrows
    void benchmark(int count) {
        var start = System.currentTimeMillis();
        var tasks = IntStream.range(0, count).mapToObj(i -> new TestTask()).toList();
        var work = new Work(tasks);
        var results = bus.publishAndAwait(work);

        var resultsCount = results.toStream().count();
        Assertions.assertEquals(count, resultsCount);
        System.out.println(System.currentTimeMillis() - start);
    }

}
