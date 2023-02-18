package io.github.imashtak.echo.quarkus;

import io.github.imashtak.echo.core.*;

@Handler
public class TestStaticEventHandler {

    @Handles(TestSimpleFirstEvent.class)
    public static void handles(TestSimpleFirstEvent event, Bus bus) {
        System.out.println("Hey");
    }

    @HandlesExceptionsOf({TestSimpleFirstEvent.class})
    public static void onException(Event event, Throwable ex, Bus bus) {

    }
}
