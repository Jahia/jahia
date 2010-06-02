/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.rules;

import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageProcessor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.drools.ObjectFilter;
import org.drools.spi.KnowledgeHelper;
import org.jahia.api.Constants;
import org.jahia.tools.imageprocess.ImageProcess;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Property;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

/**
 * Imaging service.
 * User: toto
 * Date: Oct 21, 2008
 * Time: 5:25:31 PM
 */
public class ImageService {
    private static ImageService instance;

    public static synchronized ImageService getInstance() {
        if (instance == null) {
            instance = new ImageService();
        }
        return instance;
    }


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
        ImageWrapper iw;
        File tmp = null;
        FileOutputStream os = null;
        try {
            tmp = File.createTempFile("image", null);
            Node node = imageNode.getNode();
            Node contentNode = node.getNode(Constants.JCR_CONTENT);
            os = new FileOutputStream(tmp);
            InputStream is = contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream();
            try {
                IOUtils.copy(is, os);
            } finally {
                IOUtils.closeQuietly(os);
                IOUtils.closeQuietly(is);
            }
            Opener op = new Opener();
            ImagePlus ip = op.openImage(tmp.getPath());
            iw =  new ImageWrapper(imageNode, ip, op.getFileType(tmp.getPath()));
            drools.insertLogical(iw);
        } finally {
            IOUtils.closeQuietly(os);
            FileUtils.deleteQuietly(tmp);
        }
        return iw;
    }

    /**
     * Creates a JPEG thumbnail from inputFile and saves it to disk in
     * outputFile. scaleWidth is the width to scale the image to
     */
    private boolean createThumb(ImageWrapper iw, File outputFile, int size, boolean square) throws IOException {
        ImagePlus ip = iw.getImagePlus();
        // Load the input image.
        if (ip == null) {
            return false;
        }
        ip = new ImagePlus(ip.getTitle(), ip.getImageStack());
        ImageProcessor processor = ip.getProcessor();
        if(square) {
            processor = processor.resize(size, size);
        } else if (ip.getWidth() > ip.getHeight()) {
            processor = processor.resize(size, ip.getHeight()*size/ip.getWidth());
        } else {
            processor = processor.resize(ip.getWidth()*size/ip.getHeight(), size);
        }
        ip.setProcessor(null,processor);
        int type = iw.getImageType();

        return ImageProcess.save(type, ip,outputFile);
    }

    public void addThumbnail(NodeWrapper imageNode, String name, int size, KnowledgeHelper drools) throws Exception {
        addThumbnail(imageNode, name, size,false, drools);
    }
    public void addThumbnail(NodeWrapper imageNode, String name, int size,boolean square, KnowledgeHelper drools) throws Exception {
        if (imageNode.getNode().hasNode(name)) {
            Node node = imageNode.getNode().getNode(name);
            Calendar thumbDate = node.getProperty("jcr:lastModified").getDate();
            Calendar contentDate = imageNode.getNode().getNode("jcr:content").getProperty("jcr:lastModified").getDate();
            if (contentDate.after(thumbDate)) {
                NodeWrapper thumbNode = new NodeWrapper(node);
                File f = getThumbFile(imageNode, size,square, drools);
                drools.insert(new PropertyWrapper(thumbNode, Constants.JCR_DATA, f, drools, false));
                drools.insert(new PropertyWrapper(thumbNode, Constants.JCR_LASTMODIFIED, new GregorianCalendar(), drools, false));
            }
        } else {
            File f = getThumbFile(imageNode, size,square, drools);

            NodeWrapper thumbNode = new NodeWrapper(imageNode, name, "jnt:resource", drools);
            if (thumbNode.getNode() != null) {
                drools.insert(thumbNode);
                drools.insert(new PropertyWrapper(thumbNode, Constants.JCR_DATA, f, drools, false));
                drools.insert(new PropertyWrapper(thumbNode, Constants.JCR_MIMETYPE, imageNode.getMimeType(), drools, false));
                drools.insert(new PropertyWrapper(thumbNode, Constants.JCR_LASTMODIFIED, new GregorianCalendar(), drools, false));
            }
        }
    }

    public void addThumbnail(PropertyWrapper propertyWrapper, String name, int size, KnowledgeHelper drools) throws Exception {
        final Property property = propertyWrapper.getProperty();
        final Session session = property.getSession();
        Node node = session.getNodeByIdentifier(property.getString());
        addThumbnail(new NodeWrapper(node),name, size,drools);
    }

    public void addSquareThumbnail(PropertyWrapper propertyWrapper, String name, int size, KnowledgeHelper drools) throws Exception {
        final Property property = propertyWrapper.getProperty();
        final Session session = property.getSession();
        Node node = session.getNodeByIdentifier(property.getString());
        addThumbnail(new NodeWrapper(node),name, size,drools);
    }

    private File getThumbFile(NodeWrapper imageNode, int size,boolean square,KnowledgeHelper drools) throws Exception {
        String savePath = org.jahia.settings.SettingsBean.getInstance().getTmpContentDiskPath();
        File Ftemp = new File(savePath);
        if (!Ftemp.exists()) Ftemp.mkdirs();
        final File f = File.createTempFile("thumb","jpg", Ftemp);

        ImageWrapper iw = getImageWrapper(imageNode, drools);

        createThumb(iw, f, size,square);
        f.deleteOnExit();
        return f;
    }

    public void setHeight(NodeWrapper imageNode, String propertyName, KnowledgeHelper drools) throws Exception {
        if (!imageNode.getNode().hasProperty(propertyName)) {
            ImageWrapper iw = getImageWrapper(imageNode, drools);
            ImagePlus ip = iw.getImagePlus();
            if (ip == null) {
                return;
            }
            drools.insert(new PropertyWrapper(imageNode, propertyName, ip.getHeight(), drools, false));
        }
    }

    public void setWidth(NodeWrapper imageNode, String propertyName, KnowledgeHelper drools) throws Exception {
        if (!imageNode.getNode().hasProperty(propertyName)) {
            ImageWrapper iw = getImageWrapper(imageNode, drools);
            ImagePlus ip = iw.getImagePlus();
            if (ip == null) {
                return;
            }
            drools.insert(new PropertyWrapper(imageNode, propertyName, ip.getWidth(), drools, false));
        }
    }

}
