package com.github.imashtak.echo.communication.kafka;

import com.github.imashtak.echo.communication.AbstractStore;
import com.github.imashtak.echo.communication.Deserializer;
import com.github.imashtak.echo.communication.Serializer;
import com.github.imashtak.echo.core.Event;
import com.github.imashtak.echo.core.SerializedEvent;
import com.github.imashtak.echo.core.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class KafkaStore extends AbstractStore {

    private final Serializer serializer;

    private final Deserializer deserializer;

    private final Duration pollInterval;

    private final Options options;

    private final KafkaProducer<String, String> producer;

    public KafkaStore(Serializer serializer, Deserializer deserializer, Duration pollInterval, Options options) {
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.pollInterval = pollInterval;
        this.options = options;
        this.producer = new KafkaProducer<>(options.producerProperties);
    }

    @Override
    public Flux<SerializedEvent> consumeInternal(List<Class<Event>> destinations) {
        var map = destinations.stream()
            .collect(Collectors.partitioningBy(Task.class::isAssignableFrom));
        var taskDestinations = map.get(true);
        var otherDestinations = map.get(false);
        var taskFlux = consumeInternal(
            taskDestinations.stream().map(Class::getName).toList(),
            options.consumerProperties,
            options.groupIdForTaskTopics
        );
        var otherFlux = consumeInternal(
            otherDestinations.stream().map(Class::getName).toList(),
            options.consumerProperties,
            options.groupIdForOtherTopics
        );
        return Flux.merge(taskFlux, otherFlux);
    }

    private Flux<SerializedEvent> consumeInternal(List<String> topics, Map<String, Object> options, String groupId) {
        var properties = new Properties();
        properties.putAll(options);
        properties.put("group.id", groupId);
        var consumer = new KafkaConsumer<String, String>(properties);
        consumer.subscribe(topics);
        return Flux.create(x -> {
            while (true) {
                var records = consumer.poll(pollInterval);
                for (var record : records) {
                    var serializedEvent = deserializer.deserialize(record.value());
                    x.next(serializedEvent);
                }
            }
        }).publishOn(Schedulers.newSingle("echo-kafka-task-consumer")).map(x -> (SerializedEvent) x);
    }

    @Override
    public void produceInternal(Class<Event> destination, SerializedEvent event) {
        var msg = new ProducerRecord<String, String>(
            destination.getName(),
            null,
            serializer.serialize(event)
        );
        producer.send(msg);
    }

    @Getter
    @RequiredArgsConstructor
    public static class Options {
        private final Map<String, Object> consumerProperties;
        private final Map<String, Object> producerProperties;
        private final String groupIdForTaskTopics;
        private final String groupIdForOtherTopics;
    }
}
