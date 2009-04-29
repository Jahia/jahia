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
package org.jahia.services.content.automation;

import org.drools.spi.KnowledgeHelper;
import org.jahia.services.categories.Category;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.SelectorType;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.lang.reflect.Array;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 20 déc. 2007
 * Time: 17:27:10
 * To change this template use File | Settings | File Templates.
 */
public class PropertyWrapper implements Updateable  {
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(PropertyWrapper.class);

    private String path;
    private Property property;

    private String nodePath;
    private String name;
    private Object value;

    private NodeWrapper nodeWrapper;

    public PropertyWrapper(NodeWrapper nodeWrapper, Property property) throws RepositoryException {
        this.nodeWrapper = nodeWrapper;
        this.property = property;
        path = property.getPath();
    }

    public PropertyWrapper(NodeWrapper nodeWrapper, final String name, final Object o, KnowledgeHelper drools) throws RepositoryException {
        if (nodeWrapper == null) {
            return;
        }

        this.nodeWrapper = nodeWrapper;

        Node node = nodeWrapper.getNode();
        nodePath = nodeWrapper.getPath();
        this.name = name;
        value = o;

        if (node == null || node.isLocked()) {
            logger.debug("Node is locked, delay property update to later");
            List<Updateable> list = (List<Updateable>) drools.getWorkingMemory().getGlobal("delayedUpdates");
            list.add(this);
        } else {
            setProperty(node, name, o);
        }
    }

    public void doUpdate(Session s, List<Updateable> delayedUpdates) throws RepositoryException {
        try {
            Node node = (Node) s.getItem(nodePath);

            if (node.isLocked()) {
                logger.debug("Node is still locked, delay property update to later");
                delayedUpdates.add(this);
            } else {
                setProperty(node, name, value);
            }
        } catch (PathNotFoundException e) {
            logger.warn("Node does not exist "+nodePath);
        }
    }

    protected void setProperty(Node node, String name, Object objectValue) throws RepositoryException {

        Map<String, ExtendedPropertyDefinition> defs = new HashMap<String, ExtendedPropertyDefinition>();
        NodeTypeRegistry reg = NodeTypeRegistry.getInstance();
        ExtendedNodeType nt = null;
        try {
            nt = reg.getNodeType(node.getPrimaryNodeType().getName());
            defs.putAll(nt.getPropertyDefinitionsAsMap());
            NodeType[] p = node.getMixinNodeTypes();
            for (int i = 0; i < p.length; i++) {
                defs.putAll(reg.getNodeType(p[i].getName()).getPropertyDefinitionsAsMap());
            }
            ExtendedPropertyDefinition propDef = defs.get(name);
            if (propDef == null) {
                logger.error("Property "+name +" does not exist in " +node.getPath() + " !");
                return;
            }
            ValueFactory factory = node.getSession().getValueFactory();

            Value[] values = null;
            if (objectValue.getClass().isArray()) {
                values = new Value[Array.getLength(objectValue)];
                for (int i=0; i<Array.getLength(objectValue); i++) {
                    values[i] = createValue(Array.get(objectValue, i), propDef, factory);
                }
            } else {
                values = new Value[] {createValue(objectValue, propDef, factory)};
            }

            if (values != null && values.length>0) {
                if (!propDef.isMultiple()) {
                    property = node.setProperty(name, values[0]);
                } else {
                    if (node.hasProperty(name)) {
                        Value[] oldValues = property.getValues();
                        Value[] newValues = new Value[oldValues.length+values.length];
                        System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
                        System.arraycopy(values, oldValues.length, newValues, 0, values.length);
                        property.setValue(newValues);
                    } else {
                        property = node.setProperty(name, values);
                    }
                }

                logger.debug("Property set" + nodePath +" / " +name);
                if (property != null) {
                    path = property.getPath();
                }
                if (objectValue instanceof File) {
                    ((File)objectValue).delete();
                }
            }
        } catch (NoSuchNodeTypeException e) {
            logger.debug("Nodetype not supported",e);
        }
    }

    private Value createValue(Object objectValue, ExtendedPropertyDefinition propDef, ValueFactory factory) {
        if (objectValue instanceof String) {
            if (propDef.getSelector() == SelectorType.CATEGORY) {
                try {
                    return factory.createValue(Category.getCategoryPath((String) objectValue)) ;
                } catch (Exception e) {
                    logger.warn("Can't get category "+objectValue + ", cause " + e.getMessage());
                }
            } else {
                return factory.createValue((String) objectValue) ;
            }
        } else if (objectValue instanceof Long) {
            return factory.createValue((Long) objectValue) ;
        } else if (objectValue instanceof Integer) {
            return factory.createValue(((Integer) objectValue).longValue()) ;
        } else if (objectValue instanceof Calendar) {
            return factory.createValue((Calendar) objectValue) ;
        } else if (objectValue instanceof Date) {
            Calendar c = new GregorianCalendar();
            c.setTime((Date) objectValue);
            return factory.createValue(c) ;
        } else if (objectValue instanceof byte[]) {
            return factory.createValue(new ByteArrayInputStream((byte[]) objectValue)) ;
        } else if (objectValue instanceof File) {
            try {
                return factory.createValue(new FileInputStream((File) objectValue)) ;
            } catch (FileNotFoundException e) {
                logger.error("File not found ",e);
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
        if (property != null) {
            Value[] vs =  property.getValues();
            for (int i = 0; i < vs.length; i++) {
                Value v = vs[i];
                r.add(v.getString());
            }
        }
        return r;
    }

    public Object getValue() throws RepositoryException {
        if (property != null) {
            return  property.getValues();
        }
        return null;
    }

    public int getType() throws RepositoryException {
        if (property != null) {
            return property.getType();
        }
        return 0;
    }

    public NodeWrapper getNode() {
        return nodeWrapper;
    }

    Property getProperty() {
        return property;
    }

    public String toString() {
        return path;
    }

}
