package io.github.imashtak.echo.core;

import lombok.extern.log4j.Log4j2;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Log4j2
public class AutoRegistration {

    public static void auto(
        Bus bus,
        Collection<? extends Class<?>> types,
        Function<Class<?>, Optional<?>> beanProvider
    ) {
        for (var type : types) {
            processAsHandler(type, bus, beanProvider);
            processAsSelfHandler(type, bus);
        }
    }

    private static void processAsHandler(
        Class<?> type,
        Bus bus,
        Function<Class<?>, Optional<?>> beanProvider
    ) {
        var ok = type.isAnnotationPresent(Handler.class);
        if (!ok) return;
        var bean = new AtomicReference<>(null);
        bean.set(beanProvider.apply(type).orElse(null));
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
                throw new IllegalStateException("@Handles method must be static for type '%s' in class '%s'"
                    .formatted(eventType.getName(), type.getName()));
            }
            var exHandlerMethod = eventTypeToExceptionHandleMethod.get(eventType);
            if (exHandlerMethod == null) {
                throw new RuntimeException(("Exception handler (method annotated with @HandlesExceptionsOf) " +
                    "not found for type '%s' in class '%s'").formatted(eventType.getName(), type.getName()));
            }
            checkHandlerParameterTypes(handleMethod);
            checkExceptionHandlerParameterTypes(exHandlerMethod);
            if (eventType.isAnnotation()) {
                log.info("Auto subscribing on events annotated by: {}", eventType.getName());
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
                log.info("Auto subscribing on events of type: {}", eventType.getName());
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
    private static void processAsSelfHandler(Class<?> type, Bus bus) {
        var ok = Arrays.asList(type.getInterfaces()).contains(SelfHandler.class);
        if (!ok) return;
        bus.subscribeOn((Class<? extends SelfHandler>) type);
    }

    private static void checkHandlerParameterTypes(Method method) {
        var parameterTypes = method.getParameterTypes();
        if (Modifier.isStatic(method.getModifiers())) {
            if (parameterTypes.length != 2 ||
                !Event.class.isAssignableFrom(parameterTypes[0]) ||
                !Bus.class.equals(parameterTypes[1])
            ) {
                throw new IllegalStateException("@Handles method '%s#%s' has incorrect signature"
                    .formatted(method.getDeclaringClass().getName(), method.getName())
                );
            }
        } else {
            if (parameterTypes.length != 1 ||
                !Event.class.isAssignableFrom(parameterTypes[0])
            ) {
                throw new IllegalStateException("@Handles method '%s#%s' has incorrect signature"
                    .formatted(method.getDeclaringClass().getName(), method.getName())
                );
            }
        }
    }

    private static void checkExceptionHandlerParameterTypes(Method method) {
        var parameterTypes = method.getParameterTypes();
        if (Modifier.isStatic(method.getModifiers())) {
            if (parameterTypes.length != 3 ||
                !Event.class.isAssignableFrom(parameterTypes[0]) ||
                !Throwable.class.equals(parameterTypes[1]) ||
                !Bus.class.equals(parameterTypes[2])
            ) {
                throw new IllegalStateException("@HandlesExceptionsOf method '%s#%s' has incorrect signature"
                    .formatted(method.getDeclaringClass().getName(), method.getName())
                );
            }
        } else {
            if (parameterTypes.length != 2 ||
                !Event.class.isAssignableFrom(parameterTypes[0]) ||
                !Throwable.class.equals(parameterTypes[1])
            ) {
                throw new IllegalStateException("@HandlesExceptionsOf method '%s#%s' has incorrect signature"
                    .formatted(method.getDeclaringClass().getName(), method.getName())
                );
            }
        }
    }
}
