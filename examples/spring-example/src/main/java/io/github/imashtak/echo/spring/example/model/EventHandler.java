package io.github.imashtak.echo.spring.example.model;

import io.github.imashtak.echo.core.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Handler
@Component
@Log4j2
public class EventHandler {

    private final Bus bus;

    public EventHandler(@Lazy Bus bus) {
        this.bus = bus;
    }

    @Handles(ChangeParameters.class)
    public void handle(ChangeParameters e) {
        log.info("handling ChangeParameters event");
        var parameters = Parameters.instance();
        var oldSecondParameterValue = parameters.getSecond();
        e.getFirst().ifPresent(parameters::setFirst);
        e.getSecond().ifPresent(x -> {
            bus.publish(new SecondParameterChanged(e, oldSecondParameterValue, x));
            parameters.setSecond(x);
        });
        e.getThird().ifPresent(parameters::setThird);
        e.getForth().ifPresent(parameters::setForth);
        bus.publish(new ChangeParameters.ParametersChanged(e));
    }

    @Handles(Auditable.class)
    public void handleAuditable(Event e) {
        log.info("Event raised: " + e.getClass().getName());
    }

    @HandlesExceptionsOf({ChangeParameters.class, Auditable.class})
    public void handleExceptions(Event e, Throwable ex) {
       log.error(ex);
    }

}
