/*
 * Applitools software.
 */
package com.applitools.utils;

import com.applitools.eyes.*;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.internal.Coordinates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * We named this class EyesSeleniumUtils because there's a SeleniumUtils
 * class, and it caused collision.
 */
public class EyesSeleniumUtils {
    // See Applitools WiKi for explanation.
    private static final String JS_GET_VIEWPORT_SIZE =
            "var height = undefined;"
                + "var width = undefined;"
                + "  if (window.innerHeight) {height = window.innerHeight;}"
                + "  else if (document.documentElement "
                + "&& document.documentElement.clientHeight) "
                + "{height = document.documentElement.clientHeight;}"
                + "  else { var b = document.getElementsByTagName('body')[0]; "
                + "if (b.clientHeight) {height = b.clientHeight;}"
                + "};"
                + " if (window.innerWidth) {width = window.innerWidth;}"
                + " else if (document.documentElement "
                + "&& document.documentElement.clientWidth) "
                + "{width = document.documentElement.clientWidth;}"
                +" else { var b = document.getElementsByTagName('body')[0]; "
                + "if (b.clientWidth) {"
                + "width = b.clientWidth;}"
                + "};"
                + "return [width, height];";

    private static final String JS_GET_CURRENT_SCROLL_POSITION =
            "var doc = document.documentElement; " +
                    "var x = window.scrollX || " +
                    "((window.pageXOffset || doc.scrollLeft) - (doc.clientLeft || 0));"
                    + " var y = window.scrollY || " +
                    "((window.pageYOffset || doc.scrollTop) - (doc.clientTop || 0));" +
                    "return [x, y];";


    // IMPORTANT: Notice there's a major difference between scrollWidth
    // and scrollHeight. While scrollWidth is the maximum between an
    // element's width and its content width, scrollHeight might be
    // smaller (!) than the clientHeight, which is why we take the
    // maximum between them.
    private static final String JS_GET_CONTENT_ENTIRE_SIZE =
            "var scrollWidth = document.documentElement.scrollWidth; " +
                    "var bodyScrollWidth = document.body.scrollWidth; " +
                    "var totalWidth = Math.max(scrollWidth, bodyScrollWidth); " +
                    "var clientHeight = document.documentElement.clientHeight; " +
                    "var bodyClientHeight = document.body.clientHeight; " +
                    "var scrollHeight = document.documentElement.scrollHeight; " +
                    "var bodyScrollHeight = document.body.scrollHeight; " +
                    "var maxDocElementHeight = Math.max(clientHeight, scrollHeight); " +
                    "var maxBodyHeight = Math.max(bodyClientHeight, bodyScrollHeight); "
                    + "var totalHeight = Math.max(maxDocElementHeight, maxBodyHeight); "
                    + "return [totalWidth, totalHeight];";

    private static final String[] JS_TRANSFORM_KEYS = { "transform",
            "-webkit-transform"
    };

    /**
     * Extracts the location relative to the entire page from the coordinates
     * (e.g. as opposed to viewport)
     * @param coordinates The coordinates from which location is extracted.
     * @return The location relative to the entire page
     */
    public static Location getPageLocation(Coordinates coordinates) {
        if (coordinates == null) {
            return null;
        }

        Point p = coordinates.onPage();
        return new Location(p.getX(), p.getY());
    }

    /**
     * Extracts the location relative to the <b>viewport</b> from the
     * coordinates (e.g. as opposed to the entire page).
     * @param coordinates The coordinates from which location is extracted.
     * @return The location relative to the viewport.
     */
    public static Location getViewportLocation(Coordinates coordinates) {
        if (coordinates == null) {
            return null;
        }

        Point p = coordinates.inViewPort();
        return new Location(p.getX(), p.getY());
    }

    /**
     *
     * @param driver The driver for which to check if it represents a mobile
     *               device.
     * @return {@code true} if the platform running the test is a mobile
     * platform. {@code false} otherwise.
     */
    public static boolean isMobileDevice(WebDriver driver) {
        return driver instanceof AppiumDriver;
    }

