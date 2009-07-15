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
package org.jahia.services.content.impl.jahia;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jahia.api.Constants;
import org.jahia.services.content.NodeIteratorImpl;
import org.jahia.services.content.PropertyIteratorImpl;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the JCR's {@link Node} interface to represent repository hierarchy item.
 * 
 * @author Serge Huber
 * Date: 17 d�c. 2007
 * Time: 10:05:27
 */
public abstract class NodeImpl extends ItemImpl implements Node {
    protected ExtendedNodeType nodetype;
    protected List<ExtendedNodeType> mixin = new ArrayList<ExtendedNodeType>();
    protected ExtendedNodeDefinition definition;

    protected Map<String, PropertyImpl> properties;
    protected List<PropertyImpl> i18nProperties = new ArrayList<PropertyImpl>();
    protected Map<String, List<Node>> nodes;
    protected Map<String, TranslationNodeImpl> translationNodes;
    protected Map<String, PropertyImpl> emptyProperties;

    public NodeImpl(SessionImpl session) {
        super(session);
    }

    protected void setDefinition(ExtendedNodeDefinition definition) {
        this.definition = definition;
    }

    protected void setNodetype(ExtendedNodeType nodetype) {
        this.nodetype = nodetype;
    }

    protected void initMixin(ExtendedNodeType mixin) {
        this.mixin.add(mixin);
    }

    protected void initProperties() throws RepositoryException {
        if (properties == null) {
            properties = new HashMap<String, PropertyImpl>();
            emptyProperties = new HashMap<String, PropertyImpl>();

            initProperty(new PropertyImpl(session,this,
                    NodeTypeRegistry.getInstance().getNodeType(Constants.NT_BASE).getPropertyDefinition(Constants.JCR_PRIMARYTYPE),null,
                    new ValueImpl(nodetype.getName(), PropertyType.NAME)));
        }
    }

    protected void initNodes() throws RepositoryException {
        if (nodes == null) {
            nodes = new ListOrderedMap();
            translationNodes = new HashMap<String, TranslationNodeImpl>();
        }
    }

    protected void initProperty(PropertyImpl p) throws RepositoryException {
        if (!p.isI18n()) {
            properties.put(p.getName(), p);
            if (p.getName()== null) {
                System.out.println("xx");
            }
        } else {
            i18nProperties.add(p);
        }
    }

    protected void initNode(NodeImpl n) throws RepositoryException  {
        List<Node> l = nodes.get(n.getName());
        if (l == null) {
            l = new ArrayList<Node>();
            nodes.put(n.getName(), l);
        }
        l.add(n);
    }

    public String getName() throws RepositoryException {
        return definition.getName();
    }

