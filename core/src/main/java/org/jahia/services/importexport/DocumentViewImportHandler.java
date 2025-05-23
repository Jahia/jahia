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
package org.jahia.services.importexport;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import static org.jahia.services.importexport.ImportExportBaseService.DEFAULT_PROPERTIES_TO_IGNORE;

/**
 * SAX handler that performs import of the JCR content, provided in a document format.
 * User: toto
 * Date: 11 févr. 2008
 * Time: 16:38:38
 */
public class DocumentViewImportHandler extends BaseDocumentViewHandler implements ImportUUIDBehavior {
    public static final int IMPORT_UUID_COLLISION_MOVE_EXISTING = 4;
    public static final int ROOT_BEHAVIOUR_IGNORE = 0;
    public static final int ROOT_BEHAVIOUR_REPLACE = 1;
    public static final int ROOT_BEHAVIOUR_RENAME = 2;
    private static Logger logger = LoggerFactory.getLogger(DocumentViewImportHandler.class);
    private int rootBehavior = ROOT_BEHAVIOUR_REPLACE;

    private int maxBatch = SettingsBean.getInstance().getImportMaxBatch();
    private int batchCount = 0;

    private Locator documentLocator;
    private Resource archive;
    private NoCloseZipInputStream zis;
    private ZipEntry nextEntry;
    private List<String> fileList = new ArrayList<String>();
    private String baseFilesPath = "/content";
    private Stack<JCRNodeWrapper> nodes = new Stack<JCRNodeWrapper>();

    private JCRSiteNode site;
    private List<String> dependencies;

    private Map<String, String> uuidMapping;
    private Map<String, String> pathMapping;
    private Map<String, List<String>> references = new HashMap<String, List<String>>();

    private Map<Pattern, String> replacements = Collections.emptyMap();
    private List<AttributeProcessor> attributeProcessors = Collections.emptyList();

    private Set<String> propertiesToSkip = Collections.emptySet();

    private String currentFilePath = null;

    private String ignorePath = null;

    private int error = 0;

    private boolean resolveReferenceAtEnd = true;

    private int uuidBehavior = ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW;

    private Map<String, String> placeHoldersMap = new HashMap<String, String>();

    private boolean importUserGeneratedContent = false;
    private int ugcLevel = 0;
    private boolean cleanPreviousLiveImport = false;

    private static final List<String> NO_SUB_NODES_IMPORT = Arrays.asList("jnt:importDropBox", "jnt:referencesKeeper");
    private static final List<String> NO_LIVE_CLEANUP_FOR_PROPS = Arrays.asList("jcr:lastModified", "jcr:lastModifiedBy");
    // List of node types that should not be updated if they already exist during import.
    private List<String> noUpdateTypes = Arrays.asList("jnt:virtualsitesFolder", "jnt:usersFolder", "jnt:groupsFolder", "jnt:user", "jnt:group");

    private Set<String> uuidsSet = new LinkedHashSet<String>();

    private boolean expandImportedFilesOnDisk = SettingsBean.getInstance().isExpandImportedFilesOnDisk();
    private String expandedFolder;

    private Set<String> missingDependencies = new HashSet<String>();
    private JCRSessionWrapper liveSession = null;

    public DocumentViewImportHandler(JCRSessionWrapper session, String rootPath) throws IOException {
        this(session, rootPath, null, null);
    }

