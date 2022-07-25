package com.github.imashtak.echo.app;

import com.github.imashtak.echo.core.Bus;
import com.github.imashtak.echo.distributed.DistributedBus;

public abstract class Module {

    protected final Bus bus;

    protected final DistributedBus dbus;

    protected Module(Bus bus, DistributedBus dbus) {
        this.dbus = dbus;
        this.bus = bus;
    }

}
