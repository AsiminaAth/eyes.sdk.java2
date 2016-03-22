package com.applitools.eyes;

import com.applitools.utils.ArgumentGuard;

/**
 * A location in a two-dimensional plane.
 */
public class Location implements Cloneable {
    private int x;
    private int y;

    public static final Location ZERO = new Location(0,0);

    /**
     * Creates a Location instance.
     *
     * @param x The X coordinate of this location.
     * @param y The Y coordinate of this location.
     */
    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Location)) {
            return false;
        }

        Location other = (Location) obj;
        return (this.getX() == other.getX()) && (this.getY() == other.getY());
    }

    @Override
    public int hashCode() {
        return this.getX() + this.getY();
    }

    /**
     * Creates a location from another location instance.
     * @param other A location instance from which to create the location.
     */
    public Location(Location other) {
        ArgumentGuard.notNull(other, "other");

        x = other.getX();
        y = other.getY();
    }

    /**
     * Translates this location by the specified amount (in place!).
     * <p>
     * @param dx The amount to offset the x-coordinate.
     * @param dy The amount to offset the y-coordinate.
     */
    public void offset(int dx, int dy) {
        x += dx;
        y += dy;
    }

    /**
     * Translates this location by the specified amount (in place!).
     * <p>
     * @param amount The amount the offset.
     */
    public void offset(Location amount) {
        x += amount.getX();
        y += amount.getY();
    }

    /**
     * @return The X coordinate of this location.
     */
    public int getX() {
        return x;
    }

    /**
     * @return The Y coordinate of this location.
     */
    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
