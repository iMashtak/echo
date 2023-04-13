package io.github.imashtak;

import io.github.imashtak.echo.core.Task;

public class TestTask extends Task<TestFailure, TestSuccess> {

    public TestTask() {
        super(TestFailure.class, TestSuccess.class);
    }
}
