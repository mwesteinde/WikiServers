package cpen221.mp3.wikimediator;

import cpen221.mp3.cache.Cacheable;

import java.time.Instant;

public class MethodCall implements Cacheable {
    //RI: callTime is > 0, cannot be before Instant.now()
    //AF: Stores a method call as the time it is called.

    private final Instant callTime = Instant.now();

    /**
     * Creates a MethodCall.
     */
    MethodCall() {
    }

    @Override
    public String id() {
        return callTime.toString();
    }
}
