package com.github.imashtak.echo.communication.kafka;

import com.github.imashtak.echo.communication.AbstractStore;
import com.github.imashtak.echo.communication.Deserializer;
import com.github.imashtak.echo.communication.Serializer;
import com.github.imashtak.echo.core.SerializedEvent;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class KafkaStore extends AbstractStore {

    private final Serializer serializer;

    private final Deserializer deserializer;

    private final KafkaConsumer<String, String> consumer;

    private final KafkaProducer<String, String> producer;

    private final Duration pollInterval;

    public KafkaStore(
        Properties consumerProperties,
        Properties producerProperties,
        Serializer serializer,
        Deserializer deserializer,
        Duration pollInterval
    ) {
        this.consumer = new KafkaConsumer<>(consumerProperties);
        this.producer = new KafkaProducer<>(producerProperties);
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.pollInterval = pollInterval;
    }

    @Override
    public Flux<SerializedEvent> consumeInternal(List<String> destinations) {
        consumer.subscribe(destinations);
        return Flux.create(x -> {
            while (true) {
                var records = consumer.poll(pollInterval);
                for (var record : records) {
                    var serializedEvent = deserializer.deserialize(record.value());
                    x.next(serializedEvent);
                }
            }
        });
    }

    @Override
    public void produceInternal(String destination, SerializedEvent event) {
        var msg = new ProducerRecord<String, String>(
            destination,
            null,
            serializer.serialize(event)
        );
        producer.send(msg);
    }
}
