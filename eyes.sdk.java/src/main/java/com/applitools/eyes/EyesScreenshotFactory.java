package com.applitools.eyes;

import java.awt.image.BufferedImage;

/**
 * Encapsulates the instantiation of an EyesScreenshot object.
 */
interface EyesScreenshotFactory {
    EyesScreenshot makeScreenshot(BufferedImage image);
}
