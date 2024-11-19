/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
            } else if (node != null && node.hasNodes()) {
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
                if(!excludedNodeTypes.isEmpty() && excludedNodeTypes.contains(child.getPrimaryNodeType().getName())) {
                	continue;  //ignore current, go to next one
                } else if (defs == null) {
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
