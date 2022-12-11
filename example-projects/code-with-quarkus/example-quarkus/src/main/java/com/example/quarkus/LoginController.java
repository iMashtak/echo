package com.example.quarkus;

import io.github.imashtak.echo.core.Bus;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Path("/login")
public class LoginController {

    @Inject
    Bus bus;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String login() {
        var task = new LoginInitiated("admin", "admin");
        var result = bus.suspend(task).block(Duration.of(2, ChronoUnit.SECONDS));
        if (result == null) {
            throw new RuntimeException();
        }
        if (result.isSuccess()) {
            return ((LoginInitiated.LoginSucceed) result).getToken();
        } else {
            return "/no/";
        }
    }
}
