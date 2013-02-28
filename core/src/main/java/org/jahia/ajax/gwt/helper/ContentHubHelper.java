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

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.*;

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
    private ContentDefinitionHelper definitionHelper;
    private ContentManagerHelper contentManager;

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<GWTJahiaNodeType> getProviderFactoriesType(Locale uiLocale) throws GWTJahiaServiceException {
        try {
            List<GWTJahiaNodeType> providerFactoriesType = new ArrayList<GWTJahiaNodeType>();
            for (ProviderFactory factory : jcrStoreService.getProviderFactories().values()) {
                ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(factory.getNodeTypeName());
                providerFactoriesType.add(definitionHelper.getGWTJahiaNodeType(type, uiLocale));
            }
            return providerFactoriesType;
        } catch (NoSuchNodeTypeException e) {
            throw new GWTJahiaServiceException(e);
        }
    }

    public void mount(String mountName, String providerType, List<GWTJahiaNodeProperty> properties, JCRSessionWrapper session, Locale uiLocale) throws GWTJahiaServiceException {
        Map<String,String> parents = new HashMap<String, String>();
        parents.put("mounts","jnt:systemFolder");
        GWTJahiaNode n = contentManager.createNode("/mounts", mountName, providerType, null, properties, session, uiLocale, parents, false);
        try {
            if (((JCRMountPointNode) session.getNode(n.getPath())).checkMountPointValidity()) {
                session.save();
            } else {
                n.removeAll();
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException("unable to mount " + mountName);
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

    public void setDefinitionHelper(ContentDefinitionHelper definitionHelper) {
        this.definitionHelper = definitionHelper;
    }

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }
}
