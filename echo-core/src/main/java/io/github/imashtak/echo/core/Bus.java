package io.github.imashtak.echo.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Log4j2
public class Bus {

    private final Map<Class<?>, Sinks.Many<Object>> classifiedSinks;
    private final Collection<Class<?>> sinkClassifiers;
    private final Map<Class<? extends Annotation>, Sinks.Many<Object>> annotatedSinks;
    private final Collection<Class<? extends Annotation>> annotatedSinksClassifiers;
    private final Map<UUID, Sinks.One<Result>> taskResults;
    private final Map<Predicate<Object>, Sinks.Many<Object>> predicativeSinks;
    private final ScheduledExecutorService park;
    private final Options options;
    private final List<Consumer<Event>> onBeforeHandle;
    private final List<Consumer<Event>> onAfterHandle;

    @Getter
    @Setter
    public static class Options {

        private Duration publishNonSerializableDelay = Duration.ofMillis(1);

        private Duration publishOverflowDelay = Duration.ofMillis(50);

        private int defaultParallelism = 7;

        private boolean logEvents = false;

        private Options() {
        }

        public static Options define() {
            return new Options();
        }
    }

    public Bus(Options options) {
        this.classifiedSinks = new ConcurrentHashMap<>();
        this.sinkClassifiers = ConcurrentHashMap.newKeySet();
        this.annotatedSinks = new ConcurrentHashMap<>();
        this.annotatedSinksClassifiers = ConcurrentHashMap.newKeySet();
        this.taskResults = new ConcurrentHashMap<>();
        this.predicativeSinks = new ConcurrentHashMap<>();
        this.park = Executors.newSingleThreadScheduledExecutor();
        this.options = options;
        this.onBeforeHandle = new ArrayList<>();
        this.onAfterHandle = new ArrayList<>();
    }

    public Bus() {
        this(Options.define());
    }

    private static <T> Sinks.Many<T> newSinkMany() {
        return Sinks.many().multicast().directAllOrNothing();
    }

    private static <T> Sinks.One<T> newSinkOne() {
        return Sinks.one();
    }

    private <T> void checkSinkClassifiers(Class<T> type) {
        if (!sinkClassifiers.contains(type)) {
            sinkClassifiers.add(type);
            classifiedSinks.put(type, newSinkMany());
        }
    }

