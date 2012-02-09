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

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.xml.SystemViewExporter;
import org.apache.jackrabbit.core.JahiaSessionImpl;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.jahia.api.Constants;
import org.jahia.services.content.impl.jackrabbit.JackrabbitStoreProvider;
import org.jahia.services.importexport.DocumentViewExporter;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.usermanager.JahiaUser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Jahia specific wrapper around <code>javax.jcr.Session</code> to be able to inject
 * Jahia specific actions and to manage sessions to multiple repository providers in
 * the backend.
 * <p/>
 * Jahia services should use this wrapper rather than the original session interface to
 * ensure that we manipulate wrapped nodes and not the ones from the underlying
 * implementation.
 *
 * @author toto
 */
public class JCRSessionWrapper implements Session {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRSessionWrapper.class);
    public static final String DEREF_SEPARATOR = "@/";

    private JCRSessionFactory sessionFactory;
    private JahiaUser user;
    private Credentials credentials;
    private JCRWorkspaceWrapper workspace;
    private boolean isLive = true;
    private Locale locale;
    private List<String> tokens = new ArrayList<String>();

    private Map<JCRStoreProvider, Session> sessions = new HashMap<JCRStoreProvider, Session>();

    private Map<String, JCRNodeWrapper> sessionCacheByPath = new HashMap<String, JCRNodeWrapper>();
    private Map<String, JCRNodeWrapper> sessionCacheByIdentifier = new HashMap<String, JCRNodeWrapper>();
    private Map<String, JCRNodeWrapper> newNodes = new HashMap<String, JCRNodeWrapper>();

    private Map<String, String> nsToPrefix = new HashMap<String, String>();
    private Map<String, String> prefixToNs = new HashMap<String, String>();

    private Map<String, String> uuidMapping = new HashMap<String, String>();
    private Map<String, String> pathMapping = new LinkedHashMap<String, String>();

    private boolean isSystem;
    private Date versionDate;

    private Locale fallbackLocale;
    private String versionLabel;

    private static AtomicLong activeSessions = new AtomicLong(0L);

    public JCRSessionWrapper(JahiaUser user, Credentials credentials, boolean isSystem, String workspace, Locale locale,
                             JCRSessionFactory sessionFactory, Locale fallbackLocale) {
        this.user = user;
        this.credentials = credentials;
        this.isSystem = isSystem;
        this.versionDate = null;
        this.versionLabel = null;
        if (workspace == null) {
            this.workspace = new JCRWorkspaceWrapper("default", this, sessionFactory);
        } else {
            this.workspace = new JCRWorkspaceWrapper(workspace, this, sessionFactory);
        }
        this.locale = locale;
        this.fallbackLocale = fallbackLocale;
        this.sessionFactory = sessionFactory;
        activeSessions.incrementAndGet();
    }


    public JCRNodeWrapper getRootNode() throws RepositoryException {
        JCRStoreProvider provider = sessionFactory.getProvider("/");
        return provider.getNodeWrapper(getProviderSession(provider).getRootNode(), "/", null, this);
    }

    public Repository getRepository() {
        return sessionFactory;
    }

    public String getUserID() {
        return ((SimpleCredentials) credentials).getUserID();
    }

    public boolean isSystem() {
        return isSystem;
    }

    public Object getAttribute(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getAttributeNames() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public JCRWorkspaceWrapper getWorkspace() {
        return workspace;
    }

    public Locale getLocale() {
        return locale;
    }

    //    public void setInterceptorsEnabled(boolean interceptorsEnabled) {
//        this.interceptorsEnabled = interceptorsEnabled;
//    }

    public Session impersonate(Credentials credentials) throws LoginException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public JCRNodeWrapper getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException {
        return getNodeByUUID(uuid, true);
    }

    public JCRNodeWrapper getNodeByUUID(final String uuid, final boolean checkVersion)
            throws ItemNotFoundException, RepositoryException {

        if (sessionCacheByIdentifier.containsKey(uuid)) {
            return sessionCacheByIdentifier.get(uuid);
        }
        RepositoryException originalEx = null;
        for (JCRStoreProvider provider : sessionFactory.getProviderList()) {
            if (!provider.isInitialized()) {
                logger.debug("Provider " + provider.getKey() + " / " + provider.getClass().getName() +
                        " is not yet initialized, skipping...");
                continue;
            }
            if (provider instanceof JackrabbitStoreProvider && JCRContentUtils.isNotJcrUuid(uuid)) {
                // not a valid UUID, probably a VFS node
                continue;
            }
            try {
                Session session = getProviderSession(provider);
                if (session instanceof JahiaSessionImpl && getUser() != null &&
                        sessionFactory.getCurrentAliasedUser() != null &&
                        sessionFactory.getCurrentAliasedUser().equals(getUser())) {
                    ((JahiaSessionImpl) session).toggleThisSessionAsAliased();
                }
                Node n = session.getNodeByIdentifier(uuid);
                JCRNodeWrapper wrapper = provider.getNodeWrapper(n, this);
                if (getUser() != null && sessionFactory.getCurrentAliasedUser() != null &&
                        !sessionFactory.getCurrentAliasedUser().equals(getUser())) {
                    JCRTemplate.getInstance()
                            .doExecuteWithUserSession(sessionFactory.getCurrentAliasedUser().getUsername(),
                                    session.getWorkspace().getName(), getLocale(), new JCRCallback<Object>() {
                                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                            return session.getNodeByUUID(uuid, checkVersion);
                                        }
                                    });
                }
                if (checkVersion  && (versionDate != null || versionLabel != null)) {
                    wrapper = getFrozenVersionAsRegular(n, provider);
                }
                sessionCacheByIdentifier.put(uuid, wrapper);
                sessionCacheByPath.put(wrapper.getPath(), wrapper);

                return wrapper;
            } catch (ItemNotFoundException ee) {
                // All good
                if (originalEx == null) {
                    originalEx = ee;
                }
            } catch (UnsupportedRepositoryOperationException uso) {
                logger.debug(
                        "getNodeByUUID unsupported by : " + provider.getKey() + " / " + provider.getClass().getName());
                if (originalEx == null) {
                    originalEx = uso;
                }
            } catch (RepositoryException ex) {
                if (originalEx == null) {
                    originalEx = ex;
                }
                logger.warn(
                        "repository exception : " + provider.getKey() + " / " + provider.getClass().getName() + " : " +
                                ex.getMessage());
            }
        }
        if (originalEx != null) {
            if (originalEx instanceof ItemNotFoundException) {
                throw originalEx;
            } else {
                throw new ItemNotFoundException(uuid, originalEx);
            }
        }
        
        throw new ItemNotFoundException(uuid);
    }

    public JCRNodeWrapper getNodeByUUID(String providerKey, String uuid)
            throws ItemNotFoundException, RepositoryException {
        JCRStoreProvider provider = sessionFactory.getProviders().get(providerKey);
        if (provider == null) {
            throw new ItemNotFoundException(uuid);
        }
        Session session = getProviderSession(provider);
        Node n = session.getNodeByIdentifier(uuid);
        return provider.getNodeWrapper(n, this);
    }

    public JCRItemWrapper getItem(String path) throws PathNotFoundException, RepositoryException {
        return getItem(path, true);
    }

    public JCRItemWrapper getItem(String path, final boolean checkVersion)
            throws PathNotFoundException, RepositoryException {
        if (sessionCacheByPath.containsKey(path)) {
            return sessionCacheByPath.get(path);
        }
        if (path.contains(DEREF_SEPARATOR)) {
            JCRNodeWrapper parent = (JCRNodeWrapper) getItem(StringUtils.substringBeforeLast(path, DEREF_SEPARATOR), checkVersion);
            return dereference(parent, StringUtils.substringAfterLast(path, DEREF_SEPARATOR));
        }
        Map<String, JCRStoreProvider> dynamicMountPoints = sessionFactory.getDynamicMountPoints();
        for (Map.Entry<String, JCRStoreProvider> mp : dynamicMountPoints.entrySet()) {
            if (path.startsWith(mp.getKey() + "/")) {
                String localPath = path.substring(mp.getKey().length());
                JCRStoreProvider provider = mp.getValue();
                Item item = getProviderSession(provider).getItem(provider.getRelativeRoot() + localPath);
                if (item.isNode()) {
                    return provider.getNodeWrapper((Node) item, localPath, null, this);
                } else {
                    return provider.getPropertyWrapper((Property) item, this);
                }
            }
        }
        Map<String, JCRStoreProvider> mountPoints = sessionFactory.getMountPoints();
        for (Map.Entry<String, JCRStoreProvider> mp : mountPoints.entrySet()) {
            String key = mp.getKey();
            if (key.equals("/") || path.equals(key) || path.startsWith(key + "/")) {
                String localPath = path;
                if (!key.equals("/")) {
                    localPath = localPath.substring(key.length());
                }
                JCRStoreProvider provider = mp.getValue();
                if (localPath.equals("")) {
                    localPath = "/";
                }
//                Item item = getProviderSession(provider).getItem(localPath);
                Session session = getProviderSession(provider);
                if (session instanceof JahiaSessionImpl && getUser() != null &&
                        sessionFactory.getCurrentAliasedUser() != null &&
                        sessionFactory.getCurrentAliasedUser().equals(getUser())) {
                    ((JahiaSessionImpl) session).toggleThisSessionAsAliased();
                }
                Item item = session.getItem(provider.getRelativeRoot() + localPath);
                if (item.isNode()) {
                    final Node node = (Node) item;
                    JCRNodeWrapper wrapper = provider.getNodeWrapper(node, localPath, null, this);
                    if (getUser() != null && sessionFactory.getCurrentAliasedUser() != null &&
                            !sessionFactory.getCurrentAliasedUser().equals(getUser())) {
                        final JCRNodeWrapper finalWrapper = wrapper;
                        JCRTemplate.getInstance()
                                .doExecuteWithUserSession(sessionFactory.getCurrentAliasedUser().getUsername(),
                                        getWorkspace().getName(), getLocale(), new JCRCallback<Object>() {
                                            public Object doInJCR(JCRSessionWrapper session)
                                                    throws RepositoryException {
                                                return session.getNodeByUUID(finalWrapper.getIdentifier(), checkVersion);
                                            }
                                        });
                    }
                    if (checkVersion && (versionDate != null || versionLabel != null) && node.isNodeType("mix:versionable")) {
                        wrapper = getFrozenVersionAsRegular(node, provider);
                    }
                    sessionCacheByPath.put(path, wrapper);
                    sessionCacheByIdentifier.put(wrapper.getIdentifier(), wrapper);

                    return wrapper;
                } else {
                    return provider.getPropertyWrapper((Property) item, this);
                }
            }
        }
        throw new PathNotFoundException(path);
    }

    private JCRNodeWrapper dereference(JCRNodeWrapper parent, String refPath) throws RepositoryException {
        JCRStoreProvider provider = parent.getProvider();
        JCRNodeWrapper wrapper;
        Node referencedNode = parent.getRealNode().getProperty("j:node").getNode();
        String fullPath = parent.getPath() + DEREF_SEPARATOR + refPath;
        if (parent.getPath().startsWith(referencedNode.getPath())) {
            throw new PathNotFoundException(fullPath);
        }
        String refRootName = StringUtils.substringBefore(refPath, "/");
        if (!referencedNode.getName().equals(refRootName)) {
            throw new PathNotFoundException(fullPath);
        }
        refPath = StringUtils.substringAfter(refPath, "/");
        if (refPath.equals("")) {
            wrapper = provider.getNodeWrapper(referencedNode, fullPath, parent, this);
        } else {
            Node node = referencedNode.getNode(refPath);
            wrapper = provider.getNodeWrapper(node, fullPath, null, this);
        }
        sessionCacheByPath.put(fullPath, wrapper);
        return wrapper;
    }
    
    public JCRNodeWrapper getNode(String path) throws PathNotFoundException, RepositoryException {
        return getNode(path, true);
    }

    public JCRNodeWrapper getNode(String path, boolean checkVersion) throws PathNotFoundException, RepositoryException {
        JCRItemWrapper item = getItem(path, checkVersion);
        if (item.isNode()) {
            return (JCRNodeWrapper) item;
        } else {
            throw new PathNotFoundException();
        }
    }

    public boolean itemExists(String path) throws RepositoryException {
        try {
            getItem(path);
            return true;
        } catch (RepositoryException e) {
            return false;
        }
    }

    public void move(String source, String dest)
            throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException,
            LockException, RepositoryException {
        getWorkspace().move(source, dest, true);
        if (sessionCacheByPath.containsKey(source)) {
            JCRNodeWrapper n = sessionCacheByPath.get(source);
            if (n instanceof JCRNodeDecorator) {
                n = ((JCRNodeDecorator)n).getDecoratedNode();
            }
            ((JCRNodeWrapperImpl)n).localPath = dest;
            ((JCRNodeWrapperImpl)n).localPathInProvider = dest;
        }
        flushCaches();
    }

    public void save()
            throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException,
            VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        save(JCRObservationManager.SESSION_SAVE);
    }

    void registerNewNode(JCRNodeWrapper node) {
        newNodes.put(node.getPath(), node);
    }

    void unregisterNewNode(JCRNodeWrapper node) {
        if (!newNodes.isEmpty()) {
            newNodes.remove(node.getPath());
            try {
                if (node.hasNodes()) {
                    NodeIterator it = node.getNodes();
                    while (it.hasNext()) {
                        unregisterNewNode((JCRNodeWrapper) it.next());
                    }
                }
            } catch (RepositoryException e) {
                logger.warn("Error unregistering new nodes", e);
            }
        }
    }    
    
    public void save(final int operationType)
            throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException,
            VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        if (!isSystem() && getLocale() != null) {
            for (JCRNodeWrapper node : newNodes.values()) {
                for (String s : node.getNodeTypes()) {
                    ExtendedPropertyDefinition[] propDefs = NodeTypeRegistry.getInstance().getNodeType(s).getPropertyDefinitions();
                    for (ExtendedPropertyDefinition propDef : propDefs) {
                        if (propDef.isMandatory() &&
                                propDef.getRequiredType() != PropertyType.WEAKREFERENCE &&
                                propDef.getRequiredType() != PropertyType.REFERENCE &&
                                !propDef.isProtected() && !node.hasProperty(propDef.getName())) {
                            throw new ConstraintViolationException("Mandatory field : "+propDef.getName());
                        }
                    }
                }
            }
        }
        newNodes.clear();

        JCRObservationManager.doWorkspaceWriteCall(this, operationType, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper thisSession) throws RepositoryException {
                for (Session session : sessions.values()) {
                    session.save();
                }
                return null;
            }
        });
    }

    public void refresh(boolean b) throws RepositoryException {
        for (Session session : sessions.values()) {
            session.refresh(b);
        }
    }

    public boolean hasPendingChanges() throws RepositoryException {
        for (Session session : sessions.values()) {
            if (session.hasPendingChanges()) {
                return true;
            }
        }
        return false;
    }

    public ValueFactory getValueFactory() {
        return ValueFactoryImpl.getInstance();
    }

    /**
     * Normally determines whether this <code>Session</code> has permission to perform
     * the specified actions at the specified <code>absPath</code>.
     * This method is not supported.
     *
     * @param absPath an absolute path.
     * @param actions a comma separated list of action strings.
     * @throws UnsupportedRepositoryOperationException
     *          as long as Jahia doesn't support it
     */
    public void checkPermission(String absPath, String actions) throws AccessControlException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public ContentHandler getImportContentHandler(String s, int i)
            throws PathNotFoundException, ConstraintViolationException, VersionException, LockException,
            RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void importXML(String path, InputStream inputStream, int uuidBehavior)
            throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException,
            VersionException, InvalidSerializedDataException, LockException, RepositoryException {
        importXML(path, inputStream, uuidBehavior, DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE);
    }

    public void importXML(String path, InputStream inputStream, int uuidBehavior, int rootBehavior)
            throws IOException, InvalidSerializedDataException, RepositoryException {
        importXML(path, inputStream, uuidBehavior, rootBehavior, null);
    }

    public void importXML(String path, InputStream inputStream, int uuidBehavior, int rootBehavior, Map<String,String> replacements)
            throws IOException, InvalidSerializedDataException, RepositoryException {
        JCRNodeWrapper node = getNode(path);
        try {
            if (!node.isCheckedOut()) {
                checkout(node);
            }
        } catch (UnsupportedRepositoryOperationException ex) {
            // versioning not supported
        }

        DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(this, path);
        documentViewImportHandler.setRootBehavior(rootBehavior);
        documentViewImportHandler.setUuidBehavior(uuidBehavior);
        documentViewImportHandler.setReplacements(replacements);
        try {
            SAXParserFactory factory;

            factory = new SAXParserFactoryImpl();

            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            SAXParser parser = factory.newSAXParser();

            parser.parse(inputStream, documentViewImportHandler);
        } catch (SAXParseException e) {
            logger.error("Cannot import - File is not a valid XML", e);
        } catch (Exception e) {
            logger.error("Cannot import", e);
        }
    }

    /**
     * Applies the namespace prefix to the appropriate sessions, including the underlying provider sessions.
     * @param prefix
     * @param uri
     * @throws NamespaceException
     * @throws RepositoryException
     */
    public void setNamespacePrefix(String prefix, String uri) throws NamespaceException, RepositoryException {
        nsToPrefix.put(uri, prefix);
        prefixToNs.put(prefix, uri);
        for (Session s : sessions.values()) {
            s.setNamespacePrefix(prefix, uri);
            try {
                NamespaceRegistry nsReg = s.getWorkspace().getNamespaceRegistry();
                if (nsReg != null) {
                    nsReg.registerNamespace(prefix, uri);
                }
            } catch (RepositoryException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Prefix/uri could not be registered in workspace's registry- " + prefix + "/" + uri,
                            e);
                }
            }
        }
    }

    public String[] getNamespacePrefixes() throws RepositoryException {
        Set<String> wsPrefixes =
                new HashSet<String>(Arrays.asList(getWorkspace().getNamespaceRegistry().getPrefixes()));
        wsPrefixes.addAll(prefixToNs.keySet());
        return wsPrefixes.toArray(new String[wsPrefixes.size()]);
    }

    public String getNamespaceURI(String prefix) throws NamespaceException, RepositoryException {
        if (prefixToNs.containsKey(prefix)) {
            return prefixToNs.get(prefix);
        }
        return getWorkspace().getNamespaceRegistry().getURI(prefix);
    }

    public String getNamespacePrefix(String uri) throws NamespaceException, RepositoryException {
        if (nsToPrefix.containsKey(uri)) {
            return nsToPrefix.get(uri);
        }
        return getWorkspace().getNamespaceRegistry().getPrefix(uri);
    }

    public void logout() {
        for (Session session : sessions.values()) {
            session.logout();
        }
        if (credentials instanceof SimpleCredentials) {
            SimpleCredentials simpleCredentials = (SimpleCredentials) credentials;
            JahiaLoginModule.removeToken(simpleCredentials.getUserID(), new String(simpleCredentials.getPassword()));
        }
        isLive = false;
        activeSessions.decrementAndGet();
    }

    public boolean isLive() {
        return isLive;
    }

    /**
     * Adds the specified lock token to the wrapped sessions. Holding a
     * lock token makes the <code>Session</code> the owner of the lock
     * specified by that particular lock token.
     *
     * @param token a lock token (a string).
     * @deprecated As of JCR 2.0, {@link LockManager#addLockToken(String)}
     *             should be used instead.
     */
    public void addLockToken(String token) {
        tokens.add(token);
        for (Session session : sessions.values()) {
            session.addLockToken(token);
        }
    }

    public String[] getLockTokens() {
        List<String> allTokens = new ArrayList<String>(tokens);
        for (Session session : sessions.values()) {
            String[] tokens = session.getLockTokens();
            for (String token : tokens) {
                if (!allTokens.contains(token)) {
                    allTokens.add(token);
                }
            }
        }
        return allTokens.toArray(new String[allTokens.size()]);
    }

    public void removeLockToken(String token) {
        tokens.remove(token);
        for (Session session : sessions.values()) {
            session.removeLockToken(token);
        }
    }

    /**
     * Get sessions from all providers used in this wrapper.
     *
     * @return a <code>Collection</code> of <code>JCRSessionWrapper</code> objects
     */
    public Collection<Session> getAllSessions() {
        return sessions.values();
    }

    public Session getProviderSession(JCRStoreProvider provider) throws RepositoryException {
        if (sessions.get(provider) == null) {
            Session s = null;

            if (credentials instanceof SimpleCredentials) {
                SimpleCredentials simpleCredentials = (SimpleCredentials) credentials;
                JahiaLoginModule.Token t = JahiaLoginModule
                        .getToken(simpleCredentials.getUserID(), new String(simpleCredentials.getPassword()));

                s = provider.getSession(credentials, workspace.getName());

                credentials =
                        JahiaLoginModule.getCredentials(simpleCredentials.getUserID(), t != null ? t.deniedPath : null);
            } else {
                s = provider.getSession(credentials, workspace.getName());
            }

            sessions.put(provider, s);
            for (String token : tokens) {
                s.addLockToken(token);
            }

            NamespaceRegistry namespaceRegistryWrapper = getWorkspace().getNamespaceRegistry();
            NamespaceRegistry providerNamespaceRegistry = s.getWorkspace().getNamespaceRegistry();


            if (providerNamespaceRegistry != null) {
                for (String prefix : namespaceRegistryWrapper.getPrefixes()) {
                    try {
                        providerNamespaceRegistry.getURI(prefix);
                    } catch (NamespaceException ne) {
                        providerNamespaceRegistry.registerNamespace(prefix, namespaceRegistryWrapper.getURI(prefix));
                    }
                }
            }

            for (String prefix : prefixToNs.keySet()) {
                s.setNamespacePrefix(prefix, prefixToNs.get(prefix));
            }
        }
        return sessions.get(provider);
    }

    public JahiaUser getUser() {
        return user;
    }

    public JahiaUser getAliasedUser() {
        return sessionFactory.getCurrentAliasedUser();
    }

    public Calendar getPreviewDate() {
        return sessionFactory.getCurrentPreviewDate();
    }
    /**
     * Generates a document view export using a {@link org.apache.jackrabbit.commons.xml.DocumentViewExporter}
     * instance.
     *
     * @param path       of the node to be exported
     * @param handler    handler for the SAX events of the export
     * @param skipBinary whether binary values should be skipped
     * @param noRecurse  whether to export just the identified node
     * @throws PathNotFoundException if a node at the given path does not exist
     * @throws SAXException          if the SAX event handler failed
     * @throws RepositoryException   if another error occurs
     */
    public void exportDocumentView(String path, ContentHandler handler, boolean skipBinary, boolean noRecurse)
            throws PathNotFoundException, SAXException, RepositoryException {
        DocumentViewExporter exporter = new DocumentViewExporter(this, handler, skipBinary, noRecurse);
        Item item = getItem(path);
        if (item.isNode()) {
            exporter.export((JCRNodeWrapper) item);
        } else {
            throw new PathNotFoundException("XML export is not defined for properties: " + path);
        }
    }

    /**
     * Generates a system view export using a {@link org.apache.jackrabbit.commons.xml.SystemViewExporter}
     * instance.
     *
     * @param path       of the node to be exported
     * @param handler    handler for the SAX events of the export
     * @param skipBinary whether binary values should be skipped
     * @param noRecurse  whether to export just the identified node
     * @throws PathNotFoundException if a node at the given path does not exist
     * @throws SAXException          if the SAX event handler failed
     * @throws RepositoryException   if another error occurs
     */
    public void exportSystemView(String path, ContentHandler handler, boolean skipBinary, boolean noRecurse)
            throws PathNotFoundException, SAXException, RepositoryException {

        //todo implement our own system view .. ?
        SystemViewExporter exporter = new SystemViewExporter(this, handler, !noRecurse, !skipBinary);
        Item item = getItem(path);
        if (item.isNode()) {
            exporter.export((JCRNodeWrapper) item);
        } else {
            throw new PathNotFoundException("XML export is not defined for properties: " + path);
        }
    }

    /**
     * Calls {@link Session#exportDocumentView(String, ContentHandler, boolean, boolean)}
     * with the given arguments and a {@link ContentHandler} that serializes
     * SAX events to the given output stream.
     *
     * @param absPath    passed through
     * @param out        output stream to which the SAX events are serialized
     * @param skipBinary passed through
     * @param noRecurse  passed through
     * @throws IOException         if the SAX serialization failed
     * @throws RepositoryException if another error occurs
     */
    public void exportDocumentView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse)
            throws IOException, RepositoryException {
        try {
            ContentHandler handler = getExportContentHandler(out);
            exportDocumentView(absPath, handler, skipBinary, noRecurse);
        } catch (SAXException e) {
            Exception exception = e.getException();
            if (exception instanceof RepositoryException) {
                throw (RepositoryException) exception;
            } else if (exception instanceof IOException) {
                throw (IOException) exception;
            } else {
                throw new RepositoryException("Error serializing document view XML", e);
            }
        }
    }

    /**
     * Calls {@link Session#exportSystemView(String, ContentHandler, boolean, boolean)}
     * with the given arguments and a {@link ContentHandler} that serializes
     * SAX events to the given output stream.
     *
     * @param absPath    passed through
     * @param out        output stream to which the SAX events are serialized
     * @param skipBinary passed through
     * @param noRecurse  passed through
     * @throws IOException         if the SAX serialization failed
     * @throws RepositoryException if another error occurs
     */
    public void exportSystemView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse)
            throws IOException, RepositoryException {
        try {
            ContentHandler handler = getExportContentHandler(out);
            exportSystemView(absPath, handler, skipBinary, noRecurse);
        } catch (SAXException e) {
            Exception exception = e.getException();
            if (exception instanceof RepositoryException) {
                throw (RepositoryException) exception;
            } else if (exception instanceof IOException) {
                throw (IOException) exception;
            } else {
                throw new RepositoryException("Error serializing system view XML", e);
            }
        }
    }

    /**
     * Creates a {@link ContentHandler} instance that serializes the
     * received SAX events to the given output stream.
     *
     * @param stream output stream to which the SAX events are serialized
     * @return SAX content handler
     * @throws RepositoryException if an error occurs
     */
    private ContentHandler getExportContentHandler(OutputStream stream) throws RepositoryException {
        try {
            SAXTransformerFactory stf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler handler = stf.newTransformerHandler();

            Transformer transformer = handler.getTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            handler.setResult(new StreamResult(stream));
            return handler;
        } catch (TransformerFactoryConfigurationError e) {
            throw new RepositoryException("SAX transformer implementation not available", e);
        } catch (TransformerException e) {
            throw new RepositoryException("Error creating an XML export content handler", e);
        }
    }

    public JCRNodeWrapper getNodeByIdentifier(String id) throws ItemNotFoundException, RepositoryException {
        return getNodeByUUID(id);
    }

    public Property getProperty(String absPath) throws PathNotFoundException, RepositoryException {
        return (Property) getItem(absPath);
    }

    public boolean nodeExists(String absPath) throws RepositoryException {
        return itemExists(absPath);
    }

    public boolean propertyExists(String absPath) throws RepositoryException {
        return itemExists(absPath);
    }

    public void removeItem(String absPath)
            throws VersionException, LockException, ConstraintViolationException, AccessDeniedException,
            RepositoryException {
        JCRItemWrapper item = getItem(absPath);
        boolean flushNeeded = false;
        if (item.isNode()) {
            JCRNodeWrapper node = (JCRNodeWrapper) item;
            unregisterNewNode(node);
            if (node.hasNodes()) {
                flushNeeded = true;
            }
        }
        item.remove();
        if (flushNeeded) {
            flushCaches();
        } else {
            removeFromCache(item);
        }
    }

    void removeFromCache(JCRItemWrapper item) throws RepositoryException {
        sessionCacheByPath.remove(item.getPath());
        if (item instanceof JCRNodeWrapper) {
            sessionCacheByIdentifier.remove(((JCRNodeWrapper) item).getIdentifier());
        }
    }

    public boolean hasPermission(String absPath, String actions) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean hasCapability(String s, Object o, Object[] objects) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Returns the access control manager for this <code>Session</code>.
     * <p/>
     * Jahia throws an <code>UnsupportedRepositoryOperationException</code>.
     *
     * @return the access control manager for this <code>Session</code>
     * @throws UnsupportedRepositoryOperationException
     *          if access control
     *          is not supported.
     * @since JCR 2.0
     */
    public AccessControlManager getAccessControlManager()
            throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public RetentionManager getRetentionManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Map<String, String> getStoredPasswordsProviders() {
        Map<String, String> results = new HashMap<String, String>();
        results.put(null, user.getUsername());
        for (JCRStoreProvider provider : sessionFactory.getProviders().values()) {
            if ("storedPasswords".equals(provider.getAuthenticationType())) {
                results.put(provider.getKey(), user.getProperty("storedUsername_" + provider.getKey()));
            }
        }
        return results;
    }

    public void storePasswordForProvider(String providerKey, String username, String password) {
        if (username == null) {
            user.removeProperty("storedUsername_" + providerKey);
        } else {
            user.setProperty("storedUsername_" + providerKey, username);
        }
        if (password == null) {
            user.removeProperty("storedPassword_" + providerKey);
        } else {
            user.setProperty("storedPassword_" + providerKey, password);
        }
    }

    /**
     * Performs check out of the specified node.
     *
     * @param node the node to perform the check out
     * @see VersionManager#checkout(String) for details
     */
    public void checkout(Node node) throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        while (!node.isCheckedOut()) {
            if (!node.isNodeType("mix:versionable") && !node.isNodeType("mix:simpleVersionable")) {
                node = node.getParent();
            } else {
                String absPath = node.getPath();
                VersionManager versionManager = getWorkspace().getVersionManager();
                if (!versionManager.isCheckedOut(absPath)) {
                    versionManager.checkout(absPath);
                }
                return;
            }
        }
    }

    public Map<String, String> getUuidMapping() {
        return uuidMapping;
    }

    public Map<String, String> getPathMapping() {
        return pathMapping;
    }

    public Locale getFallbackLocale() {
        return fallbackLocale;
    }

    public Date getVersionDate() {
        return versionDate;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public void setVersionDate(Date versionDate) {
        if (this.versionDate == null) {
            this.versionDate = versionDate;
        } else {
            throw new RuntimeException("Should not change versionDate on a session in same thread");
        }
    }

    public void setVersionLabel(String versionLabel) {
        if (this.versionLabel == null) {
            if (versionLabel != null && !versionLabel.startsWith(getWorkspace().getName())) {
                throw new RuntimeException("Cannot use label "+versionLabel + " in workspace "+getWorkspace().getName());
            }
            this.versionLabel = versionLabel;
        } else {
            throw new RuntimeException("Should not change versionLabel on a session in same thread");
        }
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper getFrozenVersionAsRegular(Node objectNode, JCRStoreProvider provider) throws RepositoryException {
        try {
            VersionHistory vh =
                    objectNode.getSession().getWorkspace().getVersionManager().getVersionHistory(objectNode.getPath());

            Version v = null;
            if (versionLabel != null) {
                v = JCRVersionService.findVersionByLabel(vh, versionLabel);
            }
            if (v == null && versionDate != null) {
                v = JCRVersionService.findClosestVersion(vh, versionDate);
            }

            if (v == null) {
                throw new PathNotFoundException();
            }

            Node frozen = v.getNode(Constants.JCR_FROZENNODE);

            return provider.getNodeWrapper(frozen, this);
        } catch (UnsupportedRepositoryOperationException e) {
            if (getVersionDate() == null && getVersionLabel() == null) {
                logger.error("Error while retrieving frozen version", e);
            }
        }
        return null;
    }

    public static AtomicLong getActiveSessions() {
        return activeSessions;
    }
    
    protected void flushCaches() {
        sessionCacheByIdentifier.clear();
        sessionCacheByPath.clear();
    }

    protected JCRNodeWrapper getCachedNode(String uuid) {
        return sessionCacheByIdentifier.get(uuid);
    }
}
