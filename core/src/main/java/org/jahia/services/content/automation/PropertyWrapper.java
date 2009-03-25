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
            Value value = null;
            if (objectValue instanceof String) {
                if (propDef.getSelector() == SelectorType.CATEGORY) {
                    try {
                        value = factory.createValue(Category.getCategoryPath((String) objectValue));
                    } catch (Exception e) {
                        logger.warn("Can't get category "+objectValue + ", cause " + e.getMessage());
                    }
                } else {
                    value = factory.createValue((String) objectValue);
                }
            } else if (objectValue instanceof Long) {
                value = factory.createValue((Long) objectValue);
            } else if (objectValue instanceof Integer) {
                value = factory.createValue(((Integer) objectValue).longValue());
            } else if (objectValue instanceof Calendar) {
                value = factory.createValue((Calendar) objectValue);
            } else if (objectValue instanceof Date) {
                Calendar c = new GregorianCalendar();
                c.setTime((Date) objectValue);
                value = factory.createValue(c);
            } else if (objectValue instanceof byte[]) {
                value = factory.createValue(new ByteArrayInputStream((byte[]) objectValue));
            } else if (objectValue instanceof File) {
                try {
                    value = factory.createValue(new FileInputStream((File) objectValue));
                } catch (FileNotFoundException e) {
                    logger.error("File not found ",e);
                }
            }
            if (value != null) {
                if (!propDef.isMultiple()) {
                    property = node.setProperty(name, value);
                } else {
                    if (node.hasProperty(name)) {
                        Value[] values = property.getValues();
                        Value[] newValues = new Value[values.length+1];
                        System.arraycopy(values, 0, newValues, 0, values.length);
                        newValues[values.length] = value;
                        property.setValue(newValues);
                    } else {
                        property = node.setProperty(name, new Value[] {value});
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

    public String getName() throws RepositoryException {
        if (property != null) {
            return property.getName();
        }
        return null;
    }

    public String getStringValue() throws RepositoryException {
        if (property != null) {
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
