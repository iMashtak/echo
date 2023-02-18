package com.example;

import io.github.imashtak.echo.core.Bus;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class ExampleResource {

    @Inject
    Bus bus;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "";
    }
}
