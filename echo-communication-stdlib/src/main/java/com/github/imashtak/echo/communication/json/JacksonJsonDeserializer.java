package com.github.imashtak.echo.communication.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.imashtak.echo.communication.Deserializer;
import com.github.imashtak.echo.core.SerializedEvent;

import java.util.Map;

public final class JacksonJsonDeserializer implements Deserializer {

    private final ObjectMapper mapper;

    public JacksonJsonDeserializer() {
        this.mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public SerializedEvent deserialize(String value) {
        try {
            //noinspection unchecked
            return new SerializedEvent(mapper.readValue(value, Map.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