    @SuppressWarnings("unchecked")
    public DocumentViewImportHandler(JCRSessionWrapper session, String rootPath, Resource archive, List<String> fileList) throws IOException {
        super(session);
        JCRNodeWrapper node = null;
        try {
            this.uuidMapping = session.getUuidMapping();
            this.pathMapping = session.getPathMapping();
            if (rootPath == null) {
                node = (JCRNodeWrapper) session.getRootNode();
            } else {
                node = (JCRNodeWrapper) session.getNode(rootPath);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage() + getLocation(), e);
            throw new IOException();
        }
        nodes.add(node);

        this.archive = archive;

        if (archive != null && !archive.isReadable() && archive instanceof FileSystemResource) {
            expandedFolder = archive.getFile().getPath();
            expandImportedFilesOnDisk = true;
        } else if (expandImportedFilesOnDisk && archive != null) {
            expandedFolder = ImportExportBaseService.getExpandFolder(archive, SettingsBean.getInstance().getExpandImportedFilesOnDiskPath()).getPath();
        }

        this.fileList = fileList;
        setPropertiesToSkip(new HashSet<>((Set<String>) SpringContextSingleton.getBean("DocumentViewImportHandler.propertiesToSkip")));
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (error > 0) {
            error++;
            return;
        }

        batchCount++;
        // do a session.save each maxBatch
        if (batchCount > maxBatch) {
            try {
                ReferencesHelper.resolveCrossReferences(session, references, false);
                session.save(JCRObservationManager.IMPORT);
                batchCount = 0;
                session.refresh(false);
            } catch (CompositeConstraintViolationException e) {
                logger.error("Constraint violation exception", e);
                throw new SAXException("Cannot save batch", e);
            } catch (ConstraintViolationException e) {
                // save on the next node when next node is needed (like content node for files)
                boolean retryToSave = e.getMessage().contains("mandatory child node") && nodes.size() > 1 && e.getMessage().startsWith(nodes.peek().getPath());
                if (!retryToSave) {
                    throw new SAXException("Cannot save batch", e);
                }
            } catch (RepositoryException e) {
                throw new SAXException("Cannot save batch", e);
            }
        }

        if (ugcLevel > 0 || "live".equals(atts.getValue("j:originWS"))) {
            if (importUserGeneratedContent) {
                ugcLevel++;
            } else {
                error++;
                return;
            }
        }

        if (atts.getIndex(ImportExportBaseService.STATIC_MOUNT_POINT_ATTR) > -1) {
            String providerKey = atts.getValue(ImportExportBaseService.STATIC_MOUNT_POINT_ATTR);
            Map<String, JCRStoreProvider> providers = JCRSessionFactory.getInstance().getProviders();
            if (!providers.containsKey(providerKey) || !providers.get(providerKey).isAvailable()) {
                error++;
                return;
            }
        }
        if (atts.getIndex(ImportExportBaseService.DYNAMIC_MOUNT_POINT_ATTR) > -1) {
            try {
                String providerKey = session.getNode(atts.getValue(ImportExportBaseService.DYNAMIC_MOUNT_POINT_ATTR)).getIdentifier();
                Map<String, JCRStoreProvider> providers = JCRSessionFactory.getInstance().getProviders();
                if (!providers.containsKey(providerKey) || !providers.get(providerKey).isAvailable()) {
                    error++;
                    return;
                }
            } catch (RepositoryException e) {
                error++;
                return;
            }
        }

        String decodedLocalName = ISO9075.decode(localName);

        for (Map.Entry<Pattern, String> entry : replacements.entrySet()) {
            decodedLocalName = entry.getKey().matcher(decodedLocalName).replaceAll(entry.getValue());
        }

        String decodedQName = qName.replace(localName, decodedLocalName);

        if (rootBehavior == ROOT_BEHAVIOUR_RENAME && pathes.size() <= 1 && !pathMapping.containsKey(pathes.peek() + "/" + decodedQName)) {
            String newName = JCRContentUtils.findAvailableNodeName(nodes.peek(), decodedQName);
            if (!decodedQName.equals(newName)) {
                pathMapping.put(nodes.peek().getPath() + "/" + decodedQName + "/", nodes.peek().getPath() + "/" + newName + "/");
                decodedQName = newName;
            }
        }

        if (rootBehavior == ROOT_BEHAVIOUR_IGNORE && pathes.size() <= 1) {
            session.getPathMapping().put("/" + decodedQName + "/", nodes.peek().getPath().equals("/") ? "/" : nodes.peek().getPath() + "/");
            pathes.push("");
            return;
        }

        pathes.push(pathes.peek() + "/" + decodedQName);

        try {

            if (ignorePath != null) {
                nodes.push(null);
                return;
            }

            String path;
            if (nodes.peek().getPath().equals("/")) {
                path = "/" + decodedQName;
            } else {
                path = nodes.peek().getPath() + "/" + decodedQName;
            }

            if (pathes.peek().startsWith("/users/") && "jnt:user".equals(atts.getValue("jcr:primaryType"))) {
                Matcher m = Pattern.compile("/users/([^/]+)").matcher(pathes.peek());
                if (m.matches()) {
                    path = JahiaUserManagerService.getInstance().getUserSplittingRule().getPathForUsername(m.group(1));
                }
            }

            String pt = atts.getValue(Constants.JCR_PRIMARYTYPE);

            // Create missing structure for group members
            if ("jnt:member".equals(pt) && nodes.peek().isNodeType("jnt:members") && "j:members".equals(nodes.peek().getName())) {
                String memberRef = atts.getValue("j:member");
                if (memberRef != null) {
                    String referenceValue = getReferenceValue(memberRef, path);
                    JCRNodeWrapper principal;
                    if (referenceValue.startsWith("/")) {
                        for (String key : pathMapping.keySet()) {
                            if (referenceValue.startsWith(key)) {
                                referenceValue = StringUtils.replace(referenceValue, key, pathMapping.get(key));
                                break;
                            }
                        }
                        principal = session.getNode(referenceValue);
                    } else {
                        principal = session.getNodeByIdentifier(memberRef);
                    }
                    JCRGroupNode groupNode = JahiaGroupManagerService.getInstance().lookupGroupByPath(StringUtils.substringBeforeLast(nodes.peek().getPath(), "/"), session);
                    JCRNodeWrapper member = groupNode.addMember(principal);
                    if (member != null) {
                        uuidsSet.add(member.getIdentifier());
                    }
                    nodes.push(member);
                    return;
                }
            }

            if (pathMapping.containsKey(path + "/")) {
                path = StringUtils.substringBeforeLast(pathMapping.get(path + "/"), "/");
                decodedQName = StringUtils.substringAfter(path, "/");
            }

            if (NO_SUB_NODES_IMPORT.contains(pt)) {
                ignorePath = path;
            }
            JCRNodeWrapper child = null;

            boolean isValid = true;
            try {
                child = session.getNode(path);
                if (child.hasPermission("jcr:versionManagement") && child.isVersioned() && !child.isCheckedOut()) {
                    session.checkout(child);
                }
            } catch (PathNotFoundException e) {
                isValid = false;
            }

            if (!importUserGeneratedContent || ugcLevel > 0) {
                // UUID handling, in case a live repository was imported first in the default workspace
                // We need to enforce the UUIDs of the nodes
                String originalUuid = atts.getValue("jcr:uuid");
                String uuid = originalUuid;
                if (uuid != null && uuidMapping.containsKey(uuid)) {
                    uuid = uuidMapping.get(uuid);
                } else if (cleanPreviousLiveImport) {
                    uuid = null;
                }

                if (isValid && cleanPreviousLiveImport && uuid != null) {
                    if (!child.getIdentifier().equals(uuid)) {
                        child.remove();
                        isValid = false;
                    }
                }

                if (!isValid || (child.getDefinition() != null && child.getDefinition().allowsSameNameSiblings())) {
                    isValid = false;
                    if (nodes.peek().hasPermission("jcr:addChildNodes")) {
                        if ("jnt:acl".equals(pt) && !nodes.peek().isNodeType("jmix:accessControlled")) {
                            nodes.peek().addMixin("jmix:accessControlled");
                        }
                        Calendar created = null;
                        String createdBy = null;
                        Calendar lastModified = null;
                        String lastModifiedBy = null;
                        if (!StringUtils.isEmpty(atts.getValue("jcr:created"))) {
                            created = ISO8601.parse(atts.getValue("jcr:created"));
                        }
                        if (!StringUtils.isEmpty(atts.getValue("jcr:lastModified"))) {
                            lastModified = ISO8601.parse(atts.getValue("jcr:lastModified"));
                        }
                        if (!StringUtils.isEmpty(atts.getValue("jcr:createdBy"))) {
                            createdBy = atts.getValue("jcr:createdBy");
                        }
                        if (!StringUtils.isEmpty(atts.getValue("jcr:lastModifiedBy"))) {
                            lastModifiedBy = atts.getValue("jcr:lastModifiedBy");
                        }

                        if (!StringUtils.isEmpty(uuid)) {
                            switch (uuidBehavior) {
                                case IMPORT_UUID_COLLISION_THROW:
                                    try {
                                        JCRNodeWrapper node = session.getNodeByUUID(uuid);
                                        if (node.isNodeType("mix:shareable")) {
                                            // ..
                                        } else {
                                            throw new ItemExistsException(uuid);
                                        }
                                    } catch (ItemNotFoundException e) {
                                    }
                                case IMPORT_UUID_COLLISION_REMOVE_EXISTING:
                                    try {
                                        JCRNodeWrapper node = session.getNodeByUUID(uuid);
                                        // make sure conflicting node is not importTargetNode or an ancestor thereof
                                        if (nodes.peek().getPath().startsWith(node.getPath())) {
                                            String msg = "cannot remove ancestor node";
                                            logger.debug(msg);
                                            throw new ConstraintViolationException(msg);
                                        }
                                        // remove conflicting
                                        node.remove();
                                    } catch (ItemNotFoundException e) {
                                    }
                                    break;
                                case IMPORT_UUID_COLLISION_REPLACE_EXISTING:
                                    throw new UnsupportedOperationException();

                                case IMPORT_UUID_CREATE_NEW:
                                    uuid = null;

                                    break;
                                case IMPORT_UUID_COLLISION_MOVE_EXISTING:
                                    try {
                                        JCRNodeWrapper node = session.getNodeByUUID(uuid);
                                        // make sure conflicting node is not importTargetNode or an ancestor thereof
                                        if (nodes.peek().getPath().startsWith(node.getPath())) {
                                            String msg = "cannot move ancestor node";
                                            logger.debug(msg);
                                            throw new ConstraintViolationException(msg);
                                        }
                                        if (!node.getPath().equals(path)) {
                                            // move conflicting
                                            session.move(node.getPath(), path);
                                            node = session.getNodeByUUID(uuid);
                                        }
                                        child = node;
                                        isValid = true;
                                    } catch (ItemNotFoundException e) {
                                    }
                                    break;
                            }
                        }
                        if (!isValid) {
                            try {
                                session.checkout(nodes.peek());
                            } catch (PathNotFoundException e) {
                                logger.error("Couldn't find parent node " + nodes.peek(), e);
                            }
                            try {
                                checkDependencies(path, pt, atts);
                                child = nodes.peek().addNode(decodedQName, pt, uuid, created, createdBy, lastModified, lastModifiedBy);
                            } catch (ConstraintViolationException e) {
                                if (pathes.size() <= 2 && nodes.peek().getName().equals(decodedQName) && nodes.peek().getPrimaryNodeTypeName().equals(pt)) {
                                    session.getPathMapping().put("/" + decodedQName, nodes.peek().getPath().equals("/") ? "" : nodes.peek().getPath());
                                    return;
                                } else {
                                    throw e;
                                }
                            }
                        }

                        addMixins(child, atts);
                        setAttributes(child, atts);
                        handleBinary(atts, decodedQName, path, child);

                        uuidsSet.add(child.getIdentifier());
                        if (child.isFile() && currentFilePath == null) {
                            currentFilePath = child.getPath();
                        }
                        if (child.isNodeType(Constants.JAHIANT_MOUNTPOINT)) {
                            session.save(JCRObservationManager.IMPORT);
                        }
                    } else {
                        throw new AccessDeniedException("Missing jcr:addChildNodes permission for user " + session.getUser().getName());
                    }
                } else {
                    if (updatesAllowedOnExistingChild(child, atts)) {
                        addMixins(child, atts);
                        setAttributes(child, atts);
                        handleBinary(atts, decodedQName, path, child);
                    }
                    uuidsSet.add(child.getIdentifier());
                }
                if (originalUuid != null) {
                    uuidMapping.put(originalUuid, child.getIdentifier());
                }
                if (nodes.peek().getPrimaryNodeType().hasOrderableChildNodes() && nodes.peek().hasPermission("jcr:write")
                        && !JCRSessionFactory.getInstance().getMountPoints().containsKey(child.getPath())) {
                    nodes.peek().orderBefore(decodedQName, null);
                }
            }
            if (child == null) {
                error++;
            } else {
                nodes.push(child);
            }
        } catch (NoSuchNodeTypeException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Cannot import " + pathes.pop(), e);
            } else {
                logger.warn("Cannot import \"{}\" due to missing node type definition \"{}\"",
                        pathes.pop(), e.getMessage());
            }
            error++;
        } catch (AccessDeniedException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Cannot import " + pathes.pop() + getLocation(), e);
            } else {
                logger.warn("Cannot import \"{}\" due to \"{}\"",
                        pathes.pop(), e.getMessage());
            }
            error++;
        } catch (RepositoryException re) {
            logger.error("Cannot import " + pathes.pop() + getLocation(), re);
            error++;
        } catch (Exception re) {
            throw new SAXException(re);
        }
    }

    private boolean updatesAllowedOnExistingChild(JCRNodeWrapper nodeToCheck, Attributes atts) throws RepositoryException {
        // initial check
        if (!nodeToCheck.hasPermission("jcr:modifyProperties") ||
                !nodeToCheck.isCheckedOut() ||
                atts.getValue("jcr:primaryType") == null) {
            return false;
        }

        /*
            Edge case handling:
            Intermediate nodes contained in the import file only have the jcr:primaryType attribute, example:

            <contentFolderTest jcr:primaryType="jnt:contentFolder">
                <richTexttest jcr:primaryType="jnt:contentFolder" j:text="My rich text content"/>
            </contentFolderTest>

             contentFolderTest node is not really part of the import, it's just here for keeping the parenting structure.
             Such nodes should never be updated during a import, that have the option: cleanPreviousLiveImport=true.
             Because it could have bad effect, like existing mixins/properties loss.
        */
        if (cleanPreviousLiveImport && atts.getLength() == 1 && "jcr:primaryType".equals(atts.getQName(0))) {
            return false;
        }

        // Check noUpdateTypes list for node types that should not be updated if they already exist during import.
        String primaryNodeTypeName = nodeToCheck.getPrimaryNodeTypeName();
        return !noUpdateTypes.contains(Constants.JAHIANT_TRANSLATION.equals(primaryNodeTypeName) ?
                nodeToCheck.getParent().getPrimaryNodeTypeName() : primaryNodeTypeName);
    }

    private void handleBinary(Attributes atts, String decodedQName, String path, JCRNodeWrapper child) throws IOException, RepositoryException {
        // Introduce support of j:isSameAsLiveBinary attribute to reuse the binary from live node
        if (liveSession != null && cleanPreviousLiveImport && atts.getValue("j:isSameAsLiveBinary") != null) {
            // try first to get the binary instance from live node
            try {
                String livePath = child.getCorrespondingNodePath("live");
                JCRNodeWrapper liveNode = liveSession.getNode(livePath);
                if (liveNode.hasProperty("jcr:data")) {
                    // Reuse existing binary !
                    setJCRDataBinary(child, liveNode.getProperty("jcr:data").getBinary(), atts.getValue(Constants.JCR_MIMETYPE));
                } else {
                    // This ensures to trigger catch block
                    throw new RepositoryException("No binary correspondence found in existing live node.");
                }
            } catch (RepositoryException e) {
                // not found in existing live nodes, or in case of any JCR error ?
                // then use the binary from the zip, but will only exist under /live-content
                uploadFile(atts, decodedQName, path, child, "/live-content");
            }
            return;
        }

        // Standard binary import
        uploadFile(atts, decodedQName, path, child, baseFilesPath);
    }

    private void uploadFile(Attributes atts, String decodedQName, String path, JCRNodeWrapper child, String basePath) throws IOException, RepositoryException {
        if (!expandImportedFilesOnDisk) {
            boolean contentFound = findContent(basePath);
            if (contentFound) {
                uploadFile(atts, decodedQName, path, child, zis);
                zis.close();
            }
        } else {
            String contentInExpandedPath = findContentInExpandedPath(basePath);
            if (contentInExpandedPath != null) {
                final InputStream is = FileUtils.openInputStream(new File(expandedFolder + contentInExpandedPath));
                uploadFile(atts, decodedQName, path, child, is);
                is.close();
            }
        }
    }

    private void uploadFile(Attributes atts, String decodedQName, String path, JCRNodeWrapper child, InputStream is) throws RepositoryException {
        if (child.isFile()) {
            String mime = atts.getValue(Constants.JCR_MIMETYPE);
            if (mime == null) {
                if (logger.isWarnEnabled()) {
                    mime = JCRContentUtils.getMimeType(decodedQName);
                    if (mime != null) {
                        logger.warn("Legacy or invalid import detected for node " + path +
                                ", mime type cannot be resolved from file node, it should come from jcr:content node. Resolved mime type using servlet context instead=" +
                                mime + ".");
                    } else {
                        logger.warn("Legacy or invalid import detected for node " + path +
                                ", mime type cannot be resolved from file node, it should come from jcr:content node. Tried resolving mime type using servlet context but it isn't registered!");
                    }
                }
            }
            child.getFileContent().uploadFile(is, mime);
        } else {
            setJCRDataBinary(child, session.getValueFactory().createBinary(is), atts.getValue(Constants.JCR_MIMETYPE));
        }
    }

    private void setJCRDataBinary(JCRNodeWrapper node, Binary binary, String mimeType) throws RepositoryException {
        node.setProperty(Constants.JCR_DATA, binary);
        node.setProperty(Constants.JCR_MIMETYPE, mimeType);
        node.setProperty(Constants.JCR_LASTMODIFIED, Calendar.getInstance());
    }

    private void checkDependencies(String path, String pt, Attributes atts) throws RepositoryException {
        if (path.startsWith("/modules/")) {
            List<ExtendedNodeType> nodeTypes = new ArrayList<ExtendedNodeType>();
            nodeTypes.add(NodeTypeRegistry.getInstance().getNodeType(pt));
            if (atts.getValue(Constants.JCR_MIXINTYPES) != null) {
                for (String mixin : atts.getValue(Constants.JCR_MIXINTYPES).split(" ")) {
                    nodeTypes.add(NodeTypeRegistry.getInstance().getNodeType(mixin));
                }
            }
            JCRSiteNode currentSite = nodes.peek().getResolveSite();
            if (site == null || !currentSite.getIdentifier().equals(site.getIdentifier())) {
                dependencies = null;
                site = currentSite;
                if (site.hasProperty("j:resolvedDependencies")) {
                    dependencies = new ArrayList<String>();
                    dependencies.add(site.getName());
                    for (int i = 0; i < dependencies.size(); i++) {
                        JahiaTemplatesPackage aPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(dependencies.get(i));
                        if (aPackage == null) {
                            continue;
                        }
                        for (JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                            if (!dependencies.contains(depend.getId())) {
                                dependencies.add(depend.getId());
                            }
                        }
                    }
                }
            }
            for (ExtendedNodeType type : nodeTypes) {
                if (type.getTemplatePackage() != null && dependencies != null && !dependencies.contains(type.getTemplatePackage().getId())) {
                    String fileName = type.getTemplatePackage().getId();
                    logger.debug("Missing dependency : " + path + " (" + type.getName() + ") requires " + fileName + getLocation());
                    if (!missingDependencies.contains(fileName)) {
                        missingDependencies.add(fileName);
                    }
                }
            }
        }
    }

    private void addMixins(JCRNodeWrapper child, Attributes atts) throws RepositoryException {
        // Always unmark for deletion during import !
        // This could happen in case the node already exists and have been marked for deletion but not published yet.
        // - If deletion mark is part of the import, it will just be re-applied.
        // - If deletion mark is not part of the import (have been done after the export), it will not be re-applied.
        // (Anyway we need to remove the mark first, since live import will be imported in default, then published)
        // But that sounds logical.
        if (child.isMarkedForDeletion()) {
            child.unmarkForDeletion();
        }

        // Read mixins to add from the attribute.
        String mixinTypesAttributeValue = atts.getValue(Constants.JCR_MIXINTYPES);
        Set<String> mixinsToAdd = mixinTypesAttributeValue != null ?
                new LinkedHashSet<>(Arrays.asList(StringUtils.split(mixinTypesAttributeValue, " ,"))) :
                Collections.emptySet();


        // Due to live version being imported first, it's possible that default version contains removed mixins not published yet.
        // (Does not make sense to clean up live/default differences in case of jmix:autoPublish nodes, or new nodes only existing in default)
        if (cleanPreviousLiveImport &&
                !child.isNew() &&
                !child.isNodeType("jmix:autoPublish")) {
            Set<ExtendedNodeType> mixinsToRemove =
                    Arrays.stream(child.getMixinNodeTypes())
                            .filter(mixin -> !mixinsToAdd.contains(mixin))
                            .collect(Collectors.toSet());
            for (ExtendedNodeType mixinToRemove : mixinsToRemove) {
                child.removeMixin(mixinToRemove.getName());
            }
        }

        // Add mixins from attributes
        for (String mixinToAdd : mixinsToAdd) {
            try {
                child.addMixin(mixinToAdd);
            } catch (NoSuchNodeTypeException e) {
                logger.warn("Cannot add node type " + e.getMessage());
            }
        }
    }

    private void setAttributes(JCRNodeWrapper child, Attributes atts) throws RepositoryException {
        Set<String> processedProperties = new HashSet<String>();
        String lang;
        if (child.getPrimaryNodeTypeName().equals(Constants.JAHIANT_TRANSLATION)) {
            lang = atts.getValue(Constants.JCR_LANGUAGE);
            child.setProperty(Constants.JCR_LANGUAGE, lang);
            processedProperties.add(Constants.JCR_LANGUAGE);
        }

        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getURI(i).equals("http://www.w3.org/2000/xmlns/")) {
                continue;
            }

            String attrName = ISO9075.decode(atts.getQName(i));
            String attrValue = atts.getValue(i);

            boolean processed = false;
            for (AttributeProcessor processor : attributeProcessors) {
                if (processor.process(child, attrName, attrValue)) {
                    processed = true;
                    processedProperties.addAll(processor.getPropertyNamesProcessed());
                    break;
                }
            }
            if (processed) {
                continue;
            }

            for (String placeHolder : placeHoldersMap.keySet()) {
                if (attrValue.contains(placeHolder)) {
                    attrValue = attrValue.replace(placeHolder, placeHoldersMap.get(placeHolder));
                }
            }

            if (propertiesToSkip.contains(attrName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping property {}", attrName);
                }
            } else {
                for (Map.Entry<Pattern, String> entry : replacements.entrySet()) {
                    attrValue = entry.getKey().matcher(attrValue).replaceAll(entry.getValue());
                }

                if (attrName.equals("j:privileges") && child.isNodeType("jnt:ace")) {
                    attrName = "j:roles";
                    attrValue = mapAclAttributes(child, attrValue);
                    if (StringUtils.isEmpty(attrValue)) {
                        // Value is mandatory, set a fake role to allow import to continue
                        attrValue = "dummy-role";
                    }
                }

                if ((attrName.equals(Constants.JCR_TITLE) || attrName
                        .equals("jcr:description"))
                        && !child.isNodeType(Constants.MIX_TITLE)) {
                    child.addMixin(Constants.MIX_TITLE);
                } else if (attrName.equals("j:defaultCategory")
                        && !child.isNodeType(Constants.JAHIAMIX_CATEGORIZED)) {
                    child.addMixin(Constants.JAHIAMIX_CATEGORIZED);
                }
                ExtendedPropertyDefinition propDef;
                propDef = child.getApplicablePropertyDefinition(attrName);
                if (propDef == null) {
                    logger.error("Couldn't find definition for property " + attrName + " in " + child.getPrimaryNodeTypeName() + getLocation());
                    continue;
                }
                processedProperties.add(attrName);

                if (propDef.getRequiredType() == PropertyType.REFERENCE || propDef.getRequiredType() == ExtendedPropertyType.WEAKREFERENCE) {
                    if (attrValue.length() > 0) {
                        String[] values = propDef.isMultiple() ? Patterns.SPACE.split(attrValue) : new String[]{attrValue};
                        int idx = 0;
                        for (String value : values) {
                            value = JCRMultipleValueUtils.decode(value);
                            if (!StringUtils.isEmpty(value)) {
                                value = getReferenceValue(value, child.getPath());
                                if (attrName.equals("j:defaultCategory") && value.startsWith("/root")) {
                                    // Map categories from legacy imports
                                    value = JCRContentUtils.getSystemSitePath() + "/categories" + StringUtils.substringAfter(value, "/root");
                                }
                                if (!references.containsKey(value)) {
                                    references.put(value, new ArrayList<String>());
                                }
                                references.get(value).add(child.getIdentifier() + "/" + attrName + "["+ idx++ + "]");
                            }
                        }
                    }
                } else {
                    if (propDef.isMultiple()) {
                        String[] s = "".equals(attrValue) ? new String[0] : Patterns.SPACE.split(attrValue);
                        List<Value> oldvalues = new ArrayList<Value>();

                        if (child.getRealNode().hasProperty(attrName)) {
                            Value[] oldValues = child.getRealNode().getProperty(attrName).getValues();
                            for (Value oldValue : oldValues) {
                                oldvalues.add(oldValue);
                            }
                        }

                        // in case of previous Live import we need to override the values completely.
                        // As original values may come from the live version, and the default version could be different.
                        if (cleanPreviousLiveImport) {
                            List<Value> values = new ArrayList<Value>();
                            for (int j = 0; j < s.length; j++) {
                                values.add(child.getRealNode().getSession().getValueFactory().createValue(JCRMultipleValueUtils.decode(s[j]), propDef.getRequiredType()));
                            }
                            if (!values.equals(oldvalues)) {
                                child.getRealNode().setProperty(attrName, values.toArray(new Value[values.size()]));
                            }
                        } else {
                            List<Value> values = new ArrayList<Value>(oldvalues);
                            for (int j = 0; j < s.length; j++) {
                                final Value value = child.getRealNode().getSession().getValueFactory().createValue(JCRMultipleValueUtils.decode(s[j]), propDef.getRequiredType());
                                if (!oldvalues.contains(value)) {
                                    values.add(value);
                                }
                            }
                            try {
                                if (!values.equals(oldvalues)) {
                                    child.getRealNode().setProperty(attrName, values.toArray(new Value[values.size()]));
                                }
                            } catch (RepositoryException e) {
                                logger.error(e.getMessage(), e);  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    } else {
                        if (!child.getRealNode().hasProperty(attrName) || !child.getRealNode().getProperty(attrName).getString().equals(attrValue)) {
                            if (propDef.getRequiredType() == PropertyType.UNDEFINED) {
                                child.getRealNode().setProperty(attrName, attrValue);
                            } else {
                                child.getRealNode().setProperty(attrName, attrValue, propDef.getRequiredType());
                            }
                        }
                    }
                }
            }
        }

        // (Does not make sense to clean up live/default difference in case of jmix:autoPublish nodes, or new nodes only existing in default)
        if (cleanPreviousLiveImport &&
                !child.isNew() &&
                !child.isNodeType("jmix:autoPublish")) {
            // Since live is imported first in the default workspace, it's possible that the default version of the node contains
            // removed properties compare to the live version. In this case, we need to remove the properties from the default version.
            PropertyIterator propertyIterator = child.getRealNode().getProperties();
            while (propertyIterator.hasNext()) {
                Property property = propertyIterator.nextProperty();
                if (!NO_LIVE_CLEANUP_FOR_PROPS.contains(property.getName()) && // Additional properties to be skipped during cleanup
                        !propertiesToSkip.contains(property.getName()) && // Properties skipped to be imported
                        !DEFAULT_PROPERTIES_TO_IGNORE.contains(property.getName()) && // Properties skipped by export
                        !processedProperties.contains(property.getName())) { // Exclude properties that have been processed during import
                    // This property is most likely a property that has been removed from the default version of the node, but imported from the live version.
                    property.remove();
                }
            }
        }
    }

    private String getReferenceValue(String value, String basePath) throws RepositoryException {
        if (value.startsWith("$currentSite")) {
            value = nodes.peek().getResolveSite().getPath() + value.substring(12);
        } else if (value.startsWith("#")) {
            value = value.substring(1);
            String rootPath = nodes.firstElement().getPath();
            if (!rootPath.equals("/")) {
                value = (nodes.size() > 1 ? nodes.get(1).getPath() : basePath) + value;
            }
        }
        return value;
    }

    private boolean findContent(String basePath) throws IOException {
        if (archive == null) {
            return false;
        }
        String path = pathes.peek();
        if (path.endsWith("/jcr:content")) {
            String[] p = Patterns.SLASH.split(path);
            path = path.replace("/jcr:content", "/" + p[p.length - 2]);
        } else {
            path = JCRContentUtils.replaceColon(path);
        }
        int fileIndex = fileList.indexOf(basePath + path);
        if (fileIndex == -1 && path.startsWith("/content")) {
            // Case of root node export - root node has been renamed to "content" during export
            path = path.substring("/content".length());
            fileIndex = fileList.indexOf(basePath + path);
        }
        if (fileIndex != -1) {
            if (nextEntry == null || fileList.indexOf("/" + nextEntry.getName().replace('\\', '/')) > fileIndex) {
                if (zis != null) {
                    zis.reallyClose();
                }
                zis = new NoCloseZipInputStream(new BufferedInputStream(archive.getInputStream()));
            }
            do {
                nextEntry = zis.getNextEntry();
            } while (!("/" + nextEntry.getName().replace('\\', '/')).equals(basePath + path));

            return true;
        }
        return false;
    }

    private String findContentInExpandedPath(String basePath) throws IOException {
        if (archive == null) {
            return null;
        }
        String path = pathes.peek();
        if (path.endsWith("/jcr:content")) {
            String[] p = Patterns.SLASH.split(path);
            path = path.replace("/jcr:content", "/" + p[p.length - 2]);
        } else {
            path = JCRContentUtils.replaceColon(path);
        }
        int fileIndex = fileList.indexOf(basePath + path);
        if (fileIndex == -1 && path.startsWith("/content")) {
            // Case of root node export - root node has been renamed to "content" during export
            path = path.substring("/content".length());
            fileIndex = fileList.indexOf(basePath + path);
        }
        if (fileIndex != -1) {
            return basePath + path;
        }
        return null;
    }

    private String mapAclAttributes(JCRNodeWrapper node, String aclValue) {
        Set<String> roles = new HashSet<String>();
        if (aclValue.contains("jcr:read")) {
            if (CollectionUtils.isEmpty(LegacyImportHandler.CUSTOM_FILES_READ_ROLES)) {
                roles.addAll(LegacyImportHandler.READ_ROLES);
            } else {
                roles.addAll(LegacyImportHandler.CUSTOM_FILES_READ_ROLES);
            }
        }
        if (aclValue.contains("jcr:write")) {
            if (CollectionUtils.isEmpty(LegacyImportHandler.CUSTOM_FILES_WRITE_ROLES)) {
                roles.addAll(LegacyImportHandler.WRITE_ROLES);
            } else {
                roles.addAll(LegacyImportHandler.CUSTOM_FILES_WRITE_ROLES);
            }
        }
        StringBuilder s = new StringBuilder();
        for (String role : roles) {
            s.append(role).append(" ");
        }
        return s.toString().trim();
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (error > 0) {
            error--;
            return;
        }
        if (ugcLevel > 0) {
            ugcLevel--;
        }

        JCRNodeWrapper w = nodes.pop();
        pathes.pop();

        if (w != null && ignorePath != null && w.getPath().equals(ignorePath)) {
            ignorePath = null;
        }
        if (w != null && currentFilePath != null && w.getPath().equals(currentFilePath)) {
            currentFilePath = null;
        }

        if (w != null && w.isFile()) {
            try {
                if (!w.hasNode(Constants.JCR_CONTENT) || !w.getNode(Constants.JCR_CONTENT).hasProperty(Constants.JCR_DATA)) {
                    logger.warn("Cannot find the file content for the node " + w.getPath()
                            + ". Skipping importing it.");
                    final NodeIterator iterator = w.getNodes();
                    while (iterator.hasNext()) {
                        final Node node = iterator.nextNode();
                        uuidsSet.remove(node.getIdentifier());
                    }
                    uuidsSet.remove(w.getIdentifier());
                    w.remove();
                }
            } catch (RepositoryException e) {
                throw new SAXException(e);
            }
        }

    }

    public void endDocument() throws SAXException {
        if (zis != null) {
            try {
                zis.reallyClose();

            } catch (IOException re) {
                throw new SAXException(re);
            }
            zis = null;
        }
        try {
            if (resolveReferenceAtEnd) {
                ReferencesHelper.resolveCrossReferences(session, references, false);
            }
        } catch (RepositoryException e) {
            throw new SAXException(e);
        }
    }

    public void setReferences(Map<String, List<String>> references) {
        this.references = references;
    }

    public void setRootBehavior(int rootBehavior) {
        this.rootBehavior = rootBehavior;
    }

    public void setUuidBehavior(int uuidBehavior) {
        this.uuidBehavior = uuidBehavior;
    }

    public void setResolveReferenceAtEnd(boolean resolveReferenceAtEnd) {
        this.resolveReferenceAtEnd = resolveReferenceAtEnd;
    }

    public void setPlaceHoldersMap(Map<String, String> placeHoldersMap) {
        this.placeHoldersMap = placeHoldersMap;
    }

    public List<String> getNoUpdateTypes() {
        return noUpdateTypes;
    }

    public void setNoUpdateTypes(List<String> noUpdateTypes) {
        this.noUpdateTypes = noUpdateTypes;
    }

    public boolean isImportUserGeneratedContent() {
        return importUserGeneratedContent;
    }

    public void setImportUserGeneratedContent(boolean importUserGeneratedContent) {
        this.importUserGeneratedContent = importUserGeneratedContent;
    }

    /**
     * @deprecated Use {@link #isCleanPreviousLiveImport()} instead.
     */
    @Deprecated(since = "8.2.1.0", forRemoval = true)
    public boolean isReplaceMultipleValues() {
        return isCleanPreviousLiveImport();
    }

    /**
     * @deprecated Use {@link #cleanPreviousLiveImport()} instead.
     */
    @Deprecated(since = "8.2.1.0", forRemoval = true)
    public void setReplaceMultipleValues(boolean replaceMultipleValues) {
        if (replaceMultipleValues) {
            try {
                cleanPreviousLiveImport();
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @deprecated Use {@link #isCleanPreviousLiveImport()} instead.
     */
    @Deprecated(since = "8.2.1.0", forRemoval = true)
    public boolean isEnforceUuid() {
        return isCleanPreviousLiveImport();
    }

    /**
     * @deprecated Use {@link #cleanPreviousLiveImport()} instead.
     */
    @Deprecated(since = "8.2.1.0", forRemoval = true)
    public void setEnforceUuid(boolean enforceUuid) {
        if (enforceUuid) {
            try {
                cleanPreviousLiveImport();
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setReplacements(Map<String, String> replacements) {
        if (replacements == null || replacements.isEmpty()) {
            this.replacements = Collections.emptyMap();
        } else {
            this.replacements = new HashMap<Pattern, String>(replacements.size());
            for (Map.Entry<String, String> repl : replacements.entrySet()) {
                this.replacements.put(Pattern.compile(repl.getKey()), repl.getValue());
            }
        }
    }

    public void setAttributeProcessors(List<AttributeProcessor> attributeProcessors) {
        this.attributeProcessors = attributeProcessors;
    }

    public Set<String> getPropertiesToSkip() {
        return propertiesToSkip;
    }

    public void setPropertiesToSkip(Set<String> propertiesToSkip) {
        if (propertiesToSkip == null || propertiesToSkip.isEmpty()) {
            this.propertiesToSkip = Collections.emptySet();
        } else {
            this.propertiesToSkip = propertiesToSkip;
        }
    }

    public List<String> getUuids() {
        return new ArrayList<>(uuidsSet);
    }
    
     public Set<String> getUuidsSet() {
        return uuidsSet;
    }

    public String getBaseFilesPath() {
        return baseFilesPath;
    }

    public void setBaseFilesPath(String baseFilesPath) {
        this.baseFilesPath = baseFilesPath;
    }

    public String getLocation() {
        if (documentLocator != null) {
            return " (line " + documentLocator.getLineNumber() + ", column " + documentLocator.getColumnNumber() + ")";
        }
        return "";
    }

    public Set<String> getMissingDependencies() {
        return missingDependencies;
    }

    public void setDocumentLocator(Locator documentLocator) {
        this.documentLocator = documentLocator;
    }

    /**
     * @deprecated Use {@link #isCleanPreviousLiveImport()} instead.
     */
    @Deprecated(since = "8.2.1.0", forRemoval = true)
    public boolean isRemoveMixins() {
        return isCleanPreviousLiveImport();
    }

    /**
     * @deprecated Use {@link #cleanPreviousLiveImport()} instead.
     */
    @Deprecated(since = "8.2.1.0", forRemoval = true)
    public void setRemoveMixins(boolean removeMixins) {
        if (removeMixins) {
            try {
                cleanPreviousLiveImport();
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * configuration in order to restore publication statuses related properties.
     */
    public void configureToRestorePublicationStatuses() {
        this.propertiesToSkip.remove(Constants.LASTPUBLISHED);
        this.propertiesToSkip.remove(Constants.LASTPUBLISHEDBY);
        this.propertiesToSkip.remove(Constants.PUBLISHED);
    }

    /**
     * In case a live repository was imported first in the default workspace,
     * this method allows to configure the default import that follows in order to have correct behavior against existing
     * nodes from the live workspace that would still be present in the default workspace.
     */
    public void cleanPreviousLiveImport() throws RepositoryException {
        this.cleanPreviousLiveImport = true;
        this.liveSession = JCRSessionFactory.getInstance().getCurrentSystemSession("live", null, null);
        configureToRestorePublicationStatuses();
        setUuidBehavior(IMPORT_UUID_COLLISION_MOVE_EXISTING);
    }

    public boolean isCleanPreviousLiveImport() {
        return cleanPreviousLiveImport;
    }
}
