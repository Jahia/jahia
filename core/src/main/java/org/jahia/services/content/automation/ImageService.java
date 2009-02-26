/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.automation;

import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.jcr.Node;

import org.drools.ObjectFilter;
import org.drools.spi.KnowledgeHelper;
import org.jahia.api.Constants;
import org.jahia.tools.imageprocess.ImageProcess;
import org.jahia.utils.FileUtils;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 21, 2008
 * Time: 5:25:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageService {
    private static ImageService instance;

    private ImageService() {
    }

    public static synchronized ImageService getInstance() {
        if (instance == null) {
            instance = new ImageService();
        }
        return instance;
    }


//        public boolean createThumb(InputStream istream, OutputStream ostream, int size) throws IOException {
//        BufferedImage image = ImageIO.read(istream);
//
//        int height;
//        int width;
//        if (image.getWidth() > image.getHeight()) {
//            width = size;
//            height = image.getHeight()*size/image.getWidth();
//        } else {
//            width = image.getWidth()*size/image.getHeight();
//            height = size;
//        }
//
//        ScaleFilter f = new ScaleFilter(width,height);
//        BufferedImage dest = new BufferedImage(width,height,image.getType());
//        f.filter(image, dest);
//        ImageIO.write(dest, "jpg", ostream);
//        return true;
//    }

    private ImageWrapper getImageWrapper(final NodeWrapper imageNode, KnowledgeHelper drools) throws Exception {
        Iterator<?> it = drools.getWorkingMemory().iterateObjects(new ObjectFilter() {
            public boolean accept(Object o) {
                if (o instanceof ImageWrapper) {
                    return (((ImageWrapper) o).getParentNode() == imageNode);
                }
                return false;
            }
        });
        if (it.hasNext()) {
            return (ImageWrapper) it.next();
        }
        File tmp = File.createTempFile("image","tmp");
        Node node = imageNode.getNode();
        Node contentNode = node.getNode(Constants.JCR_CONTENT);
        FileUtils.copyStream(contentNode.getProperty(Constants.JCR_DATA).getStream(), new FileOutputStream(tmp));
        Opener op = new Opener();
        ImagePlus ip = op.openImage(tmp.getPath());
        ImageWrapper iw =  new ImageWrapper(imageNode, ip, op.getFileType(tmp.getPath()));
        tmp.delete();
        drools.insertLogical(iw);
        return iw;
    }

    /**
     * Creates a JPEG thumbnail from inputFile and saves it to disk in
     * outputFile. scaleWidth is the width to scale the image to
     */
    private boolean createThumb(ImageWrapper iw, File outputFile, int size) throws IOException {
        ImagePlus ip = iw.getImagePlus();
        // Load the input image.
        if (ip == null) {
            return false;
        }
        ip = new ImagePlus(ip.getTitle(), ip.getImageStack());
        ImageProcessor processor = ip.getProcessor();
        if (ip.getWidth() > ip.getHeight()) {
            processor = processor.resize(size, ip.getHeight()*size/ip.getWidth());
        } else {
            processor = processor.resize(ip.getWidth()*size/ip.getHeight(), size);
        }
        ip.setProcessor(null,processor);
        int type = iw.getImageType();

        return ImageProcess.save(type, ip,outputFile);
    }


    public void addThumbnail(NodeWrapper imageNode, String name, int size, KnowledgeHelper drools) throws Exception {
        if (!imageNode.getNode().hasNode(name)) {
            String savePath = org.jahia.settings.SettingsBean.getInstance().getTmpContentDiskPath();
            File Ftemp = new File(savePath);
            if (!Ftemp.exists()) Ftemp.mkdir();
            final File f = File.createTempFile("thumb","jpg", Ftemp);

            ImageWrapper iw = getImageWrapper(imageNode, drools);

            createThumb(iw, f, size);
            f.deleteOnExit();

            NodeWrapper thumbNode = new NodeWrapper(imageNode, name, "jnt:extraResource", drools);
            if (thumbNode.getNode() != null) {
                drools.insert(thumbNode);
                drools.insert(new PropertyWrapper(thumbNode, Constants.JCR_DATA, f, drools));
                drools.insert(new PropertyWrapper(thumbNode, Constants.JCR_MIMETYPE, imageNode.getMimeType(), drools));
                drools.insert(new PropertyWrapper(thumbNode, Constants.JCR_LASTMODIFIED, new GregorianCalendar(), drools));
            }
        }
    }

    public void setHeight(NodeWrapper imageNode, String propertyName, KnowledgeHelper drools) throws Exception {
        if (!imageNode.getNode().hasProperty(propertyName)) {
            ImageWrapper iw = getImageWrapper(imageNode, drools);
            ImagePlus ip = iw.getImagePlus();
            if (ip == null) {
                return;
            }
            drools.insert(new PropertyWrapper(imageNode, propertyName, ip.getHeight(), drools));
        }
    }

    public void setWidth(NodeWrapper imageNode, String propertyName, KnowledgeHelper drools) throws Exception {
        if (!imageNode.getNode().hasProperty(propertyName)) {
            ImageWrapper iw = getImageWrapper(imageNode, drools);
            ImagePlus ip = iw.getImagePlus();
            if (ip == null) {
                return;
            }
            drools.insert(new PropertyWrapper(imageNode, propertyName, ip.getWidth(), drools));
        }
    }

}
