package com.github.imashtak.echo.demo.events;

import com.github.imashtak.echo.core.Event;
import lombok.Getter;

import java.util.Optional;

@Getter
public class AsyncDemoTriggered extends Event {

    private final String message;

    public AsyncDemoTriggered(String message) {
        this(Optional.empty(), message);
    }

    public AsyncDemoTriggered(Optional<Event> parent, String message) {
        super(parent);
        this.message = message;
    }
}
