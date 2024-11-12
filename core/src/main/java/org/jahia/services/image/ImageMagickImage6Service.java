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
