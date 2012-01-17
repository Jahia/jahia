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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.query.JahiaQueryObjectModelImpl;
import org.apache.jackrabbit.core.query.lucene.JahiaLuceneQueryFactoryImpl;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry;
import org.apache.jackrabbit.rmi.server.ServerAdapterFactory;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.decorator.JCRFrozenNodeAsRegular;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.content.impl.jackrabbit.JackrabbitStoreProvider;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.jcr.JCRGroup;
import org.jahia.services.usermanager.jcr.JCRGroupManagerProvider;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.settings.SettingsBean;
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
import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.*;

/**
 * A store provider to handle different back-end stores within a site. There are multiple
 * subclasses for the different repository vendors.
 * <p/>
 * The main and default repository in Jahia is based on the {@link JackrabbitStoreProvider},
 * but you can have different repositories mounted, which are based on other store providers.
 *
 * @author toto
 */
public class JCRStoreProvider {

    private static Logger logger = LoggerFactory.getLogger(JCRStoreProvider.class);

    private static String httpPath;

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
    protected String guestUser;
    protected String guestPassword;

    protected String authenticationType = null;

    protected String rmibind;

    private boolean running;

    private JahiaUserManagerService userManagerService;
    private JahiaGroupManagerService groupManagerService;
    private JahiaSitesService sitesService;

    private JCRStoreService service;
    private JCRPublicationService publicationService;

    protected JCRSessionFactory sessionFactory;
    protected Repository repo = null;

    private boolean mainStorage = false;
    private boolean isDynamicallyMounted = false;
    private boolean initialized = false;

    private boolean providesDynamicMountPoints;

    private Boolean versioningAvailable = null;
    private Boolean lockingAvailable = null;
    private Boolean searchAvailable = null;
    private Boolean updateMixinAvailable = null;

    private final Object syncRepoInit = new Object();

