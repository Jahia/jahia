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

package org.jahia.services.content.impl.vfs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.Selectors;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.Name;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * User: toto
 * Date: Apr 23, 2008
 * Time: 11:46:22 AM
 *
 */
public class VFSNodeImpl extends VFSItemImpl implements Node {

    private static final Logger logger = LoggerFactory.getLogger(VFSNodeImpl.class);

    private FileObject fileObject;
    private VFSSessionImpl session;
    private Map<String, VFSPropertyImpl> properties = null;

    public VFSNodeImpl(FileObject fileObject, VFSSessionImpl session) {
        this.fileObject = fileObject;
        this.session = session;
        this.properties = new HashMap<String, VFSPropertyImpl>();
        try {
            if (fileObject.exists() && (fileObject.getContent() != null)) {
                long lastModifiedTime = fileObject.getContent().getLastModifiedTime();
                if (lastModifiedTime > 0) {
                    ValueFactory valueFactory = session.getValueFactory();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(lastModifiedTime);
                    properties.put(Constants.JCR_CREATED, new VFSPropertyImpl(new Name("created", org.apache.jackrabbit.spi.Name.NS_JCR_PREFIX, org.apache.jackrabbit.spi.Name.NS_JCR_URI), this, session, valueFactory.createValue(calendar)));
                    properties.put(Constants.JCR_LASTMODIFIED, new VFSPropertyImpl(new Name("lastModified", org.apache.jackrabbit.spi.Name.NS_JCR_PREFIX, org.apache.jackrabbit.spi.Name.NS_JCR_URI), this, session, valueFactory.createValue(calendar)));
                }
            }
        } catch (FileSystemException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnsupportedRepositoryOperationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    public String getPath() throws RepositoryException {
        String s = fileObject.getName().getPath().substring(((VFSRepositoryImpl) session.getRepository()).getRootPath().length());
        if (!s.startsWith("/")) {
            s = "/" + s;
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
            FileObject parentFileObject = fileObject.getParent();
            if (parentFileObject == null) {
                throw new ItemNotFoundException("Trying to retrieve parent of root node");
            }
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
            fileObject.delete(Selectors.SELECT_ALL);
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
    }

    public Node addNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        return null;
    }

    public Node addNode(String name, String type) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        try {
            if (type.equals(Constants.NT_FOLDER) || type.equals(Constants.JAHIANT_FOLDER) || type.equals(Constants.JAHIANT_CONTENTLIST)) {
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
            throw new RepositoryException("Cannot add node", e);
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
            } else if (fileObject.isReadable() && fileObject.getType() == FileType.FOLDER) {
                FileObject child = fileObject.getChild(s);
                if (child == null) {
                    throw new PathNotFoundException(s);
                }
                return new VFSNodeImpl(child, session);
            } else if (fileObject.isReadable()) {
                logger.warn("Found non file or folder entry, maybe an alias. VFS file type=" + fileObject.getType());
                 throw new PathNotFoundException(s);
            } else {
                logger.warn("Item "+ fileObject.getName() +" is not readable. VFS file type=" + fileObject.getType());
                 throw new PathNotFoundException(s);
            }
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
    }

    public NodeIterator getNodes() throws RepositoryException {
        try {
            if (fileObject.getType() == FileType.FILE) {
                return new VFSContentNodeIteratorImpl(session, fileObject.getContent());
            } else if (fileObject.getType() == FileType.FOLDER) {
                return new VFSNodeIteratorImpl(session, fileObject.getChildren());
            } else {
                logger.warn("Found non file or folder entry, maybe an alias. VFS file type=" + fileObject.getType());
                return VFSNodeIteratorImpl.EMPTY;
            }
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
    }

    public NodeIterator getNodes(String s) throws RepositoryException {
        try {
            FileObject child = fileObject.isReadable() && fileObject.getType() ==  FileType.FOLDER ? fileObject.getChild(s) : null;
            return child != null ? new VFSNodeIteratorImpl(session, child) : VFSNodeIteratorImpl.EMPTY;
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
    }

    public Property getProperty(String s) throws PathNotFoundException, RepositoryException {
        Property property = properties.get(s);
        if (property == null) {
            throw new PathNotFoundException(s);
        }
        return property;
    }

    public PropertyIterator getProperties() throws RepositoryException {
        return PropertyIteratorImpl.EMPTY;
    }

    public PropertyIterator getProperties(String s) throws RepositoryException {
        return PropertyIteratorImpl.EMPTY;
    }

    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        return null;
    }

    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        return getIdentifier();
    }

    public int getIndex() throws RepositoryException {
        return 0;
    }

    public PropertyIterator getReferences() throws RepositoryException {
        return PropertyIteratorImpl.EMPTY;
    }

    public boolean hasNode(String s) throws RepositoryException {
        try {
            if (fileObject.getType() == FileType.FILE) {
                if ("jcr:content".equals(s)) {
                    return true;
                }
            } else if (fileObject.isReadable() && fileObject.getType() == FileType.FOLDER) {
                FileObject child = fileObject.getChild(s);
                return child != null && child.exists();
            } else if (fileObject.isReadable()) {
                logger.warn("Found non file or folder entry, maybe an alias. VFS file type=" + fileObject.getType());
                return false;
            } else {
                logger.warn("Item "+ fileObject.getName() +" is not readable. VFS file type=" + fileObject.getType());
                return false;
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
            } else if (fileObject.getType() == FileType.FOLDER) {
                return fileObject.getChildren().length > 0;
            } else {
                logger.warn("Found non file or folder entry, maybe an alias. VFS file type=" + fileObject.getType());
                return false;
            }
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
    }

    public boolean hasProperties() throws RepositoryException {
        return false;
    }

    public NodeType getPrimaryNodeType() throws RepositoryException {
        return getExtendedPrimaryNodeType();
    }

    public ExtendedNodeType getExtendedPrimaryNodeType() throws RepositoryException {
        try {
            FileType fileType = fileObject.getType();
            if (fileType == FileType.FILE) {
                return NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIANT_FILE);
            } else if (fileType == FileType.FOLDER) {
                return NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIANT_FOLDER);
            }
        } catch (FileSystemException e) {
            throw new RepositoryException(e);
        }
        return NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIANT_FILE);
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
        VFSNodeImpl parentNode = (VFSNodeImpl) getParent();
        ExtendedNodeType parentNodeType = parentNode.getExtendedPrimaryNodeType();
        ExtendedNodeDefinition nodeDefinition = parentNodeType.getNodeDefinition(getPrimaryNodeType().getName());
        if (nodeDefinition != null) {
            return nodeDefinition;
        }
        for (ExtendedNodeDefinition extendedNodeDefinition : parentNodeType.getUnstructuredChildNodeDefinitions().values()) {
            return extendedNodeDefinition;
        }
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
        throw new UnsupportedRepositoryOperationException("Locking is not supported on VFS repository");
    }

    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedRepositoryOperationException("Locking is not supported on VFS repository");
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
        return VFSNodeIteratorImpl.EMPTY;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getProperties(String[] strings) throws RepositoryException {
        return PropertyIteratorImpl.EMPTY;
    }

    public String getIdentifier() throws RepositoryException {
        try {
            // return UUID.nameUUIDFromBytes(fileObject.getURL().toString().getBytes()).toString();
            return fileObject.getURL().toString();
        } catch (FileSystemException fse) {
            throw new RepositoryException("Error retrieving URL for VFS file", fse);
        }
    }

    public PropertyIterator getReferences(String name) throws RepositoryException {
        return PropertyIteratorImpl.EMPTY;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getWeakReferences() throws RepositoryException {
        return PropertyIteratorImpl.EMPTY;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getWeakReferences(String name) throws RepositoryException {
        return PropertyIteratorImpl.EMPTY;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public NodeIterator getSharedSet() throws RepositoryException {
        return VFSNodeIteratorImpl.EMPTY;  //To change body of implemented methods use File | Settings | File Templates.
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
