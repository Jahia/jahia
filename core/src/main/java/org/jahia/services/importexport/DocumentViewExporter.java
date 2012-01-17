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
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.value.ValueHelper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.jcr.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler for export in a document view format.
 * User: toto
 * Date: Dec 17, 2009
 * Time: 3:02:35 PM
 * 
 */
public class DocumentViewExporter {
    protected static final Logger logger = org.slf4j.LoggerFactory.getLogger(DocumentViewExporter.class);
    
    private static final String CDATA = "CDATA";
    private static final String NS_URI = "http://www.w3.org/2000/xmlns/";

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("/sites/[^/]*/templates/(.*)");


    private JCRSessionWrapper session;
    private JCRSessionWrapper publicationStatusSession;
    private ContentHandler ch;
    private boolean noRecurse;
    private boolean skipBinary;
    private Set<String> typesToIgnore = new HashSet<String>();
    private Set<String> externalReferences = new HashSet<String>();
    private Map<String, String> prefixes;
    private HashMap<String, String> exportedShareable;
    private JCRNodeWrapper rootNode;
    private List<JCRNodeWrapper> nodesList;
    private Stack<String> stack;

    private List<String> propertiestoIgnore = Arrays.asList("jcr:predecessors", "j:nodename", "jcr:versionHistory", "jcr:baseVersion", "jcr:isCheckedOut", "jcr:uuid");

    public DocumentViewExporter(JCRSessionWrapper session, ContentHandler ch, boolean skipBinary, boolean noRecurse) {
        this.session = session;
        this.ch = ch;
        this.noRecurse = noRecurse;
        this.skipBinary = skipBinary;

        this.stack = new Stack<String>();

        prefixes = new HashMap<String, String>();
        try {
            Map<String, String> map = NodeTypeRegistry.getInstance().getNamespaces();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (!StringUtils.isEmpty(entry.getKey()) && !entry.getKey().startsWith(Name.NS_XML_PREFIX)) {
                    prefixes.put(entry.getKey(), entry.getValue());
                }

            }

            for (String prefix : session.getNamespacePrefixes()) {
                if (!StringUtils.isEmpty(prefix) && !prefix.startsWith(Name.NS_XML_PREFIX) && !map.containsKey(prefix)) {
                    prefixes.put(prefix, session.getNamespaceURI(prefix));
                }
            }
        } catch (RepositoryException e) {
            logger.warn("Namespace not correctly exported", e);
        }

