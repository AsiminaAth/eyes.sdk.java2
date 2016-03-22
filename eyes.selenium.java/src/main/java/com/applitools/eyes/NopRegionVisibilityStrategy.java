package com.applitools.eyes;

/**
 * An implementation of {@link RegionVisibilityStrategy} which does nothing.
 */
public class NopRegionVisibilityStrategy implements RegionVisibilityStrategy {

    private final Logger logger;

    NopRegionVisibilityStrategy(Logger logger) {
        this.logger = logger;
    }

    public void moveToRegion(PositionProvider positionProvider,
                             Location location) {
        logger.verbose("Ignored (no op).");
    }

    public void returnToOriginalPosition(PositionProvider positionProvider) {
        logger.verbose("Ignored (no op).");
    }
}