    /**
     * @param driver The driver for which to check the orientation.
     * @return {@code true} if this is a mobile device and is in landscape
     * orientation. {@code false} otherwise.
     */
    public static boolean isLandscapeOrientation(WebDriver driver) {
        // We can only find orientation for mobile devices.
        if (isMobileDevice(driver)) {
            AppiumDriver appiumDriver = (AppiumDriver) driver;

            try {
                // We must be in native context in order to ask for orientation,
                // because of an Appium bug.
                String originalContext = appiumDriver.getContext();
                if (appiumDriver.getContextHandles().size() > 1 &&
                        !originalContext.equalsIgnoreCase("NATIVE_APP")) {
                    appiumDriver.context("NATIVE_APP");
                } else {
                    originalContext = null;
                }

                ScreenOrientation orientation = appiumDriver.getOrientation();

                if (originalContext != null) {
                    appiumDriver.context(originalContext);
                }

                return orientation == ScreenOrientation.LANDSCAPE;
            } catch (Exception e) {
                throw new EyesDriverOperationException(
                        "Failed to get orientation!", e);
            }
        }

        return false;
    }

    /**
     * Sets the overflow of the current context's document element.
     * @param executor The executor to use for setting the overflow.
     * @param value The overflow value to set.
     * @return The previous overflow value (could be {@code null} if undefined).
     */
    public static String setOverflow(JavascriptExecutor executor,
                                     String value) {
        String script;
        if (value == null) {
            script =
                "var origOverflow = document.documentElement.style.overflow; "
                        + "document.documentElement.style.overflow = undefined;"
                        + " return origOverflow";
        } else {
            script = String.format(
                "var origOverflow = document.documentElement.style.overflow; " +
                        "document.documentElement.style.overflow = \"%s\"; " +
                        "return origOverflow",
                value);
        }
        return (String) executor.executeScript(script);
    }

    /**
     * Hides the scrollbars of the current context's document element.
     *
     * @param executor The executor to use for hiding the scrollbars.
     * @param stabilizationTimeout The amount of time to wait for the "hide
     *                             scrollbars" action to take effect
     *                             (Milliseconds). Zero/negative values are
     *                             ignored.
     * @return The previous value of the overflow property (could be
     *          {@code null}).
     */
    public static String hideScrollbars(JavascriptExecutor executor, int
            stabilizationTimeout) {
        String originalOverflow = setOverflow(executor, "hidden");
        if (stabilizationTimeout > 0) {
            try {
                Thread.sleep(stabilizationTimeout);
            } catch (InterruptedException e) {
                // Nothing to do.
            }
        }
        return originalOverflow;
    }

    /**
     *
     * @param executor The executor to use.
     * @return The current scroll position of the current frame.
     */
    public static Location getCurrentScrollPosition(
            JavascriptExecutor executor) {
        //noinspection unchecked
        List<Long> positionAsList = (List<Long>) executor.executeScript(
                JS_GET_CURRENT_SCROLL_POSITION);
        return new Location(positionAsList.get(0).intValue(),
                positionAsList.get(1).intValue());
    }

    /**
     * Sets the scroll position of the current frame.
     * @param executor The executor to use.
     * @param location The position to be set.
     */
    public static void setCurrentScrollPosition(JavascriptExecutor executor,
                                                Location location) {
        executor.executeScript(String.format("window.scrollTo(%d,%d)",
                location.getX(), location.getY()));
    }

    /**
     *
     * @param executor The executor to use.
     * @return The size of the entire content.
     */
    public static RectangleSize getCurrentFrameContentEntireSize(
            JavascriptExecutor executor) {
        RectangleSize result;
        try {
            //noinspection unchecked
            List<Long> esAsList =
                (List<Long>) executor.executeScript(JS_GET_CONTENT_ENTIRE_SIZE);
            if (esAsList.size() <= 0) {
                throw new EyesDriverOperationException(
                        "Received empty value as frame's size");
            }
            result = new RectangleSize(esAsList.get(0).intValue(),
                    esAsList.get(1).intValue());
        } catch (WebDriverException e) {
            throw new EyesDriverOperationException(
                    "Got exception while trying to extract entire size!", e);
        }
        return result;
    }

