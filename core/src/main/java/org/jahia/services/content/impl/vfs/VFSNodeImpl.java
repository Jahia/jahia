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
package org.jahia.services.content.impl.vfs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.impl.vfs.PropertyIteratorImpl;
import org.jahia.api.Constants;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.lock.Lock;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.version.VersionException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Arrays;
import java.util.ArrayList;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 23, 2008
 * Time: 11:46:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class VFSNodeImpl extends VFSItemImpl implements Node {
    private FileObject fileObject;
    private VFSSessionImpl session;

    public VFSNodeImpl(FileObject fileObject, VFSSessionImpl session) {
        this.fileObject = fileObject;
        this.session = session;
    }


    public String getPath() throws RepositoryException {
        String s = fileObject.getName().getPath().substring(((VFSRepositoryImpl)session.getRepository()).getRootPath().length());
        if (!s.startsWith("/")) {
            s = "/"+s;
        }
        return s;
    }

    public String getName() throws RepositoryException {
        return fileObject.getName().getBaseName();
    }

    public Item getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return super.getAncestor(i);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        try {
            return new VFSNodeImpl(fileObject.getParent(), session);
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
    }

    public int getDepth() throws RepositoryException {
        return super.getDepth();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public Session getSession() throws RepositoryException {
        return session;
    }

    public boolean isNode() {
        return true;
    }

    public boolean isNew() {
        return false;
    }

    public boolean isModified() {
        return false;
    }

    public boolean isSame(Item item) throws RepositoryException {
        return false;
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        super.save();
    }

    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {
        super.refresh(b);
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        try {
            fileObject.delete();
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
    }

    public Node addNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        return null;
    }

    public Node addNode(String name, String type) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        try {
            if (type.equals(Constants.NT_FOLDER) || type.equals(Constants.JAHIANT_FOLDER)) {
                FileObject obj = fileObject.resolveFile(name);
                obj.createFolder();
                return new VFSNodeImpl(obj, session);
            } else if (type.equals(Constants.NT_FILE) || type.equals(Constants.JAHIANT_FILE)) {
                FileObject obj = fileObject.resolveFile(name);
                obj.createFile();
                return new VFSNodeImpl(obj, session);
            } else if (type.equals(Constants.NT_RESOURCE)) {
                // 
            }
        } catch (FileSystemException e) {
            throw new RepositoryException("Cannot add node",e);
        }
        return null;
    }

    public void orderBefore(String s, String s1) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
    }

    public Property setProperty(String s, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String s, Value value, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String s, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String s, Value[] values, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String s, String[] strings) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String s, String[] strings, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String s, String s1) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String s, String s1, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String s, InputStream inputStream) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String s, boolean b) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String s, double v) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String s, long l) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String s, Calendar calendar) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String s, Node node) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Node getNode(String s) throws PathNotFoundException, RepositoryException {
        try {
            if (fileObject.getType() == FileType.FILE) {
                if ("jcr:content".equals(s)) {
                    return new VFSContentNodeImpl(session, fileObject.getContent());
                } else {
                    throw new PathNotFoundException(s);
                }
            } else {
                return new VFSNodeImpl(fileObject.getChild(s), session);
            }
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
    }

    public NodeIterator getNodes() throws RepositoryException {
        try {
            if (fileObject.getType() == FileType.FILE) {
                return  new VFSContentNodeIteratorImpl(session, fileObject.getContent());
            } else {
                FileObject[] fo = fileObject.getChildren();
                return new VFSNodeIteratorImpl(session, Arrays.asList(fo).iterator(), fo.length);
            }
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
    }

    public NodeIterator getNodes(String s) throws RepositoryException {
        try {
            if (fileObject.getType() == FileType.FILE) {
                return  new VFSContentNodeIteratorImpl(session, fileObject.getContent());
            } else {
                FileObject[] fo = fileObject.getChildren();
                return new VFSNodeIteratorImpl(session, Arrays.asList(fo).iterator(), fo.length);
            }
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
    }

    public Property getProperty(String s) throws PathNotFoundException, RepositoryException {
        throw new PathNotFoundException(s);
    }

    public PropertyIterator getProperties() throws RepositoryException {
        return new PropertyIteratorImpl(new ArrayList().iterator(),0);
    }

    public PropertyIterator getProperties(String s) throws RepositoryException {
        return new PropertyIteratorImpl(new ArrayList().iterator(),0);
    }

    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        return null;
    }

    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public int getIndex() throws RepositoryException {
        return 0;
    }

    public PropertyIterator getReferences() throws RepositoryException {
        return new PropertyIteratorImpl(new ArrayList().iterator(),0);
    }

    public boolean hasNode(String s) throws RepositoryException {
        try {
            if (fileObject.getType() == FileType.FILE) {
                if ("jcr:content".equals(s)) {
                    return true;
                }
            } else {
                FileObject child = fileObject.getChild(s);
                return child != null && child.exists();
            }
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
        return false;
    }

    public boolean hasProperty(String s) throws RepositoryException {
        return false;
    }

    public boolean hasNodes() throws RepositoryException {
        try {
            if (fileObject.getType() == FileType.FILE) {
                return true;
            } else {
                return fileObject.getChildren().length > 0;
            }
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
    }

    public boolean hasProperties() throws RepositoryException {
        return false;
    }

    public NodeType getPrimaryNodeType() throws RepositoryException {
        try {
            FileType fileType = fileObject.getType();
            if (fileType == FileType.FILE) {
                return NodeTypeRegistry.getInstance().getNodeType("nt:file");
            } else if (fileType == FileType.FOLDER) {
                return NodeTypeRegistry.getInstance().getNodeType("nt:folder");
            }
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
        return NodeTypeRegistry.getInstance().getNodeType("nt:file");
    }

    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return new NodeType[0];
    }

    public boolean isNodeType(String s) throws RepositoryException {
        return getPrimaryNodeType().isNodeType(s);
    }

    public void addMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {

    }

    public void removeMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {

    }

    public boolean canAddMixin(String s) throws NoSuchNodeTypeException, RepositoryException {
        return false;
    }

    public NodeDefinition getDefinition() throws RepositoryException {
        return null;
    }

    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        return null;
    }

    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
    }

    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
    }

    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
    }

    public void update(String s) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
    }

    public NodeIterator merge(String s, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        return null;
    }

    public String getCorrespondingNodePath(String s) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        return null;
    }

    public boolean isCheckedOut() throws RepositoryException {
        return false;
    }

    public void restore(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
    }

    public void restore(Version version, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
    }

    public void restore(Version version, String s, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
    }

    public void restoreByLabel(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
    }

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;
    }

    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;
    }

    public Lock lock(boolean b, boolean b1) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        return null;
    }

    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
        return null;
    }

    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
    }

    public boolean holdsLock() throws RepositoryException {
        return false;
    }

    public boolean isLocked() throws RepositoryException {
        return false;
    }

    public Property setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getProperties(String[] strings) throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getIdentifier() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getReferences(String name) throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getWeakReferences() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getWeakReferences(String name) throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public NodeIterator getSharedSet() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
