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

package org.jahia.services.content.nodetypes.initializers;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.renderer.AbstractChoiceListRenderer;
import org.jahia.services.render.RenderContext;

import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Initializer to fill combobox with field names, which can be chosen to sort a
 * list.
 *
 * @author Benjamin Papez
 * @author Sergiy Shyrkov
 */
public class SortableFieldnamesChoiceListInitializerImpl extends AbstractChoiceListRenderer implements ChoiceListInitializer {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(TemplatesChoiceListInitializerImpl.class);

    private Set<String> excludedNodeTypes = Collections.emptySet();

    private boolean showHidden;

    private boolean showProtected = true;

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition declaringPropertyDefinition, String param,
                                                     List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        if (context == null) {
            return Collections.emptyList();
        }

        JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
        ExtendedNodeType realNodeType = (ExtendedNodeType) context.get("contextType");

        ExtendedPropertyDefinition[] propertyDefs;
        CommonDefinitions commonChildNodeDefinitions = null;
        try {
            if (node == null && realNodeType == null) {
                return Collections.emptyList();
            } else if (node != null && node.getNodes().hasNext()) {
                commonChildNodeDefinitions = getCommonChildNodeDefinitions(node, showHidden,
                        showProtected, excludedNodeTypes);
                propertyDefs = commonChildNodeDefinitions.getPropertyDefinitions();
            } else {
                propertyDefs = realNodeType.getPropertyDefinitions();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }

        List<ChoiceListValue> vs = new LinkedList<ChoiceListValue>();
        for (ExtendedPropertyDefinition propertyDef : propertyDefs) {
            vs.add(new ChoiceListValue(propertyDef.getLabel(locale), null, new ValueImpl(
                    propertyDef.getName(), PropertyType.STRING, false)));
        }
        if(commonChildNodeDefinitions!=null && commonChildNodeDefinitions.getReferencedDefs()!=null && commonChildNodeDefinitions.getReferencedDefs().size()>0) {
            Map<String, Map<ExtendedPropertyDefinition, Map<String, ExtendedPropertyDefinition>>> referencedDefs = commonChildNodeDefinitions.getReferencedDefs();
            for (Map.Entry<String, Map<ExtendedPropertyDefinition, Map<String, ExtendedPropertyDefinition>>> entry : referencedDefs.entrySet()) {
                for (Map.Entry<ExtendedPropertyDefinition, Map<String, ExtendedPropertyDefinition>> s : entry.getValue().entrySet()) {
                    for (ExtendedPropertyDefinition propertyDefinition : s.getValue().values()) {
                        vs.add(new ChoiceListValue(s.getKey().getLabel(locale)+"->"+propertyDefinition.getLabel(locale),null,new ValueImpl(
                                s.getKey().getName()+";"+propertyDefinition.getName(),PropertyType.STRING,false)));
                    }
                }
            }
        }
        Collections.sort(vs);
        return vs;
    }

