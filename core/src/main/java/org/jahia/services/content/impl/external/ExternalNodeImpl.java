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

package org.jahia.services.content.impl.external;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ChildrenCollectorFilter;
import org.apache.jackrabbit.value.BinaryImpl;
import org.jahia.services.content.nodetypes.*;

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
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * User: toto
 * Date: Apr 23, 2008
 * Time: 11:46:22 AM
 */
public class ExternalNodeImpl extends ExternalItemImpl implements Node {

    private ExternalData data;
    private Map<String, ExternalPropertyImpl> properties = null;
    public ExternalNodeImpl(ExternalData data, ExternalSessionImpl session) throws RepositoryException {
        super(session);
        this.data = data;
        this.properties = new HashMap<String, ExternalPropertyImpl>();

        for (Map.Entry<String, String[]> entry : data.getProperties().entrySet()) {
            ExtendedPropertyDefinition definition = getPropertyDefinition(entry.getKey());
            if (definition != null) {
                if (definition.isMultiple()) {
                    Value[] values = new Value[entry.getValue().length];
                    for (int i = 0; i < entry.getValue().length; i++) {
                        values[i] = session.getValueFactory().createValue(entry.getValue()[i], definition.getRequiredType());
                    }
                    properties.put(entry.getKey(),new ExternalPropertyImpl(new Name(entry.getKey(), NodeTypeRegistry.getInstance().getNamespaces()), this, session, values));
                } else {
                    properties.put(entry.getKey(),
                            new ExternalPropertyImpl(new Name(entry.getKey(), NodeTypeRegistry.getInstance().getNamespaces()), this, session,
                                    session.getValueFactory().createValue(entry.getValue()[0], definition.getRequiredType())));
                }
            }
        }
        if (data.getBinaryProperties() != null) {
            for (Map.Entry<String, Binary[]> entry : data.getBinaryProperties().entrySet()) {
                ExtendedPropertyDefinition definition =  getPropertyDefinition(entry.getKey());
                if (definition != null && definition.getRequiredType() == PropertyType.BINARY) {
                    if (definition.isMultiple()) {
                        Value[] values = new Value[entry.getValue().length];
                        for (int i = 0; i < entry.getValue().length; i++) {
                            values[i] = session.getValueFactory().createValue(entry.getValue()[i]);
                        }
                        properties.put(entry.getKey(),new ExternalPropertyImpl(new Name(entry.getKey(), NodeTypeRegistry.getInstance().getNamespaces()), this, session, values));
                    } else {
                        properties.put(entry.getKey(),
                                new ExternalPropertyImpl(new Name(entry.getKey(), NodeTypeRegistry.getInstance().getNamespaces()), this, session,
                                        session.getValueFactory().createValue(entry.getValue()[0])));
                    }
                }
            }

        }
    }

    private ExtendedPropertyDefinition getPropertyDefinition(String name) throws RepositoryException {
        Map<String, ExtendedPropertyDefinition> propertyDefinitionsAsMap = getExtendedPrimaryNodeType().getPropertyDefinitionsAsMap();
        if (propertyDefinitionsAsMap.containsKey(name)) {
            return propertyDefinitionsAsMap.get(name);
        }
        for (NodeType nodeType : getMixinNodeTypes()) {
            propertyDefinitionsAsMap = ((ExtendedNodeType)nodeType).getPropertyDefinitionsAsMap();
            if (propertyDefinitionsAsMap.containsKey(name)) {
                return propertyDefinitionsAsMap.get(name);
            }
        }

        return null;
    }


    public String getPath() throws RepositoryException {
        return data.getPath();
    }

    public String getName() throws RepositoryException {
        return StringUtils.substringAfterLast(data.getPath(), "/");
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        if (data.getPath().equals("/")) {
            throw new ItemNotFoundException();
        }
        String path = StringUtils.substringBeforeLast(data.getPath(), "/");
        return session.getNode(path.isEmpty() ? "/" : path);
    }

    public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public int getDepth() throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Session getSession() throws RepositoryException {
        return session;
    }

    public boolean isNode() {
        return true;
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        session.getDeletedData().put(getPath(),data);
    }

    public void removeProperty(String name) throws RepositoryException {
        data.getBinaryProperties().remove(name);
        data.getProperties().remove(name);
        session.getChangedData().put(getPath(),data);
    }

    public Node addNode(String relPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        return addNode(relPath,null);
    }

