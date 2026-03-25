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

import ij.ImagePlus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.image.BufferImage;
import org.jahia.services.image.Image;
import org.jahia.services.image.ImageJImage;

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
import java.math.BigDecimal;
import java.util.Iterator;

/**
 * Combined ImageJ and Java2D image service implementation.
 * This is the default service that provides broad format support by combining Java2D and ImageJ capabilities.
 * <p>
 * Image loading strategy:
 * 1. If Java2D can read the format, use Java2D (progressive bilinear)
 * 2. Otherwise, try ImageJ for extended format support (e.g., TIFF)
 * <p>
 * This is an internal implementation that should NOT be registered as an OSGi service directly.
 * It is selected and used by ImageServiceDelegator based on configuration.
 */
public class ImageJAndJava2DImageServiceImpl extends AbstractImageService {

    private static final BigDecimal NINETY = BigDecimal.valueOf(90.);
    private static final BigDecimal MINUS_NINETY = BigDecimal.valueOf(-90.);
    private static final BigDecimal HUNDREDEIGHTY = BigDecimal.valueOf(180.);

    private boolean imageJAvailable = false;

    /**
     * Initialize the ImageJ and Java2D service.
     * Checks for ImageJ availability on the classpath.
     */
    ImageJAndJava2DImageServiceImpl() {
        checkImageJAvailability();
        logger.info("ImageJ and Java2D image service initialized - ImageJ available: {}", imageJAvailable);
    }

    private void checkImageJAvailability() {
        try {
            Class.forName("ij.ImagePlus");
            imageJAvailable = true;
        } catch (ClassNotFoundException e) {
            imageJAvailable = false;
            logger.debug("ImageJ not available on classpath");
        }
    }

    @Override
    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {

        if (canReadWithJava2D(node)) {
            Image img = getImageJava2D(node);
            if (img != null) {
                return img;
            }
        }

        return getImageJ(node);
    }

    private boolean canReadWithJava2D(JCRNodeWrapper node) throws RepositoryException {
            Node contentNode = node.getNode(Constants.JCR_CONTENT);
            String mimeType = contentNode.getProperty(Constants.JCR_MIMETYPE).getString();
            Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(mimeType);
            if (readers.hasNext()) {
                Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(mimeType);
                return writers.hasNext();
            }
            return false;
    }

