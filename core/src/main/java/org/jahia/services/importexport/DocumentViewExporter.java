/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.value.ValueHelper;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRMultipleValueUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.sites.JahiaSitesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import pl.touk.throwing.ThrowingPredicate;

import javax.jcr.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler for export in a document view format.
 * User: toto
 * Date: Dec 17, 2009
 * Time: 3:02:35 PM
 */
public class DocumentViewExporter {
    protected static final Logger logger = LoggerFactory.getLogger(DocumentViewExporter.class);

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

    private List<String> propertiestoIgnore = Arrays.asList("jcr:predecessors", "j:nodename", "jcr:versionHistory", "jcr:baseVersion", "jcr:isCheckedOut", "jcr:uuid", "jcr:mergeFailed");
    private ExportContext exportContext;
    private PropertyChangeSupport pcs;

    public DocumentViewExporter(JCRSessionWrapper session, ContentHandler ch, boolean skipBinary, boolean noRecurse) {
        this.session = session;
        this.ch = ch;
        this.noRecurse = noRecurse;
        this.skipBinary = skipBinary;
        this.stack = new Stack<String>();
        this.pcs = new PropertyChangeSupport(this);
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
        TreeSet<JCRNodeWrapper> set = new TreeSet<JCRNodeWrapper>(new Comparator<JCRNodeWrapper>() {
            public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
                return o1.getPath().compareTo(o2.getPath());
            }
        });
        set.add(node);
        export(node, set);
    }

    public void export(JCRNodeWrapper rootNode, SortedSet<JCRNodeWrapper> nodes) throws SAXException, RepositoryException {
        this.rootNode = rootNode;

        ch.startDocument();

        nodesList = new ArrayList<JCRNodeWrapper>(nodes);
        long nodesListSize = nodesList.size();
        for (int i = 0; i < nodesList.size(); i++) {
            if (nodesList.size() != nodesListSize) {
                // We have add node to be exported ensure we export them by path order
                final List<JCRNodeWrapper> subList = new ArrayList<JCRNodeWrapper>(nodesList.subList(i, nodesList.size()));
                Collections.sort(subList, nodes.comparator());
                nodesList.removeAll(subList);
                nodesList.addAll(subList);
                nodesListSize = nodesList.size();
            }
            if (nodesList.get(i).getProvider().canExportNode(nodesList.get(i))) {
                exportNode(nodesList.get(i));
            }
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
        try {
            if (typesToIgnore.stream().noneMatch(ThrowingPredicate.unchecked(node::isNodeType))) {
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

                    while (!stack.isEmpty() && !parentpath.startsWith(stack.peek() + "/") && !parentpath.equals(stack.peek())) {
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
                        JCRNodeWrapper n = session.getNode(currentpath);
                        String pt = n.getPrimaryNodeTypeName();
                        AttributesImpl atts = new AttributesImpl();
                        atts.addAttribute(Name.NS_JCR_URI, "primaryType", "jcr:primaryType", CDATA, pt);
                        setProviderRootAttribute(n, atts);
                        startElement(encodedName, atts);
                        stack.push(currentpath);
                    }
                }
                AttributesImpl atts = new AttributesImpl();
                if (node.isNodeType("jmix:shareable")) {

                    if (exportedShareable.containsKey(node.getIdentifier())) {
                        atts.addAttribute(Constants.JAHIA_NS, "share", "j:share", CDATA, exportedShareable.get(node.getIdentifier()));
                        String encodedName = ISO9075.encode(node.getName());
                        startElement(encodedName, atts);
                        endElement(encodedName);
                        notifyListObservers(node.getPath());
                        return;
                    } else {
                        exportedShareable.put(node.getIdentifier(), node.getPath());
                    }
                }
                PropertyIterator propsIterator = node.getRealNode().getProperties();
                SortedSet<String> sortedProps = new TreeSet<String>();
                while (propsIterator.hasNext()) {
                    Property property = propsIterator.nextProperty();
                    if (node.getProvider().canExportProperty(property)) {
                        sortedProps.add(property.getName());
                    }
                }
                for (String prop : sortedProps) {
                    try {
                        Property property = node.getRealNode().getProperty(prop);
                        if (node.hasProperty(prop) && (property.getType() != PropertyType.BINARY || !skipBinary) && !propertiestoIgnore.contains(property.getName())) {
                            String key = property.getName();
                            String prefix = null;
                            String localname = key;
                            if (key.indexOf(':') > -1) {
                                prefix = key.substring(0, key.indexOf(':'));
                                localname = key.substring(key.indexOf(':') + 1);
                            }

                            String encodedLocalName = ISO9075.encode(localname);

                            String value;
                            if (!property.isMultiple()) {
                                if (property.getDefinition().getRequiredType() == PropertyType.REFERENCE || property.getDefinition().getRequiredType() == ExtendedPropertyType.WEAKREFERENCE) {
                                    value = JCRMultipleValueUtils.encode(getValue(property.getValue()));
                                } else {
                                    value = getValue(property.getValue());
                                }
                            } else {
                                Value[] vs = property.getValues();
                                List<String> values = new ArrayList<String>();
                                for (Value v : vs) {
                                    values.add(JCRMultipleValueUtils.encode(getValue(v)));
                                }
                                StringBuilder b = new StringBuilder();
                                for (int i = 0; i < values.size(); i++) {
                                    String v = values.get(i);
                                    b.append(v);
                                    if (i + 1 < values.size()) {
                                        b.append(" ");
                                    }
                                }
                                value = b.toString();
                            }

                            if (prefix == null) {
                                atts.addAttribute("", encodedLocalName, encodedLocalName, CDATA, value);
                            } else {
                                atts.addAttribute(prefixes.get(prefix), encodedLocalName, prefix + ":" + encodedLocalName, CDATA, value);
                            }
                        }
                    } catch (RepositoryException e) {
                        logger.error("Cannot export property", e);
                    }
                }
                if (publicationStatusSession != null && node.isNodeType("jmix:publication")) {
                    String s = Integer.toString(JCRPublicationService.getInstance().getStatus(node, publicationStatusSession, null));
                    atts.addAttribute(prefixes.get("j"), "publicationStatus", "j:publicationStatus", CDATA, s);
                }
                setProviderRootAttribute(node, atts);

                String encodedName = ISO9075.encode(node.getName());
                startElement(encodedName, atts);
                if (path.equals("/")) {
                    stack.push("");
                } else {
                    stack.push(path);
                }

                if (!noRecurse) {
                    List<String> exportedMountPointNodes = new ArrayList<String>();
                    NodeIterator ni = node.getNodes();
                    while (ni.hasNext()) {
                        JCRNodeWrapper c = (JCRNodeWrapper) ni.next();
                        if (c.getProvider().canExportNode(c) && !exportedMountPointNodes.contains(c.getName())) {
                            if (!"/".equals(path) && !c.getProvider().equals(node.getProvider())) { // is external provider root
                                String mountPointName = c.getName() + JCRMountPointNode.MOUNT_SUFFIX;
                                if (node.hasNode(mountPointName) && !exportedMountPointNodes.contains(mountPointName)) { // mounted from a dynamic mountPoint
                                    JCRNodeWrapper mountPointNode = node.getNode(mountPointName);
                                    if (mountPointNode.isNodeType(Constants.JAHIANT_MOUNTPOINT)) {
                                        exportNode(mountPointNode);
                                        exportedMountPointNodes.add(mountPointName);
                                    }
                                }
                            }
                            exportNode(c);
                            if (c.getName().endsWith(JCRMountPointNode.MOUNT_SUFFIX) && c.isNodeType(Constants.JAHIANT_MOUNTPOINT)) {
                                exportedMountPointNodes.add(c.getName());
                            }
                        }
                    }
                }
                notifyListObservers(node.getPath());
            }
        } catch (Exception e) {
            logger.warn("Unable to export node with path {}, it won't be part of the export. Set class in DEBUG to get full error", node.getPath());
            logger.debug(e.getMessage(), e);
        }
    }

    /**
     * Notify the list of observers about a node path being exported
     *
     * @param path the node path
     */
    private void notifyListObservers(String path) {
        if (exportContext != null) {
            exportContext.setActualPath(path);
            pcs.firePropertyChange(new PropertyChangeEvent(this, "exportContext", null, exportContext));
        }
    }

    private void setProviderRootAttribute(JCRNodeWrapper node, AttributesImpl atts) throws RepositoryException {
        if (!"/".equals(node.getPath()) && !node.getProvider().equals(node.getParent().getProvider())) {
            if (node.getProvider().isDynamicallyMounted()) {
                JCRNodeWrapper mountPoint = session.getNodeByIdentifier(node.getProvider().getKey());
                atts.addAttribute("", ImportExportBaseService.DYNAMIC_MOUNT_POINT_ATTR, ImportExportBaseService.DYNAMIC_MOUNT_POINT_ATTR, CDATA, mountPoint.getPath());
            } else {
                atts.addAttribute("", ImportExportBaseService.STATIC_MOUNT_POINT_ATTR, ImportExportBaseService.STATIC_MOUNT_POINT_ATTR, CDATA, node.getProvider().getKey());
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
                    path = "$currentSite/templates/" + matcher.group(1);

                } else {
                    boolean root = rootNode.getPath().equals("/");
                    if (!root && !path.startsWith(rootNode.getPath() + "/") && !path.equals(rootNode.getPath())) {
                        externalReferences.add(v.getString());
                    } else if (typesToIgnore.stream().noneMatch(ThrowingPredicate.unchecked(reference::isNodeType)) && reference.getResolveSite() != null) {
                        if (reference.getResolveSite().getSiteKey().equals(JahiaSitesService.SYSTEM_SITE_KEY)) {
                            boolean foundInExportedNodes = false;

                            for (JCRNodeWrapper node : nodesList) {
                                if (path.startsWith(node.getPath() + "/") || path.equals(node.getPath())) {
                                    foundInExportedNodes = true;
                                    break;
                                }
                            }
                            if (!foundInExportedNodes) {
                                nodesList.add(reference);
                            }
                        }
                        if (rootNode.getPath().equals(path)) {
                            path = "#/";
                        } else {
                            path = "#" + path.substring(rootNode.getPath().length() + (root ? -1 : 0));
                        }
                    }
                }
                return path;
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
            for (Iterator<String> iterator = (new TreeSet<String>(prefixes.keySet())).iterator(); iterator.hasNext(); ) {
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

    public void setExportContext(ExportContext exportContext) {
        this.exportContext = exportContext;
    }

    public ExportContext getExportContext() {
        return exportContext;
    }

    public void addObserver(PropertyChangeListener propertyChangeListener) {
        pcs.addPropertyChangeListener("exportContext", propertyChangeListener);
    }
}
