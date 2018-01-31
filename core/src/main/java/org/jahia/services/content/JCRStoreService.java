/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.JahiaService;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.jahia.services.content.decorator.validation.JCRNodeValidator;
import org.jahia.services.content.interceptor.InterceptorChain;
import org.jahia.services.content.interceptor.PropertyInterceptor;
import org.jahia.services.content.nodetypes.JahiaCndWriter;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.NodeTypesDBServiceImpl;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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
    private JCRStoreProviderChecker providerChecker;

    private Map<String, List<DefaultEventListener>> listeners;

    private NodeTypesDBServiceImpl nodeTypesDBService;

    private final Properties deploymentProperties = new Properties() {
        @Override
        public synchronized Enumeration<Object> keys() {
            return new Vector(new TreeSet<>(keySet())).elements();
        }
    };

    private final List<String> initializedSystemIds = new ArrayList<>();

    private JCRSessionFactory sessionFactory;

    public void setProviderChecker(JCRStoreProviderChecker providerChecker) {
        this.providerChecker = providerChecker;
    }

    public JCRStoreProviderChecker getProviderChecker() {
        return providerChecker;
    }

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
                    Query query = session.getProviderSession(session.getNode("/").getProvider()).getWorkspace().getQueryManager().createQuery(
                            JCRStoreProvider.SELECT_ALL_MOUNT_POINTS, Query.JCR_SQL2);
                    QueryResult queryResult = query.execute();
                    NodeIterator queryResultNodes = queryResult.getNodes();
                    while (queryResultNodes.hasNext()) {
                        Node node = (Node) queryResultNodes.next();
                        JCRNodeWrapper jcrNodeWrapper = session.getNodeByIdentifier(node.getIdentifier());
                        if (jcrNodeWrapper instanceof JCRMountPointNode && externalProviderFactory.getNodeTypeName().equals(jcrNodeWrapper.getPrimaryNodeTypeName())) {
                            final JCRMountPointNode jcrMountPointNode = (JCRMountPointNode) jcrNodeWrapper;
                            if (jcrMountPointNode.getMountStatus() == JCRMountPointNode.MountStatus.mounted) {
                                JCRNodeWrapper mountPointNode = jcrMountPointNode.getVirtualMountPointNode();
                                final JCRStoreProvider provider = externalProviderFactory.mountProvider(mountPointNode);
                                if (!provider.isAvailable(true)) {
                                    logger.warn("Issue while trying to mount an external provider (" + mountPointNode.getPath()
                                            + ") upon startup, all references to file coming from this mount won't be available until it is fixed. If you migrating from Jahia 6.6 this might be normal until the migration scripts have been completed.");
                                    jcrMountPointNode.setMountStatus(JCRMountPointNode.MountStatus.waiting);
                                    session.save();
                                    providerChecker.checkPeriodically(provider);
                                }
                            } else if (jcrMountPointNode.getMountStatus() == JCRMountPointNode.MountStatus.waiting) {
                                jcrMountPointNode.setMountStatus(JCRMountPointNode.MountStatus.mounted);
                                session.save();
                            }
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
                        Query query = session.getProviderSession(session.getNode("/").getProvider()).getWorkspace().getQueryManager().createQuery(
                                JCRStoreProvider.SELECT_ALL_MOUNT_POINTS, Query.JCR_SQL2);
                        QueryResult queryResult = query.execute();
                        NodeIterator queryResultNodes = queryResult.getNodes();
                        while (queryResultNodes.hasNext()) {
                            Node node = (Node) queryResultNodes.next();
                            JCRNodeWrapper jcrNodeWrapper = session.getNodeByIdentifier(node.getIdentifier());
                            if (jcrNodeWrapper instanceof JCRMountPointNode
                                    && jcrNodeWrapper.getPrimaryNodeTypeName().equals(externalProviderFactory.getNodeTypeName())) {
                                providerChecker.remove(jcrNodeWrapper.getIdentifier());
                                JCRStoreProvider provider = ((JCRMountPointNode) jcrNodeWrapper).getMountProvider();
                                if (provider != null) {
                                    provider.stop();
                                }
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

    /**
     * Retrieves all mount points with the given status or all mount points if no status is specified.
     *
     * @param status <code>null</code> to retrieve all mount points or the status that the mount points must have to be retrieved
     * @return a NodeIterator of the mount points matching the given status or all mount points if no status was specified
     */
    public NodeIterator getKnownMountPointsWithStatus(JCRMountPointNode.MountStatus status) throws RepositoryException {
        final String sql = status == null ? JCRStoreProvider.SELECT_ALL_MOUNT_POINTS : JCRStoreProvider.SELECT_ALL_MOUNT_POINTS + " as mount where ["
                + JCRMountPointNode.MOUNT_STATUS_PROPERTY_NAME + "] = '" + status.name() + "'";
        Query query = getSessionFactory().getSystemSession().getWorkspace().getQueryManager().createQuery(sql, Query.JCR_SQL2);
        QueryResult queryResult = query.execute();
        return queryResult.getNodes();
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

    /**
     * Deploy definitions in all providers and store them in database
     * @param systemId
     * @throws IOException
     * @throws RepositoryException in case of JCR-related errors
     */
    public void deployDefinitions(String systemId) throws IOException, RepositoryException {
        deployDefinitions(systemId, null, -1);
    }

    /**
     * Deploy definitions in all providers and store them in database
     * @param systemId
     * @param moduleVersion
     * @param lastModified
     * @throws IOException
     * @throws RepositoryException in case of JCR-related errors
     */
    public void deployDefinitions(String systemId, String moduleVersion, long lastModified) throws IOException, RepositoryException {
        registerNamespaces();

        for (JCRStoreProvider provider : sessionFactory.getProviders().values()) {
            if (provider.canRegisterCustomNodeTypes()) {
                provider.registerNamespaces();
                provider.deployDefinitions(systemId);
            }
        }

        logger.info("Added {} definitions, updating database cnd", systemId);

        synchronized (deploymentProperties) {
            // If deployment goes well, store deployed definitions in DB
            if (moduleVersion != null) {
                deploymentProperties.put(systemId + ".version", moduleVersion);
            }
            if (lastModified > -1) {
                deploymentProperties.put(systemId + ".lastModified", Long.toString(lastModified));
            }
            final StringWriter out = new StringWriter();
            new JahiaCndWriter(NodeTypeRegistry.getInstance().getNodeTypes(systemId), NodeTypeRegistry.getInstance().getNamespaces(), out);
            nodeTypesDBService.saveCndFile(systemId + ".cnd", out.toString(), deploymentProperties);
        }
    }

    public void undeployDefinitions(String systemId) throws IOException, RepositoryException {
        for (JCRStoreProvider provider : sessionFactory.getProviders().values()) {
            if (provider.canRegisterCustomNodeTypes()) {
                provider.undeployDefinitions(systemId);
            }
        }

        logger.info("Removing {} definitions, updating database cnd", systemId);

        synchronized (deploymentProperties) {
            deploymentProperties.remove(systemId + ".version");
            deploymentProperties.remove(systemId + ".lastModified");
            nodeTypesDBService.saveCndFile(systemId + ".cnd", null, deploymentProperties);
        }
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
                final Session session = getSessionFactory().getSystemSession(null, null, ws, null);
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

    public void setNodeTypesDBService(NodeTypesDBServiceImpl nodeTypesDBService) {
        this.nodeTypesDBService = nodeTypesDBService;
    }

    public void start() throws JahiaInitializationException {
        try {
            initPropertiesFile();
            initNodeTypeRegistry();
            reloadNodeTypeRegistry();
            initObservers(listeners);
        } catch (Exception e) {
            logger.error("Repository init error", e);
        }
    }

    public void stop() throws JahiaException {
    }

    private void initPropertiesFile() throws IOException {
        try {
            final String propertyFile = nodeTypesDBService.readDefinitionPropertyFile();
            if (propertyFile != null) {
                deploymentProperties.load(new StringReader(propertyFile));
            }
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
    }

    private void initNodeTypeRegistry() throws ParseException, IOException, RepositoryException {
        if (settingsBean.isProcessingServer()) {
            for (Map.Entry<String, File> entry : NodeTypeRegistry.getSystemDefinitionsFiles().entrySet()) {
                String systemId = entry.getKey();
                File file = entry.getValue();
                NodeTypeRegistry.getInstance().addDefinitionsFile(file, systemId);
                if (isLatestDefinitions(systemId, new ModuleVersion(Jahia.VERSION), file.lastModified())) {
                    initializedSystemIds.add(systemId);
                    deployDefinitions(systemId, Jahia.VERSION, file.lastModified());
                }
            }
        }
    }

    public void reloadNodeTypeRegistry() throws RepositoryException {
        List<String> filesList = new ArrayList<>();
        List<String> remfiles;

        NodeTypeRegistry instance = NodeTypeRegistry.getInstance();

        logger.info("Loading all CNDs from DB ..");
        remfiles = new ArrayList<>(nodeTypesDBService.getFilesList());
        List<String> reloadedSystemIds = new ArrayList<>();
        while (!remfiles.isEmpty() && !remfiles.equals(filesList)) {
            filesList = new ArrayList<>(remfiles);
            remfiles.clear();
            for (final String file : filesList) {
                try {
                    if (file.endsWith(".cnd")) {
                        final String cndFile = nodeTypesDBService.readFile(file);
                        final String systemId = StringUtils.substringBeforeLast(file, ".cnd");
                        if (!initializedSystemIds.contains(systemId)) {
                            logger.debug("Loading CND : {}" , file);
                            instance.addDefinitionsFile(new ByteArrayResource(cndFile.getBytes("UTF-8"), file), systemId);
                        }
                        reloadedSystemIds.add(systemId);
                    }
                } catch (ParseException | NoSuchNodeTypeException e) {
                    logger.debug(file + " cannot be parsed, reorder later");
                    remfiles.add(file);
                } catch (IOException e) {
                    logger.error("Cannot parse CND file from DB : "+file,e);
                }
            }
        }

        List<String> systemIds = NodeTypeRegistry.getInstance().getSystemIds();
        systemIds.removeAll(reloadedSystemIds);
        for (String systemId : systemIds) {
            NodeTypeRegistry.getInstance().unregisterNodeTypes(systemId);
        }
        if (!remfiles.isEmpty()) {
            logger.error("Cannot read CND from : "+remfiles);
        }

        registerNamespaces();
    }

    private void registerNamespaces() {
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
        } catch (RepositoryException e) {
            logger.error("Unable to register namespaces", e);
        }
    }

    public boolean isLatestDefinitions(String systemId, ModuleVersion version, long lastModified) {
        if (version != null) {
            String key = systemId + ".version";
            if (deploymentProperties.containsKey(key)) {
                ModuleVersion lastDeployed = new ModuleVersion(deploymentProperties.getProperty(key));
                if (lastDeployed.compareTo(version) > 0) {
                    logger.info("Previously deployed " + systemId + " version was : "+deploymentProperties.getProperty(key) + ", ignoring version "+systemId + " / " + version + " / " + lastModified);
                    return false;
                }
            }
        }
        String key2 = systemId + ".lastModified";
        if (deploymentProperties.containsKey(key2)) {
            long lastDeployed = (long) Long.parseLong(deploymentProperties.getProperty(key2));
            if (lastDeployed >= lastModified) {
                logger.info("Previously deployed " + systemId + " was done at : " + new Date(lastDeployed) + ", ignoring version "+systemId + " / " + version + " / " + new Date(lastModified));
                return false;
            }
        }

        return true;
    }

    public List<String> getInitializedSystemIds() {
        return initializedSystemIds;
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
    
    @SuppressWarnings("unchecked")
    public void setValidators(Map<String, String> validators) {
        if (validators == null) {
            return;
        }
        for (Map.Entry<String, String> validator : validators.entrySet()) {
            try {
                addValidator(validator.getKey(),
                        (Class<? extends JCRNodeValidator>) Class.forName(validator.getValue()));
            } catch (ClassNotFoundException e) {
                logger.error("Unable to find the validator class " + validator.getClass() + " defined for node type "
                        + validator.getKey(), e);
            }
        }
    }
}
