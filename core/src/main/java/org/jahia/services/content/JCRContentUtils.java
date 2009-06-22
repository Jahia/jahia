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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.content.impl.jahia.JahiaContentNodeImpl;
import org.jahia.services.content.impl.jahia.JahiaRootNodeImpl;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;

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
        String name = null;
        if (node instanceof JahiaContentNodeImpl) {
            name = ((JahiaContentNodeImpl) node).getContentObject()
                    .getObjectKey().getKey();
        } else if (node instanceof JCRJahiaContentNode) {
            name = ((JCRJahiaContentNode) node).getContentObject()
                    .getObjectKey().getKey();
        } else {
            name = node.getName();
        }
        return name;
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
            if (parent instanceof JahiaRootNodeImpl
                    || (parent instanceof JCRNodeDecorator && ((JCRNodeDecorator) parent)
                            .getRealNode() instanceof JahiaRootNodeImpl)) {
                parent = null; // stop
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

    /**
     * Returns the node type for the specified content object.
     * 
     * @param contentObjectKey
     *            the content object key
     * @return the node type for the specified content object
     */
    public static ExtendedNodeType getNodeType(ContentObjectKey contentObjectKey) {
        ExtendedNodeType nt = null;
        ContentObject contentObject = null;
        try {
            contentObject = ContentObject
                    .getContentObjectInstance(contentObjectKey);
        } catch (ClassNotFoundException e) {
            logger.warn("Unable to get content object for key '"
                    + contentObjectKey + "'", e);
        }

        if (contentObject != null) {
            if (contentObject instanceof ContentContainer || contentObject instanceof ContentContainerList) {
                JahiaContainerDefinition containerDef = null;
                try {
                    containerDef = JahiaContainerDefinitionsRegistry
                            .getInstance()
                            .getDefinition(
                                    contentObject instanceof ContentContainer ? ((ContentContainer) contentObject)
                                            .getDefinitionID()
                                            : ((ContentContainerList) contentObject)
                                                    .getDefinitionID());
                } catch (JahiaException e) {
                    logger.warn("Unable to get container definition for ID '"
                            + ((ContentContainer) contentObject)
                                    .getDefinitionID() + "'", e);
                }
                if (containerDef != null) {
                    try {
                        nt = NodeTypeRegistry.getInstance().getNodeType(
                                containerDef.getContainerType());
                    } catch (NoSuchNodeTypeException e) {
                        logger
                                .warn(
                                        "Unable to find node type definition for type '"
                                                + containerDef
                                                        .getContainerType()
                                                + "'", e);
                    }
                }
            }
        }
        return nt;
    }

    /**
     * Returns the name of the primary node type for the specified content
     * object.
     * 
     * @param contentObjectKey
     *            the content object key
     * @return the name of the primary node type for the specified content
     *         object
     */
    public static String getNodeTypeName(ContentObjectKey contentObjectKey) {
        ExtendedNodeType nt = getNodeType(contentObjectKey);
        return nt != null ? nt.getName() : null;
    }

    /**
     * Returns the names of the primary node and all the super types for the
     * specified content object.
     * 
     * @param contentObjectKey
     *            the content object key
     * @return Returns the names of the primary node and all the super types for
     *         the specified content object
     */
    public static String[] getNodeTypeNamesWithSuperTypes(
            ContentObjectKey contentObjectKey) {
        List<String> typeNames = new LinkedList<String>();
        ExtendedNodeType nt = getNodeType(contentObjectKey);
        if (nt != null) {
            typeNames.add(nt.getName());
            ExtendedNodeType[] superTypes = nt.getSupertypes();
            for (ExtendedNodeType superType : superTypes) {
                typeNames.add(superType.getName());
            }
        }
        return typeNames.toArray(new String[] {});
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
     * jcr:contains() (inside XPath queries). The characters - and " have
     * special meaning, and may be escaped with a backslash to obtain their
     * literal value. See JSR-170 spec v1.0, Sec. 6.6.5.2.
     * 
     * @param str
     *            Any string.
     * @return A valid XPath 2.0 string literal suitable for use in
     *         jcr:contains(), including enclosing quotes.
     */
    public static String stringToJCRSearchExp(String str) {
        // Escape ' and \ everywhere, preceding them with \ except when \
        // appears
        // in one of the combinations \" or \-
        return stringToXPathLiteral(str.replaceAll("\\\\(?![-\"])", "\\\\\\\\")
                .replaceAll("'", "\\\\'").replaceAll("\"", "\\\\\""));
    }

    /**
     * Convert a string to an XPath 2.0 string literal, suitable for inclusion
     * in a query. See JSR-170 spec v1.0, Sec. 6.6.4.9.
     * 
     * @param str
     *            Any string.
     * @return A valid XPath 2.0 string literal, including enclosing quotes.
     */
    public static String stringToXPathLiteral(String str) {
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
}
