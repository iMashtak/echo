package io.github.imashtak;

import io.github.imashtak.echo.core.*;
import lombok.RequiredArgsConstructor;

@Handler
@RequiredArgsConstructor
public class TestEventHandler {
    private final Bus bus;

    @Handles(TestTask.class)
    public void handles(TestTask e) {
        bus.publish(new TestSuccess(e));
    }
    @HandlesExceptionsOf({})
    public void onException(Event e, Throwable ex) {
    }
}
