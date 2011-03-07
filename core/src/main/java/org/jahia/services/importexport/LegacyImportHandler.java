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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.util.ISO8601;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.importexport.DefinitionsMapping.Action;
import org.jahia.services.importexport.DefinitionsMapping.AddMixin;
import org.jahia.services.importexport.DefinitionsMapping.AddNode;
import org.jahia.services.importexport.DefinitionsMapping.SetProperties;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.Query;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA. User: toto Date: Oct 28, 2009 Time: 2:31:18 PM
 */
public class LegacyImportHandler extends DefaultHandler {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(LegacyImportHandler.class);

    private final static int CTX_PAGE = 0;
    private final static int CTX_CTN = 1;
    private final static int CTX_LIST = 2;
    private final static int CTX_FIELD = 3;
    private final static int CTX_SKIP = 4;
    private final static int CTX_SHAREABLE = 5;
    // private final static int CTX_MERGED = 5;
    private final static int CTX_BOX = 6;
    private final static int CTX_NAVLINK = 7;

    private NodeTypeRegistry registry;
    private DefinitionsMapping mapping;

    private Locale locale;

    private JCRNodeWrapper currentSiteNode;
    private Stack<PageContext> currentCtx = new Stack<PageContext>();

    private String originatingJahiaRelease = null;

    private int ctnId = 1;

    private Map<String, String> uuidMapping = new HashMap<String, String>();
    private Map<String, String> pathMapping = new HashMap<String, String>();
    private Map<String, List<String>> references = new HashMap<String, List<String>>();

    private JCRSessionWrapper session;

    private static final String HTTP_WWW_JAHIA_ORG = "http://www.jahia.org/";
    private static final String PAGE = "page";
    private static final String LINK = "link";

    private Set<String> readRoles = new HashSet<String>(Arrays.asList("visitor"));
    private Set<String> writeRoles = new HashSet<String>(Arrays.asList("viewer", "editor", "contributor"));
    private Set<String> adminRoles = new HashSet<String>(Arrays.asList("publisher", "owner"));

    private String currentNode;
    private int level = 0;

    public LegacyImportHandler(JCRSessionWrapper session, JCRNodeWrapper currentSiteNode, NodeTypeRegistry registry,
                               DefinitionsMapping mapping, Locale locale, String originatingJahiaRelease) {
        this.session = session;
        this.uuidMapping = session.getUuidMapping();
        this.pathMapping = session.getPathMapping();
        this.currentSiteNode = currentSiteNode;

        this.registry = registry;

        if (mapping == null) {
            mapping = new DefinitionsMapping();
        }

        this.mapping = mapping;
        this.locale = locale;
        this.originatingJahiaRelease = originatingJahiaRelease;
    }

    public void setReferences(Map<String, List<String>> references) {
        this.references = references;
    }

