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

package org.jahia.services.importexport;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.jackrabbit.util.ISO9075;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.utils.zip.ZipEntry;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.ImmutableSet;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 11 févr. 2008
 * Time: 16:38:38
 * 
 */
public class DocumentViewImportHandler extends DefaultHandler {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(DocumentViewImportHandler.class);

    private String siteKey;

    private File archive;
    private NoCloseZipInputStream zis;
    private ZipEntry nextEntry;
    private List<String> fileList = new ArrayList<String>();

    private Stack<JCRNodeWrapper> nodes = new Stack<JCRNodeWrapper>();
    private Stack<String> pathes = new Stack<String>();

    private Map<String, String> uuidMapping;
    private Map<String, String> pathMapping;
    private Map<String, List<String>> references = new HashMap<String, List<String>>();

    private String currentFilePath = null;

    private String ignorePath = null;

    private int error = 0;

    private boolean noRoot = false;
    private boolean resolveReferenceAtEnd = true;

    private int uuidBehavior = ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW;

    private JCRSessionWrapper session;
    private Map<String,String> placeHoldersMap = new HashMap<String,String>();

    private List<String> noCreateTypes = Arrays.asList("jnt:importDropBox");
    private List<String> noUpdateTypes = Arrays.asList("jnt:virtualsitesFolder", "jnt:usersFolder", "jnt:groupsFolder");

    public DocumentViewImportHandler(JCRSessionWrapper session, String rootPath, String siteKey) throws IOException {
        this(session, rootPath, null, null, siteKey);
    }

