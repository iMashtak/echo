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
    private final ConcurrentHashMap<UUID, Boolean> producedEventsIds = new ConcurrentHashMap<>();

    public void start() {
        var consumingThread = new Thread(() -> store
            .consume(bus.eventClasses())
            .log()
            .subscribe(event -> {
                var id = event.id();
                consumedEventsIds.put(id, true);
                var produced = producedEventsIds.remove(id);
                if (produced != null && produced) return;
                bus.publish(event);
            })
        );
        consumingThread.setName("echo-consuming");
        consumingThread.start();

        bus.subscribe((x) -> true, x -> {
            var event = (Event) x;
            var consumed = consumedEventsIds.remove(event.id());
            if (consumed != null && consumed) return;
            //noinspection unchecked
            var destination = (Class<Event>) event.getClass();
            producedEventsIds.put(event.id(), true);
            store.produce(destination, event);
        });
    }

}
