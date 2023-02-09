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
package org.jahia.services.content.decorator;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.*;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(JCRMountPointNode.class);

    public static final String MOUNT_POINT_PROPERTY_NAME = "mountPoint";
    public static final String MOUNT_STATUS_PROPERTY_NAME = "mountStatus";
    public static final String MOUNT_SUFFIX = "-mount";
    public static final String MOUNT_POINT_SUFFIX = "-mountPoint";
    private static final String PERMISSION_TO_READ_PROPERTIES = "adminMountPoints";
    public static final String PROTECTED_PROPERTIES_PROPERTY_NAME = "protectedProperties";

    public static enum MountStatus {
        mounted, unmounted, waiting, error
    }

    private static Boolean restrictReadForAllProperties;

    @Override
    protected boolean canReadProperty(String propertyName) throws RepositoryException {

        if (node.hasPermission(PERMISSION_TO_READ_PROPERTIES)) {
            return true;
        } else if (isRestrictReadForAllProperties()) {
            // if we restrict access to all properties for unauthorized users, we do not allow to read any of them
            return false;
        }

        if (PROTECTED_PROPERTIES_PROPERTY_NAME.equals(propertyName)) {
            return false;
        }

        // we do not restrict access to all properties by default; check if the node defines which properties have to be restricted
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

    /**
     * Flag to restrict read access to all properties of this mount point node for users which do not have appropriate permission
     * {@link #PERMISSION_TO_READ_PROPERTIES}. If <code>true</code>, all properties will be protected by default for unauthorized users.
     *
     * @return <code>true</code> if the access to all properties of this node should be restricted for all unauthorized users
     */
    private boolean isRestrictReadForAllProperties() {
        if (restrictReadForAllProperties == null) {
            restrictReadForAllProperties = Boolean.valueOf(SettingsBean.getInstance().getPropertiesFile()
                    .getProperty("jahia.jcr.mountPointNode.restrictReadForAllProperties", "true"));
        }
        return restrictReadForAllProperties.booleanValue();
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
