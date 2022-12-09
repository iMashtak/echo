package io.github.imashtak.echo.core;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Flow {
    private final UUID id;
    private final Instant createdAt;

    public Flow() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }
}
