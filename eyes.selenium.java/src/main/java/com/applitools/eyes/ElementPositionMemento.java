package com.applitools.eyes;

/**
 * Encapsulates state for {@link ElementPositionProvider} instances.
 */
class ElementPositionMemento extends PositionMemento {
    private final Location position;

    /**
     *
     * @param l The current location to be saved.
     */
    public ElementPositionMemento(Location l) {
        position = new Location(l);
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }
}
