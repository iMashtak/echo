package com.github.imashtak.echo.storage.kafka;

import com.github.imashtak.echo.core.Result;
import com.github.imashtak.echo.core.Task;
import com.github.imashtak.echo.distributed.EventStorage;
import com.github.imashtak.echo.distributed.Identity;
import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import reactor.core.Disposable;

import java.util.Map;
import java.util.function.Consumer;

public class KafkaEventStorage implements EventStorage, Disposable {

    private static final String DIRECT = "direct:input";

    private final CamelContext ctx;

    private final ProducerTemplate producer;

    @SneakyThrows
    public KafkaEventStorage(String topic, Consumer<KafkaConfiguration> cfg) {
        ctx = new DefaultCamelContext();
        ctx.addRoutes(routes(topic, cfg));
        producer = ctx.createProducerTemplate();
        ctx.start();
    }

    RouteBuilder routes(String topic, Consumer<KafkaConfiguration> cfg) {
        return new RouteBuilder() {
            @Override
            public void configure() {
                var endpoint = (KafkaEndpoint) ctx.getEndpoint("kafka://" + topic);
                cfg.accept(endpoint.getConfiguration());
                from(DIRECT).to(endpoint);
            }
        };
    }

    @Override
    public void publish(Identity identity, Object event) {
        producer.sendBodyAndHeaders(DIRECT, null, Map.of());
    }

    @Override
    public void publish(Identity identity, Task<?, ?> task) {
        publish(identity, (Object) task);
    }

    @Override
    public void publish(Identity identity, Result result) {
        publish(identity, (Object) result);
    }

    @Override
    public void dispose() {
        ctx.stop();
    }

    @Override
    public boolean isDisposed() {
        return ctx.isStopped() || ctx.isStopping();
    }
}
