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
package org.jahia.taglibs.jcr.node;

import java.util.*;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.templates.ComponentRegistry;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @param nodeObject the node object
     * @param locale as a string
     * @return the label value depending on the locale
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
        return JCRContentUtils.getParentOfType(node, type);
    }

    public static boolean hasPermission(JCRNodeWrapper node,String permission) {
        return node != null && node.hasPermission(permission);
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
            return Patterns.SPACE.matcher(ConstraintsHelper.getConstraints(node)).replaceAll(",");
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }

    /**
     * @param inputString the string to escape
     * @return the escaped string
     * @see org.apache.jackrabbit.util.Text#escapeIllegalJcrChars(String)
     */
    public static String escapeIllegalJcrChars(String inputString) {
        return Text.escapeIllegalJcrChars(inputString);
    }

    /**
     * @deprecated
     */
    @Deprecated
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
                } else if (n.isNodeType("jnt:simpleComponent") && n.hasPermission("useComponentForCreate")) {
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

        String[] constraints = Patterns.SPACE.split(ConstraintsHelper.getConstraints(node));
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
        if (typeList == null) { // there is type restriction defined and none is allowed in contribute mode
            return Collections.emptyMap();
        }

        return ComponentRegistry.getComponentTypes(node, typeList, null, displayLocale);
    }

    private static List<String> getContributeTypesAsString(JCRNodeWrapper node, JCRNodeWrapper areaNode, Value[] typelistValues) throws RepositoryException {
        if ((typelistValues == null || typelistValues.length == 0) && !node.isNodeType("jnt:contentList") && !node.isNodeType("jnt:contentFolder")) {
            return Arrays.asList(Patterns.SPACE.split(ConstraintsHelper.getConstraints(node)));
        }
        if (typelistValues == null && node.hasProperty(Constants.JAHIA_CONTRIBUTE_TYPES)) {
            typelistValues = node.getProperty(Constants.JAHIA_CONTRIBUTE_TYPES).getValues();
        }
        if (typelistValues == null && areaNode != null && areaNode.hasProperty(Constants.JAHIA_CONTRIBUTE_TYPES)) {
            typelistValues = areaNode.getProperty(Constants.JAHIA_CONTRIBUTE_TYPES).getValues();
        }

        if (typelistValues == null) {
            return Collections.emptyList();
        }

        Value[] allowedTypeValues = null;
        if (node.hasProperty("j:allowedTypes")) {
            allowedTypeValues = node.getProperty("j:allowedTypes").getValues();
        }
        if (allowedTypeValues == null && areaNode != null && areaNode.hasProperty("j:allowedTypes")) {
            allowedTypeValues = areaNode.getProperty("j:allowedTypes").getValues();
        }
        Set<String> allowedTypes = allowedTypeValues == null ? Collections.<String>emptySet() : new HashSet<String>(allowedTypeValues.length);
        if (allowedTypeValues != null) {
            for (Value value : allowedTypeValues) {
                allowedTypes.add(value.getString());
            }
        }

        List<String> typeList = new LinkedList<String>();
        for (Value value : typelistValues) {
            String type = value.getString();
            if (allowedTypes.isEmpty() || allowedTypes.contains(type)
                    || isAllowedSubnodeType(type, allowedTypes)) {
                typeList.add(type);
            }
        }
        return !allowedTypes.isEmpty() && typeList.isEmpty() ? null : typeList;
    }

    private static boolean isAllowedSubnodeType(String nodeType, Set<String> allowedTypes) {
        boolean isAllowed = false;
        try {
            ExtendedNodeType t = NodeTypeRegistry.getInstance().getNodeType(nodeType);
            for (String allowedType : allowedTypes) {
                if (t.isNodeType(allowedType)) {
                    isAllowed = true;
                    break;
                }
            }
        } catch (RepositoryException e) {
            logger.warn("Nodetype " + nodeType + " not found while checking for allowed node types!", nodeType);
        }

        return isAllowed;
    }

    public static JCRNodeWrapper findDisplayableNode(JCRNodeWrapper node, RenderContext context) {
        return JCRContentUtils.findDisplayableNode(node, context);
    }

    public static JCRNodeWrapper findDisplayableNodeInSite(JCRNodeWrapper node, RenderContext context,JCRSiteNode siteNode) {
        return JCRContentUtils.findDisplayableNode(node, context, siteNode);
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
                    "select * from ["+ nodeType + "] as u where isdescendantnode(u,'" + JCRContentUtils.sqlEncode(parentNode.getPath()) + "')",
                            Query.JCR_SQL2);
            QueryResult groupQueryResult = groupQuery.execute();
            final NodeIterator nodeIterator = groupQueryResult.getNodes();
            while (nodeIterator.hasNext()) {
                JCRNodeWrapper node = (JCRNodeWrapper) nodeIterator.next();
                if (hasPermission(node, permission)) {
                    results.add(node);
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return results;
    }

    public static JCRNodeWrapper getFirstAllowedNodeForPermission(String permission, JCRNodeWrapper parentNode,
                                                                  String nodeType) {
        try {
            JCRSessionWrapper session = parentNode.getSession();
            Query groupQuery = session.getWorkspace().getQueryManager().createQuery(
                    "select * from ["+ nodeType + "] as u where isdescendantnode(u,'" + JCRContentUtils.sqlEncode(parentNode.getPath()) + "')",
                            Query.JCR_SQL2);
            QueryResult groupQueryResult = groupQuery.execute();
            final NodeIterator nodeIterator = groupQueryResult.getNodes();
            while (nodeIterator.hasNext()) {
                JCRNodeWrapper node = (JCRNodeWrapper) nodeIterator.next();
                if (hasPermission(node, permission)) {
                    return node;
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns a string with comma-separated keywords, found on the current node (or the parent one, if inheritance is considered), or an
     * empty string if no keywords are present.
     *
     * @param node
     *            the node to retrieve keywords from
     * @param considerInherted
     *            if set to <code>true</code> the keywords are also looked up to the parent nodes, if not found on the current one
     * @return a string with comma-separated keywords, found on the current node (or the parent one, if inheritance is considered), or an
     *         empty string if no keywords are present
     */
    public static String getKeywords(JCRNodeWrapper node, boolean considerInherted) {
        if (node == null) {
            return StringUtils.EMPTY;
        }
        String keywords = null;
        try {
            JCRNodeWrapper current = node;
            while (current != null) {
                JCRPropertyWrapper property = current.hasProperty("j:keywords") ? current
                        .getProperty("j:keywords") : null;

                if (property != null) {
                    if (property.getDefinition().isMultiple()) {
                        StringBuilder buff = new StringBuilder(64);
                        for (Value val : property.getValues()) {
                            String keyword = val.getString();
                            if (StringUtils.isNotEmpty(keyword)) {
                                if (buff.length() > 0) {
                                    buff.append(", ");
                                }
                                buff.append(keyword);
                            }
                        }
                        keywords = buff.toString();
                    } else {
                        keywords = property.getString();
                    }
                    break;
                } else if (considerInherted && !"/".equals(current.getPath())) {
                    current = current.getParent();
                } else {
                    break;
                }
            }
        } catch (RepositoryException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            } else {
                logger.warn("Unable to get keyworkds for node " + node.getPath() + ". Cause: "
                        + e.getMessage());
            }
        }

        return StringUtils.defaultString(keywords);
    }

    public static boolean needPublication(JCRNodeWrapper node, String language, boolean includesReferences,
                                                     boolean includesSubnodes, boolean allsubtree) {
        if (node != null) {
            JCRPublicationService publicationService = JCRPublicationService.getInstance();
            if (publicationService != null) {
                try {
                    List<PublicationInfo> publicationInfos = publicationService.getPublicationInfo(node.getIdentifier(),
                            (StringUtils.isEmpty(language) ? null : Collections.singleton(language)), includesReferences, includesSubnodes,
                            allsubtree, node.getSession().getWorkspace().getName(), Constants.LIVE_WORKSPACE);
                    for (PublicationInfo publicationInfo : publicationInfos) {
                        if (publicationInfo.needPublication(StringUtils.isEmpty(language) ? null : language)) {
                            return true;
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error("Failed to get PublicationInfo for node " + node.getPath(), e);
                }
            }
        }
        return false;
    }
}
