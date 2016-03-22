package com.applitools.eyes;

/**
 * Encapsulates implementations for providing region visibility during
 * checkRegion.
 */
interface RegionVisibilityStrategy {
    void moveToRegion(PositionProvider positionProvider, Location location);
    void returnToOriginalPosition(PositionProvider positionProvider);
}
