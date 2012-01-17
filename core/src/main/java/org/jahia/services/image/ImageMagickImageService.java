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

import org.apache.commons.io.IOUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;
import org.im4java.core.InfoException;
import org.im4java.process.ProcessStarter;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: toto
 * Date: 3/11/11
 * Time: 11:33
 */
public class ImageMagickImageService implements JahiaImageService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageMagickImageService.class);

    private static ImageMagickImageService instance;

    private String imageMagickPath;

    protected ImageMagickImageService() {

    }

    public void init() {
        ProcessStarter.setGlobalSearchPath(imageMagickPath);
    }

    public static synchronized ImageMagickImageService getInstance() {
        if (instance == null) {
            instance = new ImageMagickImageService();
        }
        return instance;
    }

    public void setImageMagickPath(String imageMagickPath) {
        this.imageMagickPath = imageMagickPath;
    }

    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
        File tmp = File.createTempFile("image", "tmp");
        Node contentNode = node.getNode(Constants.JCR_CONTENT);
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

    private File getFile(Image i) {
        return ((ImageMagickImage) i).getFile();
    }

    public boolean createThumb(Image iw, File outputFile, int size, boolean square) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(getFile(iw).getPath());

            if(square) {
                op.resize(size,size,"^");
                op.gravity("center");
                op.crop(size,size,0,0);
            } else {
                op.resize(size,size);
            }

            op.addImage(outputFile.getPath());

            cmd.run(op);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    public int getHeight(Image i) throws IOException {
        try {
            Info imageInfo = new Info(getFile(i).getPath());
            return Integer.parseInt(imageInfo.getProperty("Geometry").split("[x+]")[1]);
        } catch (InfoException e) {
            throw new IOException(e.getMessage());
        }
    }

    public int getWidth(Image i) throws IOException {
        try {
            Info imageInfo = new Info(getFile(i).getPath());
            return Integer.parseInt(imageInfo.getProperty("Geometry").split("[x+]")[0]);
        } catch (InfoException e) {
            throw new IOException(e.getMessage());
        }
    }

    public boolean cropImage(Image i, File outputFile, int top, int left, int width, int height) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(getFile(i).getPath());
            op.crop(width, height, left, top);
            op.addImage(outputFile.getPath());

            cmd.run(op);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    public boolean resizeImage(Image i, File outputFile, int width, int height) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(getFile(i).getPath());
            op.resize(width, height);
            op.addImage(outputFile.getPath());

            cmd.run(op);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    public boolean rotateImage(Image i, File outputFile, boolean clockwise) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(getFile(i).getPath());
            op.rotate(clockwise ? 90. : -90.);
            op.addImage(outputFile.getPath());

            cmd.run(op);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
}
