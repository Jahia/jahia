package org.jahia.services.importexport;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
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
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA. User: toto Date: Oct 28, 2009 Time: 2:31:18 PM To change this template use File | Settings | File Templates.
 */
public class LegacyImportHandler extends DefaultHandler {
    private static Logger logger = Logger.getLogger(LegacyImportHandler.class);

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

    private int pid = 1;
    private int ctnId = 1;

    private Map<String, String> uuidMapping = new HashMap<String, String>();
    private Map<String, String> pathMapping = new HashMap<String, String>();
    private Map<String, List<String>> references = new HashMap<String, List<String>>();

    private JCRSessionWrapper session;

    private static final String HTTP_WWW_JAHIA_ORG = "http://www.jahia.org/";
    private static final String PAGE = "page";
    private static final String LINK = "link";

    public LegacyImportHandler(JCRSessionWrapper session,
            JCRNodeWrapper currentSiteNode, NodeTypeRegistry registry,
            DefinitionsMapping mapping, Locale locale) {
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
     * @param uri
     *            The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being
     *            performed.
     * @param localName
     *            The local name (without prefix), or the empty string if Namespace processing is not being performed.
     * @param qName
     *            The qualified name (with prefix), or the empty string if qualified names are not available.
     * @param attributes
     *            The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
     * @throws org.xml.sax.SAXException
     *             Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        try {

            String uuid = attributes.getValue("jcr:uuid");

            int ctx = -1;
            if (!currentCtx.isEmpty()) {
                ctx = currentCtx.peek().ctx.peek();
            }

            if (ctx == -1 && HTTP_WWW_JAHIA_ORG.equals(uri)
                    && PAGE.equals(localName)) {
                // System.out.println("create page" + attributes.getValue("jahia:title"));
                createPage(
                        attributes.getValue(Constants.JCR_NS, "primaryType"),
                        attributes.getValue("jahia:title"), attributes
                                .getValue("jahia:template"), attributes
                                .getValue(HTTP_WWW_JAHIA_ORG, "pageKey"), uuid);
                return;
            }

            switch (ctx) {
                case CTX_PAGE:
                    if (localName.endsWith("List")
                            && getCurrentContentType() != null
                            && getCurrentContentType()
                                    .getChildNodeDefinitionsAsMap()
                                    .containsKey(
                                            StringUtils.substringBeforeLast(
                                                    localName, "List"))) {
                        // System.out.println("create list " + localName);
                        // Must be a container list
                        ExtendedNodeDefinition nodeDef = getCurrentContentType()
                                .getChildNodeDefinitionsAsMap().get(
                                        StringUtils.substringBeforeLast(
                                                localName, "List"));

                        createContentList(nodeDef, uuid);
                        setMetadata(attributes);
                    } else {
                        throw new SAXException("Unexpected" + localName);
                    }
                    break;
                case CTX_CTN:
                    if (localName.endsWith("List")
                            && getCurrentContentType() != null
                            && getCurrentContentType()
                                    .getChildNodeDefinitionsAsMap()
                                    .containsKey(
                                            StringUtils.substringBeforeLast(
                                                    localName, "List"))) {
                        // System.out.println("create list " + localName);
                        // Must be a container list
                        ExtendedNodeDefinition nodeDef = getCurrentContentType()
                                .getChildNodeDefinitionsAsMap().get(
                                        StringUtils.substringBeforeLast(
                                                localName, "List"));

                        createContentList(nodeDef, uuid);
                        setMetadata(attributes);
                    } else {
                        // System.out.println("create field " + localName);
                        ExtendedItemDefinition itemDef;
                        if (getCurrentContentType()
                                .getChildNodeDefinitionsAsMap().containsKey(
                                        localName)) {
                            itemDef = getCurrentContentType()
                                    .getChildNodeDefinitionsAsMap().get(
                                            localName);
                        } else {
                            itemDef = getCurrentContentType()
                                    .getPropertyDefinitionsAsMap().get(
                                            localName);
                        }
                        if (itemDef != null
                                && (itemDef.isNode() || setPropertyField(getCurrentContentType(),
                                        localName, attributes.getValue("jahia:value")))) {
                            currentCtx.peek().pushField(
                                    mapping.getMappedProperty(getCurrentContentType(), localName));
                        } else {
                            currentCtx.peek().pushSkip();
                        }
                    }
                    break;
                case CTX_BOX:
                    if (localName.endsWith("List")
                            && getCurrentContentType() != null
                            && getCurrentContentType().getChildNodeDefinitionsAsMap().containsKey(
                                    StringUtils.substringBeforeLast(localName, "List"))) {
                        if (!isSingleContainerBox(localName)) {
                            ExtendedNodeDefinition nodeDef = getCurrentContentType()
                                    .getChildNodeDefinitionsAsMap().get(
                                            StringUtils.substringBeforeLast(localName, "List"));

                            createContentList(nodeDef, uuid);
                            setMetadata(attributes);
                        } else {
                            currentCtx.peek().pushBox(null);
                        }
                    } else {
                        String propertyName = mapping.getMappedProperty(getCurrentContentType(),
                                localName);
                        currentCtx.peek().boxProperties.peek().put(propertyName,
                                mapping.getMappedPropertyValue(getCurrentContentType(),
                                        localName, attributes.getValue("jahia:value")));
                        currentCtx.peek().pushField(propertyName);
                    }
                    break;
                case CTX_LIST:
                    /**
                     * ExtendedNodeDefinition ctnDefinition =
                     * listType.getDeclaredUnstructuredChildNodeDefinitions().values().iterator().next(); ExtendedNodeType ctnType =
                     * ctnDefinition.getRequiredPrimaryTypes()[0];
                     */

                    createContent(attributes.getValue(Constants.JCR_NS, "primaryType"), uuid,
                            attributes.getValue("jahia:jahiaLinkActivation_picker_relationship"));
                    setMetadata(attributes);
                    break;
                case CTX_FIELD:
                    setNodeField(uri, localName, uuid, attributes);
                    break;

                case CTX_SHAREABLE:
                    // System.out.println("create shareable "+localName);
                    if ("#shareableSource".equals(mapping.getMappedNode(
                            getCurrentContentType(), localName))) {
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
                    currentCtx.peek().pushField(
                            mapping.getMappedNode(getCurrentContentType(),
                                    localName));
                    break;
            }
        } catch (RepositoryException e) {
            throw new SAXException(e);
        }

    }

    private boolean isSingleContainerBox(String localName) {
        String listName = StringUtils.substringBeforeLast(localName, "List");
        boolean isSingleContainer = false;
        for (String requiredPrimaryType : getCurrentContentType()
                .getChildNodeDefinitionsAsMap().get(
                        StringUtils.substringBeforeLast(localName, "List"))
                .getRequiredPrimaryTypeNames()) {
            if (requiredPrimaryType.contains(listName)
                    && requiredPrimaryType.endsWith("Single")) {
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
     * @param uri
     *            The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being
     *            performed.
     * @param localName
     *            The local name (without prefix), or the empty string if Namespace processing is not being performed.
     * @param qName
     *            The qualified name (with prefix), or the empty string if qualified names are not available.
     * @throws org.xml.sax.SAXException
     *             Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#endElement
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (currentCtx.peek().ctx.peek() == CTX_PAGE) {
            currentCtx.pop();
        } else {
            currentCtx.peek().pop();
        }
    }

    private void createPage(String primaryType, String title, String template,
            String pageKey, String uuid) throws RepositoryException {
        JCRNodeWrapper subPage;
        ExtendedNodeType t = registry.getNodeType(primaryType);
        if (uuidMapping.containsKey(uuid)) {
            subPage = session.getNodeByIdentifier(uuidMapping.get(uuid));
        } else {
            JCRNodeWrapper parent = (currentCtx.isEmpty() ? currentSiteNode
                    : getCurrentPageNode());

            if (!parent.isCheckedOut()) {
                parent.checkout();
            }

            if (currentCtx.isEmpty()) {
                pageKey = "home";
            }

            if (pageKey == null) {
                pageKey = "page" + (pid++);
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
                templateNode = !StringUtils.isEmpty(template) ? currentSiteNode
                        .getNode("templates/" + template) : null;
            } catch (PathNotFoundException e) {
                logger
                        .warn("Template '" + template
                                + "' not found. Plain jnt:page will be created");
            }

            subPage = addOrCheckoutPageNode(templateNode, parent, pageKey);
            uuidMapping.put(uuid, subPage.getIdentifier());                    
                    
            performActions(mapping.getActions(pageType, "jahia:template",
                    template), subPage);
            subPage.setProperty("j:template", template);
            performActions(mapping.getActions(t), subPage);
        }

        currentCtx.push(new PageContext(subPage, t));

        Node translation = subPage.getOrCreateI18N(locale);

        if (title != null && title.length() > 0) {
            translation.setProperty("jcr:title_" + locale.toString(), title);
        }
    }
    
    private JCRNodeWrapper addOrCheckoutPageNode (JCRNodeWrapper template, JCRNodeWrapper parent, String nodeName) throws RepositoryException {
        JCRNodeWrapper node = null;
        try {
            node = parent.getNode(nodeName);
            if (!node.isCheckedOut()) {
                node.checkout();
            }
        } catch (PathNotFoundException e) {
            if (!parent.isCheckedOut()) {
                parent.checkout();
            }
            if (template != null) {
                template.copy(parent, nodeName, true);
                node = parent.getNode(nodeName);
                node.setProperty("j:sourceTemplate", template);
            } else {
                node = parent.addNode(nodeName, Constants.JAHIANT_PAGE);
            }
        }
        return node;
    }    
    
    private JCRNodeWrapper addOrCheckoutNode (JCRNodeWrapper parent, String nodeName, String nodeType, List<String> followingNodeNames) throws RepositoryException {
        JCRNodeWrapper node = null;
        try {
            node = parent.getNode(nodeName);
            if (!node.isCheckedOut()) {
                node.checkout();
            }
        } catch (PathNotFoundException e) {
            if (!parent.isCheckedOut()) {
                parent.checkout();
            }
            node = parent.addNode(nodeName, nodeType);
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

    private void createContentList(ExtendedNodeDefinition listDefinition,
            String uuid) throws RepositoryException {
        String nodeName = mapping.getMappedNode(getCurrentContentType(),
                listDefinition.getName());
        
        String nodeType = Constants.JAHIANT_CONTENTLIST;
        if (nodeName.indexOf(".") > 0) {
            nodeType = StringUtils.substringBefore(nodeName, ".");
            nodeName = StringUtils.substringAfter(nodeName, ".");
        }
        ExtendedNodeType primaryNodeType = listDefinition.getRequiredPrimaryTypes()[0];
        try {
            primaryNodeType = registry.getNodeType(StringUtils.substringBeforeLast(listDefinition
                    .getRequiredPrimaryTypes()[0].getName(), "List"));
        } catch (NoSuchNodeTypeException ex) {
        }
        String mappedNodeType = mapping.getMappedType(primaryNodeType);
        if ("#skip".equals(mappedNodeType)) {
            currentCtx.peek().pushSkip();
        } else if ("#navlink".equals(mappedNodeType)) {
            currentCtx.peek().pushNavLink(listDefinition.getRequiredPrimaryTypes()[0]);
        } else {
            JCRNodeWrapper parent = getCurrentContentNode();
            if (StringUtils.contains(nodeName, "/")) {
                String parentPath = StringUtils.substringBeforeLast(nodeName, "/");
                if (parent.hasNode(parentPath)) {
                    parent = parent.getNode(parentPath);
                }
                nodeName = StringUtils.substringAfterLast(nodeName, "/");
            }
            if (StringUtils.isEmpty(nodeType)
                    && parent.getPrimaryNodeType().getChildNodeDefinitionsAsMap().get(nodeName) != null) {
                String[] strings = parent.getPrimaryNodeType().getChildNodeDefinitionsAsMap().get(
                        nodeName).getRequiredPrimaryTypeNames();
                nodeType = strings[0];
            }
            List<String> mappedOldNodeNames = mapping.getMappedNodesForType(
                    getCurrentContentType(), true);
            int indexOfName = mappedOldNodeNames.indexOf(listDefinition.getName());
            List<String> mappedNewNodeNames = null;
            if (indexOfName != -1) {
                mappedNewNodeNames = mapping.getMappedNodesForType(getCurrentContentType(), false)
                        .subList(indexOfName, mappedOldNodeNames.size());
            }
            JCRNodeWrapper node = addOrCheckoutNode(parent, nodeName, nodeType, mappedNewNodeNames);

            performActions(mapping.getActions(getCurrentContentType(), listDefinition.getName()),
                    node);
            uuidMapping.put(uuid, node.getIdentifier());

            ExtendedNodeType listType = listDefinition.getRequiredPrimaryTypes()[0];
            currentCtx.peek().pushList(node, listType);

            if (currentCtx.peek().boxProperties.peek() != null) {
                for (Map.Entry<String, String> entry : currentCtx.peek().boxProperties.peek()
                        .entrySet()) {
                    setPropertyField(getCurrentContentType(), entry.getKey(), entry.getValue());
                }
            }
        }
    }
    
    private void createShareableNode(String uuid) throws RepositoryException {
        if (!references.containsKey(uuid)) {
            references.put(uuid, new ArrayList<String>());
        }
        references.get(uuid).add(
                getCurrentContentNode().getIdentifier() + "/@ctn" + (ctnId++));
        currentCtx.peek().pushSkip();
    }

    private void createContent(String primaryType, String uuid,
            String pickerRelationshipUuid) throws RepositoryException {
        ExtendedNodeType t = registry.getNodeType(primaryType);
        String nodeType = mapping.getMappedType(t);

        if (nodeType.equals("#skip")) {
            currentCtx.peek().pushSkip();
        } else if (nodeType.equals("#box")) {
            currentCtx.peek().pushBox(t);
        } else if (nodeType.equals("#navlink")) {
            currentCtx.peek().pushNavLink(t);
        } else if (nodeType.equals("#shareable")) {
            currentCtx.peek().pushShareable(t);
        } else {
            if (uuidMapping.containsKey(uuid)) {
                JCRNodeWrapper node = session.getNodeByIdentifier(uuidMapping
                        .get(uuid));
                currentCtx.peek().pushContainer(node, t);
            } else if (pickerRelationshipUuid != null) {
                if (!references.containsKey(pickerRelationshipUuid)) {
                    references.put(pickerRelationshipUuid,
                            new ArrayList<String>());
                }
                references.get(pickerRelationshipUuid).add(
                        getCurrentContentNode().getIdentifier() + "/@ctn"
                                + (ctnId++));
                currentCtx.peek().pushSkip();
            } else {
                try {
                    NodeTypeRegistry.getInstance().getNodeType(nodeType);
                } catch (NoSuchNodeTypeException e) {
                    // System.out.println("--- Cannot found type, skip : "+nodeType);
                    currentCtx.peek().pushSkip();
                }
                JCRNodeWrapper node = addOrCheckoutNode(getCurrentContentNode(), 
                        StringUtils.substringAfter(nodeType, ":") + "_"
                                + (ctnId++), nodeType, null);
                uuidMapping.put(uuid, node.getIdentifier());
                performActions(mapping.getActions(t), node);
                currentCtx.peek().pushContainer(node, t);

            }

            if (currentCtx.peek().boxProperties.peek() != null) {
                for (Map.Entry<String, String> entry : currentCtx.peek().boxProperties
                        .peek().entrySet()) {
                    setPropertyField(getCurrentContentType(), entry.getKey(),
                            entry.getValue());
                }
            }
        }
    }

    private void performActions(List<Action> actions, JCRNodeWrapper node)
            throws RepositoryException {
        for (Action action : actions) {
            if (action instanceof AddMixin) {
                AddMixin addMixinAction = (AddMixin) action;
                node.addMixin(addMixinAction.getNodeType());
            } else if (action instanceof AddNode) {
                AddNode addNodeAction = (AddNode) action;
                JCRNodeWrapper addedNode = addOrCheckoutNode(node, addNodeAction.getName(), addNodeAction
                        .getNodeType(), null);
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
                if (propertyName.contains(".")) {
                    String mixinType = StringUtils.substringBefore(property.getKey(), ".");
                    propertyName = StringUtils.substringAfter(property.getKey(), ".");
                    if (!node.isNodeType(mixinType)) {
                        node.addMixin(mixinType);
                    }
                }
                node.setProperty(propertyName, property.getValue());
            } catch (RepositoryException e) {
                logger.warn("Error setting property: " + propertyName + " on node: "
                        + node.getPath(), e);
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

    private void setNodeField(String uri, String localName, String uuid,
            Attributes attributes) throws RepositoryException, SAXException {

        JCRNodeWrapper node = getCurrentContentNode();
        String title = attributes.getValue("jahia:title");
        String propertyName = currentCtx.peek().propertyNames.peek();

        if (HTTP_WWW_JAHIA_ORG.equals(uri) && PAGE.equals(localName)) {
            createPage(attributes.getValue(Constants.JCR_NS, "primaryType"),
                    title, attributes.getValue("jahia:template"), attributes
                            .getValue(HTTP_WWW_JAHIA_ORG, "pageKey"), uuid);

            // todo : add a link here ??
        } else if (HTTP_WWW_JAHIA_ORG.equals(uri) && LINK.equals(localName)) {
            // System.out.println("link Field-node : " + localName);
            String reference = attributes.getValue("jahia:reference");
            if (!node.hasNode(propertyName)) {
                JCRNodeWrapper sub = addOrCheckoutNode(node, propertyName, "jnt:nodeLink", null);

                Node translation = sub.getOrCreateI18N(locale);
                if (title != null && title.length() > 0) {
                    translation.setProperty("jcr:title_" + locale.toString(),
                            title);
                }

                sub.setProperty("jcr:title", title);

                if (!references.containsKey(reference)) {
                    references.put(reference, new ArrayList<String>());
                }
                references.get(reference).add(sub.getIdentifier() + "/j:node");
            }
            currentCtx.peek().pushSkip();
        } else if (HTTP_WWW_JAHIA_ORG.equals(uri) && localName.equals("url")) {
            // System.out.println("external link Field-node : " + localName);

            String value = attributes.getValue("jahia:value");
            if (!node.hasNode(propertyName)) {
                JCRNodeWrapper sub = addOrCheckoutNode(node, propertyName,
                        "jnt:externalLink", null);

                Node translation = sub.getOrCreateI18N(locale);
                if (title != null && title.length() > 0) {
                    translation.setProperty("jcr:title_" + locale.toString(),
                            title);
                }

                sub.setProperty("j:url", value);
            }
            currentCtx.peek().pushSkip();
        } else {
            throw new SAXException("Unexpected " + localName);
        }
    }

    private boolean setPropertyField(ExtendedNodeType baseType,
            String localName, String value) throws RepositoryException {
        String propertyName = baseType != null ? mapping.getMappedProperty(
                baseType, localName) : mapping
                .getMappedMetadataProperty(localName);
        JCRNodeWrapper node = getCurrentContentNode();
        if (propertyName.contains(".")) {
            String mixinType = StringUtils.substringBefore(propertyName, ".");
            propertyName = StringUtils.substringAfter(propertyName, ".");
            if (!node.isNodeType(mixinType)) {
                node.addMixin(mixinType);
            }
        }
        ExtendedPropertyDefinition propertyDefinition = null;
        try {
            propertyDefinition = node
                    .getApplicablePropertyDefinition(propertyName);
        } catch (ConstraintViolationException e) {
            // System.out.println("Ignore/not found here : " + propertyName);
            return false;
        }
        if (propertyDefinition.isProtected()) {
            // System.out.println("protected : " + propertyName);
            return false;
        }
        Node n = getCurrentContentNode();
        if (propertyDefinition.isInternationalized()) {
            n = node.getOrCreateI18N(locale);
            propertyName = propertyName + "_" + locale.toString();
        }
        // System.out.println("setting " + propertyName);

        if (value != null && value.length() != 0 && !value.equals("<empty>")) {
            switch (propertyDefinition.getRequiredType()) {
                case PropertyType.DATE: {
                    GregorianCalendar cal = new GregorianCalendar();
                    try {
                        DateFormat df = new SimpleDateFormat(
                                ImportExportService.DATE_FORMAT);
                        Date d = df.parse(value);
                        cal.setTime(d);
                        n.setProperty(propertyName, cal);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                }
                    break;

                case PropertyType.REFERENCE:
                case PropertyType.WEAKREFERENCE: {
                    if (propertyDefinition.isMultiple()) {
                        String[] strings = value.split("\\$\\$\\$");
                        List<Value> values = new ArrayList<Value>();
                        for (String s : strings) {
                            Value v = createReferenceValue(s,
                                    propertyDefinition.getSelector());
                            if (v != null) {
                                values.add(v);
                            }
                        }
                        n.setProperty(propertyName, values
                                .toArray(new Value[values.size()]));
                    } else {
                        Value v = createReferenceValue(value,
                                propertyDefinition.getSelector());
                        if (v != null) {
                            n.setProperty(propertyName, v);
                        }
                    }
                    break;
                }

                default: {
                    switch (propertyDefinition.getSelector()) {
                        case SelectorType.RICHTEXT: {
                            n.setProperty(propertyName, value);
                            break;
                        }
                        default: {
                            String[] vcs = propertyDefinition
                                    .getValueConstraints();
                            List<String> constraints = Arrays.asList(vcs);
                            if (!propertyDefinition.isMultiple()) {
                                if (value.startsWith("<jahia-resource")) {
                                    value = ResourceBundleMarker
                                            .parseMarkerValue(value)
                                            .getResourceKey();
                                    if (value.startsWith(propertyDefinition
                                            .getResourceBundleKey())) {
                                        value = value
                                                .substring(propertyDefinition
                                                        .getResourceBundleKey()
                                                        .length() + 1);
                                    }
                                }
                                value = baseType != null ? mapping
                                        .getMappedPropertyValue(baseType,
                                                localName, value) : value;
                                if (constraints.isEmpty()
                                        || constraints.contains(value)) {
                                    n.setProperty(propertyName, value);
                                }
                            } else {
                                String[] strings = value.split("\\$\\$\\$");
                                List<Value> values = new ArrayList<Value>();
                                for (int i = 0; i < strings.length; i++) {
                                    String string = strings[i];

                                    if (string.startsWith("<jahia-resource")) {
                                        string = ResourceBundleMarker
                                                .parseMarkerValue(string)
                                                .getResourceKey();
                                        if (string
                                                .startsWith(propertyDefinition
                                                        .getResourceBundleKey())) {
                                            string = string
                                                    .substring(propertyDefinition
                                                            .getResourceBundleKey()
                                                            .length() + 1);
                                        }
                                    }
                                    value = baseType != null ? mapping
                                            .getMappedPropertyValue(baseType,
                                                    localName, value) : value;
                                    if (constraints.isEmpty()
                                            || constraints.contains(value)) {
                                        values.add(new ValueImpl(string,
                                                propertyDefinition
                                                        .getRequiredType()));
                                    }
                                }
                                ;
                                n.setProperty(propertyName, values
                                        .toArray(new Value[values.size()]));
                            }
                            break;
                        }
                    }
                }
            }
        } else {
            return false;
        }

        return true;
    }

    private Value createReferenceValue(String value, int selector)
            throws RepositoryException {
        try {
            switch (selector) {
                case SelectorType.CATEGORY: {
                    List<Category> c = ServicesRegistry.getInstance()
                            .getCategoryService().getCategory(value);
                    if (c.isEmpty()) {
                        logger.warn("Cannot find category : " + value);
                    } else {
                        Value v = new ValueImpl(c.get(0).getID(),
                                PropertyType.REFERENCE);
                        if (c.size() > 1) {
                            logger.warn("Multiple category match : " + value);
                        }
                        return v;
                    }
                }
                case SelectorType.PORTLET: {

                }
                default: {
                    if (value.startsWith("/")) {
                        try {
                            if (pathMapping != null) {
                                for (String map : pathMapping.keySet()) {
                                    if (value.startsWith(map)) {
                                        value = pathMapping.get(map)
                                                + value.substring(map.length());
                                        break;
                                    }
                                }
                            }
                            JCRNodeWrapper file = session.getNode(value);
                            return new ValueImpl(file.getIdentifier(),
                                    PropertyType.WEAKREFERENCE);
                        } catch (PathNotFoundException e) {

                        }
                    } else {
                        try {
                            String providerKey = StringUtils.substringBefore(
                                    value, ":");
                            String uuid = StringUtils
                                    .substringAfter(value, ":");
                            if (!uuid.equals("/")) {
                                JCRNodeWrapper file = session.getNodeByUUID(
                                        providerKey, uuid);
                                return new ValueImpl(file.getIdentifier(),
                                        PropertyType.WEAKREFERENCE);
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

    private ExtendedNodeType getCurrentPageType() {
        return currentCtx.peek().contentsType.firstElement();
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
        Stack<Map<String, String>> boxProperties = new Stack<Map<String, String>>();

        PageContext(JCRNodeWrapper page, ExtendedNodeType pageType) {
            contents.push(page);
            contentsType.push(pageType);
            propertyNames.push(null);
            boxProperties.push(null);
            ctx.push(CTX_PAGE);
        }

        void pushList(JCRNodeWrapper node, ExtendedNodeType type) {
            contents.push(node);
            contentsType.push(type);
            propertyNames.push(null);
            boxProperties.push(boxProperties.peek());
            ctx.push(CTX_LIST);
        }

        void pushContainer(JCRNodeWrapper node, ExtendedNodeType type) {
            contents.push(node);
            contentsType.push(type);
            propertyNames.push(null);
            boxProperties.push(boxProperties.peek());
            ctx.push(CTX_CTN);
        }

        void pushField(String propertyName) {
            contents.push(contents.peek());
            contentsType.push(contentsType.peek());
            propertyNames.push(propertyName);
            boxProperties.push(null);
            ctx.push(CTX_FIELD);
        }

        void pushBox(ExtendedNodeType t) {
            contents.push(contents.peek());
            contentsType.push(t);
            propertyNames.push(null);
            if (ctx.peek() == CTX_LIST) {
                ctx.push(CTX_BOX);
                boxProperties.push(new HashMap<String, String>());
            } else {
                ctx.push(CTX_LIST);
                boxProperties.push(boxProperties.peek());
            }
        }

        void pushNavLink(ExtendedNodeType t) {
            contents.push(contents.peek());
            contentsType.push(contentsType.peek());
            propertyNames.push(null);
            boxProperties.push(null);
            ctx.push(CTX_NAVLINK);
        }

        void pushSkip() {
            contents.push(contents.peek());
            contentsType.push(contentsType.peek());
            propertyNames.push(null);
            boxProperties.push(null);
            ctx.push(CTX_SKIP);
        }

        void pushShareable(ExtendedNodeType t) {
            contents.push(contents.peek());
            contentsType.push(t);
            propertyNames.push(null);
            boxProperties.push(null);
            ctx.push(CTX_SHAREABLE);
        }

        void pop() {
            contents.pop();
            contentsType.pop();
            propertyNames.pop();
            boxProperties.pop();
            ctx.pop();
        }

    }
}