    public DocumentViewImportHandler(JCRSessionWrapper session, String rootPath, File archive, List<String> fileList, String siteKey) throws IOException {
        JCRNodeWrapper node = null;
        try {
            this.session = session;
            this.uuidMapping = session.getUuidMapping();
            this.pathMapping = session.getPathMapping();
            if (rootPath == null) {
                node = (JCRNodeWrapper) session.getRootNode();
            } else {
                node = (JCRNodeWrapper) session.getNode(rootPath);
            }
            if (node.isNodeType("jnt:user")) {
                placeHoldersMap.put("$user","u:" + node.getPath().substring(node.getPath().lastIndexOf("/") + 1));
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new IOException();
        }
        nodes.add(node);
        pathes.add("");

        this.archive = archive;
        this.fileList = fileList;
        this.siteKey = siteKey;
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        try {
            NamespaceRegistry nsRegistry = session.getWorkspace().getNamespaceRegistry();
            Set<String> prefixes = ImmutableSet.of(nsRegistry.getPrefixes());
            if (!prefixes.contains(prefix)) {
                nsRegistry.registerNamespace(prefix, uri);
                session.setNamespacePrefix(prefix, uri);
            }
        } catch (RepositoryException re) {
            throw new SAXException(re);
        }
    }    
    
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (error > 0) {
            error++;
            return;
        }

        String decodedLocalName = ISO9075.decode(localName);
        String decodedQName = qName.replace(localName, decodedLocalName);
        pathes.push(pathes.peek() + "/" + decodedQName);

        if (noRoot && pathes.size() <= 2) {
             session.getPathMapping().put("/" + decodedQName, nodes.peek().getPath().equals("/") ? "" : nodes.peek().getPath());return;
        }

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

            String pt = atts.getValue(Constants.JCR_PRIMARYTYPE);
            if (Constants.JAHIANT_VIRTUALSITE.equals(pt) && siteKey != null) {
                decodedQName = siteKey;
                pathMapping.put(path + "/", "/sites/"+ siteKey + "/");
                path = "/sites/"+ siteKey;
            }
            if (noCreateTypes.contains(pt)) {
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
            if (!isValid || child.getDefinition().allowsSameNameSiblings()) {
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

                    String uuid = atts.getValue("jcr:uuid");
                    String share = atts.getValue("j:share");
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
                                case ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW:
                                    try {
                                        JCRNodeWrapper node = session.getNodeByUUID(uuid);
                                        if (node.isNodeType("mix:shareable")) {
                                            // ..
                                        } else {
                                            throw new ItemExistsException(uuid);
                                        }
                                    } catch (ItemNotFoundException e) {
                                    }
                                case ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING:
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
                                case ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING:
                                    throw new UnsupportedOperationException();

                                case ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW:
                                    uuid = null;
                            }
                        }
                        child = nodes.peek().addNode(decodedQName, pt, uuid, created, createdBy, lastModified, lastModifiedBy);

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

                        if (child.isFile() && currentFilePath == null) {
                            currentFilePath = child.getPath();
                        }
//                    }
                }
            } else {
                if (child.hasPermission("jcr:modifyProperties") && child.isCheckedOut()) {
                    if (!noUpdateTypes.contains(child.getPrimaryNodeType().getName())) {
                        addMixins(child, atts);
                        setAttributes(child, atts);
                    }
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
                    logger.warn("Cannot add node type "+e.getMessage());
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
                    attrValue = attrValue.replace(placeHolder,placeHoldersMap.get(placeHolder));
                }
            }

            if (attrName.equals(Constants.JCR_PRIMARYTYPE)) {
                // nodeType = attrValue; // ?
            } else if (attrName.equals("j:share")) {
            } else if (attrName.equals(Constants.JCR_MIXINTYPES)) {
            } else if (attrName.equals(Constants.SITEID)) {
            } else if (attrName.equals(Constants.JCR_UUID)) {
                uuidMapping.put(attrValue, child.getIdentifier());
            } else if (attrName.equals(Constants.JCR_CREATED)) {
            } else if (attrName.equals(Constants.JCR_CREATEDBY)) {
            } else if (attrName.equals("j:password") && child.hasProperty("j:password")) {
            } else if (attrName.equals(Constants.JCR_MIMETYPE)) {
            } else {
                if ((attrName.equals(Constants.JCR_TITLE) || attrName
                        .equals("jcr:description"))
                        && !child.isNodeType(Constants.MIX_TITLE)) {
                    child.addMixin(Constants.MIX_TITLE);
                } else if (attrName.equals("j:defaultCategory")
                        && !child.isNodeType(Constants.JAHIAMIX_CATEGORIZED)) {
                    child.addMixin(Constants.JAHIAMIX_CATEGORIZED);
                }
                ExtendedPropertyDefinition propDef;
//                    if (lang != null && attrName.endsWith("_" + lang)) {
//                        propDef = nodes.peek().getApplicablePropertyDefinition(StringUtils.substringBeforeLast(attrName, "_" + lang));
//                    } else if (!"jcr:language".equals(attrName) && child.isNodeType("jnt:translation")) {
//                        propDef = nodes.peek().getApplicablePropertyDefinition(attrName);
//                    } else {
                    propDef = child.getApplicablePropertyDefinition(attrName);
                    if (propDef == null) {
                        logger.error("Couldn't find definition for property " + attrName);
                        continue;
                    }
//                    }

                if (propDef.getRequiredType() == PropertyType.REFERENCE || propDef.getRequiredType() == ExtendedPropertyType.WEAKREFERENCE) {
                    if (attrValue.length() > 0) {
                        String[] values = attrValue.split(" ");
                        for (String value : values) {
                            for (Map.Entry<String, String> entry : pathMapping.entrySet()) {
                                if (value.startsWith(entry.getKey())) {
                                    value = entry.getValue() + StringUtils.substringAfter(value, entry.getKey());
                                    break;
                                }
                            }
                            if (!references.containsKey(value)) {
                                references.put(value, new ArrayList<String>());
                            }
                            references.get(value).add(child.getIdentifier() + "/" + attrName);                            
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
            zis = new NoCloseZipInputStream(new FileInputStream(archive));
            nextEntry = zis.getNextEntry();
        }
        String path = pathes.peek();
        if (path.endsWith("/jcr:content")) {
            String[] p = path.split("/");
            path = path.replace("/jcr:content", "/" + p[p.length - 2]);
        } else {
            path = path.replace(":", "_");
        }
        int fileIndex = fileList.indexOf(path);
        if (fileIndex != -1) {
            if (fileList.indexOf("/" + nextEntry.getName().replace('\\', '/')) > fileIndex) {
                zis.reallyClose();
                zis = new NoCloseZipInputStream(new FileInputStream(archive));
            }
            do {
                nextEntry = zis.getNextEntry();
            } while (!("/" + nextEntry.getName().replace('\\', '/')).equals(path));

            return true;
        }
        return false;
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (error > 0) {
            error--;
            return;
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

    public void setNoRoot(boolean noRoot) {
        this.noRoot = noRoot;
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
}
