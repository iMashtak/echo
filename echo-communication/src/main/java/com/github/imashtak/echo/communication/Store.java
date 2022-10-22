package com.github.imashtak.echo.communication;

import com.github.imashtak.echo.core.Event;
import reactor.core.publisher.Flux;

import java.util.List;

public interface Store {
    Flux<Event> consume(List<String> destination);
    void produce(String destination, Event event);
}
