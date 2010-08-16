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

package org.jahia.services.content.nodetypes.initializers;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Template;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.*;

/**
 * Choice list initializer ro provide a selection of available templates.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 17 nov. 2009
 */
public class TemplatesChoiceListInitializerImpl implements ChoiceListInitializer {
    private transient static Logger logger = Logger.getLogger(TemplatesChoiceListInitializerImpl.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition declaringPropertyDefinition, String param,
                                                     List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        if (context == null) {
            return new ArrayList<ChoiceListValue>();
        }

        JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
        ExtendedNodeType realNodeType = (ExtendedNodeType) context.get("contextType");

        SortedSet<Template> templates = new TreeSet<Template>();

        try {
            final List<String> nodeTypeList = new ArrayList<String>();
            String nextParam = "";
            if (param.contains(",")) {
                nextParam = StringUtils.substringAfter(param, ",");
                param =  StringUtils.substringBefore(param, ",");
            }
            if ("subnodes".equals(param)) {
                if (node != null && node.hasProperty("j:allowedTypes")) {
                    Value[] types = node.getProperty("j:allowedTypes").getValues();
                    for (Value type : types) {
                        nodeTypeList.add(type.getString());
                    }
                }
                param = nextParam;
            } else if ("reference".equals(param)) {
                if (node != null && node.hasProperty("j:node")) {
                    try {
                        JCRNodeWrapper refnode = (JCRNodeWrapper) node.getProperty("j:node").getNode();
                        nodeTypeList.addAll(refnode.getNodeTypes());
                    } catch (ItemNotFoundException e) {
                    }
                }
                param = nextParam;
            } else if ("mainresource".equals(param)) {
                JCRNodeWrapper matchingParent;
                JCRNodeWrapper parent;
                if (node == null) {
                    parent = (JCRNodeWrapper) context.get("contextParent");
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

            templates = new TreeSet<Template>();

            for (String s : nodeTypeList) {
                templates.addAll(RenderService.getInstance().getTemplatesSet(
                        NodeTypeRegistry.getInstance().getNodeType(s)));
            }

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        List<ChoiceListValue> vs = new ArrayList<ChoiceListValue>();
        for (Template template : templates) {
            if (!"false".equals(template.getProperties().getProperty("visible")) &&
                    ((StringUtils.isEmpty(param) && template.getProperties().get("type") == null) ||
                            param.equals(template.getProperties().get("type"))) &&
                    !template.getKey().startsWith("wrapper.") && !template.getKey().contains("hidden.")
//                    && (site == null || !"siteLayout".equals(template.getModule().getModuleType()) ||
//                            template.getModule().getName().equals(site.getTemplatePackageName()))
                    ) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                Properties properties = template.getProperties();
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    map.put(entry.getKey().toString(), entry.getValue());
                }
                vs.add(new ChoiceListValue(template.getKey(), map,
                        new ValueImpl(template.getKey(), PropertyType.STRING, false)));
            }
        }
        return vs;
    }
}
