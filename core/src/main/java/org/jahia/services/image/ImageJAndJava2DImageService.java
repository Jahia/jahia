package org.jahia.services.image;

import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;

/**
 * An image service implementation that combines the Java2D and ImageJ service implementations
 * to expand the supported file formats (such as TIFF)
 */
public class ImageJAndJava2DImageService extends Java2DImageService {

    private ImageJImageService imageJImageService = new ImageJImageService();
    private static ImageJAndJava2DImageService instance = new ImageJAndJava2DImageService();

    private static final Logger logger = LoggerFactory.getLogger(ImageJAndJava2DImageService.class);

    protected ImageJAndJava2DImageService() {
    }

    public void init() {
    }

    public static synchronized ImageJAndJava2DImageService getInstance() {
        return instance;
    }

    @Override
    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
        if (super.canRead(node)) {
            return super.getImage(node);
        } else {
            return imageJImageService.getImage(node);
        }
    }

    @Override
    public boolean createThumb(Image iw, File outputFile, int size, boolean square) throws IOException {
        ImageJImage imageJImage = (ImageJImage) iw;
        if (imageJImage.isJava2DUsed()) {
            return super.createThumb(iw, outputFile, size, square);
        } else {
            return imageJImageService.createThumb(iw, outputFile, size, square);
        }
    }

    @Override
    public int getHeight(Image i) {
        ImageJImage imageJImage = (ImageJImage) i;
        if (imageJImage.isJava2DUsed()) {
            return super.getHeight(i);
        } else {
            return imageJImageService.getHeight(i);
        }
    }

    @Override
    public int getWidth(Image i) {
        ImageJImage imageJImage = (ImageJImage) i;
        if (imageJImage.isJava2DUsed()) {
            return super.getWidth(i);
        } else {
            return imageJImageService.getWidth(i);
        }
    }

    @Override
    public boolean cropImage(Image image, File outputFile, int top, int left, int width, int height) throws IOException {
        ImageJImage imageJImage = (ImageJImage) image;
        if (imageJImage.isJava2DUsed()) {
            return super.cropImage(image, outputFile, top, left, width, height);
        } else {
            return imageJImageService.cropImage(image, outputFile, top, left, width, height);
        }
    }

    @Override
    public boolean rotateImage(Image image, File outputFile, boolean clockwise) throws IOException {
        ImageJImage imageJImage = (ImageJImage) image;
        if (imageJImage.isJava2DUsed()) {
            return super.rotateImage(image, outputFile, clockwise);
        } else {
            return imageJImageService.rotateImage(image, outputFile, clockwise);
        }
    }

    @Override
    public boolean resizeImage(Image image, File outputFile, int newWidth, int newHeight, ResizeType resizeType) throws IOException {
        ImageJImage imageJImage = (ImageJImage) image;
        if (imageJImage.isJava2DUsed()) {
            return super.resizeImage(image, outputFile, newWidth, newHeight, resizeType);
        } else {
            return imageJImageService.resizeImage(image, outputFile, newWidth, newHeight, resizeType);
        }
    }
}
