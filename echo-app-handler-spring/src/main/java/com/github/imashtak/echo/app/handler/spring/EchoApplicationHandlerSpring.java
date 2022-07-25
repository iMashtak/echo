package com.github.imashtak.echo.app.handler.spring;

import com.github.imashtak.echo.app.handler.Api;
import com.github.imashtak.echo.app.handler.ApiScaffolder;
import com.github.imashtak.echo.core.Bus;
import com.github.imashtak.echo.distributed.DistributedBus;
import com.github.imashtak.echo.distributed.EventSource;
import com.github.imashtak.echo.distributed.EventStorage;
import com.github.imashtak.echo.distributed.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.List;

@SpringBootApplication
@EnableAspectJAutoProxy
public class EchoApplicationHandlerSpring {
    @Bean
    public Bus bus() {
        return new Bus();
    }

    @Bean
    public DistributedBus distributedBus(
        @Autowired Bus bus,
        @Autowired Identity identity,
        @Autowired List<EventSource> sources,
        @Autowired List<EventStorage> storages
    ) {
        return new DistributedBus(bus, sources, identity, storages);
    }

    @Bean
    public Api api() {
        return ApiScaffolder.aspectOf().apiBuilder.build();
    }
}