    /**
     * Receive notification of the start of an element.
     * <p/>
     * <p>
     * By default, do nothing. Application writers may override this method in a subclass to take specific actions at the start of each
     * element (such as allocating a new tree node or writing output to a file).
     * </p>
     *
     * @param uri        The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being
     *                   performed.
     * @param localName  The local name (without prefix), or the empty string if Namespace processing is not being performed.
     * @param qName      The qualified name (with prefix), or the empty string if qualified names are not available.
     * @param attributes The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            currentNode = localName;
            String uuid = attributes.getValue("jcr:uuid");

            int ctx = -1;
            if (!currentCtx.isEmpty()) {
                ctx = currentCtx.peek().ctx.peek();
            }

            logger.info(StringUtils.repeat(" ", level) + "<" + currentNode + "> , ctx = " + ctx);
            level++;
            if (ctx == -1 && HTTP_WWW_JAHIA_ORG.equals(uri) && PAGE.equals(localName)) {
                // System.out.println("create page" + attributes.getValue("jahia:title"));
                createPage(attributes.getValue(Name.NS_JCR_URI, "primaryType"), attributes.getValue("jahia:title"),
                        attributes.getValue("jahia:template"), attributes.getValue(HTTP_WWW_JAHIA_ORG, "pageKey"),
                        uuid, getMetadataForNodeCreation(attributes));
                setAcl(attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl"));
                return;
            }

            switch (ctx) {
                case CTX_PAGE:
                    if (localName.endsWith("List") && getCurrentContentType() != null &&
                            getCurrentContentType().getChildNodeDefinitionsAsMap()
                                    .containsKey(StringUtils.substringBeforeLast(localName, "List"))) {
                        // System.out.println("create list " + localName);
                        // Must be a container list
                        ExtendedNodeDefinition nodeDef = getCurrentContentType().getChildNodeDefinitionsAsMap()
                                .get(StringUtils.substringBeforeLast(localName, "List"));

                        createContentList(nodeDef, uuid, getMetadataForNodeCreation(attributes));
                        setMetadata(attributes);
                        setAcl(attributes.getValue(HTTP_WWW_JAHIA_ORG,"acl"));
                    } else {
                        logger.warn(
                                "Unexpected " + localName + " element in import file - skipping it and its subtree");
                        if (logger.isDebugEnabled() && localName.endsWith("List")) {
                            if (getCurrentContentType() == null) {
                                logger.debug("CurrentContentType is null!");
                            } else {
                                logger.debug("Only the following elements are allowed: " + getCurrentContentType().getChildNodeDefinitionsAsMap().keySet().toString());
                            }
                        }
                        currentCtx.peek().pushSkip();
                    }
                    break;
                case CTX_CTN:
                    if (localName.endsWith("List") && getCurrentContentType() != null &&
                            getCurrentContentType().getChildNodeDefinitionsAsMap()
                                    .containsKey(StringUtils.substringBeforeLast(localName, "List"))) {
                        // System.out.println("create list " + localName);
                        // Must be a container list
                        ExtendedNodeDefinition nodeDef = getCurrentContentType().getChildNodeDefinitionsAsMap()
                                .get(StringUtils.substringBeforeLast(localName, "List"));

                        createContentList(nodeDef, uuid, getMetadataForNodeCreation(attributes));
                        setMetadata(attributes);
                        setAcl(attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl"));
                    } else {
                        // System.out.println("create field " + localName);
                        ExtendedItemDefinition itemDef;
                        if (getCurrentContentType().getChildNodeDefinitionsAsMap().containsKey(localName)) {
                            itemDef = getCurrentContentType().getChildNodeDefinitionsAsMap().get(localName);
                        } else {
                            itemDef = getCurrentContentType().getPropertyDefinitionsAsMap().get(localName);
                        }
                        if (itemDef != null && (itemDef.isNode() || setPropertyField(getCurrentContentType(), localName,
                                attributes.getValue("jahia:value")))) {
                            String mappedProperty = mapping.getMappedProperty(getCurrentContentType(), localName);
                            if ("#skip".equals(mappedProperty)) {
                                currentCtx.peek().pushSkip();
                            } else {
                                currentCtx.peek().pushField(mappedProperty);
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Not imported field " + localName + ", definition not found");
                                String s = "";
                                for (String def : getCurrentContentType().getChildNodeDefinitionsAsMap().keySet())
                                    s += def + ", ";
                                logger.debug("Allowed sub definitions: " + (s.length() > 0 ? s : "none"));
                                for (String def : getCurrentContentType().getPropertyDefinitionsAsMap().keySet())
                                    s += def + ", ";
                                logger.debug("Allowed properties: " + (s.length() > 0 ? s : "none"));
                            }
                            currentCtx.peek().pushSkip();
                        }
                    }
                    break;
                case CTX_BOX:
                    boolean isNode = false;
                    if (localName.endsWith("List") && getCurrentContentType() != null) {
                        String mappedName = mapping.getMappedItem(getCurrentContentType(),
                                StringUtils.substringBeforeLast(localName, "List"));

                        if (getCurrentContentType().getChildNodeDefinitionsAsMap().containsKey(
                                mappedName)) {
                            isNode = true;

                            if (!isSingleContainerBox(mappedName)) {
                                ExtendedNodeDefinition nodeDef = getCurrentContentType()
                                        .getChildNodeDefinitionsAsMap().get(mappedName);

                                createContentList(nodeDef, uuid, getMetadataForNodeCreation(attributes));
                                setMetadata(attributes);
                                setAcl(attributes.getValue(HTTP_WWW_JAHIA_ORG,"acl"));
                            } else {
                                currentCtx.peek().pushBox(null);
                            }
                        }
                    }
                    if (!isNode) {
                        String propertyName = mapping.getMappedProperty(getCurrentContentType(),
                                localName);
                        currentCtx.peek().boxProperties.peek().put(
                                propertyName,
                                mapping.getMappedPropertyValue(getCurrentContentType(), localName,
                                        attributes.getValue("jahia:value")));
                        currentCtx.peek().pushField(propertyName);
                    }
                    break;
                case CTX_LIST:
                    /**
                     * ExtendedNodeDefinition ctnDefinition =
                     * listType.getDeclaredUnstructuredChildNodeDefinitions().values().iterator().next(); ExtendedNodeType ctnType =
                     * ctnDefinition.getRequiredPrimaryTypes()[0];
                     */
                    String pt = attributes.getValue(Name.NS_JCR_URI, "primaryType");
                    if (pt == null && StringUtils.startsWith(originatingJahiaRelease, "5")) {
                        pt = qName;
                    }

