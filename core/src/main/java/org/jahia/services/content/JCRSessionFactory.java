/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import org.apache.jackrabbit.core.JahiaSessionImpl;
import org.apache.jackrabbit.core.security.JahiaCallbackHandler;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.api.Constants;
import org.jahia.jaas.JahiaPrincipal;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.query.QueryWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.readonlymode.ReadOnlyModeCapable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.context.ServletContextAware;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.security.auth.Subject;
import javax.servlet.ServletContext;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The entry point into the content repositories provided by the <code>JCRStoreProvider</code> list.
 *
 * Instead of using this class for creating and using sessions, please rather use the JCRTemplate.
 *
 * @author toto
 * @see JCRTemplate
 */
public class JCRSessionFactory implements Repository, ServletContextAware, ReadOnlyModeCapable {

    private static final Comparator<String> invertedStringComparator = new Comparator<String>() {

        @Override
        public int compare(String s1, String s2) {
            return s2.compareTo(s1);
        }
    };

    public static final String DEFAULT_PROVIDER_KEY = "default";

    private static transient Logger logger = LoggerFactory.getLogger(JCRSessionFactory.class);

    protected ThreadLocal<Map<String, Map<String, JCRSessionWrapper>>> userSession = new ThreadLocal<Map<String, Map<String, JCRSessionWrapper>>>();
    protected ThreadLocal<Map<String, Map<String, JCRSessionWrapper>>> systemSession = new ThreadLocal<Map<String, Map<String, JCRSessionWrapper>>>();
    private NamespaceRegistryWrapper namespaceRegistry;
    private Map<String, String> descriptors = new HashMap<String, String>();
    private JahiaUserManagerService userService;
    private Map<String, JCRStoreProvider> providers = new HashMap<String, JCRStoreProvider>();
    private List<JCRStoreProvider> providerList = new LinkedList<JCRStoreProvider>();
    private SortedMap<String, JCRStoreProvider> mountPoints;
    private String servletContextAttributeName;
    private ServletContext servletContext;
    private ThreadLocal<JahiaUser> currentUser = new ThreadLocal<JahiaUser>();
    private ThreadLocal<Locale> currentLocale = new ThreadLocal<Locale>();
    private ThreadLocal<Locale> fallbackLocale = new ThreadLocal<Locale>();
    private ThreadLocal<JahiaUser> currentAliasedUser = new ThreadLocal<JahiaUser>();
    private ThreadLocal<String> currentServletPath = new ThreadLocal<String>();
    private ThreadLocal<Calendar> currentPreviewDate = new ThreadLocal<Calendar>();
    private ThreadLocal<Boolean> readOnlyCacheEnabled = new ThreadLocal<Boolean>();
    private LocalValidatorFactoryBean validatorFactoryBean;
    private boolean readOnlyModeEnabled;
    private final ReadWriteLock readOnlyModeLock = new ReentrantReadWriteLock();

    private JCRSessionFactory() {
        super();
    }

    public void start() {
        synchronized (this) {
            mountPoints = new TreeMap<String, JCRStoreProvider>(invertedStringComparator);
        }
        namespaceRegistry = new NamespaceRegistryWrapper();

        if ((servletContextAttributeName != null) && (servletContext != null)) {
            servletContext.setAttribute(servletContextAttributeName, this);
        }
    }

