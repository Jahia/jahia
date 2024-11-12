/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.rules;

import org.drools.core.spi.KnowledgeHelper;
import org.jahia.api.Constants;
import org.jahia.services.categories.Category;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.SelectorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class describe a property change event for the drools engine.
 * User: toto
 */

public class ChangedPropertyFact implements Updateable, ModifiedPropertyFact {
    private static final Logger logger = LoggerFactory.getLogger(ChangedPropertyFact.class);

    private String path;
    private JCRPropertyWrapper property;

    private String nodePath;
    private String name;
    private Object value;

    private AddedNodeFact nodeWrapper;
    private String operationType;

    public ChangedPropertyFact(AddedNodeFact nodeWrapper, JCRPropertyWrapper property) throws RepositoryException {
        this.nodeWrapper = nodeWrapper;
        this.property = property;
        path = property.getPath();
        operationType = nodeWrapper.getOperationType();
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

        if (node == null || AddedNodeFact.isLocked(node)) {
            logger.debug("Node is locked, delay property update to later");
            @SuppressWarnings("unchecked") List<Updateable> list = (List<Updateable>) drools.getWorkingMemory().getGlobal("delayedUpdates");
            list.add(this);
        } else {
            setProperty(node, name, o, overrideIfExisting);
        }
        operationType = nodeWrapper.getOperationType();
    }

    @Override
    public void doUpdate(JCRSessionWrapper s, List<Updateable> delayedUpdates) throws RepositoryException {
        try {
            JCRNodeWrapper node = s.getNode(nodePath);

            if (AddedNodeFact.isLocked(node)) {
                logger.debug("Node is still locked, delay property update to later");
                delayedUpdates.add(this);
            } else {
                setProperty(node, name, value, true);
            }
        } catch (PathNotFoundException e) {
            logger.warn("Node does not exist {}", nodePath);
        }
    }

    private ExtendedPropertyDefinition getPropertyDefinition(Node node, String name)
            throws RepositoryException {

        Map<String, ExtendedPropertyDefinition> defs = new HashMap<>();
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

    protected void setProperty(JCRNodeWrapper node, String name, Object objectValue, final boolean overrideIfExisting)
            throws RepositoryException {

        try {
            if (!overrideIfExisting && propertyExists(node, name)) {
                return;
            }

            ExtendedPropertyDefinition propDef = getPropertyDefinition(node, name);
            if (propDef == null) {
                logger.error("Property {} does not exist in {} !", node, node.getPath());
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
                values = new Value[]{createValue(objectValue, propDef,
                        factory)};
            }

            if (values.length > 0) {
                setProperty(node, name, propDef, values);
            }
        } catch (NoSuchNodeTypeException e) {
            logger.debug("Nodetype not supported", e);
        } finally {
            if (objectValue instanceof File) {
                ((File) objectValue).delete();
            }
        }
    }

    private void setProperty(JCRNodeWrapper node, String name, ExtendedPropertyDefinition propDef, Value[] values) throws RepositoryException {
        JCRNodeWrapper nodeRef = node;

        if (propDef.isInternationalized()) {
            String language = node.getResolveSite().getDefaultLanguage();
            String translationNodeName = "j:translation_" + language;

            if (!node.hasNode(translationNodeName)) {
                nodeRef = node.addNode(translationNodeName, Constants.JAHIANT_TRANSLATION);
                nodeRef.setProperty(Constants.JCR_LANGUAGE, language);
            } else {
                nodeRef = node.getNode(translationNodeName);
            }

            if (propDef.getDeclaringNodeType().isMixin() && !nodeRef.isNodeType(propDef.getDeclaringNodeType().getName())) {
                nodeRef.addMixin(propDef.getDeclaringNodeType().getName());
            }
        }

        if (!propDef.isMultiple()) {
            property = nodeRef.setProperty(name, values[0]);
        } else {
            if (nodeRef.hasProperty(name)) {
                property = nodeRef.getProperty(name);
                Value[] oldValues = property.getValues();
                Value[] newValues = new Value[oldValues.length + values.length];
                System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
                System.arraycopy(values, 0, newValues, oldValues.length, values.length);
                property.setValue(newValues);
            } else {
                property = nodeRef.setProperty(name, values);
            }
        }

        logger.debug("Property set {} / {}", nodePath, name);
        if (property != null) {
            path = property.getPath();
        }
    }

    private boolean propertyExists(JCRNodeWrapper node, String name) {
        try {
            node.getProperty(name);
            return true;
        } catch (RepositoryException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Create new property {} on node ", node.getPath());
            }
        }
        return false;
    }

