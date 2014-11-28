/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.content.decorator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.settings.SettingsBean;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import java.util.Map;

/**
 * A node representing a mount point for an external data provider
 * Date: Dec 8, 2008
 * Time: 2:19:40 PM
 */
public class JCRMountPointNode extends JCRNodeDecorator {

    public static final String MOUNT_POINT_PROPERTY_NAME = "mountPoint";
    public static final String MOUNT_STATUS_PROPERTY_NAME = "mountStatus";
    public static final String MOUNT_SUFFIX = "-mount";
    public static final String MOUNT_POINT_SUFFIX = "-mountPoint";

    public static enum MountStatus {
        mounted, unmounted, waiting, error
    }

    private class JCRVirtualMountPointNode extends JCRNodeDecorator {
        private JCRVirtualMountPointNode(JCRMountPointNode jcrMountPointNode) {
            super(jcrMountPointNode.getDecoratedNode());
        }

        @Override
        public String getPath() {
            return getTargetMountPointPath();
        }
    }

    private transient static Logger logger = Logger.getLogger(JCRMountPointNode.class);

    /**
     * Initializes an instance of this class.
     *
     * @param node the node to be decorated
     */
    public JCRMountPointNode(JCRNodeWrapper node) {
        super(node);
    }

    public JCRStoreProvider getMountProvider() throws RepositoryException {
        JCRSessionFactory sessionFactory = getProvider().getSessionFactory();
        Map<String, JCRStoreProvider> mountPoints = sessionFactory.getProviders();
        return mountPoints.get(getIdentifier());
    }

    /**
     * Returns a special wrapper which is transparently handling the path of the mount point.
     *
     * @return a special wrapper which is transparently handling the path of the mount point
     */
    public JCRNodeWrapper getVirtualMountPointNode() {
        return new JCRVirtualMountPointNode(this);
    }

    @Override
    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        Map<String, JCRStoreProvider> mountPoints = getProvider().getSessionFactory().getMountPoints();
        JCRVirtualMountPointNode mountPoint = new JCRVirtualMountPointNode(this);
        String path = mountPoint.getPath();
        JCRStoreProvider p = mountPoints != null ? mountPoints.get(path) : null;
        if (p != null) {
            p.stop();
        }
        super.remove();
    }

    public MountStatus getMountStatus() {
        final String status = getPropertyAsString(MOUNT_STATUS_PROPERTY_NAME);
        return status == null ? MountStatus.mounted : MountStatus.valueOf(status);
    }

    public void setMountStatus(String status) {
        if (status != null) {
            // convert to MountStatus first to check if it's a valid status
            final MountStatus mountStatus = MountStatus.valueOf(status);
            setMountStatus(mountStatus);
        }
    }

    public void setMountStatus(MountStatus mountStatus) {
        if (mountStatus != null) {
            JCRSessionWrapper session = null;
            Boolean isValidationSkipped = null;
            try {
                if (!hasProperty(MOUNT_STATUS_PROPERTY_NAME)
                        || !getProperty(MOUNT_STATUS_PROPERTY_NAME).getValue().getString().equals(mountStatus.name())) {
                    session = getSession();
                    isValidationSkipped = session.isSkipValidation();
                    session.setSkipValidation(true);
                    setProperty(MOUNT_STATUS_PROPERTY_NAME, mountStatus.name());
                }
            } catch (InvalidItemStateException ise) {
                // ignore in cluster mode as servers could modify the mountStatus concurrently
                if (!SettingsBean.getInstance().isClusterActivated()) {
                    logger.error("Couldn't save mount status for node " + node.getName(), ise);
                }
            } catch (RepositoryException e) {
                logger.error("Couldn't save mount status for node " + node.getName(), e);
            } finally {
                if (session != null && isValidationSkipped != null) {
                    session.setSkipValidation(isValidationSkipped);
                }
            }
        }
    }

    public String getTargetMountPointPath() {
        String path;
        try {
            if (node.hasProperty(MOUNT_POINT_PROPERTY_NAME)) {
                path = node.getProperty(MOUNT_POINT_PROPERTY_NAME).getNode().getPath() + "/" + StringUtils.removeEnd(node.getName(), MOUNT_SUFFIX);
            } else if (node.getPath().endsWith(MOUNT_SUFFIX)) {
                path = StringUtils.removeEnd(node.getPath(), MOUNT_SUFFIX);
            } else {
                path = node.getPath() + MOUNT_POINT_SUFFIX;
            }
        } catch (RepositoryException e) {
            if (!(e instanceof ItemNotFoundException)) {
                logger.error(e.getMessage(), e);
            }
            path = node.getPath() + MOUNT_POINT_SUFFIX;
        }

        return path;
    }
}
