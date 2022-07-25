package com.github.imashtak.echo.app.handler.spring;

import com.github.imashtak.echo.app.handler.Api;
import com.github.imashtak.echo.app.handler.HandlerIdentity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SpringGrpcIdentity implements HandlerIdentity {

    @Getter
    private final Api api;

    @Getter
    private final URI uri = URI.create("grpc://" + UUID.randomUUID());
}
