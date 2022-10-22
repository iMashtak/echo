package com.github.imashtak.echo.communication;

import com.github.imashtak.echo.core.Bus;
import com.github.imashtak.echo.core.Event;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public final class Communicator {

    private final Bus bus;

    private final Store store;

    private final ConcurrentHashMap<UUID, Boolean> consumedEventsIds = new ConcurrentHashMap<>();

    public void start() {
        var eventClasses = bus.eventClasses();
        for (var eventClass : eventClasses) {
            var destination = eventClassToDestination(eventClass);
            store.consume(destination)
                .log()
                .subscribe(event -> {
                    var id = event.id();
                    consumedEventsIds.put(id, true);
                    bus.publish(event);
                });
        }
        bus.subscribe(Event.class, event -> {
            var consumed = consumedEventsIds.remove(event.id());
            if (consumed != null && consumed) return;
            //noinspection unchecked
            var destination = eventClassToDestination((Class<Event>) event.getClass());
            store.produce(destination, event);
        });
    }

    public static String eventClassToDestination(Class<Event> event) {
        return event.getName();
    }

    public static Class<Event> destinationToEventClass(String destination) throws ClassNotFoundException {
        var classLoader = ClassLoader.getSystemClassLoader();
        var result = classLoader.loadClass(destination);
        if (Event.class.isAssignableFrom(result)) {
            //noinspection unchecked
            return (Class<Event>) result;
        } else {
            throw new ClassNotFoundException("");
        }
    }
}
