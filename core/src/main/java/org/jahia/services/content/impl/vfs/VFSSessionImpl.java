/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.impl.vfs;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileObject;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import java.security.AccessControlException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 23, 2008
 * Time: 11:56:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class VFSSessionImpl implements Session {
    private VFSRepositoryImpl repository;
    private VFSWorkspaceImpl workspace;
    private Credentials credentials;

    public VFSSessionImpl(VFSRepositoryImpl repository, Credentials credentials) {
        this.repository = repository;
        this.workspace = new VFSWorkspaceImpl(this);
        this.credentials = credentials;
    }

    public Repository getRepository() {
        return repository;
    }

    public String getUserID() {
        return ((SimpleCredentials)credentials).getUserID();
    }

    public Object getAttribute(String s) {
        return null;
    }

    public String[] getAttributeNames() {
        return new String[0];
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public Session impersonate(Credentials credentials) throws LoginException, RepositoryException {
        return this;
    }

    public Node getRootNode() throws RepositoryException {
        try {
            return new VFSNodeImpl(repository.getFile("/"), this);
        } catch (FileSystemException e) {
            throw new RepositoryException(e);

        }
    }

    public Node getNodeByUUID(String s) throws ItemNotFoundException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Item getItem(String s) throws PathNotFoundException, RepositoryException {
        try {
            FileObject object = repository.getFile(s);
            if (!object.exists()) {
                throw new PathNotFoundException(s);
            }
            return new VFSNodeImpl(object, this);
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
    }

    public boolean itemExists(String s) throws RepositoryException {
        return false;
    }

    public void move(String source, String dest) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        try {
            FileObject object1 = repository.getFile(source);
            if (!object1.exists()) {
                throw new PathNotFoundException(source);
            }
            FileObject object2 = repository.getFile(dest);
            if (object2.exists()) {
                throw new ItemExistsException(dest);
            }
            object1.moveTo(object2);
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }

    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
      
    }

    public void refresh(boolean b) throws RepositoryException {
      
    }

    public boolean hasPendingChanges() throws RepositoryException {
        return false;
    }

    public ValueFactory getValueFactory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;
    }

    public void checkPermission(String s, String s1) throws AccessControlException, RepositoryException {
      
    }

    public ContentHandler getImportContentHandler(String s, int i) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, RepositoryException {
        return null;
    }

    public void importXML(String s, InputStream inputStream, int i) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, VersionException, InvalidSerializedDataException, LockException, RepositoryException {
      
    }

    public void exportSystemView(String s, ContentHandler contentHandler, boolean b, boolean b1) throws PathNotFoundException, SAXException, RepositoryException {
      
    }

    public void exportSystemView(String s, OutputStream outputStream, boolean b, boolean b1) throws IOException, PathNotFoundException, RepositoryException {
      
    }

    public void exportDocumentView(String s, ContentHandler contentHandler, boolean b, boolean b1) throws PathNotFoundException, SAXException, RepositoryException {
      
    }

    public void exportDocumentView(String s, OutputStream outputStream, boolean b, boolean b1) throws IOException, PathNotFoundException, RepositoryException {
      
    }

    public void setNamespacePrefix(String s, String s1) throws NamespaceException, RepositoryException {
      
    }

    public String[] getNamespacePrefixes() throws RepositoryException {
        return new String[0];
    }

    public String getNamespaceURI(String s) throws NamespaceException, RepositoryException {
        return null;
    }

    public String getNamespacePrefix(String s) throws NamespaceException, RepositoryException {
        return null;
    }

    public void logout() {
      
    }

    public boolean isLive() {
        return false;
    }

    public void addLockToken(String s) {
      
    }

    public String[] getLockTokens() {
        return new String[0];
    }

    public void removeLockToken(String s) {
      
    }
}
