package com.github.imashtak.echo.distributed;

import com.github.imashtak.echo.core.Event;
import reactor.core.publisher.Flux;

public interface EventSource {
    Flux<Event> receive(Identity identity);
}