    private Value createValue(Object objectValue, ExtendedPropertyDefinition propDef, ValueFactory factory) {
        if (objectValue instanceof String && propDef.getSelector() == SelectorType.CATEGORY) {
            try {
                return factory.createValue(Category.getCategoryPath((String) objectValue));
            } catch (Exception e) {
                logger.warn("Can't get category {}, cause {}", objectValue, e.getMessage());
                return null;
            }
        } else {
            return JCRContentUtils.createValue(objectValue, factory);
        }
    }

    @Override
    public String getName() throws RepositoryException {
        if (property != null) {
            return property.getName();
        }
        return null;
    }

    public String getStringValue() throws RepositoryException {
        if (property != null) {
            if (property.isMultiple()) {
                return getStringValues().toString();
            }
            JCRValueWrapper v = property.getValue();
            if (v.getType() == PropertyType.WEAKREFERENCE || v.getType() == PropertyType.REFERENCE) {
                JCRNodeWrapper node = v.getNode();
                if (node != null) {
                    return node.getPath();
                }
            } else {
                return v.getString();
            }
        }
        return null;
    }

    public List<String> getStringValues() throws RepositoryException {
        List<String> r = new ArrayList<>();
        if (property != null && property.isMultiple()) {
            Value[] vs = property.getValues();
            for (Value v : vs) {
                if (v.getType() == PropertyType.WEAKREFERENCE || v.getType() == PropertyType.REFERENCE) {
                    JCRNodeWrapper node = ((JCRValueWrapper) v).getNode();
                    if (node != null) {
                        r.add(node.getPath());
                    }
                } else {
                    r.add(v.getString());
                }
            }
        } else {
            r.add(getStringValue());
        }
        return r;
    }

    public AddedNodeFact getNodeValue() throws RepositoryException {
        if (property != null) {
            if (property.isMultiple()) {
                return null;
            }
            JCRValueWrapper v = property.getValue();
            if (v.getType() == PropertyType.WEAKREFERENCE || v.getType() == PropertyType.REFERENCE) {
                JCRNodeWrapper node = v.getNode();
                if (node != null) {
                    return new AddedNodeFact(node);
                }
            } else {
                return null;
            }
        }
        return null;
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

    public String getLanguage() throws RepositoryException {
        if (property != null) {
            return property.getLocale();
        }
        return null;
    }

    @Override
    public AddedNodeFact getNode() {
        return nodeWrapper;
    }

    JCRPropertyWrapper getProperty() {
        return property;
    }

    @Override
    public String toString() {
        return path;
    }

    /**
     * Returns the current JCR operation type.
     *
     * @return the current JCR operation type
     * @throws javax.jcr.RepositoryException in case of a repository access error
     * @since Jahia 6.6
     */
    public String getOperationType() {
        return operationType;
    }

    public List<String> getInstalledModules() {
        return nodeWrapper.getInstalledModules();
    }

    @Override
    public String getWorkspace() throws RepositoryException {
        return getNode().getWorkspace();
    }

    @Override
    public JCRSessionWrapper getSession() throws RepositoryException {
        return getNode().getSession();
    }

    @Override
    public String getNodeIdentifier() throws RepositoryException {
        return getNode().getIdentifier();
    }

    @Override
    public String getNodeType() throws RepositoryException {
        return getNode().getNodeType();
    }
}
