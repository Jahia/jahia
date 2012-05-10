package org.jahia.services.image;

/**
 * Abstract class to share some methods between service implementations
 */
public abstract class AbstractImageService implements JahiaImageService {

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

}
