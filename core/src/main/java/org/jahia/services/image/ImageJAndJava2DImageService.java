/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

    private ImageJImageService imageJImageService = ImageJImageService.getInstance();
    private static ImageJAndJava2DImageService instance = new ImageJAndJava2DImageService();

    private static final Logger logger = LoggerFactory.getLogger(ImageJAndJava2DImageService.class);

    private ImageJAndJava2DImageService() {
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
