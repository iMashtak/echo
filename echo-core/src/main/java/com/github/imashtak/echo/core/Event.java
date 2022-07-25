package com.github.imashtak.echo.core;

import lombok.Getter;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static lombok.AccessLevel.PACKAGE;

@Getter(PACKAGE)
public abstract class Event {
    private final UUID id;
    private final Instant createdAt;
    private final Flow flow;

    protected Event(Optional<Event> parent) {
        if (parent.isPresent()) {
            this.flow = parent.get().flow();
        } else {
            this.flow = new Flow();
        }
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }
}
