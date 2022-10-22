package com.github.imashtak.echo.communication;

import com.github.imashtak.echo.core.Event;
import reactor.core.publisher.Flux;

public interface Store {
    Flux<Event> consume(String destination);
    void produce(String destination, Event event);
}
