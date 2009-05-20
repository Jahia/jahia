/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.jahia.services.content.impl.vfs.VFSContentStoreProvider;
import org.springframework.beans.BeanUtils;

import javax.jcr.*;
import javax.jcr.version.VersionException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyDescriptor;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 8, 2008
 * Time: 2:19:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRMountPointNode extends JCRNodeDecorator {
    public JCRMountPointNode(JCRNodeWrapper node) {
        super(node);

    }

    public boolean checkValidity() {
        try {
            getRootNode();
            return true;
        } catch (RepositoryException e) {
            getProvider().getService().getDynamicMountPoints().remove(getPath());
            return false;
        }
    }

    public List<JCRNodeWrapper> getChildren() {
        try {
            return getRootNode().getChildren();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return new ArrayList<JCRNodeWrapper>();
    }

    public Node getNode(String s) throws PathNotFoundException, RepositoryException {
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
        JCRStoreProvider provider = null;
        if (!getProvider().getService().getDynamicMountPoints().containsKey(getPath())) {
            if (isNodeType("jnt:vfsMountPoint")) {
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("root",getProperty("j:root").getString());
                provider = mount(VFSContentStoreProvider.class, getPath(), getUUID(), m);
            }
        } else {
            provider = getProvider().getService().getDynamicMountPoints().get(getPath());
        }

        if (provider != null) {
            return provider.getNodeWrapper("/", (JCRSessionWrapper) getSession());
        }
        return null;
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        getProvider().getService().unmount(getProvider());
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
            for (String k : params.keySet()) {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(providerClass, k);
                pd.getWriteMethod().invoke(provider, params.get(k));
            }
            provider.start();
            return provider;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }


}
