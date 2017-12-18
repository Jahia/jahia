/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import java.util.zip.ZipEntry;

/**
 * SAX handler that performs import of the JCR content, provided in a document format.
 * User: toto
 * Date: 11 févr. 2008
 * Time: 16:38:38
 */
public class DocumentViewImportHandler extends BaseDocumentViewHandler implements ImportUUIDBehavior {
    private static Logger logger = LoggerFactory.getLogger(DocumentViewImportHandler.class);

    public static final int IMPORT_UUID_COLLISION_MOVE_EXISTING = 4;

    public static final int ROOT_BEHAVIOUR_IGNORE = 0;
    public static final int ROOT_BEHAVIOUR_REPLACE = 1;
    public static final int ROOT_BEHAVIOUR_RENAME = 2;

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

    private boolean replaceMultipleValues = false;
    private boolean importUserGeneratedContent = false;
    private int ugcLevel = 0;

    private boolean enforceUuid = false;

    private List<String> noSubNodesImport = Arrays.asList("jnt:importDropBox", "jnt:referencesKeeper");
    private List<String> noUpdateTypes = Arrays.asList("jnt:virtualsitesFolder", "jnt:usersFolder", "jnt:groupsFolder", "jnt:user");

    private List<String> uuids = new ArrayList<String>();

    private boolean expandImportedFilesOnDisk = SettingsBean.getInstance().isExpandImportedFilesOnDisk();
    private String expandImportedFilesOnDiskPath = SettingsBean.getInstance().getExpandImportedFilesOnDiskPath();

    private Set<String> missingDependencies = new HashSet<String>();

