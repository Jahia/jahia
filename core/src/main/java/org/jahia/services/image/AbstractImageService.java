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
package org.jahia.services.image;

import org.jahia.osgi.BundleUtils;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.RepositoryException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Abstract class to share some methods between service implementations.
 * All methods now delegate to the OSGi image service bundle.
 * @deprecated since 8.2.4.0 - Use {@link org.jahia.services.image.JahiaImageService} OSGi service instead (org.jahia.bundles.imageservice)
 */
@Deprecated(since = "8.2.4.0", forRemoval = true)
public abstract class AbstractImageService implements JahiaImageService {

    private static final UnsupportedOperationException IMAGE_SERVICE_NOT_AVAILABLE =
            new UnsupportedOperationException("OSGi Image Service not available. Please ensure core-services bundle is deployed.");

    public class ResizeCoords {
        private int targetStartPosX, targetStartPosY, targetHeight, targetWidth,
                sourceStartPosX, sourceStartPosY, sourceWidth, sourceHeight;

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

        public int getTargetStartPosX() {
            return targetStartPosX;
        }

        public int getTargetStartPosY() {
            return targetStartPosY;
        }

        public int getTargetHeight() {
            return targetHeight;
        }

        public int getTargetWidth() {
            return targetWidth;
        }

        public int getSourceStartPosX() {
            return sourceStartPosX;
        }

        public int getSourceStartPosY() {
            return sourceStartPosY;
        }

        public int getSourceWidth() {
            return sourceWidth;
        }

        public int getSourceHeight() {
            return sourceHeight;
        }
    }

    public ResizeCoords getResizeCoords(ResizeType resizeType, int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        int resultTargetStartPosX = 0, resultTargetStartPosY = 0,
                resultTargetWidth = targetWidth, resultTargetHeight = targetHeight,
                resultSourceWidth = sourceWidth, resultSourceHeight = sourceHeight,
                resultSourceStartPosX = 0, resultSourceStartPosY = 0;
        double xScaleRatio = ((double) targetWidth) / ((double) sourceWidth);
        double yScaleRatio = ((double) targetHeight) / ((double) sourceHeight);
        if (ResizeType.SCALE_TO_FILL.equals(resizeType)) {
            // nothing to do in this case, the defaults are fine
        } else if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            if (sourceWidth > sourceHeight) {
                resultTargetWidth = targetWidth;
                resultTargetHeight = sourceHeight * targetWidth / sourceWidth;
            } else {
                resultTargetWidth = sourceWidth * targetHeight / sourceHeight;
                resultTargetHeight = targetHeight;
            }
        } else if (ResizeType.ASPECT_FIT.equals(resizeType)) {
            double scaleRatio = xScaleRatio;
            if (sourceHeight * scaleRatio > targetHeight) {
                scaleRatio = yScaleRatio;
            }
            resultTargetWidth = (int) (sourceWidth * scaleRatio);
            resultTargetHeight = (int) (sourceHeight * scaleRatio);
            resultTargetStartPosX = (targetWidth - resultTargetWidth) / 2;
            resultTargetStartPosY = (targetHeight - resultTargetHeight) / 2;
        } else if (ResizeType.ASPECT_FILL.equals(resizeType)) {
            double scaleRatio = xScaleRatio;
            if (sourceHeight * scaleRatio < targetHeight) {
                scaleRatio = yScaleRatio;
            }
            resultSourceWidth = (int) (targetWidth / scaleRatio);
            resultSourceHeight = (int) (targetHeight / scaleRatio);
            resultSourceStartPosX = (sourceWidth - resultSourceWidth) / 2;
            resultSourceStartPosY = (sourceHeight - resultSourceHeight) / 2;
        }
        return new ResizeCoords(resultTargetStartPosX, resultTargetStartPosY, resultTargetWidth, resultTargetHeight, resultSourceStartPosX, resultSourceStartPosY, resultSourceWidth, resultSourceHeight);
    }

    public boolean createThumb(Image iw, File outputFile, int size, boolean square) throws IOException {
        JahiaImageService osgiService = BundleUtils.getOsgiService(JahiaImageService.class, null);
        if (osgiService != null) {
            return osgiService.createThumb(iw, outputFile, size, square);
        }
        throw IMAGE_SERVICE_NOT_AVAILABLE;
    }

    public boolean resizeImage(Image i, File outputFile, int width, int height) throws IOException {
        JahiaImageService osgiService = BundleUtils.getOsgiService(JahiaImageService.class, null);
        if (osgiService != null) {
            return osgiService.resizeImage(i, outputFile, width, height);
        }
        throw IMAGE_SERVICE_NOT_AVAILABLE;
    }


    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
        JahiaImageService osgiService = BundleUtils.getOsgiService(JahiaImageService.class, null);
        if (osgiService != null) {
            return osgiService.getImage(node);
        }
        throw IMAGE_SERVICE_NOT_AVAILABLE;
    }

    public int getHeight(Image i) throws IOException {
        JahiaImageService osgiService = BundleUtils.getOsgiService(JahiaImageService.class, null);
        if (osgiService != null) {
            return osgiService.getHeight(i);
        }
        throw IMAGE_SERVICE_NOT_AVAILABLE;
    }

    public int getWidth(Image i) throws IOException {
        JahiaImageService osgiService = BundleUtils.getOsgiService(JahiaImageService.class, null);
        if (osgiService != null) {
            return osgiService.getWidth(i);
        }
        throw IMAGE_SERVICE_NOT_AVAILABLE;
    }

    public boolean cropImage(Image image, File outputFile, int top, int left, int width, int height) throws IOException {
        JahiaImageService osgiService = BundleUtils.getOsgiService(JahiaImageService.class, null);
        if (osgiService != null) {
            return osgiService.cropImage(image, outputFile, top, left, width, height);
        }
        throw IMAGE_SERVICE_NOT_AVAILABLE;
    }

    public boolean rotateImage(Image image, File outputFile, boolean clockwise) throws IOException {
        JahiaImageService osgiService = BundleUtils.getOsgiService(JahiaImageService.class, null);
        if (osgiService != null) {
            return osgiService.rotateImage(image, outputFile, clockwise);
        }
        throw IMAGE_SERVICE_NOT_AVAILABLE;
    }

    public boolean rotateImage(Image image, File outputFile, double degrees) throws IOException {
        JahiaImageService osgiService = BundleUtils.getOsgiService(JahiaImageService.class, null);
        if (osgiService != null) {
            return osgiService.rotateImage(image, outputFile, degrees);
        }
        throw IMAGE_SERVICE_NOT_AVAILABLE;
    }

    public boolean resizeImage(Image image, File outputFile, int newWidth, int newHeight, ResizeType resizeType) throws IOException {
        JahiaImageService osgiService = BundleUtils.getOsgiService(JahiaImageService.class, null);
        if (osgiService != null) {
            return osgiService.resizeImage(image, outputFile, newWidth, newHeight, resizeType);
        }
        throw IMAGE_SERVICE_NOT_AVAILABLE;
    }

    public BufferedImage resizeImage(BufferedImage image, int width, int height, ResizeType resizeType) throws IOException {
        JahiaImageService osgiService = BundleUtils.getOsgiService(JahiaImageService.class, null);
        if (osgiService != null) {
            return osgiService.resizeImage(image, width, height, resizeType);
        }
        throw IMAGE_SERVICE_NOT_AVAILABLE;
    }
}