    public Node addNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Node addNode(String s, String s1) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void orderBefore(String s, String s1) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String s, Value value) throws ValueFormatException, VersionException, LockException, RepositoryException {
        Property p = getPropertyForSet(s);
        p.setValue(value);
        return p;
    }

    public Property setProperty(String s, Value[] values) throws ValueFormatException, VersionException, LockException, RepositoryException {
        Property p = getPropertyForSet(s);
        p.setValue(values);
        return p;
    }

    public Property setProperty(String s, Value[] values, int i) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String s, String[] strings) throws ValueFormatException, VersionException, LockException, RepositoryException {
        Property p = getPropertyForSet(s);
        p.setValue(strings);
        return p;
    }

    public Property setProperty(String s, String[] strings, int i) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String s, String value) throws ValueFormatException, VersionException, LockException, RepositoryException {
        Property p = getPropertyForSet(s);
        p.setValue(value);
        return p;
    }

    public Property setProperty(String s, InputStream inputStream) throws ValueFormatException, VersionException, LockException, RepositoryException {
        Property p = getPropertyForSet(s);
        p.setValue(inputStream);
        return p;
    }

    public Property setProperty(String s, boolean b) throws ValueFormatException, VersionException, LockException, RepositoryException {
        Property p = getPropertyForSet(s);
        p.setValue(b);
        return p;
    }

    public Property setProperty(String s, double v) throws ValueFormatException, VersionException, LockException, RepositoryException {
        Property p = getPropertyForSet(s);
        p.setValue(v);
        return p;
    }

    public Property setProperty(String s, long l) throws ValueFormatException, VersionException, LockException, RepositoryException {
        Property p = getPropertyForSet(s);
        p.setValue(l);
        return p;
    }

    public Property setProperty(String s, Calendar calendar) throws ValueFormatException, VersionException, LockException, RepositoryException {
        Property p = getPropertyForSet(s);
        p.setValue(calendar);
        return p;
    }

    public Property setProperty(String s, Node node) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String s, Value value, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String s, String s1, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    protected PropertyImpl getPropertyForSet(String s) throws RepositoryException {
        initProperties();
        PropertyImpl p = properties.get(s);
        if (p == null) {
            if (emptyProperties.containsKey(s)) {
                p = emptyProperties.remove(s);
                properties.put(s,p);
            }
        }
        return p;
    }

    public Node getNode(String name) throws PathNotFoundException, RepositoryException {
        int i = 0;
        if (name.indexOf('[')>0) {
            i = Integer.parseInt(name.substring(name.indexOf('[')+1, name.indexOf(']'))) - 1;
            name = name.substring(0,name.indexOf('['));
        }

        initNodes();

        List<Node> l = nodes.get(name);
        if (l == null) {
            throw new PathNotFoundException(name);
        }

        try {
            return l.get(i);
        } catch (IndexOutOfBoundsException e) {
            throw new PathNotFoundException(name);
        }
    }

    public NodeIterator getNodes() throws RepositoryException {
        initNodes();

        List<Node> results = new ArrayList<Node>();
        if (nodes != null) {
            for (List<Node> nodeList : nodes.values()) {
                results.addAll(nodeList);
            }
        }
        return new NodeIteratorImpl(results.iterator(), results.size());
    }

    public NodeIterator getNodes(String name) throws RepositoryException {
        initNodes();

        List<Node> l = nodes.get(name);
        if (l == null) {
            return new NodeIteratorImpl(Collections.EMPTY_LIST.iterator(), 0);
        }

        try {
            return new NodeIteratorImpl(l.iterator(), l.size());
        } catch (IndexOutOfBoundsException e) {
            throw new PathNotFoundException(name);
        }
    }

    public Property getProperty(String s) throws PathNotFoundException, RepositoryException {
        initProperties();

        Property p = properties.get(s);

        if (p == null) {
            throw new PathNotFoundException(s);
        }

        return p;
    }

    public PropertyIterator getProperties() throws RepositoryException {
        initProperties();
        return new PropertyIteratorImpl(properties.values().iterator(), properties.size());
    }

    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        String s = nodetype.getPrimaryItemName();
        if (s != null) {
            initProperties();
            if (properties.containsKey(s)) {
                return properties.get(s);
            }
            initNodes();
            if (nodes.containsKey(s)) {
                return nodes.get(s).iterator().next();
            }
        }

        throw new ItemNotFoundException();
    }

    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public int getIndex() throws RepositoryException {
        return 1;
    }

    public PropertyIterator getReferences() throws RepositoryException {
        return new PropertyIteratorImpl(Collections.emptyList().iterator(),0);
    }

    public boolean hasNode(String s) throws RepositoryException {
        initNodes();

        return nodes.containsKey(s);
    }

    public boolean hasProperty(String s) throws RepositoryException {
        initProperties();

        return properties.containsKey(s);
    }

    public boolean hasNodes() throws RepositoryException {
        initNodes();

        return !nodes.isEmpty();
    }

    public boolean hasProperties() throws RepositoryException {
        initProperties();

        return !properties.isEmpty();
    }

    public ExtendedNodeType getPrimaryNodeType() throws RepositoryException {
        return nodetype;
    }

    public ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException {
        return mixin.toArray(new ExtendedNodeType[mixin.size()]);
    }

    public boolean isNodeType(String s) throws RepositoryException {
        if (getPrimaryNodeType().isNodeType(s)) {
            return true;
        }
        for (ExtendedNodeType mixinType : mixin) {
            if (mixinType.isNodeType(s)) {
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

    public boolean canAddMixin(String s) throws RepositoryException {
        return false;
    }

    public NodeDefinition getDefinition() throws RepositoryException {
        return definition;
    }

    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
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

    public NodeIterator merge(String s, boolean b) throws UnsupportedRepositoryOperationException, NoSuchWorkspaceException, AccessDeniedException, VersionException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public String getCorrespondingNodePath(String s) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean isCheckedOut() throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
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
        throw new LockException();
    }

    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
        throw new LockException();
    }

    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        throw new LockException();
    }

    public boolean holdsLock() throws RepositoryException {
        return false;
    }

    public boolean isLocked() throws RepositoryException {
        return false;
    }

    public boolean isNode() {
        return true;
    }

    public void save() throws AccessDeniedException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, RepositoryException {
        NodeIterator ni = getNodes();
        while (ni.hasNext()) {
            ni.nextNode().save();
        }
        PropertyIterator pi = getProperties();
        while (pi.hasNext()) {
            pi.nextProperty().save();
        }
        super.save();
    }
}
