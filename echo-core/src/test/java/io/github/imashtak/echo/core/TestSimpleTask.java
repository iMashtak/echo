package io.github.imashtak.echo.core;

public class TestSimpleTask extends Task<TestSimpleFailure, TestSimpleSuccess> {

    public TestSimpleTask() {
        super(TestSimpleFailure.class, TestSimpleSuccess.class);
    }
}
