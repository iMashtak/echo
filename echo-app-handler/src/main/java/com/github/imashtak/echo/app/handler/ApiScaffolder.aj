package com.github.imashtak.echo.app.handler;

import com.github.imashtak.echo.distributed.EventHandlerVersion;
import com.github.imashtak.echo.distributed.EventVersion;
import com.github.imashtak.echo.distributed.Unstable;

public aspect ApiScaffolder {
    public final Api.ApiBuilder apiBuilder = Api.builder();

    after(): staticinitialization(EventHandler+) {
        var handler = thisJoinPoint
            .getSignature()
            .getDeclaringType();
        var handlerVersion = ((EventHandlerVersion) handler.getAnnotation(EventHandlerVersion.class)).value();
        var event = thisJoinPoint
            .getSignature()
            .getDeclaringType()
            .getTypeParameters()[0];
        var eventVersion = event.getAnnotation(EventVersion.class).value();
        var unstable = event.isAnnotationPresent(Unstable.class) || handler.isAnnotationPresent(Unstable.class);
        apiBuilder.entry(new ApiEntry(
            handler.getName(),
            event.getName(),
            new ApiEntryVersion(handlerVersion + "-" + eventVersion, unstable)
        ));
    }
}
