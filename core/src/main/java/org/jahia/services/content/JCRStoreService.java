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

import org.apache.jackrabbit.rmi.server.ServerAdapterFactory;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModel;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.jaas.JahiaLoginModule;
import org.jahia.jaas.JahiaPrincipal;
import org.jahia.params.ProcessingContext;
import org.jahia.services.JahiaService;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.webdav.UsageEntry;
import org.jahia.api.Constants;
import org.springframework.web.context.ServletContextAware;
import org.xml.sax.ContentHandler;

import javax.jcr.*;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

/**
 *
 *
 * User: toto
 * Date: 15 nov. 2007 - 15:18:34
 */
public class JCRStoreService extends JahiaService implements Repository, ServletContextAware {
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JCRStoreService.class);

    private Map<String,String> descriptors = new HashMap<String,String>();
    private Map<String,JCRStoreProvider> providers = new HashMap<String,JCRStoreProvider>();
    private List<JCRStoreProvider> providerList = new ArrayList<JCRStoreProvider>();
    private SortedMap<String,JCRStoreProvider> mountPoints = new TreeMap<String,JCRStoreProvider>();
    private SortedMap<String,JCRStoreProvider> dynamicMountPoints = new TreeMap<String,JCRStoreProvider>();
    private JahiaFieldXRefManager fieldXRefManager = null;
    private JahiaUserManagerService userService;
    private String servletContextAttributeName;
    private ServletContext servletContext;

    private Map<String,String> decorators = new HashMap<String,String>();

    private NamespaceRegistryWrapper namespaceRegistry;

    static private JCRStoreService instance = null;

    protected JCRStoreService() {
    }

    public synchronized static JCRStoreService getInstance() {
        if (instance == null) {
            instance = new JCRStoreService();
        }
        return instance;
    }

    public void setFieldXRefManager(JahiaFieldXRefManager fieldXRefManager) {
        this.fieldXRefManager = fieldXRefManager;
    }

    public void setUserService(JahiaUserManagerService userManagerService) {
        this.userService = userManagerService;
    }

    public void setServletContextAttributeName(String servletContextAttributeName) {
        this.servletContextAttributeName = servletContextAttributeName;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void start() throws JahiaInitializationException {
        try {
            NodeTypeRegistry.getInstance();

            Comparator<String> invertedStringComparator = new Comparator<String>() {
                public int compare(String s1, String s2) {
                    return s2.compareTo(s1);
                }
            };
            this.mountPoints = new TreeMap<String,JCRStoreProvider>(invertedStringComparator);
            this.dynamicMountPoints = new TreeMap<String,JCRStoreProvider>(invertedStringComparator);

            try {
                Registry r = LocateRegistry.createRegistry(2099);
                r.bind("jcrStoreService", new ServerAdapterFactory().getRemoteRepository(this));
            } catch (RemoteException e) {
                logger.error(e.getMessage(), e);
            }

            this.descriptors = new HashMap<String,String>();

            descriptors.put("jcr.specification.version" , "1.0");
            descriptors.put("jcr.specification.name" , "Content Repository API for Java(TM) Technology Specification");
            descriptors.put("jcr.repository.vendor" , "Jahia");
            descriptors.put("jcr.repository.vendor.url", "http://www.jahia.org/");
            descriptors.put("jcr.repository.name" , "UnitedContentBus");
            descriptors.put("jcr.repository.version" , "6.1");
            descriptors.put("level.1.supported" , "true");
            descriptors.put("level.2.supported" , "true");
            descriptors.put("option.transactions.supported" , "true");
            descriptors.put("option.versioning.supported" , "true");
            descriptors.put("option.observation.supported" , "false");
            descriptors.put("option.locking.supported" , "true");
            descriptors.put("option.query.sql.supported" , "true");
            descriptors.put("query.xpath.pos.index" , "true");
            descriptors.put("query.xpath.doc.order" , "false");

            namespaceRegistry = new NamespaceRegistryWrapper();

        } catch (Exception e){
            logger.error("Repository init error",e);
        }

        if ((servletContextAttributeName != null) &&
            (servletContext != null)) {
            servletContext.setAttribute(servletContextAttributeName, this);
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

    /**
     * @deprecated
     * @return
     */
    public JCRStoreProvider getMainStoreProvider() {
        return mountPoints.get("/");
    }

    public Map<String,JCRStoreProvider> getMountPoints() {
        return mountPoints;
    }

    public Map<String, JCRStoreProvider> getDynamicMountPoints() {
        return dynamicMountPoints;
    }

    public void deployDefinitions(String systemId) {
        for (JCRStoreProvider provider : providers.values()) {
            if (provider.canRegisterCustomNodeTypes()) {
                provider.deployDefinitions(systemId);
            }
        }
    }

    //    private ThreadLocal systemSession = new ThreadLocal();
    protected ThreadLocal<Map<String,Map<String,JCRSessionWrapper>>> userSession = new ThreadLocal<Map<String,Map<String,JCRSessionWrapper>>>();


    public JCRSessionWrapper getThreadSession(JahiaUser user) throws RepositoryException {
        return getThreadSession(user, null);
    }

    public JCRSessionWrapper getThreadSession(JahiaUser user, String workspace) throws RepositoryException {
        return getThreadSession(user, workspace, null);
    }

    public JCRSessionWrapper getThreadSession(JahiaUser user, String workspace, Locale locale) throws RepositoryException {
        // thread user session might be inited/closed in an http filter, instead of keeping it

        Map<String,Map<String,JCRSessionWrapper>> smap = userSession.get();

        if (smap == null) {
            smap = new HashMap<String,Map<String,JCRSessionWrapper>>();
        }
        userSession.set(smap);

        String username;

        if (JahiaUserManagerService.isGuest(user)) {
            username = JahiaLoginModule.GUEST;
        } else {
            username = user.getUsername();
        }

//        try {
//            if (s != null && !username.equals(s.getUserID())) {
//                logger.error("Session is switching user, was :"+ s.getUserID() + " now :" + username);
//                s.logout();
//            }
//        } catch (IllegalStateException e) {
//            logger.error("Exception on session : "+e);
//            s = null;
//        }
        Map<String,JCRSessionWrapper> wsMap = smap.get(username);
        if (wsMap == null) {
            wsMap = new HashMap<String,JCRSessionWrapper>();
            smap.put(username, wsMap);
        }

        if (workspace == null) {
            workspace = "default";
        }

        String localeString = "default";
        if (locale != null) {
            localeString = locale.toString();
        }

        JCRSessionWrapper s = wsMap.get(workspace+"-"+localeString);

        if (s == null || !s.isLive()) {
            if (!JahiaLoginModule.GUEST.equals(username)) {
                s = login(org.jahia.jaas.JahiaLoginModule.getCredentials(username), workspace, locale);
                // should be done somewhere else, call can be quite expensive
                deployExternalUser(username,user.getProviderName());
            } else {
                s = login(org.jahia.jaas.JahiaLoginModule.getGuestCredentials(), workspace, locale);
            }
            wsMap.put(workspace+"-"+localeString, s);
        } else {
            s.refresh(true);
        }
        return s;
    }

    public JCRSessionWrapper getSystemSession() throws RepositoryException {
        return login(JahiaLoginModule.getSystemCredentials());
    }

    public JCRSessionWrapper getSystemSession(String username) throws RepositoryException {
        return login(JahiaLoginModule.getSystemCredentials(username));
    }

    public JCRSessionWrapper getSystemSession(String username, String workspace) throws RepositoryException {
        return login(JahiaLoginModule.getSystemCredentials(username), workspace);
    }

    public void deployNewSite(JahiaSite site, JahiaUser user) throws RepositoryException {
        JCRStoreProvider provider = mountPoints.get("/");
        provider.deployNewSite(site, user);
    }

    public void deployExternalUser(String username, String providerName) throws RepositoryException {
        JCRStoreProvider provider = mountPoints.get("/");
        provider.deployExternalUser(username, providerName);
    }

    public void addProvider(String key, String mountPoint, JCRStoreProvider p) {
        providers.put(key, p);
        providerList.add(p);

        if (mountPoint != null) {
            if (p.isDynamicallyMounted()) {
                dynamicMountPoints.put(mountPoint,p);
            } else {
                mountPoints.put(mountPoint,p);
            }
        }
    }

    public void removeProvider(String key) {
        JCRStoreProvider p = providers.remove(key);
        providerList.remove(p);
        if (p != null && p.getMountPoint() != null) {
            mountPoints.remove(p.getMountPoint());
            dynamicMountPoints.remove(p.getMountPoint());
        }
    }

    public boolean unmount(JCRStoreProvider p) {
        if (p != null && p.isDynamicallyMounted()) {
            p.stop();
            return true;
        }
        return false;
    }

    /**
     * @deprecated Use getThreadSession().getNode()
     */
    public JCRNodeWrapper getFileNode(String path, JahiaUser user) {
        if (path != null) {
            if (path.startsWith("/")) {
                for (Iterator<String> iterator = dynamicMountPoints.keySet().iterator(); iterator.hasNext();) {
                    String mp = iterator.next();
                    if (path.startsWith(mp+"/")) {
                        String localPath = path.substring(mp.length());
                        JCRStoreProvider provider = dynamicMountPoints.get(mp);
                        return provider.getNodeWrapper(provider.getRelativeRoot()+ localPath, user);
                    }
                }
                for (Iterator<String> iterator = mountPoints.keySet().iterator(); iterator.hasNext();) {
                    String mp = iterator.next();
                    if (mp.equals("/") || path.equals(mp) || path.startsWith(mp+"/")) {
                        String localPath = path;
                        if (!mp.equals("/")) {
                            localPath = path.substring(mp.length());
                        }
                        JCRStoreProvider provider = mountPoints.get(mp);
                        if (localPath.equals("")) {
                            localPath = "/";
                        }
                        return provider.getNodeWrapper(provider.getRelativeRoot()+localPath, user);
                    }
                }
                return null;
            } else if (path.length()>0 && path.contains(":")) {
                int index = path.indexOf(":");
                String key = path.substring(0,index);
                String localPath = path.substring(index+1);
                JCRStoreProvider provider = providers.get(key);
                if (provider != null) {
                    return provider.getNodeWrapper(provider.getRelativeRoot()+localPath, user);
                }
            }
        }
        return new JCRNodeWrapperImpl("?", null, null);
    }

    public Map<String, JCRStoreProvider> getProviders() {
        return providers;
    }

    /**
     * Returns a list of providers ordered by registration order. This is important because some providers
     * are more "low-level" than others.
     * @return an ORDERED list of providers
     */
    public List<JCRStoreProvider> getProviderList() {
        return providerList;
    }

    public JCRStoreProvider getProvider(String path) {
        for (Iterator<String> iterator = dynamicMountPoints.keySet().iterator(); iterator.hasNext();) {
            String mp = iterator.next();
            if (path.startsWith(mp+"/")) {
                return dynamicMountPoints.get(mp);
            }
        }
        for (Iterator<String> iterator = mountPoints.keySet().iterator(); iterator.hasNext();) {
            String mp = iterator.next();
            if (mp.equals("/") || path.equals(mp) || path.startsWith(mp+"/")) {
                return mountPoints.get(mp);
            }
        }
        return null;
    }

    public void export(String path, ContentHandler ch, JahiaUser user) {
        getProvider(path).export(path, ch , user);
    }

    public List<JCRNodeWrapper> getUserFolders(String site, JahiaUser user) {
        List<JCRNodeWrapper> r = new ArrayList<JCRNodeWrapper>();
        for (JCRStoreProvider storeProvider : getMountPoints().values()) {
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
        for (JCRStoreProvider storeProvider : getMountPoints().values()) {
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
        for (JCRStoreProvider storeProvider : getMountPoints().values()) {
                try {
                    r.addAll(storeProvider.getSiteFolders(site, user));
                } catch (RepositoryException e) {
                    logger.warn("Error when querying repository",e);
                }
        }
        return r;
    }

    public void closeAllSessions() {
        Map<String,Map<String,JCRSessionWrapper>> smap = userSession.get();
        if (smap != null) {
            for (Map<String,JCRSessionWrapper> wsMap : smap.values()) {
                for (JCRSessionWrapper s : wsMap.values()) {
                    s.logout();
                }
            }
            userSession.set(null);
        }
    }

    public List<UsageEntry> findUsages(String sourceUri, boolean onlyLockedUsages) {
        return findUsages (sourceUri, Jahia.getThreadParamBean(), onlyLockedUsages);
    }

    public List<UsageEntry> findUsages (String sourceUri, ProcessingContext jParams,
                            boolean onlyLockedUsages) {
        return findUsages (sourceUri, Jahia.getThreadParamBean(), onlyLockedUsages,null);
    }
        public List<UsageEntry> findUsages (String sourceUri, ProcessingContext jParams,
                            boolean onlyLockedUsages, String versionName) {
        List<UsageEntry> res = new ArrayList<UsageEntry>();
        if (fieldXRefManager == null) {
            fieldXRefManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());
        }

        Collection<JahiaFieldXRef> c = fieldXRefManager.getReferencesForTarget(JahiaFieldXRefManager.FILE+sourceUri);

        for (Iterator<JahiaFieldXRef> iterator = c.iterator(); iterator.hasNext();) {
            JahiaFieldXRef jahiaFieldXRef = iterator.next();
            try {
                if (!onlyLockedUsages || jahiaFieldXRef.getComp_id().getWorkflow() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    int version = 0;
                    if (jahiaFieldXRef.getComp_id().getWorkflow() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                        version = ContentField.getField(jahiaFieldXRef.getComp_id().getFieldId()).getActiveVersionID();
                    }
                    UsageEntry entry = new UsageEntry(jahiaFieldXRef.getComp_id().getFieldId(), version, jahiaFieldXRef.getComp_id().getWorkflow(), jahiaFieldXRef.getComp_id().getLanguage(), jahiaFieldXRef.getComp_id().getTarget().substring(JahiaFieldXRefManager.FILE.length()), jParams);
                    if(versionName!=null) {
                        entry.setVersionName(versionName);
                    }
                    res.add(entry);
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return res;
    }

    public static String removeDiacritics(String name) {
        if (name == null) return null;
        StringBuffer sb = new StringBuffer(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c>='\u0080') {
                if      (c >= '\u00C0' && c < '\u00C6') sb.append('A');
                else if (c == '\u00C6'                ) sb.append("AE");
                else if (c == '\u00C7'                ) sb.append('C');
                else if (c >= '\u00C8' && c < '\u00CC') sb.append('E');
                else if (c >= '\u00CC' && c < '\u00D0') sb.append('I');
                else if (c == '\u00D0'                ) sb.append('D');
                else if (c == '\u00D1'                ) sb.append('N');
                else if (c >= '\u00D2' && c < '\u00D7') sb.append('O');
                else if (c == '\u00D7'                ) sb.append('x');
                else if (c == '\u00D8'                ) sb.append('O');
                else if (c >= '\u00D9' && c < '\u00DD') sb.append('U');
                else if (c == '\u00DD'                ) sb.append('Y');
                else if (c == '\u00DF'                ) sb.append("SS");
                else if (c >= '\u00E0' && c < '\u00E6') sb.append('a');
                else if (c == '\u00E6'                ) sb.append("ae");
                else if (c == '\u00E7'                ) sb.append('c');
                else if (c >= '\u00E8' && c < '\u00EC') sb.append('e');
                else if (c >= '\u00EC' && c < '\u00F0') sb.append('i');
                else if (c == '\u00F0'                ) sb.append('d');
                else if (c == '\u00F1'                ) sb.append('n');
                else if (c >= '\u00F2' && c < '\u00FF') sb.append('o');
                else if (c == '\u00F7'                ) sb.append('/');
                else if (c == '\u00F8'                ) sb.append('o');
                else if (c >= '\u00F9' && c < '\u00FF') sb.append('u');
                else if (c == '\u00FD'                ) sb.append('y');
                else if (c == '\u00FF'                ) sb.append("y");
                else if (c == '\u0152'                ) sb.append("OE");
                else if (c == '\u0153'                ) sb.append("oe");
                else sb.append('_');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public JCRNodeWrapper decorate(JCRNodeWrapper w) {
        try {
            for (String type : decorators.keySet()) {
                if (w.isNodeType(type)) {
                    String className = decorators.get(type);
                    try {
                        return (JCRNodeWrapper) Class.forName(className).getConstructor(JCRNodeWrapper.class).newInstance(w);
                    } catch (Exception e) {
                        logger.error("Cannot decorate node",e);
                    }
                }
            }
//            if (w.isNodeType(Constants.NT_FILE)) {
//                return new JCRFileNode(w);
//            } else if (w.isNodeType(Constants.NT_FOLDER)) {
//                return new JCRFileNode(w);
//            } else if (w.isNodeType(Constants.JAHIANT_PORTLET)) {
//                return new JCRPortletNode(w);
//            } else if (w.isNodeType(Constants.NT_QUERY)) {
//                return new JCRQueryNode(w);
//            } else if (w.isNodeType(Constants.JAHIANT_MOUNTPOINT)) {
//                return new JCRMountPointNode(w);
//            } else if (w.isNodeType(Constants.JAHIANT_JAHIACONTENT)) {
//                return new JCRJahiaContentNode(w);
//            }
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return w;
    }

    public JCRNodeWrapper getNodeByUUID(String uuid, JahiaUser user) throws ItemNotFoundException, RepositoryException {
        return getThreadSession(user).getNodeByUUID(uuid);
    }

    public JCRNodeWrapper getNodeByUUID(String providerKey, String uuid, JahiaUser user) throws ItemNotFoundException, RepositoryException {
        return getThreadSession(user).getNodeByUUID(providerKey, uuid);
    }

    public QueryManager getQueryManager(JahiaUser user) {
        try {
            return getThreadSession(user).getWorkspace().getQueryManager();
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public QueryManager getQueryManager(JahiaUser user,ProcessingContext context) {
        try {
            return getThreadSession(user).getWorkspace().getQueryManager(context);
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public QueryManager getQueryManager(JahiaUser user,ProcessingContext context,Properties properties) {
        try {
            return getThreadSession(user).getWorkspace().getQueryManager(context, properties);
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    /**
     *
     * @param queryObjectModel
     * @param user
     * @return
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public QueryResult execute(QueryObjectModel queryObjectModel, JahiaUser user) throws InvalidQueryException,
                    RepositoryException {
        return getThreadSession(user).getWorkspace().execute(queryObjectModel);
    }


    public String[] getDescriptorKeys() {
        return descriptors.keySet().toArray(new String[descriptors.size()]);
    }

    public String getDescriptor(String s) {
        return descriptors.get(s);
    }

    public JCRSessionWrapper login(Credentials credentials, String workspace) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return login(credentials, workspace, null);
    }

    public JCRSessionWrapper login(Credentials credentials, String workspace, Locale locale) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        if (!(credentials instanceof SimpleCredentials)) {
            throw new LoginException("Only SimpleCredentials supported in this implementation");
        }

        final SimpleCredentials simpleCreds = (SimpleCredentials) credentials;

        JahiaLoginModule m = new JahiaLoginModule();
        Subject s = new Subject();
        HashMap<String,?> sharedState = new HashMap<String,Object>();
        HashMap<String,?> options = new HashMap<String,Object>();
        m.initialize(s, new CallbackHandler() {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        ((NameCallback)callback).setName(simpleCreds.getUserID());
                    } else if (callback instanceof PasswordCallback) {
                        ((PasswordCallback)callback).setPassword(simpleCreds.getPassword());
                    } else {
                        throw new UnsupportedCallbackException(callback);
                    }
                }
            }
        }, sharedState, options);

        try {
            JahiaLoginModule.Token t = JahiaLoginModule.getToken(simpleCreds.getUserID(), new String(simpleCreds.getPassword()));
            m.login();
            m.commit();
            credentials = JahiaLoginModule.getCredentials(simpleCreds.getUserID(), t != null ? t.deniedPath : null);
        } catch (javax.security.auth.login.LoginException e) {
            throw new LoginException(e);
        }

        Set<JahiaPrincipal> p = s.getPrincipals(JahiaPrincipal.class);
        for (JahiaPrincipal jahiaPrincipal : p) {
            JahiaUser user = null;
            if (!jahiaPrincipal.getName().equals(JahiaLoginModule.SYSTEM)) {
                if (jahiaPrincipal.isGuest()) {
                    user = userService.lookupUser(JahiaUserManagerService.GUEST_USERNAME);
                } else {
                    user = userService.lookupUser(jahiaPrincipal.getName());
                }
            }
            return new JCRSessionWrapper(user, credentials, jahiaPrincipal.isSystem(), workspace, locale, this);
        }
        throw  new LoginException("Can't login");
    }

    public JCRSessionWrapper login(Credentials credentials) throws LoginException, RepositoryException {
        return login(credentials, null);
    }

    public JCRSessionWrapper login(String workspace) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return login(JahiaLoginModule.getGuestCredentials(), workspace);
    }

    public JCRSessionWrapper login() throws LoginException, RepositoryException {
        return login(null, null);
    }

    /**
     * Check existence of a given path in the repository.
     *
     * @param path the path to check
     * @param user the current user
     * @return the node if it exists, null otherwise
     * @throws javax.jcr.RepositoryException an exception occured while retrieving the node
     */
    public JCRNodeWrapper checkExistence(String path, JahiaUser user) throws RepositoryException {
        try {
            JCRNodeWrapper node = getThreadSession(user).getNode(path);
            if (node != null && node.isValid()) {
                return node;
            }
        } catch (RepositoryException e) {
            if (!(e instanceof PathNotFoundException)) {
                throw e;
            }
        }
        return null;
    }

    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
        return namespaceRegistry;
    }

    /**
     * Search in all sub nodes and properties the referenced nodes and get the list of nodes where the publication
     * should stop ( currently sub pages, sub folders and sub files ).
     *
     * @param start The root node where to start the search
     * @param pruneNodes Empty list, will be filled with the list of sub nodes that should not be part of the publication
     * @param referencedNode Empty list, will be filled with the list of nodes referenced in the sub tree
     * @param languages The list of languages to publish, null to publish all
     * @throws RepositoryException
     */
    private void getBlockedAndReferencesList(Node start, Set<String> pruneNodes, Set<String> referencedNode, Set<String> languages) throws RepositoryException {
        PropertyIterator pi = start.getProperties();
        while (pi.hasNext()) {
            Property p = pi.nextProperty();
            if ((p.getType() == PropertyType.REFERENCE || p.getType() == ExtendedPropertyType.WEAKREFERENCE) && !p.getName().startsWith("jcr:")) {
                if (p.getDefinition().isMultiple()) {
                    Value[] vs = p.getValues();
                    for (Value v : vs) {
                        referencedNode.add(start.getSession().getNodeByUUID(v.getString()).getPath());
                    }
                } else {
                    referencedNode.add(p.getNode().getPath());
                }
            } else if ((p.getType() == PropertyType.REFERENCE || p.getType() == ExtendedPropertyType.WEAKREFERENCE)) {
                System.out.println("-->"+p.getName());
            }
        }
        NodeIterator ni = start.getNodes();
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            if (n.isNodeType("jnt:page") || n.isNodeType("jnt:folder") || n.isNodeType("jnt:file")) {
                pruneNodes.add(n.getPath());
            } else if (languages != null && n.isNodeType("jnt:translation")) {
                String lang = n.getProperty("jcr:language").getString();
                if (languages.contains(lang)) {
                    getBlockedAndReferencesList(n, pruneNodes, referencedNode, languages);
                } else {
                    pruneNodes.add(n.getPath());
                }
            } else {
                getBlockedAndReferencesList(n, pruneNodes, referencedNode, languages);
            }
        }
    }

    /**
     * Publish a node into the live workspace.
     * Referenced nodes will also be published.
     * Parent node must be published, or will be published if publishParent is true.
     *
     * @param path Path of the node to publish
     * @param languages
     * @param user the user
     * @param publishParent Recursively publish the parents
     */
    public void publish(String path, Set<String> languages, JahiaUser user,  boolean publishParent) throws RepositoryException {
        JCRSessionWrapper session = getThreadSession(user);
        JCRNodeWrapper w = session.getNode(path);

        String parentPath = w.getParent().getPath();
        JCRSessionWrapper liveSession = getThreadSession(user, "live");
        try {
            liveSession.getNode(parentPath);
        } catch (PathNotFoundException e) {
            if (publishParent) {
                publish(parentPath, languages, user, true);
            } else {
                return;
            }
        }

        Set<String> blocked = new HashSet<String>();
        Set<String> referencedNodes = new HashSet<String>();

        getBlockedAndReferencesList(w, blocked, referencedNodes, languages);

        for (String node : referencedNodes) {
            publish(node, languages, user, true);
        }

        List<String> deniedPathes = new ArrayList<String>();
        for (String node : blocked) {
            deniedPathes.add(node);
        }

        JCRSessionWrapper liveSessionForPublish = login(JahiaLoginModule.getSystemCredentials(user.getUsername(), deniedPathes), Constants.LIVE_WORKSPACE);

        try {
            Node liveNode = liveSessionForPublish.getNode(path);
            liveNode.update(Constants.EDIT_WORKSPACE);
        } catch (PathNotFoundException e) {
            try {
                liveSessionForPublish.getWorkspace().clone(Constants.EDIT_WORKSPACE, path, path, true);
            } catch (RepositoryException ee) {
                ee.printStackTrace();
            }
        }

        liveSessionForPublish.logout();
    }

    /**
     * Unpublish a node from live workspace.
     * Referenced Node will not be unpublished.
     *
     * @param path path of the node to unpublish
     * @param languages
     * @param user
     * @throws RepositoryException
     */
    public void unpublish(String path, Set<String> languages, JahiaUser user) throws RepositoryException {
        JCRSessionWrapper session = getThreadSession(user);
        JCRNodeWrapper w = session.getNode(path);

        String parentPath = w.getParent().getPath();
        JCRSessionWrapper liveSession = getThreadSession(user, Constants.LIVE_WORKSPACE);
        final JCRNodeWrapper parentNode = liveSession.getNode(parentPath);
        final JCRNodeWrapper node = liveSession.getNode(path);
        node.remove();
        parentNode.save();
        liveSession.logout();
    }

}
