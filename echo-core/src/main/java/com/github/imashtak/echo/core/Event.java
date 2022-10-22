package com.github.imashtak.echo.core;

import lombok.Getter;

import java.time.Instant;
import java.util.Map;
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

    public Event(SerializedEvent x) {
        this.id = x.get("id", UUID.class);
        this.createdAt = x.get("createdAt", Instant.class);
        this.flow = new Flow(
            x.get("flowId", UUID.class),
            x.get("flowCreatedAt", Instant.class)
        );
    }

    protected void serialize(Map<String, Object> x){}
}