    @SuppressWarnings("unchecked")
    public static CommonDefinitions getCommonChildNodeDefinitions(JCRNodeWrapper node,boolean showHidden,boolean showProtected,Set<String> excludedNodeTypes) throws RepositoryException {
        Map<String, Map<String, ExtendedPropertyDefinition>> defs = null;
        Map<String, Map<ExtendedPropertyDefinition, Map<String, ExtendedPropertyDefinition>>> referencedDefs = null;
        if (node.hasNodes()) {
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
                    referencedDefs = LazyMap.decorate(
                            new HashMap<String, Map<String, Map<String, ExtendedPropertyDefinition>>>(), new Factory() {
                                public Object create() {
                                    return LazyMap.decorate(
                                            new HashMap<ExtendedPropertyDefinition, Map<String, ExtendedPropertyDefinition>>(),
                                            new Factory() {
                                                public Object create() {
                                                    return new HashMap<String, ExtendedPropertyDefinition>();
                                                }
                                            });
                                }
                            });
                    for (ExtendedPropertyDefinition propertyDef : child.getPrimaryNodeType().getPropertyDefinitions()) {
                        // filter out hidden and protected if needed
                        if ((showHidden || !propertyDef.isHidden()) && (showProtected || !propertyDef.isProtected())) {
                            if (propertyDef.getRequiredType() == PropertyType.WEAKREFERENCE || propertyDef.getRequiredType() == PropertyType.REFERENCE) {
                                ExtendedNodeType nodeType = propertyDef.getDeclaringNodeType();
                                if (excludedNodeTypes.isEmpty() || !excludedNodeTypes.contains(nodeType.getName())) {
                                    String[] constraints = propertyDef.getValueConstraints();
                                    for (String constraint : constraints) {
                                        ExtendedNodeType extendedNodeType = NodeTypeRegistry.getInstance().getNodeType(
                                                constraint);
                                        ExtendedPropertyDefinition[] declaredPropertyDefinitions = extendedNodeType.getPropertyDefinitions();
                                        for (ExtendedPropertyDefinition declaredPropertyDefinition : declaredPropertyDefinitions) {
                                            if ((showHidden || !declaredPropertyDefinition.isHidden()) && (showProtected || !declaredPropertyDefinition.isProtected())) {
                                                referencedDefs.get(nodeType.getName()).get(propertyDef).put(declaredPropertyDefinition.getName(),declaredPropertyDefinition);
                                            }
                                        }
                                    }
                                }
                            } else {
                                ExtendedNodeType nodeType = propertyDef.getDeclaringNodeType();
                                if (excludedNodeTypes.isEmpty() || !excludedNodeTypes.contains(nodeType.getName())) {
                                    defs.get(nodeType.getName()).put(propertyDef.getName(), propertyDef);
                                }
                            }
                        }
                    }
                } else {
                    // filter out node types
                    for (Iterator<String> iterator = defs.keySet().iterator(); iterator.hasNext();) {
                        String commonType = iterator.next();
                        if (!child.isNodeType(commonType)) {
                            // the node has no such type --> remove the type from common
                            iterator.remove();
                            referencedDefs.remove(commonType);
                        }
                    }
                }

                if (defs.isEmpty()) {
                    // no common property definitions found -> stop
                    break;
                }
            }
            List<ExtendedPropertyDefinition> propertyDefinitions = new LinkedList<ExtendedPropertyDefinition>();
            if (defs != null) {
                for (Map<String, ExtendedPropertyDefinition> props : defs.values()) {
                    propertyDefinitions.addAll(props.values());
                }
            }
            return new CommonDefinitions((ExtendedPropertyDefinition[]) propertyDefinitions.toArray(new ExtendedPropertyDefinition[propertyDefinitions.size()]),referencedDefs);
        } else {
            return new CommonDefinitions(new ExtendedPropertyDefinition[0],null);
        }
    }

    public String getStringRendering(RenderContext context, JCRPropertyWrapper propertyWrapper)
            throws RepositoryException {
        return JCRContentUtils.getDisplayLabel(propertyWrapper, context.getMainResource().getLocale(), propertyWrapper.getParent().getPrimaryNodeType());
    }
    
    public String getStringRendering(Locale locale, ExtendedPropertyDefinition propDef,
            Object propertyValue) throws RepositoryException {
        return JCRContentUtils.getDisplayLabel(propertyValue, locale, propDef.getDeclaringNodeType());
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

    public static class CommonDefinitions {

        ExtendedPropertyDefinition[] propertyDefinitions = null;
        Map<String, Map<ExtendedPropertyDefinition, Map<String, ExtendedPropertyDefinition>>> referencedDefs = null;

        public CommonDefinitions(ExtendedPropertyDefinition[] propertyDefinitions,
                                 Map<String, Map<ExtendedPropertyDefinition, Map<String, ExtendedPropertyDefinition>>> referencedDefs) {
            this.propertyDefinitions = propertyDefinitions;
            this.referencedDefs = referencedDefs;
        }

        public ExtendedPropertyDefinition[] getPropertyDefinitions() {
            return propertyDefinitions;
        }

        public Map<String, Map<ExtendedPropertyDefinition, Map<String, ExtendedPropertyDefinition>>> getReferencedDefs() {
            return referencedDefs;
        }
    }
}