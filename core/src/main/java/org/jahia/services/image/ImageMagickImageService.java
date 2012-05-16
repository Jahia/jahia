/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.image;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.im4java.core.*;
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
import java.util.regex.Pattern;

/**
 * An image service implementation that uses the external ImageMagick command line tool to
 * perform high-quality image manipulation for a wide variety of image formats.
 */
public class ImageMagickImageService extends AbstractImageService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageMagickImageService.class);
    
    private static final Pattern GEOMETRY_PATTERN = Pattern.compile("[x+]");

    private static ImageMagickImageService instance;

    private String imageMagickPath;

    protected ImageMagickImageService() {

    }

    public void init() {
        ProcessStarter.setGlobalSearchPath(imageMagickPath);
    }

    public static ImageMagickImageService getInstance() {
        if (instance == null) {
            synchronized (ImageMagickImageService.class) {
                instance = new ImageMagickImageService();
            }
        }
        return instance;
    }

    public void setImageMagickPath(String imageMagickPath) {
        this.imageMagickPath = imageMagickPath;
    }

    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
        Node contentNode = node.getNode(Constants.JCR_CONTENT);
        String fileExtension = FilenameUtils.getExtension(node.getName());
        if ((fileExtension != null) && (!"".equals(fileExtension))) {
            fileExtension += "." + fileExtension;
        } else {
            fileExtension = null;
        }
        File tmp = File.createTempFile("image", fileExtension);
        tmp.deleteOnExit();
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

    public int getHeight(Image i) throws IOException {
        ImageMagickImage imageMagickImage = (ImageMagickImage) i;
        try {
            Info imageInfo = new Info(getFile(i).getPath());
            return Integer.parseInt(GEOMETRY_PATTERN.split(imageInfo.getProperty("Geometry"))[1]);
        } catch (InfoException e) {
            logger.error("Error retrieving image " + imageMagickImage.getPath() + " height: " + e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error retrieving image " + imageMagickImage.getPath() + " height", e);
            }
            return -1;
        }
    }

    public int getWidth(Image i) throws IOException {
        ImageMagickImage imageMagickImage = (ImageMagickImage) i;
        try {
            Info imageInfo = new Info(getFile(i).getPath());
            return Integer.parseInt(GEOMETRY_PATTERN.split(imageInfo.getProperty("Geometry"))[0]);
        } catch (InfoException e) {
            logger.error("Error retrieving image " + imageMagickImage.getPath() + " weight: " + e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error retrieving image " + imageMagickImage.getPath() + " weight", e);
            }
            return -1;
        }
    }

    public boolean cropImage(Image i, File outputFile, int top, int left, int width, int height) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(i.getPath());
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
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(image.getPath());
            op.rotate(clockwise ? 90. : -90.);
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
