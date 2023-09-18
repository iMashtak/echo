package io.github.imashtak.echo.quarkus;

import io.github.imashtak.echo.core.AutoRegistration;
import io.github.imashtak.echo.core.Bus;
import io.quarkus.arc.Arc;
import io.quarkus.arc.DefaultBean;
import lombok.extern.log4j.Log4j2;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Dependent
public class EchoQuarkusConfiguration {

    @Produces
    @DefaultBean
    public Bus eventBus() {
        var options = Bus.Options.define();

        ConfigProvider.getConfig()
            .getOptionalValue("echo.publishNonSerializableDelay", Long.class)
            .ifPresent(x -> options.onNonSerializableRetryDelay(Duration.ofMillis(x)));

        ConfigProvider.getConfig()
            .getOptionalValue("echo.publishOverflowDelay", Long.class)
            .ifPresent(x -> options.onOverflowRetryDelay(Duration.ofMillis(x)));

        ConfigProvider.getConfig()
            .getOptionalValue("echo.defaultParallelism", Integer.class)
            .ifPresent(options::defaultParallelism);

        ConfigProvider.getConfig()
            .getOptionalValue("echo.logEvents", Boolean.class)
            .ifPresent(options::logEvents);

        ConfigProvider.getConfig()
            .getOptionalValue("echo.onOverflowRetriesCount", Integer.class)
            .ifPresent(options::onOverflowRetriesCount);

        var packages = ConfigProvider.getConfig().getValues("echo.packages.to.scan", String.class);
        var types = new HashSet<Class<?>>();
        for (var x : packages) {
            var found = findAllClasses(x, Thread.currentThread().getContextClassLoader());
            types.addAll(found);
        }
        var bus = new Bus(options);
        AutoRegistration.auto(bus, types, (type) -> {
            try (var instance = Arc.container().instance(type)) {
                if (instance.isAvailable()) {
                    return Optional.of(instance.get());
                } else {
                    return Optional.empty();
                }
            } catch (Exception e) {
                log.debug("Not found bean of type: %s. Expecting @Handles methods are static".formatted(type.getName()));
                return Optional.empty();
            }
        });
        return bus;
    }

    private Set<Class<?>> findAllClasses(String packageName, ClassLoader classLoader) {
        var stream = classLoader
            .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        var reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
            .filter(line -> line.endsWith(".class"))
            .map(line -> getClass(line, packageName, classLoader))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private Class<?> getClass(String className, String packageName, ClassLoader classLoader) {
        try {
            return classLoader.loadClass(packageName + "."
                + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
