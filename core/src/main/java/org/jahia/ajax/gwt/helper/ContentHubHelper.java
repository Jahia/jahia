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

package org.jahia.ajax.gwt.helper;

import org.jahia.utils.i18n.JahiaResourceBundle;
import org.slf4j.Logger;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.usermanager.JahiaUser;
import ucar.nc2.util.net.EasyX509TrustManager;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:47:08 PM
 *
 */
public class ContentHubHelper {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ContentHubHelper.class);

    private JCRSessionFactory sessionFactory;
    private JCRStoreService jcrStoreService;
    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void mount(final String name, final String root, final JahiaUser user,final Locale uiLocale) throws GWTJahiaServiceException {
        if (user.isRoot()) {
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(user.getName(), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper parent = session.getNode("/");
                        JCRNodeWrapper mounts;
                        try {
                            mounts = parent.getNode("mounts");
                        } catch (PathNotFoundException nfe) {
                            mounts = parent.addNode("mounts", "jnt:systemFolder");
                        }

                        JCRMountPointNode childNode = null;
                        if (!mounts.isFile()) {
                            if (!mounts.isCheckedOut()) {
                                mounts.checkout();
                            }
                            Map<String, ExternalProvider> providers = jcrStoreService.getExternalProviders();
                            // create the node depending of the root
                            for (String k : providers.keySet()) {
                                ExternalProvider ext;
                                if (root.startsWith((ext = providers.get(k)).getPrefix())) {
                                    childNode = (JCRMountPointNode) mounts.addNode(name,"jnt:mountPoint");
                                    childNode.setProperty("j:provider", ext.getKey());
                                    break;
                                }
                            }
                            if (childNode == null) {
                                childNode = (JCRMountPointNode) mounts.addNode(name, "jnt:mountPoint");
                                childNode.setProperty("j:provider", "vfs");
                            }
                            childNode.setProperty("j:root", root);
                            boolean valid = childNode.checkMountPointValidity();
                            if (!valid) {
                                childNode.remove();
                                throw new RepositoryException(JahiaResourceBundle.getJahiaInternalResource("label.gwt.error.invalid.path",uiLocale));
                            }
                            session.save();
                        }
                        if (childNode == null) {
                            throw new RepositoryException(JahiaResourceBundle.getJahiaInternalResource("label.gwt.error.system.error.happened",uiLocale));
                        }
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                logger.error(JahiaResourceBundle.getJahiaInternalResource("label.gwt.error.system.error.happened",uiLocale), e);
                throw new GWTJahiaServiceException(JahiaResourceBundle.getJahiaInternalResource("label.gwt.error.system.error.happened",uiLocale));
            }
        } else {
            throw new GWTJahiaServiceException(JahiaResourceBundle.getJahiaInternalResource("label.gwt.error.only.root.can.mount.folders",uiLocale));
        }
    }

    public Map<String, String> getStoredPasswordsProviders(JahiaUser user) {
        Map<String, String> results = new HashMap<String, String>();
        results.put(null, user.getUsername());
        for (JCRStoreProvider provider : sessionFactory.getProviders().values()) {
            if ("storedPasswords".equals(provider.getAuthenticationType())) {
                results.put(provider.getKey(), user.getProperty("storedUsername_" + provider.getKey()));
            }
        }
        return results;
    }

    public void storePasswordForProvider(JahiaUser user, String providerKey, String username, String password) {
        if (username == null) {
            user.removeProperty("storedUsername_" + providerKey);
        } else {
            user.setProperty("storedUsername_" + providerKey, username);
        }
        if (password == null) {
            user.removeProperty("storedPassword_" + providerKey);
        } else {
            user.setProperty("storedPassword_" + providerKey, password);
        }
    }

    public void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }
}
