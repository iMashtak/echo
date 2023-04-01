package io.github.imashtak.echo.spring.example.controller;

import io.github.imashtak.echo.core.Bus;
import io.github.imashtak.echo.spring.example.model.ChangeParameters;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@RestController
public class Controller {

    public Controller(@Lazy Bus bus) {
        this.bus = bus;
    }

    private final Bus bus;

    @PostMapping("post")
    public Mono<ResponseEntity<String>> changeParameters(
        @RequestParam Optional<String> first,
        @RequestParam Optional<String> second,
        @RequestParam Optional<String> third,
        @RequestParam Optional<String> forth
    ) {
        var task = new ChangeParameters(first, second, third, forth);
        return bus.suspend(task)
            .map(result -> {
                if (result.isSuccess()) {
                    return ResponseEntity.ok("ok");
                } else {
                    return ResponseEntity.status(500).body("error");
                }
            })
            .timeout(Duration.ofSeconds(60));
    }

}
