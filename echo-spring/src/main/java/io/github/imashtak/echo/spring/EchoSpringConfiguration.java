package io.github.imashtak.echo.spring;


import io.github.imashtak.echo.core.Bus;
import io.github.imashtak.echo.core.SelfHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
@Log4j2
public class EchoSpringConfiguration {

    @Autowired
    ApplicationContext context;

    @Bean
    public Bus echoBus(Environment environment) {
        var options = Bus.Options.define();

        var publishNonSerializableDelay = environment
            .getProperty("echo.publishNonSerializableDelay", Long.class);
        if (publishNonSerializableDelay != null)
            options.publishNonSerializableDelay(Duration.ofMillis(publishNonSerializableDelay));

        var publishOverflowDelay = environment
            .getProperty("echo.publishOverflowDelay", Long.class);
        if (publishOverflowDelay != null)
            options.publishOverflowDelay(Duration.ofMillis(publishOverflowDelay));

        var defaultParallelism = environment
            .getProperty("echo.defaultParallelism", Integer.class);
        if (defaultParallelism != null)
            options.defaultParallelism(defaultParallelism);

        var logEvents = environment
            .getProperty("echo.publishOverflowDelay", Boolean.class);
        if (logEvents != null)
            options.logEvents(logEvents);

        var bus = new Bus(options);
        registerEventHandlers(bus);
        return bus;
    }

    @SuppressWarnings("ConstantConditions")
    private void registerEventHandlers(Bus bus) {
        var registry = new SimpleBeanDefinitionRegistry();
        var scanner = new ClassPathBeanDefinitionScanner(registry);
        scanner.resetFilters(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Handler.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(SelfHandler.class));
        scanner.scan(System.getProperty("echo.packages.to.scan").split(","));
        Arrays.stream(registry.getBeanDefinitionNames())
            .filter(name -> !name.startsWith("org.spring"))
            .map(name -> {
                var def = (ScannedGenericBeanDefinition) registry.getBeanDefinition(name);
                try {
                    return def.resolveBeanClass(ClassLoader.getSystemClassLoader());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            })
            .forEach(type -> {
                processAsHandler(type, bus);
                processAsSelfHandler(type, bus);
            });
    }

    private void processAsHandler(Class<?> type, Bus bus) {
        var ok = type.isAnnotationPresent(Handler.class);
        if (!ok) return;
        var bean = new AtomicReference<>(null);
        try {
            bean.set(context.getBean(type));
        } catch (BeanCurrentlyInCreationException ex) {
            log.error("Error while auto registering handlers on bus: do not inject bus directly via constructor. Use @Lazy or another injection methods", ex);
            throw new RuntimeException(ex);
        } catch (NoSuchBeanDefinitionException ex) {
            log.info("Not found bean of type: %s. Expecting @Handles methods are static".formatted(type.getName()));
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
            if (bean.get() == null && !Modifier.isStatic(eventType.getModifiers())) {
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
