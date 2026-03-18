package com.metrostamp.api.common.utils;

import java.time.Clock;

public final class ClockProvider {

    private ClockProvider() {
    }

    public static Clock system() {
        return Clock.systemUTC();
    }
}

