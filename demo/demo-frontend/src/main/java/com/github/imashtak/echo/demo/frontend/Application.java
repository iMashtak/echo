package com.github.imashtak.echo.demo.frontend;

import com.github.imashtak.echo.distributed.EventStorage;
import com.github.imashtak.echo.distributed.Identity;
import com.github.imashtak.echo.storage.kafka.KafkaEventStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.URI;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public EventStorage kafkaEventStorage() {
        return new KafkaEventStorage("demo", x -> {});
    }

    @Bean
    public Identity identity() {
        return () -> URI.create("echo-frontend://demo");
    }
}
