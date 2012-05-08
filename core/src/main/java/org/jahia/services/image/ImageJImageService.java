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
import ij.process.ImageProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.tools.imageprocess.ImageProcess;

import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * User: toto
 * Date: 3/11/11
 * Time: 11:33
 */
public class ImageJImageService  implements JahiaImageService {
    private static ImageJImageService instance;

    protected ImageJImageService() {

    }

    public void init() {
    }

    public static synchronized ImageJImageService getInstance() {
        if (instance == null) {
            instance = new ImageJImageService();
        }
        return instance;
    }

    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
        File tmp = null;
        OutputStream os = null;
        try {
            tmp = File.createTempFile("image", null);
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
            BufferedImage originalImage = null;
            int fileType = op.getFileType(tmp.getPath());
            if (fileType == Opener.PNG) {
                // Read image to scale
                originalImage = ImageIO.read(new File(tmp.getPath()));
                ip = new ImagePlus(node.getName(), originalImage);
            } else {
                ip = op.openImage(tmp.getPath());
            }
            return new ImageJImage(node.getPath(), ip, fileType, originalImage, mimeType);
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
        int type = ((ImageJImage) iw).getImageType();
        if (type == Opener.PNG) {
            // Create scaled destination image with transparency (ARGB)

            int destWidth, destHeight;
            if(square) {
                destWidth = size;
                destHeight = size;
            } else if (ip.getWidth() > ip.getHeight()) {
                destWidth = size;
                destHeight = ip.getHeight()*size/ip.getWidth();
            } else {
                destWidth = ip.getWidth()*size/ip.getHeight();
                destHeight = size;
            }

            BufferedImage originalImage = ((ImageJImage)iw).getOriginalImage();
            BufferedImage dest = new BufferedImage(destWidth, destHeight, originalImage.getType());

            // Paint source image into the destination, scaling as needed
            Graphics2D graphics2D = dest.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.drawImage(originalImage, 0, 0, destWidth, destHeight, null);
            graphics2D.dispose();

            // Save destination image
            return ImageIO.write(dest, "PNG", outputFile);
        } else {
            ip = new ImagePlus(ip.getTitle(), ip.getImageStack());
            ImageProcessor processor = ip.getProcessor();
            if(square) {
                processor = processor.resize(size, size, true);
            } else if (ip.getWidth() > ip.getHeight()) {
                processor = processor.resize(size, ip.getHeight()*size/ip.getWidth(), true);
            } else {
                processor = processor.resize(ip.getWidth()*size/ip.getHeight(), size, true);
            }
            ip.setProcessor(null,processor);

            return ImageProcess.save(type, ip, outputFile);
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

    public boolean cropImage(Image i, File outputFile, int top, int left, int width, int height) {
        ImagePlus ip = ((ImageJImage)i).getImagePlus();
        ImageProcessor processor = ip.getProcessor();

        processor.setRoi(left, top, width, height);
        processor = processor.crop();
        ip.setProcessor(null, processor);

        return ImageProcess.save(((ImageJImage) i).getImageType(), ip, outputFile);
    }

    public boolean resizeImage(Image i, File outputFile, int width, int height) {
        ImagePlus ip = ((ImageJImage)i).getImagePlus();
        ImageProcessor processor = ip.getProcessor();
        processor = processor.resize(width, height, true);
        ip.setProcessor(null, processor);

        return ImageProcess.save(((ImageJImage) i).getImageType(), ip, outputFile);
    }

    public boolean rotateImage(Image i, File outputFile, boolean clockwise) {
        ImagePlus ip = ((ImageJImage)i).getImagePlus();
        ImageProcessor processor = ip.getProcessor();
        if (clockwise) {
            processor = processor.rotateRight();
        } else {
            processor = processor.rotateLeft();
        }
        ip.setProcessor(null, processor);

        return ImageProcess.save(((ImageJImage) i).getImageType(), ip, outputFile);
    }

}
