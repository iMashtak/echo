package io.github.imashtak.echo.spring;


import io.github.imashtak.echo.core.AutoRegistration;
import io.github.imashtak.echo.core.Bus;
import io.github.imashtak.echo.core.Handler;
import io.github.imashtak.echo.core.SelfHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Configuration
@Log4j2
@RequiredArgsConstructor
public class EchoSpringConfiguration {

    private final ApplicationContext context;

    private final Environment environment;

    @Bean
    public Bus echoBus() {
        var options = Bus.Options.define();

        var onNonSerializableRetryDelay = environment
            .getProperty("echo.onNonSerializableRetryDelay", Long.class);
        if (onNonSerializableRetryDelay != null)
            options.onNonSerializableRetryDelay(Duration.ofMillis(onNonSerializableRetryDelay));

        var onOverflowRetryDelay = environment
            .getProperty("echo.onOverflowRetryDelay", Long.class);
        if (onOverflowRetryDelay != null)
            options.onOverflowRetryDelay(Duration.ofMillis(onOverflowRetryDelay));

        var defaultParallelism = environment
            .getProperty("echo.defaultParallelism", Integer.class);
        if (defaultParallelism != null)
            options.defaultParallelism(defaultParallelism);

        var logEvents = environment
            .getProperty("echo.logEvents", Boolean.class);
        if (logEvents != null)
            options.logEvents(logEvents);

        var onOverflowRetriesCount = environment
            .getProperty("echo.onOverflowRetriesCount", Integer.class);
        if (onOverflowRetriesCount != null)
            options.onOverflowRetriesCount(onOverflowRetriesCount);

        var resurrectOnError = environment
            .getProperty("echo.resurrectOnError", Boolean.class);
        if (resurrectOnError != null)
            options.resurrectOnError(resurrectOnError);

        var registry = new SimpleBeanDefinitionRegistry();
        var scanner = new ClassPathBeanDefinitionScanner(registry);
        scanner.resetFilters(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Handler.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(SelfHandler.class));
        scanner.scan(environment.getProperty("echo.packages.to.scan").split(","));
        var types = Arrays.stream(registry.getBeanDefinitionNames())
            .filter(name -> !name.startsWith("org.spring"))
            .map(name -> {
                var def = (ScannedGenericBeanDefinition) registry.getBeanDefinition(name);
                try {
                    return def.resolveBeanClass(ClassLoader.getSystemClassLoader());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            })
            .map(x -> (Class<?>) x)
            .toList();

        var bus = new Bus(options);
        AutoRegistration.auto(bus, types, (type) -> {
            try {
                return Optional.of(context.getBean(type));
            } catch (BeanCurrentlyInCreationException ex) {
                throw new RuntimeException("Error while auto registering handlers on bus: do not inject bus directly " +
                    "via constructor. Use @Lazy or another injection methods", ex);
            } catch (NoSuchBeanDefinitionException ex) {
                log.debug("Not found bean of type: %s. Expecting @Handles methods are static".formatted(type.getName()), ex);
                return Optional.empty();
            }
        });
        return bus;
    }
}
