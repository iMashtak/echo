package com.github.imashtak.echo.communication.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.imashtak.echo.communication.Serializer;
import com.github.imashtak.echo.core.SerializedEvent;

public final class JacksonJsonSerializer implements Serializer {

    private final ObjectMapper mapper;

    public JacksonJsonSerializer() {
        this.mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public String serialize(SerializedEvent event) {
        try {
            return mapper.writeValueAsString(event.data());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
