/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.importexport;

import org.apache.jackrabbit.util.ISO9075;
import org.jahia.api.Constants;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.utils.zip.ZipEntry;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.jcr.nodetype.NodeType;
import javax.jcr.Value;
import javax.jcr.RepositoryException;
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

    private ProcessingContext jParams;

    private File archive;
    private NoCloseZipInputStream zis;
    private ZipEntry nextEntry;
    private List<String> fileList = new ArrayList<String>();

    private Stack<JCRNodeWrapper> nodes = new Stack<JCRNodeWrapper>();
    private Stack<String> pathes = new Stack<String>();

    private Map<String,String> uuidMapping = new HashMap<String,String>();
    private Map<String,String> pathMapping = new HashMap<String,String>();

    private String currentFilePath = null;

    private String ignorePath = null;

    public DocumentViewImportHandler(ProcessingContext jParams, File archive, List<String> fileList) {
        this.jParams = jParams;

        JCRNodeWrapper node = ServicesRegistry.getInstance().getJCRStoreService().getFileNode("/", jParams.getUser());
        nodes.add(node);
        pathes.add("");

        this.archive = archive;
        this.fileList = fileList;
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        try {
            String decodedLocalName = ISO9075.decode(localName);
            String decodedQName = qName.replace(localName, decodedLocalName);
            pathes.push(pathes.peek() + "/" + decodedQName);

            if (ignorePath != null) {
                nodes.push(null);
                return;
            }

            String path;
            if (nodes.peek().getPath().equals("/")) {
                path ="/" + decodedQName;
            } else {
                path = nodes.peek().getPath() + "/" + decodedQName;
            }

            String pt = atts.getValue("jcr:primaryType");
            if (Constants.JAHIANT_VIRTUALSITE.equals(pt)) {
                decodedQName = jParams.getSiteKey();
                String newpath;
                JCRNodeWrapper siteFolder = JCRStoreService.getInstance().getSiteFolders(jParams.getSiteKey(), jParams.getUser()).get(0);
                newpath = siteFolder.getPath();
                pathMapping.put(path + "/",newpath +"/");
                path = newpath;
            }
            if ("jnt:importDropBox".equals(pt)) {
                ignorePath = path;
            }
            JCRNodeWrapper child = ServicesRegistry.getInstance().getJCRStoreService().getFileNode(path, jParams.getUser());
            if (!child.isValid() || child.getDefinition().allowsSameNameSiblings()) {
                if (nodes.peek().isWriteable()) {
                    child = nodes.peek().addNode(decodedQName, pt);

                    String m = atts.getValue(Constants.JCR_MIXINTYPES);
                    if (m != null) {
                        StringTokenizer st = new StringTokenizer(m," ,");
                        while (st.hasMoreTokens()) {
                            child.addMixin(st.nextToken());
                        }
                    }

                    boolean contentFound = findContent();

                    if (contentFound) {
                        if (child.isFile()) {
                            String mime = atts.getValue(Constants.JCR_MIMETYPE);
                            child.getFileContent().uploadFile(zis, mime);
                            zis.close() ;
                        } else {
                            child.setProperty(Constants.JCR_DATA, zis);
                            child.setProperty(Constants.JCR_MIMETYPE, atts.getValue(Constants.JCR_MIMETYPE));
                            child.setProperty(Constants.JCR_LASTMODIFIED, Calendar.getInstance());
                            zis.close();
                        }
                    }

                    Map<String, ExtendedPropertyDefinition> defs = new HashMap<String, ExtendedPropertyDefinition>();
                    NodeTypeRegistry reg = NodeTypeRegistry.getInstance();
                    ExtendedNodeType nt = null;
                    nt = reg.getNodeType(child.getPrimaryNodeType().getName());
                    defs.putAll(nt.getPropertyDefinitionsAsMap());
                    NodeType[] p = child.getMixinNodeTypes();
                    for (int i = 0; i < p.length; i++) {
                        defs.putAll(reg.getNodeType(p[i].getName()).getPropertyDefinitionsAsMap());
                    }

                    for (int i = 0; i < atts.getLength(); i++) {
                        if (atts.getURI(i).equals("http://www.w3.org/2000/xmlns/")) {
                            continue;
                        }

                        String attrName = ISO9075.decode(atts.getQName(i));
                        String attrValue = atts.getValue(i);

                        ExtendedPropertyDefinition propDef = defs.get(attrName);
                        if (propDef == null) {
                            propDef = defs.get("*");
                            if (propDef == null) {
                                continue;
                            }
                        }

                        if (attrName.equals(Constants.JCR_PRIMARYTYPE)) {
                            // nodeType = attrValue; // ?
                        } else if (attrName.equals(Constants.JCR_MIXINTYPES)) {

                        } else if (attrName.equals(Constants.JCR_UUID)) {
                            uuidMapping.put(attrValue, child.getUUID());
                        } else if (attrName.equals(Constants.JCR_CREATED)) {

                        } else if (attrName.equals(Constants.JCR_MIMETYPE)) {

                        } else {
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

                    if (child.isCollection()) {
                        nodes.peek().saveSession();
                    } else if (currentFilePath == null) {
                        currentFilePath = child.getPath();
                    }
                }
            }

            nodes.push(child);

        } catch (Exception re) {
            throw new SAXException(re);
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
            path = path.replace("/jcr:content", "/"+p[p.length-2]);
        } else {
            path = path.replace(":","_");
        }

        if (fileList.contains(path)) {
            if (fileList.indexOf("/"+nextEntry) > fileList.indexOf(pathes.peek())) {
                zis.reallyClose();
                zis = new NoCloseZipInputStream(new FileInputStream(archive));
            }
            do {
                nextEntry = zis.getNextEntry();
            } while (!("/"+nextEntry.getName()).equals(path));

            return true;
        }
        return false;
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        JCRNodeWrapper w = nodes.pop();
        pathes.pop();

        if (w != null && ignorePath != null && w.getPath().equals(ignorePath)) {
            ignorePath = null;
        }
        if (w != null && currentFilePath != null && w.getPath().equals(currentFilePath)) {
            currentFilePath = null;
            try {
                nodes.peek().saveSession();
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
    }

    public Map<String, String> getUuidMapping() {
        return uuidMapping;
    }

    public Map<String, String> getPathMapping() {
        return pathMapping;
    }
}
