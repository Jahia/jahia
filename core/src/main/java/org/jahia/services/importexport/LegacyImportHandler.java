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

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.util.ISO8601;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.categories.Category;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedItemDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.SelectorType;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.importexport.DefinitionsMapping.Action;
import org.jahia.services.importexport.DefinitionsMapping.AddMixin;
import org.jahia.services.importexport.DefinitionsMapping.AddNode;
import org.jahia.services.importexport.DefinitionsMapping.SetProperties;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Patterns;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author toto
 *         Date: Oct 28, 2009
 *         Time: 2:31:18 PM
 */
public class LegacyImportHandler extends DefaultHandler {
    private static Logger logger = LoggerFactory.getLogger(LegacyImportHandler.class);

    private final static int CTX_PAGE = 0;
    private final static int CTX_CTN = 1;
    private final static int CTX_LIST = 2;
    private final static int CTX_FIELD = 3;
    private final static int CTX_SKIP = 4;
    private final static int CTX_SHAREABLE = 5;
    private final static int CTX_DIRECTSUBNODES = 6;

    private final static int CTX_NAVLINK = 7;

    private NodeTypeRegistry registry;
    private DefinitionsMapping mapping;

    private Locale locale;

    private JCRNodeWrapper currentSiteNode;
    private Stack<PageContext> currentCtx = new Stack<PageContext>();

    private String originatingJahiaRelease = null;

    private int ctnId = 1;

    private Map<String, String> uuidMapping = new HashMap<String, String>();
    private Map<String, List<String>> references = new HashMap<String, List<String>>();

    private JCRSessionWrapper session;
    private final LegacyPidMappingTool legacyPidMappingTool;

    private final String externalLinkType;
    private final String externalLinkUrlPropertyName;
    private final boolean externalLinkInternationalized;

    private static final String HTTP_WWW_JAHIA_ORG = "http://www.jahia.org/";
    private static final String PAGE = "page";
    private static final String LINK = "link";
    private static final String EMPTY = "empty";

    public static final Set<String> READ_ROLES = new HashSet<String>(Arrays.asList("reader"));
    public static final Set<String> WRITE_ROLES = new HashSet<String>(Arrays.asList("editor", "contributor"));
    public static final Set<String> ADMIN_ROLES = new HashSet<String>(Arrays.asList("reviewer", "owner"));

    public static Set<String> CUSTOM_CONTENT_READ_ROLES;
    public static Set<String> CUSTOM_CONTENT_WRITE_ROLES;
    public static Set<String> CUSTOM_CONTENT_ADMIN_ROLES;

    public static Set<String> CUSTOM_FILES_READ_ROLES;
    public static Set<String> CUSTOM_FILES_WRITE_ROLES;

    private String currentNode;
    private int level = 0;

    private VanityUrlManager urlMgr;

