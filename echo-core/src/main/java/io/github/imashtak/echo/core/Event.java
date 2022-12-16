package io.github.imashtak.echo.core;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class Event {
    private final UUID id;
    private final Instant createdAt;
    private final Flow flow;

    public Event() {
        this.flow = new Flow();
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    public Event(Flow flow) {
        this.flow = flow;
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    public Event(Event parent) {
        this.flow = parent.flow();
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }
}
