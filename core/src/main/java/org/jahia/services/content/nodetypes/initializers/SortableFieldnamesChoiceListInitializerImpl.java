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
package org.jahia.services.content.nodetypes.initializers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import org.apache.log4j.Logger;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.renderer.ChoiceListRenderer;
import org.jahia.services.render.RenderContext;

/**
 * Initializer to fill combobox with field names, which can be chosen to sort a
 * list.
 * 
 * @author Benjamin Papez
 * @author Sergiy Shyrkov
 */
public class SortableFieldnamesChoiceListInitializerImpl implements ChoiceListInitializer, ChoiceListRenderer {
    private transient static Logger logger = Logger.getLogger(TemplatesChoiceListInitializerImpl.class);

    private Set<String> excludedNodeTypes = Collections.emptySet();

    private boolean showHidden;

    private boolean showProtected = true;

    public List<ChoiceListValue> getChoiceListValues(ProcessingContext jParams,
            ExtendedPropertyDefinition declaringPropertyDefinition, String param, String realNodeType,
            List<ChoiceListValue> values) {
        if (jParams == null) {
            return Collections.emptyList();
        }

        JCRNodeWrapper node = (JCRNodeWrapper) jParams.getAttribute("contextNode");

        ExtendedPropertyDefinition[] propertyDefs;
        try {
            if (node == null && realNodeType == null) {
                return Collections.emptyList();
            } else if (node != null) {
                // TODO get the child nodes, their types and declared properties
                propertyDefs = getCommonChildNodeDefinitions(node);
            } else {
                propertyDefs = NodeTypeRegistry.getInstance().getNodeType(realNodeType)
                        .getPropertyDefinitions();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }

        List<ChoiceListValue> vs = new LinkedList<ChoiceListValue>();
        for (ExtendedPropertyDefinition propertyDef : propertyDefs) {
            vs.add(new ChoiceListValue(propertyDef.getLabel(jParams.getLocale()), null, new ValueImpl(
                    propertyDef.getName(), PropertyType.STRING, false)));
        }
        Collections.sort(vs);

        return vs;
    }

    @SuppressWarnings("unchecked")
    private ExtendedPropertyDefinition[] getCommonChildNodeDefinitions(JCRNodeWrapper node)
            throws RepositoryException {
        Map<String, Map<String, ExtendedPropertyDefinition>> defs = null;

        NodeIterator children = node.getNodes();
        while (children.hasNext()) {
            
            JCRNodeWrapper child = (JCRNodeWrapper) children.nextNode();
            if (defs == null) {
                // first child
                defs = LazyMap.decorate(new HashMap<String, Map<String, ExtendedPropertyDefinition>>(),
                        new Factory() {
                            public Object create() {
                                return new HashMap<String, ExtendedPropertyDefinition>();
                            }
                        });               
                for (ExtendedPropertyDefinition propertyDef : child.getPrimaryNodeType().getPropertyDefinitions()) {
                    // filter out hidden and protected if needed
                    if ((showHidden || !propertyDef.isHidden()) && (showProtected || !propertyDef.isProtected())) {
                        ExtendedNodeType nodeType = propertyDef.getDeclaringNodeType();
                        if (excludedNodeTypes.isEmpty() || !excludedNodeTypes.contains(nodeType.getName())) {
                            defs.get(nodeType.getName()).put(propertyDef.getName(), propertyDef);
                        }
                    }
                }
            } else {
                // filter out node types
                for (Iterator<String> iterator = defs.keySet().iterator(); iterator.hasNext();) {
                    String commonType = (String) iterator.next();
                    if (!child.isNodeType(commonType)) {
                        // the node has no such type --> remove the type from common
                        iterator.remove();
                    }
                }
            }
            
            if (defs.isEmpty()) {
                // no common property definitions found -> stop
                break;
            }
        }
        List<ExtendedPropertyDefinition> propertyDefinitions = new LinkedList<ExtendedPropertyDefinition>();
        for (Map<String, ExtendedPropertyDefinition> props : defs.values()) {
            propertyDefinitions.addAll(props.values());
        }

        return propertyDefinitions.toArray(new ExtendedPropertyDefinition[0]);
    }

    public Map<String, Object> getObjectRendering(RenderContext context, JCRPropertyWrapper propertyWrapper)
            throws RepositoryException {
        Map<String, Object> map = new HashMap<String, Object>(1);
        map.put("displayName", getStringRendering(context, propertyWrapper));
        return map;
    }

    public String getStringRendering(RenderContext context, JCRPropertyWrapper propertyWrapper)
            throws RepositoryException {
        return JCRContentUtils.getDisplayLabel(propertyWrapper, context.getMainResource().getLocale());
    }

    public void setExcludedNodeTypes(Set<String> excludedNodeTypes) {
        this.excludedNodeTypes = excludedNodeTypes;
    }

    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }

    public void setShowProtected(boolean showProtected) {
        this.showProtected = showProtected;
    }
}