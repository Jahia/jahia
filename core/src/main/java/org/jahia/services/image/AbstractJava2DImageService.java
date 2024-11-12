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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Abstract Java2D common image operation implementations
 */
public abstract class AbstractJava2DImageService extends AbstractImageService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractJava2DImageService.class);

    public enum OperationType {
        RESIZE, CROP, ROTATE
    }

    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
        InputStream is = null;
        try {
            String fileExtension = FilenameUtils.getExtension(node.getName());
            if ((fileExtension != null) && (!"".equals(fileExtension))) {
                fileExtension += "." + fileExtension;
            } else {
                fileExtension = null;
            }
            Node contentNode = node.getNode(Constants.JCR_CONTENT);
            is = contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream();
            String mimeType = contentNode.getProperty(Constants.JCR_MIMETYPE).getString();
            BufferedImage originalImage = ImageIO.read(is);
            if (originalImage == null) {
                logger.warn("Unable to load image for node {}", node.getPath());
                return null;
            }
            return new BufferImage(node.getPath(), originalImage, mimeType);
        } catch (Exception e) {
            logger.error("Error opening image for node {}. Cause: {}", node.getPath(), e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error opening image for node " + node.getPath(), e);
            }
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public int getHeight(Image i) {
        BufferImage bufferImage = ((BufferImage)i);
        return bufferImage.getOriginalImage().getHeight();
    }

    public int getWidth(Image i) {
        BufferImage bufferImage = ((BufferImage)i);
        return bufferImage.getOriginalImage().getWidth();
    }

    public boolean cropImage(Image image, File outputFile, int top, int left, int width, int height) throws IOException {
        BufferedImage originalImage = ((BufferImage) image).getOriginalImage();

        int clippingWidth = width;
        if (left + clippingWidth > originalImage.getWidth()) {
            clippingWidth = originalImage.getWidth() - left;
        }
        int clippingHeight = height;
        if (top + clippingHeight > originalImage.getHeight()) {
            clippingHeight = originalImage.getHeight() - top;
        }
        BufferedImage clipping = getDestImage(clippingWidth, clippingHeight, originalImage);
        Graphics2D area = getGraphics2D(clipping, OperationType.CROP);
        area.drawImage(originalImage, 0, 0, clippingWidth, clippingHeight, left, top, left + clippingWidth,
                top + clippingHeight, null);
        area.dispose();

        // Save destination image
        saveImageToFile(clipping, ((BufferImage)image).getMimeType(), outputFile);

        return true;
    }

    public boolean rotateImage(Image image, File outputFile, boolean clockwise) throws IOException {
        return rotateImage(image, outputFile, clockwise ? 90. : -90);
    }

    public boolean rotateImage(Image image, File outputFile, double degrees) throws IOException {
        BufferedImage originalImage = ((BufferImage) image).getOriginalImage();

        double angle = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int w = originalImage.getWidth(), h = originalImage.getHeight();
        int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);

        BufferedImage dest = getDestImage(neww, newh, originalImage);
        // Paint source image into the destination, scaling as needed
        Graphics2D graphics2D = getGraphics2D(dest, OperationType.ROTATE);

        graphics2D.translate((neww - w) / 2, (newh - h) / 2);
        graphics2D.rotate(angle, w / (double)2, h / (double)2);
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
        if (originalImage.getColorModel() instanceof IndexColorModel) {
            graphics2D.drawImage(originalImage, 0, 0, graphics2D.getBackground(), null);
        } else {
            graphics2D.drawImage(originalImage, 0, 0, null);
        }

        // Save destination image
        saveImageToFile(dest, ((BufferImage)image).getMimeType(), outputFile);
        return true;
    }

    public boolean resizeImage(Image image, File outputFile, int newWidth, int newHeight, ResizeType resizeType) throws IOException {

        BufferedImage originalImage = ((BufferImage) image).getOriginalImage();

        BufferedImage dest = resizeImage(originalImage, newWidth, newHeight, resizeType);

        // Save destination image
        saveImageToFile(dest, ((BufferImage) image).getMimeType(), outputFile);

        return true;
    }

    public BufferedImage resizeImage(BufferedImage image, int width, int newHeight, ResizeType resizeType) {
        ResizeCoords resizeCoords = getResizeCoords(resizeType, image.getWidth(), image.getHeight(), width, newHeight);
        if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            width = resizeCoords.getTargetWidth();
            newHeight = resizeCoords.getTargetHeight();
        }

        BufferedImage dest = getDestImage(width, newHeight, image);

        // Paint source image into the destination, scaling as needed
        Graphics2D graphics2D = getGraphics2D(dest, OperationType.RESIZE);

        graphics2D.drawImage(image,
                resizeCoords.getTargetStartPosX(), resizeCoords.getTargetStartPosY(),
                resizeCoords.getTargetStartPosX() + resizeCoords.getTargetWidth(), resizeCoords.getTargetStartPosY() + resizeCoords.getTargetHeight(),
                resizeCoords.getSourceStartPosX(), resizeCoords.getSourceStartPosY(),
                resizeCoords.getSourceStartPosX() + resizeCoords.getSourceWidth(), resizeCoords.getSourceStartPosY() + resizeCoords.getSourceHeight(),
                null);
        graphics2D.dispose();
        return dest;
    }

    protected abstract Graphics2D getGraphics2D(BufferedImage bufferedImage, OperationType operationType);

    protected boolean canRead(JCRNodeWrapper node) throws RepositoryException {
        Node contentNode = node.getNode(Constants.JCR_CONTENT);
        String mimeType = contentNode.getProperty(Constants.JCR_MIMETYPE).getString();

        Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReadersByMIMEType(mimeType);
        if (imageReaderIterator.hasNext()) {
            // now let's check if we can also write it.
            Iterator<ImageWriter> imageWriterIterator = ImageIO.getImageWritersByMIMEType(mimeType);
            if (imageWriterIterator.hasNext()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    protected BufferedImage getDestImage(int newWidth, int newHeight, BufferedImage originalImage) {
        BufferedImage dest;
        if (originalImage.getColorModel() instanceof IndexColorModel) {
            // dest = new BufferedImage(newWidth, newHeight, originalImage.getType(), (IndexColorModel) originalImage.getColorModel());
            dest = new BufferedImage(originalImage.getColorModel(), originalImage.getColorModel().createCompatibleWritableRaster(newWidth, newHeight), false, new Hashtable<Object, Object>());
        } else {
            dest = new BufferedImage(newWidth, newHeight, originalImage.getType());
        }
        return dest;
    }

    protected void saveImageToFile(BufferedImage dest, String mimeType, File destFile) throws IOException {
        Iterator<ImageWriter> suffixWriters = ImageIO.getImageWritersByMIMEType(mimeType);
        if (suffixWriters.hasNext()) {
            ImageWriter imageWriter = suffixWriters.next();
            ImageOutputStream imageOutputStream = new FileImageOutputStream(destFile);
            imageWriter.setOutput(imageOutputStream);
            imageWriter.write(dest);
            imageOutputStream.close();
        } else {
            logger.warn("Couldn't find a writer for mime type : " + mimeType + "(" + this.getClass().getName() + ")");
        }
    }

}
