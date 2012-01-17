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

package org.jahia.taglibs.jcr.node;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.render.RenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.templates.ComponentRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import java.util.*;

/**
 * JCR content related utilities.
 * User: jahia
 * Date: 28 mai 2009
 * Time: 15:46:07
 */
public class JCRTagUtils {
    
    private static final transient Logger logger = LoggerFactory.getLogger(JCRTagUtils.class);

    /**
     * Get the node or property display name depending on the locale
     *
     * @param nodeObject the item to get the label for
     * @param locale current locale
     * @return the node or property display name depending on the locale
     */
    public static String label(Object nodeObject, Locale locale) {
        return JCRContentUtils.getDisplayLabel(nodeObject, locale, null);
    }

    /**
     * Get the label value depending on the locale
     *
     * @param nodeObject
     * @param locale as a string
     * @return
     */
    public static String label(Object nodeObject, String locale) {
        return label(nodeObject, LanguageCodeConverters.languageCodeToLocale(locale));
    }

    public static String label(ExtendedPropertyDefinition propertyDefinition, String locale, ExtendedNodeType nodeType) {
        return JCRContentUtils.getDisplayLabel(propertyDefinition, LanguageCodeConverters.languageCodeToLocale(locale),nodeType);
    }

    /**
     * Returns <code>true</code> if the current node has the specified type or at least one of the specified node types.
     * 
     * @param node current node to check the type
     * @param type the node type name to match or a comma-separated list of node
     *            types (at least one should be matched)
     * @return <code>true</code> if the current node has the specified type or at least one of the specified node types
     */
    public static boolean isNodeType(JCRNodeWrapper node, String type) {
        if (node == null) {
            throw new IllegalArgumentException("The specified node is null");
        }
        
        boolean hasType = false;
        try {
            if (type.contains(",")) {
                for (String typeToCheck : StringUtils.split(type, ',')) {
                    if (node.isNodeType(typeToCheck.trim())) {
                        hasType = true;
                        break;
                    }
                }
            } else {
                hasType = node.isNodeType(type);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        
        return hasType;
    }

    public static List<JCRNodeWrapper> getNodes(JCRNodeWrapper node, String type) {
        return JCRContentUtils.getNodes(node, type);
    }

    /**
     * Returns <code>true</code> if the current node has at least one child node
     * of the specified type.
     * 
     * @param node current node whose children will be queried
     * @param type the node type name to match or a comma-separated list of node
     *            types (at least one should be matched)
     * @return <code>true</code> if the current node has at least one child node
     *         of the specified type
     */
    public static boolean hasChildrenOfType(JCRNodeWrapper node, String type) {
        boolean hasChildrenOfType = false;
        String[] typesToCheck = StringUtils.split(type, ',');
        try {
            for (NodeIterator iterator = node.getNodes(); iterator.hasNext() && !hasChildrenOfType;) {
                Node child = iterator.nextNode();
                for (String matchType : typesToCheck) {
                    if (child.isNodeType(matchType)) {
                        hasChildrenOfType = true;
                        break;
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
        return hasChildrenOfType;
    }

    /**
     * Returns an iterator with the child nodes of the current node, which match
     * the specified node type name. This is an advanced version of the
     * {@link #getNodes(JCRNodeWrapper, String)} method to handle multiple node
     * types.
     * 
     * @param node current node whose children will be queried
     * @param type the node type name to match or a comma-separated list of node
     *            types (at least one should be matched)
     * @return an iterator with the child nodes of the current node, which match
     *         the specified node type name
     */
    public static List<JCRNodeWrapper> getChildrenOfType(JCRNodeWrapper node, String type) {
        return JCRContentUtils.getChildrenOfType(node, type);
    }

    /**
     * Returns an iterator with the descendant nodes of the current node, which match
     * the specified node type name.
     * 
     * @param node current node whose descendants will be queried
     * @param type the node type name to match
     * @return an iterator with the descendant nodes of the current node, which match
     *         the specified node type name
     */
    public static NodeIterator getDescendantNodes(JCRNodeWrapper node, String type) {
        return JCRContentUtils.getDescendantNodes(node, type);
    }

    public static Map<String, String> getPropertiesAsStringFromNodeNameOfThatType(JCRNodeWrapper nodeContainingProperties,JCRNodeWrapper nodeContainingNodeNames, String type) {
        List<JCRNodeWrapper> nodeNames = getNodes(nodeContainingNodeNames,type);
        Map<String, String> props = new LinkedHashMap<String, String>();
        for (JCRNodeWrapper nodeWrapper : nodeNames) {
            final String name = nodeWrapper.getName();
            try {
                JCRPropertyWrapper property = nodeContainingProperties.getProperty(name);
                String value;
                if(property.isMultiple()) {
                    value = property.getValues()[0].getString();
                } else {
                value = property.getValue().getString();
                }
                props.put(name,value);
            } catch (PathNotFoundException e) {
                logger.debug(e.getMessage(), e);
            } catch (ValueFormatException e) {
                logger.error(e.getMessage(), e);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return props;
    }

    /**
     * Returns all the parents of the current node that have the specified node type. If no matching node is found, an
     * empty list.
     * 
     * @param node
     *            the current node to start the lookup from
     * @param type
     *            the required type of the parent node(s)
     * @return the parents of the current node that have the specified node type. If no matching node is found, an
     *         empty list is returned
     */
    public static List<JCRNodeWrapper> getParentsOfType(JCRNodeWrapper node,
            String type) {
        
        List<JCRNodeWrapper> parents = new ArrayList<JCRNodeWrapper>();
        do {
            node = getParentOfType(node, type);
            if (node != null) {
                parents.add(node);
            }
        } while (node != null);

        return parents;
    }    
    
    /**
     * Returns the first parent of the current node that has the specified node type. If no matching node is found, <code>null</code> is
     * returned.
     * 
     * @param node
     *            the current node to start the lookup from
     * @param type
     *            the required type of the parent node
     * @return the first parent of the current node that has the specified node type. If no matching node is found, <code>null</code> is
     *         returned
     */
    public static JCRNodeWrapper getParentOfType(JCRNodeWrapper node,
            String type) {
        JCRNodeWrapper matchingParent = null;
        try {
            JCRNodeWrapper parent = node.getParent();
            while (parent != null) {
                if (parent.isNodeType(type)) {
                    matchingParent = parent;
                    break;
                }
                parent = parent.getParent();
            }
        } catch (ItemNotFoundException e) {
            // we reached the hierarchy top
        } catch (RepositoryException e) {
            logger.error("Error while retrieving nodes parent node. Cause: "
                    + e.getMessage(), e);
        }
        return matchingParent;
    }

    public static Map<String, JahiaGroup> getUserMembership(JCRNodeWrapper user) {
        Map<String, JahiaGroup> map = new LinkedHashMap<String, JahiaGroup>();
        final JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(
                user.getName());
        final JahiaGroupManagerService managerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        final List<String> userMembership = managerService.getUserMembership(
                jahiaUser);
        for (String groupName : userMembership) {
            final JahiaGroup group = managerService.lookupGroup(groupName);
            if(!groupName.equals(JahiaGroupManagerService.GUEST_GROUPNAME) &&
               !groupName.equals(JahiaGroupManagerService.USERS_GROUPNAME)) {
                map.put(groupName,group);
            }
        }
        return map;
    }

    public static boolean hasPermission(JCRNodeWrapper node,String permission) {
        return node.hasPermission(permission);
    }

    public static String humanReadableFileLength(JCRNodeWrapper node) {
        return FileUtils.byteCountToDisplaySize(node.getFileContent().getContentLength());
    }

    /**
     * Returns all the parents of the current node that have the specified node type. If no matching node is found, an
     * empty list.
     *
     * @param node
     *            the current node to start the lookup from
     * @param type
     *            the required type of the parent node(s)
     * @return the parents of the current node that have the specified node type. If no matching node is found, an
     *         empty list is returned
     */
    public static List<JCRNodeWrapper> getMeAndParentsOfType(JCRNodeWrapper node,String type) {

        List<JCRNodeWrapper> parents = new ArrayList<JCRNodeWrapper>();
        try {
            if(node.isNodeType(type)) {
                parents.add(node);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        do {
            node = getParentOfType(node, type);
            if (node != null) {
                parents.add(node);
            }
        } while (node != null);

        return parents;
    }

    public static boolean hasOrderableChildNodes(JCRNodeWrapper node) {
        try {
            return node.getPrimaryNodeType().hasOrderableChildNodes();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public static String getConstraints(JCRNodeWrapper node) {
        try {
            return ConstraintsHelper.getConstraints(node).replace(" ", ",");
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }

    /**
     * @see org.apache.jackrabbit.util.Text#escapeIllegalJcrChars(String)
     */    
    public static String escapeIllegalJcrChars(String inputString) {
        return Text.escapeIllegalJcrChars(inputString);
    }
    
    /**
     * Returns the full user name, including first and last name. If those are
     * not available, returns the username.
     * 
     * @param userNode the user JCR node
     * @return the full user name, including first and last name. If those are
     *         not available, returns the username
     */
    public static String userFullName(JCRNodeWrapper userNode) {
        StringBuilder name = new StringBuilder();
        String value = userNode.getPropertyAsString("j:firstName");
        if (StringUtils.isNotEmpty(value)) {
            name.append(value);
        }
        value = userNode.getPropertyAsString("j:lastName");
        if (StringUtils.isNotEmpty(value)) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(value);
        }

        if (name.length() == 0) {
            name.append(PrincipalViewHelper.getUserDisplayName(userNode.getName()));
        }

        return name.toString();
    }


    public static List<ExtendedNodeType> getContributeTypes(JCRNodeWrapper node, JCRNodeWrapper areaNode, Value[] typelistValues) throws Exception {
        List<ExtendedNodeType> types = new ArrayList<ExtendedNodeType>();

        List<String> typeList = getContributeTypesAsString(node, areaNode, typelistValues);

        if (!typeList.isEmpty()) {
            List<JCRNodeWrapper> components = new ArrayList<JCRNodeWrapper>();
            components.add(node.getResolveSite().getNode("components"));
            for (int i = 0; i < components.size(); i++) {
                JCRNodeWrapper n = components.get(i);
                if (n.isNodeType("jnt:componentFolder")) {
                    NodeIterator nodeIterator = n.getNodes();
                    while (nodeIterator.hasNext()) {
                        JCRNodeWrapper next = (JCRNodeWrapper) nodeIterator.next();
                        components.add(next);
                    }
                } else if (n.isNodeType("jnt:simpleComponent") && n.hasPermission("useComponent")) {
                    ExtendedNodeType t = NodeTypeRegistry.getInstance().getNodeType(n.getName());
                    for (String s : typeList) {
                        if (t.isNodeType(s)) {
                            types.add(t);
                            break;
                        }
                    }
                }
            }
        }
        
        String[] constraints = ConstraintsHelper.getConstraints(node).split(" ");
        List<ExtendedNodeType> finaltypes = new ArrayList<ExtendedNodeType>();
        for (ExtendedNodeType type : types) {
            for (String s : constraints) {
                if (!finaltypes.contains(type) && type.isNodeType(s)) {
                    finaltypes.add(type);
                }
            }
        }
        return finaltypes;
    }

    public static Map<String, String> getContributeTypesDisplay(final JCRNodeWrapper node,
            JCRNodeWrapper areaNode, Value[] typelistValues, Locale displayLocale) throws Exception {
        if (node == null) {
            return Collections.emptyMap();
        }

        List<String> typeList = getContributeTypesAsString(node, areaNode, typelistValues);

        return ComponentRegistry.getComponentTypes(node, typeList, null, displayLocale);
    }

    private static List<String> getContributeTypesAsString(JCRNodeWrapper node, JCRNodeWrapper areaNode, Value[] typelistValues) throws RepositoryException {
        if ((typelistValues == null || typelistValues.length == 0) && !node.isNodeType("jnt:contentList") && !node.isNodeType("jnt:contentFolder")) {
            return Arrays.asList(ConstraintsHelper.getConstraints(node).split(" "));
        }
        if (typelistValues == null && node.hasProperty("j:contributeTypes")) {
            typelistValues = node.getProperty("j:contributeTypes").getValues();
        }
        if (typelistValues == null && areaNode != null && areaNode.hasProperty("j:contributeTypes")) {
            typelistValues = areaNode.getProperty("j:contributeTypes").getValues();
        }

        if (typelistValues == null) {
            return Collections.emptyList();
        }

        List<String> typeList = new LinkedList<String>();
        for (Value value : typelistValues) {
            typeList.add(value.getString());
        }
        return typeList;
    }

    public static JCRNodeWrapper findDisplayableNode(JCRNodeWrapper node, RenderContext context) {
        return JCRContentUtils.findDisplayableNode(node, context);
    }

    public static boolean isAllowedChildNodeType(JCRNodeWrapper node, String nodeType) throws RepositoryException {
        try {
             node.getApplicableChildNodeDefinition("*", nodeType);
             return true;
        } catch (ConstraintViolationException e) {
            return false;
        }
    }

    public static List<JCRNodeWrapper> findAllowedNodesForPermission(String permission, JCRNodeWrapper parentNode,
                                                                     String nodeType) {
        final List<JCRNodeWrapper> results = new LinkedList<JCRNodeWrapper>();
        try {
            JCRSessionWrapper session = parentNode.getSession();
            Query groupQuery = session.getWorkspace().getQueryManager().createQuery(
                    "select * from [jnt:acl] as u where isdescendantnode(u,'" + parentNode.getPath() + "')",
                    Query.JCR_SQL2);
            QueryResult groupQueryResult = groupQuery.execute();
            final NodeIterator nodeIterator = groupQueryResult.getNodes();
            while (nodeIterator.hasNext()) {
                JCRNodeWrapper node = (JCRNodeWrapper) nodeIterator.next();
                JCRNodeWrapper contentNode = node.getParent();
                if (contentNode.isNodeType(nodeType) && hasPermission(contentNode, permission)) {
                    results.add(contentNode);
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return results;
    }
}