    private Image getImageJava2D(JCRNodeWrapper node) throws IOException, RepositoryException {
        InputStream is = null;
        try {
            Node contentNode = node.getNode(Constants.JCR_CONTENT);
            is = contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream();
            String mimeType = contentNode.getProperty(Constants.JCR_MIMETYPE).getString();
            BufferedImage originalImage = ImageIO.read(is);

            if (originalImage == null) {
                logger.warn("Unable to load image for node {}", node.getPath());
                return null;
            }

            return new BufferImage(node.getPath(), originalImage, mimeType);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private Image getImageJ(JCRNodeWrapper node) throws IOException, RepositoryException {
        File tmp = null;
        OutputStream os = null;
        try {
            Class<?> openerClass = Class.forName("ij.io.Opener");

            String fileExtension = FilenameUtils.getExtension(node.getName());
            if ((fileExtension != null) && (!fileExtension.isEmpty())) {
                fileExtension = "." + fileExtension;
            } else {
                fileExtension = null;
            }

            tmp = File.createTempFile("image", fileExtension);
            Node contentNode = node.getNode(Constants.JCR_CONTENT);
            os = new BufferedOutputStream(new FileOutputStream(tmp));
            InputStream is = contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream();

            try {
                IOUtils.copy(is, os);
            } finally {
                IOUtils.closeQuietly(os);
                IOUtils.closeQuietly(is);
            }

            Object opener = openerClass.newInstance();
            int fileType = (Integer) openerClass.getMethod("getFileType", String.class).invoke(opener, tmp.getPath());
            Object imagePlus = openerClass.getMethod("openImage", String.class).invoke(opener, tmp.getPath());

            if (imagePlus == null) {
                logger.error("Couldn't open file {} for node {} with ImageJ!", tmp.getPath(), node.getPath());
                return null;
            }

            return new ImageJImage(node.getPath(), (ImagePlus) imagePlus, fileType);
        } catch (Exception e) {
            logger.error("ImageJ processing failed: {}", e.getMessage());
            throw new IOException("ImageJ processing failed", e);
        } finally {
            IOUtils.closeQuietly(os);
            FileUtils.deleteQuietly(tmp);
        }
    }

    @Override
    public int getHeight(Image i) throws IOException {
        if (i instanceof BufferedImage) {
            return ((BufferedImage) i).getHeight();
        } else if (i instanceof BufferImage) {
            return ((BufferImage) i).getOriginalImage().getHeight();
        } else if (i instanceof ImageJImage) {
            return ((ImageJImage) i).getImagePlus().getHeight();
        }
        throw new IOException("Unsupported image type: " + i.getClass().getName());
    }

    @Override
    public int getWidth(Image i) throws IOException {
        if (i instanceof BufferedImage) {
            return ((BufferedImage) i).getWidth();
        } else if (i instanceof BufferImage) {
            return ((BufferImage) i).getOriginalImage().getWidth();
        } else if (i instanceof ImageJImage) {
            return ((ImageJImage) i).getImagePlus().getWidth();
        }
        throw new IOException("Unsupported image type: " + i.getClass().getName());
    }

    @Override
    public boolean cropImage(Image i, File outputFile, int top, int left, int width, int height) throws IOException {
        if (i instanceof BufferImage) {
            return cropImageJava2D((BufferImage) i, outputFile, top, left, width, height);
        } else if (i instanceof ImageJImage) {
            return cropImageJ((ImageJImage) i, outputFile, top, left, width, height);
        }
        throw new IOException("Unsupported image type: " + i.getClass().getName());
    }

    private boolean cropImageJava2D(BufferImage image, File outputFile, int top, int left, int width, int height) throws IOException {
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

    private boolean cropImageJ(ImageJImage imageJImage, File outputFile, int top, int left, int width, int height) throws IOException {
        try {
            Object imagePlus = imageJImage.getImagePlus();
            Class<?> imagePlusClass = imagePlus.getClass();
            Class<?> processorClass = Class.forName("ij.process.ImageProcessor");

            Object processor = imagePlusClass.getMethod("getProcessor").invoke(imagePlus);
            processorClass.getMethod("setRoi", int.class, int.class, int.class, int.class).invoke(processor, left, top, width, height);
            processor = processorClass.getMethod("crop").invoke(processor);
            imagePlusClass.getMethod("setProcessor", String.class, processorClass).invoke(imagePlus, null, processor);

            return saveImageJ(imageJImage.getImageType(), imagePlus, outputFile);
        } catch (Exception e) {
            logger.error("ImageJ crop failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean rotateImage(Image i, File outputFile, double angle) throws IOException {
        if (i instanceof BufferImage) {
            return rotateImageJava2D((BufferImage) i, outputFile, angle);
        } else if (i instanceof ImageJImage) {
            return rotateImageJ((ImageJImage) i, outputFile, angle);
        }
        throw new IOException("Unsupported image type: " + i.getClass().getName());
    }

    private boolean rotateImageJava2D(BufferImage image, File outputFile, double degrees) throws IOException {
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

    private boolean rotateImageJ(ImageJImage imageJImage, File outputFile, double angle) throws IOException {
        try {
            Object imagePlus = imageJImage.getImagePlus();
            Class<?> imagePlusClass = imagePlus.getClass();
            Class<?> processorClass = Class.forName("ij.process.ImageProcessor");

            Object processor = imagePlusClass.getMethod("getProcessor").invoke(imagePlus);
            BigDecimal comparableAngle = BigDecimal.valueOf(angle);

            if (comparableAngle.compareTo(NINETY) == 0) {
                processor = processorClass.getMethod("rotateRight").invoke(processor);
            } else if (comparableAngle.compareTo(MINUS_NINETY) == 0) {
                processor = processorClass.getMethod("rotateLeft").invoke(processor);
            } else if (comparableAngle.compareTo(HUNDREDEIGHTY) == 0) {
                processor = processorClass.getMethod("rotateRight").invoke(processor);
                processor = processorClass.getMethod("rotateRight").invoke(processor);
            } else {
                processorClass.getMethod("rotate", double.class).invoke(processor, angle);
            }

            imagePlusClass.getMethod("setProcessor", String.class, processorClass).invoke(imagePlus, null, processor);
            return saveImageJ(imageJImage.getImageType(), imagePlus, outputFile);
        } catch (Exception e) {
            logger.error("ImageJ rotate failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean resizeImage(Image i, File outputFile, int width, int height, ResizeType resizeType) throws IOException {
        if (i instanceof BufferImage) {
            BufferedImage dest = resizeImage(((BufferImage) i).getOriginalImage(), width, height, resizeType);
            if (dest == null) {
                return false;
            }
            saveImageToFile(dest, ((BufferImage) i).getMimeType(), outputFile);
            return true;
        } else if (i instanceof ImageJImage) {
            return resizeImageJ((ImageJImage) i, outputFile, width, height, resizeType);
        }
        throw new IOException("Unsupported image type: " + i.getClass().getName());
    }

    private boolean resizeImageJ(ImageJImage imageJImage, File outputFile, int width, int height, ResizeType resizeType) throws IOException {
        try {
            Object imagePlus = imageJImage.getImagePlus();
            resizeImageJProcessor(imagePlus, width, height, resizeType);
            return saveImageJ(imageJImage.getImageType(), imagePlus, outputFile);
        } catch (Exception e) {
            logger.error("ImageJ resize failed: {}", e.getMessage());
            return false;
        }
    }

    private void resizeImageJProcessor(Object imagePlus, int width, int height, ResizeType resizeType) throws Exception {
        Class<?> imagePlusClass = imagePlus.getClass();
        Class<?> processorClass = Class.forName("ij.process.ImageProcessor");
        Class<?> blitterClass = Class.forName("ij.process.Blitter");

        Object processor = imagePlusClass.getMethod("getProcessor").invoke(imagePlus);
        int originalWidth = (Integer) imagePlusClass.getMethod("getWidth").invoke(imagePlus);
        int originalHeight = (Integer) imagePlusClass.getMethod("getHeight").invoke(imagePlus);

        ResizeCoords coords = getResizeCoords(resizeType, originalWidth, originalHeight, width, height);

        if (ResizeType.SCALE_TO_FILL.equals(resizeType)) {
            processorClass.getMethod("setInterpolationMethod", int.class).invoke(processor, 2); // BICUBIC
            processor = processorClass.getMethod("resize", int.class, int.class, boolean.class).invoke(processor, width, height, true);
        } else if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            width = coords.getTargetWidth();
            height = coords.getTargetHeight();
            processorClass.getMethod("setInterpolationMethod", int.class).invoke(processor, 2); // BICUBIC
            processor = processorClass.getMethod("resize", int.class, int.class, boolean.class).invoke(processor, width, height, true);
        } else if (ResizeType.ASPECT_FILL.equals(resizeType)) {
            processorClass.getMethod("setRoi", int.class, int.class, int.class, int.class).invoke(processor,
                    coords.getSourceStartPosX(), coords.getSourceStartPosY(), coords.getSourceWidth(), coords.getSourceHeight());
            processor = processorClass.getMethod("crop").invoke(processor);
            processorClass.getMethod("setInterpolationMethod", int.class).invoke(processor, 2); // BICUBIC
            processor = processorClass.getMethod("resize", int.class, int.class, boolean.class).invoke(processor,
                    coords.getTargetWidth(), coords.getTargetHeight(), true);
        } else if (ResizeType.ASPECT_FIT.equals(resizeType)) {
            processorClass.getMethod("setInterpolationMethod", int.class).invoke(processor, 2); // BICUBIC
            processor = processorClass.getMethod("resize", int.class, int.class, boolean.class).invoke(processor,
                    coords.getTargetWidth(), coords.getTargetHeight(), true);
            Object newProcessor = processorClass.getMethod("createProcessor", int.class, int.class).invoke(processor, width, height);
            int ADD_MODE = blitterClass.getField("ADD").getInt(null);
            processorClass.getMethod("copyBits", processorClass, int.class, int.class, int.class).invoke(newProcessor,
                    processor, coords.getSourceStartPosX(), coords.getTargetStartPosY(), ADD_MODE);
            processor = newProcessor;
        }

        imagePlusClass.getMethod("setProcessor", String.class, processorClass).invoke(imagePlus, null, processor);
    }

    private boolean saveImageJ(int type, Object imagePlus, File outputFile) {
        try {
            Class<?> fileSaverClass = Class.forName("ij.io.FileSaver");
            Class<?> openerClass = Class.forName("ij.io.Opener");
            Object fileSaver = fileSaverClass.getConstructor(Class.forName("ij.ImagePlus")).newInstance(imagePlus);
            String path = outputFile.getPath();

            int TIFF = openerClass.getField("TIFF").getInt(null);
            int GIF = openerClass.getField("GIF").getInt(null);
            int JPEG = openerClass.getField("JPEG").getInt(null);
            int PNG = openerClass.getField("PNG").getInt(null);
            int BMP = openerClass.getField("BMP").getInt(null);
            int PGM = openerClass.getField("PGM").getInt(null);
            int ZIP = openerClass.getField("ZIP").getInt(null);

            if (type == TIFF) {
                return (Boolean) fileSaverClass.getMethod("saveAsTiff", String.class).invoke(fileSaver, path);
            } else if (type == GIF) {
                return (Boolean) fileSaverClass.getMethod("saveAsGif", String.class).invoke(fileSaver, path);
            } else if (type == JPEG) {
                return (Boolean) fileSaverClass.getMethod("saveAsJpeg", String.class).invoke(fileSaver, path);
            } else if (type == PNG) {
                Class<?> windowManagerClass = Class.forName("ij.WindowManager");
                Object tempImage = windowManagerClass.getMethod("getTempCurrentImage").invoke(null);
                windowManagerClass.getMethod("setTempCurrentImage", Class.forName("ij.ImagePlus")).invoke(null, imagePlus);
                try {
                    Class<?> pluginClass = Class.forName("ij.plugin.PNG_Writer");
                    Object plugin = pluginClass.newInstance();
                    pluginClass.getMethod("run", String.class).invoke(plugin, path);
                } catch (Exception e) {
                    logger.error("PNG Writer error: {}", e.getMessage());
                }
                windowManagerClass.getMethod("setTempCurrentImage", Class.forName("ij.ImagePlus")).invoke(null, tempImage);
                return true;
            } else if (type == BMP) {
                return (Boolean) fileSaverClass.getMethod("saveAsBmp", String.class).invoke(fileSaver, path);
            } else if (type == PGM) {
                return (Boolean) fileSaverClass.getMethod("saveAsPgm", String.class).invoke(fileSaver, path);
            } else if (type == ZIP) {
                return (Boolean) fileSaverClass.getMethod("saveAsZip", String.class).invoke(fileSaver, path);
            }
            return false;
        } catch (Exception e) {
            logger.error("Error saving ImageJ image: {}", e.getMessage());
            return false;
        }
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
