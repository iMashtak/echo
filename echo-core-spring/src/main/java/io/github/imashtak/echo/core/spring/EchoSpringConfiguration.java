package io.github.imashtak.echo.core.spring;


import io.github.imashtak.echo.core.Bus;
import io.github.imashtak.echo.core.SelfHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

@Configuration
@Log4j2
public class EchoSpringConfiguration {

    @Bean
    public Bus echoBus() {
        var bus = new Bus();
        registerEventHandlers(bus);
        return bus;
    }


    @SuppressWarnings("ConstantConditions")
    private static void registerEventHandlers(Bus bus) {
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

    private static void processAsHandler(Class<?> type, Bus bus) {
        var ok = type.isAnnotationPresent(Handler.class);
        if (!ok) return;
        var handleMethods = Arrays.stream(type.getMethods())
            .filter(x -> x.isAnnotationPresent(Handles.class))
            .toList();
        for (var handleMethod : handleMethods) {
            var eventType = handleMethod.getAnnotation(Handles.class).value();
            if (eventType.isAnnotation()) {
                bus.subscribeOnAnnotated(eventType.asSubclass(Annotation.class), x -> {
                    try {
                        handleMethod.invoke(null, x, bus);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                bus.subscribe(eventType, x -> {
                    try {
                        handleMethod.invoke(null, x, bus);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void processAsSelfHandler(Class<?> type, Bus bus) {
        var ok = Arrays.asList(type.getInterfaces()).contains(SelfHandler.class);
        if (!ok) return;
        try {
            var handlerType = (Class<SelfHandler>) type;
            var handleMethod = handlerType.getMethod("handleSelf", Bus.class);
            bus.subscribe(handlerType, x -> {
                try {
                    handleMethod.invoke(x, bus);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
