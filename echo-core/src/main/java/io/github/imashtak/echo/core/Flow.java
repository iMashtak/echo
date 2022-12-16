package io.github.imashtak.echo.core;

import lombok.Getter;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Getter
public class Flow {
    private final UUID id;
    private final Instant createdAt;
    private final Optional<String> initiator;

    public Flow() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.initiator = Optional.empty();
    }

    public Flow(String initiator) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.initiator = Optional.of(initiator);
    }
}
