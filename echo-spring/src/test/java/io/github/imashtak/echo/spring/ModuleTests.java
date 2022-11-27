package io.github.imashtak.echo.spring;

import io.github.imashtak.echo.core.Bus;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {EchoSpringConfiguration.class})
public class ModuleTests {

    static {
        System.setProperty("echo.packages.to.scan", "io.github.imashtak.echo.spring");
    }

    @Autowired
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
        assertEquals(6, TestEventHandler.handles.get());
    }

}
