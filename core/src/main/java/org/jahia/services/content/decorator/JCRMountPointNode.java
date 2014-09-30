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
import org.jahia.services.content.*;

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
    private transient static Logger logger = Logger.getLogger(JCRMountPointNode.class);

    public JCRMountPointNode(JCRNodeWrapper node) {
        super(node);

    }

    public boolean checkMountPointValidity() {
        final JCRStoreProvider provider;
        try {
            provider = getMountProvider();
        } catch (Exception e) {
            logger.error("Couldn't retrieve provider", e);
            return false;
        }

        try {
            getRootNodeFrom(provider);
            return true;
        } catch (Exception e) {
            logger.error("Couldn't retrieve root node", e);
            if (provider != null) {
                getProvider().getSessionFactory().removeProvider(provider.getKey());
            }
            return false;
        }
    }

    private JCRNodeWrapper getRootNodeFrom(JCRStoreProvider provider) throws RepositoryException {
        if (provider != null) {
            JCRSessionWrapper sessionWrapper = getSession();
            return provider.getNodeWrapper(sessionWrapper.getProviderSession(provider).getRootNode(), sessionWrapper);
        } else {
            throw new RepositoryException("No provider found for mount point " + getPath() + " of type " + getPrimaryNodeTypeName());
        }
    }

    private JCRStoreProvider getMountProvider() throws RepositoryException {
        JCRStoreProvider provider;
        Map<String, JCRStoreProvider> mountPoints = getProvider().getSessionFactory().getMountPoints();
        JCRVirtualMountPointNode mountPoint = new JCRVirtualMountPointNode(this);
        String path = mountPoint.getPath();
        if (mountPoints == null || !mountPoints.containsKey(path)) {
            ProviderFactory providerFactory = JCRStoreService.getInstance().getProviderFactories().get(getPrimaryNodeTypeName());
            if (providerFactory == null) {
                logger.warn("Couldn't find a provider factory for type " + getPrimaryNodeTypeName() + ". Please make sure a factory is deployed and active for this node type before the mount can be performed.");
                return null;
            }

            provider = providerFactory.mountProvider(mountPoint);
        } else {
            provider = mountPoints.get(path);
        }
        return provider;
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        Map<String, JCRStoreProvider> mountPoints = getProvider().getSessionFactory().getMountPoints();
        JCRVirtualMountPointNode mountPoint = new JCRVirtualMountPointNode(this);
        String path = mountPoint.getPath();
        JCRStoreProvider p = mountPoints != null ? mountPoints.get(path) : null;
        if (p != null) {
            getProvider().getSessionFactory().unmount(p);
        }
        super.remove();
    }

    private class JCRVirtualMountPointNode extends JCRNodeDecorator {
        public JCRVirtualMountPointNode(JCRMountPointNode jcrMountPointNode) {
            super(jcrMountPointNode.getDecoratedNode());
        }

        @Override
        public String getPath() {
            String path = StringUtils.substringBefore(node.getPath(), "-mount");
            try {
                if(node.hasProperty("mountPoint")){
                    path = node.getProperty("mountPoint").getNode().getPath()+"/"+node.getName();
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
            return path;
        }
    }
}
