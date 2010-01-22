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

import org.apache.jackrabbit.commons.xml.SystemViewExporter;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.jahia.jaas.JahiaLoginModule;
import org.jahia.services.importexport.DocumentViewExporter;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.usermanager.JahiaUser;
import org.xml.sax.*;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;
import javax.jcr.version.VersionException;
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

/**
 * Jahia specific wrapper around <code>javax.jcr.Session</code> to be able to inject
 * Jahia specific actions and to manage sessions to multiple repository providers in
 * the backend. 
 * 
 * Jahia services should use this wrapper rather than the original session interface to 
 * ensure that we manipulate wrapped nodes and not the ones from the underlying 
 * implementation.
 *
 * @author toto
 */
public class JCRSessionWrapper implements Session {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JCRSessionWrapper.class);

    private JCRSessionFactory sessionFactory;
    private JahiaUser user;
    private Credentials credentials;
    private JCRWorkspaceWrapper workspace;
    private boolean isLive = true;
    private Locale locale;
    private boolean interceptorsEnabled = true;
    private List<String> tokens = new ArrayList<String>();

    private Map<JCRStoreProvider, Session> sessions = new HashMap<JCRStoreProvider, Session>();

    private Map<String, String> nsToPrefix = new HashMap<String, String>();
    private Map<String, String> prefixToNs = new HashMap<String, String>();

    private boolean eventsDisabled = false;
    private Locale fallbackLocale;

    public JCRSessionWrapper(JahiaUser user, Credentials credentials, boolean isSystem, String workspace, Locale locale,
                             boolean eventsDisabled, JCRSessionFactory sessionFactory, Locale fallbackLocale) {
        this.user = user;
        this.credentials = credentials;
        if (workspace == null) {
            this.workspace = new JCRWorkspaceWrapper("default", this, sessionFactory);
        } else {
            this.workspace = new JCRWorkspaceWrapper(workspace, this, sessionFactory);
        }
        this.locale = locale;
        this.fallbackLocale = fallbackLocale;
        // disable interceptors 
        if (locale == null) {
            interceptorsEnabled = false;
        }
        this.eventsDisabled = eventsDisabled;
        this.sessionFactory = sessionFactory;
    }


    public Node getRootNode() throws RepositoryException {
        JCRStoreProvider provider = sessionFactory.getProvider("/");
        return provider.getNodeWrapper(getProviderSession(provider).getRootNode(), this);
    }

    public Repository getRepository() {
        return sessionFactory;
    }

    public String getUserID() {
        return ((SimpleCredentials) credentials).getUserID();
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

    public boolean isInterceptorsEnabled() {
        return interceptorsEnabled;
    }

//    public void setInterceptorsEnabled(boolean interceptorsEnabled) {
//        this.interceptorsEnabled = interceptorsEnabled;
//    }

    public Session impersonate(Credentials credentials) throws LoginException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public JCRNodeWrapper getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException {
        for (JCRStoreProvider provider : sessionFactory.getProviderList()) {
            if (!provider.isInitialized()) {
                logger.debug("Provider " + provider.getKey() + " / " + provider.getClass().getName() + " is not yet initialized, skipping...");
                continue;
            }
            try {
                Session session = getProviderSession(provider);
                Node n = session.getNodeByIdentifier(uuid);
                return provider.getNodeWrapper(n, this);
            } catch (ItemNotFoundException ee) {
                // All good
            } catch (UnsupportedRepositoryOperationException uso) {
                logger.debug("getNodeByUUID unsupported by : " + provider.getKey() + " / " + provider.getClass().getName());
            } catch (RepositoryException ex) {
                logger.warn("repository exception : " + provider.getKey() + " / " + provider.getClass().getName() + " : " + ex.getMessage());
            }
        }
        throw new ItemNotFoundException(uuid);
    }

    public JCRNodeWrapper getNodeByUUID(String providerKey, String uuid) throws ItemNotFoundException, RepositoryException {
        JCRStoreProvider provider = sessionFactory.getProviders().get(providerKey);
        if (provider == null) {
            throw new ItemNotFoundException(uuid);
        }
        Session session = getProviderSession(provider);
        Node n = session.getNodeByIdentifier(uuid);
        return provider.getNodeWrapper(n, this);
    }

    public JCRItemWrapper getItem(String path) throws PathNotFoundException, RepositoryException {
        Map<String, JCRStoreProvider> dynamicMountPoints = sessionFactory.getDynamicMountPoints();
        for (Map.Entry<String, JCRStoreProvider> mp : dynamicMountPoints.entrySet()) {
            if (path.startsWith(mp + "/")) {
                String localPath = path.substring(mp.getKey().length());
                JCRStoreProvider provider = mp.getValue();
                Item item = getProviderSession(provider).getItem(
                        provider.getRelativeRoot() + provider.encodeInternalName(localPath));
                if (item.isNode()) {
                    return provider.getNodeWrapper((Node) item, this);
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
                    localPath = path.substring(key.length());
                }
                JCRStoreProvider provider = mp.getValue();
                if (localPath.equals("")) {
                    localPath = "/";
                }
//                Item item = getProviderSession(provider).getItem(localPath);
                Item item = getProviderSession(provider).getItem(
                        provider.getRelativeRoot() + provider.encodeInternalName(localPath));
                if (item.isNode()) {
                    return provider.getNodeWrapper((Node) item, this);
                } else {
                    return provider.getPropertyWrapper((Property) item, this);
                }
            }
        }
        throw new PathNotFoundException(path);
    }

    public JCRNodeWrapper getNode(String path) throws PathNotFoundException, RepositoryException {
        return (JCRNodeWrapper) getItem(path);
    }

    public boolean itemExists(String path) throws RepositoryException {
        try {
            getItem(path);
            return true;
        } catch (RepositoryException e) {
            return false;
        }
    }

    public void move(String source, String dest) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        getWorkspace().move(source, dest, true);
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        JCRObservationManager.doWorkspaceWriteCall(this, new JCRCallback() {
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
     * @throws UnsupportedRepositoryOperationException as long as Jahia doesn't support it
     */
    public void checkPermission(String absPath, String actions) throws AccessControlException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public ContentHandler getImportContentHandler(String s, int i) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void importXML(String path, InputStream inputStream, int uuidBehavior) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, VersionException, InvalidSerializedDataException, LockException, RepositoryException {
        importXML(path, inputStream, uuidBehavior, false);
    }

    public void importXML(String path, InputStream inputStream, int uuidBehavior, boolean noRoot) throws IOException, InvalidSerializedDataException, RepositoryException {
        JCRNodeWrapper node = null;
        node = getNode(path);
        try {
            if (!node.isCheckedOut()) {
                checkout(node);
            }
        } catch (UnsupportedRepositoryOperationException ex) {
            // versioning not supported
        }

        DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(this, path, null);
        documentViewImportHandler.setNoRoot(noRoot);
        documentViewImportHandler.setUuidBehavior(uuidBehavior);
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


    public void setNamespacePrefix(String prefix, String uri) throws NamespaceException, RepositoryException {
        nsToPrefix.put(uri, prefix);
        prefixToNs.put(prefix, uri);
        for (Session s : sessions.values()) {
            s.setNamespacePrefix(prefix, uri);
        }
    }

    public String[] getNamespacePrefixes() throws RepositoryException {
        Set<String> wsPrefixes = new HashSet<String>(Arrays.asList(getWorkspace().getNamespaceRegistry().getPrefixes()));
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
     * should be used instead.
     */    
    public void addLockToken(String token) {
        tokens.add(token);
        for (Session session : sessions.values()) {
            session.addLockToken(token);
        }
    }

    public String[] getLockTokens() {
        return tokens.toArray(new String[tokens.size()]);
    }

    public void removeLockToken(String token) {
        tokens.remove(token);
        for (Session session : sessions.values()) {
            session.removeLockToken(token);
        }
    }

    /**
     * Get sessions from all providers used in this wrapper. 
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
                JahiaLoginModule.Token t = JahiaLoginModule.getToken(simpleCredentials.getUserID(), new String(simpleCredentials.getPassword()));

                s = provider.getSession(credentials, workspace.getName());

                credentials = JahiaLoginModule.getCredentials(simpleCredentials.getUserID(), t != null ? t.deniedPath : null);
            } else {
                s = provider.getSession(credentials, workspace.getName());
            }

            sessions.put(provider, s);
            for (String token : tokens) {
                s.addLockToken(token);
            }

            NamespaceRegistry namespaceRegistryWrapper = getWorkspace().getNamespaceRegistry();
            NamespaceRegistry providerNamespaceRegistry = s.getWorkspace().getNamespaceRegistry();


            for (String prefix : namespaceRegistryWrapper.getPrefixes()) {
                try {
                    providerNamespaceRegistry.getURI(prefix);
                } catch (RepositoryException e) {
                    providerNamespaceRegistry.registerNamespace(prefix, namespaceRegistryWrapper.getURI(prefix));
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
    public void exportDocumentView(
            String path, ContentHandler handler,
            boolean skipBinary, boolean noRecurse)
            throws PathNotFoundException, SAXException, RepositoryException {
        DocumentViewExporter exporter =  new DocumentViewExporter(this, handler, skipBinary, noRecurse);
        Item item = getItem(path);
        if (item.isNode()) {
            exporter.export((JCRNodeWrapper) item);
        } else {
            throw new PathNotFoundException(
                    "XML export is not defined for properties: " + path);
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
    public void exportSystemView(
            String path, ContentHandler handler,
            boolean skipBinary, boolean noRecurse)
            throws PathNotFoundException, SAXException, RepositoryException {

        //todo implement our own system view .. ?
        SystemViewExporter exporter =  new SystemViewExporter(this, handler, !noRecurse, !skipBinary);
        Item item = getItem(path);
        if (item.isNode()) {
            exporter.export((JCRNodeWrapper) item);
        } else {
            throw new PathNotFoundException(
                    "XML export is not defined for properties: " + path);
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
    public void exportDocumentView(
            String absPath, OutputStream out,
            boolean skipBinary, boolean noRecurse)
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
                throw new RepositoryException(
                        "Error serializing document view XML", e);
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
    public void exportSystemView(
            String absPath, OutputStream out,
            boolean skipBinary, boolean noRecurse)
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
                throw new RepositoryException(
                        "Error serializing system view XML", e);
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
    private ContentHandler getExportContentHandler(OutputStream stream)
            throws RepositoryException {
        try {
            SAXTransformerFactory stf = (SAXTransformerFactory)
                    SAXTransformerFactory.newInstance();
            TransformerHandler handler = stf.newTransformerHandler();

            Transformer transformer = handler.getTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            handler.setResult(new StreamResult(stream));
            return handler;
        } catch (TransformerFactoryConfigurationError e) {
            throw new RepositoryException(
                    "SAX transformer implementation not available", e);
        } catch (TransformerException e) {
            throw new RepositoryException(
                    "Error creating an XML export content handler", e);
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

    public void removeItem(String absPath) throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {
        getItem(absPath).remove();
    }

    public boolean hasPermission(String absPath, String actions) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean hasCapability(String s, Object o, Object[] objects) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Returns the access control manager for this <code>Session</code>.
     * <p>
     * Jahia throws an <code>UnsupportedRepositoryOperationException</code>.
     *
     * @return the access control manager for this <code>Session</code>
     * @throws UnsupportedRepositoryOperationException if access control
     *         is not supported.
     * @since JCR 2.0
     */    
    public AccessControlManager getAccessControlManager() throws UnsupportedRepositoryOperationException, RepositoryException {
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
     * @param node the node to perform the check out
     * @see VersionManager#checkout(String) for details
     */
    public void checkout(Node node) throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        checkout(node.getPath());
    }

    /**
     * Performs check out of the specified node.
     * @param absPath the path of the node to perform the check out
     * @see VersionManager#checkout(String) for details
     */
    public void checkout(String absPath) throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        VersionManager versionManager = getWorkspace().getVersionManager();
        if (!versionManager.isCheckedOut(absPath)) {
            versionManager.checkout(absPath);
        }
    }

    public boolean isEventsDisabled() {
        return eventsDisabled;
    }

    public Locale getFallbackLocale() {
        return fallbackLocale;
    }
}