    public void setServletContextAttributeName(String servletContextAttributeName) {
        this.servletContextAttributeName = servletContextAttributeName;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setDescriptors(Map<String, String> descriptors) {
        this.descriptors = descriptors;
    }

    public void setUserService(JahiaUserManagerService userService) {
        this.userService = userService;
    }


    public JCRSessionWrapper getCurrentUserSession() throws RepositoryException {
        return getCurrentUserSession(null);
    }

    public JCRSessionWrapper getCurrentUserSession(String workspace) throws RepositoryException {
        return getCurrentUserSession(workspace, null);
    }

    public JCRSessionWrapper getCurrentUserSession(String workspace, Locale locale) throws RepositoryException {
        return getCurrentUserSession(workspace, locale, locale != null ? getFallbackLocale() : null);
    }

    public JCRSessionWrapper getCurrentUserSession(String workspace, Locale locale, Locale fallbackLocale) throws RepositoryException {
        return getCurrentSession(workspace, locale, fallbackLocale, false);
    }

    public JCRSessionWrapper getCurrentUserSession(String workspace, Locale locale, Locale fallbackLocale, boolean enableCaching) throws RepositoryException {
        return getCurrentSession(workspace, locale, fallbackLocale, false, enableCaching);
    }

    public JCRSessionWrapper getCurrentSystemSession(String workspace, Locale locale, Locale fallbackLocale) throws RepositoryException {
        return getCurrentSession(workspace, locale, fallbackLocale, true);
    }

    public JCRSessionWrapper getCurrentSystemSession(String workspace, Locale locale, Locale fallbackLocale, boolean enableCaching) throws RepositoryException {
        return getCurrentSession(workspace, locale, fallbackLocale, true, enableCaching);
    }

    public JCRSessionWrapper getCurrentSession(String workspace, Locale locale, Locale fallbackLocale, boolean system) throws RepositoryException {
        return getCurrentSession(workspace, locale, fallbackLocale, system, true);
    }

    public JCRSessionWrapper getCurrentSession(String workspace, Locale locale, Locale fallbackLocale, boolean system, boolean enableCaching) throws RepositoryException {
        // thread user session might be initialized/closed in an HTTP filter, instead of keeping it
        ThreadLocal<Map<String, Map<String, JCRSessionWrapper>>> sessionThreadLocal = system ? systemSession : userSession;

        Map<String, Map<String, JCRSessionWrapper>> smap = sessionThreadLocal.get();
        if (smap == null) {
            smap = new HashMap<String, Map<String, JCRSessionWrapper>>();
        }
        sessionThreadLocal.set(smap);
        String username;

        JahiaUser user = !system ? getCurrentUser() : null;

        if (!system && user == null) {
            logger.error("Null thread user");
            throw new RepositoryException("Null thread user");
        }

        if (JahiaUserManagerService.isGuest(user)) {
            username = JahiaLoginModule.GUEST;
        } else {
            username = user.getUsername();
        }

        Map<String, JCRSessionWrapper> wsMap = smap.get(username);
        if (wsMap == null) {
            wsMap = new HashMap<String, JCRSessionWrapper>();
            smap.put(username, wsMap);
        }

        if (workspace == null) {
            workspace = Constants.EDIT_WORKSPACE;
        }

        String localeString = locale != null ? locale.toString() : "default";

        final String key = workspace + "-" + localeString + "-" + fallbackLocale;
        JCRSessionWrapper s = wsMap.get(key);

        if (s == null || !s.isLive()) {
            if (system) {
                s = login(JahiaLoginModule.getSystemCredentials(), workspace, locale, fallbackLocale);
                s.setCurrentUserSession(true);
                wsMap.put(key, s);
            } else {
                if (!JahiaLoginModule.GUEST.equals(username)) {
                    s = login(JahiaLoginModule.getCredentials(username, user.getRealm()), workspace, locale, fallbackLocale);
                } else {
                    s = login(JahiaLoginModule.getGuestCredentials(), workspace, locale, fallbackLocale);
                }
                s.setCurrentUserSession(true);
                wsMap.put(key, s);
            }
        }

        if (!enableCaching) {
            // Enabled by default, only need to check for disabling
            s.disableSessionCache();
        }
        s.setReadOnlyCacheEnabled(getReadOnlyCacheEnabled());

        return s;
    }

    protected JCRSessionWrapper getSystemSession() throws RepositoryException {
        return login(JahiaLoginModule.getSystemCredentials());
    }

    protected JCRSessionWrapper getSystemSession(String username, String realm, String workspace, Locale locale) throws RepositoryException {
        return login(JahiaLoginModule.getSystemCredentials(username, realm), workspace, locale, locale != null ? getFallbackLocale() : null);
    }

    protected JCRSessionWrapper getUserSession(String username, String realm, String workspace, Locale locale) throws RepositoryException {
        return login(JahiaLoginModule.getCredentials(username, realm), workspace, locale, locale != null ? getFallbackLocale() : null);
    }

    @Override
    public String[] getDescriptorKeys() {
        return descriptors.keySet().toArray(new String[descriptors.size()]);
    }

    @Override
    public String getDescriptor(String s) {
        return descriptors.get(s);
    }

    public Session findSameSession(JCRStoreProvider provider, String userID, String ws, boolean system) throws RepositoryException {
        Session s = null;
        ThreadLocal<Map<String, Map<String, JCRSessionWrapper>>> sessionThreadLocal = system ? systemSession : userSession;

        if (sessionThreadLocal != null) {
            Map<String, Map<String, JCRSessionWrapper>> smap = sessionThreadLocal.get();
            if (smap != null && smap.containsKey(userID)) {
                Map<String, JCRSessionWrapper> wsMap = smap.get(userID);
                for (String key : wsMap.keySet()) {
                    if (key.startsWith(ws)) {
                        if ((s = wsMap.get(key).getProviderSession(provider, false)) != null) {
                            break;
                        }
                    }
                }
            }
        }
        return s;
    }

    public Set<JCRSessionWrapper> getAllOpenUserSessions() {
        return getAllOpenSessions(userSession);
    }

    public Set<JCRSessionWrapper> getAllOpenSystemSessions() {
        return getAllOpenSessions(systemSession);
    }

    private Set<JCRSessionWrapper> getAllOpenSessions(ThreadLocal<Map<String, Map<String, JCRSessionWrapper>>> mapThreadLocal) {
        Set<JCRSessionWrapper> sessions = new HashSet<>();
        Map<String, Map<String, JCRSessionWrapper>> smap = mapThreadLocal.get();
        if (smap != null) {
            for (Map<String, JCRSessionWrapper> wsMap : smap.values()) {
                sessions.addAll(wsMap.values());
            }
        }
        return sessions;
    }

    @Override
    public JCRSessionWrapper login(Credentials credentials, String workspace)
            throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return login(credentials, workspace, null, null);
    }

