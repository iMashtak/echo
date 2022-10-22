package com.github.imashtak.echo.spring;

import com.github.imashtak.echo.core.Bus;
import com.github.imashtak.echo.core.Event;
import com.github.imashtak.echo.core.Handler;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

@Configuration
public class EchoSpringConfiguration {

    @Bean
    public Bus echoBus() {
        var bus = new Bus();
        registerEventHandlers(bus);
        return bus;
    }

    @SuppressWarnings({"unchecked", "ConstantConditions", "JavaReflectionInvocation"})
    private static void registerEventHandlers(Bus bus) {
        var registry = new SimpleBeanDefinitionRegistry();
        var scanner = new ClassPathBeanDefinitionScanner(registry);
        scanner.resetFilters(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Handler.class));
        scanner.scan("");
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
                var handlerType = (Class<Handler<?>>) type;
                try {
                    var handleMethod = handlerType.getMethod("handle", Object.class, Bus.class);
                    Class<?> eventType;
                    if (Event.class.isAssignableFrom(type)) {
                        eventType = handlerType;
                    }
                    else if (handleMethod.isAnnotationPresent(Handles.class)) {
                        eventType = handleMethod.getAnnotation(Handles.class).value();
                    } else {
                        return;
                    }
                    bus.subscribe(eventType, x -> {
                        try {
                            handleMethod.invoke(x, bus);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
    }
}
