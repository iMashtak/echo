package io.github.imashtak.echo.quarkus;

import io.github.imashtak.echo.core.Bus;
import io.github.imashtak.echo.core.SelfHandler;
import io.quarkus.arc.Arc;
import io.quarkus.arc.DefaultBean;
import lombok.extern.log4j.Log4j2;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
            .ifPresent(x -> options.publishNonSerializableDelay(Duration.ofMillis(x)));

        ConfigProvider.getConfig()
            .getOptionalValue("echo.publishOverflowDelay", Long.class)
            .ifPresent(aLong -> options.publishOverflowDelay(Duration.ofMillis(aLong)));

        ConfigProvider.getConfig()
            .getOptionalValue("echo.defaultParallelism", Integer.class)
            .ifPresent(options::defaultParallelism);

        ConfigProvider.getConfig()
            .getOptionalValue("echo.logEvents", Boolean.class)
            .ifPresent(options::logEvents);

        var bus = new Bus(options);
        registerEventHandlers(bus);
        return bus;
    }

    private void registerEventHandlers(Bus bus) {
        var packages = ConfigProvider.getConfig().getValues("echo.packages.to.scan", String.class);
        var classes = new HashSet<Class<?>>();
        for (var x : packages) {
            var found = findAllClasses(x, Thread.currentThread().getContextClassLoader());
            classes.addAll(found);
        }
        for (var clazz : classes) {
            if (clazz.isAnnotationPresent(Handler.class)) {
                processAsHandler(clazz, bus);
            }
            if (SelfHandler.class.isAssignableFrom(clazz)) {
                processAsSelfHandler(clazz, bus);
            }
        }
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

    private void processAsHandler(Class<?> type, Bus bus) {
        var ok = type.isAnnotationPresent(Handler.class);
        if (!ok) return;
        var bean = new AtomicReference<>(null);
        try (var instance = Arc.container().instance(type)) {
            if (instance.isAvailable()) {
                bean.set(instance.get());
            }
        } catch (Exception e) {
            log.debug("Not found bean of type: %s. Expecting @Handles methods are static".formatted(type.getName()));
        }
        var handleMethods = Arrays.stream(type.getMethods())
            .filter(x -> x.isAnnotationPresent(Handles.class))
            .toList();
        var eventTypeToExceptionHandleMethod = new HashMap<Class<?>, Method>();
        Arrays.stream(type.getMethods())
            .filter(x -> x.isAnnotationPresent(HandlesExceptionsOf.class))
            .forEach(x -> {
                var a = x.getAnnotation(HandlesExceptionsOf.class);
                var classes = a.value();
                for (var c : classes) {
                    eventTypeToExceptionHandleMethod.put(c, x);
                }
            });
        for (var handleMethod : handleMethods) {
            var eventType = handleMethod.getAnnotation(Handles.class).value();
            if (bean.get() == null && !Modifier.isStatic(handleMethod.getModifiers())) {
                throw new IllegalStateException("@Handles method must be static");
            }
            var exHandlerMethod = eventTypeToExceptionHandleMethod.get(eventType);
            if (exHandlerMethod == null) {
                throw new RuntimeException("not found exception handler for type: " + eventType.getName());
            }
            if (eventType.isAnnotation()) {
                log.info(() -> "Auto subscribing on events annotated by: " + eventType.getName());
                bus.subscribeOnAnnotated(eventType.asSubclass(Annotation.class), x -> {
                    try {
                        if (bean.get() == null) {
                            handleMethod.invoke(null, x, bus);
                        } else {
                            handleMethod.invoke(bean.get(), x);
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }, (e, ex) -> {
                    try {
                        if (bean.get() == null) {
                            exHandlerMethod.invoke(null, e, ex, bus);
                        } else {
                            exHandlerMethod.invoke(bean.get(), e, ex);
                        }
                    } catch (IllegalAccessException | InvocationTargetException exc) {
                        throw new RuntimeException(exc);
                    }
                });
            } else {
                log.info(() -> "Auto subscribing on events of type: " + eventType.getName());
                bus.subscribeOn(eventType, x -> {
                    try {
                        if (bean.get() == null) {
                            handleMethod.invoke(null, x, bus);
                        } else {
                            handleMethod.invoke(bean.get(), x);
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }, (e, ex) -> {
                    try {
                        if (bean.get() == null) {
                            exHandlerMethod.invoke(null, e, ex, bus);
                        } else {
                            exHandlerMethod.invoke(bean.get(), e, ex);
                        }
                    } catch (IllegalAccessException | InvocationTargetException exc) {
                        throw new RuntimeException(exc);
                    }
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processAsSelfHandler(Class<?> type, Bus bus) {
        var ok = Arrays.asList(type.getInterfaces()).contains(SelfHandler.class);
        if (!ok) return;
        bus.subscribeOn((Class<? extends SelfHandler>) type);
    }
}
