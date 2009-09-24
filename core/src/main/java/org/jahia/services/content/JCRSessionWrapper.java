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

import org.jahia.services.usermanager.JahiaUser;
import org.jahia.jaas.JahiaLoginModule;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.jackrabbit.commons.xml.Exporter;
import org.apache.jackrabbit.commons.xml.DocumentViewExporter;
import org.apache.jackrabbit.commons.xml.SystemViewExporter;

import javax.jcr.*;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;
import java.util.*;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 6, 2009
 * Time: 12:11:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRSessionWrapper implements Session {
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JCRSessionWrapper.class);

    private JCRSessionFactory sessionFactory;
    private JahiaUser user;
    private boolean isSystem = true;
    private Credentials credentials;
    private JCRWorkspaceWrapper workspace;
    private boolean isLive = true;
    private Locale locale;
    private List<String> tokens = new ArrayList<String>();

    private Map<JCRStoreProvider, Session> sessions = new HashMap<JCRStoreProvider, Session>();

    private Map<String,String> nsToPrefix = new HashMap<String,String>();
    private Map<String,String> prefixToNs = new HashMap<String,String>();

    public JCRSessionWrapper(JahiaUser user, Credentials credentials, boolean isSystem, String workspace, Locale locale, JCRSessionFactory sessionFactory) {
        this.user = user;
        this.isSystem = isSystem;
        this.credentials = credentials;
        this.workspace = new JCRWorkspaceWrapper(workspace, this, sessionFactory);
        this.locale = locale;
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
        return ((SimpleCredentials)credentials).getUserID();
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
                Node n = session.getNodeByUUID(uuid);
                return provider.getNodeWrapper(n, this);
            } catch (ItemNotFoundException ee) {
                // All good
            } catch (UnsupportedRepositoryOperationException uso) {
                logger.debug("getNodeByUUID unsupported by : "+provider.getKey() + " / " + provider.getClass().getName());
            } catch (RepositoryException ex) {
                logger.warn("repository exception : "+provider.getKey() + " / " + provider.getClass().getName() + " : "+ex.getMessage());
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
        Node n = session.getNodeByUUID(uuid);
        return provider.getNodeWrapper(n, this);
    }

    public Item getItem(String path) throws PathNotFoundException, RepositoryException {
        Map<String, JCRStoreProvider> dynamicMountPoints = sessionFactory.getDynamicMountPoints();
        for (String mp : dynamicMountPoints.keySet()) {
            if (path.startsWith(mp + "/")) {
                String localPath = path.substring(mp.length());
                JCRStoreProvider provider = dynamicMountPoints.get(mp);
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
        Map<String, JCRStoreProvider> mountPoints = sessionFactory.getMountPoints();
        for (String mp : mountPoints.keySet()) {
            if (mp.equals("/") || path.equals(mp) || path.startsWith(mp + "/")) {
                String localPath = path;
                if (!mp.equals("/")) {
                    localPath = path.substring(mp.length());
                }
                JCRStoreProvider provider = mountPoints.get(mp);
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
        ((JCRNodeWrapper)getItem(source)).moveFile(dest);
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        for (Session session : sessions.values()) {
            session.save();
        }
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

    public ValueFactory getValueFactory() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void checkPermission(String s, String s1) throws AccessControlException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public ContentHandler getImportContentHandler(String s, int i) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void importXML(String path, InputStream inputStream, int uuidBehavior) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, VersionException, InvalidSerializedDataException, LockException, RepositoryException {
        JCRNodeWrapper node = getNode(path);
        JCRStoreProvider jcrStoreProvider = node.getProvider();
        String mp = jcrStoreProvider.getMountPoint();
        if (mp.equals("/")) {
            mp="";
        }
        getProviderSession(jcrStoreProvider).importXML(path.substring(mp.length()), inputStream, uuidBehavior);
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
     * @param path of the node to be exported
     * @param handler handler for the SAX events of the export
     * @param skipBinary whether binary values should be skipped
     * @param noRecurse whether to export just the identified node
     * @throws PathNotFoundException if a node at the given path does not exist
     * @throws SAXException if the SAX event handler failed
     * @throws RepositoryException if another error occurs
     */
    public void exportDocumentView(
            String path, ContentHandler handler,
            boolean skipBinary, boolean noRecurse)
            throws PathNotFoundException, SAXException, RepositoryException {
        export(path, new DocumentViewExporter(
                this, handler, !noRecurse, !skipBinary));
    }

    /**
     * Generates a system view export using a {@link org.apache.jackrabbit.commons.xml.SystemViewExporter}
     * instance.
     *
     * @param path of the node to be exported
     * @param handler handler for the SAX events of the export
     * @param skipBinary whether binary values should be skipped
     * @param noRecurse whether to export just the identified node
     * @throws PathNotFoundException if a node at the given path does not exist
     * @throws SAXException if the SAX event handler failed
     * @throws RepositoryException if another error occurs
     */
    public void exportSystemView(
            String path, ContentHandler handler,
            boolean skipBinary, boolean noRecurse)
            throws PathNotFoundException, SAXException, RepositoryException {
        export(path, new SystemViewExporter(
                this, handler, !noRecurse, !skipBinary));
    }

    /**
     * Calls {@link Session#exportDocumentView(String, ContentHandler, boolean, boolean)}
     * with the given arguments and a {@link ContentHandler} that serializes
     * SAX events to the given output stream.
     *
     * @param absPath passed through
     * @param out output stream to which the SAX events are serialized
     * @param skipBinary passed through
     * @param noRecurse passed through
     * @throws IOException if the SAX serialization failed
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
     * @param absPath passed through
     * @param out output stream to which the SAX events are serialized
     * @param skipBinary passed through
     * @param noRecurse passed through
     * @throws IOException if the SAX serialization failed
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
     * Exports content at the given path using the given exporter.
     *
     * @param path of the node to be exported
     * @param exporter document or system view exporter
     * @throws SAXException if the SAX event handler failed
     * @throws RepositoryException if another error occurs
     */
    private synchronized void export(String path, Exporter exporter)
            throws PathNotFoundException, SAXException, RepositoryException {
        Item item = getItem(path);
        if (item.isNode()) {
            exporter.export((Node) item);
        } else {
            throw new PathNotFoundException(
                    "XML export is not defined for properties: " + path);
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

    public Node getNodeByIdentifier(String id) throws ItemNotFoundException, RepositoryException {
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

    public AccessControlManager getAccessControlManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public RetentionManager getRetentionManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }
}