    public LegacyImportHandler(JCRSessionWrapper session, JCRNodeWrapper currentSiteNode, NodeTypeRegistry registry,
                               DefinitionsMapping mapping, Locale locale, String originatingJahiaRelease, LegacyPidMappingTool legacyPidMappingTool, int initialCtnId) {
        this.session = session;
        this.uuidMapping = session.getUuidMapping();
        this.currentSiteNode = currentSiteNode;
        ctnId = initialCtnId < 1 ? 1 : initialCtnId;

        this.registry = registry;

        if (mapping == null) {
            mapping = new DefinitionsMapping();
        }

        this.mapping = mapping;
        this.locale = locale;
        this.originatingJahiaRelease = originatingJahiaRelease;
        this.legacyPidMappingTool = legacyPidMappingTool;

        String s = SettingsBean.getInstance().lookupString("legacy.import.externalLink.nodeType");
        externalLinkType = StringUtils.isNotBlank(s) ? s.trim() : Constants.JAHIANT_EXTERNAL_PAGE_LINK;
        s = SettingsBean.getInstance().lookupString("legacy.import.externalLink.urlPropertyName");
        externalLinkUrlPropertyName = StringUtils.isNotBlank(s) ? s.trim() : Constants.URL;
        externalLinkInternationalized = SettingsBean.getInstance().lookupBoolean("legacy.import.externalLink.internationalized");
        urlMgr = (VanityUrlManager) SpringContextSingleton.getBean("org.jahia.services.seo.jcr.VanityUrlManager");
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
        int ctx = -1;
        String uuid = null;
        try {
            currentNode = localName;
            uuid = attributes.getValue("jcr:uuid");

            if (!currentCtx.isEmpty()) {
                ctx = currentCtx.peek().ctx.peek();
            }

            if (logger.isDebugEnabled()) {
                logger.debug(StringUtils.repeat(" ", level) + "<" + currentNode + "> , ctx = " + ctx);
            }
            level++;
            final Map<String, String> creationMetadata = getMetadataForNodeCreation(attributes);
            if (ctx == -1) {
                if (HTTP_WWW_JAHIA_ORG.equals(uri) && PAGE.equals(localName)) {
                    createPage(attributes.getValue(Name.NS_JCR_URI, "primaryType"), attributes.getValue("jahia:title"),
                            attributes.getValue("jahia:template"), attributes.getValue(HTTP_WWW_JAHIA_ORG, "pageKey"),
                            uuid, creationMetadata, attributes.getValue("jahia:pid"),
                            attributes.getValue("jcr:mixinTypes"), getAdditionalProperties(attributes.getValue("jcr:additionalProperties")));
                    setAcl(attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl"));
                    setMetadata(attributes);
                    setSeoURLs(attributes.getValue(HTTP_WWW_JAHIA_ORG, "urlMappings"));
                    return;
                } else if (HTTP_WWW_JAHIA_ORG.equals(uri) && EMPTY.equals(localName)) {
                    logger.warn("The site " + currentSiteNode.getDisplayableName() + " was not published in language " + locale + ", nothing to import");
                    return;
                }
                logger.error("Unexpected root markup: <" + uri + ":" + localName + " /> for site" + currentSiteNode.getDisplayableName());
                return;
            }

            switch (ctx) {
                case CTX_PAGE:
                    if (localName.endsWith("List") && getCurrentContentType() != null &&
                            getCurrentContentType().getChildNodeDefinitionsAsMap()
                                    .containsKey(StringUtils.substringBeforeLast(localName, "List"))) {
                        // Must be a container list
                        ExtendedNodeDefinition nodeDef = getCurrentContentType().getChildNodeDefinitionsAsMap()
                                .get(StringUtils.substringBeforeLast(localName, "List"));

                        createContentList(nodeDef, uuid, creationMetadata, attributes);
                        setMetadata(attributes);
                        setAcl(attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl"));
                        setSeoURLs(attributes.getValue(HTTP_WWW_JAHIA_ORG, "urlMappings"));
                    } else {
                        logger.error(
                                "Unexpected " + localName + " element (" + uuid + ") in import file - skipping it and its subtree (more info in debug mode)");
                        if (logger.isDebugEnabled() && localName.endsWith("List")) {
                            if (getCurrentContentType() == null) {
                                logger.debug("CurrentContentType is null!");
                            } else {
                                logger.debug("Only the following elements are allowed sccording to the source definitions: " + getCurrentContentType().getChildNodeDefinitionsAsMap().keySet().toString());
                            }
                        }
                        currentCtx.peek().pushSkip();
                    }
                    break;
                case CTX_CTN:
                    if (localName.endsWith("List") && getCurrentContentType() != null &&
                            getCurrentContentType().getChildNodeDefinitionsAsMap()
                                    .containsKey(StringUtils.substringBeforeLast(localName, "List"))) {
                        // Must be a container list
                        ExtendedNodeDefinition nodeDef = getCurrentContentType().getChildNodeDefinitionsAsMap()
                                .get(StringUtils.substringBeforeLast(localName, "List"));

                        try {
                            createContentList(nodeDef, uuid, creationMetadata, attributes);
                            if (currentCtx.peek().ctx.peek() != CTX_DIRECTSUBNODES) {
                                setMetadata(attributes);
                                setAcl(attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl"));
                                setSeoURLs(attributes.getValue(HTTP_WWW_JAHIA_ORG, "urlMappings"));
                            }
                        } catch (ConstraintViolationException cve) {
                            logger.error("Error when creating contentList with def={} (localname={} , uuid={} , currentContentType={})",
                                    new Object[]{nodeDef.getName(), localName, uuid, getCurrentContentType().getName()});
                        }
                    } else {
                        logger.debug("create field " + localName);
                        ExtendedItemDefinition itemDef;
                        if (getCurrentContentType().getChildNodeDefinitionsAsMap().containsKey(localName) && getCurrentContentType().getPropertyDefinitionsAsMap().containsKey(localName)) {
                            itemDef = getCurrentContentType().getChildNodeDefinitionsAsMap().get(localName);
                            if (!itemDef.getDeclaringNodeType().getName().equals(getCurrentContentType().getNodeTypeDefinition().getName())) {
                                ExtendedItemDefinition itemDef2 = getCurrentContentType().getPropertyDefinitionsAsMap().get(localName);
                                if (itemDef2.getDeclaringNodeType().getName().equals(getCurrentContentType().getNodeTypeDefinition().getName())) {
                                    itemDef = itemDef2;
                                }
                            }
                        } else if (getCurrentContentType().getChildNodeDefinitionsAsMap().containsKey(localName)) {
                            itemDef = getCurrentContentType().getChildNodeDefinitionsAsMap().get(localName);
                        } else {
                            itemDef = getCurrentContentType().getPropertyDefinitionsAsMap().get(localName);
                        }
                        if (itemDef == null) {
                            logger.error("Definition not found for field " + localName + " in node " + getCurrentContentType().getName() + " , uuid=" + uuid);
                        } else if (logger.isDebugEnabled()) {
                            if (itemDef.isNode()) {
                                logger.debug("The field {} is a subnode of the node type {} (child node definition = {})"
                                        , new Object[]{localName, getCurrentContentType().getName(), itemDef.getName()});
                            } else {
                                logger.debug("The field is a property: " + localName);
                            }
                        }
                        if (itemDef != null && (itemDef.isNode() || setPropertyField(getCurrentContentType(), localName,
                                attributes.getValue("jahia:value"), creationMetadata))) {
                            String mappedProperty = mapping.getMappedProperty(getCurrentContentType(), localName);
                            if ("#skip".equals(mappedProperty)) {
                                currentCtx.peek().pushSkip();
                            } else {
                                currentCtx.peek().pushField(mappedProperty);
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Not imported field " + localName + ", definition not found");
                                StringBuilder s = new StringBuilder();
                                for (String def : getCurrentContentType().getChildNodeDefinitionsAsMap().keySet())
                                    s.append(def).append(", ");
                                logger.debug("Allowed sub definitions: " + (s.length() > 0 ? s : "none"));
                                for (String def : getCurrentContentType().getPropertyDefinitionsAsMap().keySet())
                                    s.append(def).append(", ");
                                logger.debug("Allowed properties: " + (s.length() > 0 ? s : "none"));
                            }
                            currentCtx.peek().pushSkip();
                        }
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

                    createContent(pt, uuid, attributes.getValue("jahia:jahiaLinkActivation_picker_relationship"), creationMetadata,
                            attributes.getValue("jcr:mixinTypes"), getAdditionalProperties(attributes.getValue("jcr:additionalProperties")));
                    setMetadata(attributes);
                    setAcl(attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl"));
                    setSeoURLs(attributes.getValue(HTTP_WWW_JAHIA_ORG, "urlMappings"));
                    break;
                case CTX_FIELD:
                    setNodeField(uri, localName, uuid, attributes);
                    break;

                case CTX_SHAREABLE:
                    if ("#shareableSource".equals(mapping.getMappedNode(getCurrentContentType(), localName))) {
                        createShareableNode(attributes.getValue("jahia:value"));
                    } else {
                        currentCtx.peek().pushSkip();
                    }
                    break;
                case CTX_SKIP:
                    currentCtx.peek().pushSkip();
                    break;
                case CTX_NAVLINK:
                    currentCtx.peek().pushNavLink(getCurrentContentType(), attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl"));
                    currentCtx.peek().properties.peek().putAll(convertToProperties(attributes));
                    final JCRNodeWrapper page = currentCtx.peek().contents.peek();

                    String title = attributes.getValue("jahia:title");
                    if (HTTP_WWW_JAHIA_ORG.equals(uri) && PAGE.equals(localName)) {
                        String acl = null;
                        if (logger.isDebugEnabled()) {
                            logger.debug("page acls: " + currentCtx.peek().acls.toString());
                        }
                        for (String a : currentCtx.peek().acls) {
                            if (a != null) {
                                acl = a;
                            }
                        }
                        if (attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl") != null) {
                            acl = attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl");
                        }
                        Map<String, String> props = currentCtx.peek().properties.peek();
                        createPage(attributes.getValue(Name.NS_JCR_URI, "primaryType"), title,
                                attributes.getValue("jahia:template"), attributes.getValue(HTTP_WWW_JAHIA_ORG, "pageKey"),
                                uuid, creationMetadata, attributes.getValue("jahia:pid"),
                                attributes.getValue("jcr:mixinTypes"), getAdditionalProperties(attributes.getValue("jcr:additionalProperties")));
                        setMetadata(props, creationMetadata);
                        setAcl(acl);
                        setSeoURLs(attributes.getValue(HTTP_WWW_JAHIA_ORG, "urlMappings"));
                        // todo : add a link here ??
                    } else if (HTTP_WWW_JAHIA_ORG.equals(uri) && LINK.equals(localName)) {
                        createInternalLink(page, title, uuid, attributes.getValue("jahia:reference"), "jnt:nodeLink", creationMetadata,
                                attributes.getValue("jcr:mixinTypes"), getAdditionalProperties(attributes.getValue("jcr:additionalProperties")));
                    } else if (HTTP_WWW_JAHIA_ORG.equals(uri) && localName.equals("url")) {
                        createExternalLink(page, title, uuid, attributes.getValue("jahia:value"), externalLinkType, externalLinkUrlPropertyName, externalLinkInternationalized, creationMetadata,
                                attributes.getValue("jcr:mixinTypes"), getAdditionalProperties(attributes.getValue("jcr:additionalProperties")));
                    }

                    break;
                case CTX_DIRECTSUBNODES:
                    String ctnPt = attributes.getValue(Name.NS_JCR_URI, "primaryType");
                    if (ctnPt == null && StringUtils.startsWith(originatingJahiaRelease, "5")) {
                        ctnPt = qName;
                    }

                    createContent(ctnPt, uuid, attributes.getValue("jahia:jahiaLinkActivation_picker_relationship"), creationMetadata,
                            attributes.getValue("jcr:mixinTypes"), getAdditionalProperties(attributes.getValue("jcr:additionalProperties")));
                    setMetadata(attributes);
                    setAcl(attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl"));
                    setSeoURLs(attributes.getValue(HTTP_WWW_JAHIA_ORG, "urlMappings"));

                    break;
            }
        } catch (RepositoryException e) {
            logger.error(MessageFormat.format("Error while processing element: [uri={0}], [localName={1}], [qName={2}], [uuid={3}], [ctx={4}]", uri, localName, qName, uuid, ctx));
            throw new SAXException(e);
        }

    }

    private JSONObject getAdditionalProperties(String value) {
        if (StringUtils.isBlank(value)) return null;

        try {
            return new JSONObject(value);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error("Impossible to read additional properties", e);
            } else {
                logger.error("Impossible to read additional properties: {}", e.getMessage());
            }
        }
        return null;
    }

    private Map<String, String> convertToProperties(Attributes attributes) {
        Map<String, String> properties = new HashMap<String, String>();
        int l = attributes.getLength();
        for (int i = 0; i < l; i++) {
            properties.put(attributes.getQName(i), attributes.getValue(i));
        }
        return properties;
    }

    private Map<String, String> getMetadataForNodeCreation(Attributes attributes) {
        Map<String, String> metadataMap = new HashMap<String, String>();
        metadataMap.put("jahia:createdBy", attributes.getValue("jahia:createdBy"));
        String date = attributes.getValue("jcr:created");
        metadataMap.put("jcr:created", (date != null && date.length() == 19) ? date + ".000Z" : date);
        metadataMap.put("jahia:lastModifiedBy", attributes.getValue("jahia:lastModifiedBy"));
        date = attributes.getValue("jcr:lastModified");
        metadataMap.put("jcr:lastModified", (date != null && date.length() == 19) ? date + ".000Z" : date);
        return metadataMap;
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
                if (logger.isDebugEnabled()) {
                    logger.debug(StringUtils.repeat(" ", level) + "</" + localName + "> , popped full ctx , ctx = " + (currentCtx.peek().ctx.empty() ? "empty" : currentCtx.peek().ctx.peek()));
                }
            }
        } else {
            level--;
            if (logger.isDebugEnabled()) {
                logger.debug(StringUtils.repeat(" ", level) + "</" + localName + "> , ctx = " + currentCtx.peek().ctx.peek());
            }
            currentCtx.peek().pop();
        }
    }

    private void createPage(String primaryType, String title, String template, String pageKey, String uuid,
                            Map<String, String> creationMetadata, String pageId,
                            String mixinsToAdd, JSONObject propertiesToSet)
            throws RepositoryException {
        JCRNodeWrapper subPage;
        ExtendedNodeType t;
        try {
            t = registry.getNodeType(primaryType);
        } catch (NoSuchNodeTypeException e) {
            t = registry.getNodeType("jnt:page");
        }
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
                try {
                    pageKey = JCRContentUtils.generateNodeName(title);
                } catch (NullPointerException npe) {
                    pageKey = "untitled";
                    logger.error(MessageFormat.format("Impossible to generate a system name from page title =[{0}] , uuid={1}", title, uuid), npe);
                }
            }

            // remove all unsupported characters
            pageKey = pageKey.replace('/', '_');
            pageKey = JCRContentUtils.replaceColon(pageKey);
            pageKey = pageKey.replace('[', '_');
            pageKey = pageKey.replace(']', '_');

            ExtendedNodeType pageType = registry.getNodeType("jnt:page");
            String templateName = "";
            if (!StringUtils.isEmpty(template)) {
                templateName = mapping.getMappedPropertyValue(pageType, "jahia:template", template);
            }
            templateName = StringUtils.substringAfterLast("/" + templateName, "/");
            subPage = addOrCheckoutPageNode(templateName, parent, pageKey, creationMetadata);
            uuidMapping.put(uuid, subPage.getIdentifier());

            performActions(mapping.getActions(pageType, "jahia:template", template), subPage, creationMetadata);
            performActions(mapping.getActions(t), subPage, creationMetadata);
        }

        currentCtx.push(new PageContext(subPage, t));

        if (StringUtils.isNotBlank(title)) {
            final Node translation = getOrCreateI18N(subPage, locale, creationMetadata);
            if (!translation.isCheckedOut()) {
                session.checkout(translation);
            }
            translation.setProperty("jcr:title", title);
        }

        handleAdditionalMixinsAndProperties(subPage, mixinsToAdd, propertiesToSet, creationMetadata);

        if (legacyPidMappingTool != null) {
            legacyPidMappingTool.defineLegacyMapping(Integer.valueOf(pageId), subPage, locale);
        }
    }

    private void createExternalLink(JCRNodeWrapper page, String title, String uuid, final String url,
                                    final String nodeType, Map<String, String> creationMetadata)
            throws RepositoryException {
        createExternalLink(page, title, uuid, url, nodeType, Constants.URL, false, creationMetadata, null, null);
    }

    private void createExternalLink(JCRNodeWrapper page, String title, String uuid, final String url,
                                    final String nodeType, final String urlPropertyName, boolean urlIsLocalized, Map<String, String> creationMetadata,
                                    String mixinsToAdd, JSONObject propertiesToSet)
            throws RepositoryException {
        JCRNodeWrapper sub;
        if (uuidMapping.containsKey(uuid)) {
            sub = session.getNodeByIdentifier(uuidMapping.get(uuid));
        } else {
            sub = addOrCheckoutNode(page, "link_" + (ctnId++), nodeType, null, creationMetadata);
            uuidMapping.put(uuid, sub.getIdentifier());
        }

        Node translation = null;
        if (StringUtils.isNotBlank(url)) {
            if (urlIsLocalized) {
                translation = getOrCreateI18N(sub, locale, creationMetadata);
                translation.setProperty(urlPropertyName, url);
            }
            else sub.setProperty(urlPropertyName, url);
        }

        if (StringUtils.isNotBlank(title)) {
            if (translation == null) translation = getOrCreateI18N(sub, locale, creationMetadata);
            translation.setProperty("jcr:title", title);
        }
        handleAdditionalMixinsAndProperties(sub, mixinsToAdd, propertiesToSet, creationMetadata);
    }

    private void createInternalLink(JCRNodeWrapper page, String title, String uuid, final String reference,
                                    final String nodeType, Map<String, String> creationMetadata,
                                    String mixinsToAdd, JSONObject propertiesToSet)
            throws RepositoryException {
        JCRNodeWrapper sub;
        if (uuidMapping.containsKey(uuid)) {
            sub = session.getNodeByIdentifier(uuidMapping.get(uuid));
        } else {
            sub = addOrCheckoutNode(page, "link_" + (ctnId++), nodeType, null, creationMetadata);
            if (!references.containsKey(reference)) {
                references.put(reference, new ArrayList<String>());
            }
            references.get(reference).add(sub.getIdentifier() + "/" + Constants.NODE);
            uuidMapping.put(uuid, sub.getIdentifier());
        }

        if (StringUtils.isNotBlank(title)) {
            Node translation = getOrCreateI18N(sub, locale, creationMetadata);
            if (!translation.isCheckedOut()) {
                session.checkout(translation);
            }
            translation.setProperty(Constants.JCR_TITLE, title);
        }
        if (!sub.isCheckedOut()) {
            session.checkout(sub);
        }
        sub.setProperty(Constants.JCR_TITLE, title);
        handleAdditionalMixinsAndProperties(sub, mixinsToAdd, propertiesToSet, creationMetadata);
    }


    private JCRNodeWrapper addOrCheckoutPageNode(String template, JCRNodeWrapper parent, String nodeName, Map<String, String> creationMetadata)
            throws RepositoryException {
        JCRNodeWrapper node;
        try {
            node = parent.getNode(nodeName);
            if (!node.isCheckedOut()) {
                session.checkout(node);
            }
        } catch (PathNotFoundException e) {
            if (!parent.isCheckedOut()) {
                session.checkout(parent);
            }
            Calendar created = !StringUtils.isEmpty(creationMetadata.get("jcr:created")) ? ISO8601
                    .parse(creationMetadata.get("jcr:created")) : null;
            String createdBy = creationMetadata.get("jahia:createdBy");
            Calendar lastModified = !StringUtils.isEmpty(creationMetadata.get("jcr:lastModified")) ? ISO8601
                    .parse(creationMetadata.get("jcr:lastModified")) : null;
            String lastModifiedBy = creationMetadata.get("jahia:lastModifiedBy");

            node = parent.addNode(
                    nodeName,
                    Constants.JAHIANT_PAGE, null, created, createdBy, lastModified, lastModifiedBy);
        }
        if (template != null) {
            node.setProperty("j:templateName", template);
        }
        return node;
    }

    private Node getOrCreateI18N(JCRNodeWrapper node, Locale locale, Map<String, String> creationMetadata) throws RepositoryException {
        final Calendar created = !StringUtils.isEmpty(creationMetadata.get("jcr:created")) ? ISO8601
                .parse(creationMetadata.get("jcr:created")) : null;
        final String createdBy = creationMetadata.get("jahia:createdBy");
        final Calendar lastModified = !StringUtils.isEmpty(creationMetadata.get("jcr:lastModified")) ? ISO8601
                .parse(creationMetadata.get("jcr:lastModified")) : null;
        final String lastModifiedBy = creationMetadata.get("jahia:lastModifiedBy");

        return node.getOrCreateI18N(locale, created, createdBy, lastModified, lastModifiedBy);
    }

    private JCRNodeWrapper checkoutNode(JCRNodeWrapper node) throws RepositoryException {
        if (!node.isCheckedOut()) {
            session.checkout(node);
        }
        return node;
    }

    private JCRNodeWrapper addOrCheckoutNode(JCRNodeWrapper parent, String nodeName, String nodeType,
                                             List<String> followingNodeNames, Map<String, String> creationMetadata) throws RepositoryException {
        JCRNodeWrapper node;
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
            try {
                Calendar created = !StringUtils.isEmpty(creationMetadata.get("jcr:created")) ? ISO8601
                        .parse(creationMetadata.get("jcr:created")) : null;
                String createdBy = creationMetadata.get("jahia:createdBy");
                Calendar lastModified = !StringUtils.isEmpty(creationMetadata.get("jcr:lastModified")) ? ISO8601
                        .parse(creationMetadata.get("jcr:lastModified")) : null;
                String lastModifiedBy = creationMetadata.get("jahia:lastModifiedBy");
                node = parent.addNode(nodeName, nodeType, null, created, createdBy, lastModified, lastModifiedBy);
            } catch (ConstraintViolationException cve) {
                throw new ConstraintViolationException(MessageFormat.format("Error while adding node {0} of type {1} to node {2}",
                        nodeName, nodeType, parent.getPath()), cve);
            }
            if (!CollectionUtils.isEmpty(followingNodeNames)) {
                boolean takeNextName = false;
                for (NodeIterator it = parent.getNodes(); it.hasNext(); ) {
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

    private void createContentList(ExtendedNodeDefinition listDefinition, String uuid, Map<String, String> creationMetadata, Attributes attributes) throws RepositoryException {
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
        } else if ("#directSubNodes".equals(nodeName) || "#directSubNodes".equals(mappedNodeType)) {
            currentCtx.peek().pushDirectContainer();
        } else {
            JCRNodeWrapper parent = getCurrentContentNode();
            if (StringUtils.contains(nodeName, "/")) {
                String parentPath = StringUtils.substringBeforeLast(nodeName, "/");
                if (parent.hasNode(parentPath)) {
                    parent = parent.getNode(parentPath);
                } else {
                    parent = parent.addNode(parentPath, Constants.JAHIANT_CONTENTLIST);
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

            performActions(mapping.getActions(getCurrentContentType(), listDefinition.getName()), node, creationMetadata);
            uuidMapping.put(uuid, node.getIdentifier());

            ExtendedNodeType listType = listDefinition.getRequiredPrimaryTypes()[0];
            currentCtx.peek().pushList(node, listType);

            if (currentCtx.peek().properties.peek() != null) {
                for (Map.Entry<String, String> entry : currentCtx.peek().properties.peek().entrySet()) {
                    setPropertyField(getCurrentContentType(), entry.getKey(), entry.getValue(), creationMetadata);
                }
            }
        }

        handleContainerListSorting(attributes.getValue("jahia:sortHandler"), primaryNodeType);
    }

    private void handleContainerListSorting(String sortHandler, ExtendedNodeType primaryNodeType) {
        if (sortHandler == null || sortHandler.length() == 0) {
            return;
        }

        JCRNodeWrapper node = currentCtx.peek().contents.peek();
        String path = node.getPath();
        try {
            String[] sortConfig = StringUtils.split(sortHandler, "; ");
            String field = sortConfig[sortConfig.length - 1];
            String targetProperty = mapping.getMappedProperty(primaryNodeType, field);
            String direction = sortConfig[1];
            if (targetProperty != null && (node.isNodeType("jnt:contentList") || node.isNodeType("jnt:area"))) {
                logger.debug("Setting automatic ordering for list {} using property {} and direction {}",
                        new String[] { path, targetProperty, direction });
                node.addMixin("jmix:orderedList");
                node.setProperty("firstField", targetProperty);
                node.setProperty("firstDirection", direction != null && "desc".equals(direction) ? "desc" : "asc");
            } else {
                logger.error("Unable to handle container list sorting property " + sortHandler + " as the target node is not a list: " + path);
            }
        } catch (Exception e) {
            logger.error("Unable to handle container list sorting property " + sortHandler + " on node " + path, e);
        }
    }

    private void createShareableNode(String uuid) throws RepositoryException {
        if (!references.containsKey(uuid)) {
            references.put(uuid, new ArrayList<String>());
        }
        references.get(uuid).add(getCurrentContentNode().getIdentifier() + "/@ctn" + (ctnId++));
        currentCtx.peek().pushSkip();
    }

    private void createContent(String primaryType, String uuid, String pickerRelationshipUuid, Map<String, String> creationMetadata,
                               String mixinsToAdd, JSONObject propertiesToSet)
            throws RepositoryException {
        ExtendedNodeType t = registry.getNodeType(primaryType);
        String nodeType = mapping.getMappedType(t);

        if (nodeType.equals("#skip")) {
            currentCtx.peek().pushSkip();
        } else if (nodeType.equals("#navlink")) {
            currentCtx.peek().pushNavLink(t, null);
        } else if (nodeType.equals("#shareable")) {
            currentCtx.peek().pushShareable(t);
        } else {
            JCRNodeWrapper node = null;
            if (uuidMapping.containsKey(uuid)) {
                node = session.getNodeByIdentifier(uuidMapping.get(uuid));
                if (pickerRelationshipUuid != null) currentCtx.peek().pushSkip();
                else currentCtx.peek().pushContainer(node, t);
            } else if (pickerRelationshipUuid != null) {
                node = addOrCheckoutNode(getCurrentContentNode(),
                        StringUtils.substringAfter(nodeType, ":") + "_" + (ctnId++), "jnt:contentReference", null, creationMetadata);
                final String contentRefUuid = node.getIdentifier();
                uuidMapping.put(uuid, contentRefUuid);
                performActions(mapping.getActions(t), node, creationMetadata);

                if (uuidMapping.containsKey(pickerRelationshipUuid)) {
                    final JCRNodeWrapper source = session.getNodeByIdentifier(uuidMapping.get(pickerRelationshipUuid));
                    node.setProperty(Constants.NODE, source);
                } else {
                    if (!references.containsKey(pickerRelationshipUuid)) {
                        references.put(pickerRelationshipUuid, new ArrayList<String>());
                    }
                    references.get(pickerRelationshipUuid)
                            .add(contentRefUuid + "/" + Constants.NODE);
                }

                currentCtx.peek().pushSkip();
            } else {
                try {
                    ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(nodeType);
                    if (StringUtils.startsWith(originatingJahiaRelease, "5") && nt.isNodeType("jnt:nodeLink")) {
                        currentCtx.peek().pushNavLink(getCurrentContentType(), null);
                        return;
                    }
                } catch (NoSuchNodeTypeException e) {
                    logger.error("Unexpected nodetype " + nodeType + " - skipping it and its subtree");
                    currentCtx.peek().pushSkip();
                    return;
                }
                node = addOrCheckoutNode(getCurrentContentNode(),
                        StringUtils.substringAfter(nodeType, ":") + "_" + (ctnId++), nodeType, null, creationMetadata);
                uuidMapping.put(uuid, node.getIdentifier());
                performActions(mapping.getActions(t), node, creationMetadata);
                currentCtx.peek().pushContainer(node, t);

            }

            handleAdditionalMixinsAndProperties(node, mixinsToAdd, propertiesToSet, creationMetadata);

            if (currentCtx.peek().properties.peek() != null) {
                for (Map.Entry<String, String> entry : currentCtx.peek().properties.peek().entrySet()) {
                    final ExtendedNodeType currentContentType = getCurrentContentType();
                    final String fieldName = entry.getKey();
                    logger.debug("About to import field {}/{}", currentContentType.getName(), fieldName);
                    if (!setPropertyField(currentContentType, fieldName, entry.getValue(), creationMetadata) && !"#skip".equals(fieldName)) {
                        logger.error("Not imported field {}/{}", currentContentType.getName(), fieldName);
                    }
                }
            }
        }
    }

    private void performActions(List<Action> actions, JCRNodeWrapper node, Map<String, String> creationMetadata) throws RepositoryException {
        for (Action action : actions) {
            if (action instanceof AddMixin) {
                AddMixin addMixinAction = (AddMixin) action;
                node = checkoutNode(node);
                node.addMixin(addMixinAction.getNodeType());
            } else if (action instanceof AddNode) {
                AddNode addNodeAction = (AddNode) action;
                JCRNodeWrapper addedNode =
                        addOrCheckoutNode(node, addNodeAction.getName(), addNodeAction.getNodeType(), null, Collections.<String, String>emptyMap());
                setProperties(addedNode, addNodeAction.getProperties(), creationMetadata);
            } else if (action instanceof SetProperties) {
                SetProperties setPropertiesAction = (SetProperties) action;
                setProperties(node, setPropertiesAction.getProperties(), creationMetadata);
            }
        }
    }

    private void setProperties(JCRNodeWrapper node, Map<String, String> properties, Map<String, String> creationMetadata) {
        if (logger.isDebugEnabled()) {
            final StringBuilder sb = new StringBuilder();
            for (String p : properties.keySet()) sb.append(MessageFormat.format("[{0}={1}]", p, properties.get(p)));
            logger.debug("setProperties action called on {} with values : {}", node.getPath(), sb.toString());
        }
        if (properties == null) {
            return;
        }

        for (Map.Entry<String, String> property : properties.entrySet()) {
            String propertyName = property.getKey();
            try {
                setPropertyField(null, null, node, propertyName, property.getValue(), creationMetadata);
            } catch (RepositoryException e) {
                logger.error("Error setting property: " + propertyName + " on node: " + node.getPath(), e);
            }
        }
    }

    private void setMetadata(Attributes attributes) throws RepositoryException {
        int l = attributes.getLength();
        for (int i = 0; i < l; i++) {
            String name = attributes.getQName(i);
            setPropertyField(null, name, attributes.getValue(i), getMetadataForNodeCreation(attributes));
        }
    }

    private void setMetadata(Map<String, String> properties, Map<String, String> creationMetadata) throws RepositoryException {
        for (Map.Entry<String, String> property : properties.entrySet()) {
            setPropertyField(null, property.getKey(), property.getValue(), creationMetadata);
        }
    }

    private void setSeoURLs(String urlMapping) {
        if (StringUtils.isBlank(urlMapping)) return;

        for (String mapping : StringUtils.split(urlMapping, "===")) {
            if (StringUtils.isBlank(mapping)) continue;
            final String[] mappingItems = StringUtils.split(mapping, "$$$");
            final JCRNodeWrapper node = getCurrentContentNode();
            final String url = mappingItems[0];
            final Boolean isActive = Boolean.valueOf(mappingItems[1]);
            final Boolean isDefault = Boolean.valueOf(mappingItems[2]);
            try {
                final String site = node.getResolveSite().getSiteKey();
                if (urlMgr.findExistingVanityUrls(url, site, node.getSession()).isEmpty()) {
                    final VanityUrl vanityUrl = new VanityUrl(url, site, locale.toString(), isDefault, isActive);
                    urlMgr.saveVanityUrlMapping(node, vanityUrl, node.getSession());
                } else {
                    logger.error("Impossible to save URL mapping " + url + " on node " + node.getPath());
                }
            } catch (RepositoryException re) {
                logger.error("Impossible to save URL mapping " + url + " on node " + node.getPath());
            }
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
                        if (perm.length() != 3) {
                            logger.error("Skipping inconsistent acl entry: " + ace);
                            continue;
                        }
                        Set<String> grantedRoles = new HashSet<String>();
                        Set<String> removedRoles = new HashSet<String>();
                        if (perm.charAt(0) == 'r') {
                            if (CollectionUtils.isEmpty(LegacyImportHandler.CUSTOM_CONTENT_READ_ROLES)) {
                                grantedRoles.addAll(LegacyImportHandler.READ_ROLES);
                            } else {
                                grantedRoles.addAll(LegacyImportHandler.CUSTOM_CONTENT_READ_ROLES);
                            }
                        } else {
                            if (CollectionUtils.isEmpty(LegacyImportHandler.CUSTOM_CONTENT_READ_ROLES)) {
                                removedRoles.addAll(LegacyImportHandler.READ_ROLES);
                            } else {
                                removedRoles.addAll(LegacyImportHandler.CUSTOM_CONTENT_READ_ROLES);
                            }
                        }
                        if (perm.charAt(1) == 'w') {
                            if (CollectionUtils.isEmpty(LegacyImportHandler.CUSTOM_CONTENT_WRITE_ROLES)) {
                                grantedRoles.addAll(LegacyImportHandler.WRITE_ROLES);
                            } else {
                                grantedRoles.addAll(LegacyImportHandler.CUSTOM_CONTENT_WRITE_ROLES);
                            }
                        } else {
                            if (CollectionUtils.isEmpty(LegacyImportHandler.CUSTOM_CONTENT_WRITE_ROLES)) {
                                removedRoles.addAll(LegacyImportHandler.WRITE_ROLES);
                            } else {
                                removedRoles.addAll(LegacyImportHandler.CUSTOM_CONTENT_WRITE_ROLES);
                            }
                        }

                        if (perm.charAt(2) == 'a') {
                            if (CollectionUtils.isEmpty(LegacyImportHandler.CUSTOM_CONTENT_ADMIN_ROLES)) {
                                grantedRoles.addAll(LegacyImportHandler.ADMIN_ROLES);
                            } else {
                                grantedRoles.addAll(LegacyImportHandler.CUSTOM_CONTENT_ADMIN_ROLES);
                            }
                        } else {
                            if (CollectionUtils.isEmpty(LegacyImportHandler.CUSTOM_CONTENT_ADMIN_ROLES)) {
                                removedRoles.addAll(LegacyImportHandler.ADMIN_ROLES);
                            } else {
                                removedRoles.addAll(LegacyImportHandler.CUSTOM_CONTENT_ADMIN_ROLES);
                            }
                        }

                        String principal = ace.substring(0, colonIndex);
                        if (principal.equals("g:guest")) {
                            principal = "u:guest";
                        }
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

        final Map<String, String> creationMetadata = getMetadataForNodeCreation(attributes);
        if (HTTP_WWW_JAHIA_ORG.equals(uri) && PAGE.equals(localName)) {
            createPage(attributes.getValue(Name.NS_JCR_URI, "primaryType"), title,
                    attributes.getValue("jahia:template"), attributes.getValue(HTTP_WWW_JAHIA_ORG, "pageKey"),
                    uuid, creationMetadata, attributes.getValue("jahia:pid"),
                    attributes.getValue("jcr:mixinTypes"), getAdditionalProperties(attributes.getValue("jcr:additionalProperties")));
            setAcl(attributes.getValue(HTTP_WWW_JAHIA_ORG, "acl"));

            // todo : add a link here ??
        } else if (HTTP_WWW_JAHIA_ORG.equals(uri) && LINK.equals(localName)) {
            String reference = attributes.getValue("jahia:reference");
            if (StringUtils.isNotBlank(reference)) {
                if (!isProperty && !node.hasNode(propertyName)) {
                    JCRNodeWrapper sub = addOrCheckoutNode(node, propertyName, "jnt:nodeLink", null, creationMetadata);

                    if (StringUtils.isNotBlank(title)) {
                        Node translation = getOrCreateI18N(sub, locale, creationMetadata);
                        translation.setProperty(Constants.JCR_TITLE, title);
                    }

                    sub.setProperty(Constants.JCR_TITLE, title);

                    if (!references.containsKey(reference)) {
                        references.put(reference, new ArrayList<String>());
                    }
                    references.get(reference).add(sub.getIdentifier() + "/" + Constants.NODE);
                } else if (isProperty && !node.hasProperty(propertyName)) {
                    Node target = node;
                    if (node.getApplicablePropertyDefinition(propertyName) != null && node.getApplicablePropertyDefinition(propertyName).isInternationalized()) {
                        target = getOrCreateI18N(node, locale, creationMetadata);
                    }
                    if (!references.containsKey(reference)) {
                        references.put(reference, new ArrayList<String>());
                    }
                    references.get(reference).add(target.getIdentifier() + "/" + propertyName);
                }
            } else {
                logger.error("Invalid reference on markup " + uuid);
            }
            currentCtx.peek().pushSkip();
        } else if (HTTP_WWW_JAHIA_ORG.equals(uri) && localName.equals("url")) {

            String value = attributes.getValue("jahia:value");
            if (!node.hasNode(propertyName)) {
                JCRNodeWrapper sub = addOrCheckoutNode(node, propertyName, "jnt:externalLink", null, creationMetadata);

                if (StringUtils.isNotBlank(title)) {
                    Node translation = getOrCreateI18N(sub, locale, creationMetadata);
                    translation.setProperty(Constants.JCR_TITLE, title);
                }

                sub.setProperty("j:url", value);
            } else if (isProperty && !node.hasProperty(propertyName)) {

            }
            currentCtx.peek().pushSkip();
        } else {
            throw new SAXException("Unexpected " + localName);
        }
    }

    private boolean setPropertyField(ExtendedNodeType baseType, String localName, String value, Map<String, String> creationMetadata)
            throws RepositoryException {
        String propertyName = baseType != null ? mapping.getMappedProperty(baseType, localName) :
                mapping.getMappedMetadataProperty(localName);
        logger.debug("localName: " + localName + ", propertyName: " + propertyName);
        return setPropertyField(baseType, localName, getCurrentContentNode(), propertyName, value, creationMetadata);
    }

    private boolean setPropertyField(ExtendedNodeType baseType, String localName, JCRNodeWrapper node,
                                     String propertyName, String value, Map<String, String> creationMetadata) throws RepositoryException {
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

        ExtendedPropertyDefinition propertyDefinition;
        propertyDefinition = parent.getApplicablePropertyDefinition(propertyName);
        if (propertyDefinition == null) {
            return false;
        }
        if (propertyDefinition.isProtected()) {
            return false;
        }

        if (StringUtils.isNotBlank(value) && !value.equals("<empty>")) {
            Node n = parent;
            if (propertyDefinition.isInternationalized()) {
                n = getOrCreateI18N(parent, locale, creationMetadata);
            }
            if (!n.isCheckedOut()) {
                session.checkout(n);
            }
            switch (propertyDefinition.getRequiredType()) {
                case PropertyType.DATE:
                    GregorianCalendar cal = new GregorianCalendar();
                    try {
                        DateFormat df = new SimpleDateFormat(ImportExportService.DATE_FORMAT);
                        Date d = df.parse(value);
                        cal.setTime(d);
                        n.setProperty(propertyName, cal);
                    } catch (java.text.ParseException e) {
                        logger.error(e.getMessage(), e);
                    }
                    break;

                case PropertyType.REFERENCE:
                case PropertyType.WEAKREFERENCE:
                    if (propertyDefinition.isMultiple()) {
                        String[] strings = Patterns.TRIPPLE_DOLLAR.split(value);
                        for (String s : strings) {
                            createReferenceValue(s, propertyDefinition.getSelector(), n, propertyName);
                        }
                    } else {
                        createReferenceValue(value, propertyDefinition.getSelector(), n, propertyName);
                    }
                    break;

                default:
                    switch (propertyDefinition.getSelector()) {
                        case SelectorType.RICHTEXT: {
                            if (value.contains("=\"###")) {
                                int count = 1;
                                StringBuilder buf = new StringBuilder(value);
                                while (buf.indexOf("=\"###") > -1) {
                                    int from = buf.indexOf("=\"###") + 2;
                                    int to = buf.indexOf("\"", from);

                                    String ref = buf.substring(from, to);
                                    if (ref.startsWith("###/webdav")) {
                                        ref = StringUtils.substringAfter(ref, "###/webdav");
                                        buf.replace(from, to, "##doc-context##/{workspace}/##ref:link" + count + "##");
                                    } else if (ref.startsWith("###file:")) {
                                        ref = StringUtils.substringAfter(ref, "###file:");
                                        final int qmPos = ref.indexOf('?');
                                        boolean isUuid = false;
                                        if (qmPos != -1) {
                                            if (StringUtils.substring(ref, qmPos+1).startsWith("uuid=default:")) {
                                                ref = StringUtils.substring(ref, qmPos+14);
                                                isUuid = true;
                                            }
                                            else ref = StringUtils.substringBefore(ref, "?");
                                        }
                                        if (!isUuid) ref = correctFilename(ref);
                                        buf.replace(from, to, "##doc-context##/{workspace}/##ref:link" + count + "##");
                                    } else {
                                        ref = StringUtils.substringAfterLast(ref, "/");
                                        // we keep the URL parameters if any
                                        String params = "";
                                        int pos = ref.indexOf('?');
                                        if (pos == -1) {
                                            pos = ref.indexOf('#');
                                        }
                                        if (pos != -1) {
                                            params = ref.substring(pos);
                                            ref = ref.substring(0, pos);
                                        }
                                        buf.replace(from, to, "##cms-context##/{mode}/{lang}/##ref:link" + count + "##.html" + params);
                                    }
                                    try {
                                        ref = URLDecoder.decode(ref, "UTF-8");
                                    } catch (UnsupportedEncodingException e) {
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
                                } else if ("jcr:description".equals(propertyName)) {
                                    value = removeHtmlTags(value);
                                }

                                value = baseType != null ? mapping.getMappedPropertyValue(baseType, localName, value) :
                                        value;
                                if (valueMatchesContraints(value, constraints)) {
                                    try {
                                        n.setProperty(propertyName, value);
                                        if (logger.isDebugEnabled())
                                            logger.debug("Setting on node " + n.getPath() + " property " + propertyName + " with value=" + value);
                                    } catch (Exception e) {
                                        logger.error("Impossible to set property " + propertyName + " due to exception", e);
                                    }
                                } else {
                                    logger.error("Impossible to set property " + propertyName + " due to some constraint error");
                                    logger.error(" - value       = " + value);
                                    logger.error(" - constraints = " + constraints.toString());
                                }
                            } else {
                                String[] strings = Patterns.TRIPPLE_DOLLAR.split(value);
                                List<Value> values = new ArrayList<Value>();
                                for (String string : strings) {

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
                                n.setProperty(propertyName, values.toArray(new Value[values.size()]));
                                if (logger.isDebugEnabled())
                                    logger.debug("Setting on node " + n.getPath() + " property " + propertyName + " with value=" + values);
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

    private boolean valueMatchesContraints(String value, List<String> constraints) {
        if (constraints == null) return true;
        if (constraints.isEmpty()) return true;
        if (constraints.contains(value)) return true;
        for (final String constraint : constraints) {
            if (Pattern.compile(constraint).matcher(value).matches()) return true;
        }
        return false;
    }

    private void createReferenceValue(String value, int selector, Node node, String prop) throws RepositoryException {
        try {
            switch (selector) {
                case SelectorType.CATEGORY: {
                    List<Category> c = ServicesRegistry.getInstance().getCategoryService().getCategory(value);
                    if (c.isEmpty()) {
                        logger.warn("Cannot find category : " + value);
                    } else {
                        if (!references.containsKey(c.get(0).getID())) {
                            references.put(c.get(0).getID(), new ArrayList<String>());
                        }
                        references.get(c.get(0).getID()).add(node.getIdentifier() + "/" + prop);
                        if (c.size() > 1) {
                            logger.warn("Multiple category match : " + value);
                        }
                    }
                    break;
                }
                default: {
                    if (value.startsWith("/")) {
                        value = correctFilename(value);
                        if (!references.containsKey(value)) {
                            references.put(value, new ArrayList<String>());
                        }
                        references.get(value).add(node.getIdentifier() + "/" + prop);
                    } else {
                        String uuid = StringUtils.substringAfter(value, ":");
                        if (!references.containsKey(uuid)) {
                            references.put(uuid, new ArrayList<String>());
                        }
                        references.get(uuid).add(node.getIdentifier() + "/" + prop);
                    }
                }
            }
        } catch (JahiaException e) {
            logger.error("Cannot get categories", e);
        }
    }

    private String correctFilename(String value) {
        /*
        if (value.startsWith("/users/")) {
            Matcher m = Pattern.compile("/users/([^/]+)(/.*)?").matcher(value);
            if (m.matches()) {
                value = ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule().getPathForUsername(m.group(1));
                value = value + "/files" + ((m.group(2) != null) ? m.group(2) : "");
            }
        } else
        */
        if (value.startsWith("/content/users/")) {
            Matcher m = Pattern.compile("/content/users/([^/]+)(/.*)?").matcher(value);
            if (m.matches()) {
                value = ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule().getPathForUsername(m.group(1));
                value = value + ((m.group(2) != null) ? m.group(2) : "");
            }
        }
        return JCRContentUtils.escapeNodePath(value);
    }

    private String removeHtmlTags(String value) {
        Source source = new Source(value);
        TextExtractor textExtractor = source.getTextExtractor();
        textExtractor.setExcludeNonHTMLElements(true);
        textExtractor.setConvertNonBreakingSpaces(false);
        textExtractor.setIncludeAttributes(false);
        return textExtractor.toString();
    }

    private void handleAdditionalMixinsAndProperties(JCRNodeWrapper node, String mixinsToAdd, JSONObject propertiesToSet, Map<String, String> creationMetadata) {
        if (node == null) return;

        if (StringUtils.isNotBlank(mixinsToAdd)) {
            if (logger.isDebugEnabled()) {
                logger.debug(MessageFormat.format("Adding mixins [{0}] to the node {1} in language {2}",
                        mixinsToAdd, node.getPath(), this.locale.toString()));
            }
            for (final String mixin : Arrays.asList(StringUtils.split(mixinsToAdd))) {
                try {
                    if (StringUtils.isNotBlank(mixin) && !node.isNodeType(mixin)) node.addMixin(mixin);
                } catch (RepositoryException re) {
                    logger.error(MessageFormat.format("Imposible to apply mixin {0} to the node {1}", mixin, node.getPath()), re);
                }
            }
        }

        if (propertiesToSet != null) {
            final Iterator<String> properties = propertiesToSet.keys();
            while (properties.hasNext()) {
                final String propName = properties.next();
                try {
                    final String propValue = String.valueOf(propertiesToSet.get(propName));
                    if (logger.isDebugEnabled()) {
                        logger.debug(MessageFormat.format("Setting the property {0} with value [{1}] on the node {2} in language {3}",
                                propName, propValue, node.getPath(), this.locale.toString()));
                    }
                    setPropertyField(null, null, node, propName, propValue, creationMetadata);
                } catch (Exception e) {
                    logger.error(MessageFormat.format("Error while setting additional property {0} on the node {1} for locale {2}",
                            propName, node.getPath(), this.locale.toString()), e);

                }
            }
        }
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

    public int getCtnId() {
        return ctnId;
    }

    class PageContext {
        Stack<Integer> ctx = new Stack<Integer>();
        Stack<JCRNodeWrapper> contents = new Stack<JCRNodeWrapper>();
        Stack<ExtendedNodeType> contentsType = new Stack<ExtendedNodeType>();
        Stack<String> propertyNames = new Stack<String>();
        Stack<String> acls = new Stack<String>();
        Stack<Map<String, String>> properties = new Stack<Map<String, String>>();

        PageContext(JCRNodeWrapper page, ExtendedNodeType pageType) {
            contents.push(page);
            contentsType.push(pageType);
            propertyNames.push(null);
            acls.push(null);
            properties.push(null);
            ctx.push(CTX_PAGE);
        }

        void pushList(JCRNodeWrapper node, ExtendedNodeType type) {
            if (logger.isDebugEnabled()) {
                logger.debug(" push " + currentNode + " , ctx = " + CTX_LIST);
            }
            contents.push(node);
            contentsType.push(type);
            propertyNames.push(null);
            acls.push(null);
            properties.push(properties.peek() != null ? new HashMap<>(properties.peek()) : null);
            ctx.push(CTX_LIST);
        }

        void pushContainer(JCRNodeWrapper node, ExtendedNodeType type) {
            if (logger.isDebugEnabled()) {
                logger.debug(" push " + currentNode + " , ctx = " + CTX_CTN);
            }
            contents.push(node);
            contentsType.push(type);
            propertyNames.push(null);
            acls.push(null);
            properties.push(properties.peek() != null ? new HashMap<>(properties.peek()) : null);
            ctx.push(CTX_CTN);
        }

        void pushDirectContainer() {
            contents.push(getCurrentContentNode());
            contentsType.push(null);
            propertyNames.push(null);
            acls.push(null);
            properties.push(null);
            ctx.push(CTX_DIRECTSUBNODES);
        }

        void pushField(String propertyName) {
            if (logger.isDebugEnabled()) {
                logger.debug(" push " + currentNode + " , ctx = " + CTX_FIELD);
            }
            contents.push(contents.peek());
            contentsType.push(contentsType.peek());
            propertyNames.push(propertyName);
            acls.push(null);
            properties.push(null);
            ctx.push(CTX_FIELD);
        }

        void pushNavLink(ExtendedNodeType t, String acl) {
            if (logger.isDebugEnabled()) {
                logger.debug(" push " + currentNode + " , ctx = " + CTX_NAVLINK);
            }
            contents.push(contents.peek());
            contentsType.push(contentsType.peek());
            propertyNames.push(null);
            acls.push(acl);
            properties.push(ctx.peek() == CTX_NAVLINK && properties.peek() != null ? new HashMap<>(properties.peek()) : new HashMap<String, String>());
            ctx.push(CTX_NAVLINK);
        }

        void pushSkip() {
            if (logger.isDebugEnabled()) {
                logger.debug(" push " + currentNode + " , ctx = " + CTX_SKIP);
            }
            contents.push(contents.peek());
            contentsType.push(contentsType.peek());
            propertyNames.push(null);
            acls.push(null);
            properties.push(null);
            ctx.push(CTX_SKIP);
        }

        void pushShareable(ExtendedNodeType t) {
            if (logger.isDebugEnabled()) {
                logger.debug(" push " + currentNode + " , ctx = " + CTX_SHAREABLE);
            }
            contents.push(contents.peek());
            contentsType.push(t);
            propertyNames.push(null);
            acls.push(null);
            properties.push(null);
            ctx.push(CTX_SHAREABLE);
        }

        void pop() {
            contents.pop();
            contentsType.pop();
            propertyNames.pop();
            acls.pop();
            properties.pop();
            ctx.pop();
        }

    }
}
