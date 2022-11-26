package com.github.imashtak.echo.example.example;

import com.github.imashtak.echo.core.Bus;
import com.github.imashtak.echo.core.spring.EchoSpringConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {ExampleApplicationTests.class, EchoSpringConfiguration.class})
class ExampleApplicationTests {

    @Autowired
    Bus bus;

    @Test
    void contextLoads() {
        bus.publishAndAwait(new ExampleTask()).block();
    }

}
