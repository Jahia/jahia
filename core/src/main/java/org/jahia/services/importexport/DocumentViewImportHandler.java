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

package org.jahia.services.importexport;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.utils.zip.ZipEntry;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private int maxBatch = 5000;
    private int batchCount = 0;

    private File archive;
    private NoCloseZipInputStream zis;
    private ZipEntry nextEntry;
    private List<String> fileList = new ArrayList<String>();
    private String baseFilesPath = "/content";
    private Stack<JCRNodeWrapper> nodes = new Stack<JCRNodeWrapper>();

    private Map<String, String> uuidMapping;
    private Map<String, String> pathMapping;
    private Map<String, List<String>> references = new HashMap<String, List<String>>();

    private Map<Pattern, String> replacements = Collections.emptyMap();

    private Set<String> propertiesToSkip = Collections.emptySet();

    private String currentFilePath = null;

    private String ignorePath = null;

    private int error = 0;

    private boolean resolveReferenceAtEnd = true;

    private int uuidBehavior = ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW;

    private Map<String, String> placeHoldersMap = new HashMap<String, String>();

    private boolean importUserGeneratedContent = false;
    private int ugcLevel = 0;

    private boolean enforceUuid = false;

    private List<String> noSubNodesImport = Arrays.asList("jnt:importDropBox", "jnt:referencesKeeper");
    private List<String> noUpdateTypes = Arrays.asList("jnt:virtualsitesFolder", "jnt:usersFolder", "jnt:groupsFolder", "jnt:user");

    private List<String> uuids = new ArrayList<String>();

    public DocumentViewImportHandler(JCRSessionWrapper session, String rootPath) throws IOException {
        this(session, rootPath, null, null);
    }

    @SuppressWarnings("unchecked")
    public DocumentViewImportHandler(JCRSessionWrapper session, String rootPath, File archive, List<String> fileList) throws IOException {
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
            if (node.isNodeType("jnt:user")) {
                placeHoldersMap.put("$user", "u:" + node.getPath().substring(node.getPath().lastIndexOf("/") + 1));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new IOException();
        }
        nodes.add(node);

        this.archive = archive;
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
                session.save();
                batchCount = 0;
            } catch (ConstraintViolationException e) {
                // save on the next node when next node is needed (like content node for files)
                batchCount = maxBatch -1;
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
            session.getPathMapping().put("/" + decodedQName, nodes.peek().getPath().equals("/") ? "" : nodes.peek().getPath());
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
                    path = ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule().getPathForUsername(m.group(1));
                }
            }

            String pt = atts.getValue(Constants.JCR_PRIMARYTYPE);

            if (pathMapping.containsKey(path + "/")) {
                path = StringUtils.substringBeforeLast(pathMapping.get(path + "/"), "/");
            }

            if (noSubNodesImport.contains(pt)) {
                ignorePath = path;
            }
            JCRNodeWrapper child = null;

            boolean isValid = true;
            try {
                child = session.getNode(path);
                if (child.hasPermission("jcr:versionManagement") && child.isVersioned() && !child.isCheckedOut()) {
                    session.getWorkspace().getVersionManager().checkout(child.getPath());
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

                if (!isValid || child.getDefinition().allowsSameNameSiblings()) {
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
                            session.checkout(nodes.peek());
                            try {
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

                        boolean contentFound = findContent();

                        if (contentFound) {
                            if (child.isFile()) {
                                String mime = atts.getValue(Constants.JCR_MIMETYPE);
                                child.getFileContent().uploadFile(zis, mime);
                                zis.close();
                            } else {
                                child.setProperty(Constants.JCR_DATA, session.getValueFactory().createBinary(zis));
                                child.setProperty(Constants.JCR_MIMETYPE, atts.getValue(Constants.JCR_MIMETYPE));
                                child.setProperty(Constants.JCR_LASTMODIFIED, Calendar.getInstance());
                                zis.close();
                            }
                        }

                        setAttributes(child, atts);
                        uuids.add(child.getIdentifier());
                        if (child.isFile() && currentFilePath == null) {
                            currentFilePath = child.getPath();
                        }
//                    }
                    }
                } else {
                    if (child.hasPermission("jcr:modifyProperties") && child.isCheckedOut()) {
                        if (!noUpdateTypes.contains(child.getPrimaryNodeType().getName()) && atts.getValue("jcr:primaryType") != null) {
                            addMixins(child, atts);
                            setAttributes(child, atts);
                        }
                    }
                    uuids.add(child.getIdentifier());
                }
                uuidMapping.put(originalUuid, child.getIdentifier());
                if (nodes.peek().getPrimaryNodeType().hasOrderableChildNodes() && nodes.peek().hasPermission("jcr:write")) {
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
        } catch (RepositoryException re) {
            logger.error("Cannot import " + pathes.pop(), re);
            error++;
        } catch (Exception re) {
            throw new SAXException(re);
        }
    }

    private void addMixins(JCRNodeWrapper child, Attributes atts) throws RepositoryException {
        String m = atts.getValue(Constants.JCR_MIXINTYPES);
        if (m != null) {
            StringTokenizer st = new StringTokenizer(m, " ,");
            while (st.hasMoreTokens()) {
                try {
                    child.addMixin(st.nextToken());
                } catch (NoSuchNodeTypeException e) {
                    logger.warn("Cannot add node type " + e.getMessage());
                }
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
                    logger.error("Couldn't find definition for property " + attrName);
                    continue;
                }

                if (propDef.getRequiredType() == PropertyType.REFERENCE || propDef.getRequiredType() == ExtendedPropertyType.WEAKREFERENCE) {
                    if (attrValue.length() > 0) {
                        String decodedValue = ISO9075.decode(attrValue);
                        String[] values = propDef.isMultiple() ? ISO9075.decode(decodedValue).split(" ") : new String[]{decodedValue};
                        for (String value : values) {
                            if (!StringUtils.isEmpty(value)) {
                                if (value.startsWith("$currentSite")) {
                                    value = nodes.peek().getResolveSite().getPath() + value.substring(12);
                                } else if (value.startsWith("#")) {
                                    value = value.substring(1);
                                    String rootPath = nodes.firstElement().getPath();
                                    if (!rootPath.equals("/")) {
                                        value = rootPath + value;
                                    }
                                } 
                                for (Map.Entry<String, String> entry : pathMapping.entrySet()) {
                                    if (value.startsWith(entry.getKey())) {
                                        value = entry.getValue() + StringUtils.substringAfter(value, entry.getKey());
                                        break;
                                    }
                                }
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
                        String[] s = "".equals(attrValue) ? new String[0] : attrValue.split(" ");
                        Value[] v = new Value[s.length];
                        for (int j = 0; j < s.length; j++) {
                            v[j] = child.getRealNode().getSession().getValueFactory().createValue(s[j]);
                        }
                        child.getRealNode().setProperty(attrName, v);
                    } else {
                        child.getRealNode().setProperty(attrName, attrValue);
                    }
                }
            }
        }
    }

    private boolean findContent() throws IOException {
        if (archive == null) {
            return false;
        }
        if (zis == null) {
            zis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(archive)));
            nextEntry = zis.getNextEntry();
        }
        String path = pathes.peek();
        if (path.endsWith("/jcr:content")) {
            String[] p = path.split("/");
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
                zis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(archive)));
            }
            do {
                nextEntry = zis.getNextEntry();
            } while (!("/" + nextEntry.getName().replace('\\', '/')).equals(baseFilesPath + path));

            return true;
        }
        return false;
    }

    private String mapAclAttributes(JCRNodeWrapper node, String aclValue) {
        Set<String> roles = new HashSet<String>();
        if (aclValue.contains("jcr:read")) {
            roles.addAll(LegacyImportHandler.READ_ROLES);
        }
        if (aclValue.contains("jcr:write")) {
            roles.addAll(LegacyImportHandler.WRITE_ROLES);
        }
        String s = "";
        for (String role : roles) {
            s += role + " ";
        }
        return s.trim();
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
                ReferencesHelper.resolveCrossReferences(session, references);
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

    public void setUuids(List<String> uuids) {
        this.uuids = uuids;
    }

    public String getBaseFilesPath() {
        return baseFilesPath;
    }

    public void setBaseFilesPath(String baseFilesPath) {
        this.baseFilesPath = baseFilesPath;
    }
}
