/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.query.JahiaQueryObjectModelImpl;
import org.apache.jackrabbit.core.query.lucene.JahiaLuceneQueryFactoryImpl;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry;
import org.apache.jackrabbit.core.state.StaleItemStateException;
import org.apache.jackrabbit.rmi.server.ServerAdapterFactory;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.content.decorator.JCRFrozenNodeAsRegular;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.patches.GroovyPatcher;
import org.jahia.utils.LuceneUtils;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.observation.Event;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.rmi.Naming;
import java.util.*;
import java.util.Map.Entry;

import static org.apache.jackrabbit.core.security.JahiaLoginModule.GUEST;
import static org.apache.jackrabbit.core.security.JahiaLoginModule.SYSTEM;

/**
 * A store provider to handle different back-end stores within a site. There are multiple
 * subclasses for the different repository vendors.
 * <p/>
 * The main and default repository in Jahia is based on the {@link org.jahia.services.content.impl.jackrabbit.JackrabbitStoreProvider},
 * but you can have different repositories mounted, which are based on other store providers.
 *
 * @author toto
 */
public class JCRStoreProvider implements Comparable<JCRStoreProvider> {

    static final String SELECT_ALL_MOUNT_POINTS = "select * from [" + Constants.JAHIANT_MOUNTPOINT + "]";
    private static final Logger logger = LoggerFactory.getLogger(JCRStoreProvider.class);

    private boolean defaultProvider;
    private String key;
    private String mountPoint;
    private String webdavPath;
    private String relativeRoot = "";

    private String repositoryName;
    private String factory;
    private String url;


    protected String systemUser;
    protected String systemPassword;
    private SimpleCredentials systemCredentials;

    protected String guestUser;
    protected String guestPassword;
    private SimpleCredentials guestCredentials;

    protected String authenticationType = null;

    protected String rmibind;

    private JahiaUserManagerService userManagerService;
    private JahiaGroupManagerService groupManagerService;
    private JahiaSitesService sitesService;

    private JCRStoreService service;

    protected JCRSessionFactory sessionFactory;
    protected volatile Repository repo = null;

    private boolean mainStorage = false;
    private boolean isDynamicallyMounted = false;
    private boolean initialized = false;

    private boolean providesDynamicMountPoints;

    private Boolean readOnly = null;
    private Boolean versioningAvailable = null;
    private Boolean lockingAvailable = null;
    private Boolean searchAvailable = null;
    private Boolean updateMixinAvailable = null;
    private Boolean slowConnection = false;

    private final Object syncRepoInit = new Object();

    private GroovyPatcher groovyPatcher;

    private boolean registerObservers = true;

    private boolean observersUseRelativeRoot = true;

    private Map<String, JCRSessionWrapper> observerSessions;

