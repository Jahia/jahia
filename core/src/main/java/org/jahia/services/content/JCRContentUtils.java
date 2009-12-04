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
package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for accessing and manipulation JCR properties.
 * 
 * @author Sergiy Shyrkov
 */
public final class JCRContentUtils {
    
    private static JCRContentUtils instance;

    private static final Logger logger = Logger.getLogger(JCRContentUtils.class);

    public static String cleanUpFilename(String name) {
        name = name.replaceAll("([\\*:/\\\\<>|?\"])"," ");
        name = name.trim();
        return name;
    }

    public static String cleanUpNodeName(String nodeTypeName) {
        return nodeTypeName != null ? StringUtils.replaceChars(nodeTypeName,
                ':', '_') : nodeTypeName;
    }
    
    public static String encodeInternalName(String name) {
        name = name.replace("[", "\\5B");
        name = name.replace("]", "\\5C");
        name = name.replace("'", "\\27");
        return name;
    }

    public static String decodeInternalName(String name) {
        name = name.replace("\\5B","[");
        name = name.replace("\\5C","]");
        name = name.replace("\\27","'");
        return name;
    }

    /**
     * Returns a content object key for a node if available. Otherwise returns
     * node name.
     * 
     * @param node
     *            the node to get name
     * @return a content object key for a node if available. Otherwise returns
     *         node name
     * @throws RepositoryException
     */
    public static String getContentNodeName(Node node)
            throws RepositoryException {
        return node.getName();
    }
    
    /**
     * Returns a node path, composed using only content object keys, i.e.
     * 
     * <code>/mySite/ContentPage_1/ContentPage_19/ContentContainerList_21/ContentContainer_13</code>
     * 
     * @param node
     *            the content node to compute path for
     * @return a node path, composed using only content object keys
     * @throws RepositoryException
     */
    public static String getContentObjectPath(Node node)
            throws RepositoryException {
        
        StringBuilder path = new StringBuilder(64);
        path.append("/").append(getContentNodeName(node));
        Node parent = null;
        try {
            parent = node.getParent();
        } catch (ItemNotFoundException e) {
            parent = null;
        }
        while (parent != null) {
            String name = getContentNodeName(parent);
            path.insert(0, name);
            if (!"/".equals(name) && name.length() > 0) {
                path.insert(0, "/");
            }
            try {
                parent = parent.getParent();
            } catch (ItemNotFoundException e) {
                parent = null;
            }
        }
        
        return path.toString();
    }
    
    public static String getDisplayLabel(String nodeName, ProcessingContext ctx) {
        return JahiaResourceBundle.getJahiaInternalResource(
                "org.jahia.services.jcr.types." + cleanUpNodeName(nodeName),
                ctx.getLocale(), nodeName);
    }
    
    public static JCRContentUtils getInstance() {
        return instance;
    }