    private JCRSessionWrapper login(Credentials credentials, String workspace, Locale locale, Locale fallbackLocale)
            throws LoginException, NoSuchWorkspaceException, RepositoryException {
        if (!(credentials instanceof SimpleCredentials)) {
            throw new LoginException("Only SimpleCredentials supported in this implementation");
        }

        final SimpleCredentials simpleCreds = (SimpleCredentials) credentials;

        JahiaLoginModule m = new JahiaLoginModule();
        Subject s = new Subject();
        m.initialize(s, new JahiaCallbackHandler(simpleCreds), null, null);

        try {
            JahiaLoginModule.Token t = JahiaLoginModule.getToken(simpleCreds.getUserID(), new String(
                    simpleCreds.getPassword()));
            m.login();
            m.commit();
            credentials = JahiaLoginModule.getCredentials(simpleCreds.getUserID(), (String) simpleCreds.getAttribute(JahiaLoginModule.REALM_ATTRIBUTE), t != null ? t.deniedPath : null);
        } catch (javax.security.auth.login.LoginException e) {
            throw new LoginException(e);
        }

        Set<JahiaPrincipal> p = s.getPrincipals(JahiaPrincipal.class);
        for (JahiaPrincipal jahiaPrincipal : p) {
            JahiaUser user = null;
            if (!jahiaPrincipal.getName().startsWith(JahiaLoginModule.SYSTEM)) {
                JCRUserNode userNode;
                if (jahiaPrincipal.isGuest()) {
                    userNode = userService.lookupUser(JahiaUserManagerService.GUEST_USERNAME);
                } else {
                    userNode = userService.lookupUser(jahiaPrincipal.getName(), jahiaPrincipal.getRealm(), false);
                }
                if (userNode != null) {
                    user = userNode.getJahiaUser();
                } else {
                    logger.warn("Cannot find user " + jahiaPrincipal.getName() + "@" + jahiaPrincipal.getRealm());
                }
            }
            return new JCRSessionWrapper(user, credentials, jahiaPrincipal.isSystem(), workspace, locale, this, fallbackLocale);
        }
        throw new LoginException("Can't login");
    }

    @Override
    public JCRSessionWrapper login(Credentials credentials) throws LoginException, RepositoryException {
        return login(credentials, null);
    }

