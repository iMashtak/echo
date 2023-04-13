package io.github.imashtak;

import io.github.imashtak.echo.core.Success;
import io.github.imashtak.echo.core.Task;

public class TestSuccess extends Success {
    protected TestSuccess(Task<?, ?> task) {
        super(task);
    }
}
