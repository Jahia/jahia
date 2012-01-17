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

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.slf4j.Logger;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.View;

import javax.jcr.*;
import java.io.File;
import java.util.*;

/**
 * Choice list initializer to provide a selection of available templates.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 17 nov. 2009
 */
public class TemplatesChoiceListInitializerImpl implements ChoiceListInitializer {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(TemplatesChoiceListInitializerImpl.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition declaringPropertyDefinition, String param,
                                                     List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        if (context == null) {
            return new ArrayList<ChoiceListValue>();
        }
        JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
        JCRNodeWrapper parentNode = (JCRNodeWrapper) context.get("contextParent");
        ExtendedNodeType realNodeType = (ExtendedNodeType) context.get("contextType");
        String propertyName = context.containsKey("dependentProperties") ? ((List<String>)context.get("dependentProperties")).get(0) : null;

        JCRSiteNode site = null;

        SortedSet<View> views = new TreeSet<View>();

        try {
            if (node != null) {
                site = node.getResolveSite();
            }
            if (site == null && parentNode != null) {
                site = parentNode.getResolveSite();
            }

            final List<String> nodeTypeList = new ArrayList<String>();
            String nextParam = "";
            if (param.contains(",")) {
                nextParam = StringUtils.substringAfter(param, ",");
                param =  StringUtils.substringBefore(param, ",");
            }
            if ("subnodes".equals(param)) {
                if (propertyName == null) {
                    propertyName = "j:allowedTypes";
                }
                if (context.containsKey(propertyName)) {
                    List<String> types = (List<String>)context.get(propertyName);
                    for (String type : types) {
                        nodeTypeList.add(type);
                    }
                } else if (node != null && node.hasProperty(propertyName)) {
                    JCRPropertyWrapper property = node.getProperty(propertyName);
                    if (property.isMultiple()) {
                        Value[] types = property.getValues();
                        for (Value type : types) {
                            nodeTypeList.add(type.getString());
                        }
                    } else {
                        nodeTypeList.add(property.getValue().getString());
                    }
                } else if (node != null && !"j:allowedTypes".equals(propertyName) && node.hasProperty("j:allowedTypes")) {
                    Value[] types = node.getProperty("j:allowedTypes").getValues();
                    for (Value type : types) {
                        nodeTypeList.add(type.getString());
                    }
                } else if (node !=null) {
                    // No restrictions get node type list from already existing nodes
                    NodeIterator nodeIterator = node.getNodes();
                    while (nodeIterator.hasNext()) {
                        Node next = (Node) nodeIterator.next();
                        String name = next.getPrimaryNodeType().getName();
                        if (!nodeTypeList.contains(name)) {
                            nodeTypeList.add(name);
                        }
                    }
                }
                param = nextParam;
            } else if ("reference".equals(param)) {
                if (propertyName == null) {
                    propertyName = "j:node";
                }
                if (context.containsKey(propertyName)) {
                    JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
                    List<String> refNodeUuids = (List<String>)context.get(propertyName);
                    for (String refNodeUuid : refNodeUuids) {
                        try {
                            JCRNodeWrapper refNode = (JCRNodeWrapper) session.getNodeByUUID(refNodeUuid);
                            nodeTypeList.addAll(refNode.getNodeTypes());
                        } catch (Exception e) {
                            logger.warn("Referenced node not found to retrieve its nodetype for initializer", e);
                        }
                    }
                } else if (node != null && node.hasProperty(propertyName)) {
                    try {
                        JCRNodeWrapper refNode = (JCRNodeWrapper) node.getProperty(propertyName).getNode();
                        nodeTypeList.addAll(refNode.getNodeTypes());
                    } catch (ItemNotFoundException e) {
                    }
                } else if (node != null && !"j:node".equals(propertyName) && node.hasProperty("j:node")) {
                    try {
                        JCRNodeWrapper refNode = (JCRNodeWrapper) node.getProperty("j:node")
                                .getNode();
                        nodeTypeList.addAll(refNode.getNodeTypes());
                    } catch (ItemNotFoundException e) {
                    }
                }
                param = nextParam;
            } else if ("mainresource".equals(param)) {
                JCRNodeWrapper matchingParent;
                JCRNodeWrapper parent;
                if (node == null) {
                    parent = (JCRNodeWrapper) context.get("contextParent");
                    site = parent.getResolveSite();
                } else {
                    parent = node.getParent();
                }
                try {
                    while (true) {
                        if (parent.isNodeType("jnt:template")) {
                            matchingParent = parent;
                            break;
                        }
                        parent = parent.getParent();
                    }
                    if (matchingParent.hasProperty("j:applyOn")) {
                        Value[] vs = matchingParent.getProperty("j:applyOn").getValues();
                        for (Value v : vs) {
                            nodeTypeList.add(v.getString());
                        }
                    }
                } catch (ItemNotFoundException e) {
                }
                if (nodeTypeList.isEmpty()) {
                    nodeTypeList.add("jnt:page");
                }
                param = nextParam;
            } else if (param != null && param.indexOf(":") > 0) {
                nodeTypeList.add(param);
                param = nextParam;
            } else {
                if (node != null) {
                    nodeTypeList.addAll(node.getNodeTypes());
                } else if (realNodeType != null) {
                    nodeTypeList.add(realNodeType.getName());
                }
            }

            if (nodeTypeList.isEmpty()) {
                nodeTypeList.add("nt:base");
            }

            views = new TreeSet<View>();

            for (String s : nodeTypeList) {
                SortedSet<View> viewsSet = RenderService.getInstance().getViewsSet(
                        NodeTypeRegistry.getInstance().getNodeType(s), site);
                if (!viewsSet.isEmpty()) {
                    if (views.isEmpty()) {
                        views.addAll(viewsSet);
                    } else {
                        Set<View> toBeRemoved = new LinkedHashSet<View>();
                        for (View view : views) {
                            boolean found = false;
                            for (View view1 : viewsSet) {
                                if (view1.getKey().equals(view.getKey()) ||
                                        (StringUtils.isEmpty(param) && view1.getProperties().get("type") != null)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                toBeRemoved.add(view);
                            }
                        }
                        views.removeAll(toBeRemoved);
                    }
                }
            }

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        List<ChoiceListValue> vs = new ArrayList<ChoiceListValue>();
        for (View view : views) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            fillProperties(map, view.getDefaultProperties());
            fillProperties(map, view.getProperties());
            boolean isStudio = site != null && site.getPath().startsWith("/templateSets");
            if (!"false".equals(map.get("visible")) && (!"studioOnly".equals(map.get("visible")) || isStudio) &&
                    ((StringUtils.isEmpty(param) && map.get("type") == null) ||
                            param.equals(map.get("type"))) &&
                    !view.getKey().startsWith("wrapper.") && !view.getKey().contains("hidden.")
                    ) {
                JahiaResourceBundle rb = new JahiaResourceBundle(null, locale, view.getModule().getName());

                String displayName = rb.get(declaringPropertyDefinition.getResourceBundleKey() + "." + JCRContentUtils.replaceColon(view.getKey()),
                        view.getKey());
                ChoiceListValue c =  new ChoiceListValue(displayName, map, new ValueImpl(view.getKey(), PropertyType.STRING, false));
                try {

                    final File imagePath = new File(
                            view.getModule().getFilePath() + File.separator + "img" + File.separator + c.getValue().getString() + ".png");
                    if (imagePath.exists()) {
                        String s = Jahia.getContextPath();
                        if (s.equals("/")) {
                            s = "";
                        }
                        c.addProperty("image", s + (view.getModule().getRootFolderPath().startsWith("/")?"":"/")+view.getModule().getRootFolderPath() + "/img/" + c.getValue().getString() + ".png");
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }

                vs.add(c);
            }
        }
        Collections.sort(vs);
        return vs;
    }

    private void fillProperties(HashMap<String, Object> map, Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue());
        }
    }
}
