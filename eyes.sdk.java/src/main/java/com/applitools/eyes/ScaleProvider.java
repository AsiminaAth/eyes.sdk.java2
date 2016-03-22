package com.applitools.eyes;

import java.awt.image.BufferedImage;

/**
 * Encapsulates scaling logic.
 */
interface ScaleProvider {
    /**
     *
     * @return The ratio by which an image will be scaled.
     */
    double getScaleRatio();

    /**
     *
     * @param image The image to scale.
     * @return A new scaled image.
     */
    BufferedImage scaleImage(BufferedImage image);
}