    private static ExtendedPropertyDefinition getPropertyDefExtension(
            PropertyDefinition propDef) {
        try {
            return NodeTypeRegistry.getInstance().getNodeType(
                    propDef.getDeclaringNodeType().getName()).getPropertyDefinition(
                    propDef.getName());
        } catch (NoSuchNodeTypeException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static PropertyDefinition getPropertyDefinition(NodeType type,
            String property) throws RepositoryException {
        PropertyDefinition foundDefintion = null;
        PropertyDefinition[] pds = type.getDeclaredPropertyDefinitions();
        for (int i = 0; i < pds.length; i++) {
            PropertyDefinition pd = pds[i];
            if (pd.getName().equals(property)) {
                foundDefintion = pd;
                break;
            }
        }

        return foundDefintion;
    }

    public static PropertyDefinition getPropertyDefinition(String nodeType,
            String property) throws RepositoryException {
        return getPropertyDefinition(NodeTypeRegistry.getInstance().getNodeType(
                        nodeType), property);
    }

    public static int getPropertyDefSelector(ItemDefinition itemDef) {
        ExtendedPropertyDefinition propDefExtension = null;
        if (itemDef instanceof PropertyDefinition) {
            propDefExtension = getPropertyDefExtension((PropertyDefinition) itemDef);
        }

        return propDefExtension != null ? propDefExtension.getSelector() : 0;
    }

    public static boolean isValidFilename(String name) {
        return (!name.startsWith(" ") && !name.endsWith(" ") && name.matches("([^\\*:/\\\\<>|?\"])*"));
    }

    /**
     * Convert a string to a JCR search expression literal, suitable for use in
     * jcr:contains() (inside XPath) or contains (SQL2). The characters - and " have
     * special meaning, and may be escaped with a backslash to obtain their
     * literal value. See JSR-283 spec v2.0, Sec. 4.6.6.19.
     * 
     * @param str
     *            Any string.
     * @return A valid string literal suitable for use in
     *         JCR contains clauses, including enclosing quotes.
     */
    public static String stringToJCRSearchExp(String str) {
        // Escape ' and \ everywhere, preceding them with \ except when \
        // appears
        // in one of the combinations \" or \-
        return stringToQueryLiteral(str.replaceAll("\\\\(?![-\"])", "\\\\\\\\")
                .replaceAll("'", "\\\\'").replaceAll("\"", "\\\\\""));
    }

    /**
     * Convert a string to a literal, suitable for inclusion
     * in a query. See JSR-283 spec v2.0, Sec. 4.6.6.19.
     * 
     * @param str
     *            Any string.
     * @return A valid JCR query string literal, including enclosing quotes.
     */
    public static String stringToQueryLiteral(String str) {
        // Single quotes needed for jcr:contains()
        return "'" + str.replaceAll("'", "''") + "'";
    }
    
    private Map<String, List<String>> mimeTypes;

    /**
     * Initializes an instance of this class.
     * 
     * @param mimeTypes
     *            a map with mime type mappings
     */
    public JCRContentUtils(Map<String, List<String>> mimeTypes) {
        super();
        instance = this;
        this.mimeTypes = mimeTypes;
    }

    /**
     * Return a map of mime types (file formats) to be available in the
     * advanced search form.
     * 
     * @return a map of mime types (file formats) to be available in the
     *         advanced search form
     */
    public Map<String, List<String>> getMimeTypes() {
        return mimeTypes;
    }

    public static boolean hasPermission(final JahiaUser user, final String role, final String nodeUUID) {
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper node = session.getNodeByUUID(nodeUUID);
                    Map<String, List<String[]>> aclEntriesMap = node.getAclEntries();

                    Set<Map.Entry<String,List<String[]>>> principalSet = aclEntriesMap.entrySet();
                    for (Map.Entry<String,List<String[]>> currentPrincipal : principalSet) {
                        boolean isUser = currentPrincipal.getKey().indexOf("u:") == 0;
                        String principalName = currentPrincipal.getKey().substring(2);

                        // test if the principal is the user or if the user belongs to the principal (group)
                        if ((isUser && principalName.equalsIgnoreCase(user.getUsername())) || user.isMemberOfGroup(Jahia.getThreadParamBean().getSiteID(), principalName)) {
                            List<String[]> principalPermValues = currentPrincipal.getValue();
                            for (String[] currentPrincipalPerm : principalPermValues) {
                                String currentPrincipalPermValue = currentPrincipalPerm[1];
                                String currentPrincipalPermName = currentPrincipalPerm[2];
                                if (currentPrincipalPermName != null && currentPrincipalPermName.equalsIgnoreCase(role)) {
                                    if (currentPrincipalPermValue != null && currentPrincipalPermValue.equalsIgnoreCase("GRANT")) {
                                        return true;
                                    }
                                }
                            }
                        }

                    }
                    return false;
                }
            });
        } catch (Exception e) {
            logger.error(e,e);
            return false;
        }
    }

    /**
     * Get the node or property display name depending on the locale
     *
     * @param item the item to get the label for
     * @param locale current locale
     * @return the node or property display name depending on the locale
     */
    public static String getDisplayLabel(Object item, Locale locale) {
        if (item != null) {

            try {
                // case of property
                if (item instanceof Property) {
                    Property property = (Property) item;
                    PropertyDefinition propertyDefintion = property.getDefinition();
                    if (propertyDefintion != null && propertyDefintion instanceof ExtendedPropertyDefinition) {
                        ExtendedPropertyDefinition itemDef = (ExtendedPropertyDefinition) propertyDefintion;
                        return itemDef.getLabel(locale);
                    } else {
                        logger.error("PropertyDefinition doesn't implement 'org.jahia.services.content.nodetypes.ExtendedPropertyDefinition'");
                    }
                }
                // case of PropertyDefinition
                else if (item instanceof PropertyDefinition) {
                    if (item instanceof ExtendedPropertyDefinition) {
                        ExtendedPropertyDefinition itemDef = (ExtendedPropertyDefinition) item;
                        return itemDef.getLabel(locale);
                    } else {
                        logger.error("PropertyDefinition doesn't implement 'org.jahia.services.content.nodetypes.ExtendedPropertyDefinition'");
                    }
                }
                // case of node type
                else if (item instanceof NodeType) {
                    NodeType nodeType = (NodeType) item;
                    if (nodeType instanceof ExtendedNodeType) {
                        ExtendedNodeType extendNodeType = (ExtendedNodeType) nodeType;
                        return extendNodeType.getLabel(locale);
                    } else {
                        logger.error("nodeType doesn't implement 'org.jahia.services.content.nodetypes.ExtendedNodeType'");
                    }
                } else {
                    logger.error("Object must be a 'javax.jcr.Property' or 'javax.jcr.nodetype.NodeType'");
                }
            } catch (RepositoryException e) {
                logger.error(e, e);
            }
        }
        return null;
    }

    /**
     * Returns the next available name for a node, appending if needed numbers.
     * 
     * @param dest the destination node, where the new one will be created
     * @param name the name of the new node
     * @param session current JCR session
     * @return the next available name for a node, appending if needed numbers
     */
    public static String findAvailableNodeName(JCRNodeWrapper dest, String name, JCRSessionWrapper session) {
        int i = 1;

        String basename = name;
        int dot = basename.lastIndexOf('.');
        String ext = "";
        if (dot > 0) {
            ext = basename.substring(dot);
            basename = basename.substring(0, dot);
        }
        int und = basename.lastIndexOf('_');
        if (und > -1 && basename.substring(und + 1).matches("[0-9]+")) {
            basename = basename.substring(0, und);
        }

        do {
            try {
                session.getNode(dest.getPath() + "/" + name);
                name = basename + "_" + (i++) + ext;
            } catch (RepositoryException e) {
                break;
            }

        } while (true);

        return name;
    }

}