    private boolean removeMixins = false;

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
            logger.error(e.getMessage()+ getLocation(), e);
            throw new IOException();
        }
        nodes.add(node);

        this.archive = archive;

        if (archive != null && !archive.isReadable() && archive instanceof FileSystemResource) {
            expandImportedFilesOnDiskPath = archive.getFile().getPath();
            expandImportedFilesOnDisk = true;
        }

        this.fileList = fileList;
        setPropertiesToSkip((Set<String>) SpringContextSingleton.getBean("DocumentViewImportHandler.propertiesToSkip"));
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
            } catch (CompositeConstraintViolationException e) {
                logger.error("Constraint violation exception", e);
                throw new SAXException("Cannot save batch", e);
            } catch (ConstraintViolationException e) {
                if (e.getMessage().contains("mandatory child node") && nodes.size() > 1 && e.getMessage().startsWith(nodes.peek().getPath())) {
                    // save on the next node when next node is needed (like content node for files)
                    batchCount = maxBatch - 1;
                } else {
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
                    String referenceValue = getReferenceValue(memberRef);
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
                        uuids.add(member.getIdentifier());
                    }
                    nodes.push(member);
                    return;
                }
            }

            if (pathMapping.containsKey(path + "/")) {
                path = StringUtils.substringBeforeLast(pathMapping.get(path + "/"), "/");
                decodedQName = StringUtils.substringAfter(path,"/");
            }

            if (noSubNodesImport.contains(pt)) {
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
                String originalUuid = atts.getValue("jcr:uuid");
                String uuid = originalUuid;
                if (uuid != null && uuidMapping.containsKey(uuid)) {
                    uuid = uuidMapping.get(uuid);
                } else if (enforceUuid) {
                    uuid = null;
                }

                if (isValid && enforceUuid && uuid != null) {
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

//                    String share = atts.getValue("j:share");
//                    if (!StringUtils.isEmpty(uuid) && uuidMapping.containsKey(uuid)) {
//                        child = nodes.peek().clone(session.getNodeByUUID(uuidMapping.get(uuid)), decodedQName);
//                    } else if (!StringUtils.isEmpty(share)) {
//                        for (Map.Entry<String, String> entry : pathMapping.entrySet()) {
//                            if (share.startsWith(entry.getKey())) {
//                                share = entry.getValue() + StringUtils.substringAfter(share, entry.getKey());
//                                break;
//                            }
//                        }
//                        child = nodes.peek().clone(session.getNode(share), decodedQName);
//                    } else {
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
                        uploadFile(atts, decodedQName, path, child);

                        setAttributes(child, atts);
                        uuids.add(child.getIdentifier());
                        if (child.isFile() && currentFilePath == null) {
                            currentFilePath = child.getPath();
                        }
                        if (child.isNodeType(Constants.JAHIANT_MOUNTPOINT)) {
                            session.save(JCRObservationManager.IMPORT);
                        }
//                    }
                    } else {
                        throw new AccessDeniedException("Missing jcr:addChildNodes permission for user "+session.getUser().getName());
                    }
                } else {
                    if (child.hasPermission("jcr:modifyProperties") && child.isCheckedOut()) {
                        if (!noUpdateTypes.contains(child.getPrimaryNodeType().getName()) && atts.getValue("jcr:primaryType") != null) {
                            addMixins(child, atts);
                            setAttributes(child, atts);
                            uploadFile(atts, decodedQName, path, child);
                        }
                    }
                    uuids.add(child.getIdentifier());
                }
                if (originalUuid != null) {
                    uuidMapping.put(originalUuid, child.getIdentifier());
                }
                if (nodes.peek().getPrimaryNodeType().hasOrderableChildNodes() && nodes.peek().hasPermission("jcr:write")
                        && !JCRSessionFactory.getInstance().getMountPoints().containsKey(child.getPath())) {
                    nodes.peek().orderBefore(decodedQName,null);
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

    private void uploadFile(Attributes atts, String decodedQName, String path, JCRNodeWrapper child) throws IOException, RepositoryException {
        if (!expandImportedFilesOnDisk) {
            boolean contentFound = findContent();
            if (contentFound) {
                uploadFile(atts, decodedQName, path, child, zis);
                zis.close();
            }
        } else {
            String contentInExpandedPath = findContentInExpandedPath();
            if (contentInExpandedPath != null) {
                final InputStream is = FileUtils.openInputStream(new File(
                        expandImportedFilesOnDiskPath + contentInExpandedPath));
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
            child.setProperty(Constants.JCR_DATA, session.getValueFactory().createBinary(is));
            child.setProperty(Constants.JCR_MIMETYPE, atts.getValue(Constants.JCR_MIMETYPE));
            child.setProperty(Constants.JCR_LASTMODIFIED, Calendar.getInstance());
        }
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
        ExtendedNodeType[] existingMixinNodeTypes = child.getMixinNodeTypes();
        String m = atts.getValue(Constants.JCR_MIXINTYPES);
        if (m != null) {
            Set<String> addedMixins =  new LinkedHashSet<String>(Arrays.asList(StringUtils.split(m, " ,")));
            if (removeMixins) {
                // first we remove existing mixins that are no longer present in added mixins
                for (ExtendedNodeType existingMixin : existingMixinNodeTypes) {
                    String existingMixinName = existingMixin.getName();
                    if (!addedMixins.contains(existingMixinName)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Removing mixin {} from node {}", existingMixinName, child.getPath());
                        }
                        child.removeMixin(existingMixinName);
                    }
                }
            }
            // and now we add the mixins
            for (String addedMixin : addedMixins) {
                try {
                    child.addMixin(addedMixin);
                } catch (NoSuchNodeTypeException e) {
                    logger.warn("Cannot add node type " + e.getMessage());
                }
            }
        } else if (removeMixins) {
            // remove all mixins as none is set on the node
            for (ExtendedNodeType mixin : child.getMixinNodeTypes()) {
                child.removeMixin(mixin.getName());
            }
        }
    }

    private void setAttributes(JCRNodeWrapper child, Attributes atts) throws RepositoryException {
        String lang = null;
        if (child.getPrimaryNodeTypeName().equals(Constants.JAHIANT_TRANSLATION)) {
            lang = atts.getValue(Constants.JCR_LANGUAGE);
            child.setProperty(Constants.JCR_LANGUAGE, lang);
        }

        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getURI(i).equals("http://www.w3.org/2000/xmlns/")) {
                continue;
            }

            String attrName = ISO9075.decode(atts.getQName(i));
            String attrValue = atts.getValue(i);

            boolean processed = false;
            for (AttributeProcessor processor : attributeProcessors) {
                if (processor.process(child,attrName, attrValue)) {
                    processed = true;
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

                if (propDef.getRequiredType() == PropertyType.UNDEFINED) {
                    // avoid illegal type 0
                    //...getValueFactory().createValue(value, 0) throw a illegal type 0 exception
                    logger.error("Couldn't resolve property type for property " + attrName + " in " + child.getPrimaryNodeTypeName() + getLocation());
                    continue;
                }

                if (propDef.getRequiredType() == PropertyType.REFERENCE || propDef.getRequiredType() == ExtendedPropertyType.WEAKREFERENCE) {
                    if (attrValue.length() > 0) {
                        String[] values = propDef.isMultiple() ? Patterns.SPACE.split(attrValue) : new String[]{attrValue};
                        for (String value : values) {
                            value = JCRMultipleValueUtils.decode(value);
                            if (!StringUtils.isEmpty(value)) {
                                value = getReferenceValue(value);
                                if (attrName.equals("j:defaultCategory") && value.startsWith("/root")) {
                                    // Map categories from legacy imports
                                    value = JCRContentUtils.getSystemSitePath() + "/categories" + StringUtils.substringAfter(value, "/root");
                                }
                                if (!references.containsKey(value)) {
                                    references.put(value, new ArrayList<String>());
                                }
                                references.get(value).add(child.getIdentifier() + "/" + attrName);
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

                        if (replaceMultipleValues) {
                            List<Value> values = new ArrayList<Value>();
                            for (int j = 0; j < s.length; j++) {
                                values.add(child.getRealNode().getSession().getValueFactory().createValue(JCRMultipleValueUtils.decode(s[j]), propDef.getRequiredType()));
                            }
                            if (!values.equals(oldvalues)) {
                                child.getRealNode().setProperty(attrName, values.toArray(new Value[values.size()]));
                            }
                        } else {
                            List<Value> values = new ArrayList<Value>(oldvalues);
                            for (int j=0; j < s.length; j++) {
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
                                logger.error(e.getMessage(),e);  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    } else {
                        if (!child.getRealNode().hasProperty(attrName) || !child.getRealNode().getProperty(attrName).getString().equals(attrValue)) {
                            child.getRealNode().setProperty(attrName, attrValue, propDef.getRequiredType());
                        }
                    }
                }
            }
        }
    }

    private String getReferenceValue(String value) throws RepositoryException {
        if (value.startsWith("$currentSite")) {
            value = nodes.peek().getResolveSite().getPath() + value.substring(12);
        } else if (value.startsWith("#")) {
            value = value.substring(1);
            String rootPath = nodes.firstElement().getPath();
            if (!rootPath.equals("/")) {
                value = rootPath + value;
            }
        }
        return value;
    }

    private boolean findContent() throws IOException {
        if (archive == null) {
            return false;
        }
        if (zis == null) {
            zis = new NoCloseZipInputStream(new BufferedInputStream(archive.getInputStream()));
            nextEntry = zis.getNextEntry();
        }
        String path = pathes.peek();
        if (path.endsWith("/jcr:content")) {
            String[] p = Patterns.SLASH.split(path);
            path = path.replace("/jcr:content", "/" + p[p.length - 2]);
        } else {
            path =JCRContentUtils.replaceColon(path);
        }
        int fileIndex = fileList.indexOf(baseFilesPath + path);
        if (fileIndex == -1 && path.startsWith("/content")) {
            // Case of root node export - root node has been renamed to "content" during export
            path = path.substring("/content".length());
            fileIndex = fileList.indexOf(baseFilesPath + path);
        }
        if (fileIndex != -1) {
            if (fileList.indexOf("/" + nextEntry.getName().replace('\\', '/')) > fileIndex) {
                zis.reallyClose();
                zis = new NoCloseZipInputStream(new BufferedInputStream(archive.getInputStream()));
            }
            do {
                nextEntry = zis.getNextEntry();
            } while (!("/" + nextEntry.getName().replace('\\', '/')).equals(baseFilesPath + path));

            return true;
        }
        return false;
    }

    private String findContentInExpandedPath() throws IOException {
        if (archive == null) {
            return null;
        }
        String path = pathes.peek();
        if (path.endsWith("/jcr:content")) {
            String[] p = Patterns.SLASH.split(path);
            path = path.replace("/jcr:content", "/" + p[p.length - 2]);
        } else {
            path =JCRContentUtils.replaceColon(path);
        }
        int fileIndex = fileList.indexOf(baseFilesPath + path);
        if (fileIndex == -1 && path.startsWith("/content")) {
            // Case of root node export - root node has been renamed to "content" during export
            path = path.substring("/content".length());
            fileIndex = fileList.indexOf(baseFilesPath + path);
        }
        if (fileIndex != -1) {
            return baseFilesPath + path;
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
                        uuids.remove(node.getIdentifier());
                    }
                    uuids.remove(w.getIdentifier());
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

    public boolean isReplaceMultipleValues() {
        return replaceMultipleValues;
    }

    public void setReplaceMultipleValues(boolean replaceMultipleValues) {
        this.replaceMultipleValues = replaceMultipleValues;
    }

    public boolean isEnforceUuid() {
        return enforceUuid;
    }

    public void setEnforceUuid(boolean enforceUuid) {
        this.enforceUuid = enforceUuid;
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
        return uuids;
    }

    public String getBaseFilesPath() {
        return baseFilesPath;
    }

    public void setBaseFilesPath(String baseFilesPath) {
        this.baseFilesPath = baseFilesPath;
    }

    public String getLocation() {
        if (documentLocator != null) {
            return " (line " + documentLocator.getLineNumber() + ", column "+documentLocator.getColumnNumber()+")";
        }
        return "";
    }

    public Set<String> getMissingDependencies() {
        return missingDependencies;
    }

    public void setDocumentLocator(Locator documentLocator) {
        this.documentLocator = documentLocator;
    }

    public boolean isRemoveMixins() {
        return removeMixins;
    }

    public void setRemoveMixins(boolean removeMixins) {
        this.removeMixins = removeMixins;
    }
}
