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
package org.jahia.services.content.impl.jahia;

import org.xml.sax.ContentHandler;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.version.EntryLoadRequest;

import javax.jcr.*;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import javax.jcr.version.Version;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;
import java.io.InputStream;
import java.io.IOException;

/**
 * @todo Workspace are not supported for the moment.
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 17 d�c. 2007
 * Time: 10:07:59
 * To change this template use File | Settings | File Templates.
 */
public class WorkspaceImpl implements Workspace {

    private SessionImpl session;
    private ObservationManagerImpl obs;
    private String name;
    private NamespaceRegistry reg = new NamespaceRegistryImpl();

    private int workflowState;

    public WorkspaceImpl(SessionImpl session, String name) throws NoSuchWorkspaceException {
        this.session = session;
        if (name == null) {
            name = "default";
        }
        this.name = name;
        if ("default".equals(name)) {
            workflowState = 2;
        } else if ("live".equals(name)) {
            workflowState = 1;
        } else {
            throw new NoSuchWorkspaceException(name);
        }
    }

    public SessionImpl getSession() {
        return session;
    }

    public String getName() {
        return name;
    }

    public void copy(String s, String s1) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void copy(String s, String s1, String s2) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clone(String s, String s1, String s2, boolean b) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void move(String s, String s1) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void restore(Version[] versions, boolean b) throws ItemExistsException, UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public QueryManager getQueryManager() throws RepositoryException {
        return null;
    }

    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
        return reg;
    }

    public NodeTypeManager getNodeTypeManager() throws RepositoryException {
        return NodeTypeRegistry.getInstance();
    }

    public ObservationManager getObservationManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        if (obs == null) {
            obs = new ObservationManagerImpl(this);
            ObservationManagerImpl.addObservationManager(obs);
        }
        return obs;
    }

    public String[] getAccessibleWorkspaceNames() throws RepositoryException {
        return new String[] { "staging" , "live" };
    }

    public ContentHandler getImportContentHandler(String s, int i) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void importXML(String s, InputStream inputStream, int i) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, InvalidSerializedDataException, LockException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void close() {
        if (obs == null) {
            ObservationManagerImpl.removeObservationManager(obs);
            obs = null;
        }
    }

    public int getWorkflowState() {
        return workflowState;
    }
}
