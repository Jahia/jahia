/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
