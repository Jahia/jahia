/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.commons.Version;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.utils.xml.JahiaDocumentBuilderFactory;
import org.jahia.utils.zip.DirectoryZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.jcr.RepositoryException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ExternalUsersImportUpdater extends ImportFileUpdater {

    private static Logger logger = LoggerFactory.getLogger(ExternalUsersImportUpdater.class);

    private static final String LIVE_REPOSITORY_XML = "live-repository.xml";
    private static final String REPOSITORY_XML = "repository.xml";
    private static final String LIVE_CONTENT = "live-content";
    private static final String CONTENT = "content";

    public boolean mustUpdate(Version version, int buildNumber) {
        Version sevenOne = new Version("7.1");
        return version.compareTo(sevenOne) < 0 || ( version.compareTo(sevenOne) == 0 && buildNumber < 50366);  // User & group refactoring
    }

    public File updateImport(File importFile, String fileName, String fileType)  {
        Map<String, String> pathMapping = new HashMap<String, String>();
        File newImportFile = null;
        ZipInputStream zin = null;
        OutputStream out = null;
        ZipOutputStream zout = null;
        boolean updated = false;
        try {
            newImportFile = File.createTempFile("import", ".zip");
            zin = importFile.isDirectory() ? new DirectoryZipInputStream(importFile) : new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(importFile)));
            out = new FileOutputStream(newImportFile);
            zout = new ZipOutputStream(out);

            while (true) {
                ZipEntry zipentry = zin.getNextEntry();
                if (zipentry == null) break;
                String name = zipentry.getName();

                if (LIVE_REPOSITORY_XML.equals(name) || REPOSITORY_XML.equals(name)) {
                    zout.putNextEntry(new ZipEntry(name));
                    if ("users.zip".equalsIgnoreCase(fileName)) {
                        updated |= transform(zin, zout, pathMapping);
                    } else {
                        updated |= clean(zin, zout, pathMapping);
                    }
                }

            }
            if (!updated) {
                FileUtils.deleteQuietly(newImportFile);
                return importFile;
            }

            zin.closeEntry();
            if (zin instanceof NoCloseZipInputStream) {
                ((NoCloseZipInputStream) zin).reallyClose();
            }
            zin = importFile.isDirectory() ? new DirectoryZipInputStream(importFile) : new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(importFile)));
            while (true) {
                ZipEntry zipentry = zin.getNextEntry();
                if (zipentry == null) break;
                String name = zipentry.getName();

                if (!LIVE_REPOSITORY_XML.equals(name) && !REPOSITORY_XML.equals(name)) {
                    if (name.startsWith(LIVE_CONTENT + "/")) {
                        for (String key : pathMapping.keySet()) {
                            if (StringUtils.startsWith(name, LIVE_CONTENT + key)) {
                                name = StringUtils.replace(name, LIVE_CONTENT + key, LIVE_CONTENT + pathMapping.get(key));
                                break;
                            }
                        }
                    } else if (name.startsWith(CONTENT + "/")) {
                        for (String key : pathMapping.keySet()) {
                            if (StringUtils.startsWith(name, CONTENT + key)) {
                                name = StringUtils.replace(name, CONTENT + key, CONTENT + pathMapping.get(key));
                                break;
                            }
                        }
                    }
                    File content = File.createTempFile("content", null);
                    IOUtils.copy(zin, new FileOutputStream(content));
                    zout.putNextEntry(new ZipEntry(name));
                    IOUtils.copy(new FileInputStream(content), zout);
                }

            }
            JCRSessionFactory.getInstance().getCurrentUserSession().getPathMapping().putAll(pathMapping);
            return newImportFile;
        } catch (IOException | RepositoryException | ParserConfigurationException | XPathExpressionException | SAXException | TransformerException e) {
            logger.error("An error occurred while updating import file", e);
        } finally {
            try {
                if (zout != null) {
                    zout.closeEntry();
                    zout.finish();
                }
                if (out != null) {
                    out.close();
                }
                if (zin != null) {
                    zin.closeEntry();
                    if (zin instanceof NoCloseZipInputStream) {
                        ((NoCloseZipInputStream) zin).reallyClose();
                    }
                }
            } catch (IOException e) {
                logger.debug("Stream already closed", e);
            }
        }

        FileUtils.deleteQuietly(newImportFile);
        return importFile;
    }


    private boolean clean(InputStream inputStream, OutputStream outputStream, Map<String, String> pathMapping) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, TransformerException {
        Document doc = JahiaDocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(inputStream));

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList) xpath.evaluate("//*[@*[name()='jcr:primaryType'] = 'jnt:usersFolder']", doc, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node legacyExtUser = nodes.item(i);
            Node parent = legacyExtUser.getParentNode();
            if (!getNodePath(parent).startsWith("/sites/")) {
                parent.removeChild(legacyExtUser);
            }
        }

        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(new DOMSource(doc), new StreamResult(outputStream));

        return nodes.getLength() > 0;
    }

    private boolean transform(InputStream inputStream, OutputStream outputStream, Map<String, String> pathMapping) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, TransformerException {
        Document doc = JahiaDocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(inputStream));

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList) xpath.evaluate("//*[@*[name()='jcr:primaryType'] = 'jnt:user' and @*[name()='j:external'] = 'true']", doc, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node legacyExtUser = nodes.item(i);
            ArrayList<Node> tree = new ArrayList<Node>();
            Element extUser = (Element) legacyExtUser.cloneNode(true);
            extUser.setAttribute("jcr:primaryType", "jnt:externalUser");
            String externalSource = extUser.getAttribute("j:externalSource");
            extUser.setAttribute("j:externalSource", externalSource + ".users");
            tree.add(extUser);

            Node parent = legacyExtUser.getParentNode();
            parent.removeChild(legacyExtUser);
            boolean removeParent = !hasChildElement(parent);
            while (parent != null && !"users".equals(parent.getNodeName())) {
                tree.add(0, parent.cloneNode(false));
                Node n = parent.getParentNode();
                if (removeParent) {
                    n.removeChild(parent);
                    removeParent = !hasChildElement(n);
                }
                parent = n;
            }
            if (parent == null) continue;
            StringBuilder mappingSrc = new StringBuilder(getNodePath(parent));
            StringBuilder mappingDst = new StringBuilder(getNodePath(parent));

            NodeList nodeList = ((Element) parent).getElementsByTagName("providers");
            if (nodeList.getLength() == 0) {
                Element e = doc.createElement("providers");
                e.setAttribute("jcr:primaryType", "jnt:usersFolder");
                e.setAttribute("jcr:mixinTypes", "jmix:hasExternalProviderExtension");
                e.setAttribute("j:published", "true");
                e.setAttribute("j:publicationStatus", "1");
                parent.appendChild(e);
                parent = e;
            } else {
                parent = nodeList.item(0);
            }
            mappingDst.append("/").append(parent.getNodeName());

            nodeList = ((Element) parent).getElementsByTagName(externalSource);
            if (nodeList.getLength() == 0) {
                Element e = doc.createElement(externalSource);
                e.setAttribute("jcr:primaryType", "jnt:usersFolder");
                e.setAttribute("provider", externalSource + ".users");
                e.setAttribute("j:publicationStatus", "4");
                parent.appendChild(e);
                parent = e;
            } else {
                parent = nodeList.item(0);
            }
            mappingDst.append("/").append(parent.getNodeName());

            for (Node n : tree) {
                String nodeName = n.getNodeName();
                mappingSrc.append("/").append(nodeName);
                mappingDst.append("/").append(nodeName);
                nodeList = ((Element) parent).getElementsByTagName(nodeName);
                if (nodeList.getLength() == 0) {
                    Node node = parent.appendChild(n);
                    parent = node;
                } else {
                    parent = nodeList.item(0);
                }
            }
            pathMapping.put(mappingSrc.toString(), mappingDst.toString());
        }

        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(new DOMSource(doc), new StreamResult(outputStream));

        return nodes.getLength() > 0;
    }

    private boolean hasChildElement(Node node) {
        for (Node n = node.getFirstChild(); n.getNextSibling() != null; n = n.getNextSibling()) {
            if (n instanceof Element) {
                return true;
            }
        }
        return false;
    }

    private String getNodePath(Node node) {
        if (node.getParentNode() == null || node.getParentNode() instanceof Document) {
            return "";
        } else {
            return getNodePath(node.getParentNode()) + "/" + node.getNodeName();
        }
    }
}
