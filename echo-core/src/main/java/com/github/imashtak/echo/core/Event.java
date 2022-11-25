package com.github.imashtak.echo.core;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class Event {
    private final UUID id;
    private final Instant createdAt;
    private final Flow flow;

    protected Event() {
        this.flow = new Flow();
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    protected Event(Event parent) {
        this.flow = parent.flow();
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }
}
