/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
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
import java.io.*;
import java.util.List;

/**
 * An image service implementation that uses the external ImageMagick command line tool to
 *  * perform high-quality image manipulation for a wide variety of image formats.
 *  NOTE : Service for imageMagick 6 support
 *
 * @author yousria
 */
public class ImageMagickImage6Service extends ImageMagickImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageMagickImage6Service.class);

    private static ImageMagickImage6Service instance = new ImageMagickImage6Service();


    public static ImageMagickImage6Service getInstance() {
        return instance;
    }

    protected void readDimensions(ImageMagickImage img) {
        long timer = System.currentTimeMillis();
        try {
            IMOperation op = new IMOperation();
            op.format("%w\n%h");
            op.addImage(img.getFile().getPath());

            // execute ...
            IdentifyCmd identify = new IdentifyCmd();

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
}
