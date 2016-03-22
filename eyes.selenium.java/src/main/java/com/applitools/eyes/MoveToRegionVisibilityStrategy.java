package com.applitools.eyes;

/**
 * An implementation of {@link RegionVisibilityStrategy}, which tries to move
 * to the region.
 */
class MoveToRegionVisibilityStrategy implements RegionVisibilityStrategy {

    private final Logger logger;
    private PositionMemento originalPosition;

    public MoveToRegionVisibilityStrategy(Logger logger) {
        this.logger = logger;
    }

    public void moveToRegion(PositionProvider positionProvider,
                             Location location) {
        logger.verbose("Getting current position state..");
        originalPosition = positionProvider.getState();
        logger.verbose("Done! Setting position..");
        positionProvider.setPosition(location);
        logger.verbose("Done!");
    }

    public void returnToOriginalPosition(PositionProvider positionProvider) {
        logger.verbose("Returning to original position...");
        positionProvider.restoreState(originalPosition);
        logger.verbose("Done!");
    }
}
