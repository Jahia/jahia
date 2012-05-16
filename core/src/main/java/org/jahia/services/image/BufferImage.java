package org.jahia.services.image;

import java.awt.image.BufferedImage;

/**
 * Simple image type to represent a buffered image
 */
public class BufferImage implements Image {
    private String path;
    private BufferedImage originalImage;
    private String mimeType;

    public BufferImage(String path, BufferedImage originalImage, String mimeType) {
        this.path = path;
        this.originalImage = originalImage;
        this.mimeType = mimeType;
    }

    public String getPath() {
        return path;
    }

    public BufferedImage getOriginalImage() {
        return originalImage;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void dispose() {
        if (originalImage != null) {
            originalImage.flush();
            originalImage = null;
        }
    }
}