        exportedShareable = new HashMap<String, String>();
    }

    public void setTypesToIgnore(Set<String> typesToIgnore) {
        this.typesToIgnore = typesToIgnore;
    }

    public void setPublicationStatusSession(JCRSessionWrapper publicationStatusSession) {
        this.publicationStatusSession = publicationStatusSession;
    }

    public void export(JCRNodeWrapper node) throws SAXException, RepositoryException {
        TreeSet<JCRNodeWrapper> set = new TreeSet<JCRNodeWrapper>();
        set.add(node);
        export(node, set);
    }

    public void export(JCRNodeWrapper rootNode, SortedSet<JCRNodeWrapper> nodes) throws SAXException, RepositoryException {
        this.rootNode = rootNode;

        ch.startDocument();

        nodesList = new ArrayList<JCRNodeWrapper>(nodes);
        for (int i = 0; i<nodesList.size(); i++) {
            List<JCRNodeWrapper> subList = new ArrayList<JCRNodeWrapper>(nodesList.subList(i, nodesList.size()));
            Collections.sort(subList, nodes.comparator());
            nodesList.removeAll(subList);
            nodesList.addAll(subList);
            exportNode(nodesList.get(i));
        }
//        for (Iterator<JCRNodeWrapper> iterator = nodes.iterator(); iterator.hasNext();) {
//            JCRNodeWrapper node = iterator.next();
//            exportNode(node);
//        }
        while (!stack.isEmpty()) {
            String end = stack.pop();
            String name = end.substring(end.lastIndexOf('/') + 1);
            String encodedName = ISO9075.encode(name);
            endElement(encodedName);
        }

        ch.endDocument();
    }

    private void exportNode(JCRNodeWrapper node) throws SAXException, RepositoryException {
        if (node.getProvider().isExportable() && !typesToIgnore.contains(node.getPrimaryNodeTypeName())) {

            String path = "";
            Node current = node;
            while (!current.getPath().equals("/")) {
                path = "/" + current.getName() + path;
                current = current.getParent();
            }
            if (path.equals("")) {
                path = "/";
            }

            if (!path.equals(rootNode.getPath())) {

                String parentpath = path.substring(0, path.lastIndexOf('/'));

                while (!stack.isEmpty() && !parentpath.startsWith(stack.peek()+"/") && !parentpath.equals(stack.peek())) {
                    String end = stack.pop();
                    if (stack.isEmpty()) {
                        throw new RepositoryException("Node not in path : " + node.getPath());
                    }
                    String name = end.substring(end.lastIndexOf('/') + 1);
                    String encodedName = ISO9075.encode(name);
                    endElement(encodedName);
                }
                if (stack.isEmpty() && !node.getPath().equals(rootNode.getPath())) {
                    String name = rootNode.getName();
                    String encodedName = ISO9075.encode(name);
                    AttributesImpl atts = new AttributesImpl();
                    startElement(encodedName, atts);
                    if (rootNode.getPath().equals("/")) {
                        stack.push("");
                    } else {
                        stack.push(rootNode.getPath());
                    }
                }

                while (!stack.isEmpty() && !stack.peek().equals(parentpath)) {
                    String peek = stack.peek();
                    String name = parentpath.substring(peek.length() + 1);
                    if (name.contains("/")) {
                        name = name.substring(0, name.indexOf('/'));
                    }
                    String encodedName = ISO9075.encode(name);
                    String currentpath = peek + "/" + name;
                    String pt = session.getNode(currentpath).getPrimaryNodeTypeName();
                    AttributesImpl atts = new AttributesImpl();
                    atts.addAttribute(Name.NS_JCR_URI, "primaryType", "jcr:primaryType", CDATA, pt);
                    startElement(encodedName, atts);
                    stack.push(currentpath);
                }
            }
            AttributesImpl atts = new AttributesImpl();
            if (node.isNodeType("jmix:shareable")) {

                if (exportedShareable.containsKey(node.getIdentifier())) {
                    atts.addAttribute(Constants.JAHIA_NS,  "share", "j:share", CDATA, exportedShareable.get(node.getIdentifier()));
                    String encodedName = ISO9075.encode(node.getName());
                    startElement(encodedName, atts);
                    endElement(encodedName);
                    return;
                } else {
                    exportedShareable.put(node.getIdentifier(), node.getPath());
                }                
            }
            PropertyIterator propsIterator = node.getRealNode().getProperties();
            SortedSet<String> sortedProps = new TreeSet<String>();
            while (propsIterator.hasNext()) {
                sortedProps.add(propsIterator.nextProperty().getName());
            }
            for (String prop : sortedProps) {
                try {
                    Property property = node.getRealNode().getProperty(prop);
                    if ((property.getType() != PropertyType.BINARY || !skipBinary) && !propertiestoIgnore.contains(property.getName())) {
                        String key = property.getName();
                        String prefix = null;
                        String localname = key;
                        if (key.indexOf(':') > -1) {
                            prefix = key.substring(0, key.indexOf(':'));
                            localname = key.substring(key.indexOf(':') + 1);
                        }

                        String attrName = ISO9075.encode(localname);

                        String value;
                        if (!property.isMultiple()) {
                            value = getValue(property.getValue());
                        } else {
                            Value[] vs = property.getValues();
                            StringBuffer b = new StringBuffer();
                            for (int i = 0; i < vs.length; i++) {
                                Value v = vs[i];
                                b.append(getValue(v));
                                if (i + 1 < vs.length) {
                                    b.append(" ");
                                }
                            }
                            value = b.toString();
                        }

                        if (prefix == null) {
                            atts.addAttribute("", localname, attrName, CDATA, value);
                        } else {
                            atts.addAttribute(prefixes.get(prefix), localname, prefix + ":" + attrName, CDATA, value);
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error("Cannot export property",e);
                }
            }
            if (publicationStatusSession != null && node.isNodeType("jmix:publication")) {
                String s = Integer.toString(JCRPublicationService.getInstance().getStatus(node, publicationStatusSession, null));
                atts.addAttribute(prefixes.get("j"), "publicationStatus", "j:publicationStatus", CDATA, s);
            }

            String encodedName = ISO9075.encode(node.getName());
            startElement(encodedName, atts);
            if (path.equals("/")) {
                stack.push("");
            } else {
                stack.push(path);
            }

            if (!noRecurse) {
                NodeIterator ni = node.getNodes();
                while (ni.hasNext()) {
                    JCRNodeWrapper c = (JCRNodeWrapper) ni.next();
                    exportNode(c);
                }
            }
        }
    }

    private String getValue(Value v) throws RepositoryException {
        if (v.getType() == PropertyType.REFERENCE || v.getType() == PropertyType.WEAKREFERENCE) {
            try {
                JCRNodeWrapper reference = session.getNodeByUUID(v.getString());
                String path = reference.getPath();

                Matcher matcher = TEMPLATE_PATTERN.matcher(path);
                if (matcher.matches()) {
                    path = "$currentSite/templates/"+matcher.group(1);

                } else {
                    boolean root = rootNode.getPath().equals("/");
                    if (!root && !path.startsWith(rootNode.getPath()+"/") && !path.equals(rootNode.getPath())) {
                        externalReferences.add(v.getString());
                    } else if (!typesToIgnore.contains(reference.getPrimaryNodeTypeName()) && reference.getResolveSite().getSiteKey().equals(JahiaSitesBaseService.SYSTEM_SITE_KEY)) {
                        boolean foundInExportedNodes = false;

                        for (JCRNodeWrapper node : nodesList) {
                            if (path.startsWith(node.getPath()+"/") || path.equals(node.getPath())) {
                                foundInExportedNodes = true;
                                break;
                            }
                        }
                        if (!foundInExportedNodes) {
                            nodesList.add(reference);
                        }
                        if (rootNode.getPath().equals(path)) {
                            path = "#/";
                        } else {
                            path = "#" + path.substring(rootNode.getPath().length() + (root ? -1 : 0));
                        }
                    }
                }
                return ISO9075.encode(path);
            } catch (ItemNotFoundException e) {
                return "";
            }
        } else if (v.getType() == PropertyType.BINARY) {
            return ValueHelper.serialize(v, false);
        }

        return v.getString();
    }

    private void startElement(String qualifiedName, AttributesImpl atts) throws SAXException {
        if (qualifiedName.equals("")) {
            qualifiedName = "content";
        }

        if (stack.isEmpty()) {
            for (Iterator<String> iterator = prefixes.keySet().iterator(); iterator.hasNext();) {
                String prefix = iterator.next();
                String uri = prefixes.get(prefix);
                atts.addAttribute(NS_URI, prefix, "xmlns:" + prefix, CDATA, uri);
                ch.startPrefixMapping(prefix, uri);
                ch.endPrefixMapping(prefix);
            }
        }

        ch.startElement("", qualifiedName, qualifiedName, atts);
    }

    private void endElement(String qualifiedName) throws SAXException {
        if (qualifiedName.equals("")) {
            qualifiedName = "content";
        }
        ch.endElement("", qualifiedName, qualifiedName);
    }

    public List<String> getPropertiestoIgnore() {
        return propertiestoIgnore;
    }

    public void setPropertiestoIgnore(List<String> propertiestoIgnore) {
        this.propertiestoIgnore = propertiestoIgnore;
    }

    public Set<String> getExternalReferences() {
        return externalReferences;
    }

    public void setExternalReferences(Set<String> externalReferences) {
        this.externalReferences = externalReferences;
    }

    public List<JCRNodeWrapper> getNodesList() {
        return nodesList;
    }
}
