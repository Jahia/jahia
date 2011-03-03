/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.bin;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.web.bind.ServletRequestUtils;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.servlet.http.HttpServletRequest;

/**
 * Base action
 * 
 * @author Sergiy Shyrkov
 */
public abstract class Action {

    public abstract ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                           JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception;
    /**
     * Returns a single value for the specified parameter. If the parameter is
     * not present or its value is empty, returns <code>null</code>.
     * 
     * @param parameters the map of action parameters
     * @param paramName the name of the parameter in question
     * @return a single value for the specified parameter. If the parameter is
     *         not present or its value is empty, returns <code>null</code>
     */
    public static String getParameter(Map<String, List<String>> parameters, String paramName) {
        return getParameter(parameters, paramName, null);
    }

    /**
     * Returns a single value for the specified parameter. If the parameter is
     * not present or its value is empty, returns the provided default value.
     * 
     * @param parameters the map of action parameters
     * @param paramName the name of the parameter in question
     * @param defaultValue the default value to be used if the parameter is not
     *            present or its value is empty
     * @return a single value for the specified parameter. If the parameter is
     *         not present or its value is empty, returns the provided default
     *         value
     */
    public static String getParameter(Map<String, List<String>> parameters, String paramName, String defaultValue) {
        List<String> vals = parameters.get(paramName);
        return CollectionUtils.isNotEmpty(vals) && StringUtils.isNotEmpty(vals.get(0)) ? vals.get(0) : defaultValue;
    }

    private String name;

    private String requiredPermission = null;

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.bin.Action#getName()
     */
    public String getName() {
        return name != null ? name : StringUtils.uncapitalize(StringUtils.substringBeforeLast(getClass().getSimpleName(), "Action"));
    }

    /**
     * Sets the action name.
     * 
     * @param name the action name
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }


    protected JCRNodeWrapper createNode(HttpServletRequest req, Map<String, List<String>> parameters,
                                        JCRNodeWrapper node, String nodeType, String nodeName)
            throws RepositoryException {
        JCRNodeWrapper newNode;
        if (StringUtils.isBlank(nodeName)) {
            String nodeNameProperty = "jcr:title";
            if (parameters.get(Render.NODE_NAME_PROPERTY) != null) {
                nodeNameProperty = parameters.get(Render.NODE_NAME_PROPERTY).get(0);
            }
            if (parameters.get(nodeNameProperty) != null) {
                nodeName = JCRContentUtils.generateNodeName(parameters.get(nodeNameProperty).get(0), 32);
            } else {
                nodeName = nodeType.substring(nodeType.lastIndexOf(":") + 1);
            }
            nodeName = JCRContentUtils.findAvailableNodeName(node, nodeName);
        }
        if (ServletRequestUtils.getBooleanParameter(req, Render.NORMALIZE_NODE_NAME, false)) {
            nodeName = JCRContentUtils.generateNodeName(nodeName, 255);
        }
        try {
            newNode = node.getNode(nodeName);
            if (!newNode.isCheckedOut()) {
                newNode.checkout();
            }
        } catch (PathNotFoundException e) {
            if (!node.isCheckedOut()) {
                node.checkout();
            }
            newNode = node.addNode(nodeName, nodeType);
        }

//        String template = parameters.containsKey("j:sourceTemplate") ? parameters.get("j:sourceTemplate").get(0) : null;
//        if (Constants.JAHIANT_PAGE.equals(nodeType) && template != null) {
//            // we will use the provided template
//            JCRNodeWrapper templateNode = null;
//            try {
//                templateNode = session.getNodeByIdentifier(template);
//                templateNode.copy(newNode.getParent(), nodeName, true);
//            } catch (RepositoryException e) {
//                logger.warn("Unable to use template node '" + template + ". Skip using template for new page.", e);
//            }
//
//        }

        if (parameters.containsKey(Constants.JCR_MIXINTYPES)) {
            for (Object o : ((ArrayList) parameters.get(Constants.JCR_MIXINTYPES))) {
                String mixin = (String) o;
                newNode.addMixin(mixin);
            }
        }
        Set<Map.Entry<String, List<String>>> set = parameters.entrySet();
        for (Map.Entry<String, List<String>> entry : set) {
            String key = entry.getKey();
            if (!Render.reservedParameters.contains(key)) {
                List<String> values = entry.getValue();
                ExtendedPropertyDefinition propertyDefinition = null;
                propertyDefinition = newNode.getApplicablePropertyDefinition(key);
                if (propertyDefinition == null) {
                    continue;
                }
                if (propertyDefinition.isMultiple()) {
                    newNode.setProperty(key, values.toArray(new String[values.size()]));
                } else if (values.get(0).length() > 0) {
                    if (propertyDefinition.getRequiredType() == ExtendedPropertyType.DATE) {
                        DateTime dateTime = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(values.get(0));
                        newNode.setProperty(key, dateTime.toCalendar(Locale.ENGLISH));
                    } else {
                        newNode.setProperty(key, values.get(0));
                    }
                }
            }
        }

        return newNode;
    }

}
