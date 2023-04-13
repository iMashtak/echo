package io.github.imashtak;

import io.github.imashtak.echo.core.AutoRegistration;
import io.github.imashtak.echo.core.Bus;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(args);
    }

    @State(Scope.Benchmark)
    public static class ExecutionPlan {

        public Bus bus;

        @Setup(Level.Iteration)
        public void setup() {
            bus = new Bus();
            AutoRegistration.auto(
                bus,
                List.of(TestEventHandler.class),
                c -> {
                    if (c.equals(TestEventHandler.class)) return Optional.of(new TestEventHandler(bus));
                    return Optional.empty();
                }
            );
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void init(ExecutionPlan plan) {
        plan.bus.suspend(new TestTask()).block();
    }
}