    private long sessionKeepAliveCheckInterval = 5000L;

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
        return mountPoint.split("/").length - 1;
    }

    public void setWebdavPath(String webdavPath) {
        this.webdavPath = webdavPath;
    }

    public String getHttpPath() {
        if (httpPath == null) {
            httpPath = Jahia.getContextPath() + "/files";
        }
        
        return httpPath;
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

    public JCRPublicationService getPublicationService() {
        return publicationService;
    }

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    public long getSessionKeepAliveCheckInterval() {
        return sessionKeepAliveCheckInterval;
    }

    public void setSessionKeepAliveCheckInterval(long sessionKeepAliveCheckInterval) {
        this.sessionKeepAliveCheckInterval = sessionKeepAliveCheckInterval;
    }

    public void start() throws JahiaInitializationException {
        try {
            String tmpAuthenticationType = authenticationType;
            authenticationType = "shared";

            getSessionFactory().addProvider(getKey(), getMountPoint(), this);

            if (SettingsBean.getInstance().isProcessingServer()) {
                initNodeTypes();
            }
            initObservers();
            initialized = true;
            initContent();
            initDynamicMountPoints();

            if (rmibind != null) {
                try {
                    Naming.rebind(rmibind, new ServerAdapterFactory().getRemoteRepository(repo));
                } catch (MalformedURLException e) {
                } catch (RemoteException e) {
                }
            }

            authenticationType = tmpAuthenticationType;
        } catch (Exception e) {
            logger.error("Repository init error", e);
            throw new JahiaInitializationException("Repository init error", e);
        }
    }

    protected void initNodeTypes() throws RepositoryException, IOException {
//        JahiaUser root = getGroupManagerService().getAdminUser(0);
        if (canRegisterCustomNodeTypes()) {

            File f = new File(SettingsBean.getInstance().getJahiaVarDiskPath()+"/definitions.properties");
            Properties p = new Properties();

            JCRSessionWrapper session = getSystemSession();
            try {
                Workspace workspace = session.getProviderSession(this).getWorkspace();
                workspace.getNodeTypeManager().getNodeType("jmix:droppableContent");

                if (f.exists()) {
                    InputStream stream = new BufferedInputStream(new FileInputStream(f));
                    try {
                        p.load(stream);
                    } finally {
                        IOUtils.closeQuietly(stream);
                    }
                }

            } catch (RepositoryException e) {
                f.delete();
            } finally {
                session.logout();
            }

            boolean needUpdate = false;
            List<String> systemIds = NodeTypeRegistry.getInstance().getSystemIds();
            for (String systemId : systemIds) {
                needUpdate |= deployDefinitions(systemId, p);
            }

            if (needUpdate) {
                OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
                try {
                    p.store(out, "");
                } finally {
                    IOUtils.closeQuietly(out);
                }
            }
        }
    }

    protected void initObservers() throws RepositoryException {
        Set<String> workspaces = service.getListeners().keySet();
        for (String ws : workspaces) {
            // This session must not be released
            final JCRSessionWrapper session = getSystemSession(null, ws);
            final Workspace workspace = session.getProviderSession(this).getWorkspace();

            ObservationManager observationManager = workspace.getObservationManager();
            JCRObservationManagerDispatcher listener = new JCRObservationManagerDispatcher();
            listener.setProvider(this);
            listener.setWorkspace(workspace.getName());
            observationManager.addEventListener(listener,
                    Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED + Event.NODE_MOVED,
                    "/", true, null, null, false);

            // The thread should always checks if the session is still alive and reconnect it if lost
            running = true;
            Thread t = new Thread(ws + "-WorkspaceObserverKeepAlive") {
                public void run() {
                    while (isRunning() && session.isLive()) {
                        try {
                            // we retrieve root node to keep the session alive (note : in Jackrabbit sessions never
                            // time-out but as this is possible in the spec, we do a simple read call in case we
                            // use other implementations).
                            Node rootNode = session.getRootNode();
                        } catch (RepositoryException e) {
                            if (running && logger != null) logger.error(e.getMessage(), e);
                        }
                        try {
                            Thread.sleep(sessionKeepAliveCheckInterval);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                    if (running && logger != null) logger.info("System session closed, deregister listeners");
                }
            };
            t.setDaemon(true); 
            t.start();
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
            List<JCRNodeWrapper> result = queryFolders(session, "select * from [jnt:mountPoint]");
            for (JCRNodeWrapper mountPointNode : result) {
                if (mountPointNode instanceof JCRMountPointNode) {
                    try {
                        if (((JCRMountPointNode) mountPointNode).checkValidity()) {
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
        running = false;
        getSessionFactory().removeProvider(key);
        if (rmibind != null) {
            try {
                Naming.unbind(rmibind);
            } catch (Exception e) {
                logger.warn("Unable to unbind the JCR repository in RMI");
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void deployDefinitions(String systemId) {
        try {
            File f = new File(SettingsBean.getInstance().getJahiaVarDiskPath()+"/definitions.properties");
            Properties p = new Properties();
            if (f.exists()) {
                InputStream stream = new BufferedInputStream(new FileInputStream(f));
                try {
                    p.load(stream);
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            }

            if (deployDefinitions(systemId, p)) {
                OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
                try {
                    p.store(out, "");
                } finally {
                    IOUtils.closeQuietly(out);
                }
            }
        } catch (IOException e) {
            logger.error("Cannot save definitions timestamps",e);
        }
    }

    private boolean deployDefinitions(String systemId, Properties p) {
        List<File> files = NodeTypeRegistry.getInstance().getFiles(systemId);
        boolean needUpdate = false;
        for (File file : files) {
            if (p.getProperty(file.getPath()) == null || Long.parseLong(p.getProperty(file.getPath())) != file.lastModified()) {
                needUpdate = true;
                p.setProperty(file.getPath(), Long.toString(file.lastModified()));
            }
        }
        if (needUpdate) {
            try {
                repo = getRepository();
                JCRSessionWrapper session = sessionFactory.getSystemSession();
                try {
                    Workspace workspace = session.getProviderSession(this).getWorkspace();

                    try {
                        registerCustomNodeTypes(systemId, workspace);
                    } catch (RepositoryException e) {
                        logger.error("Cannot register nodetypes", e);
                    }
                    session.save();
                } finally {
                    session.logout();
                }
            } catch (Exception e) {
                logger.error("Repository init error", e);
            }
        }
        return needUpdate;
    }

    public Repository getRepository() {
        if (repo == null) {
            synchronized (syncRepoInit) {
        if (repo == null) {
            if (repositoryName != null) {
                repo = getRepositoryByJNDI();
                if (rmibind != null) {
                    try {
                        Naming.rebind(rmibind, new ServerAdapterFactory().getRemoteRepository(repo));
                    } catch (MalformedURLException e) {
                    } catch (RemoteException e) {
                    }
                }
                return repo;
            } else if (factory != null && url != null) {
                repo = getRepositoryByRMI();
            }
        }
            }
        }
        return repo;
    }

    public void setRepository(Repository repo) {
        synchronized (syncRepoInit) {
        this.repo = repo;
    }
    }

    protected Repository getRepositoryByJNDI() {
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            InitialContext initctx = new InitialContext(env);
            // ((ObjectFactory)Class.forName(((Reference) initctx.lookup(repositoryName)).getFactoryClassName()).newInstance()).getObjectInstance(((Reference) initctx.lookup(repositoryName)), null,null,null)
            repo = (Repository) initctx.lookup(repositoryName);
            logger.info("Repository " + getKey() + " acquired via JNDI");
            return repo;
        } catch (NamingException e) {
            logger.error("Cannot get by JNDI", e);
        }
        return null;
    }

    protected Repository getRepositoryByRMI() {
        try {
            Class<? extends ObjectFactory> factoryClass = Class.forName(factory).asSubclass(ObjectFactory.class);
            ObjectFactory factory = (ObjectFactory) factoryClass.newInstance();
            repo = (Repository) factory.getObjectInstance(new Reference(Repository.class.getName(), new StringRefAddr("url", url)), null, null, null);
            logger.info("Repository " + getKey() + " acquired via RMI");
            return repo;
        } catch (Exception e) {
            logger.error("Cannot get by RMI", e);
        }
        return null;
    }

    public Session getSession(Credentials credentials, String workspace) throws RepositoryException {
        Session s;

        if (credentials instanceof SimpleCredentials) {
            String username = ((SimpleCredentials) credentials).getUserID();

            if ("shared".equals(authenticationType)) {
                if (username.startsWith(" system ") || guestUser == null) {
                    credentials = JahiaLoginModule.getSystemCredentials();
                } else {
                    credentials = JahiaLoginModule.getGuestCredentials();
                }
                username = ((SimpleCredentials) credentials).getUserID();
            }

            if (username.startsWith(" system ") && systemUser != null) {
                if (systemPassword != null) {
                    credentials = new SimpleCredentials(systemUser, systemPassword.toCharArray());
                } else {
                    credentials = JahiaLoginModule.getCredentials(systemUser);
                }
            } else if (username.startsWith(" guest ") && guestUser != null) {
                if (guestPassword != null) {
                    credentials = new SimpleCredentials(guestUser, guestPassword.toCharArray());
                } else {
                    credentials = JahiaLoginModule.getCredentials(guestUser);
                }
            } else if ("storedPasswords".equals(authenticationType)) {
                JahiaUser user = userManagerService.lookupUser(username);
                if (user.getProperty("storedUsername_" + getKey()) != null) {
                    username = user.getProperty("storedUsername_" + getKey());
                }
                String pass = user.getProperty("storedPassword_" + getKey());
                if (pass != null) {
                    credentials = new SimpleCredentials(username, pass.toCharArray());
                } else {
                    if (guestPassword != null) {
                        credentials = new SimpleCredentials(guestUser, guestPassword.toCharArray());
                    } else {
                        credentials = JahiaLoginModule.getCredentials(guestUser);
                    }
                }
            }
            logger.debug("Login for " + getKey() + " as " + ((SimpleCredentials) credentials).getUserID());
        }

        s = getRepository().login(credentials, workspace);
        return s;
    }

    public JCRItemWrapper getItemWrapper(Item item, JCRSessionWrapper session) throws RepositoryException {
        if (item.isNode()) {
            return getNodeWrapper((Node) item, session);
        } else {
            return getPropertyWrapper((Property) item, session);
        }
    }

    public JCRNodeWrapper getNodeWrapper(final Node objectNode, JCRSessionWrapper session) throws RepositoryException {
        if (session.getUser() != null && sessionFactory.getCurrentAliasedUser() != null &&
                !sessionFactory.getCurrentAliasedUser().equals(session.getUser())) {
            JCRTemplate.getInstance().doExecuteWithUserSession(sessionFactory.getCurrentAliasedUser().getUsername(),
                    session.getWorkspace().getName(), session.getLocale(), new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            try {
                                return session.getNodeByUUID(objectNode.getIdentifier());
                            } catch (ItemNotFoundException e) {
                                throw new PathNotFoundException();
                            }
                        }
                    });
        }
        final JCRNodeWrapper w = createWrapper(objectNode, null, null, session);
        if (w.checkValidity()) {
            return service.decorate(w);
        } else {
            throw new PathNotFoundException("This node doesn't exist in this language " + objectNode.getPath());
        }
    }

    public JCRNodeWrapper getNodeWrapper(final Node objectNode, String path, JCRNodeWrapper parent, JCRSessionWrapper session) throws RepositoryException {
        if (session.getUser() != null && sessionFactory.getCurrentAliasedUser() != null &&
                !sessionFactory.getCurrentAliasedUser().equals(session.getUser())) {
            JCRTemplate.getInstance().doExecuteWithUserSession(sessionFactory.getCurrentAliasedUser().getUsername(),
                    session.getWorkspace().getName(), session.getLocale(), new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            try {
                                return session.getNodeByUUID(objectNode.getIdentifier());
                            } catch (ItemNotFoundException e) {
                                throw new PathNotFoundException();
                            }
                        }
                    });
        }
        final JCRNodeWrapper w = createWrapper(objectNode, path, parent, session);
        if (objectNode.isNew() || w.checkValidity()) {
            return service.decorate(w);
        } else {
            throw new PathNotFoundException("This node doesn't exist in this language " + objectNode.getPath());
        }
    }

    private JCRNodeWrapper createWrapper(Node objectNode, String path, JCRNodeWrapper parent, JCRSessionWrapper session) throws RepositoryException {
        if (path == null || !path.contains(JCRSessionWrapper.DEREF_SEPARATOR)) {
            JCRNodeWrapper wrapper =  objectNode != null ? session.getCachedNode(objectNode.getIdentifier()) : null;
            if (wrapper != null) {
                return wrapper;
            }
        }

        if (session.getVersionDate() != null || session.getVersionLabel() != null) {
            try {
                if (objectNode.isNodeType(Constants.NT_FROZENNODE)) {
                    return new JCRFrozenNodeAsRegular(objectNode, path, parent, session, this, session.getVersionDate(), session.getVersionLabel());
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return new JCRNodeWrapperImpl(objectNode, path, parent, session, this);
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
            return new JCRPropertyWrapperImpl(createWrapper(prop.getParent(), null, null, session), prop, session, this, epd, name);
        } else {
            jcrNode = getNodeWrapper(prop.getParent(), session);
            ExtendedPropertyDefinition epd = jcrNode.getApplicablePropertyDefinition(prop.getName());
            return new JCRPropertyWrapperImpl(createWrapper(prop.getParent(), null, null, session), prop, session, this, epd);
        }
    }

    protected boolean canRegisterCustomNodeTypes() {
        return false;
    }

    protected void registerCustomNodeTypes(String systemId, Workspace ws) throws IOException, RepositoryException {
        return;
    }

    public void deployExternalUser(JahiaUser jahiaUser) throws RepositoryException {
        String username = jahiaUser.getUsername();
        JCRSessionWrapper session = sessionFactory.getSystemSession(username, null);
        try {
            String jcrUsernamePath[] = StringUtils.substringAfter(jahiaUser.getLocalPath(), "/").split("/");
            try {
                Node startNode = session.getNode("/" + jcrUsernamePath[0]);
                Node usersFolderNode = startNode;
                int length = jcrUsernamePath.length;
                for (int i = 1; i < length; i++) {
                        try {
                        startNode = startNode.getNode(jcrUsernamePath[i]);
                        } catch (PathNotFoundException e) {
                                try {
                            session.getWorkspace().getVersionManager().checkout(startNode.getPath());
                            if (i == (length - 1)) {
                                Node userNode = startNode.addNode(jcrUsernamePath[i], Constants.JAHIANT_USER);
                                        if (usersFolderNode.hasProperty("j:usersFolderSkeleton")) {
                                        	String skeletons = usersFolderNode.getProperty("j:usersFolderSkeleton").getString();
                                        	try {
                                        JCRContentUtils.importSkeletons(skeletons,
                                                startNode.getPath() + "/" + jcrUsernamePath[i], session,
                                                new HashMap<String, String>());
                                        	} catch (Exception importEx) {
                                        logger.error("Unable to import data using user skeletons " + skeletons,
                                                importEx);
                                        	}
                                        }

                                        userNode.setProperty(JCRUser.J_EXTERNAL, true);
                                userNode.setProperty(JCRUser.J_EXTERNAL_SOURCE, jahiaUser.getProviderName());
                                        ((JCRNodeWrapper)userNode).grantRoles("u:" + username, Collections.singleton("owner"));
                            } else {
                                // Simply create a folder
                                startNode = startNode.addNode(jcrUsernamePath[i], "jnt:usersFolder");
                            }
                                        session.save();
                                    } catch (RepositoryException e1) {
                                        logger.error("Cannot save", e1);
                                    }
                                }
                            }
            } catch (PathNotFoundException e) {
                        }
        } finally {
            session.logout();
        }
    }

    /**
     * Create an entry in the JCR for an external group.
     *
     * @param group        the unique name for the group
     * @return a reference on a group object on success, or if the group name
     *         already exists or another error occurred, null is returned.
     */
    public void deployExternalGroup(JahiaGroup group) {
        Properties properties = new Properties();
        properties.put(JCRGroup.J_EXTERNAL, Boolean.TRUE);
        properties.put(JCRGroup.J_EXTERNAL_SOURCE, group.getProviderName());
        JCRGroupManagerProvider groupManager = (JCRGroupManagerProvider) SpringContextSingleton.getInstance().getContext().getBean("JCRGroupManagerProvider");
        if (groupManager.lookupExternalGroup(group.getName()) == null) {
            groupManagerService.createGroup(0, group.getName(), properties, true);
        }
    }



    public JCRNodeWrapper getUserFolder(JahiaUser user) throws RepositoryException {
        String username = ISO9075.encode(user.getUsername());
        String sql = "select * from [jnt:user] as user where localname(user) = '" + username + "'";

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
        String xp = "select * from [jnt:virtualsite] as site where localname(site) = '" + site + "'";

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

                lqf.setProvider(this);
                lqf.setJcrSession(session);
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
        StringBuffer serverUrlBuffer = new StringBuffer(request.getScheme());
        serverUrlBuffer.append("://");
        serverUrlBuffer.append(request.getServerName());
        serverUrlBuffer.append(":");
        serverUrlBuffer.append(request.getServerPort());
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

    public boolean isExportable() {
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

    public JCRSessionWrapper getSystemSession(String user, String workspace) throws RepositoryException {
        return sessionFactory.getSystemSession(user, workspace);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setProvidesDynamicMountPoints(boolean providesDynamicMountPoints) {
        this.providesDynamicMountPoints = providesDynamicMountPoints;
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
            versioningAvailable = versioningOptionValue.getBoolean() & simpleVersioningOptionValue.getBoolean();
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

}
