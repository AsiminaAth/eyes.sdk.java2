package com.applitools.eyes;

import org.imgscalr.Scalr;

/**
 * The list of possible scaling methods.
 */
public enum  ScaleMethod {
    SPEED (Scalr.Method.SPEED),
    QUALITY (Scalr.Method.QUALITY),
    @SuppressWarnings("unused") ULTRA_QUALITY (Scalr.Method.ULTRA_QUALITY);

    private Scalr.Method method;

    public static ScaleMethod getDefault() {
        return SPEED;
    }

    ScaleMethod(Scalr.Method method) {
        this.method = method;
    }

    public Scalr.Method getMethod() {
        return method;
    }
}
