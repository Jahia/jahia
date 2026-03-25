/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.bundles.core.services.images;

import org.jahia.services.image.Image;
import org.jahia.services.image.JahiaImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Abstract base class for image service implementations providing common utility methods.
 */
public abstract class AbstractImageService implements JahiaImageService {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractImageService.class);

    protected enum OperationType {
        RESIZE, CROP, ROTATE
    }

    public static class ResizeCoords {
        private final int targetStartPosX;
        private final int targetStartPosY;
        private final int targetHeight;
        private final int targetWidth;
        private final int sourceStartPosX;
        private final int sourceStartPosY;
        private final int sourceWidth;
        private final int sourceHeight;

        public ResizeCoords(int targetStartPosX, int targetStartPosY, int targetWidth, int targetHeight,
                            int sourceStartPosX, int sourceStartPosY, int sourceWidth, int sourceHeight) {
            this.targetStartPosX = targetStartPosX;
            this.targetStartPosY = targetStartPosY;
            this.targetWidth = targetWidth;
            this.targetHeight = targetHeight;
            this.sourceStartPosX = sourceStartPosX;
            this.sourceStartPosY = sourceStartPosY;
            this.sourceWidth = sourceWidth;
            this.sourceHeight = sourceHeight;
        }

        public int getTargetStartPosX() { return targetStartPosX; }
        public int getTargetStartPosY() { return targetStartPosY; }
        public int getTargetHeight() { return targetHeight; }
        public int getTargetWidth() { return targetWidth; }
        public int getSourceStartPosX() { return sourceStartPosX; }
        public int getSourceStartPosY() { return sourceStartPosY; }
        public int getSourceWidth() { return sourceWidth; }
        public int getSourceHeight() { return sourceHeight; }
    }

    public ResizeCoords getResizeCoords(ResizeType resizeType, int sourceWidth, int sourceHeight,
                                        int targetWidth, int targetHeight) {
        int resultTargetStartPosX = 0;
        int resultTargetStartPosY = 0;
        int resultTargetWidth = targetWidth;
        int resultTargetHeight = targetHeight;
        int resultSourceWidth = sourceWidth;
        int resultSourceHeight = sourceHeight;
        int resultSourceStartPosX = 0;
        int resultSourceStartPosY = 0;

        double xScaleRatio = (double) targetWidth / sourceWidth;
        double yScaleRatio = (double) targetHeight / sourceHeight;

        if (ResizeType.SCALE_TO_FILL.equals(resizeType)) {
            // nothing to do in this case, the defaults are fine
        } else if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            if (sourceWidth > sourceHeight) {
                resultTargetHeight = sourceHeight * targetWidth / sourceWidth;
            } else {
                resultTargetWidth = sourceWidth * targetHeight / sourceHeight;
            }
        } else if (ResizeType.ASPECT_FIT.equals(resizeType)) {
            double scaleRatio = sourceHeight * xScaleRatio > targetHeight ? yScaleRatio : xScaleRatio;
            resultTargetWidth = (int) (sourceWidth * scaleRatio);
            resultTargetHeight = (int) (sourceHeight * scaleRatio);
            resultTargetStartPosX = (targetWidth - resultTargetWidth) / 2;
            resultTargetStartPosY = (targetHeight - resultTargetHeight) / 2;
        } else if (ResizeType.ASPECT_FILL.equals(resizeType)) {
            double scaleRatio = sourceHeight * xScaleRatio < targetHeight ? yScaleRatio : xScaleRatio;
            resultSourceWidth = (int) (targetWidth / scaleRatio);
            resultSourceHeight = (int) (targetHeight / scaleRatio);
            resultSourceStartPosX = (sourceWidth - resultSourceWidth) / 2;
            resultSourceStartPosY = (sourceHeight - resultSourceHeight) / 2;
        }

        return new ResizeCoords(resultTargetStartPosX, resultTargetStartPosY,
                resultTargetWidth, resultTargetHeight,
                resultSourceStartPosX, resultSourceStartPosY,
                resultSourceWidth, resultSourceHeight);
    }

    @Override
    public boolean createThumb(Image iw, File outputFile, int size, boolean square) throws IOException {
        try {
            if (square) {
                resizeImage(iw, outputFile, size, size, ResizeType.ASPECT_FILL);
            } else {
                resizeImage(iw, outputFile, size, size, ResizeType.ADJUST_SIZE);
            }
        } catch (Exception e) {
            logger.error("Error creating thumbnail of size {} for image {}: {}", size, iw.getPath(), e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error creating thumbnail", e);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean resizeImage(Image i, File outputFile, int width, int height) throws IOException {
        return resizeImage(i, outputFile, width, height, ResizeType.ADJUST_SIZE);
    }

    @Override
    public boolean rotateImage(Image i, File outputFile, boolean clockwise) throws IOException {
        return rotateImage(i, outputFile, clockwise ? 90. : -90);
    }

    protected BufferedImage createCompatibleImage(int width, int height, BufferedImage originalImage) {
        if (originalImage.getColorModel() instanceof IndexColorModel) {
            return new BufferedImage(originalImage.getColorModel(),
                    originalImage.getColorModel().createCompatibleWritableRaster(width, height),
                    false, new Hashtable<>());
        } else {
            return new BufferedImage(width, height, originalImage.getType());
        }
    }

    protected void configureGraphics(Graphics2D graphics2D, BufferedImage image, OperationType operationType) {
        if (image.getColorModel() instanceof IndexColorModel) {
            if (OperationType.RESIZE.equals(operationType)) {
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
            graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
            IndexColorModel indexColorModel = (IndexColorModel) image.getColorModel();
            int transparentPixelIndex = indexColorModel.getTransparentPixel();
            if (transparentPixelIndex > -1) {
                int transparentRGB = indexColorModel.getRGB(transparentPixelIndex);
                Color transparentColor = new Color(transparentRGB, true);
                graphics2D.setBackground(transparentColor);
                graphics2D.setColor(transparentColor);
                graphics2D.fillRect(0, 0, image.getWidth(), image.getHeight());
            }
        } else {
            if (OperationType.RESIZE.equals(operationType)) {
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
        }
    }
}
