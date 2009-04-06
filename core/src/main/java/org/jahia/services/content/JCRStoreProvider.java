/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.*;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
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

import org.apache.jackrabbit.rmi.server.ServerAdapterFactory;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.jaas.JahiaLoginModule;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 20 nov. 2007
 * Time: 18:09:21
 * To change this template use File | Settings | File Templates.
 */
public class JCRStoreProvider {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JCRStoreProvider.class);

    private String key;
    private String mountPoint;
    private String webdavPath;

    private String repositoryName;
    private String factory;
    private String url;
    private String workspace;
    protected String user;
    protected String password;
    protected String rmibind;
    protected boolean loginModuleActivated = true;
    private boolean running;
    private List<DefaultEventListener> listeners;

    private JahiaUserManagerService userManagerService;
    private JahiaGroupManagerService groupManagerService;
    private JahiaSitesService sitesService;

    private JCRStoreService service;

    protected Repository repo = null;

    private boolean isMainStorage = false;
    private boolean isDynamicallyMounted = false;

    //    private ThreadLocal systemSession = new ThreadLocal();
    protected ThreadLocal<Map<String,Session>> userSession = new ThreadLocal<Map<String,Session>>();

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
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getWebdavPath() {
        return webdavPath;
    }

    public void setWebdavPath(String webdavPath) {
        // TODO find better way to handle ROOT context and doubnle slash problem
        this.webdavPath = webdavPath;
        if (webdavPath != null) {
            if ("/".equals(webdavPath)) {
                this.webdavPath = "";
            } else if (webdavPath.startsWith("//")) {
                this.webdavPath = webdavPath.substring(1);
            }
        }
    }

    public String getHttpPath() {
        if ("/".equals(Jahia.getContextPath())) {
            return "/files";
        }
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
        this.loginModuleActivated = false;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRmibind() {
        return rmibind;
    }

    public void setRmibind(String rmibind) {
        this.rmibind = rmibind;
    }

    public List<DefaultEventListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<DefaultEventListener> listeners) {
        this.listeners = listeners;
        for (DefaultEventListener listener : listeners) {
            listener.setProvider(this);
        }
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

    public void start() throws JahiaInitializationException {
        try {
            getService().addProvider(getKey(), getMountPoint(), this);
            repo = getRepository();
            JahiaUser root = getGroupManagerService().getAdminUser(0);
            Session session = getSystemSession(root.getUsername());
            try {
                Workspace workspace = session.getWorkspace();

                try {
                    registerCustomNodeTypes(workspace);
                } catch (RepositoryException e) {
                    logger.error("Cannot register nodetypes",e);
                }

                session.save();

                initObservers();

                Node rootNode = session.getRootNode();
                initializeAcl(session);

                if ("/".equals(mountPoint) && !rootNode.hasNode(Constants.CONTENT)) {
                    session.importXML("/", new FileInputStream(org.jahia.settings.SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/root.xml"),ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
                }                              

                session.save();
            } finally {
                session.logout();
            }
        } catch (Exception e){
            logger.error("Repository init error",e);
            throw  new JahiaInitializationException("Repository init error",e) ;
        }
    }

    protected void initObservers() throws RepositoryException {
        // This session must not be released
        final Session session = getSystemSession();
        final Workspace workspace = session.getWorkspace();

        if (listeners != null) {
            ObservationManager observationManager = workspace.getObservationManager();
            for (DefaultEventListener listener : listeners) {
                observationManager.addEventListener(listener, listener.getEventTypes(), listener.getPath(), true, null, listener.getNodeTypes(), false);
            }
        }

        // The thread should always checks if the session is still alive and reconnect it if lost
        running = true;
        Thread t = new Thread() {
            public void run() {
                while (isRunning() && session.isLive()) {
                    try {
                        session.refresh(false);
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
                logger.info("System session closed, deregister listeners");
            }
        };
        t.start();
    }

    public void stop() {
        running = false;
        service.removeProvider(key);
    }

    public boolean isRunning() {
        return running;
    }

    public void deployDefinitions(String systemId) {
        try {
            repo = getRepository();
            JahiaUser root = getGroupManagerService().getAdminUser(0);
            Session session = getSystemSession(root.getUsername());
            try {
                Workspace workspace = session.getWorkspace();

                try {
                    registerCustomNodeTypes(systemId, workspace);
                } catch (RepositoryException e) {
                    logger.error("Cannot register nodetypes",e);
                }
                session.save();
            } finally {
                session.logout();
            }
        } catch (Exception e){
            logger.error("Repository init error",e);
        }
    }

    public synchronized Repository getRepository(){
        if (repo == null) {
            if (repositoryName != null) {
                Repository r = getRepositoryByJNDI();
                if (rmibind != null) {
                    try {
                        Naming.rebind(rmibind, new ServerAdapterFactory().getRemoteRepository(r));
                    } catch (MalformedURLException e) {
                    } catch (RemoteException e) {
                    }
                }
                return r;
            } else if (factory != null && url != null) {
                return getRepositoryByRMI();
            }
        }
        return repo;
    }

    public void setRepository(Repository repo) {
        this.repo = repo;
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
            logger.error("Cannot get by JNDI",e);
        }
        return null;
    }

    protected Repository getRepositoryByRMI() {
        try {
            Class<? extends ObjectFactory> factoryClass = Class.forName(factory).asSubclass(ObjectFactory.class);
            ObjectFactory factory = (ObjectFactory) factoryClass.newInstance();
            repo = (Repository) factory.getObjectInstance(new Reference(Repository.class.getName(), new StringRefAddr("url", url)),null,null,null);
            logger.info("Repository " + getKey() + " acquired via RMI");
            return repo;
        } catch (Exception e) {
            logger.error("Cannot get by RMI",e);
        }
        return null;
    }

    protected Session getThreadSession(JahiaUser user) throws RepositoryException {
        // thread user session might be inited/closed in an http filter, instead of keeping it

        Map<String,Session> smap = userSession.get();
        if (smap == null) {
            smap = new HashMap<String,Session>();
        }
        userSession.set(smap);

        String username;

        if (JahiaUserManagerService.isGuest(user)) {
            username = JahiaLoginModule.GUEST;
        } else {
            username = user.getUsername();
        }

//        try {
//            if (s != null && loginModuleActivated && !s.getUserID().equals(username)) {
//                logger.error("Session is switching user, was :"+ s.getUserID() + " now :" + username, new Exception());
//                s.logout();
//            }
//        } catch (IllegalStateException e) {
//            logger.error("Exception on session : "+e);
//            s = null;
//        }
        Session s = smap.get(username);
        if (s == null || !s.isLive()) {
            if (loginModuleActivated) {
                if (!JahiaLoginModule.GUEST.equals(username)) {
                    s = repo.login(org.jahia.jaas.JahiaLoginModule.getCredentials(username), workspace);
                    // should be done somewhere else, call can be quite expensive
                    deployNewUser(username);
                } else {
                    s = repo.login(org.jahia.jaas.JahiaLoginModule.getGuestCredentials(), workspace);
                }
            } else {
                s = repo.login(new SimpleCredentials(this.user, password.toCharArray()), workspace);
            }
            registerNamespaces(s.getWorkspace());
            smap.put(username, s);
        } else {
            s.refresh(true);
        }
        return s;
    }

    public void closeThreadSession() throws RepositoryException {
        Map<String, Session> smap = userSession.get();
        if (smap != null) {
            for (Session s : smap.values()) {
                s.logout();
            }
            userSession.set(null);
        }
    }

    public Session getSystemSession() throws RepositoryException {
        Session s;
        if (loginModuleActivated) {
            s = repo.login(JahiaLoginModule.getSystemCredentials(),workspace);
        } else {
            s = repo.login(new SimpleCredentials(user, password.toCharArray()),workspace);
        }
        registerNamespaces(s.getWorkspace());
        return s;
    }

    public Session getSystemSession(String username) throws RepositoryException {
        Session s;
        if (loginModuleActivated) {
            s = repo.login(JahiaLoginModule.getSystemCredentials(username),workspace);
        } else {
            s = repo.login(new SimpleCredentials(user, password.toCharArray()),workspace);
        }
        registerNamespaces(s.getWorkspace());
        return s;
    }

    protected void registerNamespaces(Workspace workspace) throws RepositoryException {
        NamespaceRegistry namespaceRegistry = workspace.getNamespaceRegistry();
        try {
            namespaceRegistry.getURI(Constants.JAHIA_PREF);
        } catch (NamespaceException e) {
            namespaceRegistry.registerNamespace(Constants.JAHIA_PREF, Constants.JAHIA_NS);
            namespaceRegistry.registerNamespace(Constants.JAHIANT_PREF, Constants.JAHIANT_NS);
            namespaceRegistry.registerNamespace(Constants.JAHIAMIX_PREF, org.jahia.api.Constants.JAHIAMIX_NS);
        }
    }

    public JCRNodeWrapper getNodeWrapper(String localPath, JahiaUser user) {
        try {
            Session session = getThreadSession(user);
            return getNodeWrapper(localPath, user, session);
        } catch (RepositoryException e) {
            logger.error("Repository error",e);
            return null;
        }
    }

    public NodeType getNodeType(String name) throws RepositoryException {
        Session session = getSystemSession();
        try {
            return session.getWorkspace().getNodeTypeManager().getNodeType(name);
        } finally {
            session.logout();
        }
    }

    public JCRNodeWrapper getNodeWrapper(String localPath, JahiaUser user, Session session) {
        return service.decorate(new JCRNodeWrapperImpl(localPath, user, session, this));
    }

    public JCRNodeWrapper getNodeWrapper(Node objectNode, JahiaUser user, Session session) {
        return service.decorate(new JCRNodeWrapperImpl(objectNode, user, session, this));
    }

    protected void registerCustomNodeTypes(Workspace ws) throws IOException, RepositoryException {
        return;
    }

    protected void registerCustomNodeTypes(String systemId, Workspace ws) throws IOException, RepositoryException {
        return;
    }

    protected void initializeAcl(Session session) throws RepositoryException, IOException {
        return;
    }


    public void deployNewSite(JahiaSite site, JahiaUser user) throws RepositoryException {
        Session session = getSystemSession(user.getUsername());
        try {
            Query q = session.getWorkspace().getQueryManager().createQuery("SELECT * FROM jmix:virtualsitesFolder", Query.SQL);
            QueryResult qr = q.execute();
            NodeIterator ni = qr.getNodes();
            try {
                while (ni.hasNext()) {
                    Node sitesFolder = ni.nextNode();
                    String options = "";
                    if (sitesFolder.hasProperty("j:virtualsitesFolderConfig")) {
                        options = sitesFolder.getProperty("j:virtualsitesFolderConfig").getString();
                    }

                    Node f = getPathFolder(sitesFolder, site.getSiteKey(), options);
                    try {
                        f.getNode(site.getSiteKey());
                    } catch (PathNotFoundException e) {
                        if (sitesFolder.hasProperty("j:virtualsitesFolderSkeleton")) {
                            session.importXML(f.getPath(), new FileInputStream(org.jahia.settings.SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/"+ sitesFolder.getProperty("j:virtualsitesFolderSkeleton").getString()),ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
                            session.move(f.getPath()+"/site", f.getPath()+"/"+site.getSiteKey());
                        } else {
                            f.addNode(site.getSiteKey(), Constants.JAHIANT_VIRTUALSITE);
                        }

                        Node siteNode = f.getNode(site.getSiteKey());
                        siteNode.setProperty("j:name", site.getSiteKey());
                        siteNode.setProperty("j:server", site.getServerName());

                        session.save();
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        } finally {
            session.logout();
        }
    }

    public void deployNewUser(String username) throws RepositoryException {
        Session session = getSystemSession(username);
        try {
            if (session.getWorkspace().getQueryManager() != null) {
            Query q = session.getWorkspace().getQueryManager().createQuery("SELECT * FROM jmix:usersFolder", Query.SQL);
            QueryResult qr = q.execute();
            NodeIterator ni = qr.getNodes();
            try {
                while (ni.hasNext()) {
                    Node usersFolderNode = ni.nextNode();
                    String options = "";
                    if (usersFolderNode.hasProperty("j:usersFolderConfig")) {
                        options = usersFolderNode.getProperty("j:usersFolderConfig").getString();
                    }

                    Node f = getPathFolder(usersFolderNode, username, options);

                    try {
                        f.getNode(username);
                    } catch (PathNotFoundException e) {
                        synchronized (this) {
                            try {
                                f.getNode(username);
                            } catch (PathNotFoundException ee) {
                                try {
                                    if (usersFolderNode.hasProperty("j:usersFolderSkeleton")) {
                                        session.importXML(f.getPath(), new FileInputStream(org.jahia.settings.SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/" + usersFolderNode.getProperty("j:usersFolderSkeleton").getString()),ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
                                        session.move(f.getPath()+"/user", f.getPath()+"/"+username);
                                        Node userNode = f.getNode(username);
                                        JCRNodeWrapperImpl.changePermissions(userNode, "u:"+username, "rw");
                                    } else {
                                        Node userNode = f.addNode(username, Constants.JAHIANT_USER_FOLDER);
                                        JCRNodeWrapperImpl.changePermissions(userNode, "u:"+username, "rw");
                                    }
                                    session.save();
                                } catch (RepositoryException e1) {
                                    logger.error("Cannot save", e1);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            }
        } finally {
            session.logout();
        }
    }

    private Node getPathFolder(Node root, String name, String options) throws RepositoryException {
        Node result = root;
        if (options.contains("initials")) {
            String s = "" + Character.toUpperCase(name.charAt(0));
            if (!result.hasNode(s)) {
                result = result.addNode(s,Constants.JAHIANT_SYSTEMFOLDER);
            } else {
                result = result.getNode(s);
            }
        }
        return result;
    }

    public List<JCRNodeWrapper> getUserFolders(String site, JahiaUser user) throws RepositoryException {
        Session session = getThreadSession(user);

        String username = ISO9075.encode(encodeInternalName(user.getUsername()));
        String xp = "//element("+ username +", jnt:userFolder)";

        if (site != null) {
            site = ISO9075.encode(encodeInternalName(site));
            xp = "//element("+site+", jnt:virtualsite)" + xp;
        }
        List<JCRNodeWrapper> results = queryFolders(user, session, xp);
        if (site != null) {
            results.addAll(getUserFolders(null, user));
        }
        return results;
    }

    public List<JCRNodeWrapper> getImportDropBoxes(String site, JahiaUser user) throws RepositoryException {
        Session session = getThreadSession(user);

        String username = ISO9075.encode(encodeInternalName(user.getUsername()));
        String xp = "//element("+ username +", jnt:userFolder)//element(*, jnt:importDropBox)";

        if (site != null) {
            site = ISO9075.encode(encodeInternalName(site));
            xp = "//element("+site+", jnt:virtualsite)" + xp;
        }
        List<JCRNodeWrapper> results = queryFolders(user, session, xp);
        if (site != null) {
            results.addAll(getImportDropBoxes(null, user));
        }
        return results;
    }

    public List<JCRNodeWrapper> getSiteFolders(String site, JahiaUser user) throws RepositoryException {
        Session session = getThreadSession(user);
        site = ISO9075.encode(encodeInternalName(site));
        String xp = "//element("+site+", jnt:virtualsite)";

        List<JCRNodeWrapper> results = queryFolders(user, session, xp);
        return results;
    }

    private List<JCRNodeWrapper> queryFolders(JahiaUser user, Session session, String xp) throws RepositoryException {
        List<JCRNodeWrapper> results = new ArrayList<JCRNodeWrapper>();
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        if (queryManager != null) {
            Query q = queryManager.createQuery(xp, Query.XPATH);
            QueryResult qr = q.execute();
            NodeIterator ni = qr.getNodes();
            while (ni.hasNext()) {
                Node folder = ni.nextNode();
                results.add(getNodeWrapper(folder, user, session));
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
        return isMainStorage;
    }

    public void setMainStorage(boolean mainStorage) {
        isMainStorage = mainStorage;
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
    
    public void export(String path, ContentHandler ch, JahiaUser user) {
        exportDocumentView(path, ch, user, true);
    }

    public void exportDocumentView(String path, ContentHandler ch, JahiaUser user, boolean noRecurse) {
        try {
            getThreadSession(user).exportDocumentView(path, ch, true, noRecurse);
        } catch (SAXException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void dump (Node n) throws RepositoryException {
        System.out.println(n.getPath());
        PropertyIterator pit=n.getProperties();
        while (pit.hasNext()) {
            Property p=pit.nextProperty();
            System.out.print(p.getPath()+"=");
            if (p.getDefinition().isMultiple()) {
                Value[] values = p.getValues();
                for (int i = 0; i < values.length; i++) {
                    Value value = values[i];
                    System.out.print(value+",");
                }
                System.out.println("");
            } else {
                System.out.println(p.getValue());
            }
        }
        NodeIterator nit=n.getNodes();
        while (nit.hasNext()) {
            Node cn=nit.nextNode();
            if (!cn.getName().startsWith("jcr:")) {
                dump (cn);
            }
        }
    }

    public QueryManager getQueryManager(JahiaUser user) {
        QueryManager queryManager = null;
        try {
            Session session = getThreadSession(user);
            queryManager = session.getWorkspace().getQueryManager();
            if (queryManager instanceof org.apache.jackrabbit.core.query.QueryManagerImpl){
                queryManager =
                        new JCRStoreQueryManagerAdapter((org.apache.jackrabbit.core.query.QueryManagerImpl)queryManager);
            }            
        } catch (RepositoryException e) {
            logger.error("Repository error", e);
        }
        return queryManager;
    }
    
    public ValueFactory getValueFactory(JahiaUser user) {
        ValueFactory valueFactory = null;
        try {
            Session session = getThreadSession(user);
            valueFactory = session.getValueFactory();
        } catch (RepositoryException e) {
            logger.error("Repository error", e);
        }
        return valueFactory;
    }

    public String encodeInternalName(String name) {
        name = name.replace("[", "\\5B");
        name = name.replace("]", "\\5C");
        name = name.replace("'", "\\27");
        return name;
    }

    public String decodeInternalName(String name) {
        name = name.replace("\\5B","[");
        name = name.replace("\\5C","]");
        name = name.replace("\\27","'");
        return name;
    }

}
