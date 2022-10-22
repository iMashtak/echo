package com.github.imashtak.echo.communication;

import com.github.imashtak.echo.core.SerializedEvent;

public interface Serializer {
    String serialize(SerializedEvent event);
}