    private String mountStatusMessage;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
        defaultProvider = "/".equals(mountPoint);
    }

    public String getWebdavPath() {
        return webdavPath;
    }

    public String getRelativeRoot() {
        return relativeRoot;
    }

    public void setRelativeRoot(String relativeRoot) {
        this.relativeRoot = relativeRoot;
    }

    public int getDepth() {
        if (defaultProvider) {
            return 0;
        }
        return Patterns.SLASH.split(mountPoint).length - 1;
    }

    public void setWebdavPath(String webdavPath) {
        this.webdavPath = webdavPath;
    }

    public String getHttpPath() {
        return Jahia.getContextPath() + "/files";
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSystemUser(String user) {
        this.systemUser = user;
        if (authenticationType == null) {
            authenticationType = "shared";
        }
    }

    public void setSystemPassword(String password) {
        this.systemPassword = password;
    }

    public void setGuestUser(String user) {
        this.guestUser = user;
    }

    public void setGuestPassword(String password) {
        this.guestPassword = password;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getRmibind() {
        return rmibind;
    }

    public void setRmibind(String rmibind) {
        this.rmibind = rmibind;
    }

    public JahiaUserManagerService getUserManagerService() {
        return userManagerService;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    public JahiaGroupManagerService getGroupManagerService() {
        return groupManagerService;
    }

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public JahiaSitesService getSitesService() {
        return sitesService;
    }

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    public JCRStoreService getService() {
        return service;
    }

    public void setService(JCRStoreService service) {
        this.service = service;
    }

    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @deprecated without no replacement
     */
    @Deprecated
    public void setSessionKeepAliveCheckInterval(long sessionKeepAliveCheckInterval) {
        // do nothing
    }

    public GroovyPatcher getGroovyPatcher() {
        return groovyPatcher;
    }

    public void setGroovyPatcher(GroovyPatcher groovyPatcher) {
        this.groovyPatcher = groovyPatcher;
    }

    public void start() throws JahiaInitializationException {
        start(true);
    }

    /**
     * Starts this provider if possible, checking its availability before attempting the starting procedure if so requested.
     *
     * @param checkAvailability whether or not to check the availability before attempting the starting procedure
     * @return <code>true</code> if this provider is available or availability checking was not requested, <code>false</code> otherwise.
     * @throws JahiaInitializationException
     */
    public boolean start(boolean checkAvailability) throws JahiaInitializationException {
        String tmpAuthenticationType = authenticationType;
        try {
            authenticationType = "shared";

            final boolean available = !checkAvailability || isAvailable();

            if (available && !initialized) {
                getSessionFactory().addProvider(this);

                boolean isProcessingServer = SettingsBean.getInstance().isProcessingServer();
                if (isProcessingServer) {
                    initNodeTypes();
                }
                initObservers();
                initialized = true;
                initContent();
                initDynamicMountPoints();

                if (groovyPatcher != null && isProcessingServer) {
                    groovyPatcher.executeScripts("jcrStoreProviderStarted");
                }
            }

            return available;
        } catch (Exception e) {
            logger.error("Couldn't mount provider " + getUrl(), e);
            stop();
            throw new JahiaInitializationException("Couldn't mount provider " + getUrl(), e);
        } finally {
            authenticationType = tmpAuthenticationType;
        }
    }

    /**
     * Same as <code>isAvailable(false)</code>
     *
     * @return whether this provider is available to serve content
     */
    public boolean isAvailable() {
        return isAvailable(false);
    }

    /**
     * Checks whether this provider is available to serve content.
     *
     * @param silent <code>true</code> if the check should be done silently (i.e. without outputting information on the console), <code>false</code> otherwise
     * @return <code>true</code> if this provider can serve content, <code>false</code> otherwise.
     */
    public boolean isAvailable(boolean silent) {
        try {
            checkAvailability();
            mountStatusMessage = null;
            return true;
        } catch (RepositoryException e) {
            mountStatusMessage = e.getCause() != null ? e.getMessage() + ": " + e.getCause().getMessage() : e.getMessage();
            if (!silent) {
                logger.warn("Provider '" + key + "' on mountpoint '" + getMountPoint() + "' is not accessible and will not be available. ", e);
//                if (logger.isDebugEnabled()) {
//                    logger.debug("Provider '" + key + "' is not accessible and will not be available", e);
//                } else {
//                    logger.warn("Provider '{}' on mountpoint '{}' is not accessible and will not be available. Cause: {}",
//                            new Object[] { key, getMountPoint(), mountStatusMessage });
//                }
            }
            return false;
        }
    }

    public void checkAvailability() throws RepositoryException {
        JCRSessionWrapper systemSession = null;
        try {
            systemSession = sessionFactory.getSystemSession();
            final Session providerSession = systemSession.getProviderSession(this);
            providerSession.getRootNode();
        } finally {
            if (systemSession != null) {
                systemSession.logout();
            }
        }
    }

    /**
     * Sets the mount status of this provider to the specified one.
     *
     * @param status the new status of this provider
     */
    public void setMountStatus(final JCRMountPointNode.MountStatus status) {
        setMountStatus(status, null);
    }

    /**
     * Sets the mount status of this provider to the specified one.
     *
     * @param status the new status of this provider
     */
    public void setMountStatus(final JCRMountPointNode.MountStatus status, String message) {
        if (message != null) {
            mountStatusMessage = message;
        }

        if (status != null) {
            if (isDynamicallyMounted()) {
                try {
                    JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                        @Override
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            JCRNodeWrapper node = session.getNodeByIdentifier(getKey());
                            if (node instanceof JCRMountPointNode) {
                                JCRMountPointNode mountPointNode = (JCRMountPointNode) node;
                                mountPointNode.setMountStatus(status);
                                session.save();
                            }
                            return null;
                        }
                    });
                } catch (RepositoryException e) {
                    if (!(e.getCause() instanceof StaleItemStateException)) {
                        logger.error("Error updating mount point status", e);
                    }
                }
            } else {
                stop();
                if (status == JCRMountPointNode.MountStatus.waiting) {
                    getService().getProviderChecker().checkPeriodically(this);
                } else if (status == JCRMountPointNode.MountStatus.mounted) {
                    try {
                        start();
                        isAvailable();
                    } catch (JahiaInitializationException e) {
                        logger.warn("Issue while trying to start an external provider ({}) upon startup" + getMountPoint());
                        getService().getProviderChecker().checkPeriodically(this);
                    }
                }
            }

        }
    }

    public String getMountStatusMessage() {
        return mountStatusMessage;
    }

    public void setMountStatusMessage(String mountStatusMessage) {
        this.mountStatusMessage = mountStatusMessage;
    }

    protected void initNodeTypes() throws RepositoryException, IOException {
        if (canRegisterCustomNodeTypes()) {
            for (String systemId : service.getInitializedSystemIds()) {
                deployDefinitions(systemId);
            }
        }
    }

    protected void initObservers() throws RepositoryException {
        if (!registerObservers) {
            return;
        }
        Set<String> workspaces = service.getListeners().keySet();
        observerSessions = new HashMap<String, JCRSessionWrapper>(workspaces.size());
        for (String ws : workspaces) {
            // This session must not be released
            final JCRSessionWrapper session = getSystemSession(ws);
            observerSessions.put(ws, session);
            final Workspace workspace = session.getProviderSession(this).getWorkspace();

            ObservationManager observationManager = workspace.getObservationManager();
            JCRObservationManagerDispatcher listener = new JCRObservationManagerDispatcher();
            listener.setWorkspace(workspace.getName());
            listener.setMountPoint(mountPoint);
            listener.setRelativeRoot(relativeRoot);
            observationManager.addEventListener(listener, Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_ADDED
                            + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED + Event.NODE_MOVED,
                    observersUseRelativeRoot ? StringUtils.defaultIfEmpty(relativeRoot, "/") : "/", true, null, null,
                    false);
            observerSessions.put(ws, session);
        }
    }

    protected void initContent() throws RepositoryException, IOException {
        if (defaultProvider) {
            JCRSessionWrapper session = service.getSessionFactory().getSystemSession();
            try {
                JCRNodeWrapper rootNode = session.getRootNode();
                if (!rootNode.hasNode("sites")) {
                    rootNode.addMixin("mix:referenceable");

                    JCRContentUtils.importSkeletons("WEB-INF/etc/repository/root.xml,WEB-INF/etc/repository/root-*.xml", "/", session, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW, null);

                    JahiaPrivilegeRegistry.init(session);

//                    rootNode.grantRoles("u:guest", Collections.singleton("visitor"));
//                    rootNode.grantRoles("g:users", Collections.singleton("visitor"));
//                    rootNode.grantRoles("g:administrators", Collections.singleton("administrator"));
                    Node userNode = (Node) session.getItem("/users");
                    NodeIterator nodeIterator = userNode.getNodes();
                    while (nodeIterator.hasNext()) {
                        JCRNodeWrapper node = (JCRNodeWrapper) nodeIterator.next();
                        if (!"guest".equals(node.getName())) {
                            node.grantRoles("u:" + node.getName(), Collections.singleton("owner"));
                        }
                    }
                    session.save();
                } else {
                    JahiaPrivilegeRegistry.init(session);
                }
            } finally {
                session.logout();
            }
        }
    }

    protected void initDynamicMountPoints() {
        if (!providesDynamicMountPoints) {
            return;
        }

        JCRSessionWrapper session = null;
        try {
            session = sessionFactory.getSystemSession();
            List<JCRNodeWrapper> result = queryFolders(session, SELECT_ALL_MOUNT_POINTS);
            for (JCRNodeWrapper mountPointNode : result) {
                if (mountPointNode instanceof JCRMountPointNode) {
                    try {
                        if (mountPointNode.checkValidity()) {
                            logger.info("Registered mount point: " + mountPointNode.getPath());
                        } else {
                            throw new RepositoryException("Couldn't mount dynamic mount point " + mountPointNode.getPath());
                        }
                    } catch (Exception e) {
                        logger.error("Unable to register dynamic mount point for path " + mountPointNode.getPath(), e);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error("Unable to register dynamic mount points", e);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    public void stop() {
        logger.info("Unmounting provider of mount point {}", getMountPoint());
        unregisterObservers();
        getSessionFactory().removeProvider(key);
        rmiUnbind();
        initialized = false;
    }

    protected void rmiUnbind() {
        if (rmibind != null) {
            try {
                Naming.unbind(rmibind);
            } catch (Exception e) {
                logger.warn("Unable to unbind the JCR repository in RMI");
            }
        }
    }

    /**
     * @deprecated with no replacement
     */
    @Deprecated
    public boolean isRunning() {
        return false;
    }

    /**
     * Deploy definitions registered with the given systemId into the underlying repository
     * @param systemId
     * @throws IOException
     * @throws RepositoryException
     */
    public void deployDefinitions(String systemId) throws IOException, RepositoryException {
        // create repository instance
        getRepository();
        JCRSessionWrapper session = sessionFactory.getSystemSession();
        try {
            Workspace workspace = session.getProviderSession(this).getWorkspace();

            try {
                registerCustomNodeTypes(systemId, workspace);
            } catch (RepositoryException e) {
                logger.error("Cannot register nodetypes", e);
                throw e;
            }
            session.save();
        } finally {
            session.logout();
        }
    }

    /**
     * Deploy all namespaces into provider
     *
     * @throws RepositoryException
     */
    public void registerNamespaces() throws RepositoryException {
        JCRSessionWrapper sessionWrapper = getSystemSession();
        try {
            Session s = sessionWrapper.getProviderSession(this);
            NamespaceRegistry providerNamespaceRegistry = s.getWorkspace().getNamespaceRegistry();
            if (providerNamespaceRegistry != null) {
                for (Map.Entry<String, String> namespaceEntry : NodeTypeRegistry.getInstance().getNamespaces().entrySet()) {
                    try {
                        if (providerNamespaceRegistry.getURI(namespaceEntry.getKey()).equals(namespaceEntry.getValue())) {
                            continue;
                        }
                    } catch (NamespaceException ne) {
                        // prfix not yet registered
                    }
                    providerNamespaceRegistry.registerNamespace(namespaceEntry.getKey(), namespaceEntry.getValue());
                }
            }
        } finally {
            sessionWrapper.logout();
        }
    }

    /**
     * Undeploy definitions registered with the given systemId from the underlying repository
     * @param systemId
     * @throws IOException
     * @throws RepositoryException
     */
    public void undeployDefinitions(String systemId) throws IOException, RepositoryException {
        // create repository instance
        getRepository();
        JCRSessionWrapper session = sessionFactory.getSystemSession();
        try {
            Workspace workspace = session.getProviderSession(this).getWorkspace();

            try {
                unregisterCustomNodeTypes(systemId, workspace);
            } catch (RepositoryException e) {
                logger.error("Cannot register nodetypes", e);
            }
            session.save();
        } finally {
            session.logout();
        }
    }

    public Repository getRepository() {
        // Double-checked locking only works with volatile for Java 5+
        // result variable is used to avoid accessing the volatile field multiple times to increase performance per Effective Java 2nd Ed.
        Repository result = repo;
        if (result == null) {
            synchronized (this) {
                result = repo;
                if (result == null) {
                    repo = result = createRepository();
                    rmiBind();
                }
            }
        }
        return result;
    }

    protected void rmiBind() {
        if (rmibind != null && repo != null) {
            try {
                Naming.rebind(rmibind, new ServerAdapterFactory().getRemoteRepository(repo));
            } catch (Exception e) {
                logger.warn("Unable to bind remote JCR repository to RMI using " + rmibind, e);
            }
        }
    }

    /**
     * Creates an instance of the content repository.
     *
     * @return an instance of the {@link Repository}
     */
    protected Repository createRepository() {
        Repository instance = null;

        if (repositoryName != null) {
            instance = getRepositoryByJNDI();
        } else if (factory != null && url != null) {
            instance = getRepositoryByRMI();
        }

        return instance;
    }

    public void setRepository(Repository repo) {
        synchronized (syncRepoInit) {
            this.repo = repo;
        }
    }

    protected Repository getRepositoryByJNDI() {
        Repository instance = null;
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            InitialContext initctx = new InitialContext(env);
            instance = (Repository) initctx.lookup(repositoryName);
            logger.info("Repository {} acquired via JNDI", getKey());
        } catch (NamingException e) {
            logger.error("Cannot get by JNDI", e);
        }
        return instance;
    }

    protected Repository getRepositoryByRMI() {
        Repository instance = null;
        try {
            Class<? extends ObjectFactory> factoryClass = Class.forName(factory).asSubclass(ObjectFactory.class);
            ObjectFactory factory = (ObjectFactory) factoryClass.newInstance();
            instance = (Repository) factory.getObjectInstance(new Reference(Repository.class.getName(),
                    new StringRefAddr("url", url)), null, null, null);
            logger.info("Repository {} acquired via RMI", getKey());
        } catch (Exception e) {
            logger.error("Cannot get by RMI", e);
        }
        return instance;
    }

    public Session getSession(Credentials credentials, String workspace) throws RepositoryException {
        return getRepository().login(getCredentials(credentials), workspace);
    }

    protected Credentials getCredentials(Credentials originalCredentials) throws PathNotFoundException,
            RepositoryException, ValueFormatException {
        if (!(originalCredentials instanceof SimpleCredentials)) {
            return originalCredentials;
        }

        Credentials credentials = originalCredentials;
        String username = ((SimpleCredentials) originalCredentials).getUserID();
        String realm = (String) ((SimpleCredentials) originalCredentials).getAttribute(JahiaLoginModule.REALM_ATTRIBUTE);

        if ("shared".equals(authenticationType)) {
            credentials = username.startsWith(GUEST) ? getGuestCredentials() : getSystemCredentials();
        } else if ("storedPasswords".equals(authenticationType)) {
            JCRUserNode user = userManagerService.lookupUser(username, realm, false);
            username = user.getPropertyAsString("storedUsername_" + getKey());
            JCRPropertyWrapper passProp = user.getProperty("storedPassword_" + getKey());
            if (passProp != null) {
                credentials = new SimpleCredentials(username, passProp.getString().toCharArray());
            } else {
                credentials = getGuestCredentials();
            }
        } else {
            if (systemUser != null && username.startsWith(SYSTEM)) {
                credentials = getSystemCredentials();
            } else if (guestUser != null && username.startsWith(GUEST)) {
                credentials = getGuestCredentials();
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Login for {} as {}", getKey(), username);
        }

        return credentials;
    }

    public JCRItemWrapper getItemWrapper(Item item, JCRSessionWrapper session) throws RepositoryException {
        if (item.isNode()) {
            return getNodeWrapper((Node) item, session);
        } else {
            return getPropertyWrapper((Property) item, session);
        }
    }

    public JCRNodeWrapper getNodeWrapper(final Node objectNode, JCRSessionWrapper session) throws RepositoryException {
        return getNodeWrapper(objectNode, null, null, session);
    }

    public JCRNodeWrapper getNodeWrapper(final Node objectNode, String path, JCRNodeWrapper parent, JCRSessionWrapper session) throws RepositoryException {
        if (!objectNode.getPath().startsWith(relativeRoot)) {
            throw new PathNotFoundException("Invalid node : " + objectNode.getPath());
        }
        final JahiaUser currentAliasedUser = sessionFactory.getCurrentAliasedUser();
        if (session.getUser() != null && currentAliasedUser != null && !currentAliasedUser.equals(session.getUser())) {
            JCRTemplate.getInstance().doExecute(currentAliasedUser, session.getWorkspace().getName(), session.getLocale(), new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            try {
                                return session.getNodeByUUID(objectNode.getIdentifier());
                            } catch (ItemNotFoundException e) {
                                throw new PathNotFoundException();
                            }
                        }
                    }
            );
        }
        return createWrapper(objectNode, path, parent, session);
    }

    private JCRNodeWrapper createWrapper(Node objectNode, String path, JCRNodeWrapper parent, JCRSessionWrapper session) throws RepositoryException {
        if (path == null || !path.contains(JCRSessionWrapper.DEREF_SEPARATOR)) {
            JCRNodeWrapper wrapper = objectNode != null ? session.getCachedNode(objectNode.getIdentifier()) : null;
            if (wrapper != null) {
                return wrapper;
            }
        }

        JCRNodeWrapperImpl w = null;
        if (session.getVersionDate() != null || session.getVersionLabel() != null) {
            try {
                if (objectNode.isNodeType(Constants.NT_FROZENNODE)) {
                    w = new JCRFrozenNodeAsRegular(objectNode, path, parent, session, this, session.getVersionDate(), session.getVersionLabel());
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (w == null) {
            w = new JCRNodeWrapperImpl(objectNode, path, parent, session, this);
        }
        if (objectNode.isNew() || w.checkValidity()) {
            return service.decorate(w);
        } else {
            throw new PathNotFoundException("This node doesn't exist in this language " + objectNode.getPath());
        }
    }

    public JCRPropertyWrapper getPropertyWrapper(Property prop, JCRSessionWrapper session) throws RepositoryException {
        PropertyDefinition def = prop.getDefinition();

        if (def == null) {
            throw new RepositoryException("Couldn't retrieve property definition for property " + prop.getPath());
        }

        JCRNodeWrapper jcrNode;

        if (def.getDeclaringNodeType().isNodeType(Constants.JAHIANT_TRANSLATION)) {
            Node parent = prop.getParent();
            jcrNode = getNodeWrapper(parent.getParent(), session);
            String name = prop.getName();
            ExtendedPropertyDefinition epd = jcrNode.getApplicablePropertyDefinition(name);
            return new JCRPropertyWrapperImpl(getNodeWrapper(session.getLocale() != null ? prop.getParent().getParent() : prop.getParent(), null, null, session), prop, session, this, epd, name);
        } else {
            jcrNode = getNodeWrapper(prop.getParent(), session);
            ExtendedPropertyDefinition epd = jcrNode.getApplicablePropertyDefinition(prop.getName());
            return new JCRPropertyWrapperImpl(getNodeWrapper(prop.getParent(), null, null, session), prop, session, this, epd);
        }
    }

    protected boolean canRegisterCustomNodeTypes() {
        return false;
    }

    protected void registerCustomNodeTypes(String systemId, Workspace ws) throws IOException, RepositoryException {
        return;
    }

    protected void unregisterCustomNodeTypes(String systemId, Workspace ws) throws IOException, RepositoryException {
        return;
    }

    public JCRNodeWrapper getUserFolder(JahiaUser user) throws RepositoryException {
        String username = ISO9075.encode(user.getUsername());
        String sql = "select * from [jnt:user] as user where localname(user) = '" + JCRContentUtils.sqlEncode(username) + "'";

        List<JCRNodeWrapper> results = queryFolders(sessionFactory.getCurrentUserSession(), sql);
        if (results.isEmpty()) {
            throw new ItemNotFoundException();
        }
        return results.get(0);
    }

    public List<JCRNodeWrapper> getImportDropBoxes(String site, JahiaUser user) throws RepositoryException {
        String username = ISO9075.encode(user.getUsername());
        String sql = "select imp.* from [jnt:importDropBox] as imp right outer join [jnt:user] as user on ischildnode(imp,user) where localname(user)= '" + username + "'";

        if (site != null) {
            site = ISO9075.encode(site);
            sql = "select imp.* from jnt:importDropBox as imp right outer join [jnt:user] as user on ischildnode(imp,user) right outer join [jnt:virtualsite] as site on isdescendantnode(imp,site) where localname(user)= '" + username + "' and localname(site) = '" + site + "'";
        }

        List<JCRNodeWrapper> results = queryFolders(sessionFactory.getCurrentUserSession(), sql);
        if (site != null) {
            results.addAll(getImportDropBoxes(null, user));
        }
        return results;
    }

    public JCRNodeWrapper getSiteFolder(String site) throws RepositoryException {
        site = ISO9075.encode(site);
        String xp = "select * from [jnt:virtualsite] as site where localname(site) = '" + JCRContentUtils.sqlEncode(site) + "'";

        final List<JCRNodeWrapper> list = queryFolders(sessionFactory.getCurrentUserSession(), xp);
        if (list.isEmpty()) {
            throw new ItemNotFoundException();
        }
        return list.get(0);
    }

    private List<JCRNodeWrapper> queryFolders(JCRSessionWrapper session, String sql) throws RepositoryException {
        List<JCRNodeWrapper> results = new ArrayList<JCRNodeWrapper>();
        QueryManager queryManager = session.getProviderSession(this).getWorkspace().getQueryManager();
        if (queryManager != null) {
            Query q = queryManager.createQuery(sql, Query.JCR_SQL2);
            if (q instanceof JahiaQueryObjectModelImpl) {
                JahiaLuceneQueryFactoryImpl lqf = (JahiaLuceneQueryFactoryImpl) ((JahiaQueryObjectModelImpl) q)
                        .getLuceneQueryFactory();
                lqf.setQueryLanguageAndLocale(LuceneUtils.extractLanguageOrNullFromStatement(sql), session.getLocale());
            }
            QueryResult qr = q.execute();
            NodeIterator ni = qr.getNodes();
            while (ni.hasNext()) {
                Node folder = ni.nextNode();
                results.add(getNodeWrapper(folder, session));
            }
        }
        return results;
    }

    public String getAbsoluteContextPath(ServletRequest request) {
        StringBuilder serverUrlBuffer = new StringBuilder(request.getScheme());
        serverUrlBuffer.append("://");
        serverUrlBuffer.append(request.getServerName());
        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
            serverUrlBuffer.append(":");
            serverUrlBuffer.append(request.getServerPort());
        }
        return serverUrlBuffer.toString();
    }

    public boolean isMainStorage() {
        return mainStorage;
    }

    public void setMainStorage(boolean mainStorage) {
        this.mainStorage = mainStorage;
    }

    public boolean isDynamicallyMounted() {
        return isDynamicallyMounted;
    }

    public void setDynamicallyMounted(boolean dynamicallyMounted) {
        isDynamicallyMounted = dynamicallyMounted;
    }

    /**
     * @return <code>true</code> if the nodes, backed by this provider, are also included during the export operation; <code>false</code> if
     * they are skipped
     * @deprecated now canExportNode and canExportProperty are used
     * Indicates if the nodes, backed by this provider, are considered during export operation.
     */
    @Deprecated
    public boolean isExportable() {
        return true;
    }

    /**
     * Checks if the specified node, backed by this provider, can be cached. This is useful e.g. in /files servlet which can cache the last
     * modified data of the file to optimize the resource loading. A particular provider could override this method to return
     * <code>true</code> for nodes, which are either read-only or can push observation events if their content is changed, to ensure cache
     * consistency.
     *
     * @return <code>true</code> if the specified node can be cached; <code>false</code> otherwise
     */
    public boolean canCacheNode(Node node) {
        return false;
    }

    /**
     * Indicates if the specified node, backed by this provider, is considered during export operation
     *
     * @return <code>true</code> if the specified node, backed by this provider, is included during the export operation
     * <code>false</code> if it isn't
     */
    public boolean canExportNode(Node node) {
        return true;
    }

    /**
     * Indicates if the specified property, backed by this provider, is considered during export operation
     *
     * @return <code>true</code> if the specified property, backed by this provider, is included during the export operation
     * <code>false</code> if it isn't
     */
    public boolean canExportProperty(Property property) {
        return true;
    }

    public boolean isDefault() {
        return defaultProvider;
    }

    protected void dump(Node n) throws RepositoryException {
        System.out.println(n.getPath());
        PropertyIterator pit = n.getProperties();
        while (pit.hasNext()) {
            Property p = pit.nextProperty();
            System.out.print(p.getPath() + "=");
            if (p.getDefinition().isMultiple()) {
                Value[] values = p.getValues();
                for (int i = 0; i < values.length; i++) {
                    Value value = values[i];
                    System.out.print(value + ",");
                }
                System.out.println("");
            } else {
                System.out.println(p.getValue());
            }
        }
        NodeIterator nit = n.getNodes();
        while (nit.hasNext()) {
            Node cn = nit.nextNode();
            if (!cn.getName().startsWith("jcr:")) {
                dump(cn);
            }
        }
    }

    public QueryManager getQueryManager(JCRSessionWrapper session) throws RepositoryException {
        return session.getProviderSession(JCRStoreProvider.this).getWorkspace().getQueryManager();
    }

    public JCRSessionWrapper getSystemSession() throws RepositoryException {
        return sessionFactory.getSystemSession();
    }

    public JCRSessionWrapper getSystemSession(String workspace) throws RepositoryException {
        return sessionFactory.getSystemSession(null, null, workspace, null);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setProvidesDynamicMountPoints(boolean providesDynamicMountPoints) {
        this.providesDynamicMountPoints = providesDynamicMountPoints;
    }

    public boolean isReadOnly() {
        if (readOnly != null) {
            return readOnly;
        }
        Repository repository = getRepository();
        Value writeableValue = repository.getDescriptorValue(Repository.WRITE_SUPPORTED);
        if (writeableValue == null) {
            readOnly = Boolean.FALSE;
            return false;
        }
        try {
            readOnly = !writeableValue.getBoolean();
        } catch (RepositoryException e) {
            logger.warn("Error while trying to check for writeable support", e);
            readOnly = Boolean.FALSE;
            return false;
        }
        return readOnly;

    }

    public boolean isVersioningAvailable() {
        if (versioningAvailable != null) {
            return versioningAvailable;
        }
        Repository repository = getRepository();
        Value versioningOptionValue = repository.getDescriptorValue(Repository.OPTION_VERSIONING_SUPPORTED);
        if (versioningOptionValue == null) {
            versioningAvailable = Boolean.FALSE;
            return false;
        }
        Value simpleVersioningOptionValue = repository.getDescriptorValue(Repository.OPTION_SIMPLE_VERSIONING_SUPPORTED);
        if (simpleVersioningOptionValue == null) {
            versioningAvailable = Boolean.FALSE;
            return false;
        }
        try {
            versioningAvailable = versioningOptionValue.getBoolean() && simpleVersioningOptionValue.getBoolean();
        } catch (RepositoryException e) {
            logger.warn("Error while trying to check for versioning support", e);
            versioningAvailable = Boolean.FALSE;
            return false;
        }
        return versioningAvailable;
    }

    public boolean isLockingAvailable() {
        if (lockingAvailable != null) {
            return lockingAvailable;
        }
        Repository repository = getRepository();
        Value lockingOptionValue = repository.getDescriptorValue(Repository.OPTION_LOCKING_SUPPORTED);
        if (lockingOptionValue == null) {
            lockingAvailable = Boolean.FALSE;
            return false;
        }
        try {
            lockingAvailable = lockingOptionValue.getBoolean();
        } catch (RepositoryException e) {
            logger.warn("Error while trying to check for locking support", e);
            lockingAvailable = Boolean.FALSE;
        }
        return lockingAvailable;
    }

    public boolean isSearchAvailable() {
        if (searchAvailable != null) {
            return searchAvailable;
        }
        Repository repository = getRepository();
        Value[] queryLanguageValues = repository.getDescriptorValues(Repository.QUERY_LANGUAGES);
        if (queryLanguageValues == null) {
            searchAvailable = Boolean.FALSE;
            return false;
        }
        if (queryLanguageValues.length == 0) {
            searchAvailable = Boolean.FALSE;
        } else {
            searchAvailable = Boolean.TRUE;
        }
        return searchAvailable;
    }

    public boolean isUpdateMixinAvailable() {
        if (updateMixinAvailable != null) {
            return updateMixinAvailable;
        }
        Repository repository = getRepository();
        Value updateMixinOptionValue = repository.getDescriptorValue(Repository.OPTION_UPDATE_MIXIN_NODE_TYPES_SUPPORTED);
        if (updateMixinOptionValue == null) {
            updateMixinAvailable = Boolean.FALSE;
            return false;
        }
        try {
            updateMixinAvailable = updateMixinOptionValue.getBoolean();
        } catch (RepositoryException e) {
            logger.warn("Error while trying to check for mixin updates support", e);
            updateMixinAvailable = Boolean.FALSE;
        }
        return updateMixinAvailable;
    }

    public boolean isSlowConnection() {
        if (slowConnection != null) {
            return slowConnection;
        }
        Repository repository = getRepository();
        Value[] slowConnectionValues = repository.getDescriptorValues("jahia.provider.slowConnection");
        if (slowConnectionValues == null) {
            slowConnection = Boolean.FALSE;
            return false;
        }
        if (slowConnectionValues.length == 0) {
            slowConnection = Boolean.FALSE;
        } else {
            slowConnection = Boolean.TRUE;
        }
        return slowConnection;
    }

    public void setSlowConnection(boolean slowConnection) {
        this.slowConnection = slowConnection;
    }

    /**
     * Get weak references of a node
     *
     * @param node         node
     * @param propertyName name of the property
     * @param session      session
     * @return an iterator
     * @throws RepositoryException
     */
    public PropertyIterator getWeakReferences(JCRNodeWrapper node, String propertyName, Session session) throws RepositoryException {
        return null;
    }

    @Override
    public int compareTo(JCRStoreProvider o) {
        if (this == o) {
            return 0;
        }

        if (o == null) {
            return 1;
        }

        return StringUtils.defaultString(getMountPoint()).compareTo(StringUtils.defaultString(o.getMountPoint()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JCRStoreProvider that = (JCRStoreProvider) o;

        return StringUtils.defaultString(getMountPoint()).equals(that.getMountPoint());
    }

    @Override
    public int hashCode() {
        return getMountPoint() != null ? getMountPoint().hashCode() : 0;
    }

    public Map<String, Constructor<?>> getValidators() {
        return service.getValidators();
    }

    public void setRegisterObservers(boolean registerObservers) {
        this.registerObservers = registerObservers;
    }

    public void setObserversUseRelativeRoot(boolean observersUseRelativePath) {
        this.observersUseRelativeRoot = observersUseRelativePath;
    }

    protected void unregisterObservers() {
        if (!registerObservers || observerSessions == null || observerSessions.isEmpty()) {
            return;
        }
        for (Iterator<Entry<String, JCRSessionWrapper>> it = observerSessions.entrySet().iterator(); it.hasNext(); ) {
            Entry<String, JCRSessionWrapper> sessionEntry = it.next();
            try {
                sessionEntry.getValue().logout();
                logger.info("Closed session for provider {} and workspace {}", getMountPoint(), sessionEntry.getKey());
            } catch (Exception e) {
                logger.warn(
                        "Error closing session for provider " + getMountPoint() + " and workspace "
                                + sessionEntry.getKey(), e);
            } finally {
                it.remove();
            }
        }
    }

    protected Credentials getGuestCredentials() {
        if (guestCredentials == null) {
            if (guestUser != null) {
                if (guestPassword != null) {
                    guestCredentials = new SimpleCredentials(guestUser, guestPassword.toCharArray());
                } else {
                    return JahiaLoginModule.getCredentials(guestUser, null, null);
                }
            } else {
                return JahiaLoginModule.getGuestCredentials();
            }
        }

        return guestCredentials;
    }

    protected Credentials getSystemCredentials() {
        if (systemCredentials == null) {
            if (systemUser != null) {
                if (systemPassword != null) {
                    systemCredentials = new SimpleCredentials(systemUser, systemPassword.toCharArray());
                } else {
                    return JahiaLoginModule.getCredentials(systemUser, null, null);
                }
            } else {
                return JahiaLoginModule.getSystemCredentials();
            }
        }

        return systemCredentials;
    }
}
