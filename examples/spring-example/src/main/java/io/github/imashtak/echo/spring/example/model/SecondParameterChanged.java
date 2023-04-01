package io.github.imashtak.echo.spring.example.model;

import io.github.imashtak.echo.core.Event;
import io.github.imashtak.echo.core.Flow;
import lombok.Getter;

@Auditable
@Getter
public class SecondParameterChanged extends Event {

    private final String oldValue;
    private final String newValue;

    public SecondParameterChanged(
        String oldValue,
        String newValue
    ) {
        super();
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public SecondParameterChanged(
        Event parent,
        String oldValue,
        String newValue
    ) {
        super(parent);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public SecondParameterChanged(
        Flow flow,
        String oldValue,
        String newValue
    ) {
        super(flow);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

}
