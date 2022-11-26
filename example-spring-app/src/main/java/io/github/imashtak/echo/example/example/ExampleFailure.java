package com.github.imashtak.echo.example.example;

import com.github.imashtak.echo.core.*;

@Notifiable
@Handler
public class ExampleFailure extends Failure {
    protected ExampleFailure(Task<?, ?> task, Throwable cause) {
        super(task, cause);
    }

    @Handles(Notifiable.class)
    public static void handle(Event event, Bus bus) {
        System.out.println("notifiable");
    }

    @Handles(Notifiable.class)
    public static void handle2(Event event, Bus bus) {
        System.out.println("notifiable2");
    }

    @Handles(Notifiable.class)
    public static void handle3(Event event, Bus bus) {
        System.out.println("notifiable3");
    }
}