                    createContent(pt, uuid, attributes.getValue("jahia:jahiaLinkActivation_picker_relationship"), getMetadataForNodeCreation(attributes));
                    setMetadata(attributes);
                    setAcl(attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl"));
                    break;
                case CTX_FIELD:
                    setNodeField(uri, localName, uuid, attributes);
                    break;

                case CTX_SHAREABLE:
                    // System.out.println("create shareable "+localName);
                    if ("#shareableSource".equals(mapping.getMappedNode(getCurrentContentType(), localName))) {
                        createShareableNode(attributes.getValue("jahia:value"));
                    } else {
                        currentCtx.peek().pushSkip();
                    }
                    break;
                case CTX_SKIP:
                    // System.out.println("skipped " + localName);
                    currentCtx.peek().pushSkip();
                    break;
                case CTX_NAVLINK:
                    currentCtx.peek().pushNavLink(getCurrentContentType(), attributes.getValue(HTTP_WWW_JAHIA_ORG,"acl"));

                    final JCRNodeWrapper page = currentCtx.peek().contents.peek();

                    String title = attributes.getValue("jahia:title");
                    if (HTTP_WWW_JAHIA_ORG.equals(uri) && PAGE.equals(localName)) {
                        String acl = null;
                        System.out.println(currentCtx.peek().acls);
                        for (String a : currentCtx.peek().acls) {
                            if (a != null) {
                                acl = a;
                            }
                        }
                        if (attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl") != null) {
                            acl = attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl");
                        }
                        createPage(attributes.getValue(Name.NS_JCR_URI, "primaryType"), title,
                                attributes.getValue("jahia:template"), attributes.getValue(HTTP_WWW_JAHIA_ORG, "pageKey"), uuid, getMetadataForNodeCreation(attributes));
                        setAcl(acl);
                        // todo : add a link here ??
                    } else if (HTTP_WWW_JAHIA_ORG.equals(uri) && LINK.equals(localName)) {
                        createInternalLink(page, title, uuid, attributes.getValue("jahia:reference"), "jnt:nodeLink", getMetadataForNodeCreation(attributes));
                    } else if (HTTP_WWW_JAHIA_ORG.equals(uri) && localName.equals("url")) {
                        createExternalLink(page, title, uuid, attributes.getValue("jahia:value"), "jnt:externalLink", getMetadataForNodeCreation(attributes));
                    }

                    break;
            }
        } catch (RepositoryException e) {
            logger.error("Error while processing element: [uri=" + uri + "], [localName=" + localName + "], [qName=" + qName + "]");
            throw new SAXException(e);
        }

    }

    private Map<String, String> getMetadataForNodeCreation(Attributes attributes) {
        Map<String, String> metadataMap = new HashMap<String, String>();
        metadataMap.put("jahia:createdBy", attributes.getValue("jahia:createdBy"));
        String date = attributes.getValue("jcr:created");
        metadataMap.put("jcr:created", date.length() == 19 ? date + ".000Z" : date);        
        metadataMap.put("jahia:lastModifiedBy", attributes.getValue("jahia:lastModifiedBy"));
        date = attributes.getValue("jcr:lastModified");
        metadataMap.put("jcr:lastModified", date.length() == 19 ? date + ".000Z" : date);
        return metadataMap;
    }
    private boolean isSingleContainerBox(String listName) {
        boolean isSingleContainer = false;
        for (String requiredPrimaryType : getCurrentContentType().getChildNodeDefinitionsAsMap()
                .get(listName).getRequiredPrimaryTypeNames()) {
            if (requiredPrimaryType.contains(listName) && requiredPrimaryType.endsWith("Single")) {
                isSingleContainer = true;
                break;
            }
        }
        return isSingleContainer;
    }

    /**
     * Receive notification of the end of an element.
     * <p/>
     * <p>
     * By default, do nothing. Application writers may override this method in a subclass to take specific actions at the end of each
     * element (such as finalising a tree node or writing output to a file).
     * </p>
     *
     * @param uri       The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being
     *                  performed.
     * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
     * @param qName     The qualified name (with prefix), or the empty string if qualified names are not available.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#endElement
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (currentCtx.peek().ctx.peek() == CTX_PAGE) {
            level--;
            currentCtx.pop();
            if (!currentCtx.isEmpty()) {
                currentCtx.peek().pop();
                System.out.println(StringUtils.repeat(" ", level) + "</" + localName + "> , popped full ctx , ctx = " + currentCtx.peek().ctx.peek());
            }
        } else {
            level--;
            System.out.println(StringUtils.repeat(" ", level) + "</" + localName + "> , ctx = " + currentCtx.peek().ctx.peek());
            currentCtx.peek().pop();
        }
    }

    private void createPage(String primaryType, String title, String template, String pageKey, String uuid, Map<String, String> creationMetadata)
            throws RepositoryException {
        JCRNodeWrapper subPage;
        ExtendedNodeType t = registry.getNodeType(primaryType);
        if (uuidMapping.containsKey(uuid)) {
            subPage = session.getNodeByIdentifier(uuidMapping.get(uuid));
        } else {
            JCRNodeWrapper parent = (currentCtx.isEmpty() ? currentSiteNode : getCurrentPageNode());

            if (!parent.isCheckedOut()) {
                session.checkout(parent);
            }

            if (currentCtx.isEmpty()) {
                pageKey = "home";
            }

            if (pageKey == null) {
                pageKey = JCRContentUtils.generateNodeName(title, 32);
            }

            // remove all unsupported characters
            pageKey = pageKey.replace('/', '_');
            pageKey = pageKey.replace(':', '_');
            pageKey = pageKey.replace('[', '_');
            pageKey = pageKey.replace(']', '_');

            ExtendedNodeType pageType = registry.getNodeType("jnt:page");
            JCRNodeWrapper templateNode = null;
            try {
                if (!StringUtils.isEmpty(template)) {
                    template = mapping.getMappedPropertyValue(pageType, "jahia:template", template);
                }
                templateNode = !StringUtils.isEmpty(template) ? currentSiteNode.getNode("templates/" + template) : null;
            } catch (PathNotFoundException e) {
                logger.warn("Template '" + template + "' not found. Plain jnt:page will be created");
            }

            subPage = addOrCheckoutPageNode(templateNode, parent, pageKey, creationMetadata);
            uuidMapping.put(uuid, subPage.getIdentifier());

            performActions(mapping.getActions(pageType, "jahia:template", template), subPage);
            performActions(mapping.getActions(t), subPage);
        }

        currentCtx.push(new PageContext(subPage, t));

        Node translation = subPage.getOrCreateI18N(locale);

        if (title != null && title.length() > 0) {
            translation.setProperty("jcr:title", title);
        }
    }

    private void createExternalLink(JCRNodeWrapper page, String title, String uuid, final String url,
                                    final String nodeType, Map<String, String> creationMetadata)
            throws RepositoryException {
        JCRNodeWrapper sub;
        if (uuidMapping.containsKey(uuid)) {
            sub = session.getNodeByIdentifier(uuidMapping.get(uuid));
        } else {
            sub = addOrCheckoutNode(page, "link_" + (ctnId++), nodeType, null, creationMetadata);
            sub.setProperty("j:url", url);
            uuidMapping.put(uuid, sub.getIdentifier());
        }

        Node translation = sub.getOrCreateI18N(locale);
        if (title != null && title.length() > 0) {
            translation.setProperty("jcr:title", title);
        }
    }

    private void createInternalLink(JCRNodeWrapper page, String title, String uuid, final String reference,
                                    final String nodeType, Map<String, String> creationMetadata)
            throws RepositoryException {
        JCRNodeWrapper sub;
        if (uuidMapping.containsKey(uuid)) {
            sub = session.getNodeByIdentifier(uuidMapping.get(uuid));
        } else {
            // System.out.println("link Field-node : " + localName);

            sub = addOrCheckoutNode(page, "link_" + (ctnId++), nodeType, null, creationMetadata);
            if (!references.containsKey(reference)) {
                references.put(reference, new ArrayList<String>());
            }
            references.get(reference).add(sub.getIdentifier() + "/j:node");
            uuidMapping.put(uuid, sub.getIdentifier());
        }

        Node translation = sub.getOrCreateI18N(locale);
        if (title != null && title.length() > 0) {
            translation.setProperty("jcr:title", title);
        }

        sub.setProperty("jcr:title", title);
    }


    private JCRNodeWrapper addOrCheckoutPageNode(JCRNodeWrapper template, JCRNodeWrapper parent, String nodeName, Map<String, String> creationMetadata)
            throws RepositoryException {
        JCRNodeWrapper node = null;
        try {
            node = parent.getNode(nodeName);
            if (!node.isCheckedOut()) {
                session.checkout(node);
            }
        } catch (PathNotFoundException e) {
            if (!parent.isCheckedOut()) {
                session.checkout(parent);
            }
//            if (template != null) {
//                template.copy(parent, nodeName, true, true);
//                node = parent.getNode(nodeName);
//                node.setProperty("j:sourceTemplate", template);
//            } else {
            node = parent.addNode(
                    nodeName,
                    Constants.JAHIANT_PAGE,
                    null,
                    !StringUtils.isEmpty(creationMetadata.get("jcr:created")) ? ISO8601
                            .parse(creationMetadata.get("jcr:created")) : null,
                    creationMetadata.get("jahia:createdBy"),
                    !StringUtils.isEmpty(creationMetadata.get("jcr:lastModified")) ? ISO8601
                            .parse(creationMetadata.get("jcr:lastModified")) : null,
                    creationMetadata.get("jahia:lastModifiedBy"));
            if (template != null) {
                node.setProperty("j:templateNode", template);
                Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:area] as a where isdescendantnode(a,['" + template.getPath() + "'])", Query.JCR_SQL2);
                NodeIterator ni = q.execute().getNodes();
                while (ni.hasNext()) {
                    JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) ni.next();
                    node.addNode(nodeWrapper.getName(), "jnt:contentList");
                }
            }
//            }
        }
        return node;
    }

    private JCRNodeWrapper checkoutNode(JCRNodeWrapper node) throws RepositoryException {
        if (!node.isCheckedOut()) {
            session.checkout(node);
        }
        return node;
    }

    private JCRNodeWrapper addOrCheckoutNode(JCRNodeWrapper parent, String nodeName, String nodeType,
                                             List<String> followingNodeNames, Map<String, String> creationMetadata) throws RepositoryException {
        JCRNodeWrapper node = null;
        try {
            node = parent.getNode(nodeName);
            if (!node.isCheckedOut()) {
                session.checkout(node);
            }
        } catch (PathNotFoundException e) {
            if (!parent.isCheckedOut()) {
                session.checkout(parent);
            }
            if (StringUtils.isEmpty(nodeType)) {
                nodeType = Constants.JAHIANT_CONTENTLIST;
            }
            node = parent.addNode(
                    nodeName,
                    nodeType,
                    null,
                    !StringUtils.isEmpty(creationMetadata.get("jcr:created")) ? ISO8601
                            .parse(creationMetadata.get("jcr:created")) : null,
                    creationMetadata.get("jahia:createdBy"),
                    !StringUtils.isEmpty(creationMetadata.get("jcr:lastModified")) ? ISO8601
                            .parse(creationMetadata.get("jcr:lastModified")) : null,
                    creationMetadata.get("jahia:lastModifiedBy"));
            if (!CollectionUtils.isEmpty(followingNodeNames)) {
                boolean takeNextName = false;
                for (NodeIterator it = parent.getNodes(); it.hasNext();) {
                    JCRNodeWrapper nextNode = (JCRNodeWrapper) it.next();
                    int index = followingNodeNames.indexOf(nextNode.getName());
                    if (index > -1) {
                        if (followingNodeNames.get(index).equals(nodeName)) {
                            takeNextName = true;
                        } else {
                            parent.orderBefore(node.getName(), nextNode.getName());
                            break;
                        }
                    } else if (takeNextName) {
                        parent.orderBefore(node.getName(), nextNode.getName());
                        break;
                    }
                }
            }
        }
        return node;
    }

    private void createContentList(ExtendedNodeDefinition listDefinition, String uuid, Map<String, String> creationMetadata) throws RepositoryException {
        String nodeName = mapping.getMappedNode(getCurrentContentType(), listDefinition.getName());

        String nodeType = Constants.JAHIANT_CONTENTLIST;
        if (nodeName.indexOf("|") > 0) {
            nodeType = StringUtils.substringBefore(nodeName, "|");
            nodeName = StringUtils.substringAfter(nodeName, "|");
        }
        ExtendedNodeType primaryNodeType = listDefinition.getRequiredPrimaryTypes()[0];
        try {
            primaryNodeType = registry.getNodeType(
                    StringUtils.substringBeforeLast(listDefinition.getRequiredPrimaryTypes()[0].getName(), "List"));
        } catch (NoSuchNodeTypeException ex) {
        }
        String mappedNodeType = mapping.getMappedType(primaryNodeType);
        if ("#skip".equals(nodeName) || "#skip".equals(mappedNodeType)) {
            currentCtx.peek().pushSkip();
        } else if ("#navlink".equals(nodeName) || "#navlink".equals(mappedNodeType)) {
            currentCtx.peek().pushNavLink(listDefinition.getRequiredPrimaryTypes()[0], null);
        } else {
            JCRNodeWrapper parent = getCurrentContentNode();
            if (StringUtils.contains(nodeName, "/")) {
                String parentPath = StringUtils.substringBeforeLast(nodeName, "/");
                if (parent.hasNode(parentPath)) {
                    parent = parent.getNode(parentPath);
                }
                nodeName = StringUtils.substringAfterLast(nodeName, "/");
            }
            if (StringUtils.isEmpty(nodeType) &&
                    parent.getPrimaryNodeType().getChildNodeDefinitionsAsMap().get(nodeName) != null) {
                String[] strings = parent.getPrimaryNodeType().getChildNodeDefinitionsAsMap().get(nodeName)
                        .getRequiredPrimaryTypeNames();
                nodeType = strings[0];
            }
            List<String> mappedOldNodeNames = mapping.getMappedNodesForType(getCurrentContentType(), true);
            int indexOfName = mappedOldNodeNames.indexOf(listDefinition.getName());
            List<String> mappedNewNodeNames = null;
            if (indexOfName != -1) {
                mappedNewNodeNames = mapping.getMappedNodesForType(getCurrentContentType(), false)
                        .subList(indexOfName, mappedOldNodeNames.size());
            }
            JCRNodeWrapper node = addOrCheckoutNode(parent, nodeName, nodeType, mappedNewNodeNames, creationMetadata);

            performActions(mapping.getActions(getCurrentContentType(), listDefinition.getName()), node);
            uuidMapping.put(uuid, node.getIdentifier());

            ExtendedNodeType listType = listDefinition.getRequiredPrimaryTypes()[0];
            currentCtx.peek().pushList(node, listType);

            if (currentCtx.peek().boxProperties.peek() != null) {
                for (Map.Entry<String, String> entry : currentCtx.peek().boxProperties.peek().entrySet()) {
                    setPropertyField(getCurrentContentType(), entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void createShareableNode(String uuid) throws RepositoryException {
        if (!references.containsKey(uuid)) {
            references.put(uuid, new ArrayList<String>());
        }
        references.get(uuid).add(getCurrentContentNode().getIdentifier() + "/@ctn" + (ctnId++));
        currentCtx.peek().pushSkip();
    }

    private void createContent(String primaryType, String uuid, String pickerRelationshipUuid, Map<String, String> creationMetadata)
            throws RepositoryException {
        ExtendedNodeType t = registry.getNodeType(primaryType);
        String nodeType = mapping.getMappedType(t);

        if (nodeType.equals("#skip")) {
            currentCtx.peek().pushSkip();
        } else if (nodeType.equals("#box")) {
            currentCtx.peek().pushBox(t);
        } else if (nodeType.equals("#navlink")) {
            currentCtx.peek().pushNavLink(t, null);
        } else if (nodeType.equals("#shareable")) {
            currentCtx.peek().pushShareable(t);
        } else {
            if (uuidMapping.containsKey(uuid)) {
                JCRNodeWrapper node = session.getNodeByIdentifier(uuidMapping.get(uuid));
                currentCtx.peek().pushContainer(node, t);
            } else if (pickerRelationshipUuid != null) {
                if (!references.containsKey(pickerRelationshipUuid)) {
                    references.put(pickerRelationshipUuid, new ArrayList<String>());
                }
                references.get(pickerRelationshipUuid)
                        .add(getCurrentContentNode().getIdentifier() + "/@ctn" + (ctnId++));
                currentCtx.peek().pushSkip();
            } else {
                try {
                    ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(nodeType);
                    if (StringUtils.startsWith(originatingJahiaRelease, "5") && nt.isNodeType("jmix:nodeReference")) {
                        currentCtx.peek().pushNavLink(getCurrentContentType(),null);
                        return;
                    }
                } catch (NoSuchNodeTypeException e) {
                    logger.warn("Unexpected nodetype " + nodeType + " - skipping it and its subtree");
                    currentCtx.peek().pushSkip();
                    return;
                }
                JCRNodeWrapper node = addOrCheckoutNode(getCurrentContentNode(),
                        StringUtils.substringAfter(nodeType, ":") + "_" + (ctnId++), nodeType, null, creationMetadata);
                uuidMapping.put(uuid, node.getIdentifier());
                performActions(mapping.getActions(t), node);
                currentCtx.peek().pushContainer(node, t);

            }

            if (currentCtx.peek().boxProperties.peek() != null) {
                for (Map.Entry<String, String> entry : currentCtx.peek().boxProperties.peek().entrySet()) {
                    setPropertyField(getCurrentContentType(), entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void performActions(List<Action> actions, JCRNodeWrapper node) throws RepositoryException {
        for (Action action : actions) {
            if (action instanceof AddMixin) {
                AddMixin addMixinAction = (AddMixin) action;
                node = checkoutNode(node);
                node.addMixin(addMixinAction.getNodeType());
            } else if (action instanceof AddNode) {
                AddNode addNodeAction = (AddNode) action;
                JCRNodeWrapper addedNode =
                        addOrCheckoutNode(node, addNodeAction.getName(), addNodeAction.getNodeType(), null, Collections.<String, String>emptyMap());
                setProperties(addedNode, addNodeAction.getProperties());
            } else if (action instanceof SetProperties) {
                SetProperties setPropertiesAction = (SetProperties) action;
                setProperties(node, setPropertiesAction.getProperties());
            }
        }
    }

    private void setProperties(JCRNodeWrapper node, Map<String, String> properties) {
        if (properties == null) {
            return;
        }

        for (Map.Entry<String, String> property : properties.entrySet()) {
            String propertyName = property.getKey();
            try {
                setPropertyField(null, null, node, propertyName, property.getValue());
            } catch (RepositoryException e) {
                logger.warn("Error setting property: " + propertyName + " on node: " + node.getPath(), e);
            }
        }
    }

    private void setMetadata(Attributes attributes) throws RepositoryException {
        int l = attributes.getLength();
        for (int i = 0; i < l; i++) {
            String name = attributes.getQName(i);
            setPropertyField(null, name, attributes.getValue(i));
        }
    }

    private void setAcl(String acl) {
        if (acl != null) {
            JCRNodeWrapper node = getCurrentContentNode();
            StringTokenizer st = new StringTokenizer(acl, "|");
            try {
                while (st.hasMoreTokens()) {
                    String ace = st.nextToken();
                    if (ace.equals("break")) {
                        node.setAclInheritanceBreak(true);
                    } else if (ace.equals("none")) {
                        // ?
                    } else {
                        int colonIndex = ace.lastIndexOf(":");
                        String perm = ace.substring(colonIndex + 1);
                        Set<String> grantedRoles = new HashSet<String>();
                        Set<String> removedRoles = new HashSet<String>();
                        if (perm.charAt(0) == 'r') {
                            grantedRoles.addAll(readRoles);
                        } else {
                            removedRoles.addAll(readRoles);
                        }
                        if (perm.charAt(1) == 'w') {
                            grantedRoles.addAll(writeRoles);
                        } else {
                            removedRoles.addAll(writeRoles);
                        }

                        if (perm.charAt(2) == 'a') {
                            grantedRoles.addAll(adminRoles);
                        } else {
                            removedRoles.addAll(adminRoles);
                        }

                        String principal = ace.substring(0, colonIndex);
                        if (!grantedRoles.isEmpty()) {
                            node.grantRoles(principal, grantedRoles);
                        }
                        if (!removedRoles.isEmpty()) {
                            node.denyRoles(principal, removedRoles);
                        }
                    }
                }
            } catch (RepositoryException e) {
                logger.error("Cannot set user or group ACL entry !!", e);
            }
        }
    }

    private void setNodeField(String uri, String localName, String uuid, Attributes attributes)
            throws RepositoryException, SAXException {

        JCRNodeWrapper node = getCurrentContentNode();
        String title = attributes.getValue("jahia:title");
        String propertyName = currentCtx.peek().propertyNames.peek();

        boolean isProperty = false;

        for (String s : node.getNodeTypes()) {
            ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(s);
            if (nt.getChildNodeDefinitionsAsMap().containsKey(propertyName)) {
                break;
            } else if (nt.getPropertyDefinitionsAsMap().containsKey(propertyName)) {
                isProperty = true;
                break;
            }
        }

        if (HTTP_WWW_JAHIA_ORG.equals(uri) && PAGE.equals(localName)) {
            createPage(attributes.getValue(Name.NS_JCR_URI, "primaryType"), title,
                    attributes.getValue("jahia:template"), attributes.getValue(HTTP_WWW_JAHIA_ORG, "pageKey"), uuid, getMetadataForNodeCreation(attributes));
            setAcl(attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl"));

            // todo : add a link here ??
        } else if (HTTP_WWW_JAHIA_ORG.equals(uri) && LINK.equals(localName)) {
            // System.out.println("link Field-node : " + localName);
            String reference = attributes.getValue("jahia:reference");
            if (!isProperty && !node.hasNode(propertyName)) {
                JCRNodeWrapper sub = addOrCheckoutNode(node, propertyName, "jnt:nodeLink", null, getMetadataForNodeCreation(attributes));

                Node translation = sub.getOrCreateI18N(locale);
                if (title != null && title.length() > 0) {
                    translation.setProperty("jcr:title", title);
                }

                sub.setProperty("jcr:title", title);

                if (!references.containsKey(reference)) {
                    references.put(reference, new ArrayList<String>());
                }
                references.get(reference).add(sub.getIdentifier() + "/j:node");
            } else if (isProperty && !node.hasProperty(propertyName)) {
                if (!references.containsKey(reference)) {
                    references.put(reference, new ArrayList<String>());
                }
                references.get(reference).add(node.getIdentifier() + "/" + propertyName);
            }
            currentCtx.peek().pushSkip();
        } else if (HTTP_WWW_JAHIA_ORG.equals(uri) && localName.equals("url")) {
            // System.out.println("external link Field-node : " + localName);

            String value = attributes.getValue("jahia:value");
            if (!node.hasNode(propertyName)) {
                JCRNodeWrapper sub = addOrCheckoutNode(node, propertyName, "jnt:externalLink", null, getMetadataForNodeCreation(attributes));

                Node translation = sub.getOrCreateI18N(locale);
                if (title != null && title.length() > 0) {
                    translation.setProperty("jcr:title", title);
                }

                sub.setProperty("j:url", value);
            } else if (isProperty && !node.hasProperty(propertyName)) {

            }
            currentCtx.peek().pushSkip();
        } else {
            throw new SAXException("Unexpected " + localName);
        }
    }

    private boolean setPropertyField(ExtendedNodeType baseType, String localName, String value)
            throws RepositoryException {
        String propertyName = baseType != null ? mapping.getMappedProperty(baseType, localName) :
                mapping.getMappedMetadataProperty(localName);
        logger.debug("localName: " + localName + ", propertyName: " + propertyName);
        return setPropertyField(baseType, localName, getCurrentContentNode(), propertyName, value);
    }

    private boolean setPropertyField(ExtendedNodeType baseType, String localName, JCRNodeWrapper node,
                                     String propertyName, String value) throws RepositoryException {
        JCRNodeWrapper parent = node;
        String mixinType = null;
        if (propertyName.contains("|")) {
            mixinType = StringUtils.substringBefore(propertyName, "|");
            propertyName = StringUtils.substringAfter(propertyName, "|");
        }
        if (StringUtils.contains(propertyName, "/")) {
            String parentPath = StringUtils.substringBeforeLast(propertyName, "/");
            if (parent.hasNode(parentPath)) {
                parent = parent.getNode(parentPath);
            }
            propertyName = StringUtils.substringAfterLast(propertyName, "/");
        }
        parent = checkoutNode(parent);
        if (!StringUtils.isEmpty(mixinType) && !parent.isNodeType(mixinType)) {
            parent.addMixin(mixinType);
        }

        ExtendedPropertyDefinition propertyDefinition = null;
        propertyDefinition = parent.getApplicablePropertyDefinition(propertyName);
        if (propertyDefinition == null) {
            return false;
        }
        if (propertyDefinition.isProtected()) {
            // System.out.println("protected : " + propertyName);
            return false;
        }
        Node n = parent;
        if (propertyDefinition.isInternationalized()) {
            n = parent.getOrCreateI18N(locale);
//            propertyName = propertyName + "_" + locale.toString();
        }
        //logger.debug("Setting " + propertyName + " of type " + propertyDefinition.getRequiredType());

        if (value != null && value.length() != 0 && !value.equals("<empty>")) {
            switch (propertyDefinition.getRequiredType()) {
                case PropertyType.DATE:
                    GregorianCalendar cal = new GregorianCalendar();
                    try {
                        DateFormat df = new SimpleDateFormat(ImportExportService.DATE_FORMAT);
                        Date d = df.parse(value);
                        cal.setTime(d);
                        n.setProperty(propertyName, cal);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                    break;

                case PropertyType.REFERENCE:
                case PropertyType.WEAKREFERENCE:
                    if (propertyDefinition.isMultiple()) {
                        String[] strings = value.split("\\$\\$\\$");
                        List<Value> values = new ArrayList<Value>();
                        for (String s : strings) {
                            Value v = createReferenceValue(s, propertyDefinition.getSelector());
                            if (v != null) {
                                values.add(v);
                            }
                        }
                        n.setProperty(propertyName, values.toArray(new Value[values.size()]));
                    } else {
                        Value v = createReferenceValue(value, propertyDefinition.getSelector());
                        if (v != null) {
                            n.setProperty(propertyName, v);
                        }
                    }
                    break;

                default:
                    switch (propertyDefinition.getSelector()) {
                        case SelectorType.RICHTEXT: {
                            if (value.contains("=\"###/")) {
                                int count = 1;
                                StringBuffer buf = new StringBuffer(value);
                                while (buf.indexOf("=\"###/") > -1) {
                                    int from = buf.indexOf("=\"###/") + 2;
                                    int to = buf.indexOf("\"", from);

                                    String ref = buf.substring(from, to);
                                    if (ref.startsWith("###/webdav")) {
//                                        buf.replace(from, to, "##doc-context##/{mode}/#");
//                                        continue;
                                        ref = currentSiteNode.getPath() + "/files" + StringUtils.substringAfter(ref, "###/webdav");
                                        buf.replace(from, to, "##doc-context##/{mode}/##ref:link" + (count++) + "##");
                                    } else {
                                        ref = StringUtils.substringAfterLast(ref, "/");
                                        buf.replace(from, to, "##cms-context##/{mode}/{lang}/##ref:link" + count + "##.html");
                                    }

                                    if (!references.containsKey(ref)) {
                                        references.put(ref, new ArrayList<String>());
                                    }
                                    references.get(ref).add(n.getIdentifier() + "/[" + count + "]" + propertyDefinition.getName());
                                    count++;
                                }
                                value = buf.toString();
                            }
                            n.setProperty(propertyName, value);
                            if (logger.isDebugEnabled())
                                logger.debug("Setting on node " + n.getPath() + " property " + propertyName + " with value=" + value);
                            break;
                        }
                        default: {
                            String[] vcs = propertyDefinition.getValueConstraints();
                            List<String> constraints = Arrays.asList(vcs);
                            if (!propertyDefinition.isMultiple()) {
                                if (value.startsWith("<jahia-resource")) {
                                    value = ResourceBundleMarker.parseMarkerValue(value).getResourceKey();
                                    if (value.startsWith(propertyDefinition.getResourceBundleKey())) {
                                        value = value.substring(propertyDefinition.getResourceBundleKey().length() + 1);
                                    }
                                }
                                value = baseType != null ? mapping.getMappedPropertyValue(baseType, localName, value) :
                                        value;
                                if (constraints.isEmpty() || constraints.contains(value)) {
                                    try {
                                        n.setProperty(propertyName, value);
                                        if (logger.isDebugEnabled())
                                            logger.debug("Setting on node " + n.getPath() + " property " + propertyName + " with value=" + value);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    logger.error("Impossible to set property " + propertyName + " due to some constraint error");
                                }
                            } else {
                                String[] strings = value.split("\\$\\$\\$");
                                List<Value> values = new ArrayList<Value>();
                                for (int i = 0; i < strings.length; i++) {
                                    String string = strings[i];

                                    if (string.startsWith("<jahia-resource")) {
                                        string = ResourceBundleMarker.parseMarkerValue(string).getResourceKey();
                                        if (string.startsWith(propertyDefinition.getResourceBundleKey())) {
                                            string = string.substring(
                                                    propertyDefinition.getResourceBundleKey().length() + 1);
                                        }
                                    }
                                    value = baseType != null ?
                                            mapping.getMappedPropertyValue(baseType, localName, value) : value;
                                    if (constraints.isEmpty() || constraints.contains(value)) {
                                        values.add(new ValueImpl(string, propertyDefinition.getRequiredType()));
                                    }
                                }
                                ;
                                n.setProperty(propertyName, values.toArray(new Value[values.size()]));
                                if (logger.isDebugEnabled())
                                    logger.debug("Setting on node " + n.getPath() + " property " + propertyName + " with value=" + values.toArray(new Value[values.size()]));
                            }
                            break;
                        }
                    }
            }
        } else {
            return false;
        }

        return true;
    }

    private Value createReferenceValue(String value, int selector) throws RepositoryException {
        try {
            switch (selector) {
                case SelectorType.CATEGORY: {
                    List<Category> c = ServicesRegistry.getInstance().getCategoryService().getCategory(value);
                    if (c.isEmpty()) {
                        logger.warn("Cannot find category : " + value);
                    } else {
                        Value v = new ValueImpl(c.get(0).getID(), PropertyType.REFERENCE);
                        if (c.size() > 1) {
                            logger.warn("Multiple category match : " + value);
                        }
                        return v;
                    }
                }
                default: {
                    if (value.startsWith("/")) {
                        try {
                            if (value.startsWith("/users")) {
                                value = value.replaceFirst("/users/([^/]+)/", "/users/$1/files/");
                            } else if (pathMapping != null) {
                                for (String map : pathMapping.keySet()) {
                                    if (value.startsWith(map)) {
                                        value = pathMapping.get(map) + value.substring(map.length());
                                        break;
                                    }
                                }
                            }
                            JCRNodeWrapper file = session.getNode(value);
                            return new ValueImpl(file.getIdentifier(), PropertyType.WEAKREFERENCE);
                        } catch (PathNotFoundException e) {

                        }
                    } else {
                        try {
                            String providerKey = StringUtils.substringBefore(value, ":");
                            String uuid = StringUtils.substringAfter(value, ":");
                            if (!uuid.equals("/")) {
                                JCRNodeWrapper file = session.getNodeByUUID(providerKey, uuid);
                                return new ValueImpl(file.getIdentifier(), PropertyType.WEAKREFERENCE);
                            }
                        } catch (ItemNotFoundException e) {
                        } catch (UnsupportedRepositoryOperationException e) {
                        }
                    }
                }
            }
        } catch (JahiaException e) {
            logger.error("Cannot get categories", e);
        }
        return null;
    }

    private JCRNodeWrapper getCurrentPageNode() {
        return currentCtx.peek().contents.firstElement();
    }

    private JCRNodeWrapper getCurrentContentNode() {
        return currentCtx.peek().contents.peek();
    }

    private ExtendedNodeType getCurrentContentType() {
        return currentCtx.peek().contentsType.peek();
    }

    class PageContext {
        Stack<Integer> ctx = new Stack<Integer>();
        Stack<JCRNodeWrapper> contents = new Stack<JCRNodeWrapper>();
        Stack<ExtendedNodeType> contentsType = new Stack<ExtendedNodeType>();
        Stack<String> propertyNames = new Stack<String>();
        Stack<String> acls = new Stack<String>();
        Stack<Map<String, String>> boxProperties = new Stack<Map<String, String>>();

        PageContext(JCRNodeWrapper page, ExtendedNodeType pageType) {
            contents.push(page);
            contentsType.push(pageType);
            propertyNames.push(null);
            acls.push(null);
            boxProperties.push(null);
            ctx.push(CTX_PAGE);
        }

        void pushList(JCRNodeWrapper node, ExtendedNodeType type) {
            System.out.println(" push " + currentNode + " , ctx = " + CTX_LIST);
            contents.push(node);
            contentsType.push(type);
            propertyNames.push(null);
            acls.push(null);
            boxProperties.push(boxProperties.peek());
            ctx.push(CTX_LIST);
        }

        void pushContainer(JCRNodeWrapper node, ExtendedNodeType type) {
            System.out.println(" push " + currentNode + " , ctx = " + CTX_CTN);
            contents.push(node);
            contentsType.push(type);
            propertyNames.push(null);
            acls.push(null);
            boxProperties.push(boxProperties.peek());
            ctx.push(CTX_CTN);
        }

        void pushField(String propertyName) {
            System.out.println(" push " + currentNode + " , ctx = " + CTX_FIELD);
            contents.push(contents.peek());
            contentsType.push(contentsType.peek());
            propertyNames.push(propertyName);
            acls.push(null);
            boxProperties.push(null);
            ctx.push(CTX_FIELD);
        }

        void pushBox(ExtendedNodeType t) {
            contents.push(contents.peek());
            contentsType.push(t);
            propertyNames.push(null);
            acls.push(null);
            if (ctx.peek() == CTX_LIST) {
                ctx.push(CTX_BOX);
                boxProperties.push(new HashMap<String, String>());
                System.out.println(" box push " + currentNode + " , ctx = " + CTX_BOX);
            } else {
                ctx.push(CTX_LIST);
                boxProperties.push(boxProperties.peek());
                System.out.println(" box push " + currentNode + " , ctx = " + CTX_LIST);
            }
        }

        void pushNavLink(ExtendedNodeType t, String acl) {
            System.out.println(" push " + currentNode + " , ctx = " + CTX_NAVLINK);
            contents.push(contents.peek());
            contentsType.push(contentsType.peek());
            propertyNames.push(null);
            acls.push(acl);
            boxProperties.push(null);
            ctx.push(CTX_NAVLINK);
        }

        void pushSkip() {
            System.out.println(" push " + currentNode + " , ctx = " + CTX_SKIP);
            contents.push(contents.peek());
            contentsType.push(contentsType.peek());
            propertyNames.push(null);
            acls.push(null);
            boxProperties.push(null);
            ctx.push(CTX_SKIP);
        }

        void pushShareable(ExtendedNodeType t) {
            System.out.println(" push " + currentNode + " , ctx = " + CTX_SHAREABLE);
            contents.push(contents.peek());
            contentsType.push(t);
            propertyNames.push(null);
            acls.push(null);
            boxProperties.push(null);
            ctx.push(CTX_SHAREABLE);
        }

        void pop() {
            contents.pop();
            contentsType.pop();
            propertyNames.pop();
            acls.pop();
            boxProperties.pop();
            ctx.pop();
        }

    }
}