    public Node addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        ExternalData data = new ExternalData(this.data.getId() + "/" + relPath ,getPath() + "/" + relPath,primaryNodeTypeName,new HashMap<String, String[]>());
        session.getChangedData().put(data.getPath(),data);
        return  new ExternalNodeImpl(data,session);
    }

    public void orderBefore(String srcChildRelPath, String destChildRelPath) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        data.getProperties().put(name, new String[]{value.getString()});
        session.getChangedData().put(getPath(),data);
        return new ExternalPropertyImpl(new Name(name,NodeTypeRegistry.getInstance().getNamespaces()),this,(ExternalSessionImpl) getSession(), value);
    }

    public Property setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return setProperty(name,value);
    }

    public Property setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        String[]  s = null;
        if (values != null) {
            s = new String[values.length];
            for (int i = 0; i < values.length; i ++) {
                s[i] = values[i].getString();
            }
            data.getProperties().put(name,s);
            session.getChangedData().put(getPath(),data);
        }
        return new ExternalPropertyImpl(new Name(name,NodeTypeRegistry.getInstance().getNamespaces()),this,(ExternalSessionImpl) getSession(), values);
    }

    public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return setProperty(name,values);
    }

    public Property setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value[] v = null;
        if (values != null) {
            v = new Value[values.length];
            for (int i =0; i < values.length; i ++ ) {
                v[i] = getSession().getValueFactory().createValue(values[i]);
            }
        }
        return setProperty(name,v);
    }

    public Property setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return setProperty(name,values);
    }

    public Property setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return setProperty(name,getSession().getValueFactory().createValue(value));
    }

    public Property setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return setProperty(name,value);
    }

    public Property setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = null;
        try{
            Binary[] b = {new BinaryImpl(value)};
            data.getBinaryProperties().put(name,b);
            v = getSession().getValueFactory().createValue(new BinaryImpl(value));
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
        session.getChangedData().put(getPath(),data);
        return new ExternalPropertyImpl(new Name(name,NodeTypeRegistry.getInstance().getNamespaces()),this,(ExternalSessionImpl) getSession(), v);
    }

    public Property setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    public Property setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    public Property setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    public Property setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    public Property setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    public Node getNode(String s) throws PathNotFoundException, RepositoryException {
        String path = getPath().endsWith("/") ? getPath() : getPath()+"/";
        return session.getNode(path + s);
    }

    public NodeIterator getNodes() throws RepositoryException {
        final List<String> l = session.getRepository().getDataSource().getChildren(getPath());
        return new ExternalNodeIterator(l);
    }

    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        final List<String> l = session.getRepository().getDataSource().getChildren(getPath());
        final List<String> filteredList = new ArrayList<String>();
        for (String path : l) {
            if (ChildrenCollectorFilter.matches(path,namePattern)) {
                filteredList.add(path);
            }
        }
        return new ExternalNodeIterator(filteredList);
    }

    public Property getProperty(String s) throws PathNotFoundException, RepositoryException {
        Property property = properties.get(s);
        if (property == null) {
            throw new PathNotFoundException(s);
        }
        return property;
    }

    public PropertyIterator getProperties() throws RepositoryException {
        return new ExternalPropertyIterator(properties);
    }

    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        final Map<String, ExternalPropertyImpl> filteredList = new HashMap<String, ExternalPropertyImpl>();
        for (Map.Entry<String, ExternalPropertyImpl> entry : properties.entrySet()) {
            if (ChildrenCollectorFilter.matches(entry.getKey(),namePattern)) {
                filteredList.put(entry.getKey(), entry.getValue());
            }
        }
        return new ExternalPropertyIterator(filteredList);
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
        return new ExternalPropertyIterator(new HashMap<String, ExternalPropertyImpl>());
    }

    public boolean hasNode(String s) throws RepositoryException {
        try {
            getNode(s);
            return true;
        } catch (PathNotFoundException e) {
            return false;
        }
    }

    public boolean hasProperty(String relPath) throws RepositoryException {
        return properties.containsKey(relPath);
    }

    public boolean hasNodes() throws RepositoryException {
        return getNodes().hasNext();
    }

    public boolean hasProperties() throws RepositoryException {
        return !properties.isEmpty();
    }

    public NodeType getPrimaryNodeType() throws RepositoryException {
        return getExtendedPrimaryNodeType();
    }

    public ExtendedNodeType getExtendedPrimaryNodeType() throws RepositoryException {
        return NodeTypeRegistry.getInstance().getNodeType(data.getType());
    }

    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        List<NodeType> nt = new ArrayList<NodeType>();
        if (data.getMixin() != null) {
            for (String s : data.getMixin()) {
                nt.add(NodeTypeRegistry.getInstance().getNodeType(s));
            }
        }
        return nt.toArray(new NodeType[nt.size()]);
    }

    public boolean isNodeType(String nodeTypeName) throws RepositoryException {
        if (getPrimaryNodeType().isNodeType(nodeTypeName)) {
            return true;
        }
        for (NodeType nodeType : getMixinNodeTypes()) {
            if (nodeType.isNodeType(nodeTypeName)) {
                return true;
            }
        }
        return false;
    }

    public void addMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void removeMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean canAddMixin(String s) throws NoSuchNodeTypeException, RepositoryException {
        return false;
    }

    public NodeDefinition getDefinition() throws RepositoryException {
        ExternalNodeImpl parentNode = (ExternalNodeImpl) getParent();
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
        throw new UnsupportedRepositoryOperationException();
    }

    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
    }

    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void update(String s) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeIterator merge(String s, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        return getPath();
    }

    public boolean isCheckedOut() throws RepositoryException {
        return true;
    }

    public void restore(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void restore(Version version, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void restore(Version version, String s, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void restoreByLabel(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Lock lock(boolean b, boolean b1) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
        throw new UnsupportedRepositoryOperationException("Locking is not supported on External repository");
    }

    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedRepositoryOperationException("Locking is not supported on External repository");
    }

    public boolean holdsLock() throws RepositoryException {
        return false;
    }

    public boolean isLocked() throws RepositoryException {
        return false;
    }

    public Property setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return setProperty(name, value.getStream());
    }

    public Property setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        final List<String> l = session.getRepository().getDataSource().getChildren(getPath());
        final List<String> filteredList = new ArrayList<String>();
        for (String path : l) {
            if (ChildrenCollectorFilter.matches(path,nameGlobs)) {
                filteredList.add(path);
            }
        }
        return new ExternalNodeIterator(filteredList);
    }

    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        final Map<String, ExternalPropertyImpl> filteredList = new HashMap<String, ExternalPropertyImpl>();
        for (Map.Entry<String, ExternalPropertyImpl> entry : properties.entrySet()) {
            if (ChildrenCollectorFilter.matches(entry.getKey(),nameGlobs)) {
                filteredList.put(entry.getKey(), entry.getValue());
            }
        }
        return new ExternalPropertyIterator(filteredList);
    }

    public String getIdentifier() throws RepositoryException {
        if (session.getRepository().getDataSource().isSupportsUuid()) {
            return data.getId();
        }
        throw new UnsupportedRepositoryOperationException();
    }

    public PropertyIterator getReferences(String name) throws RepositoryException {
        return new ExternalPropertyIterator(new HashMap<String, ExternalPropertyImpl>());
    }

    public PropertyIterator getWeakReferences() throws RepositoryException {
        return new ExternalPropertyIterator(new HashMap<String, ExternalPropertyImpl>());
    }

    public PropertyIterator getWeakReferences(String name) throws RepositoryException {
        return new ExternalPropertyIterator(new HashMap<String, ExternalPropertyImpl>());
    }

    public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeIterator getSharedSet() throws RepositoryException {
        return new ExternalNodeIterator(new ArrayList<String>());
    }

    public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new String[0];  
    }

    private class ExternalPropertyIterator implements PropertyIterator {
        int pos;
        private final Iterator<ExternalPropertyImpl> it;
        private Map<String,ExternalPropertyImpl> properties;

        public ExternalPropertyIterator( Map<String,ExternalPropertyImpl> properties) {
            this.properties = properties;
            this.it = properties.values().iterator();
            pos = 0;
        }

        public Property nextProperty() {
            pos ++;
            return it.next();
        }

        public void skip(long skipNum) {
            for (int i=0; i<skipNum; i++) {
                nextProperty();
            }
            pos+= skipNum;
        }

        public long getSize() {
            return properties.size();
        }

        public long getPosition() {
            return pos;
        }

        public boolean hasNext() {
            return it.hasNext();
        }

        public Object next() {
            return nextProperty();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class ExternalNodeIterator implements NodeIterator {
        private int pos;
        private final Iterator<String> it;
        private final List<String> list;

        public ExternalNodeIterator(List<String> list) {
            this.list = list;
            it = list.iterator();
            pos = 0;
        }

        public Node nextNode() {
            pos++;
            try {
                return getNode(it.next());
            } catch (RepositoryException e) {
                e.printStackTrace();
                return null;
            }
        }

        public void skip(long skipNum) {
            for (int i = 0; i<skipNum ; i++) {
                it.next();
            }
            pos+=skipNum;
        }

        public long getSize() {
            return list.size();
        }

        public long getPosition() {
            return pos;
        }

        public boolean hasNext() {
            return it.hasNext();
        }

        public Object next() {
            return nextNode();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
