package com.github.imashtak.echo.core;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class Bus {

    private final Map<Class<?>, Sinks.Many<Object>> classifiedSinks;
    private final Collection<Class<?>> sinkClassifiers;
    private final Map<UUID, Sinks.One<Result>> taskResults;
    private final Map<Predicate<Object>, Sinks.Many<Object>> predicativeSinks;

    public Bus() {
        this.classifiedSinks = new HashMap<>();
        this.sinkClassifiers = new HashSet<>();
        this.taskResults = new HashMap<>();
        this.predicativeSinks = new HashMap<>();
    }

    private static <T> Sinks.Many<T> newSinkMany() {
        return Sinks.many().multicast().directAllOrNothing();
    }

    private static <T> Sinks.One<T> newSinkOne() {
        return Sinks.one();
    }

    private void publishTyped(Object event, Class<?> type) {
        if (!sinkClassifiers.contains(type)) {
            sinkClassifiers.add(type);
            classifiedSinks.put(type, newSinkMany());
        }
        for (var sinkType : sinkClassifiers) {
            if (sinkType.isAssignableFrom(type))
                classifiedSinks.get(type).tryEmitNext(event);
        }
        for (var entry : predicativeSinks.entrySet()) {
            var filter = entry.getKey();
            var sink = entry.getValue();
            if (filter.test(event))
                sink.tryEmitNext(event);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        publish(event, (Class<T>) event.getClass());
    }

    public <T> void publish(T event, Class<T> explicitType) {
        var type = event.getClass();
        if (!Event.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException();
        }
        if (!explicitType.equals(type)) {
            publishTyped(event, explicitType);
        }
        if (Task.class.isAssignableFrom(type)) {
            publish((Task<?, ?>) event);
            return;
        } else if (Result.class.isAssignableFrom(type)) {
            publish((Result) event);
            return;
        }
        publishTyped(event, type);
    }

    public void publish(Task<?, ?> task) {
        taskResults.put(task.id(), newSinkOne());
    }

    public void publish(Result result) {
        taskResults.get(result.taskId()).tryEmitValue(result);
    }

    @SuppressWarnings("unchecked")
    public <T extends Success> Mono<T> awaitSuccess(Task<?, T> task) {
        return taskResults.get(task.id()).asMono().filter(Result::isSuccess).map(x -> (T) x);
    }

    @SuppressWarnings("unchecked")
    public <T extends Failure> Mono<T> awaitFailure(Task<T, ?> task) {
        return taskResults.get(task.id()).asMono().filter(Result::isFailure).map(x -> (T) x);
    }

    public <T extends Success> Mono<T> publishAndAwaitSuccess(Task<?, T> task) {
        publish(task);
        return awaitSuccess(task);
    }

    public <T extends Failure> Mono<T> publishAndAwaitFailure(Task<T, ?> task) {
        publish(task);
        return awaitFailure(task);
    }

    public <T> void subscribe(Class<T> type, Consumer<T> operation) {
        subscribe(this.classifiedSinks.get(type).asFlux(), operation);
    }

    @SuppressWarnings("unchecked")
    public <T> void subscribe(Predicate<T> filter, Consumer<T> operation) {
        if (!predicativeSinks.containsKey(filter))
            predicativeSinks.put((Predicate<Object>) filter, newSinkMany());
        subscribe(predicativeSinks.get(filter).asFlux(), operation);
    }

    @SuppressWarnings("unchecked")
    private <T> Disposable subscribe(Flux<?> flux, Consumer<T> operation) {
        return flux.subscribe(
            x -> {
                try {
                    operation.accept((T) x);
                } catch (Exception ex) {
                    publish(new UnhandledExceptionCaught(ex));
                }
            },
            err -> {
            },
            () -> {
            }
        );
    }
}
