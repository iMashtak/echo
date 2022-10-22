package com.github.imashtak.echo.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public final class SerializedEvent {
    private final Map<String, Object> data;

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        var result = data.get(key);
        if (clazz.equals(UUID.class) && result instanceof String str) {
            result = UUID.fromString(str);
        }
        if (clazz.equals(Instant.class) && result instanceof String str) {
            result = Instant.parse(str);
        }
        return (T) result;
    }

    public static <T extends Event> SerializedEvent of(T event) {
        var x = new HashMap<String, Object>();
        x.put("type", event.getClass().getName());
        x.put("id", event.id());
        x.put("createdAt", event.createdAt());
        x.put("flowId", event.flow().id());
        x.put("flowCreatedAt", event.flow().createdAt());
        if (event instanceof Task<?, ?> task) {
            x.put("failureType", task.failureType().getName());
            x.put("successType", task.successType().getName());
        }
        if (event instanceof Result result) {
            x.put("taskId", result.taskId());
        }
        if (event instanceof Failure failure) {
            x.put("cause", failure.cause());
        }
        event.serialize(x);
        return new SerializedEvent(x);
    }
}
