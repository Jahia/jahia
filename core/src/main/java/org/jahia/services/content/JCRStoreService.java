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

package org.jahia.services.content;

import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.JahiaService;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.interceptor.PropertyInterceptor;
import org.jahia.services.content.interceptor.InterceptorChain;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.observation.ObservationManager;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a Jahia service, which manages the delegation of JCR store related deployment
 * and export functions to the right <code>JCRStoreProvider</code>.
 *
 * @author toto
 */
public class JCRStoreService extends JahiaService implements JahiaAfterInitializationService {
    
    static private JCRStoreService instance = null;

    private static Logger logger = LoggerFactory.getLogger(JCRStoreService.class);

    public static JCRStoreService getInstance() {
        if (instance == null) {
            synchronized (JCRStoreService.class) {
                if (instance == null) {
                    instance = new JCRStoreService();
                }
            }
        }
        return instance;
    }

    private Map<String, String> decorators = new HashMap<String, String>();
    private Map<String, Constructor<?>> decoratorCreators = new HashMap<String, Constructor<?>>();
    private InterceptorChain interceptorChain;
    private Map<String,ExternalProvider> externalProviders = new HashMap<String, ExternalProvider>();
    private List<PropertyInterceptor> interceptors = new LinkedList<PropertyInterceptor>();
    private Set<String> noValidityCheckTypes = new HashSet<String>();
    
    private Map<String,List<DefaultEventListener>> listeners;

    private JCRSessionFactory sessionFactory;

    protected JCRStoreService() {
        super();
    }

    /**
     * Adds an interceptor to the chain.
     * 
     * @param index index at which the specified element is to be inserted.
     * @param interceptor the interceptor instance
     */
    public void addInterceptor(int index, PropertyInterceptor interceptor) {
        this.interceptors.add(index, interceptor);
        interceptorChain = null;
    }

    /**
     * Adds an interceptor to the chain.
     * 
     * @param interceptor the interceptor instance
     */
    public void addInterceptor(PropertyInterceptor interceptor) {
        this.interceptors.add(interceptor);
        interceptorChain = null;
    }

    public void addExternalProvider(String key, ExternalProvider externalProvider) {
        this.externalProviders.put(key, externalProvider);
    }

    public JCRNodeWrapper decorate(JCRNodeWrapper w) {
        try {
            Constructor<?> creator = decoratorCreators.get(w.getPrimaryNodeTypeName());
            if (creator == null) {
                for (String type : decoratorCreators.keySet()) {
                    if (w.isNodeType(type)) {
                        creator = decoratorCreators.get(type);
                        break;
                    }
                }
            }
            if (creator != null) {
                try {
                    return (JCRNodeWrapper) creator.newInstance(w);
                } catch (Exception e) {
                    logger.error("Cannot decorate node", e);
                }
            }
        } catch (RepositoryException e) {
            logger.error("Error while decorating node", e);
        }
        return w;
    }

    public void deployDefinitions(String systemId) {
        for (JCRStoreProvider provider : sessionFactory.getProviders().values()) {
            if (provider.canRegisterCustomNodeTypes()) {
                provider.deployDefinitions(systemId);
            }
        }
    }

    public void deployExternalUser(JahiaUser jahiaUser) throws RepositoryException {
        JCRStoreProvider provider = sessionFactory.getMountPoints().get("/");
        provider.deployExternalUser(jahiaUser);
        ServicesRegistry.getInstance().getJahiaUserManagerService().updateCache(jahiaUser);
    }

    public Map<String, String> getDecorators() {
        return decorators;
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

    public InterceptorChain getInterceptorChain() {
        if (interceptorChain == null) {
            interceptorChain = new InterceptorChain();
            interceptorChain.setInterceptors(interceptors);
        }
        
        return interceptorChain;
    }
    
    public Map<String, List<DefaultEventListener>> getListeners() {
        return listeners;
    }
    
    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public JCRNodeWrapper getUserFolder(JahiaUser user) throws RepositoryException {
        return sessionFactory.getMountPoints().get("/").getUserFolder(user);
    }

    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JahiaPrivilegeRegistry.init(session);
                    return null;
                }
            });
        } catch (RepositoryException e) {
            throw new JahiaInitializationException("Cannot register permissions",e);
        }
    }

    private void initObservers(Map<String, List<DefaultEventListener>> listeners)
            throws RepositoryException {
        if (listeners != null) {
            for (String ws : listeners.keySet()) {
                List<DefaultEventListener> l = listeners.get(ws);

                // This session must not be released
                final Session session = getSessionFactory().getSystemSession(null, ws);
                final Workspace workspace = session.getWorkspace();

                ObservationManager observationManager = workspace.getObservationManager();
                for (DefaultEventListener listener : l) {
                    if (listener.getEventTypes() > 0) {
                        listener.setWorkspace(ws);
                        observationManager.addEventListener(listener, listener.getEventTypes(),
                                listener.getPath(), listener.isDeep(), listener.getUuids(),
                                listener.getNodeTypes(), false);
                    } else {
                        logger.info("Skipping listener {} as it has no event types configured.",
                                listener.getClass().getName());
                    }
                }
            }
        }
    }

    /**
     * Removes the specififed interceptor from the chain.
     * 
     * @param interceptor
     *            the interceptor instance
     */
    public void removeInterceptor(PropertyInterceptor interceptor) {
        if (this.interceptors.remove(interceptor)) {
            interceptorChain = null;
        }
    }

    public void setDecorators(Map<String, String> decorators) {
        this.decorators = decorators;
        if (decorators != null) {
            for (Map.Entry<String, String> decorator : decorators.entrySet()) {
                try {
                    decoratorCreators.put(decorator.getKey(), Class.forName(decorator.getValue())
                            .getConstructor(JCRNodeWrapper.class));
                } catch (Exception e) {
                    logger.error("Unable to instanciate decorator: " + decorator.getValue(), e);
                }
            }
        }
    }

    public void setInterceptors(List<PropertyInterceptor> interceptors) {
        this.interceptors.addAll(interceptors);
        interceptorChain = null;
    }

    public Map<String, ExternalProvider> getExternalProviders() {
        return externalProviders;
    }

    public void setListeners(Map<String, List<DefaultEventListener>> listeners) {
        this.listeners = listeners;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void start() throws JahiaInitializationException {
        try {
            NamespaceRegistry nsRegistry = sessionFactory.getNamespaceRegistry();
            NodeTypeRegistry ntRegistry = NodeTypeRegistry.getInstance();
            Set<String> prefixes = ImmutableSet.copyOf(nsRegistry.getPrefixes());
            for (Map.Entry<String, String> namespaceEntry : ntRegistry.getNamespaces().entrySet()) {
                if (!prefixes.contains(namespaceEntry.getKey())) {
                    nsRegistry
                            .registerNamespace(namespaceEntry.getKey(), namespaceEntry.getValue());
                }
            }

            initObservers(listeners);
        } catch (Exception e) {
            logger.error("Repository init error", e);
        }
    }

    public void stop() throws JahiaException {
    }

    public Set<String> getNoValidityCheckTypes() {
        return noValidityCheckTypes;
    }

    public void setNoValidityCheckTypes(Set<String> noValidityCheckTypes) {
        this.noValidityCheckTypes = noValidityCheckTypes;
    }
}
