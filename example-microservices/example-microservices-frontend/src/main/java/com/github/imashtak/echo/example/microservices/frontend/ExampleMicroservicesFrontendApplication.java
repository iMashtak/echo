package com.github.imashtak.echo.example.microservices.frontend;

import com.github.imashtak.echo.communication.Communicator;
import com.github.imashtak.echo.communication.json.JacksonJsonDeserializer;
import com.github.imashtak.echo.communication.json.JacksonJsonSerializer;
import com.github.imashtak.echo.communication.kafka.KafkaStore;
import com.github.imashtak.echo.core.Bus;
import com.github.imashtak.echo.core.spring.EchoSpringConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootApplication
public class ExampleMicroservicesFrontendApplication {

    public static void main(String[] args) {
        SpringApplication.run(new Class[]{
            ExampleMicroservicesFrontendApplication.class,
            EchoSpringConfiguration.class
        }, args);
    }

    private final Bus bus;

    public ExampleMicroservicesFrontendApplication(Bus bus) {
        this.bus = bus;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        var consumerProperties = new HashMap<String, Object>();
        consumerProperties.put("bootstrap.servers", "localhost:29092");
        consumerProperties.put("enable.auto.commit", "true");
        consumerProperties.put("auto.commit.interval.ms", "1000");
        consumerProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        var producerProperties = new HashMap<String, Object>();
        producerProperties.put("bootstrap.servers", "localhost:29092");
        producerProperties.put("linger.ms", 1);
        producerProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        var store = new KafkaStore(
            new JacksonJsonSerializer(),
            new JacksonJsonDeserializer(),
            Duration.ofMillis(200),
            new KafkaStore.Options(
                consumerProperties,
                producerProperties,
                "example",
                UUID.randomUUID().toString()
            )
        );
        var communicator = new Communicator(bus, store);
        communicator.start();
    }

}