    @Override
    public JCRSessionWrapper login(String workspace)
            throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return login(JahiaLoginModule.getGuestCredentials(), workspace);
    }

    @Override
    public JCRSessionWrapper login() throws LoginException, RepositoryException {
        return login(null, null);
    }

    @Override
    public boolean isStandardDescriptor(String key) {
        return false;
    }

    @Override
    public boolean isSingleValueDescriptor(String key) {
        return false;
    }

    @Override
    public Value getDescriptorValue(String key) {
        return null;
    }

    @Override
    public Value[] getDescriptorValues(String key) {
        return new Value[0];
    }

    /**
     * Returns a <strong>read-only</strong> view of the known mount points.
     * @return a <strong>read-only</strong> view of the known mount points.
     */
    public Map<String, JCRStoreProvider> getMountPoints() {
        return mountPoints;
    }

    public Map<String, JCRStoreProvider> getProviders() {
        return providers;
    }

    public JCRStoreProvider getDefaultProvider() {
        return getProviders().get(DEFAULT_PROVIDER_KEY);
    }

    /**
     * Registers the JCR store provider.
     *
     * @param p the provider instance
     */
    public void addProvider(JCRStoreProvider p) {
        String key = p.getKey();
        String mountPoint = p.getMountPoint();

        synchronized (this) {
            for (JCRSessionWrapper wrapper : JCRSessionWrapper.getActiveSessionsObjects().values()) {
                try {
                    wrapper.getCache().removeNode(mountPoint);
                } catch (RepositoryException e) {
                    logger.warn("Cannot flush cache", e);
                }
            }

            List<JCRStoreProvider> newList = new ArrayList<JCRStoreProvider>(providerList);
            newList.add(p);
            Collections.sort(newList);
            providerList = Collections.unmodifiableList(newList);

            initProviders();

            if (mountPoint != null) {
                SortedMap<String, JCRStoreProvider> newMountPoints = new TreeMap<String, JCRStoreProvider>(mountPoints);
                newMountPoints.put(mountPoint, p);
                mountPoints = Collections.unmodifiableSortedMap(newMountPoints);
            }
        }
        logger.info("Added provider " + key + " at mount point " + mountPoint + " using implementation "
                + p.getClass().getName());
    }

    /**
     * Registers the JCR store provider.
     *
     * @param key        the key of the provider
     * @param mountPoint provider's mount point
     * @param p          the provider instance
     * @deprecated use {@link #addProvider(JCRStoreProvider)} instead
     */
    @Deprecated(since = "7.1.0.0", forRemoval = true)
    public void addProvider(String key, String mountPoint, JCRStoreProvider p) {
        addProvider(p);
    }

    /**
     * Unregister a provider
     *
     * @param key the key of the provider
     */
    public void removeProvider(String key) {
        JCRStoreProvider p = getProviders().get(key);
        if (p == null) {
            return;
        }
        synchronized (this) {
            for (JCRSessionWrapper wrapper : JCRSessionWrapper.getActiveSessionsObjects().values()) {
                try {
                    wrapper.getCache().removeNode(p.getMountPoint());
                } catch (RepositoryException e) {
                    logger.warn("Cannot flush cache", e);
                }
            }

            List<JCRStoreProvider> newList = new ArrayList<JCRStoreProvider>(providerList);
            newList.remove(p);
            providerList = Collections.unmodifiableList(newList);

            initProviders();

            if (p.getMountPoint() != null) {
                SortedMap<String, JCRStoreProvider> newMountPoints = new TreeMap<String, JCRStoreProvider>(mountPoints);
                newMountPoints.remove(p.getMountPoint());
                mountPoints = Collections.unmodifiableSortedMap(newMountPoints);
            }
        }
        logger.info("Removed provider " + key + " at mount point " + p.getMountPoint() + " using implementation " + p.getClass().getName());
    }

    private void initProviders() {
        Map<String, JCRStoreProvider> providerMap = new LinkedHashMap<String, JCRStoreProvider>(providerList.size());
        for (JCRStoreProvider p : providerList) {
            providerMap.put(p.getKey(), p);
        }
        providers = Collections.unmodifiableMap(providerMap);
    }

    /**
     * Returns a list of providers ordered by registration order. This is important because some providers
     * are more "low-level" than others.
     *
     * @return an ORDERED list of providers
     */
    public List<JCRStoreProvider> getProviderList() {
        return providerList;
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final JCRSessionFactory INSTANCE = new JCRSessionFactory();
    }

    public static JCRSessionFactory getInstance() {
        return Holder.INSTANCE;
    }

    public void closeAllSessions() {
        closeAllSessions(userSession);
        closeAllSessions(systemSession);
    }

    private void closeAllSessions(ThreadLocal<Map<String, Map<String, JCRSessionWrapper>>> mapThreadLocal) {
        Map<String, Map<String, JCRSessionWrapper>> smap = mapThreadLocal.get();
        if (smap != null) {
            try {
                for (Map<String, JCRSessionWrapper> wsMap : smap.values()) {
                    for (JCRSessionWrapper s : wsMap.values()) {
                        if (s.isLive()) {
                            try {
                                s.doLogout();
                            } catch (Exception e) {
                                logger.warn("Error performing JCR session logout for sesison " + s, e);
                            }
                        }
                    }
                }
            } finally {
                mapThreadLocal.set(null);
                smap.clear();
            }
        }
    }

    /**
     * Returns the provider which is handling the provided node path. If there is no other provider which can handle the specified node
     * path, the default Jackrabbit ("/") provider is returned.
     *
     * @param path
     *            the node path to be checked
     * @return the provider which handles the provided node path or the default provider if there is no other provider which can handle the
     *         specified node path
     */
    public JCRStoreProvider getProvider(String path) {
        return getProvider(path, true);
    }

    /**
     * Returns the provider which is handling the provided node path.
     *
     * @param path
     *            the node path to be checked
     * @param includeDefault
     *            if <code>true</code> the default provider is considered; otherwise only the non-default providers (others that "/") are
     *            checked.
     * @return the provider which handles the provided node path or null if there is no such provider; note, please, if
     *         <code>includeDefault</code> parameter is passed with true value the default provider is returned if there is no other
     *         provider which can handle the specified node path
     */
    public JCRStoreProvider getProvider(String path, boolean includeDefault) {
        Map<String, JCRStoreProvider> currentMountPoints = getMountPoints();
        if (includeDefault && currentMountPoints.size() == 1) {
            return getDefaultProvider();
        }
        for (Map.Entry<String, JCRStoreProvider> mp : currentMountPoints.entrySet()) {
            if ((includeDefault && mp.getValue().isDefault()) || path.equals(mp.getKey())
                    || path.startsWith(mp.getKey() + "/")) {
                return mp.getValue();
            }
        }
        return null;
    }

    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
        return namespaceRegistry;
    }

    public JahiaUser getCurrentUser() {
        return currentUser.get();
    }

    public void setCurrentUser(JahiaUser user) {
        currentUser.set(user);
    }

    public Locale getCurrentLocale() {
        return currentLocale.get();
    }

    public void setCurrentLocale(Locale locale) {
        currentLocale.set(locale);
    }

    public Locale getFallbackLocale() {
        return fallbackLocale.get();
    }

    public void setFallbackLocale(Locale locale) {
        fallbackLocale.set(locale);
    }

    public JahiaUser getCurrentAliasedUser() {
        return currentAliasedUser.get();
    }

    public void setCurrentAliasedUser(JahiaUser user) {
        currentAliasedUser.set(user);
    }

    public Boolean getReadOnlyCacheEnabled() {
        return readOnlyCacheEnabled.get() == null ? false : readOnlyCacheEnabled.get();
    }

    public void setReadOnlyCacheEnabled(Boolean readOnlyCacheEnabled) {
        this.readOnlyCacheEnabled.set(readOnlyCacheEnabled);
    }

    boolean checkAliasedStatusAndToggleSessionIfNeeded(Session session, final JahiaUser user) {
        final JahiaUser currentAliasedUser = getCurrentAliasedUser();
        if (user != null && currentAliasedUser != null && currentAliasedUser.equals(user)) {
            if (session instanceof JahiaSessionImpl) {
                ((JahiaSessionImpl) session).toggleThisSessionAsAliased();
            }
            return true;
        }
        return false;
    }

    public String getCurrentServletPath() {
        return currentServletPath.get();
    }

    public void setCurrentServletPath(String path) {
        currentServletPath.set(path);
    }

    public void setCurrentPreviewDate(Calendar previewDate) {
        currentPreviewDate.set(previewDate);
    }

    public Calendar getCurrentPreviewDate() {
        return currentPreviewDate.get();
    }

    public LocalValidatorFactoryBean getValidatorFactoryBean() {
        return validatorFactoryBean;
    }

    public void setValidatorFactoryBean(LocalValidatorFactoryBean validatorFactoryBean) {
        this.validatorFactoryBean = validatorFactoryBean;
    }

    /**
     * Returns <code>true</code> if more than one (default) mount points are registered.
     *
     * @return <code>true</code> if more than one (default) mount points are registered; <code>false</code> otherwise
     */
    public boolean areMultipleMountPointsRegistered() {
        return mountPoints.size() > 1;
    }

    @Override
    public int getReadOnlyModePriority() {
        return 200;
    }

    @Override
    public void switchReadOnlyMode(boolean enable) {
        readOnlyModeLock.writeLock().lock();
        try {
            logger.info("Read only mode switch: JCR session are" + (enable ? " not " : " ") + "allowed to perform saving");

            this.readOnlyModeEnabled = enable;

            // we will unlock all the nodes that are locked because of opened engines
            if (enable) {
                try {
                    clearEngineLocks(true);
                } catch (Exception e) {
                    logger.warn("Unable to clear the engine locks while switching Read only mode to ON", e);
                }
            }

            logger.info("Read only mode on JCR sessions: " + (this.readOnlyModeEnabled ? "ON" : "OFF"));

        } finally {
            readOnlyModeLock.writeLock().unlock();
        }
    }

    boolean isReadOnlyModeEnabled() {
        readOnlyModeLock.readLock().lock();
        try {
            return readOnlyModeEnabled;
        } finally {
            readOnlyModeLock.readLock().unlock();
        }
    }

    private void clearEngineLocks(boolean ignoreReadOnlyMode) throws RepositoryException {
        JCRSessionWrapper systemSession = null;
        try {
            systemSession = getSystemSession();
            systemSession.setIgnoreReadOnlyMode(ignoreReadOnlyMode);

            QueryWrapper engineLockedQuery = systemSession.getWorkspace().getQueryManager().createQuery(
                    "select * from [jmix:lockable] where isdescendantnode('/sites') and [j:lockTypes] like '%:engine'",
                    Query.JCR_SQL2);
            QueryResult engineLockedQueryResult = engineLockedQuery.execute();
            final NodeIterator nodeIterator = engineLockedQueryResult.getNodes();

            while (nodeIterator.hasNext()) {
                clearEngineLocks(nodeIterator.nextNode(), systemSession);
            }
        } finally {
            if (systemSession != null) {
                systemSession.logout();
            }
        }
    }

    private void clearEngineLocks(Node node, JCRSessionWrapper session) {
        try {
            if (node instanceof JCRNodeWrapper && !node.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                for (NodeIterator ni = ((JCRNodeWrapper) node).getI18Ns(); ni.hasNext();) {
                    clearEngineLocks(ni.nextNode(), session);
                }
            }
            if (node.hasProperty("j:lockTypes")) {
                for (Value lockTypeValue : node.getProperty("j:lockTypes").getValues()) {
                    // lockTypes format is like: root:engine
                    String[] lockTypeInfos = StringUtils.split(lockTypeValue.getString(), ":");
                    if (lockTypeInfos.length >= 1 && StringUtils.equals(lockTypeInfos[1], "engine")) {
                        logger.info("Clearing engine lock for node {} and user {} before switching Read only mode",
                                node.getPath(), lockTypeInfos[0]);
                        JCRNodeWrapperImpl.unlock(node, lockTypeInfos[1], lockTypeInfos[0], session);
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("Unable to check and clean engine locks for node " + node, ex);
        }
    }
}
