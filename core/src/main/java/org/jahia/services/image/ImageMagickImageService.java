/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
import org.apache.commons.lang.StringUtils;
import org.im4java.core.*;
import org.im4java.process.ArrayListOutputConsumer;
import org.im4java.process.ProcessStarter;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * An image service implementation that uses the external ImageMagick command line tool to
 * perform high-quality image manipulation for a wide variety of image formats.
 */
public class ImageMagickImageService extends AbstractImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageMagickImageService.class);

    private static ImageMagickImageService instance = new ImageMagickImageService();

    private String imageMagickPath;

    protected ImageMagickImageService() {
        super();
    }

    public void init() {
        ProcessStarter.setGlobalSearchPath(imageMagickPath);
    }

    public static ImageMagickImageService getInstance() {
        return instance;
    }

    public void setImageMagickPath(String imageMagickPath) {
        this.imageMagickPath = imageMagickPath;
    }

    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
        Node contentNode = node.getNode(Constants.JCR_CONTENT);
        String fileExtension = FilenameUtils.getExtension(node.getName());
        File tmp = File.createTempFile("image", StringUtils.isNotEmpty(fileExtension) ? "."
                + fileExtension : null);
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

    protected void readDimensions(ImageMagickImage img) {
        long timer = System.currentTimeMillis();
        try {
            IMOperation op = new IMOperation();
            op.format("%w\n%h");
            op.addImage(img.getFile().getPath());

            // execute ...
            //because of incompatibility between imagemagick and im4java we can't use the IdentifyCmd class directly
            ImageCommand identify = new ImageCommand();
            identify.setCommand("magick", "identify");

            ArrayListOutputConsumer output = new ArrayListOutputConsumer();
            identify.setOutputConsumer(output);
            identify.run(op);

            // ... and parse result
            List<String> cmdOutput = output.getOutput();
            img.setWidth(Integer.parseInt(cmdOutput.get(0)));
            img.setHeight(Integer.parseInt(cmdOutput.get(1)));
        } catch (Exception e) {
            logger.error("Error retrieving image dimensions for " + img.getPath() + ": " + e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.error("Error retrieving image dimensions for " + img.getPath(), e);
            }
            img.setWidth(-1);
            img.setHeight(-1);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Dimensions read for image {} in {} ms: {} x {}", new Object[] {
                    img.getFile().getPath(), System.currentTimeMillis() - timer, img.getWidth(),
                    img.getHeight() });
        }
    }

    public int getHeight(Image i) throws IOException {
        ImageMagickImage img = (ImageMagickImage) i;
        if (img.getHeight() != null) {
            return img.getHeight();
        }

        readDimensions(img);

        return img.getHeight();
    }

    public int getWidth(Image i) throws IOException {
        ImageMagickImage img = (ImageMagickImage) i;
        if (img.getWidth() != null) {
            return img.getWidth();
        }

        readDimensions(img);

        return img.getWidth();
    }

    public boolean cropImage(Image i, File outputFile, int top, int left, int width, int height) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(((ImageMagickImage)i).getFile().getPath());
            op.background("none");
            op.crop(width, height, left, top);
            op.p_repage();
            op.addImage(outputFile.getPath());

            // logger.info("Running ImageMagic command: convert " + op);
            cmd.run(op);
        } catch (Exception e) {
            logger.error("Error cropping image " + i.getPath() + " to size " + width + "x" + height + ": " + e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error cropping image " + i.getPath() + " to size " + width + "x" + height, e);
            }
            return false;
        }
        return true;
    }

    public boolean rotateImage(Image image, File outputFile, boolean clockwise) throws IOException {
        return rotateImage(image, outputFile, clockwise ? 90. : -90);
    }

    public boolean rotateImage(Image image, File outputFile, double angle) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(((ImageMagickImage)image).getFile().getPath());
            op.rotate(angle);
            op.addImage(outputFile.getPath());

            // logger.info("Running ImageMagic command: convert " + op);
            cmd.run(op);
        } catch (Exception e) {
            logger.error("Error rotating image " + image.getPath(), e);
            if (logger.isDebugEnabled()) {
                logger.debug("Error rotating image " + image.getPath(), e);
            }
            return false;
        }
        return true;
    }

    public boolean resizeImage(Image i, File outputFile, int width, int height, ResizeType resizeType) throws IOException {
        return resizeImage(getFile(i), outputFile, width, height, resizeType);
    }

    public BufferedImage resizeImage(BufferedImage image, int width, int height,
                                     ResizeType resizeType) throws IOException {
        try {
        IMOperation op = new IMOperation();
        op.addImage();                        // input

        setupIMResize(op, width, height, resizeType);

        op.addImage("png:-");                 // output: stdout

        // set up command
        ConvertCmd convert = new ConvertCmd();
        Stream2BufferedImage s2b = new Stream2BufferedImage();
        convert.setOutputConsumer(s2b);

        // run command and extract BufferedImage from OutputConsumer
        convert.run(op, image);
        BufferedImage img = s2b.getImage();
            return img;
        } catch (Exception e) {
            logger.error("Error resizing image : " + e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error resizing image ", e);
            }
            return null;
        }
    }

    protected boolean resizeImage(File inputFile, File outputFile, int width, int height, ResizeType resizeType) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(inputFile.getPath());

            setupIMResize(op, width, height, resizeType);

            op.addImage(outputFile.getPath());

            cmd.run(op);
        } catch (Exception e) {
            logger.error("Error resizing image " + inputFile + ": " + e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error resizing image " + inputFile, e);
            }
            return false;
        }
        return true;
    }

    private File getFile(Image i) {
        return ((ImageMagickImage) i).getFile();
    }

    private void setupIMResize(IMOperation op, int width, int height, ResizeType resizeType) {
        if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            op.resize(width,height);
        } else if (ResizeType.ASPECT_FILL.equals(resizeType)) {
            op.resize(width,height,"^");
            op.gravity("center");
            op.crop(width,height,0,0);
            op.p_repage();
        } else if (ResizeType.ASPECT_FIT.equals(resizeType)) {
            op.resize(width,height);
            op.gravity("center");
            op.background("none");
            op.extent(width,height);
        } else {
            op.resize(width,height,"!");
        }
    }

}
