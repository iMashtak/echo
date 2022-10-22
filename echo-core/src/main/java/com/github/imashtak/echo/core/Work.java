package com.github.imashtak.echo.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public final class Work {
    private final List<Task<?, ?>> tasks;
    private final AtomicInteger offset = new AtomicInteger();

    public Work(Task<?, ?>... tasks) {
        this.tasks = Arrays.stream(tasks).toList();
    }

    public Work(List<Task<?,?>> tasks) {
        this.tasks = tasks;
    }

    public Optional<Task<?, ?>> next() {
        var index = offset.getAndIncrement();
        return tasks.size() > index
            ? Optional.of(tasks.get(index))
            : Optional.empty();
    }

    public int processed() {
        return offset.get();
    }

    public static class Builder {
        private final List<Task<?, ?>> tasks;

        public Builder() {
            this.tasks = new ArrayList<>();
        }

        public Builder task(Task<?, ?> task) {
            this.tasks.add(task);
            return this;
        }

        public Work build() {
            return new Work(this.tasks);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
