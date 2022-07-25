package com.github.imashtak.echo.distributed;

import com.github.imashtak.echo.core.*;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public final class DistributedBus implements Disposable {
    private final Bus bus;
    private final Identity identity;
    private final Iterable<EventStorage> storages;
    private final Disposable listener;

    public DistributedBus(Bus bus, Iterable<EventSource> sources, Identity identity, Iterable<EventStorage> storages) {
        this.bus = bus;
        this.identity = identity;
        this.storages = storages;
        var merged = Flux.merge(StreamSupport
            .stream(sources.spliterator(), false)
            .map(x -> x.receive(identity))
            .toList()
        );
        this.listener = merged.subscribe(bus::publish);
    }

    public void publish(Object event) {
        for (var storage : storages) {
            storage.publish(identity, event);
        }
    }

    public void publish(Task<?, ?> task) {
        for (var storage : storages) {
            storage.publish(identity, task);
        }
    }

    public void publish(Result result) {
        for (var storage : storages) {
            storage.publish(identity, result);
        }
    }

    public <T extends Success> Mono<T> awaitSuccess(Task<?, T> task) {
        return bus.awaitSuccess(task);
    }

    public <T extends Failure> Mono<T> awaitFailure(Task<T, ?> task) {
        return bus.awaitFailure(task);
    }

    public <T extends Success> Mono<T> publishAndAwaitSuccess(Task<?, T> task) {
        return bus.publishAndAwaitSuccess(task);
    }

    public <T extends Failure> Mono<T> publishAndAwaitFailure(Task<T, ?> task) {
        return bus.publishAndAwaitFailure(task);
    }

    public <T> void subscribe(Class<T> type, Consumer<T> operation) {
        bus.subscribe(type, operation);
    }

    public <T> void subscribe(Predicate<T> filter, Consumer<T> operation) {
        bus.subscribe(filter, operation);
    }

    @Override
    public void dispose() {
        listener.dispose();
    }

    @Override
    public boolean isDisposed() {
        return listener.isDisposed();
    }
}
