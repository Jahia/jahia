/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.stream.Streams;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.gc.GarbageCollector;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.util.Text;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.decorator.*;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Template;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.FileUtils;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;

import javax.jcr.*;
import javax.jcr.nodetype.*;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.servlet.ServletContext;
import java.io.*;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jahia.api.Constants.*;

/**
 * Utility class for accessing and manipulation JCR properties.
 *
 * @author Sergiy Shyrkov
 */
public final class JCRContentUtils implements ServletContextAware {

    private static final Value[] EMPTY_VALUES = new Value[] {};

    public static final Pattern COLON_PATTERN = Patterns.COLON;

    public static final Comparator<NodeType> NODE_TYPE_NAME_COMPARATOR = new Comparator<NodeType>() {

        @Override
        public int compare(NodeType o1, NodeType o2) {
            return o1 == o2 ? 0 : o1.getName().compareTo(o2.getName());
        }
    };

    private static final String PREFIX = Jahia.getContextPath() + "/modules/";
    private static final Logger logger = LoggerFactory.getLogger(JCRContentUtils.class);

    private static Map<String, Boolean> iconsPresence = new ConcurrentHashMap<String, Boolean>(512, 0.8f, 32);
    private static volatile JCRContentUtils instance;

    private final Map<String, String> fileExtensionIcons;
    private final Map<String, List<String>> mimeTypes;
    private final Map<String, String> defaultUserFolderTypes;

    private NameGenerationHelper nameGenerationHelper;
    private Set<String> unsupportedMarkForDeletionNodeTypes = Collections.emptySet();
    private Pattern handleFallbackLocaleForPath;
    private ServletContext servletContext;

    /**
     * Initializes an instance of this class.
     *
     * @param mimeTypes a map with mime type mappings
     * @param fileExtensionIcons mapping between file extensions and corresponding icons
     */
    @SuppressWarnings("unchecked")
    public JCRContentUtils(Map<String, List<String>> mimeTypes, Map<String, String> fileExtensionIcons, Map<String, String> defaultUserFolderTypes) {
        instance = this;
        this.mimeTypes = UnmodifiableMap.decorate(mimeTypes);
        this.fileExtensionIcons = UnmodifiableMap.decorate(fileExtensionIcons);
        this.defaultUserFolderTypes = UnmodifiableMap.decorate(defaultUserFolderTypes);
    }

