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
package org.jahia.ajax.gwt.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:47:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContentHubHelper {
    private static Logger logger = Logger.getLogger(ContentHubHelper.class);

    private JCRSessionFactory sessionFactory;

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void mount(final String name, final String root, final JahiaUser user) throws GWTJahiaServiceException {
        if (user.isAdminMember(0)) {
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper parent = session.getNode("/content");
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

                            childNode = (JCRMountPointNode) mounts.addNode(name, "jnt:vfsMountPoint");
                            childNode.setProperty("j:root", root);

                            boolean valid = childNode.checkValidity();
                            if (!valid) {
                                childNode.remove();
                                throw new RepositoryException("Invalid path");
                            }
                            session.save();
                        }
                        if (childNode == null) {
                            throw new RepositoryException("A system error happened");
                        }
                        return null;
                    }
                }, user.getName());
            } catch (RepositoryException e) {
                logger.error("A system error happened", e);
                throw new GWTJahiaServiceException("A system error happened");
            }
        } else {
            throw new GWTJahiaServiceException("Only root can mount folders");
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
}
