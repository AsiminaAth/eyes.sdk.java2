package com.applitools.eyes;

import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.EyesSeleniumUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;

class ScrollPositionProvider implements PositionProvider {


    protected final Logger logger;
    protected final JavascriptExecutor executor;

    public ScrollPositionProvider(Logger logger, JavascriptExecutor executor) {
        ArgumentGuard.notNull(logger, "logger");
        ArgumentGuard.notNull(executor, "executor");

        this.logger = logger;
        this.executor = executor;
    }

    /**
     * @return The scroll position of the current frame.
     */
    public Location getCurrentPosition() {
        logger.verbose("getCurrentScrollPosition()");
        Location result;
        try {
            result = EyesSeleniumUtils.getCurrentScrollPosition(executor);
        } catch (WebDriverException e) {
            throw new EyesDriverOperationException(
                    "Failed to extract current scroll position!");
        }
        logger.verbose(String.format("Current position: %s", result));
        return result;
    }

    /**
     * Go to the specified location.
     * @param location The position to scroll to.
     */
    public void setPosition(Location location) {
        logger.verbose(String.format("Scrolling to %s", location));
        EyesSeleniumUtils.setCurrentScrollPosition(executor, location);
        logger.verbose("Done scrolling!");
    }

    /**
     *
     * @return The entire size of the container which the position is relative
     * to.
     */
    public RectangleSize getEntireSize() {
        RectangleSize result =
                EyesSeleniumUtils.getCurrentFrameContentEntireSize(executor);
        logger.verbose(String.format("Entire size: %s", result));
        return result;
    }

    public PositionMemento getState() {
        return new ScrollPositionMemento(getCurrentPosition());
    }

    public void restoreState(PositionMemento state) {
        ScrollPositionMemento s = (ScrollPositionMemento) state;
        setPosition(new Location(s.getX(), s.getY()));
    }
}
