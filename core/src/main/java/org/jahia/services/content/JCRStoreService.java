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
import org.jahia.services.content.interceptor.PropertyInterceptor;
import org.jahia.services.content.interceptor.InterceptorChain;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.xml.sax.ContentHandler;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.observation.ObservationManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a Jahia service, which manages the delegation of JCR store related deployment
 * and export functions to the right <code>JCRStoreProvider</code>.
 *
 * @author toto
 */
public class JCRStoreService extends JahiaService  {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JCRStoreService.class);

    private Map<String, String> decorators = new HashMap<String, String>();

    private List<PropertyInterceptor> interceptors = new ArrayList<PropertyInterceptor>();
    private InterceptorChain interceptorChain;

    static private JCRStoreService instance = null;
    private JCRSessionFactory sessionFactory;

    private Map<String,List<DefaultEventListener>> listeners;

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

            initObservers(listeners);
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

    public void setListeners(Map<String, List<DefaultEventListener>> listeners) {
        this.listeners = listeners;
    }

    public void setInterceptors(List<PropertyInterceptor> interceptors) {
        this.interceptors = interceptors;
        interceptorChain = new InterceptorChain();
        interceptorChain.setInterceptors(interceptors);
    }

    public InterceptorChain getInterceptorChain() {
        return interceptorChain;
    }

    public Map<String, List<DefaultEventListener>> getListeners() {
        return listeners;
    }

    private void initObservers(Map<String, List<DefaultEventListener>> listeners) throws RepositoryException {
        if (listeners != null) {
            for (String ws : listeners.keySet()) {
                List<DefaultEventListener> l = listeners.get(ws);

                // This session must not be released
                final Session session = getSessionFactory().getSystemSession(null,ws);
                final Workspace workspace = session.getWorkspace();

                ObservationManager observationManager = workspace.getObservationManager();
                for (DefaultEventListener listener : l) {
                    listener.setWorkspace(ws);
                    observationManager.addEventListener(listener, listener.getEventTypes(), listener.getPath(), true, null, listener.getNodeTypes(), false);
                }
            }
        }
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

    public List<JCRNodeWrapper> getSiteFolders(String site) {
        List<JCRNodeWrapper> r = new ArrayList<JCRNodeWrapper>();
        for (JCRStoreProvider storeProvider : sessionFactory.getMountPoints().values()) {
            try {
                r.addAll(storeProvider.getSiteFolders(site));
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
            logger.error("Error while decorating node", e);
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
