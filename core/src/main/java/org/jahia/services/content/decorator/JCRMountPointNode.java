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

package org.jahia.services.content.decorator;

import org.jahia.services.content.*;
import org.jahia.services.content.impl.vfs.VFSContentStoreProvider;
import org.springframework.beans.BeanUtils;

import javax.jcr.*;
import javax.jcr.version.VersionException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyDescriptor;
import java.io.InputStream;

/**
 * User: toto
 * Date: Dec 8, 2008
 * Time: 2:19:40 PM
 */
public class JCRMountPointNode extends JCRNodeDecorator {
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

    public NodeIterator getNodes() throws RepositoryException {
        return getRootNode().getNodes();
    }

    public NodeIterator getNodes(String s) throws RepositoryException {
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
        }
        return null;
    }

    private JCRStoreProvider getMountProvider() throws RepositoryException {
        JCRStoreProvider provider = null;
        Map<String, JCRStoreProvider> dynamicMountPoints = getProvider().getSessionFactory().getDynamicMountPoints();
        if (!dynamicMountPoints.containsKey(getPath())) {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("root",getProperty("j:root").getString());
            ExternalProvider e = JCRStoreService.getInstance().getExternalProviders().get(hasProperty("j:provider")?getProperty("j:provider").getString():"vfs");
            provider = mount(e.getProvider().getClass(), getPath(), getUUID(), m);
        } else {
            provider = dynamicMountPoints.get(getPath());
        }
        return provider;
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        getProvider().getSessionFactory().unmount(getMountProvider());
        super.remove();
    }


    public JCRStoreProvider mount(Class<? extends JCRStoreProvider> providerClass, String mountPoint, String key, Map<String, Object> params) throws RepositoryException {
        JCRStoreProvider provider = null;
        try {
            provider = providerClass.newInstance();
            provider.setUserManagerService(getProvider().getUserManagerService());
            provider.setGroupManagerService(getProvider().getGroupManagerService());
            provider.setSitesService(getProvider().getSitesService());
            provider.setService(getProvider().getService());
            provider.setKey(key);
            provider.setMountPoint(mountPoint);
            provider.setDynamicallyMounted(true);
            for (Map.Entry<String,Object> k : params.entrySet()) {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(providerClass, k.getKey());
                pd.getWriteMethod().invoke(provider, k.getValue());
            }
            provider.setSessionFactory(getProvider().getSessionFactory());
            provider.setPublicationService(getProvider().getPublicationService());
            provider.start();
            return provider;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }


}
