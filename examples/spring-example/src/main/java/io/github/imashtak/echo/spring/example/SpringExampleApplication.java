package io.github.imashtak.echo.spring.example;

import io.github.imashtak.echo.spring.EchoSpringConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = {EchoSpringConfiguration.class})
public class SpringExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringExampleApplication.class, args);
    }

}
