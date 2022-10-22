package com.github.imashtak.echo.communication;

import com.github.imashtak.echo.core.SerializedEvent;

public interface Deserializer {
    SerializedEvent deserialize(String value);
}
