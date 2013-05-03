/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * An image service implementation that combines the Java2D and ImageJ service implementations
 * to expand the supported file formats (such as TIFF)
 */
public class ImageJAndJava2DImageService extends Java2DProgressiveBilinearImageService {

    private ImageJImageService imageJImageService = new ImageJImageService();
    private static ImageJAndJava2DImageService instance = new ImageJAndJava2DImageService();

    private static final Logger logger = LoggerFactory.getLogger(ImageJAndJava2DImageService.class);

    protected ImageJAndJava2DImageService() {
    }

    public void init() {
    }

    public static ImageJAndJava2DImageService getInstance() {
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
        if (iw instanceof BufferImage) {
            return super.createThumb(iw, outputFile, size, square);
        } else {
            return imageJImageService.createThumb(iw, outputFile, size, square);
        }
    }

    @Override
    public int getHeight(Image i) {
        if (i instanceof BufferImage) {
            return super.getHeight(i);
        } else {
            return imageJImageService.getHeight(i);
        }
    }

    @Override
    public int getWidth(Image i) {
        if (i instanceof BufferImage) {
            return super.getWidth(i);
        } else {
            return imageJImageService.getWidth(i);
        }
    }

    @Override
    public boolean cropImage(Image image, File outputFile, int top, int left, int width, int height) throws IOException {
        if (image instanceof BufferImage) {
            return super.cropImage(image, outputFile, top, left, width, height);
        } else {
            logger.info("Using ImageJ code for file " + image.getPath() + "...");
            return imageJImageService.cropImage(image, outputFile, top, left, width, height);
        }
    }

    @Override
    public boolean rotateImage(Image image, File outputFile, boolean clockwise) throws IOException {
        if (image instanceof BufferImage) {
            return super.rotateImage(image, outputFile, clockwise);
        } else {
            return imageJImageService.rotateImage(image, outputFile, clockwise);
        }
    }

    @Override
    public boolean resizeImage(Image image, File outputFile, int newWidth, int newHeight, ResizeType resizeType) throws IOException {
        if (image instanceof BufferImage) {
            return super.resizeImage(image, outputFile, newWidth, newHeight, resizeType);
        } else {
            return imageJImageService.resizeImage(image, outputFile, newWidth, newHeight, resizeType);
        }
    }

    public BufferedImage resizeImage(BufferedImage image, int width, int height, ResizeType resizeType) {
        return super.resizeImage(image, width, height, resizeType);
    }
}
