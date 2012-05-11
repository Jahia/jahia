package org.jahia.services.image;

import org.apache.commons.io.FilenameUtils;
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
 * Java2D based image service
 */
public class Java2DImageService extends AbstractImageService {

    private static Java2DImageService instance = new Java2DImageService();

    private static final Logger logger = LoggerFactory.getLogger(Java2DImageService.class);

    protected Java2DImageService() {
        super();
    }

    public void init() {
    }

    public static Java2DImageService getInstance() {
        return instance;
    }

    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
        try {
            String fileExtension = FilenameUtils.getExtension(node.getName());
            if ((fileExtension != null) && (!"".equals(fileExtension))) {
                fileExtension += "." + fileExtension;
            } else {
                fileExtension = null;
            }
            Node contentNode = node.getNode(Constants.JCR_CONTENT);
            InputStream is = contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream();
            String mimeType = contentNode.getProperty(Constants.JCR_MIMETYPE).getString();
            BufferedImage originalImage = ImageIO.read(is);
            if (originalImage == null) {
                logger.warn("Unable to load image for node {}", node.getPath());
                return null;
            }
            return new ImageJImage(node.getPath(), null, 0, originalImage, mimeType, true);
        } catch (Exception e) {
            logger.error("Error opening image for node {}. Cause: {}", node.getPath(), e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error opening image for node " + node.getPath(), e);
            }
            return null;
        }
    }


    /**
     * Creates a JPEG thumbnail from inputFile and saves it to disk in
     * outputFile. scaleWidth is the width to scale the image to
     */
    public boolean createThumb(Image iw, File outputFile, int size, boolean square) throws IOException {
        if (((ImageJImage)iw).getOriginalImage() == null) {
            return false;
        }
        if (square) {
            return resizeImage(iw, outputFile, size, size, ResizeType.ASPECT_FILL);
        } else {
            return resizeImage(iw, outputFile, size, size, ResizeType.ADJUST_SIZE);
        }
    }

    public int getHeight(Image i) {
        ImageJImage imageJImage = ((ImageJImage)i);
        return imageJImage.getOriginalImage().getHeight();
    }

    public int getWidth(Image i) {
        ImageJImage imageJImage = ((ImageJImage)i);
        return imageJImage.getOriginalImage().getWidth();
    }

    public boolean cropImage(Image image, File outputFile, int top, int left, int width, int height) throws IOException {
        BufferedImage originalImage = ((ImageJImage) image).getOriginalImage();

        int clippingWidth = width;
        if (left + clippingWidth > originalImage.getWidth()) {
            clippingWidth = originalImage.getWidth() - left;
        }
        int clippingHeight = height;
        if (top + clippingHeight > originalImage.getHeight()) {
            clippingHeight = originalImage.getHeight() - top;
        }
        BufferedImage clipping = getDestImage(clippingWidth, clippingHeight, originalImage);
        Graphics2D area = getGraphics2D(clipping, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        area.drawImage(originalImage, 0, 0, clippingWidth, clippingHeight, left, top, left + clippingWidth,
                top + clippingHeight, null);
        area.dispose();

        // Save destination image
        saveImageToFile(clipping, ((ImageJImage) image).getMimeType(), outputFile);

        return true;
    }

    public boolean resizeImage(Image i, File outputFile, int width, int height) throws IOException {
        return resizeImage(i, outputFile, width, height, ResizeType.ADJUST_SIZE);
    }

    public boolean rotateImage(Image image, File outputFile, boolean clockwise) throws IOException {
        BufferedImage originalImage = ((ImageJImage) image).getOriginalImage();

        BufferedImage dest = getDestImage(originalImage.getHeight(), originalImage.getWidth(), originalImage);
        // Paint source image into the destination, scaling as needed
        Graphics2D graphics2D = getGraphics2D(dest, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        double angle = Math.toRadians(clockwise ? 90 : -90);
        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int w = originalImage.getWidth(), h = originalImage.getHeight();
        int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);
        graphics2D.translate((neww - w) / 2, (newh - h) / 2);
        graphics2D.rotate(angle, w / 2, h / 2);
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
        if (originalImage.getColorModel() instanceof IndexColorModel) {
            graphics2D.drawImage(originalImage, 0, 0, graphics2D.getBackground(), null);
        } else {
            graphics2D.drawImage(originalImage, 0, 0, null);
        }

        // Save destination image
        saveImageToFile(dest, ((ImageJImage) image).getMimeType(), outputFile);
        return true;
    }

    public boolean resizeImage(Image image, File outputFile, int newWidth, int newHeight, ResizeType resizeType) throws IOException {
        BufferedImage dest = resizeImage(((ImageJImage) image).getOriginalImage(), newWidth, newHeight, resizeType);
        if (dest != null) {
            // Save destination image
            saveImageToFile(dest, ((ImageJImage) image).getMimeType(), outputFile);
            return true;
        } else {
            return false;
        }
    }

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

    protected Graphics2D getGraphics2D(BufferedImage dest, Object interpolationValue) {
        // Paint source image into the destination, scaling as needed
        Graphics2D graphics2D = dest.createGraphics();
        if (dest.getColorModel() instanceof IndexColorModel) {
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
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
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationValue);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
        }
        return graphics2D;
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

    public BufferedImage resizeImage(BufferedImage originalImage, int newWidth, int newHeight,
            ResizeType resizeType) throws IOException {
        ResizeCoords resizeCoords = getResizeCoords(resizeType, originalImage.getWidth(), originalImage.getHeight(), newWidth, newHeight);
        if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            newWidth = resizeCoords.getTargetWidth();
            newHeight = resizeCoords.getTargetHeight();
        }

        int currentWidth = resizeCoords.getSourceWidth();
        int currentHeight = resizeCoords.getSourceHeight();
        int targetWidth = resizeCoords.getTargetWidth();
        int targetHeight = resizeCoords.getTargetHeight();

        BufferedImage dest = getDestImage(newWidth, newHeight, originalImage);

        // Paint source image into the destination, scaling as needed
        Graphics2D graphics2D = getGraphics2D(dest, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // If multi-step downscaling is not required, perform one-step.
        if ((newWidth * 2 >= resizeCoords.getSourceWidth()) && (newHeight * 2 >= resizeCoords.getSourceHeight())) {
            graphics2D.drawImage(originalImage,
                    resizeCoords.getTargetStartPosX(), resizeCoords.getTargetStartPosY(),
                    resizeCoords.getTargetStartPosX() + resizeCoords.getTargetWidth(), resizeCoords.getTargetStartPosY() + resizeCoords.getTargetHeight(),
                    resizeCoords.getSourceStartPosX(), resizeCoords.getSourceStartPosY(),
                    resizeCoords.getSourceStartPosX() + resizeCoords.getSourceWidth(), resizeCoords.getSourceStartPosY() + resizeCoords.getSourceHeight(),
                    null);
            graphics2D.dispose();
            return dest;
        }

        // Temporary image used for in-place resizing of image.
        BufferedImage tempImage = new BufferedImage(
                currentWidth,
                currentHeight,
                dest.getType()
        );
        
        Graphics2D g = getGraphics2D(tempImage, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
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
        g.drawImage(originalImage, 0, 0, currentWidth, currentHeight,
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
        graphics2D.drawImage(tempImage, resizeCoords.getTargetStartPosX(), resizeCoords.getTargetStartPosY(), targetWidth, targetHeight, 0, 0, currentWidth, currentHeight, null);
        graphics2D.dispose();
        return dest;
    }

}
