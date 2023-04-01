package io.github.imashtak.echo.spring.example;

import io.github.imashtak.echo.core.Bus;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class BusHooks {

    public BusHooks(@Lazy Bus bus) {
        bus.onBeforeHandle(e -> {
            MDC.put("flowId", e.flow().id().toString());
        });
        bus.onAfterHandle(e -> {
            MDC.remove("flowId");
        });
    }


}
