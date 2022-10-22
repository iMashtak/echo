package com.github.imashtak.echo.example.example;

import com.github.imashtak.echo.spring.EchoSpringConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(new Class[]{ExampleApplication.class, EchoSpringConfiguration.class}, args);
    }

}
