package com.applitools.eyes;

import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.GeneralUtils;
import com.applitools.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

class FullPageCaptureAlgorithm {
    // This should pretty much cover all scroll bars (and some fixed position
    // footer elements :).
    private static final int MAX_SCROLL_BAR_SIZE = 50;
    private static final int MIN_SCREENSHOT_PART_HEIGHT = 10;

    private final Logger logger;

    public FullPageCaptureAlgorithm(Logger logger) {
        ArgumentGuard.notNull(logger, "logger");
        this.logger = logger;
    }
    /**
     * Returns a stitching of a region.
     * @param regionProvider A provider of the region to stitch. If {@code
     *                       getRegion} returns {@code Region.EMPTY}, the
     *                       entire image will be stitched.
     * @param positionProvider A provider of the scrolling implementation.
     * @param scaleProvider The provider which performs the necessary
     *                         scaling.
     * @return An image which represents the stitched region.
     */
    public BufferedImage getStitchedRegion(ImageProvider imageProvider,
            RegionProvider regionProvider, PositionProvider originProvider,
            PositionProvider positionProvider, ScaleProvider scaleProvider,
            int waitBeforeScreenshots,
            EyesScreenshotFactory screenshotFactory) {
        logger.verbose("getStitchedRegion()");

        ArgumentGuard.notNull(regionProvider, "regionProvider");
        ArgumentGuard.notNull(positionProvider, "positionProvider");

        logger.verbose(String.format("Region to check: %s",
                regionProvider.getRegion()));
        logger.verbose(String.format("Coordinates type: %s",
                regionProvider.getCoordinatesType()));

        // TODO use scaling overlap offset.
        final int SCALE_MARGIN_PX = 5;

        // Saving the original position (in case we were already in the
        // outermost frame).
        PositionMemento originalPosition = originProvider.getState();
        Location currentPosition;

        int setPositionRetries = 3;
        do {
            positionProvider.setPosition(new Location(0, 0));
            // Give the scroll time to stabilize
            GeneralUtils.sleep(waitBeforeScreenshots);
            currentPosition = positionProvider.getCurrentPosition();
        } while (currentPosition.getX() != 0
                && currentPosition.getY() != 0
                && (--setPositionRetries > 0));

        if (currentPosition.getX() != 0 || currentPosition.getY() != 0) {
            originProvider.restoreState(originalPosition);
            throw new EyesException(
                    "Couldn't set position to the to the top/left corner!");
        }

        RectangleSize entireSize = positionProvider.getEntireSize();
        logger.verbose("Entire size of region context: " + entireSize);

        logger.verbose("Getting top/left image...");
        BufferedImage image = imageProvider.getImage();
        // FIXME - scaling should be refactored
        image = scaleProvider.scaleImage(image);
        logger.verbose("Done! Creating screenshot object...");
        // We need the screenshot to be able to convert the region to
        // screenshot coordinates.
        EyesScreenshot screenshot = screenshotFactory.makeScreenshot(image);
        logger.verbose("Done! Getting region in screenshot...");
        Region regionInScreenshot =
                screenshot.convertRegionLocation(regionProvider.getRegion(),
                        regionProvider.getCoordinatesType(),
                        CoordinatesType.SCREENSHOT_AS_IS);

        logger.verbose("Done! Region in screenshot: " + regionInScreenshot);

        // Handling a specific case where the region is actually larger than
        // the screenshot (e.g., when body width/height are set to 100%, and
        // an internal div is set to value which is larger than the viewport).
        regionInScreenshot.intersect(new Region(0, 0, image.getWidth(), image
                .getHeight()));
        logger.verbose("Region after intersect: " + regionInScreenshot);

        if (!regionInScreenshot.isEmpty()) {
            image = ImageUtils.getImagePart(image, regionInScreenshot);
        }

        // Notice that this might still happen even if we used
        // "getImagePart", since "entirePageSize" might be that of a frame.
        if (image.getWidth() >= entireSize.getWidth() &&
                image.getHeight() >= entireSize.getHeight()) {
            originProvider.restoreState(originalPosition);

            return image;
        }

        // The screenshot part is a bit smaller than the screenshot size,
        // in order to eliminate duplicate bottom scroll bars, as well as fixed
        // position footers.
        RectangleSize partImageSize =
                new RectangleSize(image.getWidth(),
                        Math.max(image.getHeight() - MAX_SCROLL_BAR_SIZE,
                                MIN_SCREENSHOT_PART_HEIGHT));

        logger.verbose(String.format("Total size: %s, image part size: %s",
                entireSize, partImageSize));

        // Getting the list of sub-regions composing the whole region (we'll
        // take screenshot for each one).
        Region entirePage = new Region(Location.ZERO, entireSize);
        Iterable<Region> imageParts =
                entirePage.getSubRegions(partImageSize);

        logger.verbose("Creating stitchedImage container. Size: " + entireSize);
        //Notice stitchedImage uses the same type of image as the screenshots.
        BufferedImage stitchedImage = new BufferedImage(
                entireSize.getWidth(), entireSize.getHeight(),
                image.getType());
        logger.verbose("Done! Adding initial screenshot..");
        // Starting with the screenshot we already captured at (0,0).
        Raster initialPart = image.getData();
        logger.verbose(String.format("Initial part:(0,0)[%d x %d]",
                initialPart.getWidth(), initialPart.getHeight()));
        stitchedImage.getRaster().setRect(0, 0, initialPart);
        logger.verbose("Done!");

        PositionMemento originalStitchedState = positionProvider.getState();

        // Take screenshot and stitch for each screenshot part.
        logger.verbose("Getting the rest of the image parts...");
        BufferedImage partImage;
        for (Region partRegion: imageParts) {
            // Skipping screenshot for 0,0 (already taken)
            if (partRegion.getLeft() == 0 && partRegion.getTop() == 0) {
                continue;
            }
            logger.verbose(String.format("Taking screenshot for %s",
                    partRegion));
            // Set the position to the part's top/left.
            positionProvider.setPosition(partRegion.getLocation());
            // Giving it time to stabilize.
            GeneralUtils.sleep(waitBeforeScreenshots);
            // Screen size may cause the scroll to only reach part of the way.
            currentPosition = positionProvider.getCurrentPosition();
            logger.verbose(String.format("Set position to %s",
                    currentPosition));

            // Actually taking the screenshot.
            logger.verbose("Getting image...");
            partImage = imageProvider.getImage();
            // FIXME - scaling should be refactored
            partImage = scaleProvider.scaleImage(partImage);
            logger.verbose("Done!");

            if (!regionInScreenshot.isEmpty()) {
                partImage = ImageUtils.getImagePart(partImage,
                        regionInScreenshot);
            }

            // Stitching the current part.
            logger.verbose("Stitching part into the image container...");
            stitchedImage.getRaster().setRect(currentPosition.getX(),
                    currentPosition.getY(), partImage.getData());
            logger.verbose("Done!");
        }

        logger.verbose("Stitching done!");
        positionProvider.restoreState(originalStitchedState);
        originProvider.restoreState(originalPosition);
        return stitchedImage;
    }
}
