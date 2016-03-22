package com.applitools.eyes;

/**
 * Encapsulates state for {@link ScrollPositionProvider} instances.
 */
class ScrollPositionMemento extends PositionMemento {
    private final Location position;

    /**
     *
     * @param l The current location to be saved.
     */
    public ScrollPositionMemento(Location l) {
        position = new Location(l);
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }
}
