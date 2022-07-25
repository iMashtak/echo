package com.github.imashtak.echo.serialization;

import com.github.imashtak.echo.distributed.Identity;

import java.util.Map;

public interface EventSerializer {
    <T> String serialize(Identity identity, T event);
    <T> Map.Entry<Identity, T> deserialize(String raw, Class<T> type);
}
