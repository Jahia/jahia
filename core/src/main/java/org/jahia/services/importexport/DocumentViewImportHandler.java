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
package org.jahia.services.importexport;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.utils.zip.ZipEntry;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.ConstraintViolationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 11 févr. 2008
 * Time: 16:38:38
 * To change this template use File | Settings | File Templates.
 */
public class DocumentViewImportHandler extends DefaultHandler {
    private static Logger logger = Logger.getLogger(DocumentViewImportHandler.class);

    private ProcessingContext jParams;

    private File archive;
    private NoCloseZipInputStream zis;
    private ZipEntry nextEntry;
    private List<String> fileList = new ArrayList<String>();

    private Stack<JCRNodeWrapper> nodes = new Stack<JCRNodeWrapper>();
    private Stack<String> pathes = new Stack<String>();

    private Map<String, String> uuidMapping = new HashMap<String, String>();
    private Map<String, String> pathMapping = new HashMap<String, String>();
    private Map<String, List<String>> references = new HashMap<String, List<String>>();

    private String currentFilePath = null;

    private String ignorePath = null;

    private int error = 0;

    private JCRSessionWrapper session;

    public DocumentViewImportHandler(JCRSessionWrapper session, File archive, List<String> fileList, ProcessingContext jParams) throws IOException {
        this.jParams = jParams;
        JCRNodeWrapper node = null;
        try {
            this.session = session;
            node = (JCRNodeWrapper) session.getRootNode();
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new IOException();
        }
        nodes.add(node);
        pathes.add("");

        this.archive = archive;
        this.fileList = fileList;
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (error > 0) {
            error++;
            return;
        }

        String decodedLocalName = ISO9075.decode(localName);
        String decodedQName = qName.replace(localName, decodedLocalName);
        pathes.push(pathes.peek() + "/" + decodedQName);

        if (pathes.size() <= 2) {
            return;
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
            if (Constants.JAHIANT_VIRTUALSITE.equals(pt)) {
                decodedQName = jParams.getSiteKey();
                String newpath;
                JCRNodeWrapper siteFolder = JCRStoreService.getInstance().getSiteFolders(jParams.getSiteKey(), jParams.getUser()).get(0);
                newpath = siteFolder.getPath();
                pathMapping.put(path + "/", newpath + "/");
                path = newpath;
            }
            if ("jnt:importDropBox".equals(pt)) {
                ignorePath = path;
            }
            JCRNodeWrapper child = null;

            boolean isValid = true;
            try {
                child = session.getNode(path);
                if (child.isWriteable() && child.isVersioned() && !child.isCheckedOut()) {
                    child.checkout();
                }

            } catch (PathNotFoundException e) {
                isValid = false;
            }
            if (!isValid || child.getDefinition().allowsSameNameSiblings()) {
                if (nodes.peek().isWriteable()) {
                    if ("jnt:acl".equals(pt) && !nodes.peek().isNodeType("jmix:accessControlled")) {
                        nodes.peek().addMixin("jmix:accessControlled");
                    }
                    child = nodes.peek().addNode(decodedQName, pt);

                    addMixins(child, atts);

                    boolean contentFound = findContent();

                    if (contentFound) {
                        if (child.isFile()) {
                            String mime = atts.getValue(Constants.JCR_MIMETYPE);
                            child.getFileContent().uploadFile(zis, mime);
                            zis.close();
                        } else {
                            child.setProperty(Constants.JCR_DATA, zis);
                            child.setProperty(Constants.JCR_MIMETYPE, atts.getValue(Constants.JCR_MIMETYPE));
                            child.setProperty(Constants.JCR_LASTMODIFIED, Calendar.getInstance());
                            zis.close();
                        }
                    }

                    setAttributes(child, atts);

                    if (child.isCollection()) {
                    } else if (currentFilePath == null) {
                        currentFilePath = child.getPath();
                    }
                }
            } else {
                if (child.isWriteable() && child.isCheckedOut()) {
                    addMixins(child, atts);
                    setAttributes(child, atts);
                }
            }
            if (child == null) {
                error++;
            } else {
                nodes.push(child);
            }

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
                child.addMixin(st.nextToken());
            }
        }
    }

    private void setAttributes(JCRNodeWrapper child, Attributes atts) throws RepositoryException {
        String lang = null;
        if (child.getPrimaryNodeTypeName().equals("jnt:translation")) {
            lang = atts.getValue("jcr:language");
        }
        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getURI(i).equals("http://www.w3.org/2000/xmlns/")) {
                continue;
            }

            String attrName = ISO9075.decode(atts.getQName(i));
            String attrValue = atts.getValue(i);

            if (attrName.equals(Constants.JCR_PRIMARYTYPE)) {
                // nodeType = attrValue; // ?
            } else if (attrName.equals(Constants.JCR_MIXINTYPES)) {
            } else if (attrName.equals(Constants.JCR_UUID)) {
                uuidMapping.put(attrValue, child.getIdentifier());
            } else if (attrName.equals(Constants.JCR_CREATED)) {
            } else if (attrName.equals(Constants.JCR_CREATEDBY)) {
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
                try {
                    if (lang != null && attrName.endsWith("_" + lang)) {
                        propDef = nodes.peek().getApplicablePropertyDefinition(StringUtils.substringBeforeLast(attrName, "_" + lang));
                    } else {
                        propDef = child.getApplicablePropertyDefinition(attrName);
                    }
                } catch (ConstraintViolationException e) {
                    logger.error(e.getMessage());
                    continue;
                }

                if (propDef.getRequiredType() == PropertyType.REFERENCE) {
                    if (!references.containsKey(attrValue)) {
                        references.put(attrValue, new ArrayList<String>());
                    }
                    references.get(attrValue).add(child.getIdentifier() + "/" + attrName);
                } else if (propDef.getRequiredType() == ExtendedPropertyType.WEAKREFERENCE) {
                    if (!references.containsKey(attrValue)) {
                        references.put(attrValue, new ArrayList<String>());
                    }
                    references.get(attrValue).add(child.getIdentifier() + "/" + attrName);
                } else {
                    if (propDef.isProtected()) {
                        continue;
                    }

                    if (propDef.isMultiple()) {
                        String[] s = "".equals(attrValue) ? new String[0] : attrValue.split(" ");
                        Value[] v = new Value[s.length];
                        for (int j = 0; j < s.length; j++) {
                            v[j] = child.getRealNode().getSession().getValueFactory().createValue(s[j]);
                        }
                        child.setProperty(attrName, v);
                    } else {
                        child.setProperty(attrName, attrValue);
                    }
                }
            }
        }
    }

    private boolean findContent() throws IOException {
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

        if (fileList.contains(path)) {
            if (fileList.indexOf("/" + nextEntry) > fileList.indexOf(pathes.peek())) {
                zis.reallyClose();
                zis = new NoCloseZipInputStream(new FileInputStream(archive));
            }
            do {
                nextEntry = zis.getNextEntry();
            } while (!("/" + nextEntry.getName()).equals(path));

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
    }

    public Map<String, String> getUuidMapping() {
        return uuidMapping;
    }

    public Map<String, String> getPathMapping() {
        return pathMapping;
    }

    public Map<String, List<String>> getReferences() {
        return references;
    }

    public void setUuidMapping(Map<String, String> uuidMapping) {
        this.uuidMapping = uuidMapping;
    }

    public void setPathMapping(Map<String, String> pathMapping) {
        this.pathMapping = pathMapping;
    }

    public void setReferences(Map<String, List<String>> references) {
        this.references = references;
    }
}
