package cpen221.mp3.wikimediator;

import cpen221.mp3.cache.Cacheable;

import java.time.Instant;

public class MethodCall implements Cacheable {
    //RI: callTime is > 0, cannot be before Instant.now()
    //AF: Stores a method call as the time it is called with the name of the method that has been called.

    private final Instant callTime = Instant.now();
    private final String methodName;

    /**
     * Creates a MethodCall.
     * @param methodName is the name of the method that has been called.
     */
    MethodCall(String methodName) {
        if(methodName == null) {
            methodName = "";
        }
        this.methodName = methodName;
    }

    @Override
    public String id() {
        return callTime.toString();
    }
}
