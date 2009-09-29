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
package org.jahia.services.content;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.xml.sax.ContentHandler;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: toto
 * Date: 15 nov. 2007 - 15:18:34
 */
public class JCRStoreService extends JahiaService  {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JCRStoreService.class);

    private Map<String, String> decorators = new HashMap<String, String>();

    static private JCRStoreService instance = null;
    private JCRSessionFactory sessionFactory;

    protected JCRStoreService() {
    }

    public synchronized static JCRStoreService getInstance() {
        if (instance == null) {
            instance = new JCRStoreService();
        }
        return instance;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void start() throws JahiaInitializationException {
        try {
            NodeTypeRegistry.getInstance();
        } catch (Exception e) {
            logger.error("Repository init error", e);
        }
    }

    public Map<String, String> getDecorators() {
        return decorators;
    }

    public void setDecorators(Map<String, String> decorators) {
        this.decorators = decorators;
    }

    public void stop() throws JahiaException {
    }



    public void deployDefinitions(String systemId) {
        for (JCRStoreProvider provider : sessionFactory.getProviders().values()) {
            if (provider.canRegisterCustomNodeTypes()) {
                provider.deployDefinitions(systemId);
            }
        }
    }

    public void deployNewSite(JahiaSite site, JahiaUser user) throws RepositoryException {
        JCRStoreProvider provider = sessionFactory.getMountPoints().get("/");
        provider.deployNewSite(site, user);
    }

    public void deployExternalUser(String username, String providerName) throws RepositoryException {
        JCRStoreProvider provider = sessionFactory.getMountPoints().get("/");
        provider.deployExternalUser(username, providerName);
    }

    public void export(String path, ContentHandler ch, JahiaUser user) {
        sessionFactory.getProvider(path).export(path, ch, user);
    }

    public List<JCRNodeWrapper> getUserFolders(String site, JahiaUser user) {
        List<JCRNodeWrapper> r = new ArrayList<JCRNodeWrapper>();
        for (JCRStoreProvider storeProvider : sessionFactory.getMountPoints().values()) {
            try {
                r.addAll(storeProvider.getUserFolders(site, user));
            } catch (RepositoryException e) {
                logger.warn("Error when querying repository", e);
            }
        }
        return r;
    }

    public List<JCRNodeWrapper> getImportDropBoxes(String site, JahiaUser user) {
        List<JCRNodeWrapper> r = new ArrayList<JCRNodeWrapper>();
        for (JCRStoreProvider storeProvider : sessionFactory.getMountPoints().values()) {
            try {
                r.addAll(storeProvider.getImportDropBoxes(site, user));
            } catch (RepositoryException e) {
                logger.warn("Error when querying repository", e);
            }
        }
        return r;
    }

    public List<JCRNodeWrapper> getSiteFolders(String site, JahiaUser user) {
        List<JCRNodeWrapper> r = new ArrayList<JCRNodeWrapper>();
        for (JCRStoreProvider storeProvider : sessionFactory.getMountPoints().values()) {
            try {
                r.addAll(storeProvider.getSiteFolders(site, user));
            } catch (RepositoryException e) {
                logger.warn("Error when querying repository", e);
            }
        }
        return r;
    }

    public JCRNodeWrapper decorate(JCRNodeWrapper w) {
        try {
            for (String type : decorators.keySet()) {
                if (w.isNodeType(type)) {
                    String className = decorators.get(type);
                    try {
                        return (JCRNodeWrapper) Class.forName(className).getConstructor(JCRNodeWrapper.class).newInstance(w);
                    } catch (Exception e) {
                        logger.error("Cannot decorate node", e);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e);
        }
        return w;
    }

     /**
     * @deprecated Use getCurrentUserSession().getNode()
     */
    public JCRNodeWrapper getFileNode(String path, JahiaUser user) {
        throw new UnsupportedOperationException("getFileNode: "+path);
//        // Todo Suppress this method
    }
}
