package io.github.imashtak.echo.core;

import lombok.Getter;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Getter
public class Flow {
    private final UUID id;
    private final Instant createdAt;
    private final Optional<Object> creator;

    public Flow() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.creator = Optional.empty();
    }

    public Flow(Object creator) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.creator = Optional.of(creator);
    }
}
