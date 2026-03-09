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

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.image.BufferImage;
import org.jahia.services.image.Image;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.jcr.RepositoryException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Java2D-based image service implementation.
 * This service uses native Java2D APIs with progressive bilinear interpolation for image processing.
 * <p>
 * This is an internal implementation that should NOT be registered as an OSGi service directly.
 * It is selected and used by ImageServiceDelegator based on configuration.
 */
public class Java2DImageServiceImpl extends AbstractImageService {
    /**
     * Initialize the Java2D service.
     */
    Java2DImageServiceImpl() {
        logger.info("Java2D image service initialized");
    }

    @Override
    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
        JCRNodeWrapper contentNode = node.getNode(Constants.JCR_CONTENT);
        try (InputStream is = contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream()) {
            String mimeType = contentNode.getProperty(Constants.JCR_MIMETYPE).getString();
            BufferedImage originalImage = ImageIO.read(is);

            if (originalImage == null) {
                throw new IOException("Unable to load image for node " + node.getPath());
            }

            return new BufferImage(node.getPath(), originalImage, mimeType);
        }
    }

    @Override
    public int getHeight(Image i) throws IOException {
        if (i instanceof BufferedImage) {
            return ((BufferedImage) i).getHeight();
        }
        if (i instanceof BufferImage) {
            return ((BufferImage) i).getOriginalImage().getHeight();
        }
        throw new IOException("Unsupported image type for Java2D service: " + i.getClass().getName());
    }

    @Override
    public int getWidth(Image i) throws IOException {
        if (i instanceof BufferedImage) {
            return ((BufferedImage) i).getWidth();
        }
        if (i instanceof BufferImage) {
            return ((BufferImage) i).getOriginalImage().getWidth();
        }
        throw new IOException("Unsupported image type for Java2D service: " + i.getClass().getName());
    }

    @Override
    public boolean cropImage(Image i, File outputFile, int top, int left, int width, int height) throws IOException {
        if (!(i instanceof BufferImage)) {
            throw new IOException("Unsupported image type for Java2D service: " + i.getClass().getName());
        }

        BufferImage image = (BufferImage) i;
        BufferedImage originalImage = image.getOriginalImage();

        int clippingWidth = Math.min(width, originalImage.getWidth() - left);
        int clippingHeight = Math.min(height, originalImage.getHeight() - top);

        BufferedImage clipping = createCompatibleImage(clippingWidth, clippingHeight, originalImage);
        Graphics2D area = clipping.createGraphics();
        configureGraphics(area, originalImage, OperationType.CROP);
        area.drawImage(originalImage, 0, 0, clippingWidth, clippingHeight, left, top, left + clippingWidth, top + clippingHeight, null);
        area.dispose();

        saveImageToFile(clipping, image.getMimeType(), outputFile);
        return true;
    }

    @Override
    public boolean rotateImage(Image i, File outputFile, double degrees) throws IOException {
        if (!(i instanceof BufferImage)) {
            throw new IOException("Unsupported image type for Java2D service: " + i.getClass().getName());
        }

        BufferImage image = (BufferImage) i;
        BufferedImage originalImage = image.getOriginalImage();

        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians)), cos = Math.abs(Math.cos(radians));
        int w = originalImage.getWidth(), h = originalImage.getHeight();
        int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);

        BufferedImage dest = createCompatibleImage(neww, newh, originalImage);
        Graphics2D graphics2D = dest.createGraphics();
        configureGraphics(graphics2D, originalImage, OperationType.ROTATE);

        graphics2D.translate((neww - w) / 2, (newh - h) / 2);
        graphics2D.rotate(radians, w / 2.0, h / 2.0);
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));

        if (originalImage.getColorModel() instanceof IndexColorModel) {
            graphics2D.drawImage(originalImage, 0, 0, graphics2D.getBackground(), null);
        } else {
            graphics2D.drawImage(originalImage, 0, 0, null);
        }
        graphics2D.dispose();

        saveImageToFile(dest, image.getMimeType(), outputFile);
        return true;
    }

    @Override
    public boolean resizeImage(Image i, File outputFile, int width, int height, ResizeType resizeType) throws IOException {
        if (!(i instanceof BufferImage)) {
            throw new IOException("Unsupported image type for Java2D service: " + i.getClass().getName());
        }

        BufferImage image = (BufferImage) i;
        BufferedImage dest = resizeImage(image.getOriginalImage(), width, height, resizeType);

        if (dest == null) {
            return false;
        }

        saveImageToFile(dest, image.getMimeType(), outputFile);
        return true;
    }

    @Override
    public BufferedImage resizeImage(BufferedImage image, int width, int height, ResizeType resizeType) throws IOException {
        ResizeCoords coords = getResizeCoords(resizeType, image.getWidth(), image.getHeight(), width, height);

        if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            width = coords.getTargetWidth();
            height = coords.getTargetHeight();
        }

        BufferedImage dest = createCompatibleImage(width, height, image);
        Graphics2D graphics2D = dest.createGraphics();
        configureGraphics(graphics2D, image, OperationType.RESIZE);

        graphics2D.drawImage(image,
                coords.getTargetStartPosX(), coords.getTargetStartPosY(),
                coords.getTargetStartPosX() + coords.getTargetWidth(), coords.getTargetStartPosY() + coords.getTargetHeight(),
                coords.getSourceStartPosX(), coords.getSourceStartPosY(),
                coords.getSourceStartPosX() + coords.getSourceWidth(), coords.getSourceStartPosY() + coords.getSourceHeight(),
                null);
        graphics2D.dispose();

        return dest;
    }

    private void saveImageToFile(BufferedImage dest, String mimeType, File destFile) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(mimeType);
        if (writers.hasNext()) {
            ImageWriter imageWriter = writers.next();
            try (ImageOutputStream imageOutputStream = new FileImageOutputStream(destFile)) {
                imageWriter.setOutput(imageOutputStream);
                imageWriter.write(dest);
            }
        } else {
            logger.warn("Couldn't find a writer for mime type: {}", mimeType);
            throw new IOException("No writer available for mime type: " + mimeType);
        }
    }
}