    /**
     *
     * @param executor The executor to use.
     * @return The viewport size.
     */
    public static RectangleSize executeViewportSizeExtraction(
            JavascriptExecutor executor) {
        //noinspection unchecked
        List<Long> vsAsList =
                (List<Long>) executor.executeScript(JS_GET_VIEWPORT_SIZE);
        return new RectangleSize(vsAsList.get(0).intValue(),
                vsAsList.get(1).intValue());
    }

    /**
     * @param logger The logger to use.
     * @param driver The web driver to use.
     * @return The viewport size of the current context.
     */
    public static RectangleSize extractViewportSize(Logger logger, WebDriver
            driver) {
        logger.verbose("extractViewportSize()");

        try {
            return executeViewportSizeExtraction((JavascriptExecutor) driver);
        } catch (Exception ex) {
            logger.verbose(String.format(
                    "Failed to extract viewport size using Javascript: %s",
                    ex.getMessage()));
        }
        // If we failed to extract the viewport size using JS, will use the
        // window size instead.
        logger.verbose("Using window size as viewport size.");
        Dimension windowSize = driver.manage().window().getSize();
        int width = windowSize.getWidth();
        int height = windowSize.getHeight();
        try {
            if (EyesSeleniumUtils.isLandscapeOrientation(driver) &&
                    height > width) {
                //noinspection SuspiciousNameCombination
                int height2 = width;
                //noinspection SuspiciousNameCombination
                width = height;
                height = height2;
            }
        } catch (WebDriverException e) {
            // Not every WebDriver supports querying for orientation.
        }
        logger.verbose(String.format("Done! Size %d x %d", width, height));
        return new RectangleSize(width, height);
    }

    /**
     *
     * @param logger The logger to use.
     * @param driver The web driver to use.
     * @param size The size to set as the viepwort size.
     */
    public static void setViewportSize(Logger logger, WebDriver driver,
                                RectangleSize size) {
        logger.verbose("setViewportSize(" + size + ")");

        ArgumentGuard.notNull(size, "size");

        final int SLEEP = 1000;
        final int RETRIES = 3;

        // We move the window to (0,0) to have the best chance to be able to
        // set the viewport size as requested.
        driver.manage().window().setPosition(new Point(0, 0));

        RectangleSize actualViewportSize = extractViewportSize(logger, driver);
        logger.verbose("Initial viewport size:" + actualViewportSize);

        // If the viewport size is already the required size
        if (size.getWidth() == actualViewportSize.getWidth() &&
                size.getHeight() == actualViewportSize.getHeight()) {
            logger.verbose("Required size already set.");
            return;
        }

        Dimension browserSize = driver.manage().window().getSize();
        logger.verbose("Current browser size: " + browserSize);
        Dimension requiredBrowserSize = new Dimension(
                browserSize.width +
                        (size.getWidth() - actualViewportSize.getWidth()),
                browserSize.height +
                        (size.getHeight() - actualViewportSize.getHeight()));
        logger.verbose("Trying to set browser size to: " + requiredBrowserSize);

        int retriesLeft = RETRIES;
        do {
            driver.manage().window().setSize(requiredBrowserSize);
            GeneralUtils.sleep(SLEEP);
            browserSize = driver.manage().window().getSize();
            logger.verbose("Current browser size: " + browserSize);
        } while (--retriesLeft > 0 && !browserSize.equals(requiredBrowserSize));

        if (!browserSize.equals(requiredBrowserSize)) {
            throw new EyesException("Failed to set browser size!");
        }

        actualViewportSize = extractViewportSize(logger, driver);
        logger.verbose("Current viewport size: " + actualViewportSize);
        if (!actualViewportSize.equals(size)) {
            // Additional attempt. This Solves the "maximized browser" bug
            // (border size for maximized browser sometimes different than
            // non-maximized, so the original browser size calculation is
            // wrong).
            logger.verbose("Attempting one more time...");
            browserSize = driver.manage().window().getSize();
            requiredBrowserSize = new Dimension(
                    browserSize.width +
                            (size.getWidth() - actualViewportSize.getWidth()),
                    browserSize.height +
                        (size.getHeight() - actualViewportSize.getHeight()));

            logger.verbose("Browser size: " + browserSize);
            logger.verbose("Required browser size: " + requiredBrowserSize);

            retriesLeft = RETRIES;
            do {
                driver.manage().window().setSize(requiredBrowserSize);
                GeneralUtils.sleep(SLEEP);
                actualViewportSize = extractViewportSize(logger, driver);
                logger.verbose("Browser size: "
                        + driver.manage().window().getSize());
                logger.verbose("Viewport size: " + actualViewportSize);
            } while (--retriesLeft > 0 && !actualViewportSize.equals(size));
        }

        if (!actualViewportSize.equals(size)) {
            throw new EyesException("Failed to set the viewport size.");
        }
    }

