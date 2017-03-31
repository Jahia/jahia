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
package org.jahia.services.content.rules;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.drools.core.spi.KnowledgeHelper;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.image.Image;
import org.jahia.services.image.JahiaImageService;
import org.jahia.settings.SettingsBean;
import org.kie.api.runtime.ObjectFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.io.File;
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

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private JahiaImageService imageService;

    public void setImageService(JahiaImageService imageService) {
        this.imageService = imageService;
    }

    private static File contentTempFolder;

    private ImageService() {
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final ImageService INSTANCE = new ImageService();

        static {
            contentTempFolder = new File(SettingsBean.getInstance().getTmpContentDiskPath());
            if (!contentTempFolder.exists()) {
                contentTempFolder.mkdirs();
            }
        }
    }

    public static ImageService getInstance() {
        return Holder.INSTANCE;
    }


    private Image getImageWrapper(final AddedNodeFact imageNode, KnowledgeHelper drools) throws Exception {
        Iterator<?> it = drools.getWorkingMemory().iterateObjects(new ObjectFilter() {
            public boolean accept(Object o) {
                if (o instanceof Image) {
                    try {
                        return (((Image) o).getPath().equals(imageNode.getPath()));
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                return false;
            }
        });
        if (it.hasNext()) {
            return (Image) it.next();
        }
        Image iw = imageService.getImage(imageNode.getNode());
        if (iw == null) {
            return null;
        }
        drools.insertLogical(iw);
        return iw;
    }

    public void addThumbnail(AddedNodeFact imageNode, String name, int size, KnowledgeHelper drools) throws Exception {
        addThumbnail(imageNode, name, size, false, drools);
    }

    public void addThumbnail(AddedNodeFact imageNode, String name, int size, boolean square, KnowledgeHelper drools) throws Exception {
        long timer = System.currentTimeMillis();
        if (imageNode.getNode().hasNode(name)) {
            JCRNodeWrapper node = imageNode.getNode().getNode(name);
            Calendar thumbDate = node.getProperty("jcr:lastModified").getDate();
            Calendar contentDate = imageNode.getNode().getNode("jcr:content").getProperty("jcr:lastModified").getDate();
            if (contentDate.after(thumbDate)) {
                AddedNodeFact thumbNode = new AddedNodeFact(node);
                File f = getThumbFile(imageNode, size, square, drools);
                if (f == null) {
                    return;
                }
                drools.insert(new ChangedPropertyFact(thumbNode, Constants.JCR_DATA, f, drools));
                drools.insert(new ChangedPropertyFact(thumbNode, Constants.JCR_LASTMODIFIED, new GregorianCalendar(), drools));
            }
        } else {
            File f = getThumbFile(imageNode, size, square, drools);
            if (f == null) {
                return;
            }

            AddedNodeFact thumbNode = new AddedNodeFact(imageNode, name, "jnt:resource", drools);
            if (thumbNode.getNode() != null) {
                drools.insert(thumbNode);
                drools.insert(new ChangedPropertyFact(thumbNode, Constants.JCR_DATA, f, drools));
                drools.insert(new ChangedPropertyFact(thumbNode, Constants.JCR_MIMETYPE, imageNode.getMimeType(), drools));
                drools.insert(new ChangedPropertyFact(thumbNode, Constants.JCR_LASTMODIFIED, new GregorianCalendar(), drools));
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{}px thumbnail for node {} created in {} ms", new Object[]{size,
                    imageNode.getNode().getPath(), System.currentTimeMillis() - timer});
        }
    }

    public void addThumbnail(ChangedPropertyFact propertyWrapper, String name, int size, KnowledgeHelper drools) throws Exception {
        final JCRPropertyWrapper property = propertyWrapper.getProperty();
        final JCRSessionWrapper session = property.getSession();
        JCRNodeWrapper node = session.getNodeByIdentifier(property.getString());
        addThumbnail(new AddedNodeFact(node), name, size, drools);
    }

    public void addSquareThumbnail(ChangedPropertyFact propertyWrapper, String name, int size, KnowledgeHelper drools) throws Exception {
        final JCRPropertyWrapper property = propertyWrapper.getProperty();
        final JCRSessionWrapper session = property.getSession();
        JCRNodeWrapper node = session.getNodeByIdentifier(property.getString());
        addThumbnail(new AddedNodeFact(node), name, size, true, drools);
    }

    protected boolean isSmallerThan(JCRNodeWrapper node, int size) {
        long width = 0;
        long height = 0;

        try {
            width = node.getProperty("j:width").getLong();
            height = node.getProperty("j:height").getLong();
        } catch (PathNotFoundException e) {
            // no size properties found
        } catch (RepositoryException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Error reading j:width/j:height properties on node " + node.getPath(),
                        e);
            } else {
                logger.warn("Error reading j:width/j:height properties on node " + node.getPath()
                        + ". Casue: " + e.getMessage());
            }
        }

        return width > 0 && height > 0 && width <= size && height <= size;
    }

    protected File getThumbFile(AddedNodeFact imageNode, int size, boolean square, KnowledgeHelper drools) throws Exception {
        String fileExtension = FilenameUtils.getExtension(imageNode.getName());

        if (!square && isSmallerThan(imageNode.getNode(), size)) {
            // no need to resize the small image for thumbnail
            final File f = File.createTempFile("thumb", StringUtils.isNotEmpty(fileExtension) ? "." + fileExtension : null, contentTempFolder);
            JCRContentUtils.downloadFileContent(imageNode.getNode(), f);
            f.deleteOnExit();

            return f;
        }

        Image iw = getImageWrapper(imageNode, drools);
        if (iw == null) {
            return null;
        }

        final File f = File.createTempFile("thumb", StringUtils.isNotEmpty(fileExtension) ? "." + fileExtension : null, contentTempFolder);

        if (imageService.createThumb(iw, f, size, square)) {
            f.deleteOnExit();
            return f;
        } else {
            f.deleteOnExit();
            return null;
        }
    }

    public void setHeight(AddedNodeFact imageNode, String propertyName, KnowledgeHelper drools) throws Exception {
        Image iw = getImageWrapper(imageNode, drools);
        if (iw == null) {
            return;
        }
        int height = imageService.getHeight(iw);
        if (height == -1) {
            return;
        }
        drools.insert(new ChangedPropertyFact(imageNode, propertyName, height, drools));
    }

    public void setWidth(AddedNodeFact imageNode, String propertyName, KnowledgeHelper drools) throws Exception {
        Image iw = getImageWrapper(imageNode, drools);
        if (iw == null) {
            return;
        }
        int width = imageService.getWidth(iw);
        if (width == -1) {
            return;
        }
        drools.insert(new ChangedPropertyFact(imageNode, propertyName, width, drools));
    }

    public void disposeImageForNode(final AddedNodeFact imageNode, KnowledgeHelper drools) throws Exception {
        Iterator<?> it = drools.getWorkingMemory().iterateObjects(new ObjectFilter() {
            public boolean accept(Object o) {
                if (o instanceof Image) {
                    try {
                        return (((Image) o).getPath().equals(imageNode.getPath()));
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(),e);
                    }
                }
                return false;
            }
        });
        for (; it.hasNext(); ) {
            Image img = (Image) it.next();
            drools.retract(img);
            img.dispose();
            img = null;
        }
    }
}
