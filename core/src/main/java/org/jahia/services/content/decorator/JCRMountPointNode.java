/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.decorator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.content.*;
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
public class JCRMountPointNode extends JCRProtectedNodeAbstractDecorator {

    public static final String MOUNT_POINT_PROPERTY_NAME = "mountPoint";
    public static final String MOUNT_STATUS_PROPERTY_NAME = "mountStatus";
    public static final String MOUNT_SUFFIX = "-mount";
    public static final String MOUNT_POINT_SUFFIX = "-mountPoint";
    public static final String PROTECTED_PROPERTIES_PROPERTY_NAME = "protectedProperties";

    public static enum MountStatus {
        mounted, unmounted, waiting, error
    }

    @Override
    protected boolean canReadProperty(String propertyName) throws RepositoryException {

        if (node.hasPermission("adminMountPoints")) {
            return true;
        }

        if (PROTECTED_PROPERTIES_PROPERTY_NAME.equals(propertyName)) {
            return false;
        }

        if (node.hasProperty(PROTECTED_PROPERTIES_PROPERTY_NAME)) {
            JCRValueWrapper[] values = node.getProperty(PROTECTED_PROPERTIES_PROPERTY_NAME).getValues();
            if(values != null && values.length > 0) {
                for (JCRValueWrapper value : values) {
                    if(value != null && value.getString() != null && value.getString().equals(propertyName)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void setProtectedPropertyNames(String[] propertyNames) throws RepositoryException {
        super.setProperty(PROTECTED_PROPERTIES_PROPERTY_NAME, propertyNames);
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
        super(node, true);
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
