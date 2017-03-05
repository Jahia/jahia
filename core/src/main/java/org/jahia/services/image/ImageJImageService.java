/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.process.Blitter;
import ij.process.ImageProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * ImageJ application operation implementation
 */
public class ImageJImageService extends AbstractImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageJImageService.class);

    private ImageJImageService() {
        super();
    }

    public void init() {
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final ImageJImageService INSTANCE = new ImageJImageService();
    }

    public static ImageJImageService getInstance() {
        return Holder.INSTANCE;
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
            try {
                IOUtils.copy(is, os);
            } finally {
                IOUtils.closeQuietly(os);
                IOUtils.closeQuietly(is);
            }
            ImagePlus ip = null;
            Opener op = new Opener();
            int fileType = op.getFileType(tmp.getPath());
            ip = op.openImage(tmp.getPath());
            if (ip == null) {
                logger.error("Couldn't open file " + tmp.getPath() + " for node " + node.getPath() + " with ImageJ !");
                return null;
            }
            return new ImageJImage(node.getPath(), ip, fileType);
        } finally {
            IOUtils.closeQuietly(os);
            FileUtils.deleteQuietly(tmp);
        }
    }

    public int getHeight(Image i) {
        ImagePlus ip = ((ImageJImage) i).getImagePlus();
        if (ip != null) {
            return ip.getHeight();
        }
        return -1;
    }

    public int getWidth(Image i) {
        ImagePlus ip = ((ImageJImage) i).getImagePlus();
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

        return save(imageJImage.getImageType(), ip, outputFile);
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

        return save(imageJImage.getImageType(), ip, outputFile);
    }

    public boolean resizeImage(Image i, File outputFile, int width, int height, ResizeType resizeType) throws IOException {

        ImageJImage imageJImage = (ImageJImage) i;

        ImagePlus ip = imageJImage.getImagePlus();

        resizeImage(ip, width, height, resizeType);

        return save(imageJImage.getImageType(), ip, outputFile);
    }

    public BufferedImage resizeImage(BufferedImage image, int width, int height,
                                     ResizeType resizeType) throws IOException {
        ImagePlus ip = new ImagePlus(null, image);

        resizeImage(ip, width, height, resizeType);

        return ip.getBufferedImage();
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

    protected static boolean save(int type, ImagePlus ip, File outputFile) {
        switch (type) {
            case Opener.TIFF:
                return new FileSaver(ip).saveAsTiff(outputFile.getPath());
            case Opener.GIF:
                return new FileSaver(ip).saveAsGif(outputFile.getPath());
            case Opener.JPEG:
                return new FileSaver(ip).saveAsJpeg(outputFile.getPath());
            case Opener.TEXT:
                return new FileSaver(ip).saveAsText(outputFile.getPath());
            case Opener.LUT:
                return new FileSaver(ip).saveAsLut(outputFile.getPath());
            case Opener.ZIP:
                return new FileSaver(ip).saveAsZip(outputFile.getPath());
            case Opener.BMP:
                return new FileSaver(ip).saveAsBmp(outputFile.getPath());
            case Opener.PNG:
                ImagePlus tempImage = WindowManager.getTempCurrentImage();
                WindowManager.setTempCurrentImage(ip);
                PlugIn p = null;
                try {
                    p = (PlugIn) Class.forName("ij.plugin.PNG_Writer").newInstance();
                    p.run(outputFile.getPath());
                } catch (InstantiationException e) {
                    logger.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage(), e);
                }
                WindowManager.setTempCurrentImage(tempImage);
                return true;
            case Opener.PGM:
                return new FileSaver(ip).saveAsPgm(outputFile.getPath());
        }
        return false;
    }

}
