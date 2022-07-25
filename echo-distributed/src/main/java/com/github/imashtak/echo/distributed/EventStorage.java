package com.github.imashtak.echo.distributed;

import com.github.imashtak.echo.core.Result;
import com.github.imashtak.echo.core.Task;

public interface EventStorage {
    void publish(Identity identity, Object event);

    void publish(Identity identity, Task<?, ?> task);

    void publish(Identity identity, Result result);
}
