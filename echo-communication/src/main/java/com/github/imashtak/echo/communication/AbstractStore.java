package com.github.imashtak.echo.communication;

import com.github.imashtak.echo.core.Event;
import com.github.imashtak.echo.core.SerializedEvent;
import reactor.core.publisher.Flux;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public abstract class AbstractStore implements Store {

    public static final String TYPE_FIELD_NAME = "type";

    @Override
    public final Flux<Event> consume(List<String> destinations) {
        return consumeInternal(destinations)
            .map(x -> {
                var type = x.get(TYPE_FIELD_NAME, String.class);
                try {
                    return (Event) Class.forName(type).getDeclaredConstructor(SerializedEvent.class).newInstance(x);
                } catch (
                    InstantiationException |
                    IllegalAccessException |
                    InvocationTargetException |
                    NoSuchMethodException |
                    ClassNotFoundException e
                ) {
                    throw new RuntimeException(e);
                }
            });
    }

    @Override
    public final void produce(String destination, Event event) {
        produceInternal(destination, SerializedEvent.of(event));
    }

    protected abstract Flux<SerializedEvent> consumeInternal(List<String> destination);

    protected abstract void produceInternal(String destination, SerializedEvent event);
}
