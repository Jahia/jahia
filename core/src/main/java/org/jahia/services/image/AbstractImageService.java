/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Abstract class to share some methods between service implementations
 */
public abstract class AbstractImageService implements JahiaImageService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractImageService.class);

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
        double sourceAspectRadio = ((double) sourceWidth) / ((double) sourceHeight);
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
        try {
            if (square) {
                resizeImage(iw, outputFile, size, size, ResizeType.ASPECT_FILL);
            } else {
                resizeImage(iw, outputFile, size, size, ResizeType.ADJUST_SIZE);
            }
        } catch (Exception e) {
            logger.error("Error creating thumbnail of size "+size +" for image " + iw.getPath() + ":" + e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error creating thumbnail of size "+size +" for image " + iw.getPath(), e);
            }
            return false;
        }
        return true;
    }

    public boolean resizeImage(Image i, File outputFile, int width, int height) throws IOException {
        return resizeImage(i, outputFile, width, height, ResizeType.ADJUST_SIZE);
    }

}
