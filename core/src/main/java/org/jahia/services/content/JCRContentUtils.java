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

package org.jahia.services.content;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.util.Text;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.usermanager.JahiaUser;

import com.ibm.icu.text.Normalizer;

import javax.jcr.*;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
    
    public static JCRContentUtils getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("JCRContentUtils is not initialized yet");
        }
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
        return stringToQueryLiteral(Text.escapeIllegalXpathSearchChars(str));
    }    

    /**
     * Convert a path string to encoded path Strings in XPATH queries
     * 
     * @param str
     *            Any string.
     * @return A valid string path suitable for use in XPATH queries
     */
    public static String stringToJCRPathExp(String str) {
        return ISO9075.encode(Text.escapeIllegalJcrChars(str));
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

    /**
     * If the node path contains site information (i.e.
     * <code>/sites/&lt;siteKey&gt;/...</code>) this method returns the site key
     * part; otherwise <code>null</code> is returned.
     * 
     * @param jcrNodePath the JCR node path
     * @return if the node path contains site information (i.e.
     *         <code>/sites/&lt;siteKey&gt;/...</code>) this method returns the
     *         site key part; otherwise <code>null</code> is returned
     */
    public static String getSiteKey(String jcrNodePath) {
        return jcrNodePath != null ? (jcrNodePath.startsWith("/sites/") ? StringUtils.substringBetween(jcrNodePath,
                "/sites/", "/") : null) : null;
    }
    
    /**
     * Returns the number of elements in the provided iterator.
     * 
     * @param iterator the item iterator to check the size
     * @return the number of elements in the provided iterator
     */
    public static long size(RangeIterator iterator) {
        long size = iterator.getSize();
        if (size <= 0) {
            size = 0;
            while (iterator.hasNext()) {
                size++;
                iterator.next();
            }
        }

        return size;
    }

    private Map<String, List<String>> mimeTypes;

    private Map<String, String> fileExtensionIcons;

    /**
     * Initializes an instance of this class.
     * 
     * @param mimeTypes
     *            a map with mime type mappings
     * @param fileExtensionIcons mapping between file extensions and corresponding icons 
     */
    @SuppressWarnings("unchecked")
    public JCRContentUtils(Map<String, List<String>> mimeTypes, Map<String, String> fileExtensionIcons) {
        super();
        instance = this;
        this.mimeTypes = UnmodifiableMap.decorate(mimeTypes);
        this.fileExtensionIcons = UnmodifiableMap.decorate(fileExtensionIcons);
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
                    JCRSiteNode site = node.getResolveSite();
                    Map<String, List<String[]>> aclEntriesMap = node.getAclEntries();

                    Set<Map.Entry<String,List<String[]>>> principalSet = aclEntriesMap.entrySet();
                    for (Map.Entry<String,List<String[]>> currentPrincipal : principalSet) {
                        boolean isUser = currentPrincipal.getKey().indexOf("u:") == 0;
                        String principalName = currentPrincipal.getKey().substring(2);

                        // test if the principal is the user or if the user belongs to the principal (group)
                        if ((isUser && principalName.equalsIgnoreCase(user.getUsername())) || user.isMemberOfGroup(site != null ? site.getID() : 0, principalName)) {
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
     * @return the next available name for a node, appending if needed numbers
     */
    public static String findAvailableNodeName(Node dest, String name) {
        int i = 1;

        String basename = name;
        int dot = basename.lastIndexOf('.');
        String ext = "";
        if (dot > 0) {
            ext = basename.substring(dot);
            basename = basename.substring(0, dot);
        }
        int und = basename.lastIndexOf('-');
        if (und > -1 && basename.substring(und + 1).matches("[0-9]+")) {
            basename = basename.substring(0, und);
        }

        do {
            try {
                dest.getNode(name);
                name = basename + "-" + (i++) + ext;
            } catch (RepositoryException e) {
                break;
            }

        } while (true);

        return name;
    }
    
    /**
     * Generates the JCR node name from the provided text by normalizing it,
     * converting to lower case, replacing spaces with dashes and truncating to
     * ${@code maxLength} characters.
     * 
     * @param text the original text to be used as a source
     * @param maxLength the maximum length of the resulting name (it will be
     *            truncated if needed)
     * @return the JCR node name from the provided text by normalizing it,
     *         converting to lower case, replacing spaces with dashes and
     *         truncating to ${@code maxLength} characters
     */
    public static String generateNodeName(String text, int maxLength) {
        String nodeName = text;
        final char[] chars = Normalizer.normalize(nodeName, Normalizer.NFKD).toCharArray();
        final char[] newChars = new char[chars.length];
        int j = 0;
        for (char aChar : chars) {
            if (CharUtils.isAsciiAlphanumeric(aChar) || aChar == 32) {
                newChars[j++] = aChar;
            }
        }
        nodeName = new String(newChars, 0, j).trim().replaceAll(" ", "-").toLowerCase();
        if (nodeName.length() > maxLength) {
            nodeName = nodeName.substring(0, maxLength);
            if (nodeName.endsWith("-") && nodeName.length() > 2) {
                nodeName = nodeName.substring(0, nodeName.length() - 1);
            }
        }

        return nodeName;
    }

    /**
     * Returns a mapping between file extensions and corresponding icons.
     * 
     * @return a mapping between file extensions and corresponding icons
     */
    public Map<String, String> getFileExtensionIcons() {
        return fileExtensionIcons;
    }

    public static Node getPathFolder(Node root, String name, String options) throws RepositoryException {
        Node result = root;
        if (options.contains("initials")) {
            String s = "" + Character.toUpperCase(name.charAt(0));
            if (!result.hasNode(s)) {
                result = result.addNode(s, Constants.JAHIANT_SYSTEMFOLDER);
            } else {
                result = result.getNode(s);
            }
        }
        return result;
    }

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

    public static List<JCRNodeWrapper> getChildrenOfType(JCRNodeWrapper node, String type) {
        List<JCRNodeWrapper> children = null;
        if (type.contains(",")) {
            String[] typesToCheck = type.split(",");
            List<JCRNodeWrapper> matchingChildren = new LinkedList<JCRNodeWrapper>();
            try {
                for (NodeIterator iterator = node.getNodes(); iterator.hasNext();) {
                    Node child = iterator.nextNode();
                    for (String matchType : typesToCheck) {
                        if (child.isNodeType(matchType)) {
                            matchingChildren.add((JCRNodeWrapper) child);
                            break;
                        }
                    }
                }
            } catch (RepositoryException e) {
                logger.warn(e.getMessage(), e);
            }
            children = matchingChildren;
        } else {
            children = getNodes(node, type);
        }
        return children;
    }

    public static List<JCRNodeWrapper> getNodes(JCRNodeWrapper node, String type) {
        try {
            List<JCRNodeWrapper> res = new ArrayList<JCRNodeWrapper>();
            NodeIterator ni = node.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
                if (child.isNodeType(type)) {
                    res.add(child);
                }
            }
            return res;
//            return node.getSession().getWorkspace().getQueryManager().createQuery("select * from ["+type+"] as sel where ischildnode(sel,['"+node.getPath()+"'])",
//                                                                                  Query.JCR_SQL2).execute().getNodes();
        } catch (InvalidQueryException e) {
            logger.error("Error while retrieving nodes", e);
        } catch (RepositoryException e) {
            logger.error("Error while retrieving nodes", e);
        }
        return new LinkedList<JCRNodeWrapper>();
    }

    public static NodeIterator getDescendantNodes(JCRNodeWrapper node, String type) {
        try {
            return node.getSession().getWorkspace().getQueryManager().createQuery("select * from ["+type+"] as sel where isdescendantnode(sel,['"+node.getPath()+"'])",
                                                                                  Query.JCR_SQL2).execute().getNodes();
        } catch (InvalidQueryException e) {
            logger.error("Error while retrieving nodes", e);
        } catch (RepositoryException e) {
            logger.error("Error while retrieving nodes", e);
        }
        return NodeIteratorImpl.EMPTY;
    }

    
    /**
     * Downloads the JCR content to a specified file. 
     * @param node the JCR node with the file content 
     * @param targetFile target file to write data into
     * @return the target file descriptor
     * @throws IOException in case of an error
     */
    public static File downloadFileContent(JCRNodeWrapper node, File targetFile) throws IOException {
        InputStream is = node.getFileContent().downloadFile();
        if (is == null) {
            throw new IllegalArgumentException("Provided node has no file content");
        }
        FileOutputStream os = new FileOutputStream(targetFile);
        try {
            IOUtils.copy(is, os);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
        return targetFile;
    }
    
    /**
     * Downloads the JCR content to a temporary file. 
     * @param node the JCR node with the file content 
     * @return the target file descriptor
     * @throws IOException in case of an error
     */
    public static File downloadFileContent(JCRNodeWrapper node) throws IOException {
        return downloadFileContent(node, File.createTempFile("data", null));
    }

    /**
     * Little utility method to retrieve or create a path, building it if necessary. For example let's say that we want
     * to get or create the path from a parentNode : messages/inbox . We can simply pass the parent node, the session
     * and the path to check, and it will either retrieve it if it exists, or create it if it doesn't.
     *
     * Please note that this method also checks out the parent nodes.
     * @param session
     * @param parentNode the parent node in from which we want to retrieve the relative path.
     * @param path the path to retrieve or create. Note that this path MUST be relative
     * @param pathNodeType the type to use for the intermediary nodes (and the last path too !) if they need to be
     * created. Usually you'll want to use Constants.JAHIANT_CONTENTLIST here.
     * @return the leaf that is equivalent to the lowest path value.
     * @throws RepositoryException occurs if there is any problem accessing content or creating the nodes.
     */
    public static JCRNodeWrapper getOrAddPath(JCRSessionWrapper session, JCRNodeWrapper parentNode, String path, String pathNodeType) throws RepositoryException {
        String[] subPaths = path.split("/");
        JCRNodeWrapper node = parentNode;
        for (String subPath : subPaths) {
            if (StringUtils.isNotBlank(subPath) && !"*".equals(subPath)) {
                try {
                    node = node.getNode(subPath);
                    session.checkout(node);
                } catch (PathNotFoundException e) {
                    if (node != null) {
                        session.checkout(node);
                        node = node.addNode(subPath, pathNodeType);
                    }
                }
            }
        }
        return node;
    }

}
