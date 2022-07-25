package com.github.imashtak.echo.app.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;

@Getter
@RequiredArgsConstructor
public final class ApiEntryVersion {
    private static final Pattern pattern = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)-(\\d)$");

    private final Integer major;
    private final Integer minor;
    private final Integer patch;
    private final String eventVersion;
    private final Boolean unstable;

    public ApiEntryVersion(Integer major, Integer minor, Integer patch, String eventVersion) {
        this(major, minor, patch, eventVersion, false);
    }

    public ApiEntryVersion(String version) {
        this(version, false);
    }

    public ApiEntryVersion(String version, Boolean unstable) {
        var parts = pattern.split(version);
        this.major = Integer.valueOf(parts[0]);
        this.minor = Integer.valueOf(parts[1]);
        this.patch = Integer.valueOf(parts[2]);
        this.eventVersion = parts[3];
        this.unstable = unstable;
    }
}
