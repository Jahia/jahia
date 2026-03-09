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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.ImageCommand;
import org.im4java.process.ArrayListOutputConsumer;
import org.im4java.process.ProcessStarter;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.image.Image;
import org.jahia.services.image.ImageMagickImage;
import org.jahia.settings.SettingsBean;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

/**
 * ImageMagick-based image service implementation.
 * This service uses the external ImageMagick command-line tool for high-quality image processing.
 * <p>
 * This is an internal implementation that should NOT be registered as an OSGi service directly.
 * It is selected and used by ImageServiceDelegator based on configuration.
 */
public class ImageMagickImageServiceImpl extends AbstractImageService {

    private static Integer imageMagickMajorVersion;

    /**
     * Initialize the ImageMagick service with the given configuration.
     *
     */
    ImageMagickImageServiceImpl() {
        String pathValue = SettingsBean.getInstance().getPropertyValue("imageMagickPath");

        if (pathValue == null || pathValue.trim().isEmpty()) {
            throw new RuntimeException("ImageMagick path not configured. Set imageMagickPath property.");
        }

        try {
            ProcessStarter.setGlobalSearchPath(pathValue);
            imageMagickMajorVersion = getImageMagickMajorVersion();
            logger.info("ImageMagick image service initialized with path: {}", pathValue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ImageMagick with path " + pathValue + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {

        Node contentNode = node.getNode(Constants.JCR_CONTENT);
        String fileExtension = FilenameUtils.getExtension(node.getName());
        File tmp = File.createTempFile("image", StringUtils.isNotEmpty(fileExtension) ? "." + fileExtension : null);

        InputStream is = contentNode.getProperty(Constants.JCR_DATA).getStream();
        OutputStream os = new BufferedOutputStream(new FileOutputStream(tmp));
        try {
            IOUtils.copy(is, os);
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }

        return new ImageMagickImage(tmp, node.getPath());
    }

    @Override
    public int getHeight(Image i) throws IOException {
        if (!(i instanceof ImageMagickImage)) {
            throw new IOException("Unsupported image type for ImageMagick service: " + i.getClass().getName());
        }

        ImageMagickImage img = (ImageMagickImage) i;
        if (img.getHeight() != null) {
            return img.getHeight();
        }

        readDimensions(img);
        return img.getHeight();
    }

    @Override
    public int getWidth(Image i) throws IOException {
        if (!(i instanceof ImageMagickImage)) {
            throw new IOException("Unsupported image type for ImageMagick service: " + i.getClass().getName());
        }

        ImageMagickImage img = (ImageMagickImage) i;
        if (img.getWidth() != null) {
            return img.getWidth();
        }

        readDimensions(img);
        return img.getWidth();
    }

    private void readDimensions(ImageMagickImage img) throws IOException {
        try {
            IMOperation op = new IMOperation();
            op.format("%w\n%h");
            op.addImage(img.getFile().getPath());

            ImageCommand identify = new ImageCommand();
            // magick command only exist and is required for version 7 and plus.
            if (imageMagickMajorVersion > 6) {
                identify.setCommand("magick", "identify");
            }

            ArrayListOutputConsumer output = new ArrayListOutputConsumer();
            identify.setOutputConsumer(output);
            identify.run(op);

            List<String> cmdOutput = output.getOutput();
            img.setWidth(Integer.parseInt(cmdOutput.get(0)));
            img.setHeight(Integer.parseInt(cmdOutput.get(1)));
        } catch (Exception e) {
            logger.error("Error retrieving image dimensions for {}: {}", img.getPath(), e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.error("Error retrieving image dimensions", e);
            }
            img.setWidth(-1);
            img.setHeight(-1);
        }
    }

    @Override
    public boolean cropImage(Image i, File outputFile, int top, int left, int width, int height) throws IOException {
        if (!(i instanceof ImageMagickImage)) {
            throw new IOException("Unsupported image type for ImageMagick service: " + i.getClass().getName());
        }

        ImageMagickImage img = (ImageMagickImage) i;
        try {
            ConvertCmd cmd = new ConvertCmd();
            IMOperation op = new IMOperation();
            op.addImage(img.getFile().getPath());
            op.background("none");
            op.crop(width, height, left, top);
            op.p_repage();
            op.addImage(outputFile.getPath());
            cmd.run(op);
        } catch (Exception e) {
            logger.error("Error cropping image {} to size {}x{}: {}", img.getPath(), width, height, e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error cropping image", e);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean rotateImage(Image i, File outputFile, double angle) throws IOException {
        if (!(i instanceof ImageMagickImage)) {
            throw new IOException("Unsupported image type for ImageMagick service: " + i.getClass().getName());
        }

        ImageMagickImage img = (ImageMagickImage) i;
        try {
            ConvertCmd cmd = new ConvertCmd();
            IMOperation op = new IMOperation();
            op.addImage(img.getFile().getPath());
            op.rotate(angle);
            op.addImage(outputFile.getPath());
            cmd.run(op);
        } catch (Exception e) {
            logger.error("Error rotating image {}: {}", img.getPath(), e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error rotating image", e);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean resizeImage(Image i, File outputFile, int width, int height, ResizeType resizeType) throws IOException {
        if (!(i instanceof ImageMagickImage)) {
            throw new IOException("Unsupported image type for ImageMagick service: " + i.getClass().getName());
        }

        ImageMagickImage img = (ImageMagickImage) i;
        try {
            ConvertCmd cmd = new ConvertCmd();
            IMOperation op = new IMOperation();
            op.addImage(img.getFile().getPath());
            setupIMResize(op, width, height, resizeType);
            op.addImage(outputFile.getPath());
            cmd.run(op);
        } catch (Exception e) {
            logger.error("Error resizing image {}: {}", img.getFile(), e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error resizing image", e);
            }
            return false;
        }
        return true;
    }

    private void setupIMResize(IMOperation op, int width, int height, ResizeType resizeType) {
        if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            op.resize(width, height);
        } else if (ResizeType.ASPECT_FILL.equals(resizeType)) {
            op.resize(width, height, "^");
            op.gravity("center");
            op.crop(width, height, 0, 0);
            op.p_repage();
        } else if (ResizeType.ASPECT_FIT.equals(resizeType)) {
            op.resize(width, height);
            op.gravity("center");
            op.background("none");
            op.extent(width, height);
        } else {
            op.resize(width, height, "!");
        }
    }

    @Override
    public BufferedImage resizeImage(BufferedImage image, int width, int height, ResizeType resizeType) throws IOException {
        throw new UnsupportedOperationException("ImageMagick service does not support BufferedImage operations. Use Java2D service instead.");
    }

    /**
     * Resolve magick version by executing a command and read the version from the return
     *
     * @return the magick version
     * @throws Exception if an error occur
     */
    private int getImageMagickMajorVersion() throws Exception {
        if (imageMagickMajorVersion == null) {
            ImageCommand cmd = new ImageCommand();
            cmd.setCommand("convert");

            ArrayListOutputConsumer output = new ArrayListOutputConsumer();
            cmd.setOutputConsumer(output);

            IMOperation op = new IMOperation();
            op.version();

            cmd.run(op);

            List<String> cmdOutput = output.getOutput();
            if (!cmdOutput.isEmpty()) {
                String line = cmdOutput.get(0);
                // Look for version in the first line
                if (line != null && line.contains("ImageMagick")) {
                    String[] parts = line.split("\\s+");
                    for (String part : parts) {
                        if (part.matches("\\d+\\..*")) {
                            imageMagickMajorVersion = Integer.parseInt(part.split("\\.")[0]);
                            return imageMagickMajorVersion;
                        }
                    }
                }
            }
            logger.warn("Cannot get version of image magick from \"convert\" command, fallback to image magick version 6");
            imageMagickMajorVersion = 6; // default fallback
        }
        return imageMagickMajorVersion;
    }
}
