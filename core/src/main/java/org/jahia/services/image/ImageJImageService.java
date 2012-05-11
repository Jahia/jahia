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

import ij.ImagePlus;
import ij.io.Opener;
import ij.process.Blitter;
import ij.process.ImageProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.tools.imageprocess.ImageProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.awt.image.BufferedImage;
import java.io.*;

/**
 * User: toto
 * Date: 3/11/11
 * Time: 11:33
 */
public class ImageJImageService extends AbstractImageService {
    private static ImageJImageService instance;

    private static final Logger logger = LoggerFactory.getLogger(ImageJImageService.class);

    protected ImageJImageService() {
        super();
    }

    public void init() {
    }

    public static ImageJImageService getInstance() {
        if (instance == null) {
            synchronized (ImageJImageService.class) {
                if (instance == null) {
                    instance = new ImageJImageService();
                }
            }
        }
        return instance;
    }

    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
        File tmp = null;
        OutputStream os = null;
        try {
            String fileExtension = FilenameUtils.getExtension(node.getName());
            if ((fileExtension != null) && (!"".equals(fileExtension))) {
                fileExtension += "." + fileExtension;
            } else {
                fileExtension = null;
            }
            tmp = File.createTempFile("image", fileExtension);
            Node contentNode = node.getNode(Constants.JCR_CONTENT);
            os = new BufferedOutputStream(new FileOutputStream(tmp));
            InputStream is = contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream();
            String mimeType = contentNode.getProperty(Constants.JCR_MIMETYPE).getString();
            try {
                IOUtils.copy(is, os);
            } finally {
                IOUtils.closeQuietly(os);
                IOUtils.closeQuietly(is);
            }
            ImagePlus ip = null;
            Opener op = new Opener();
            boolean java2DUsed = false;
            int fileType = op.getFileType(tmp.getPath());
            ip = op.openImage(tmp.getPath());
            if (ip == null) {
                logger.error("Couldn't open file " + tmp.getPath() + " for node " + node.getPath() + " with ImageJ !");
                return null;
            }
            return new ImageJImage(node.getPath(), ip, fileType, ip.getBufferedImage(), mimeType, java2DUsed);
        } finally {
            IOUtils.closeQuietly(os);
            FileUtils.deleteQuietly(tmp);
        }
    }


    /**
     * Creates a JPEG thumbnail from inputFile and saves it to disk in
     * outputFile. scaleWidth is the width to scale the image to
     */
    public boolean createThumb(Image iw, File outputFile, int size, boolean square) throws IOException {
        ImagePlus ip = ((ImageJImage)iw).getImagePlus();
        // Load the input image.
        if (ip == null) {
            return false;
        }
        if (square) {
            return resizeImage(iw, outputFile, size, size, ResizeType.ASPECT_FILL);
        } else {
            return resizeImage(iw, outputFile, size, size, ResizeType.ADJUST_SIZE);
        }
    }

    public int getHeight(Image i) {
        ImagePlus ip = ((ImageJImage)i).getImagePlus();
        if (ip != null) {
            return ip.getHeight();
        }
        return -1;
    }

    public int getWidth(Image i) {
        ImagePlus ip = ((ImageJImage)i).getImagePlus();
        if (ip != null) {
            return ip.getWidth();
        }
        return -1;
    }

    public boolean cropImage(Image i, File outputFile, int top, int left, int width, int height) throws IOException {

        ImageJImage imageJImage = (ImageJImage) i;

        ImagePlus ip = imageJImage.getImagePlus();
        ImageProcessor processor = ip.getProcessor();

        processor.setRoi(left, top, width, height);
        processor = processor.crop();
        ip.setProcessor(null, processor);

        return ImageProcess.save(imageJImage.getImageType(), ip, outputFile);
    }

    public boolean resizeImage(Image i, File outputFile, int width, int height) throws IOException {
        return resizeImage(i, outputFile, width, height, ResizeType.ADJUST_SIZE);
    }

    public boolean rotateImage(Image i, File outputFile, boolean clockwise) throws IOException {

        ImageJImage imageJImage = (ImageJImage) i;

        ImagePlus ip = imageJImage.getImagePlus();
        ImageProcessor processor = ip.getProcessor();
        if (clockwise) {
            processor = processor.rotateRight();
        } else {
            processor = processor.rotateLeft();
        }
        ip.setProcessor(null, processor);

        return ImageProcess.save(imageJImage.getImageType(), ip, outputFile);
    }

    public boolean resizeImage(Image i, File outputFile, int width, int height, ResizeType resizeType) throws IOException {

        ImageJImage imageJImage = (ImageJImage) i;

        ImagePlus ip = imageJImage.getImagePlus();
        
        resizeImage(ip, width, height, resizeType);

        return ImageProcess.save(imageJImage.getImageType(), ip, outputFile);
    }

    protected void resizeImage(ImagePlus ip, int width, int height, ResizeType resizeType) throws IOException {
        ImageProcessor processor = ip.getProcessor();

        int originalWidth = ip.getWidth();
        int originalHeight = ip.getHeight();
        ResizeCoords resizeCoords = getResizeCoords(resizeType, originalWidth, originalHeight, width, height);

        if (ResizeType.SCALE_TO_FILL.equals(resizeType)) {
            processor.setInterpolationMethod(ImageProcessor.BICUBIC);
            processor = processor.resize(width, height, true);
        } else if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            width = resizeCoords.getTargetWidth();
            height = resizeCoords.getTargetHeight();
            processor.setInterpolationMethod(ImageProcessor.BICUBIC);
            processor = processor.resize(width, height, true);
        } else if (ResizeType.ASPECT_FILL.equals(resizeType)) {
            processor.setRoi(resizeCoords.getSourceStartPosX(), resizeCoords.getSourceStartPosY(), resizeCoords.getSourceWidth(), resizeCoords.getSourceHeight());
            processor = processor.crop();
            processor.setInterpolationMethod(ImageProcessor.BICUBIC);
            processor = processor.resize(resizeCoords.getTargetWidth(), resizeCoords.getTargetHeight(), true);
        } else if (ResizeType.ASPECT_FIT.equals(resizeType)) {
            processor.setInterpolationMethod(ImageProcessor.BICUBIC);
            processor = processor.resize(resizeCoords.getTargetWidth(), resizeCoords.getTargetHeight(), true);
            ImageProcessor newProcessor = processor.createProcessor(width, height);
            newProcessor.copyBits(processor, resizeCoords.getTargetStartPosX(), resizeCoords.getTargetStartPosY(), Blitter.ADD);
            processor = newProcessor;
        }
        ip.setProcessor(null, processor);
    }

    public BufferedImage resizeImage(BufferedImage image, int width, int height,
            ResizeType resizeType) throws IOException {
        ImagePlus ip = new ImagePlus(null, image);

        resizeImage(ip, width, height, resizeType);

        return ip.getBufferedImage();
    }
}
