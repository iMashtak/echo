package io.github.imashtak.echo.spring.example.model;

import io.github.imashtak.echo.core.Success;
import io.github.imashtak.echo.core.Task;
import lombok.Getter;

import java.util.Optional;

@Getter
public class ChangeParameters extends Task<TaskFailed, ChangeParameters.ParametersChanged> {

    private final Optional<String> first;
    private final Optional<String> second;
    private final Optional<String> third;
    private final Optional<String> forth;

    public ChangeParameters(
        Optional<String> first,
        Optional<String> second,
        Optional<String> third,
        Optional<String> forth
    ) {
        super(TaskFailed.class, ParametersChanged.class);
        this.first = first;
        this.second = second;
        this.third = third;
        this.forth = forth;
    }

    public static class ParametersChanged extends Success {

        protected ParametersChanged(Task<?, ?> task) {
            super(task);
        }
    }
}
