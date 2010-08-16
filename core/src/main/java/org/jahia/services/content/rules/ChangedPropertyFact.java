/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.content.rules;

import org.apache.tika.io.IOUtils;
import org.drools.spi.KnowledgeHelper;
import org.jahia.services.categories.Category;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.SelectorType;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 20 déc. 2007
 * Time: 17:27:10
 * To change this template use File | Settings | File Templates.
 */

public class ChangedPropertyFact implements Updateable {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ChangedPropertyFact.class);

    private String path;
    private JCRPropertyWrapper property;

    private String nodePath;
    private String name;
    private Object value;

    private AddedNodeFact nodeWrapper;

    public ChangedPropertyFact(AddedNodeFact nodeWrapper, JCRPropertyWrapper property) throws RepositoryException {
        this.nodeWrapper = nodeWrapper;
        this.property = property;
        path = property.getPath();
    }

    public ChangedPropertyFact(AddedNodeFact nodeWrapper, final String name, final Object o, KnowledgeHelper drools) throws RepositoryException {
        this(nodeWrapper, name, o, drools, true);
    }
    public ChangedPropertyFact(AddedNodeFact nodeWrapper, final String name, final Object o, KnowledgeHelper drools,
                               final boolean overrideIfExisting) throws RepositoryException {
        if (nodeWrapper == null) {
            return;
        }

        this.nodeWrapper = nodeWrapper;

        JCRNodeWrapper node = nodeWrapper.getNode();
        nodePath = nodeWrapper.getPath();
        this.name = name;
        value = o;

        if (node == null || node.isLocked()) {
            logger.debug("Node is locked, delay property update to later");
            List<Updateable> list = (List<Updateable>) drools.getWorkingMemory().getGlobal("delayedUpdates");
            list.add(this);
        } else {
            setProperty(node, name, o,overrideIfExisting);
        }
    }

    public void doUpdate(JCRSessionWrapper s, List<Updateable> delayedUpdates) throws RepositoryException {
        try {
            JCRNodeWrapper node = s.getNode(nodePath);

            if (node.isLocked()) {
                logger.debug("Node is still locked, delay property update to later");
                delayedUpdates.add(this);
            } else {
                if (!node.isCheckedOut()) {
                    node.checkout();
                }

                setProperty(node, name, value,true);
            }
        } catch (PathNotFoundException e) {
            logger.warn("Node does not exist " + nodePath);
        }
    }

    private ExtendedPropertyDefinition getPropertyDefinition(Node node, String name)
            throws RepositoryException {

        Map<String, ExtendedPropertyDefinition> defs = new HashMap<String, ExtendedPropertyDefinition>();
        NodeTypeRegistry reg = NodeTypeRegistry.getInstance();
        ExtendedPropertyDefinition propDef = null;
        try {

            ExtendedNodeType nt = reg.getNodeType(node.getPrimaryNodeType()
                    .getName());
            defs.putAll(nt.getPropertyDefinitionsAsMap());
            NodeType[] p = node.getMixinNodeTypes();
            for (int i = 0; i < p.length; i++) {
                defs.putAll(reg.getNodeType(p[i].getName())
                        .getPropertyDefinitionsAsMap());
            }
            propDef = defs.get(name);
        } catch (NoSuchNodeTypeException e) {
            logger.debug("Nodetype not supported", e);
        }
        return propDef;

    }

    protected void setProperty(JCRNodeWrapper node, String name, Object objectValue,final boolean overrideIfExisting)
            throws RepositoryException {

        try {
            if(!overrideIfExisting){
                try {
                    node.getProperty(name);
                    return;
                } catch (RepositoryException e) {
                    logger.debug("Create new property "+name+" on node "+node.getPath());
                }
            }
            // deal with versioning. this method is called at restore(...)
//            if (node.isNodeType(Constants.MIX_VERSIONABLE)) {
//                node.checkout();
//            }

            ExtendedPropertyDefinition propDef = getPropertyDefinition(node, name);
            if (propDef == null) {
                logger.error("Property " + name + " does not exist in "
                        + node.getPath() + " !");
                return;
            }
            ValueFactory factory = node.getSession().getValueFactory();

            Value[] values;
            if (objectValue.getClass().isArray()) {
                values = new Value[Array.getLength(objectValue)];
                for (int i = 0; i < Array.getLength(objectValue); i++) {
                    values[i] = createValue(Array.get(objectValue, i), propDef,
                            factory);
                }
            } else {
                values = new Value[] { createValue(objectValue, propDef,
                        factory) };
            }

            if (values.length > 0) {
                if (!node.isCheckedOut()) {
                    node.checkout();
                }
                if (!propDef.isMultiple()) {
                    property = node.setProperty(name, values[0]);
                } else {
                    if (node.hasProperty(name)) {
                        property = node.getProperty(name);
                        Value[] oldValues = property.getValues();
                        Value[] newValues = new Value[oldValues.length + values.length];
                        System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
                        System.arraycopy(values, oldValues.length, newValues, 0, values.length);
                        property.setValue(newValues);
                    } else {
                        property = node.setProperty(name, values);
                    }
                }

                logger.debug("Property set" + nodePath + " / " + name);
                if (property != null) {
                    path = property.getPath();
                }
            }
        } catch (NoSuchNodeTypeException e) {
            logger.debug("Nodetype not supported", e);
        } finally {
            if (objectValue instanceof File) {
                ((File) objectValue).delete();
            }
        }
    }

    private Value createValue(Object objectValue, ExtendedPropertyDefinition propDef, ValueFactory factory) {
        if (objectValue instanceof String) {
            if (propDef.getSelector() == SelectorType.CATEGORY) {
                try {
                    return factory.createValue(Category.getCategoryPath((String) objectValue));
                } catch (Exception e) {
                    logger.warn("Can't get category " + objectValue + ", cause " + e.getMessage());
                }
            } else {
                return factory.createValue((String) objectValue);
            }
        } else if (objectValue instanceof Long) {
            return factory.createValue((Long) objectValue);
        } else if (objectValue instanceof Integer) {
            return factory.createValue(((Integer) objectValue).longValue());
        } else if (objectValue instanceof Calendar) {
            return factory.createValue((Calendar) objectValue);
        } else if (objectValue instanceof Date) {
            Calendar c = new GregorianCalendar();
            c.setTime((Date) objectValue);
            return factory.createValue(c);
        } else if (objectValue instanceof byte[] || objectValue instanceof File) {
            InputStream is = null;
            try {
                is = objectValue instanceof File ? new FileInputStream((File) objectValue) : new ByteArrayInputStream((byte[]) objectValue);
                return factory.createValue(factory.createBinary(is));
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        return null;
    }

    public String getName() throws RepositoryException {
        if (property != null) {
            return property.getName();
        }
        return null;
    }

    public String getStringValue() throws RepositoryException {
        if (property != null) {
            if (property.getDefinition().isMultiple()) {
                return getStringValues().toString();
            }
            return property.getString();
        }
        return null;
    }

    public List<String> getStringValues() throws RepositoryException {
        List<String> r = new ArrayList<String>();
        if (property != null && property.getDefinition().isMultiple()) {
            Value[] vs = property.getValues();
            for (Value v : vs) {
                r.add(v.getString());
            }
        } else {
            r.add(getStringValue());
        }
        return r;
    }

    public Object getValues() throws RepositoryException {
        if (property != null) {
            return property.getValues();
        }
        return null;
    }

    public Object getValue() throws RepositoryException {
        if (property != null) {
            return property.getValue();
        }
        return null;
    }

    public int getType() throws RepositoryException {
        if (property != null) {
            return property.getType();
        }
        return 0;
    }

    public AddedNodeFact getNode() {
        return nodeWrapper;
    }

    JCRPropertyWrapper getProperty() {
        return property;
    }

    public String toString() {
        return path;
    }

}