    public static JCRContentUtils getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("JCRContentUtils is not initialized yet");
        }
        return instance;
    }

    /**
     * Calls the datastore garbage collector and returns the number of data entries deleted.
     *
     * @return the number of data entries deleted
     * @throws RepositoryException in case of an error
     */
    public static int callDataStoreGarbageCollector() throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Integer>() {
            public Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
                int deleted = 0;
                GarbageCollector gc = ((SessionImpl) session.getProviderSession(session
                        .getNode("/").getProvider())).createDataStoreGarbageCollector();
                try {
                    gc.mark();
                    deleted = gc.sweep();
                    logger.info("Datastore garbage collector deleted {} data records", deleted);
                } finally {
                    gc.close();
                }
                return deleted;
            }
        });
    }

    public static boolean check(String icon) {
        String moduleId = StringUtils.substringBefore(icon, "/");
        String pathAfter = StringUtils.substringAfter(icon, "/");
        JahiaTemplatesPackage module = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(moduleId);
        icon = module.getId() + "/" + module.getVersion() + "/" + pathAfter;

        Boolean present = iconsPresence.get(icon);
        if (present == null) {
            present = module.resourceExists(pathAfter + ".png");
            iconsPresence.put(icon, present);
        }
        return present;
    }

    /**
     * Removes all locks on the current node and optionally on its children.
     *
     * @param path               the path of the node remove locks from
     * @param processChildNodes  do we need to also remove locks in children?
     * @param workspace          workspace
     * @throws RepositoryException in case of an error
     */
    public static void clearAllLocks(final String path, final boolean processChildNodes, final String workspace) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                clearAllLocks(path, processChildNodes, session);
                return null;
            }
        });
    }

    private static void clearAllLocks(String path, boolean processChildNodes,
                                      JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper node = session.getNode(path);
        node.clearAllLocks();
        if (processChildNodes) {
            for (NodeIterator iterator = node.getNodes(); iterator.hasNext(); ) {
                JCRNodeWrapper child = (JCRNodeWrapper) iterator.next();
                clearAllLocks(child.getPath(), processChildNodes, session);
            }
        }
    }

    /**
     * Creates the JCR property value, depending on the type of the corresponding value.
     *
     * @param objectValue the object value to be converted
     * @param factory     the {@link ValueFactory} instance
     * @return the JCR property value
     */
    public static Value createValue(Object objectValue, ValueFactory factory) {
        if (objectValue instanceof String) {
            return factory.createValue((String) objectValue);
        } else if (objectValue instanceof Boolean) {
            return factory.createValue((Boolean) objectValue);
        } else if (objectValue instanceof Long) {
            return factory.createValue((Long) objectValue);
        } else if (objectValue instanceof Integer) {
            return factory.createValue(((Integer) objectValue).longValue());
        } else if (objectValue instanceof Calendar) {
            return factory.createValue((Calendar) objectValue);
        } else if (objectValue instanceof Date) {
            Calendar c = new GregorianCalendar();
            c.setTime((Date) objectValue);
            return factory.createValue(c);
        } else if (objectValue instanceof byte[] || objectValue instanceof File) {
            InputStream is = null;
            try {
                is = objectValue instanceof File ? new BufferedInputStream(new FileInputStream((File) objectValue))
                        : new ByteArrayInputStream((byte[]) objectValue);
                return factory.createValue(factory.createBinary(is));
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        } else {
            logger.warn("Do not know, how to handle value of type {}", objectValue.getClass()
                    .getName());
        }
        return null;
    }

    /**
     * Creates the JCR property values, depending on the type of the corresponding value.
     *
     * @param objectValues the object values to be converted
     * @param factory     the {@link ValueFactory} instance
     * @return the JCR property value
     */
    public static Value[] createValues(Collection<?> objectValues, ValueFactory factory) {
        if (objectValues == null) {
            return null;
        } else if (objectValues.isEmpty()) {
            return EMPTY_VALUES;
        } else {
            List<Value> vals = new ArrayList<>(objectValues.size());
            for (Object objValue : objectValues) {
                vals.add(createValue(objValue, factory));
            }
            return vals.toArray(EMPTY_VALUES);
        }
    }

    /**
     * Reverse operation of encodeJCRNamePrefix.
     * Note : this is not yet complete as it depends on the restrictions imposed on XML namespaces as
     * defined here http://www.w3.org/TR/REC-xml-names/
     *
     * @param encodedPrefix
     * @return
     */
    public static String decodeJCRNamePrefix(final String encodedPrefix) {
        String originalPrefix = encodedPrefix.replace("\\3A", ":");
        return originalPrefix;
    }

    /**
     * Can be used to delete the Jackrabbit indexes folders for version and workspaces to force re-indexing on the next repository startup.
     * Note please that this method can only be invoked when the repository is not started (offline).
     *
     * @param repositoryHome the repository home folder
     */
    public static void deleteJackrabbitIndexes(File repositoryHome) {
        if (repositoryHome == null || !repositoryHome.isDirectory()) {
            return;
        }
        logger.info("Removing JCR repository indexes in repository {}", repositoryHome);

        // Remove index directories to force re-indexing on next startup
        org.apache.commons.io.FileUtils.deleteQuietly(new File(repositoryHome, "index"));
        File workspaces = new File(repositoryHome, "workspaces");
        org.apache.commons.io.FileUtils.deleteQuietly(new File(new File(workspaces,
                Constants.EDIT_WORKSPACE), "index"));
        org.apache.commons.io.FileUtils.deleteQuietly(new File(new File(workspaces,
                Constants.LIVE_WORKSPACE), "index"));

        logger.info("...done removing index folders.");
    }

    /**
     * Downloads the JCR content to a temporary file.
     *
     * @param node the JCR node with the file content
     * @return the target file descriptor
     * @throws IOException in case of an error
     */
    public static File downloadFileContent(JCRNodeWrapper node) throws IOException {
        return downloadFileContent(node, File.createTempFile("data", null));
    }

    /**
     * Downloads the JCR content to a specified file.
     *
     * @param node       the JCR node with the file content
     * @param targetFile target file to write data into
     * @return the target file descriptor
     * @throws IOException in case of an error
     */
    public static File downloadFileContent(JCRNodeWrapper node, File targetFile) throws IOException {
        InputStream is = node.getFileContent().downloadFile();
        if (is == null) {
            throw new IllegalArgumentException("Provided node has no file content");
        }
        OutputStream os = new BufferedOutputStream(new FileOutputStream(targetFile));
        try {
            IOUtils.copy(is, os);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
        return targetFile;
    }

    /**
     * Encode a JCR qualified form name prefix, according to the grammar defined in section 3.2.5.2
     * of the JCR 2.0 specification http://www.day.com/specs/jcr/2.0/3_Repository_Model.html#Names
     * Note : this is not yet complete as it depends on the restrictions imposed on XML namespaces as
     * defined here http://www.w3.org/TR/REC-xml-names/
     *
     * @param originalPrefix
     * @return
     */
    public static String encodeJCRNamePrefix(final String originalPrefix) {
        String encodedPrefix = originalPrefix.replace(":", "\\3A");
        return encodedPrefix;
    }

    public static String escapeLocalNodeName(String name) {
        name = name.trim();
        StringBuilder buffer = new StringBuilder(name.length() * 2);
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == '[' || ch == ']' || ch == '*' || ch == '|') {
                buffer.append('%');
                buffer.append(Character.toUpperCase(Character.forDigit(ch / 16, 16)));
                buffer.append(Character.toUpperCase(Character.forDigit(ch % 16, 16)));
            } else if (ch == '/' || ch == '<' || ch == '>' || ch == '\\' || Character.isWhitespace(ch)) {
                if (buffer.length() > 0) {
                    buffer.append(' ');
                }
            } else {
                buffer.append(ch);
            }
        }
        // Use NFC normalization to format characters as some browser like SAFARI encode requests using NFC (QA-14109)
        return Normalizer.normalize(buffer.toString(), Normalizer.Form.NFC).trim();
    }

    public static String escapeNodePath(String path) {
        StringBuilder buffer = new StringBuilder(path.length() * 2);
        for (int i = 0; i < path.length(); i++) {
            char ch = path.charAt(i);
            if (ch == '[' || ch == ']' || ch == '*' || ch == '|') {
                buffer.append('%');
                buffer.append(Character.toUpperCase(Character.forDigit(ch / 16, 16)));
                buffer.append(Character.toUpperCase(Character.forDigit(ch % 16, 16)));
            } else {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }

    /**
     * Returns the next available name for a node, appending if needed numbers.
     *
     * @param dest the destination node, where the new one will be created
     * @param name the name of the new node
     * @return the next available name for a node, appending if needed numbers
     */
    public static String findAvailableNodeName(Node dest, String name) {
        return findAvailableNodeName(dest, name, true);
    }

    /**
     * Returns the next available name for a node, appending if needed numbers.
     *
     * @param dest the destination node, where the new one will be created
     * @param name the name of the new node
     * @param hasExtension <code>true</code> if the name has an extension (e.g. in case of file names)
     * @return the next available name for a node, appending if needed numbers
     */
    public static String findAvailableNodeName(Node dest, String name, boolean hasExtension) {
        try {
            dest.getNode(name);
        } catch (RepositoryException e) {
            return name;
        }

        int i = 1;

        String basename = name;
        String ext = "";
        if (hasExtension) {
            int dot = basename.lastIndexOf('.');
            if (dot > 0) {
                ext = basename.substring(dot);
                basename = basename.substring(0, dot);
            }
        }
        int und = basename.lastIndexOf('-');
        if (und > -1 && Patterns.NUMBERS.matcher(basename.substring(und + 1)).matches()) {
            basename = basename.substring(0, und);
        }

        do {
            try {
                String newSuffix = hasExtension ? ("-" + (i++) + ext) : ("-" + (i++));
                name = basename + newSuffix;
                //name has a size limit of 128 chars
                int maxNameSize = SettingsBean.getInstance().getMaxNameSize();
                if (name.length() > maxNameSize) {
                    name = basename.substring(0, (basename.length() <= maxNameSize ? basename.length() : maxNameSize) - newSuffix.length()) + newSuffix;
                }
                dest.getNode(name);
            } catch (RepositoryException e) {
                break;
            }

        } while (true);

        return name;
    }

    public static JCRNodeWrapper findDisplayableNode(JCRNodeWrapper node, RenderContext context) {
        Template template = null;
        JCRNodeWrapper currentNode = node;
        try {
            while (template == null && currentNode != null) {
                template = RenderService.getInstance().resolveTemplate(new org.jahia.services.render.Resource(
                        currentNode, "html", null, org.jahia.services.render.Resource.CONFIGURATION_PAGE), context);
                if (template == null) {
                    currentNode = currentNode.getParent();
                }
            }
        } catch (Exception e) {
            currentNode = null;
        }
        return currentNode;
    }

    public static JCRNodeWrapper findDisplayableNode(JCRNodeWrapper node, RenderContext context, JCRSiteNode contextSite) {
        if (contextSite != null) {
            final JCRSiteNode old = context.getSite();
            context.setSite(contextSite);
            final JCRNodeWrapper displayableNode = JCRContentUtils.findDisplayableNode(node, context);
            context.setSite(old);
            return displayableNode;
        } else {
            return JCRContentUtils.findDisplayableNode(node, context);
        }
    }

    public static String generateNodeName(String text) {
        return generateNodeName(text, SettingsBean.getInstance().getMaxNameSize());
    }

    /**
     * Generates the JCR node name from the provided text by normalizing it,
     * converting to lower case, replacing spaces with dashes and truncating to
     * ${@code maxLength} characters.
     *
     * @param text      the original text to be used as a source
     * @param maxLength the maximum length of the resulting name (it will be
     *                  truncated if needed)
     * @return the JCR node name from the provided text by normalizing it,
     *         converting to lower case, replacing spaces with dashes and
     *         truncating to ${@code maxLength} characters
     */
    public static String generateNodeName(String text, int maxLength) {
        String nodeName = text.toLowerCase();
        nodeName = Normalizer.normalize(nodeName, Normalizer.Form.NFKD);
        nodeName = Patterns.ACCENTS.matcher(nodeName).replaceAll("");

        Matcher matcher = Patterns.NON_ALLOWED_CHARS.matcher(nodeName);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String replacement = Patterns.CHARMAP.getProperty(matcher.group(0));
            if (replacement == null) {
                replacement = "-";
            }
            matcher.appendReplacement(buffer, "");
            buffer.append(replacement);
        }
        matcher.appendTail(buffer);
        nodeName = buffer.toString();

        nodeName = Patterns.NON_ALLOWED_CHARS.matcher(nodeName).replaceAll("-");
        nodeName = Patterns.CONSECUTIVE_DASHES.matcher(nodeName).replaceAll("-");
        nodeName = Patterns.HEADING_DASH.matcher(nodeName).replaceAll("");
        if (nodeName.length() > maxLength) {
            nodeName = nodeName.substring(0, maxLength);
        }
        nodeName = Patterns.TRAILING_DASH.matcher(nodeName).replaceAll("");

        return StringUtils.isNotEmpty(nodeName) ? nodeName : "untitled";
    }

    /**
     * Returns a set of all mixin types, which can be added to the provided node.
     *
     * @param node the node to add mixin types to
     * @return a set of all mixin types, which can be added to the provided node
     * @throws RepositoryException in case of a repository access error
     */
    public static Set<String> getAssignableMixins(JCRNodeWrapper node) throws RepositoryException {
        Set<String> mixins = new TreeSet<String>();
        Set<String> existingMixins = new HashSet<String>(node.getNodeTypes());
        existingMixins.add("mix:shareable"); // we will skip this one

        NodeTypeIterator allMixins = node.getSession().getWorkspace().getNodeTypeManager()
                .getMixinNodeTypes();
        while (allMixins.hasNext()) {
            String nt = allMixins.nextNodeType().getName();
            if (!existingMixins.contains(nt) && node.canAddMixin(nt)) {
                mixins.add(nt);
            }
        }

        return mixins;
    }

    /**
     * Returns a list of child nodes of the provided node, matching the specified node type (multiple node types can be specified, separated
     * by a comma).
     *
     * @param node the parent node to retrieve children from
     * @param type the node type to be matched by a retrieved child. Multiple node types can be specified, separated by a comma.
     * @return a list of child nodes of the provided node, matching the specified node type (multiple node types can be specified, separated
     *         by a comma).
     */
    public static List<JCRNodeWrapper> getChildrenOfType(JCRNodeWrapper node, String type) {
        return getChildrenOfType(node, type, 0);
    }

    public static List<JCRNodeWrapper> getChildrenOfType(JCRNodeWrapper node, String type, int limit) {
        List<JCRNodeWrapper> children = null;
        if (node == null) {
            return null;
        }
        if (type.contains(",")) {
            String[] typesToCheck = Patterns.COMMA.split(type);
            List<JCRNodeWrapper> matchingChildren = new LinkedList<JCRNodeWrapper>();
            try {
                for (NodeIterator iterator = node.getNodes(); iterator.hasNext(); ) {
                    if (limit > 0 && matchingChildren.size() == limit) {
                        break;
                    }
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
            children = getNodes(node, type, limit);
        }
        return children;
    }

    /**
     * Returns a content object key for a node if available. Otherwise returns
     * node name.
     *
     * @param node the node to get name
     * @return a content object key for a node if available. Otherwise returns
     *         node name
     * @throws RepositoryException in case of JCR-related errors
     */
    public static String getContentNodeName(Node node)
            throws RepositoryException {
        return node.getName();
    }

    /**
     * Returns a node path, composed using only content object keys, i.e.
     * <p/>
     * <code>/mySite/ContentPage_1/ContentPage_19/ContentContainerList_21/ContentContainer_13</code>
     *
     * @param node the content node to compute path for
     * @return a node path, composed using only content object keys
     * @throws RepositoryException in case of JCR-related errors
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

    public static NodeIterator getDescendantNodes(JCRNodeWrapper node, String type) {
        try {
            return node.getSession().getWorkspace().getQueryManager().createQuery("select * from [" + type + "] as sel where isdescendantnode(sel,['" + JCRContentUtils.sqlEncode(node.getPath()) + "'])",
                    Query.JCR_SQL2).execute().getNodes();
        } catch (InvalidQueryException e) {
            logger.error("Error while retrieving nodes", e);
        } catch (RepositoryException e) {
            logger.error("Error while retrieving nodes", e);
        }
        return NodeIteratorImpl.EMPTY;
    }

    /**
     * Get the node or property display name depending on the locale
     *
     * @param item                      the item to get the label for
     * @param locale                    current locale
     * @param nodeTypeForSearchingLabel
     * @return the node or property display name depending on the locale
     */
    public static String getDisplayLabel(Object item, Locale locale, ExtendedNodeType nodeTypeForSearchingLabel) {
        if (item != null) {

            try {
                // case of property
                if (item instanceof Property) {
                    Property property = (Property) item;
                    PropertyDefinition propertyDefintion = property.getDefinition();
                    if (propertyDefintion != null && propertyDefintion instanceof ExtendedPropertyDefinition) {
                        ExtendedPropertyDefinition itemDef = (ExtendedPropertyDefinition) propertyDefintion;
                        return itemDef.getLabel(locale, nodeTypeForSearchingLabel);
                    } else {
                        logger.error("PropertyDefinition doesn't implement 'org.jahia.services.content.nodetypes.ExtendedPropertyDefinition'");
                    }
                }
                // case of PropertyDefinition
                else if (item instanceof PropertyDefinition) {
                    if (item instanceof ExtendedPropertyDefinition) {
                        ExtendedPropertyDefinition itemDef = (ExtendedPropertyDefinition) item;
                        return itemDef.getLabel(locale, nodeTypeForSearchingLabel);
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
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public static String getExpandedName(String name, NamespaceRegistry namespaceRegistry) throws RepositoryException {
        if (!name.startsWith("{")) {
            if (name.contains(":")) {
                name = "{" + namespaceRegistry.getURI(StringUtils.substringBefore(name, ":")) + "}" +
                        StringUtils.substringAfter(name, ":");
            } else {
                name = "{}" + name;
            }
        }

        return name;
    }

    public static String getJCRName(String qualifiedName, NamespaceRegistry namespaceRegistry) throws RepositoryException {
        if (qualifiedName.startsWith("{")) {
            final String uri = StringUtils.substringBetween(qualifiedName, "{", "}");
            if (uri.isEmpty()) {
                qualifiedName = StringUtils.substringAfter(qualifiedName, "}");
            } else {
                qualifiedName = namespaceRegistry.getPrefix(uri) + ":" + StringUtils.substringAfter(qualifiedName, "}");
            }
        }

        return qualifiedName;
    }

    public static String getIconWithContext(ExtendedNodeType type) throws RepositoryException {
        String icon = getIcon(type, null);
        if (icon == null) {
            icon = getIcon(NodeTypeRegistry.getInstance().getNodeType("jmix:droppableContent"));
        }
        return PREFIX + icon;
    }

    /**
     * Returns the icon path for the specified type
     * @param type wanted type
     * @return Path of the icon
     * @throws RepositoryException in case of JCR-related errors
     */
    public static String getIcon(ExtendedNodeType type) throws RepositoryException {
        return getIcon(type, null);
    }

    private static String getIcon(ExtendedNodeType type, String subType) throws RepositoryException {
        String icon = getIconsFolder(type) + replaceColon(type.getName()) + (StringUtils.isEmpty(subType) ? "" : "_" + subType);
        if (check(icon)) {
            return icon;
        } else if (!StringUtils.isEmpty(subType)) {
            icon = getIconsFolder(type) + replaceColon(type.getName());
            if (check(icon)) {
                return icon;
            }
        }
        for (ExtendedNodeType nodeType : type.getSupertypes()) {
            icon = getIconsFolder(nodeType) + replaceColon(nodeType.getName());
            if (check(icon)) {
                return icon;
            }
        }
        return null;
    }

    public static String getIcon(JCRNodeWrapper f) throws RepositoryException {
        return getIconWithContext(f, false);
    }

    public static String getIconWithContext(JCRNodeWrapper f, boolean useContext) throws RepositoryException {
        ExtendedNodeType primaryNodeType = f.getPrimaryNodeType();
        String folder = getIconsFolder(primaryNodeType);
        if (f.isNodeType("jmix:hasIcon") && f.hasProperty("j:icon")) {
            return ((JCRFileNode) f.getProperty("j:icon").getNode()).getUrl();
        }

        if (f.isFile()) {
            String icon;
            String nodeName = f.getName();

            if (FilenameUtils.indexOfExtension(nodeName) != -1) {
                icon = FileUtils.getFileIcon(nodeName);
            } else {
                icon = FileUtils.getFileIconFromMimetype(f.getFileContent().getContentType());
            }

            return (useContext ? PREFIX : "") + folder + "jnt_file_" + icon;
        } else if (f instanceof JCRComponentNode) {
            String type = f.getName();
            ExtendedNodeType nt = primaryNodeType;
            if (!"components".equals(type)) {
                try {
                    nt = NodeTypeRegistry.getInstance().getNodeType(type);
                } catch (NoSuchNodeTypeException e) {

                }
            }
            return (useContext ? PREFIX : "") + getIcon(nt, getSubType(nt, f));
        } else if (!f.getProvider().isDefault() && f.isNodeType("jnt:folder")) {
            return (useContext ? PREFIX : "") + folder + "remoteFolder";
        } else {
            return (useContext ? PREFIX : "") + getIcon(primaryNodeType, getSubType(primaryNodeType, f));
        }
    }

    private static String getSubType(ExtendedNodeType nt, JCRNodeWrapper f) {
        String subType = null;
        try {
            if (Constants.JAHIANT_VIRTUALSITE.equals(nt.getName()) && f.hasProperty(Constants.SITETYPE)) {
                subType = f.getPropertyAsString(Constants.SITETYPE);
            }
        } catch (RepositoryException e) {
        }
        return subType;
    }

    /**
     * For a nodetype gets the folder that contains its icon
     * for system and module default nodetypes, icons asset folder is returned
     * @param primaryNodeType
     * @return folder
     * @throws RepositoryException in case of JCR-related errors
     */
    public static String getIconsFolder(final ExtendedNodeType primaryNodeType) throws RepositoryException {
        String systemId = primaryNodeType.getSystemId();
        JahiaTemplatesPackage aPackage = !systemId.startsWith("system-") && !JahiaTemplatesPackage.ID_DEFAULT.equals(systemId) ? ServicesRegistry
                .getInstance().getJahiaTemplateManagerService().getTemplatePackageById(systemId) : null;

        return aPackage != null ? aPackage.getId() + "/icons/" : "assets/icons/";
    }

    /**
     * Returns the parsed lock type from the provided token.
     *
     * @param lockTypeToken the token to detect lock type from
     * @return the parsed lock type from the provided token
     */
    public static JCRNodeLockType getLockType(String lockTypeToken) {
        JCRNodeLockType type = JCRNodeLockType.UNKNOWN;
        if (lockTypeToken != null && lockTypeToken.length() > 1) {
            if (lockTypeToken.charAt(0) == ' ') {
                // system or process type lock
                if (lockTypeToken.startsWith(MARKED_FOR_DELETION_LOCK_USER)) {
                    type = JCRNodeLockType.DELETION;
                } else if (lockTypeToken.endsWith(":validation")) {
                    type = JCRNodeLockType.WORKFLOW;
                }
            } else {
                // user lock
                type = JCRNodeLockType.USER;
            }
        }

        return type;
    }

    /**
     * Returns a set of lock types for the current node. If there are no locks found in the lock infos, returns an empty set.
     *
     * @param lockInfos the lock information of a node
     * @return a set of lock types for the current node. If there are no locks found in the lock infos, returns an empty set.
     */
    public static Set<JCRNodeLockType> getLockTypes(Map<String, List<String>> lockInfos) {
        Set<JCRNodeLockType> types = Collections.emptySet();
        if (lockInfos != null && !lockInfos.isEmpty()) {
            types = new HashSet<JCRNodeLockType>(4);
            for (List<String> infos : lockInfos.values()) {
                for (String lockToken : infos) {
                    types.add(getLockType(lockToken));
                }
            }
        }

        return types;
    }

    /**
     * Returns the last part of a path, so the last name. This method supports expanded form names in the path, so the
     * last slash detection will be properly handled.
     *
     * @param path the path to get the name from
     * @return the name of the leaf in the path.
     */
    public static String getNameFromPath(String path) {
        String[] pathNames = splitJCRPath(path);
        return pathNames[pathNames.length - 1];
    }

    public static List<JCRNodeWrapper> getNodes(JCRNodeWrapper node, String type) {
        return getNodes(node, type, 0);
    }

    public static List<JCRNodeWrapper> getNodes(JCRNodeWrapper node, String type, int limit) {
        try {
            List<JCRNodeWrapper> res = new ArrayList<JCRNodeWrapper>();
            NodeIterator ni = node.getNodes();
            while (ni.hasNext()) {
                if (limit > 0 && res.size() == limit) {
                    break;
                }
                JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
                if (StringUtils.isEmpty(type) || child.isNodeType(type)) {
                    res.add(child);
                }
            }
            return res;
        } catch (RepositoryException e) {
            logger.error("Error while retrieving nodes", e);
        }
        return new LinkedList<JCRNodeWrapper>();
    }

    /**
     * Little utility method to retrieve or create a path, building it if necessary. For example let's say that we want
     * to get or create the path from a parentNode : messages/inbox . We can simply pass the parent node, the session
     * and the path to check, and it will either retrieve it if it exists, or create it if it doesn't.
     * <p/>
     * Please note that this method also checks out the parent nodes.
     *
     * @param session
     * @param parentNode   the parent node in from which we want to retrieve the relative path.
     * @param path         the path to retrieve or create. Note that this path MUST be relative
     * @param pathNodeType the type to use for the intermediary nodes (and the last path too !) if they need to be
     *                     created. Usually you'll want to use Constants.JAHIANT_CONTENTLIST here.
     * @return the leaf that is equivalent to the lowest path value.
     * @throws RepositoryException occurs if there is any problem accessing content or creating the nodes.
     */
    public static JCRNodeWrapper getOrAddPath(JCRSessionWrapper session, JCRNodeWrapper parentNode, String path, String pathNodeType) throws RepositoryException {
        String[] subPaths = splitJCRPath(path);
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

    /**
     * A small utility method to retrieve the parent path of a path that contains expanded form names.
     *
     * @param path the path for which we want to retrieve the parent path.
     * @return the parent path including all names in expanded or qualified form.
     */
    public static String getParentJCRPath(String path) {
        String[] pathNames = splitJCRPath(path);
        StringBuilder parentPath = new StringBuilder();
        // if we are dealing with an absolute path, we add the initial separator
        if (path.startsWith("/")) {
            parentPath.append("/");
        }
        for (int i = 0; i < pathNames.length - 1; i++) {
            parentPath.append(pathNames[i]);
            if (i < pathNames.length - 2) {
                parentPath.append("/");
            }
        }
        return parentPath.toString();
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

    public static JCRNodeWrapper getPathFolder(JCRNodeWrapper root, String name, String options, String nodeType) throws RepositoryException {
        JCRNodeWrapper result = root;
        if (options.contains("initials")) {
            String s = "" + Character.toUpperCase(name.charAt(0));
            if (!result.hasNode(s)) {
                result = result.addNode(s, nodeType);
            } else {
                result = result.getNode(s);
            }
        }
        return result;
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

    public static String getSystemSitePath() {
        return "/sites/" + JahiaSitesService.SYSTEM_SITE_KEY;
    }

    /**
     * Returns an object value that corresponds to the provided JCR property value depending on its type.
     *
     * @param propertyValue the JCR property value to be converted
     * @return the object value
     * @throws RepositoryException  in case of a conversion error
     * @throws ValueFormatException in case of a conversion error
     */
    public static Object getValue(Value propertyValue) throws ValueFormatException,
            RepositoryException {
        Object value = propertyValue.getString();
        switch (propertyValue.getType()) {
            case PropertyType.BOOLEAN:
                value = Boolean.valueOf(propertyValue.getBoolean());
                break;
            case PropertyType.DATE:
                value = propertyValue.getDate();
                break;
            case PropertyType.DECIMAL:
            case PropertyType.LONG:
                value = Long.valueOf(propertyValue.getDecimal().longValue());
                break;
            case PropertyType.DOUBLE:
                value = Double.valueOf(propertyValue.getDouble());
                break;
        }
        return value;
    }

    /**
     * Used by portlet backends to determine if a user is part of a specific permissionName on a node specified by it's
     * UUID
     *
     * @param workspaceName  the name of the workspace in which we load the node from.
     * @param permissionName
     * @param nodeUUID
     * @return
     */
    public static boolean hasPermission(String workspaceName, final String permissionName, final String nodeUUID) {
        try {
            JCRSessionWrapper session = JCRTemplate.getInstance().getSessionFactory().getCurrentUserSession(workspaceName);
            JCRNodeWrapper node = session.getNodeByIdentifier(nodeUUID);
            return node.hasPermission(permissionName);
        } catch (Exception e) {
            logger.error("Error while checking permission " + permissionName + " for node UUID " + nodeUUID, e);
        }
        return false;
    }

    /**
     * Performs import of JCR data using provided skeleton locations. This method is used when a new virtual site or a new user is created.
     *
     * @param skeletonLocations the (pattern-based) location to search for resources. Multiple locations can be provided separated by comma (or any
     *                          delimiter, defined in {@link org.springframework.context.ConfigurableApplicationContext#CONFIG_LOCATION_DELIMITERS} )
     * @param targetPath        target JCR path to perform import into
     * @param session           the current JCR session
     * @throws IOException                    in case of skeleton lookup error
     * @throws InvalidSerializedDataException import related exception
     * @throws RepositoryException            general JCR exception
     */
    public static void importSkeletons(String skeletonLocations, String targetPath,
                                       JCRSessionWrapper session) throws IOException, InvalidSerializedDataException,
            RepositoryException {
        importSkeletons(skeletonLocations, targetPath, session, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, null);
    }

    /**
     * Performs import of JCR data using provided skeleton locations. This method is used when a new virtual site or a new user is created.
     *
     * @param skeletonLocations  the (pattern-based) location to search for resources. Multiple locations can be provided separated by comma (or any
     *                           delimiter, defined in {@link org.springframework.context.ConfigurableApplicationContext#CONFIG_LOCATION_DELIMITERS} )
     * @param targetPath         target JCR path to perform import into
     * @param session            the current JCR session
     * @param importUUIDBehavior the {@link javax.jcr.ImportUUIDBehavior} to use during import
     * @param replacements
     * @throws IOException                    in case of skeleton lookup error
     * @throws InvalidSerializedDataException import related exception
     * @throws RepositoryException            general JCR exception
     */
    public static void importSkeletons(String skeletonLocations, String targetPath,
                                       JCRSessionWrapper session, int importUUIDBehavior, Map<String, String> replacements) throws IOException, InvalidSerializedDataException,
            RepositoryException {
        for (Resource resource : SpringContextSingleton.getInstance().getResources(
                skeletonLocations)) {
            logger.info("Importing data using skeleton [{}]", resource);
            InputStream is = null;
            try {
                is = resource.getInputStream();
                session.importXML(targetPath, is, importUUIDBehavior, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE, replacements, null);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }

    /**
     * Performs import of JCR data using provided skeleton locations. This method is used when a new virtual site or a new user is created.
     *
     * @param skeletonLocations the (pattern-based) location to search for resources. Multiple locations can be provided separated by comma (or any
     *                          delimiter, defined in {@link org.springframework.context.ConfigurableApplicationContext#CONFIG_LOCATION_DELIMITERS} )
     * @param targetPath        target JCR path to perform import into
     * @param session           the current JCR session
     * @param replacements
     * @throws IOException                    in case of skeleton lookup error
     * @throws InvalidSerializedDataException import related exception
     * @throws RepositoryException            general JCR exception
     */
    public static void importSkeletons(String skeletonLocations, String targetPath,
                                       JCRSessionWrapper session, Map<String, String> replacements) throws IOException, InvalidSerializedDataException,
            RepositoryException {
        importSkeletons(skeletonLocations, targetPath, session, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, replacements);
    }

    public static boolean isADisplayableNode(JCRNodeWrapper node, RenderContext context) {
        Template template = null;
        JCRNodeWrapper currentNode = node;
        try {
            template = RenderService.getInstance().resolveTemplate(new org.jahia.services.render.Resource(currentNode,
                    "html", null, org.jahia.services.render.Resource.CONFIGURATION_PAGE), context);
            return template != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the specified mime type belongs to one of the specified groups (like pdf,word,openoffice, etc.).
     *
     * @param mimeType      the mime type to be checked
     * @param mimeTypeGroup the group (or multiple groups, separated by comma) the specified mime type should belong to
     * @return if the specified mime type belongs to one of the specified groups (like pdf,word,openoffice, etc.)
     */
    public static boolean isMimeTypeGroup(String mimeType, String mimeTypeGroup) {
        return isMimeTypeGroup(mimeType, StringUtils.split(mimeTypeGroup, ", ;|"));
    }

    /**
     * Checks if the specified mime type belongs to one of the specified groups (like pdf,word,openoffice, etc.).
     *
     * @param mimeType       the mime type to be checked
     * @param mimeTypeGroups the groups the specified mime type should belong to
     * @return if the specified mime type belongs to one of the specified groups (like pdf,word,openoffice, etc.)
     */
    public static boolean isMimeTypeGroup(String mimeType, String... mimeTypeGroups) {
        if (mimeType == null) {
            return false;
        }

        boolean found = false;
        for (String grp : mimeTypeGroups) {
            List<String> mimeTypes = getInstance().getMimeTypes().get(grp);
            if (mimeTypes == null) {
                continue;
            }
            for (String mime : mimeTypes) {
                if (mime.contains("*")) {
                    found = Pattern.matches(
                            StringUtils.replace(StringUtils.replace(mime, ".", "\\."), "*", ".*"),
                            mimeType);
                } else {
                    found = mime.equals(mimeType);
                }
                if (found) {
                    break;
                }
            }
            if (found) {
                break;
            }
        }

        return found;
    }

    /**
     * Checks if the specified language is marked as invalid for displaying.
     *
     * @param node
     *            the node to be checked
     * @param languageCode
     *            language code to be tested
     * @return <code>true</code> if the specified language is in the list of languages marked as invalid for displaying; <code>false</code>
     *         otherwise.
     * @throws RepositoryException
     *             in case of an exception
     */
    public static boolean isLanguageInvalid(Node node, String languageCode)
            throws RepositoryException {
        boolean invalid = false;
        if (node.hasProperty("j:invalidLanguages")) {
            final Value[] values = node.getProperty("j:invalidLanguages").getValues();
            for (Value value : values) {
                if (value.getString().equals(languageCode)) {
                    invalid = true;
                    break;
                }
            }
        }

        return invalid;
    }

    /**
     * Returns <code>true</code> if the node is locked and cannot be edited by current user.
     *
     * @param node the node to be tested
     * @return <code>true</code> if the node is locked and cannot be edited by current user
     * @throws RepositoryException in case of a JCR error
     */
    public static boolean isLockedAndCannotBeEdited(JCRNodeWrapper node) throws RepositoryException {
        if (node == null) {
            return false;
        }

        String username = node.getSession().getUser().getName();
        String lockOwner = node.getLockOwner();
        boolean isLocked = node.isLocked() && (lockOwner == null || !lockOwner.equals(username));
        try {
            if (!isLocked && node.hasProperty("j:lockTypes")) {
                Value[] values = node.getProperty("j:lockTypes").getValues();
                for (Value value : values) {
                    if (!value.getString().startsWith(username)) {
                        isLocked = true;
                        break;
                    }
                }
            }
        } catch (PathNotFoundException e) {
            logger.debug("concurrency issue lock is not present anymore");
        }

        return isLocked;
    }

    /**
     * Returns <code>true</code> if the provided node matches one of the specified node types.
     *
     * @param node  the node to be tested
     * @param types an array of node types to be matched.
     * @return <code>true</code> if the provided node matches one of the specified node types
     * @throws RepositoryException in case of a JCR error
     */
    public static boolean isNodeType(JCRNodeWrapper node, Iterable<String> types)
            throws RepositoryException {
        if (node == null || types == null) {
            return false;
        }
        boolean matches = false;
        for (String matchType : types) {
            if (node.isNodeType(matchType)) {
                matches = true;
                break;
            }
        }

        return matches;
    }

    /**
     * Returns <code>true</code> if the provided node matches one of the specified node types.
     *
     * @param node  the node to be tested
     * @param types an array of node types to be matched.
     * @return <code>true</code> if the provided node matches one of the specified node types
     * @throws RepositoryException in case of a JCR error
     */
    public static boolean isNodeType(JCRNodeWrapper node, String... types)
            throws RepositoryException {
        if (node == null || types == null) {
            return false;
        }
        boolean matches = false;
        for (String matchType : types) {
            if (node.isNodeType(matchType)) {
                matches = true;
                break;
            }
        }

        return matches;
    }

    /**
     * Returns <code>true</code> if the provided node matches the specified node type (multiple node types can be specified, separated by a
     * comma).
     *
     * @param node the node to be tested
     * @param type the node type to be matched. Multiple node types can be specified, separated by a comma.
     * @return <code>true</code> if the provided node matches the specified node type (multiple node types can be specified, separated by a
     *         comma).
     * @throws RepositoryException in case of a JCR error
     */
    public static boolean isNodeType(JCRNodeWrapper node, String type) throws RepositoryException {
        if (node == null || StringUtils.isEmpty(type)) {
            return false;
        }
        boolean matches = false;
        if (type.contains(",")) {
            String[] types = Patterns.COMMA.split(type);
            for (String matchType : types) {
                if (node.isNodeType(matchType)) {
                    matches = true;
                    break;
                }
            }
        } else {
            matches = node.isNodeType(type);
        }

        return matches;
    }

    /**
     * Returns <code>true</code> if the provided UUID string does not seem like a valid Jackrabbit node UUID. In such a case it comes from a
     * different provide, like VFS.
     *
     * @param uuid the UUID string to check
     * @return code>true</code> if the provided UUID string does not seem like a valid Jackrabbit node UUID. In such a case it comes from a
     *         different provide, like VFS
     * @deprecated without any replacement
     */
    @Deprecated
    public static boolean isNotJcrUuid(String uuid) {
        return StringUtils.isEmpty(uuid) || uuid.contains("/");
    }

    public static boolean isValidFilename(String name) {
        return (!name.startsWith(" ") && !name.endsWith(" ") && name.matches("([^\\*:/\\\\<>|?\"])*"));
    }


    /**
     * Validates if the specified name is a valid JCR workspace (either <code>default</code> or <code>live</code>).
     *
     * @param workspace the workspace name to check
     * @return <code>true</code> if the specified name is a valid JCR workspace (either <code>default</code> or <code>live</code>);
     *         otherwise returns <code>false</code>
     */
    public static final boolean isValidWorkspace(String workspace) {
        return isValidWorkspace(workspace, false);
    }

    /**
     * Validates if the specified name is a valid JCR workspace (either <code>default</code> or <code>live</code>).
     *
     * @param workspace  the workspace name to check
     * @param allowBlank set to true if the workspace name is allowed to be null or empty
     * @return <code>true</code> if the specified name is a valid JCR workspace (either <code>default</code> or <code>live</code>);
     *         otherwise returns <code>false</code>
     */
    public static final boolean isValidWorkspace(String workspace, boolean allowBlank) {
        return StringUtils.isEmpty(workspace) ? allowBlank : EDIT_WORKSPACE.equals(workspace)
                || LIVE_WORKSPACE.equals(workspace);
    }

    /**
     * Small utility method to help with proper namespace registration in all JCR providers.
     *
     * @param session
     * @param prefix
     * @param uri
     * @throws RepositoryException in case of JCR-related errors
     */
    public static void registerNamespace(Session session, String prefix, String uri) throws RepositoryException {
        NamespaceRegistry namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
        Set<String> prefixes = ImmutableSet.copyOf(namespaceRegistry.getPrefixes());
        if (!prefixes.contains(prefix)) {
            namespaceRegistry.registerNamespace(prefix, uri);
            session.setNamespacePrefix(prefix, uri);
        }

    }

    public static String replaceColon(String name) {
        return name != null ? StringUtils.replace(name, ":", "_") : name;
    }


    /**
     * Calculates the size of a {@link RangeIterator}. If the iterator's size is
     * already known (i.e., {@link RangeIterator#getSize()} returns a value greater than 0),
     * it returns this size directly. Otherwise, it iterates through the {@link RangeIterator}
     * to count the number of elements.
     * Iterator Consumption: if the iterator does not know its size upfront,
     * the method will consume the iterator to count its elements.
     * This is an important detail because after this method is called,
     * the iterator cannot be reused to iterate through the elements
     *
     * When an implementation necessitates knowing the size of data,
     * it is advised to perform a query on the JCR to determine the count of items beneath a node, rather than relying on JCRContentUtils.size
     * @param iterator the item iterator to check the size
     * @return the number of elements in the provided iterator
     */
    @Deprecated
    public static long size(RangeIterator iterator) {
        long size = iterator.getSize();
        if (size <= 0) {
            logger.debug("The size of the iterator is not known, it will be consumed to calculate the size");
            size = 0;
            while (iterator.hasNext()) {
                size++;
                iterator.next();
            }
        }

        return size;
    }

    /**
     * Utility method to split a JCR path into names. Note that this method supports expanded name notation (using
     * URIs), such as {http://www.jcp.org/jcr/1.0}read, as it is tricky to split simply using the "/" character when
     * URIs are present.
     *
     * @param path a relative or absolute path, with node names in qualified or expanded form
     * @return an array of String instances that contain the node names in order of the path.
     */
    public static String[] splitJCRPath(String path) {
        List<String> result = new ArrayList<String>();
        int pathPos = 0;

        if (path.startsWith("/")) {
            pathPos++;
        }

        int nextSlashPos = -1;
        do {
            StringBuilder currentName = new StringBuilder();
            if (path.indexOf('{', pathPos) == pathPos) {
                int endingBracketPos = path.indexOf('}', pathPos + 1);
                currentName.append(path.substring(pathPos, endingBracketPos + 1));
                pathPos = endingBracketPos + 1;
            }
            nextSlashPos = path.indexOf('/', pathPos);
            if (nextSlashPos > -1) {
                currentName.append(path.substring(pathPos, nextSlashPos));
                pathPos = nextSlashPos + 1;
            } else {
                currentName.append(path.substring(pathPos));
            }
            result.add(currentName.toString());
        } while (nextSlashPos > -1);

        return result.toArray(new String[result.size()]);
    }

    /**
     * Convert a path string to encoded path Strings in XPATH queries
     *
     * @param str Any string.
     * @return A valid string path suitable for use in XPATH queries
     */
    public static String stringToJCRPathExp(String str) {
        return ISO9075.encodePath(str);
    }

    /**
     * Convert a string to a JCR search expression literal, suitable for use in
     * jcr:contains() (inside XPath) or contains (SQL2). The characters - and " have
     * special meaning, and may be escaped with a backslash to obtain their
     * literal value. See JSR-283 spec v2.0, Sec. 6.7.19.
     *
     * @param str Any string.
     * @return A valid string literal suitable for use in
     * JCR contains clauses, including enclosing quotes.
     */
    public static String stringToJCRSearchExp(String str) {
        // escape single double quotes and \ except if preceded by a \

        if (str == null) {
            throw new IllegalArgumentException("Must pass a valid String");
        }

        str = str.trim();
        final int length = str.length();

        // quickly return
        if (length == 0) {
            return "''";
        }

        // if we don't have a double quote, just return the given string as a query literal
        int nextDoubleQuote = str.indexOf('"');
        if (nextDoubleQuote < 0) {
            return stringToQueryLiteral(str);
        }

        StringBuilder stringBuilder = new StringBuilder(length + 10);

        // copy string up to first double quote
        stringBuilder.append(str.substring(0, nextDoubleQuote));

        char previousChar = 0;
        boolean hasStartingDoubleQuote = false;
        for (int i = nextDoubleQuote; i < length; i++) {
            char c = str.charAt(i);
            if (c == '"') {
                // only check if we have another double quote later in which case we should not escape if we haven't seen one already
                if (!hasStartingDoubleQuote) {
                    nextDoubleQuote = str.indexOf('"', i + 1);
                    if (nextDoubleQuote < 0) {
                        // only escape if we don't have a preceding \ and we don't have a starting double quote
                        if (previousChar != '\\') {
                            stringBuilder.append('\\');
                        }
                        // and finish the string since we don't have anything left to escape
                        stringBuilder.append(str.substring(i, length));
                        break;
                    }
                    hasStartingDoubleQuote = true;
                } else {
                    hasStartingDoubleQuote = false;
                }
            }


            stringBuilder.append(c);
            previousChar = c;
        }
        return stringToQueryLiteral(stringBuilder.toString());
    }

    /**
     * Convert a string to a literal, suitable for inclusion
     * in a query. See JSR-283 spec v2.0, Sec. 4.6.6.19.
     *
     * @param str Any string.
     * @return A valid JCR query string literal, including enclosing quotes.
     */
    public static String stringToQueryLiteral(String str) {
        // Single quotes needed for jcr:contains()
        return "'" + Patterns.SINGLE_QUOTE.matcher(str).replaceAll("''") + "'";
    }

    /**
     * Decode an encoded JCR local name encoded with the {@link #escapeLocalNodeName(String)} method
     *
     * @param encodedLocalName the node name to unescape
     * @return the unescaped name
     */
    public static String unescapeLocalNodeName(final String encodedLocalName) {
        return encodedLocalName != null && encodedLocalName.indexOf('%') != -1 ? Text
                .unescapeIllegalJcrChars(encodedLocalName) : encodedLocalName;
    }

    /**
     * Generates the node name by using a configurable NameGenerationHelper implementation.
     *
     * @return a node name generated by configurable helper service
     */
    public String generateNodeName(JCRNodeWrapper parent, String nodeType) {
        return getNameGenerationHelper().generatNodeName(parent, nodeType);
    }

    /**
     * Generates the node name by using a configurable NameGenerationHelper implementation.
     *
     * @return a node name generated by configurable helper service
     */
    public String generateNodeName(JCRNodeWrapper parent, String defaultLanguage, ExtendedNodeType nodeType, String targetName) {
        return getNameGenerationHelper().generatNodeName(parent, defaultLanguage, nodeType, targetName);
    }

    /**
     * Returns a mapping between file extensions and corresponding icons.
     *
     * @return a mapping between file extensions and corresponding icons
     */
    public Map<String, String> getFileExtensionIcons() {
        return fileExtensionIcons;
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

    public NameGenerationHelper getNameGenerationHelper() {
        return nameGenerationHelper;
    }

    public Set<String> getUnsupportedMarkForDeletionNodeTypes() {
        return unsupportedMarkForDeletionNodeTypes;
    }

    public void setNameGenerationHelper(NameGenerationHelper nameGenerationHelper) {
        this.nameGenerationHelper = nameGenerationHelper;
    }

    public void setUnsupportedMarkForDeletionNodeTypes(Set<String> unsupportedMarkForDeletionNodeTypes) {
        this.unsupportedMarkForDeletionNodeTypes = unsupportedMarkForDeletionNodeTypes;
    }

    public Pattern getHandleFallbackLocaleForPathPattern() {
        return handleFallbackLocaleForPath;
    }

    public void setHandleFallbackLocaleForPath(String handleFallbackLocaleForPath) {
        this.handleFallbackLocaleForPath = StringUtils.isNotEmpty(handleFallbackLocaleForPath) ? Pattern
                .compile(handleFallbackLocaleForPath) : null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<Map<String, Object>> getRolesForNode(JCRNodeWrapper node, boolean includeInherited, boolean expandGroups, String roles, int limit,
                                                            boolean latestFirst) {
        List<Map<String, Object>> results = new LinkedList<Map<String, Object>>();
        Map<String, List<String[]>> entries = node.getAclEntries();
        if (latestFirst) {
            entries = reverse(entries);
        }
        String siteKey = null;
        List<String> rolesList = Arrays.asList(StringUtils.splitByWholeSeparator(roles, null));
        JahiaUserManagerService userService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JahiaGroupManagerService groupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();

        for (Map.Entry<String, List<String[]>> entry : entries.entrySet()) {
            Map<String, Object> m = new HashMap<String, Object>();
            String entryKey = entry.getKey();
            if (siteKey == null) {
                try {
                    JCRSiteNode resolveSite = node.getResolveSite();
                    siteKey = resolveSite != null ? resolveSite.getSiteKey() : null;
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (entryKey.startsWith("u:")) {
                String name = StringUtils.substringAfter(entryKey, "u:");
                JCRUserNode u = userService.lookupUser(name, siteKey);
                if (u == null) {
                    logger.warn("User {} cannot be found. Skipping.", name);
                    continue;
                }
                m.put("principalType", "user");
                m.put("principal", u);
            } else if (entryKey.startsWith("g:")) {
                String name = StringUtils.substringAfter(entryKey, "g:");
                JCRGroupNode g = groupService.lookupGroup(siteKey, name);
                if (g == null) {
                    g = groupService.lookupGroup(null, name);
                }
                if (g == null) {
                    logger.warn("Group {} cannot be found. Skipping.", name);
                    continue;
                }
                m.put("principalType", "group");
                m.put("principal", g);
            }

            for (String[] details : entry.getValue()) {
                if (details[1].equals("GRANT")) {
                    if (!rolesList.isEmpty()) {
                        if (!rolesList.contains(details[2])) {
                            continue;
                        }
                    }

                    if (!includeInherited) {
                        if (!details[0].equals(node.getPath())) {
                            continue;
                        }
                    }
                    if (!m.containsKey("roles")) {
                        m.put("roles", new LinkedList<String>());
                        results.add(m);
                    }
                    ((List) m.get("roles")).add(details[2]);
                }
            }

            if (limit > 0 && results.size() >= limit) {
                break;
            }
        }
        if (expandGroups) {
            List<Map<String, Object>> expandedResults = new LinkedList<Map<String, Object>>();
            for (Map<String, Object> result : results) {
                if (result.get("principalType").equals("group")) {
                    JCRGroupNode g = (JCRGroupNode) result.get("principal");
                    Set<JCRUserNode> principals = g.getRecursiveUserMembers();
                    for (JCRUserNode user : principals) {
                        Map<String, Object> m = new HashMap<String, Object>(result);
                        m.put("principalType", "user");
                        m.put("principal", user);
                        expandedResults.add(m);
                    }
                } else {
                    expandedResults.add(result);
                }
            }
            results = expandedResults;
        }

        return results;
    }

    public static <T> Map<String, T> reverse(Map<String, T> orderedMap) {
        if (orderedMap == null || orderedMap.isEmpty()) {
            return orderedMap;
        }
        LinkedHashMap<String, T> reversed = new LinkedHashMap<String, T>(orderedMap.size());
        ListIterator<String> li = new LinkedList<String>(orderedMap.keySet())
                .listIterator(orderedMap.size());
        while (li.hasPrevious()) {
            String key = li.previous();
            reversed.put(key, orderedMap.get(key));
        }
        return reversed;
    }

    /**
     * Encode a string to be used in a JCR SQL2 query by "escaping" the single quotes.
     *
     * @param s the string to be encoded
     * @return encoded string to be used in a JCR SQL2 query
     */
    public static String sqlEncode(String s) {
        return s != null && s.indexOf('\'') != -1 ? Patterns.SINGLE_QUOTE.matcher(s).replaceAll("''") : s;
    }

    /**
     * Returns the first parent of the specified node, which has the ACL inheritance broken. If not found, null<code>null</code> is
     * returned.
     *
     * @param node the node to search parent for
     * @return the first parent of the specified node, which has the ACL inheritance broken. If not found, null<code>null</code> is returned
     * @throws RepositoryException in case of JCR errors
     */
    public static JCRNodeWrapper getParentWithAclInheritanceBroken(JCRNodeWrapper node)
            throws RepositoryException {
        JCRNodeWrapper found = null;
        JCRNodeWrapper parent = node;
        try {
            while (true) {
                parent = parent.getParent();
                if (parent.getAclInheritanceBreak()) {
                    found = parent;
                    break;
                }
            }
        } catch (ItemNotFoundException e) {
            // reached the root node
        }

        return found;
    }

    /**
     * Detects the mime-type for the specified file name, based on its extension (uses mime types, configured in the web.xml deployment
     * descriptor).
     *
     * @param fileName the name of the file to detect mime type for
     * @return the mime-type for the specified file name, based on its extension (uses mime types, configured in the web.xml deployment
     *         descriptor)
     */
    public static String getMimeType(String fileName) {
        return fileName != null ? getInstance().servletContext.getMimeType(fileName.toLowerCase()) : null;
    }

    /**
     * Detects the mime-type for the specified file name, based on its extension (uses mime types, configured in the web.xml deployment
     * descriptor). If the type cannot be detected, the provided fallback mime type is returned.
     *
     * @param fileName         the name of the file to detect mime type for
     * @param fallbackMimeType the fallback mime-type to use if the type cannot be detected
     * @return the mime-type for the specified file name, based on its extension (uses mime types, configured in the web.xml deployment
     *         descriptor). If the type cannot be detected, the provided fallback mime type is returned.
     */
    public static String getMimeType(String fileName, String fallbackMimeType) {
        return StringUtils.defaultIfEmpty(getMimeType(fileName), fallbackMimeType);
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Returns the /files/private folder for the current session user, creating it if it does not exist yet.
     *
     * @param session current JCR session
     * @return the JCR node, which corresponds to the /files/private folder for the current user
     * @throws RepositoryException in case of an error
     */
    public JCRNodeWrapper getUserPrivateFilesFolder(JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper privateFilesFolder = getDefaultUserFolder(session, "/files/private", false);
        if (privateFilesFolder.isNew()) {
            privateFilesFolder.grantRoles("u:" + session.getUser().getName(), Collections.singleton("owner"));
            privateFilesFolder.setAclInheritanceBreak(true);
            session.save();
        }

        return privateFilesFolder;
    }

    public JCRNodeWrapper getDefaultUserFolder(JCRSessionWrapper session, String path) throws RepositoryException {
        return getDefaultUserFolder(session, path, true);
    }

    /**
     * Retrieves the default user folder found at the specified path, creating it (and all intermediate folders) if needed, saving the session if requested.
     * @param session the session with which to access the JCR data
     * @param path the path of the default user folder to retrieve, if path is empty or <code>null</code> the user's node is returned
     * @param saveIfCreate <code>true</code> if we want the session to be immediately saved, <code>false</code> if the client code will save the session to commit the changes
     * @return the JCR node associated with the requested default user folder
     * @throws RepositoryException in case of JCR-related errors
     */
    public JCRNodeWrapper getDefaultUserFolder(JCRSessionWrapper session, String path, boolean saveIfCreate) throws RepositoryException {
        // if path is null or empty, return the user's node
        if(StringUtils.isEmpty(path)) {
            return session.getUserNode();
        }

        // make it possible to use relative paths without '/' prefix
        if(!path.startsWith("/")) {
            path = "/" + path;
        }

        // first check that we know this default user folder
        final String primaryNodeTypeName = defaultUserFolderTypes.get(path);
        if(primaryNodeTypeName == null) {
            throw new IllegalArgumentException("Unknown default user folder: " + path + ". Known default user folders are: " + defaultUserFolderTypes);
        }

        final String userPath = session.getUserNode().getPath();
        if (!session.itemExists(userPath + path)) {
            final String name = StringUtils.substringAfterLast(path, "/");
            final JCRNodeWrapper parentUserFolder = getDefaultUserFolder(session, StringUtils.substringBeforeLast(path, "/"), false);
            final JCRNodeWrapper userFolder = parentUserFolder.addNode(name, primaryNodeTypeName);

            if(saveIfCreate) {
                session.save();
            }

            return userFolder;
        }

        return session.getNode(userPath + path);
    }

    /**
     * Check if user is member of the given group
     * @param siteKey the site key
     * @param groupName the group name
     * @param userName the user name
     * @param userRealm hte user realm
     * @param isRoot is the user root ?
     * @return true in case the user is member of the group
     */
    public static boolean isUserMemberOfGroup(String siteKey, String groupName, String userName, String userRealm, boolean isRoot) {
        return JahiaGroupManagerService.GUEST_GROUPNAME.equals(groupName) ||
                JahiaGroupManagerService.USERS_GROUPNAME.equals(groupName) ||
                (JahiaGroupManagerService.SITE_USERS_GROUPNAME.equals(groupName) && (userRealm == null || userRealm.equals(siteKey))) ||
                (isRoot && JahiaGroupManagerService.POWERFUL_GROUPS.contains(groupName)) ||
                JahiaGroupManagerService.getInstance().isMember(userName, userRealm, groupName, siteKey);
    }

    /**
     * this method checks if the node type is versionable from a jahia property
     *
     * @param node                   the node we want to version
     * @param versionedTypes         the jahia property with types to version
     * @param excludedVersionedTypes the jahia property with types to exclude from versionedTypes
     * @return true if the node can be versioned, false if it can't
     * @throws RepositoryException
     */
    public static boolean needVersion(JCRNodeWrapper node, Set<String> versionedTypes, Set<String> excludedVersionedTypes) throws RepositoryException {
        boolean isVersionedType = Streams.stream(versionedTypes.stream()).anyMatch(node::isNodeType);
        if (isVersionedType) {
            // Check exclusion only for when node is a versionable type
            return !Streams.stream(excludedVersionedTypes.stream()).anyMatch(node::isNodeType);
        } else if (node.isNodeType(Constants.MIX_VERSIONABLE) || node.isNodeType(Constants.MIX_SIMPLEVERSIONABLE)) {
            JCRNodeWrapper parent = node.getParent();
            return Streams.stream(versionedTypes.stream()).anyMatch(parent::isNodeType)
                    && !Streams.stream(excludedVersionedTypes.stream()).anyMatch(node::isNodeType);
        }
        return false;
    }

    /**
     * this method splits a String with a separator and put all its elements in a Set
     * @param input String containing different inputs with a separator
     * @param separator string specifying the separator to split with
     * @return a Set with the inputs
     */
    public static Set<String> splitAndUnify(String input, String separator) {
        String[] tokens = StringUtils.split(input, separator);
        if (tokens == null || tokens.length == 0) {
            return Collections.emptySet();
        } else {
            Set<String> result = new LinkedHashSet<String>();
            result.addAll(Arrays.asList(tokens));
            return result;
        }
    }
}