    private void checkAnnotatedSinkClassifiers(Class<? extends Annotation> type) {
        if (!annotatedSinksClassifiers.contains(type)) {
            annotatedSinksClassifiers.add(type);
            annotatedSinks.put(type, newSinkMany());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Class<Event>> eventClasses() {
        return sinkClassifiers.stream()
            .filter(Event.class::isAssignableFrom)
            .map(x -> (Class<Event>) x)
            .toList();
    }

    // region Publish methods

    private <T> void emit(Sinks.Many<T> sink, T event) {
        log.trace("Bus: emitting event of type: {}", event.getClass().getName());
        var result = sink.tryEmitNext(event);
        switch (result) {
            case FAIL_OVERFLOW -> park.schedule(
                () -> emit(sink, event),
                options.publishOverflowDelay.toNanos(),
                TimeUnit.NANOSECONDS
            );
            case FAIL_NON_SERIALIZED -> park.schedule(
                () -> emit(sink, event),
                options.publishNonSerializableDelay.toNanos(),
                TimeUnit.NANOSECONDS
            );
            case OK, FAIL_ZERO_SUBSCRIBER -> {
            }
            default -> result.orThrow();
        }
    }

    private void publishTyped(Object event, Class<?> type) {
        checkSinkClassifiers(type);
        for (var sinkType : sinkClassifiers) {
            if (sinkType.isAssignableFrom(type))
                emit(classifiedSinks.get(type), event);
        }
        for (var entry : predicativeSinks.entrySet()) {
            var filter = entry.getKey();
            var sink = entry.getValue();
            if (filter.test(event))
                emit(sink, event);
        }
        for (var annotation : annotatedSinksClassifiers) {
            checkAnnotatedSinkClassifiers(annotation);
            if (type.isAnnotationPresent(annotation)) {
                var sink = annotatedSinks.get(annotation);
                emit(sink, event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        publish(event, (Class<T>) event.getClass());
    }

    public <T> void publish(T event, Class<T> explicitType) {
        var type = event.getClass();
        if (!Event.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Attempting to publish object that is not an event, actual type is: " + type.getName());
        }
        if (!explicitType.equals(type)) {
            publishTyped(event, explicitType);
            return;
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
        publishTyped(task, task.getClass());
    }

    public void publish(Result result) {
        taskResults.get(result.taskId()).tryEmitValue(result);
        publishTyped(result, result.getClass());
    }

    // endregion

    // region Awaiting methods

    public Mono<Result> await(Task<?, ?> task) {
        return taskResults.get(task.id()).asMono().map(r -> {
            taskResults.remove(task.id());
            return r;
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Success> Mono<T> awaitSuccess(Task<?, T> task) {
        return await(task)
            .map(r -> {
                if (r.isSuccess()) {
                    return (T) r;
                } else {
                    throw new IllegalStateException("Awaited success but task resulted in failure");
                }
            });
    }

    @SuppressWarnings("unchecked")
    public <T extends Failure> Mono<T> awaitFailure(Task<T, ?> task) {
        return await(task)
            .map(r -> {
                if (r.isFailure()) {
                    return (T) r;
                } else {
                    throw new IllegalStateException("Awaited failure but task resulted in success");
                }
            });
    }

    public Mono<Result> suspend(Task<?, ?> task) {
        publish(task);
        return await(task);
    }

    public <T extends Success> Mono<T> suspendForSuccess(Task<?, T> task) {
        publish(task);
        return awaitSuccess(task);
    }

    public <T extends Failure> Mono<T> suspendForFailure(Task<T, ?> task) {
        publish(task);
        return awaitFailure(task);
    }

    // endregion

    // region Subscription methods

    public <T> Disposable subscribeOn(
        Class<T> type,
        Consumer<T> operation,
        BiConsumer<T, Throwable> onException
    ) {
        return subscribeOn(type, operation, null, onException);
    }

    public <T> Disposable subscribeOn(
        Class<T> type,
        Consumer<T> operation,
        Integer parallelism,
        BiConsumer<T, Throwable> onException
    ) {
        checkSinkClassifiers(type);
        return subscribeOn(this.classifiedSinks.get(type).asFlux(), operation, parallelism, onException);
    }

    public <T> Disposable subscribeOnAnnotated(
        Class<? extends Annotation> type,
        Consumer<T> operation,
        BiConsumer<T, Throwable> onException
    ) {
        return subscribeOnAnnotated(type, operation, null, onException);
    }

    public <T> Disposable subscribeOnAnnotated(
        Class<? extends Annotation> type,
        Consumer<T> operation,
        Integer parallelism,
        BiConsumer<T, Throwable> onException
    ) {
        checkAnnotatedSinkClassifiers(type);
        return subscribeOn(this.annotatedSinks.get(type).asFlux(), operation, parallelism, onException);
    }

    public <T extends SelfHandler> Disposable subscribeOn(
        Class<T> type
    ) {
        return subscribeOn(type, null);
    }

    public <T extends SelfHandler> Disposable subscribeOn(
        Class<T> type,
        Integer parallelism
    ) {
        return subscribeOn(type, (x) -> x.handleSelf(this), parallelism, (x, ex) -> x.onException(this, ex));
    }

    public <T> Disposable subscribeOn(
        Predicate<T> filter,
        Consumer<T> operation,
        BiConsumer<T, Throwable> onException
    ) {
        return subscribeOn(filter, operation, null, onException);
    }

    @SuppressWarnings("unchecked")
    public <T> Disposable subscribeOn(
        Predicate<T> filter,
        Consumer<T> operation,
        Integer parallelism,
        BiConsumer<T, Throwable> onException
    ) {
        if (!predicativeSinks.containsKey(filter))
            predicativeSinks.put((Predicate<Object>) filter, newSinkMany());
        return subscribeOn(predicativeSinks.get(filter).asFlux(), operation, parallelism, onException);
    }

    @SuppressWarnings("unchecked")
    private <T> Disposable subscribeOn(
        Flux<?> flux,
        Consumer<T> operation,
        Integer parallelism,
        BiConsumer<T, Throwable> onException
    ) {
        var f = flux
            .parallel(parallelism == null ? options.defaultParallelism : parallelism)
            .runOn(Schedulers.boundedElastic());
        if (options.logEvents) {
            f = f.log();
        }
        return f.subscribe(x -> {
            try {
                for (var hook : onBeforeHandle) {
                    hook.accept((Event) x);
                }
                try {
                    operation.accept((T) x);
                } catch (Exception ex) {
                    onException.accept((T) x, ex);
                }
            } finally {
                for (var hook : onAfterHandle) {
                    hook.accept((Event) x);
                }
            }
        });
    }

    // endregion

    // region Hooks

    public void onBeforeHandle(Consumer<Event> r) {
        onBeforeHandle.add(r);
    }

    public void onAfterHandle(Consumer<Event> r) {
        onAfterHandle.add(r);
    }

    // endregion
}
