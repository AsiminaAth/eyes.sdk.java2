package com.applitools.eyes;

/**
 * A scale provider which does not do scaling.
 */
class NullScaleProvider extends FixedScaleProvider {

    public NullScaleProvider() {
        super(1);
    }
}
