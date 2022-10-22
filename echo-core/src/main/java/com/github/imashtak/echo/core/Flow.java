package com.github.imashtak.echo.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public final class Flow {
    private final UUID id;
    private final Instant createdAt;

    public Flow() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }
}
