package io.github.imashtak.echo.core;

import lombok.Getter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class Flow {
    private final UUID id;
    private final Instant createdAt;
    private final Map<String, Object> context;

    public Flow() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.context = new HashMap<>();
    }

    public Flow(Map<String, Object> context) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.context = context;
    }
}
