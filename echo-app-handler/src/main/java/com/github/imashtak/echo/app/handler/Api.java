package com.github.imashtak.echo.app.handler;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

import java.util.Collection;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
public final class Api {

    @Singular("entry")
    private final Collection<ApiEntry> entries;
}
