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
package org.jahia.services.content;

import com.google.common.collect.ImmutableSet;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.JahiaService;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.jahia.services.content.decorator.validation.JCRNodeValidator;
import org.jahia.services.content.interceptor.InterceptorChain;
import org.jahia.services.content.interceptor.PropertyInterceptor;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a Jahia service, which manages the delegation of JCR store related deployment
 * and export functions to the right <code>JCRStoreProvider</code>.
 *
 * @author toto
 */
public class JCRStoreService extends JahiaService implements JahiaAfterInitializationService {

    private static Logger logger = LoggerFactory.getLogger(JCRStoreService.class);

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final JCRStoreService INSTANCE = new JCRStoreService();
    }

    public static JCRStoreService getInstance() {
        return Holder.INSTANCE;
    }

    private Map<String, Class<? extends JCRNodeDecorator>> decorators = new ConcurrentHashMap<String, Class<? extends JCRNodeDecorator>>();
    private Map<String, Constructor<?>> decoratorCreators = new ConcurrentHashMap<String, Constructor<?>>();
    private InterceptorChain interceptorChain;
    private Map<String, ProviderFactory> providerFactories = new ConcurrentHashMap<String, ProviderFactory>();
    private List<PropertyInterceptor> interceptors = new LinkedList<PropertyInterceptor>();
    private Set<String> noValidityCheckTypes = new HashSet<String>();
    private Set<String> noLanguageValidityCheckTypes = new HashSet<String>();
    private Map<String, Class<? extends JCRNodeValidator>> validators = new ConcurrentHashMap<String, Class<? extends JCRNodeValidator>>();
    private Map<String, Constructor<?>> validatorCreators = new ConcurrentHashMap<String, Constructor<?>>();

    private Map<String, List<DefaultEventListener>> listeners;

    private JCRSessionFactory sessionFactory;

    private JCRStoreService() {
        super();
    }

    /**
     * Adds an interceptor to the chain.
     *
     * @param index       index at which the specified element is to be inserted.
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

    public void addProviderFactory(String nodeType, final ProviderFactory externalProviderFactory) {
        this.providerFactories.put(nodeType, externalProviderFactory);

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Query query = session.getWorkspace().getQueryManager().createQuery(
                            "select * from [" + externalProviderFactory.getNodeTypeName() + "] as mount", Query.JCR_SQL2);
                    QueryResult queryResult = query.execute();
                    NodeIterator queryResultNodes = queryResult.getNodes();
                    while (queryResultNodes.hasNext()) {
                        JCRNodeWrapper mountPointNodeWrapper = (JCRNodeWrapper) queryResultNodes.next();
                        if (mountPointNodeWrapper instanceof JCRMountPointNode) {
                            JCRMountPointNode mountPointNode = (JCRMountPointNode) mountPointNodeWrapper;
                            if (!mountPointNode.checkMountPointValidity()) {
                                logger.warn("Issue while trying to mount an external provider (" + mountPointNodeWrapper.getPath() + ") upon startup, all references " +
                                        "to file coming from this mount won't be available until it is fixed. If you migrating from Jahia 6.6 this might be normal until the migration scripts have been completed.");
                                continue;
                            }
                        }
                        try {
                            mountPointNodeWrapper.getNodes();
                        } catch (RepositoryException e) {
                            logger.warn(
                                    "Issue while trying to mount an external provider (" + mountPointNodeWrapper.getPath() + ") upon startup, all references " +
                                            "to file coming from this mount won't be available until it is fixed. If you migrating from Jahia 6.6 this might be normal until the migration scripts have been completed", e
                            );
                        }
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Cannot mount provider " + nodeType, e);
        }
    }

    public void removeProviderFactory(String nodeType, final ProviderFactory externalProviderFactory) {
        if (this.providerFactories.get(nodeType) == externalProviderFactory) {
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                    @Override
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        Query query = session.getWorkspace().getQueryManager().createQuery(
                                "select * from [" + externalProviderFactory.getNodeTypeName() + "] as mount", Query.JCR_SQL2);
                        QueryResult queryResult = query.execute();
                        NodeIterator queryResultNodes = queryResult.getNodes();
                        while (queryResultNodes.hasNext()) {
                            JCRNodeWrapper mountPointNodeWrapper = (JCRNodeWrapper) queryResultNodes.next();
                            JCRStoreProvider provider = JCRSessionFactory.getInstance().getMountPoints().get(mountPointNodeWrapper.getPath());
                            if (provider != null && provider.isDynamicallyMounted()) {
                                provider.stop();
                            }
                        }
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Cannot unmount provider " + nodeType, e);
            }
            providerFactories.remove(nodeType);
        }
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

    public void undeployDefinitions(String systemId) {
        for (JCRStoreProvider provider : sessionFactory.getProviders().values()) {
            if (provider.canRegisterCustomNodeTypes()) {
                provider.undeployDefinitions(systemId);
            }
        }
    }

    public void deployExternalUser(JahiaUser jahiaUser) throws RepositoryException {
        JCRStoreProvider provider = sessionFactory.getMountPoints().get("/");
        provider.deployExternalUser(jahiaUser);
        ServicesRegistry.getInstance().getJahiaUserManagerService().updateCache(jahiaUser);
    }

    public Map<String, Class<? extends JCRNodeDecorator>> getDecorators() {
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
        //
    }

    private void initObservers(Map<String, List<DefaultEventListener>> listeners)
            throws RepositoryException {
        if (listeners != null) {
            for (String ws : listeners.keySet()) {
                List<DefaultEventListener> l = listeners.get(ws);

                // This session must not be released
                final Session session = getSessionFactory().getSystemSession(null, ws);
                try {
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
                } finally {
                    session.logout();
                }
            }
        }
    }

    /**
     * Removes the specified interceptor from the chain.
     *
     * @param interceptor the interceptor instance
     */
    public void removeInterceptor(PropertyInterceptor interceptor) {
        if (this.interceptors.remove(interceptor)) {
            interceptorChain = null;
        }
    }

    @SuppressWarnings("unchecked")
    public void setDecorators(Map<String, String> decorators) {
        if (!this.decorators.isEmpty()) {
            throw new RuntimeException("setDecorators should not be called after initialization of system, use addDecorator instead");
        }
        if (decorators != null) {
            for (Map.Entry<String, String> decorator : decorators.entrySet()) {
                try {
                    this.decorators.put(decorator.getKey(), (Class<? extends JCRNodeDecorator>) Class.forName(decorator.getValue()));
                    decoratorCreators.put(decorator.getKey(), Class.forName(decorator.getValue())
                            .getConstructor(JCRNodeWrapper.class));
                } catch (Exception e) {
                    logger.error("Unable to instantiate decorator: " + decorator.getValue(), e);
                }
            }
        }
    }

    public void addDecorator(String nodeType, Class<? extends JCRNodeDecorator> decoratorClass) {
        try {
            if (!NodeTypeRegistry.getInstance().getNodeType(nodeType).isMixin()) {
                if (decorators == null) {
                    decorators = new ConcurrentHashMap<String, Class<? extends JCRNodeDecorator>>();
                }
                decorators.put(nodeType, decoratorClass);
                try {
                    decoratorCreators.put(nodeType, decoratorClass.getConstructor(JCRNodeWrapper.class));
                } catch (Exception e) {
                    logger.error("Unable to instantiate decorator: " + decoratorClass, e);
                }
            } else {
                logger.error("It is impossible to decorate a mixin (" + nodeType + "), only primary node type can be decorated");
            }
        } catch (NoSuchNodeTypeException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void removeDecorator(String nodeType) {
        decorators.remove(nodeType);
        decoratorCreators.remove(nodeType);
    }

    public void setInterceptors(List<PropertyInterceptor> interceptors) {
        this.interceptors.addAll(interceptors);
        interceptorChain = null;
    }

    public Map<String, ProviderFactory> getProviderFactories() {
        return providerFactories;
    }

    public void setProviderFactories(Map<String, ProviderFactory> providerFactories) {
        this.providerFactories = providerFactories;
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

    public Set<String> getNoLanguageValidityCheckTypes() {
        return noLanguageValidityCheckTypes;
    }

    public void setNoLanguageValidityCheckTypes(
            Set<String> noLanguageValidityCheckTypes) {
        this.noLanguageValidityCheckTypes = noLanguageValidityCheckTypes;
    }

    public void addValidator(String nodeType, Class<? extends JCRNodeValidator> validatorClass) {
        if (validators == null) {
            validators = new ConcurrentHashMap<String, Class<? extends JCRNodeValidator>>();
        }
        validators.put(nodeType, validatorClass);
        try {
            validatorCreators.put(nodeType, validatorClass.getConstructor(JCRNodeWrapper.class));
        } catch (Exception e) {
            logger.error("Unable to instantiate decorator: " + validatorClass, e);
        }
    }

    public void removeValidator(String nodeType) {
        if (validators == null) {
            validators = new ConcurrentHashMap<String, Class<? extends JCRNodeValidator>>();
        }
        validators.remove(nodeType);
        validatorCreators.remove(nodeType);
    }

    public Map<String, Constructor<?>> getValidators() {
        return validatorCreators;
    }
}
