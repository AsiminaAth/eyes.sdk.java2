package com.applitools.eyes;

import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.ImageUtils;

import java.awt.image.BufferedImage;

@SuppressWarnings("SpellCheckingInspection")
/**
 * Scale provider which determines the scale ratio according to the context.
 */
class ContextBasedScaleProvider implements ScaleProvider {

    // Allowed deviations for viewport size and default content entire size.
    private static final int ALLOWED_VS_DEVIATION = 1;
    private static final int ALLOWED_DCES_DEVIATION = 10;
    private static final int UNKNOWN_SCALE_RATIO = 0;

    private final ScaleMethod scaleMethod;
    private double scaleRatio;
    private double devicePixelRatio;
    private RectangleSize topLevelContextEntireSize;
    private RectangleSize viewportSize;

    /**
     *
     * @param topLevelContextEntireSize The total size of the top level
     *                                  context. E.g., for selenium this
     *                                  would be the document size of the top
     *                                  level frame.
     * @param viewportSize              The viewport size.
     * @param devicePixelRatio          The device pixel ratio of the
     *                                  platfrom on which the application is
     *                                  running.
     */
    public ContextBasedScaleProvider(
            RectangleSize topLevelContextEntireSize, RectangleSize viewportSize,
            ScaleMethod scaleMethod, double devicePixelRatio) {

        this.topLevelContextEntireSize = topLevelContextEntireSize;
        this.viewportSize = viewportSize;
        this.scaleMethod = scaleMethod;
        this.devicePixelRatio = devicePixelRatio;

        // Since we need the image size to decide what the scale ratio is.
        scaleRatio = UNKNOWN_SCALE_RATIO;
    }

    public double getScaleRatio() {
        ArgumentGuard.isValidState(scaleRatio != UNKNOWN_SCALE_RATIO,
                "scaleRatio not defined yet");
        return scaleRatio;
    }

    public BufferedImage scaleImage(BufferedImage image) {
        // First time an image is given we determine the scale ratio.
        if (scaleRatio == UNKNOWN_SCALE_RATIO) {

            int imageWidth = image.getWidth();
            int viewportWidth = viewportSize.getWidth();
            int dcesWidth = topLevelContextEntireSize.getWidth();

            // If the image's width is the same as the viewport's width or the
            // top level context's width, no scaling is necessary.
            if (((imageWidth >= viewportWidth - ALLOWED_VS_DEVIATION)
                        && (imageWidth <= viewportWidth + ALLOWED_VS_DEVIATION))
                    || ((imageWidth >= dcesWidth - ALLOWED_DCES_DEVIATION)
                        && imageWidth <= dcesWidth + ALLOWED_DCES_DEVIATION)) {
                scaleRatio = 1;
            } else {
                scaleRatio = 1 / devicePixelRatio;
            }
        }
        return ImageUtils.scaleImage(image, scaleMethod, scaleRatio);
    }
}