    /**
     *
     * @param driver The driver to test.
     * @return {@code true} if the driver is an Android driver.
     * {@code false} otherwise.
     */
    public static boolean isAndroid(WebDriver driver) {
        return driver instanceof AndroidDriver;
    }

    /**
     *
     * @param driver The driver to test.
     * @return {@code true} if the driver is an iOS driver.
     * {@code false} otherwise.
     */
    public static boolean isIOS(WebDriver driver) {
        return driver instanceof IOSDriver;
    }

    /**
     *
     * @param driver The driver to get the platform version from.
     * @return The plaform version or {@code null} if it is undefined.
     */
    public static String getPlatformVersion(HasCapabilities driver) {
        Capabilities capabilities = driver.getCapabilities();
        Object platformVersionObj =
                capabilities.getCapability
                        (MobileCapabilityType.PLATFORM_VERSION);

        return platformVersionObj == null ?
                null : String.valueOf(platformVersionObj);
    }

    /**
     * @param executor The executor to use.
     * @return The device pixel ratio.
     */
    public static float getDevicePixelRatio(JavascriptExecutor executor) {
        return Float.parseFloat(
                executor.executeScript("return window.devicePixelRatio")
                        .toString());
    }

    /**
     *
     * @param executor The executor to use.
     * @return The current documentElement transform values, according to
     * {@link #JS_TRANSFORM_KEYS}.
     */
    public static Map<String, String> getCurrentTransform(JavascriptExecutor
                                                           executor) {

        String script = "return { ";

        for (String key: JS_TRANSFORM_KEYS) {
            script += "'" + key + "'" +
                    ": document.documentElement.style['" + key + "'],";
        }

        // Ending the list
        script += " }";

        //noinspection unchecked
        return (Map<String, String>) executor.executeScript(script);

    }

    /**
     * Sets transforms for document.documentElement according to the given
     * map of style keys and values.
     *
     * @param executor The executor to use.
     * @param transforms The transforms to set. Keys are used as style keys,
     *                   and values are the values for those styles.
     */
    public static void setTransforms(JavascriptExecutor executor,
                                    Map<String, String> transforms) {

        String script = "";

        for (Map.Entry<String, String> entry : transforms.entrySet()) {
            script += "document.documentElement.style['" + entry.getKey() +
                    "'] = '" + entry.getValue() + "';";
        }

        executor.executeScript(script);
    }

    /**
     * Set the given transform to document.documentElement for all style keys
     * defined in {@link #JS_TRANSFORM_KEYS} .
     *
     * @param executor The executor to use.
     * @param transform The transform value to set.
     */
    public static void setTransform(JavascriptExecutor executor,
                                      String transform) {
        Map<String, String> transforms =
                new HashMap<String, String>(JS_TRANSFORM_KEYS.length);

        for (String key: JS_TRANSFORM_KEYS) {
            transforms.put(key, transform);
        }

        setTransforms(executor, transforms);
    }

    /**
     * Translates the current documentElement to the given position.
     * @param executor The executor to use.
     * @param position The position to translate to.
     */
    public static void translateTo(JavascriptExecutor executor,
                                     Location position) {
        setTransform(executor, String.format("translate(-%spx, -%spx)",
                position.getX(), position.getY()));
    }
}
