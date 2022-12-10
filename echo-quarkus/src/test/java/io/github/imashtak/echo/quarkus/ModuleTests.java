package io.github.imashtak.echo.quarkus;

import io.github.imashtak.echo.core.Bus;
import io.quarkus.test.junit.QuarkusTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class ModuleTests {

    static {
        System.setProperty("echo.packages.to.scan", "io.github.imashtak.echo.quarkus");
    }

    @Inject
    Bus bus;

    @Test
    @SneakyThrows
    public void testAutoRegistration() {
        var classes = bus.eventClasses();
        assertTrue(classes.contains(TestSelfHandlerEvent.class));
        assertTrue(classes.contains(TestSimpleFirstEvent.class));
        assertTrue(classes.contains(TestSimpleSecondEvent.class));

        bus.publish(new TestSelfHandlerEvent());
        bus.publish(new TestSelfHandlerEvent());
        Thread.sleep(50);
        assertEquals(2, TestSelfHandlerEvent.handles.get());

        bus.publish(new TestSimpleFirstEvent());
        bus.publish(new TestSimpleSecondEvent());
        bus.publish(new TestSimpleSecondEvent());
        bus.publish(new TestSimpleFirstEvent());
        Thread.sleep(50);
        assertEquals(8, TestEventHandler.handles.get());
    }
}
