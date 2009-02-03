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

package org.jahia.services.content.impl.vfs;

import org.xml.sax.ContentHandler;

import javax.jcr.*;
import javax.jcr.observation.ObservationManager;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.query.QueryManager;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import javax.jcr.version.Version;
import javax.jcr.nodetype.*;
import java.io.InputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 23, 2008
 * Time: 11:45:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class VFSWorkspaceImpl implements Workspace {
    private VFSSessionImpl session;

    public VFSWorkspaceImpl(VFSSessionImpl session) {
        this.session = session;
    }

    public Session getSession() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NodeTypeManager getNodeTypeManager() throws RepositoryException {
        return new NodeTypeManager() {
            public NodeType getNodeType(String s) throws NoSuchNodeTypeException, RepositoryException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public NodeTypeIterator getAllNodeTypes() throws RepositoryException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public NodeTypeIterator getPrimaryNodeTypes() throws RepositoryException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public NodeTypeIterator getMixinNodeTypes() throws RepositoryException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    public ObservationManager getObservationManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new ObservationManager() {
            public void addEventListener(EventListener eventListener, int i, String s, boolean b, String[] strings, String[] strings1, boolean b1) throws RepositoryException {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void removeEventListener(EventListener eventListener) throws RepositoryException {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public EventListenerIterator getRegisteredEventListeners() throws RepositoryException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    public String[] getAccessibleWorkspaceNames() throws RepositoryException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ContentHandler getImportContentHandler(String s, int i) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, AccessDeniedException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void importXML(String s, InputStream inputStream, int i) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, InvalidSerializedDataException, LockException, AccessDeniedException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
