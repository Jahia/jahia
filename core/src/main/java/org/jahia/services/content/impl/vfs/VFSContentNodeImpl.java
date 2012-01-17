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


import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.io.IOUtils;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.Name;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.impl.vfs.PropertyIteratorImpl;
import org.jahia.api.Constants;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.lock.Lock;
import javax.jcr.nodetype.*;
import javax.jcr.version.VersionException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.activation.MimetypesFileTypeMap;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * 
 * User: toto
 * Date: Jul 24, 2008
 * Time: 3:54:17 PM
 * 
 */
public class VFSContentNodeImpl extends VFSItemImpl implements Node {
    private VFSSessionImpl session;
    private FileContent content;

    public VFSContentNodeImpl(VFSSessionImpl session, FileContent content) {
        this.session = session;
        this.content = content;
    }

    public Node addNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        return null;
    }

    public Node addNode(String s, String s1) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public void orderBefore(String s, String s1) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, Value value, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, Value[] values, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, String[] strings) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, String[] strings, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, String s1) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (s.equals(Constants.JCR_MIMETYPE)) {
//            try {
//                content.setAttribute();
//            } catch (FileSystemException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
            return null;
        }
        throw new ConstraintViolationException("Unknown type");
    }

    public Property setProperty(String s, String s1, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, InputStream inputStream) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (s.equals(Constants.JCR_DATA)) {
            try {
                OutputStream outputStream = content.getOutputStream();
                IOUtils.copy(inputStream, outputStream);
                outputStream.close();
                VFSValueFactoryImpl valueFactory = (VFSValueFactoryImpl) session.getValueFactory();
                Value value = valueFactory.createValue(new VFSBinaryImpl(content));
                return new DataPropertyImpl(new Name(s, "", ""), this, session, value);
            } catch (IOException e) {
                throw new RepositoryException("Cannot write to stream", e);
            }
        }
        throw new ConstraintViolationException("Unknown type");
    }

    public Property setProperty(String s, boolean b) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, double v) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, long l) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, Calendar calendar) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (s.equals(Constants.JCR_LASTMODIFIED)) {
            try {
                content.setLastModifiedTime(calendar.getTime().getTime());
            } catch (FileSystemException e) {
                
            }
            return null;
        }
        throw new ConstraintViolationException("Unknown type");
    }

    public Property setProperty(String s, Node node) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Node getNode(String s) throws PathNotFoundException, RepositoryException {
        return null;
    }

    public NodeIterator getNodes() throws RepositoryException {
        return VFSNodeIteratorImpl.EMPTY;
    }

    public NodeIterator getNodes(String s) throws RepositoryException {
        return VFSNodeIteratorImpl.EMPTY;
    }

    public Property getProperty(String s) throws PathNotFoundException, RepositoryException {
        VFSValueFactoryImpl valueFactory = (VFSValueFactoryImpl) session.getValueFactory();
        if (s.equals(Constants.JCR_DATA)) {
            Value value = null;
            value = valueFactory.createValue(new VFSBinaryImpl(content));
            return new VFSPropertyImpl(new Name(s, "", ""), this, session, value) {
                public long getLength() throws ValueFormatException, RepositoryException {
                    try {
                        return content.getSize();
                    } catch (FileSystemException e) {
                        return -1L;
                    }
                }

                public InputStream getStream() throws ValueFormatException, RepositoryException {
                    try {
                        return content.getInputStream();
                    } catch (FileSystemException e) {
                        return null;
                    }
                }

                public String getName() throws RepositoryException {
                    return Constants.JCR_DATA;
                }

                public PropertyDefinition getDefinition() throws RepositoryException {
                    return NodeTypeRegistry.getInstance().getNodeType(Constants.NT_RESOURCE).getPropertyDefinition(Constants.JCR_DATA);
                }
            };
        } else if (s.equals(Constants.JCR_MIMETYPE)) {
            String s1 = null;
            try {
                s1 = content.getContentInfo().getContentType();
            } catch (FileSystemException e) {
                throw new RepositoryException("Error while retrieving file's content type", e);
            }
            if (s1 == null) {
                s1 = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType("."+content.getFile().getName().getExtension());
            }
            Value value = valueFactory.createValue(s1);
            return new VFSPropertyImpl(new Name(s, "", ""), this, session, value) {
                public String getString() throws ValueFormatException, RepositoryException {
                    try {
                        String s1 = content.getContentInfo().getContentType();
                        if (s1 == null) {
                            return MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType("."+content.getFile().getName().getExtension());
                        }
                        return s1;
                    } catch (FileSystemException e) {
                        return null;
                    }
                }

                public String getName() throws RepositoryException {
                    return Constants.JCR_MIMETYPE;
                }

                public PropertyDefinition getDefinition() throws RepositoryException {
                    return NodeTypeRegistry.getInstance().getNodeType("mix:mimeType").getPropertyDefinition(Constants.JCR_MIMETYPE);
                }

            };
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getProperties() throws RepositoryException {
        List l = new ArrayList(2);
        l.add(getProperty(Constants.JCR_DATA));
        l.add(getProperty(Constants.JCR_MIMETYPE));

        return new PropertyIteratorImpl(l.iterator(), l.size());
    }

    public PropertyIterator getProperties(String s) throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return false;
    }

    public boolean hasProperty(String s) throws RepositoryException {
        return s.equals("jcr:data") || s.equals("jcr:mimeType");
    }

    public boolean hasNodes() throws RepositoryException {
        return false;
    }

    public boolean hasProperties() throws RepositoryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NodeType getPrimaryNodeType() throws RepositoryException {
        return NodeTypeRegistry.getInstance().getNodeType("jnt:resource");
    }

    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return new NodeType[0];  //To change body of implemented methods use File | Settings | File Templates.
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
        ExtendedNodeDefinition nodeDefinition = parentNodeType.getNodeDefinition("jcr:content");
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

    public String getPath() throws RepositoryException {
        String s = content.getFile().getName().getPath().substring(((VFSRepositoryImpl)session.getRepository()).getRootPath().length());
        if (!s.startsWith("/")) {
            s = "/"+s;
        }
        return s+"/"+Constants.JCR_CONTENT;
    }

    public String getName() throws RepositoryException {
        return Constants.JCR_CONTENT;
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return new VFSNodeImpl(content.getFile(), session);
    }


    public Property setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return setProperty(name, value.getStream());
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
        try {
            // return UUID.nameUUIDFromBytes(fileObject.getURL().toString().getBytes()).toString();
            return content.getFile().getURL().toString() + "/" + getName();
        } catch (FileSystemException fse) {
            throw new RepositoryException("Error retrieving URL for VFS file", fse);
        }
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

    class DataPropertyImpl extends VFSPropertyImpl {

        public DataPropertyImpl(Name name, Node node, VFSSessionImpl session, Value value) {
            super(name, node, session, value);
        }

        public InputStream getStream() throws ValueFormatException, RepositoryException {
            try {
                return content.getInputStream();
            } catch (FileSystemException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            throw new RepositoryException();
        }
    }
}
