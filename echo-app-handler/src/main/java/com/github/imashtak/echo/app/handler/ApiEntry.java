package com.github.imashtak.echo.app.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class ApiEntry {
    private final String handlerTypeName;
    private final String eventTypeName;
    private final ApiEntryVersion version;
}
