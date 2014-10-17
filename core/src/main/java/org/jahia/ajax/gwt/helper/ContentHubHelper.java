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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import java.util.*;

/**
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:47:08 PM
 */
public class ContentHubHelper {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ContentHubHelper.class);
    
    private static final Map<String, String> MOUNT_PARENTS;

    static {
        MOUNT_PARENTS = new HashMap<String, String>();
        MOUNT_PARENTS.put("mounts", "jnt:systemFolder");
    }
    
    private JCRSessionFactory sessionFactory;
    private JCRStoreService jcrStoreService;
    private ContentDefinitionHelper definitionHelper;
    private ContentManagerHelper contentManager;
    private JahiaUserManagerService userManagerService;

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

    public void mount(String mountName, String providerType, List<GWTJahiaNodeProperty> properties,
            JCRSessionWrapper session, Locale uiLocale) throws GWTJahiaServiceException {
        String mountPoint = getMountParentPath(properties);
        try {
            GWTJahiaNode mount = contentManager.createNode("/mounts", (mountPoint == null ? mountName + "-mount"
                    : mountName), providerType, null, properties, session, uiLocale, MOUNT_PARENTS, false);
            session.save();
            ((JCRMountPointNode) session.getNode(mount.getPath())).getMountProvider();
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("failure.mount.label", uiLocale,
                    mountName, e.getMessage()));
        }
    }

    private String getMountParentPath(List<GWTJahiaNodeProperty> properties) {
        String mountPoint = null;
        if (properties != null) {
            for (GWTJahiaNodeProperty p : properties) {
                if (p.getName().equals("mountPoint")) {
                    List<GWTJahiaNodePropertyValue> values = p.getValues();
                    if (values != null && values.size() > 0) {
                        GWTJahiaNodePropertyValue v = values.get(0);
                        if (v != null && v.getNode() != null) {
                            mountPoint = v.getNode().getPath();
                            logger.info("Using specified mount parent path {}", mountPoint);
                        }
                    }
                    break;
                }

            }
        }
        
        return mountPoint;
    }

    public void unmount(String path, JCRSessionWrapper session, Locale uiLocale) throws GWTJahiaServiceException {
        try {
            JCRStoreProvider provider = JCRSessionFactory.getInstance().getMountPoints().get(path);
            if (provider != null && provider.isDynamicallyMounted()) {
                provider.stop();
            }
            String mountNodePath = "/mounts/"+ StringUtils.substringAfterLast(path,"/");
            if(!session.nodeExists(mountNodePath)){
                mountNodePath = mountNodePath+"-mount";
            }
            if(session.nodeExists(mountNodePath)) {
                JCRNodeWrapper node = session.getNode(mountNodePath);
                node.remove();
                session.save();
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(Messages.getInternal("failure.unmount.label", uiLocale) + " " + path);
        }
    }

    public Map<String, String> getStoredPasswordsProviders(JahiaUser user) {
        Map<String, String> results = new HashMap<String, String>();
        results.put(null, user.getUsername());
        JCRUserNode userNode = userManagerService.lookupUserByPath(user.getLocalPath());
        if (userNode != null) {
            for (JCRStoreProvider provider : sessionFactory.getProviders().values()) {
                if ("storedPasswords".equals(provider.getAuthenticationType())) {
                    results.put(provider.getKey(), userNode.getPropertyAsString("storedUsername_" + provider.getKey()));
                }
            }
        }
        return results;
    }

    public void storePasswordForProvider(JCRUserNode user, String providerKey, String username, String password) {
        try {
            if (username == null) {
                user.getProperty("storedUsername_" + providerKey).remove();
            } else {
                user.setProperty("storedUsername_" + providerKey, username);
            }
            if (password == null) {
                user.getProperty("storedPassword_" + providerKey).remove();
            } else {
                user.setProperty("storedPassword_" + providerKey, password);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
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

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }
}
