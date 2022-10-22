package com.github.imashtak.echo.example.microservices.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.imashtak.echo.core.Bus;
import com.github.imashtak.echo.example.microservices.application.ExampleTask;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("example")
public class ExampleController {

    private final Bus bus;

    @PostMapping(value = "test", produces = "application/json", consumes = "application/json")
    public ResponseEntity<OutputDto> test(@RequestBody InputDto dto) {
        var task = new ExampleTask(dto.one(), dto.two());
        var result = bus.publishAndAwaitSuccess(task).block(Duration.ofSeconds(5));
        if (result == null) {
            return ResponseEntity.internalServerError().body(new OutputDto(dto.one()));
        }
        return ResponseEntity.ok(new OutputDto(result.result()));
    }

    @Data
    public static class InputDto {
        @JsonProperty
        private final String one;
        @JsonProperty
        private final String two;
    }

    @Data
    public static class OutputDto {
        @JsonProperty
        private final String three;
    }
}
