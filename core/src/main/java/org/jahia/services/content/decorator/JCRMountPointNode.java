/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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

import org.apache.log4j.Logger;
import org.jahia.services.content.*;
import javax.jcr.*;
import javax.jcr.version.VersionException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import java.util.Map;
import java.io.InputStream;

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
        try {
            getRootNode();
            return true;
        } catch (RepositoryException e) {
            getProvider().getSessionFactory().getDynamicMountPoints().remove(getPath());
            return false;
        }
    }

    public JCRNodeWrapper getNode(String s) throws PathNotFoundException, RepositoryException {
        return getRootNode().getNode(s);
    }

    public JCRNodeIteratorWrapper getNodes() throws RepositoryException {
        return getRootNode().getNodes();
    }

    public JCRNodeIteratorWrapper getNodes(String s) throws RepositoryException {
        return getRootNode().getNodes(s);
    }

    public JCRNodeWrapper addNode(String name) throws RepositoryException {
        return getRootNode().addNode(name);
    }

    public JCRNodeWrapper addNode(String name, String type) throws RepositoryException {
        return getRootNode().addNode(name, type);
    }

    @Override
    public JCRNodeWrapper uploadFile(String name, InputStream is, String contentType) throws RepositoryException {
        return getRootNode().uploadFile(name, is, contentType);
    }

    private JCRNodeWrapper getRootNode() throws RepositoryException {
        JCRStoreProvider provider = getMountProvider();

        if (provider != null) {
            JCRSessionWrapper sessionWrapper = (JCRSessionWrapper) getSession();
            return provider.getNodeWrapper(sessionWrapper.getProviderSession(provider).getRootNode(), sessionWrapper);
        } else {
            throw new RepositoryException("No provider found for mount point " + getPath() + " of type " + getPrimaryNodeTypeName());
        }
    }

    private JCRStoreProvider getMountProvider() throws RepositoryException {
        JCRStoreProvider provider;
        Map<String, JCRStoreProvider> dynamicMountPoints = getProvider().getSessionFactory().getDynamicMountPoints();
        if (dynamicMountPoints == null || !dynamicMountPoints.containsKey(getPath())) {
            ProviderFactory providerFactory = JCRStoreService.getInstance().getProviderFactories().get(getPrimaryNodeTypeName());
            if (providerFactory == null) {
                logger.warn("Couldn't find a provider factory for type " + getPrimaryNodeTypeName() + ". Please make sure a factory is deployed and active for this node type before the mount can be performed.");
                return null;
            }
            provider = providerFactory.mountProvider(this);
        } else {
            provider = dynamicMountPoints.get(getPath());
        }
        return provider;
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        try {
            getProvider().getSessionFactory().unmount(getMountProvider());
        } catch (RepositoryException e) {
            logger.warn("unable to unmount provider " + getProvider().getKey() + " at " + getPath() + " but node will be deleted anyway",e);
        }
        super.remove();
    }

}
