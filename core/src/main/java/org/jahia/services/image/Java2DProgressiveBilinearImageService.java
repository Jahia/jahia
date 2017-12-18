/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;

/**
 * Progressive Bilinear implementation of the Java 2D image operation.
 * <p/>
 * This algorithm comes from http://code.google.com/p/thumbnailator/, itself used from the
 * example code from the resizing technique
 * discussed in <em>Chapter 4: Images</em> of
 * <a href="http://filthyrichclients.org">Filthy Rich Clients</a>
 * by Chet Haase and Romain Guy.
 */
public class Java2DProgressiveBilinearImageService extends AbstractJava2DImageService {

    @Override
    public boolean resizeImage(Image image, File outputFile, int newWidth, int newHeight, ResizeType resizeType) throws IOException {

        BufferedImage originalImage = ((BufferImage) image).getOriginalImage();

        BufferedImage dest = resizeImage(originalImage, newWidth, newHeight, resizeType);
        if (dest == null) {
            return false;
        }

        // Save destination image
        saveImageToFile(dest, ((BufferImage) image).getMimeType(), outputFile);
        return true;
    }

    @Override
    public BufferedImage resizeImage(BufferedImage image, int width, int height, ResizeType resizeType) {
        ResizeCoords resizeCoords = getResizeCoords(resizeType, image.getWidth(), image.getHeight(), width, height);
        if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            width = resizeCoords.getTargetWidth();
            height = resizeCoords.getTargetHeight();
        }

        int currentWidth = resizeCoords.getSourceWidth();
        int currentHeight = resizeCoords.getSourceHeight();
        int targetWidth = resizeCoords.getTargetWidth();
        int targetHeight = resizeCoords.getTargetHeight();

        BufferedImage dest = getDestImage(width, height, image);

        // Paint source image into the destination, scaling as needed
        Graphics2D graphics2D = getGraphics2D(dest, OperationType.RESIZE);

        // If multi-step downscaling is not required, perform one-step.
        if ((width * 2 >= resizeCoords.getSourceWidth()) && (height * 2 >= resizeCoords.getSourceHeight())) {
            graphics2D.drawImage(image,
                    resizeCoords.getTargetStartPosX(), resizeCoords.getTargetStartPosY(),
                    resizeCoords.getTargetStartPosX() + resizeCoords.getTargetWidth(), resizeCoords.getTargetStartPosY() + resizeCoords.getTargetHeight(),
                    resizeCoords.getSourceStartPosX(), resizeCoords.getSourceStartPosY(),
                    resizeCoords.getSourceStartPosX() + resizeCoords.getSourceWidth(), resizeCoords.getSourceStartPosY() + resizeCoords.getSourceHeight(),
                    null);
            graphics2D.dispose();
            // Save destination image
            return dest;
        }

        // Temporary image used for in-place resizing of image.
        BufferedImage tempImage = new BufferedImage(
                currentWidth,
                currentHeight,
                dest.getType()
        );

        Graphics2D g = getGraphics2D(tempImage, OperationType.RESIZE);
        g.setComposite(AlphaComposite.Src);

        /*
         * Determine the size of the first resize step should be.
         * 1) Beginning from the target size
         * 2) Increase each dimension by 2
         * 3) Until reaching the original size
         */
        int startWidth = resizeCoords.getTargetWidth();
        int startHeight = resizeCoords.getTargetHeight();

        while (startWidth < currentWidth && startHeight < currentHeight) {
            startWidth *= 2;
            startHeight *= 2;
        }

        currentWidth = startWidth / 2;
        currentHeight = startHeight / 2;

        // Perform first resize step.
        g.drawImage(image, 0, 0, currentWidth, currentHeight,
                resizeCoords.getSourceStartPosX(), resizeCoords.getSourceStartPosY(),
                resizeCoords.getSourceStartPosX() + resizeCoords.getSourceWidth(), resizeCoords.getSourceStartPosY() + resizeCoords.getSourceHeight(),
                null);

        // Perform an in-place progressive bilinear resize.
        while ((currentWidth >= targetWidth * 2) && (currentHeight >= targetHeight * 2)) {
            currentWidth /= 2;
            currentHeight /= 2;

            if (currentWidth < targetWidth) {
                currentWidth = targetWidth;
            }
            if (currentHeight < targetHeight) {
                currentHeight = targetHeight;
            }

            g.drawImage(
                    tempImage,
                    0, 0, currentWidth, currentHeight,
                    0, 0, currentWidth * 2, currentHeight * 2,
                    null
            );
        }

        g.dispose();

        // Draw the resized image onto the destination image.
        graphics2D.drawImage(tempImage,
                resizeCoords.getTargetStartPosX(),
                resizeCoords.getTargetStartPosY(), resizeCoords.getTargetStartPosX() + targetWidth, resizeCoords.getTargetStartPosY() + targetHeight, 0, 0, currentWidth, currentHeight, null);
        graphics2D.dispose();

        return dest;
    }

    protected Graphics2D getGraphics2D(BufferedImage dest, OperationType operationType) {
        // Paint source image into the destination, scaling as needed
        Graphics2D graphics2D = dest.createGraphics();
        if (dest.getColorModel() instanceof IndexColorModel) {
            if (OperationType.RESIZE.equals(operationType)) {
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            }
            graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
            IndexColorModel indexColorModel = (IndexColorModel) dest.getColorModel();
            int transparentPixelIndex = indexColorModel.getTransparentPixel();
            if (transparentPixelIndex > -1) {
                int transparentRGB = indexColorModel.getRGB(transparentPixelIndex);
                Color transparentColor = new Color(transparentRGB, true);
                graphics2D.setBackground(transparentColor);
                graphics2D.setColor(transparentColor);
                graphics2D.setPaint(transparentColor);
                graphics2D.fillRect(0, 0, dest.getWidth(), dest.getHeight());
            }
        } else {
            if (OperationType.RESIZE.equals(operationType)) {
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
            }
        }
        return graphics2D;
    }

}
