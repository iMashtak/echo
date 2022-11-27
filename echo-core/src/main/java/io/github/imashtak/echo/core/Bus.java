package io.github.imashtak.echo.core;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Log4j2
public final class Bus {

    private final Map<Class<?>, Sinks.Many<Object>> classifiedSinks;
    private final Collection<Class<?>> sinkClassifiers;
    private final Map<Class<? extends Annotation>, Sinks.Many<Object>> annotatedSinks;
    private final Collection<Class<? extends Annotation>> annotatedSinksClassifiers;
    private final Map<UUID, Sinks.One<Result>> taskResults;
    private final Map<Predicate<Object>, Sinks.Many<Object>> predicativeSinks;
    private final ScheduledExecutorService park;
    private final Options options;

    @Accessors(fluent = true, chain = true)
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
    }

    public Bus() {
        this(Options.define());
    }

    public Bus(Consumer<Options> m) {
        this();
        m.accept(options);
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

    private <T> void emit(Sinks.Many<T> sink, T event) {
        log.trace(() -> "Bus. Emit event of type: " + event.getClass().getName());
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
            case OK, FAIL_ZERO_SUBSCRIBER -> {}
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
    public List<Class<Event>> eventClasses() {
        return sinkClassifiers.stream()
            .filter(Event.class::isAssignableFrom)
            .map(x -> (Class<Event>) x)
            .toList();
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
        publishTyped(task, task.getClass());
    }

    public void publish(Result result) {
        taskResults.get(result.taskId()).tryEmitValue(result);
        publishTyped(result, result.getClass());
    }

    public Mono<Result> await(Task<?, ?> task) {
        return taskResults.get(task.id()).asMono().map(r -> {
            taskResults.remove(task.id());
            return r;
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Success> Mono<T> awaitSuccess(Task<?, T> task) {
        return taskResults.get(task.id()).asMono().map(r -> {
            taskResults.remove(task.id());
            return r;
        }).filter(Result::isSuccess).map(x -> (T) x);
    }

    @SuppressWarnings("unchecked")
    public <T extends Failure> Mono<T> awaitFailure(Task<T, ?> task) {
        return taskResults.get(task.id()).asMono().map(r -> {
            taskResults.remove(task.id());
            return r;
        }).filter(Result::isFailure).map(x -> (T) x);
    }

    public Mono<Result> publishAndAwait(Task<?, ?> task) {
        publish(task);
        return await(task);
    }

    public <T extends Success> Mono<T> publishAndAwaitSuccess(Task<?, T> task) {
        publish(task);
        return awaitSuccess(task);
    }

    public <T extends Failure> Mono<T> publishAndAwaitFailure(Task<T, ?> task) {
        publish(task);
        return awaitFailure(task);
    }

    public Flux<Result> publishAndAwait(Work work) {
        Optional<Task<?, ?>> task;
        var results = new ArrayList<Mono<Result>>();
        while ((task = work.next()).isPresent()) {
            var result = publishAndAwait(task.get());
            results.add(result);
        }
        return Flux.merge(results);
    }

    public <T> Disposable subscribe(Class<T> type, Consumer<T> operation) {
        checkSinkClassifiers(type);
        return subscribe(this.classifiedSinks.get(type).asFlux(), operation);
    }

    public <T> Disposable subscribeOnAnnotated(Class<? extends Annotation> type, Consumer<T> operation) {
        checkAnnotatedSinkClassifiers(type);
        return subscribe(this.annotatedSinks.get(type).asFlux(), operation);
    }

    public <T extends Event & SelfHandler> Disposable subscribe(Class<T> type) {
        return subscribe(type, (x) -> x.handleSelf(this));
    }

    @SuppressWarnings("unchecked")
    public <T> Disposable subscribe(Predicate<T> filter, Consumer<T> operation) {
        if (!predicativeSinks.containsKey(filter))
            predicativeSinks.put((Predicate<Object>) filter, newSinkMany());
        return subscribe(predicativeSinks.get(filter).asFlux(), operation);
    }

    @SuppressWarnings("unchecked")
    private <T> Disposable subscribe(Flux<?> flux, Consumer<T> operation) {
        var fluxx = flux
            .parallel(options.defaultParallelism)
            .runOn(Schedulers.boundedElastic());
        if (options.logEvents) {
            fluxx = fluxx.log();
        }
        return fluxx.subscribe(
            x -> {
                try {
                    operation.accept((T) x);
                } catch (Exception ex) {
                    log.error("Bus. Error on accepting event", ex);
                    publish(new Panic((Event) x, ex));
                }
            }
        );
    }
}
