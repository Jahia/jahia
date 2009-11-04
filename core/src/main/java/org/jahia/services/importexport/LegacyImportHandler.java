package org.jahia.services.importexport;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.*;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 28, 2009
 * Time: 2:31:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class LegacyImportHandler extends DefaultHandler {
    private static Logger logger = Logger.getLogger(LegacyImportHandler.class);

    private final static int CTX_PAGE = 0;
    private final static int CTX_CTN = 1;
    private final static int CTX_LIST = 2;
    private final static int CTX_FIELD = 3;
    private final static int CTX_SKIP = 4;
    //    private final static int CTX_MERGED = 5;
    private final static int CTX_BOX = 6;

    private NodeTypeRegistry registry;
    private DefinitionsMapping mapping;

    private Locale locale;

    private JCRNodeWrapper currentSiteNode;
    private Stack<PageContext> currentCtx = new Stack<PageContext>();

    private boolean isInList = false;

    private int pid = 1;
    private int ctnId = 1;

    private Map<String, String> uuidMapping = new HashMap<String, String>();
    private Map<String, String> pathMapping = new HashMap<String, String>();
    private Map<String, List<String>> references = new HashMap<String, List<String>>();

    private static final String HTTP_WWW_JAHIA_ORG = "http://www.jahia.org/";
    private static final String PAGE = "page";
    private static final String LINK = "link";
    private static final String URL = "url";


    public LegacyImportHandler(JCRNodeWrapper currentSiteNode, NodeTypeRegistry registry, DefinitionsMapping mapping, Locale locale) {
        this.currentSiteNode = currentSiteNode;

        this.registry = registry;

        if (mapping == null) {
            mapping = new DefinitionsMapping();
        }

        this.mapping = mapping;
        this.locale = locale;
    }

    public Map getUuidMapping() {
        return uuidMapping;
    }

    public void setUuidMapping(Map uuidMapping) {
        this.uuidMapping = uuidMapping;
    }

    public void setPathMapping(Map pathMapping) {
        this.pathMapping = pathMapping;
    }

    public Map<String, List<String>> getReferences() {
        return references;
    }

    public void setReferences(Map<String, List<String>> references) {
        this.references = references;
    }

    /**
     * Receive notification of the start of an element.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the start of
     * each element (such as allocating a new tree node or writing
     * output to a file).</p>
     *
     * @param uri        The Namespace URI, or the empty string if the
     *                   element has no Namespace URI or if Namespace
     *                   processing is not being performed.
     * @param localName  The local name (without prefix), or the
     *                   empty string if Namespace processing is not being
     *                   performed.
     * @param qName      The qualified name (with prefix), or the
     *                   empty string if qualified names are not available.
     * @param attributes The attributes attached to the element.  If
     *                   there are no attributes, it shall be an empty
     *                   Attributes object.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {

            String uuid = attributes.getValue("jcr:uuid");

            int ctx = -1;
            if (!currentCtx.isEmpty()) {
                ctx = currentCtx.peek().ctx.peek();
            }

            if (ctx == -1 && HTTP_WWW_JAHIA_ORG.equals(uri) && PAGE.equals(localName)) {
                System.out.println("create page" + attributes.getValue("jahia:title"));
                createPage(attributes.getValue(Constants.JCR_NS, "primaryType"),
                        attributes.getValue("jahia:title"),
                        attributes.getValue("jahia:template"),
                        attributes.getValue(HTTP_WWW_JAHIA_ORG, "pageKey"), uuid);
                return;
            }

            switch (ctx) {
                case CTX_PAGE:
                    if (localName.endsWith("List") && getCurrentContentType() != null && getCurrentContentType().getChildNodeDefinitionsAsMap().containsKey(StringUtils.substringBeforeLast(localName, "List"))) {
                        System.out.println("create list " + localName);
                        // Must be a container list
                        ExtendedNodeDefinition nodeDef = getCurrentContentType().getChildNodeDefinitionsAsMap().get(StringUtils.substringBeforeLast(localName, "List"));

                        createContentList(nodeDef, uuid);
                        setMetadata(attributes);
                    } else {
                        throw new SAXException("Unexpected" + localName);
                    }
                    break;
                case CTX_CTN:
                    if (localName.endsWith("List") && getCurrentContentType() != null && getCurrentContentType().getChildNodeDefinitionsAsMap().containsKey(StringUtils.substringBeforeLast(localName, "List"))) {
                        System.out.println("create list " + localName);
                        // Must be a container list
                        ExtendedNodeDefinition nodeDef = getCurrentContentType().getChildNodeDefinitionsAsMap().get(StringUtils.substringBeforeLast(localName, "List"));

                        createContentList(nodeDef, uuid);
                        setMetadata(attributes);
                    } else {
                        System.out.println("create field " + localName);
                        ExtendedItemDefinition itemDef;
                        if (getCurrentContentType().getChildNodeDefinitionsAsMap().containsKey(localName)) {
                            itemDef = getCurrentContentType().getChildNodeDefinitionsAsMap().get(localName);
                        } else {
                            itemDef = getCurrentContentType().getPropertyDefinitionsAsMap().get(localName);
                        }
                        if (itemDef != null) {
                            if (itemDef.isNode()) {
                                currentCtx.peek().pushField(mapping.getMappedField(getCurrentContentType().getName(), localName));
                            } else {
                                if (setPropertyField(getCurrentContentType().getName(), localName, attributes.getValue("jahia:value"))) {
                                    currentCtx.peek().pushField(mapping.getMappedField(getCurrentContentType().getName(), localName));
                                } else {
                                    currentCtx.peek().pushSkip();
                                }
                            }
                        } else {
                            System.out.println("Ignore/not found in original : " + localName);
                            currentCtx.peek().pushSkip();
                        }
                    }
                    break;
                case CTX_BOX:
                    if (localName.endsWith("List") && getCurrentContentType() != null && getCurrentContentType().getChildNodeDefinitionsAsMap().containsKey(StringUtils.substringBeforeLast(localName, "List"))) {
                        System.out.println("create boxlist " + localName);
                        currentCtx.peek().pushBox(null);
                    } else {
                        System.out.println("create box-field " + localName);
                        currentCtx.peek().pushField(mapping.getMappedField(getCurrentContentType().getName(), localName));
                    }
                    break;
                case CTX_LIST:
                    /**
                     ExtendedNodeDefinition ctnDefinition = listType.getDeclaredUnstructuredChildNodeDefinitions().values().iterator().next();
                     ExtendedNodeType ctnType = ctnDefinition.getRequiredPrimaryTypes()[0];
                     */
                    System.out.println("create content " + localName);

                    createContent(attributes.getValue(Constants.JCR_NS, "primaryType"), uuid);
                    setMetadata(attributes);
                    break;
                case CTX_FIELD:
                    setNodeField(uri, localName, uuid, attributes);
                    break;
//                case CTX_MERGED:
//                    System.out.println("merge " + localName);
//                    currentCtx.peek().pushMerge(registry.getNodeType(attributes.getValue(Constants.JCR_NS, "primaryType")));
//                    break;
                case CTX_SKIP:
                    System.out.println("skipped " + localName);
                    currentCtx.peek().pushSkip();
                    break;
            }
        } catch (RepositoryException e) {
            throw new SAXException(e);
        }

    }

    /**
     * Receive notification of the end of an element.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the end of
     * each element (such as finalising a tree node or writing
     * output to a file).</p>
     *
     * @param uri       The Namespace URI, or the empty string if the
     *                  element has no Namespace URI or if Namespace
     *                  processing is not being performed.
     * @param localName The local name (without prefix), or the
     *                  empty string if Namespace processing is not being
     *                  performed.
     * @param qName     The qualified name (with prefix), or the
     *                  empty string if qualified names are not available.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.ContentHandler#endElement
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (HTTP_WWW_JAHIA_ORG.equals(uri) && PAGE.equals(localName)) {
            currentCtx.pop();
        } else {
            currentCtx.peek().pop();
        }
    }

    private void createPage(String primaryType, String title, String template, String pageKey, String uuid) throws RepositoryException {
        JCRNodeWrapper subPage;
        if (uuidMapping.containsKey(uuid)) {
            subPage = currentSiteNode.getSession().getNodeByIdentifier(uuidMapping.get(uuid));
        } else {
            JCRNodeWrapper parent = (currentCtx.isEmpty() ? currentSiteNode : getCurrentPageNode());

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


            subPage = parent.addNode(pageKey, Constants.JAHIANT_PAGE);
            uuidMapping.put(uuid, subPage.getIdentifier());
            if (template != null && template.length() > 0) {
                subPage.setProperty("j:template", template);
            }
        }

        ExtendedNodeType t = registry.getNodeType(primaryType);

        currentCtx.push(new PageContext(subPage, t));

        Node translation = subPage.getOrCreateI18N(locale);

        if (title != null && title.length() > 0) {
            translation.setProperty("jcr:title_" + locale.toString(), title);
        }
    }


    private void createContentList(ExtendedNodeDefinition listDefinition, String uuid) throws RepositoryException {
        String nodeName = mapping.getMappedList(getCurrentContentType(), listDefinition.getName());
//        if (nodeName.equals("merge")) {
//            currentCtx.peek().pushMerge(listDefinition.getRequiredPrimaryTypes()[0]);
//            return;
//        }

        JCRNodeWrapper parent = getCurrentContentNode();
        JCRNodeWrapper node;
        if (parent.hasNode(nodeName)) {
            node = parent.getNode(nodeName);
        } else {
            node = parent.addNode(nodeName, Constants.JAHIANT_CONTENTLIST);
            uuidMapping.put(uuid, node.getIdentifier());
        }
        ExtendedNodeType listType = listDefinition.getRequiredPrimaryTypes()[0];
        currentCtx.peek().pushList(node, listType);
    }

    private void createContent(String primaryType, String uuid) throws RepositoryException {
        ExtendedNodeType t = registry.getNodeType(primaryType);
        String nodeType = mapping.getMappedType(t);

        if (nodeType.equals("#box")) {
            currentCtx.peek().pushBox(t);
        } else {
            if (uuidMapping.containsKey(uuid)) {
                JCRNodeWrapper node = currentSiteNode.getSession().getNodeByIdentifier(uuidMapping.get(uuid));
                currentCtx.peek().pushContainer(node, t);
            } else {
                JCRNodeWrapper node = getCurrentContentNode().addNode("ctn" + (ctnId++), nodeType);
                uuidMapping.put(uuid, node.getIdentifier());
                currentCtx.peek().pushContainer(node, t);
            }
        }
    }

    private void setMetadata(Attributes attributes) throws RepositoryException {
        int l = attributes.getLength();
        for (int i = 0; i < l; i++) {
            String name = attributes.getQName(i);
            setPropertyField("metadata", name, attributes.getValue(i));
        }
    }

    private void setNodeField(String uri, String localName, String uuid, Attributes attributes) throws RepositoryException, SAXException {

        JCRNodeWrapper node = getCurrentContentNode();
        String title = attributes.getValue("jahia:title");
        String propertyName = currentCtx.peek().propertyNames.peek();

        if (HTTP_WWW_JAHIA_ORG.equals(uri) && PAGE.equals(localName)) {
            createPage(attributes.getValue(Constants.JCR_NS, "primaryType"), title,
                    attributes.getValue("jahia:template"),
                    attributes.getValue(HTTP_WWW_JAHIA_ORG, "pageKey"), uuid);

            // todo : add a link here ??
        } else if (HTTP_WWW_JAHIA_ORG.equals(uri) && LINK.equals(localName)) {
            System.out.println("link Field-node : " + localName);
            String reference = attributes.getValue("jahia:reference");
            if (!node.hasNode(propertyName)) {
                JCRNodeWrapper sub = node.addNode(propertyName, "jnt:nodeLink");

                Node translation = sub.getOrCreateI18N(locale);
                if (title != null && title.length() > 0) {
                    translation.setProperty("jcr:title_" + locale.toString(), title);
                }

                sub.setProperty("jcr:title", title);

                if (!references.containsKey(reference)) {
                    references.put(reference, new ArrayList<String>());
                }
                references.get(reference).add(sub.getIdentifier() + "/j:node");
            }
            currentCtx.peek().pushSkip();
        } else if (HTTP_WWW_JAHIA_ORG.equals(uri) && localName.equals("url")) {
            System.out.println("external link Field-node : " + localName);

            String value = attributes.getValue("jahia:value");
            if (!node.hasNode(propertyName)) {
                JCRNodeWrapper sub = node.addNode(propertyName, "jnt:externalLink");

                Node translation = sub.getOrCreateI18N(locale);
                if (title != null && title.length() > 0) {
                    translation.setProperty("jcr:title_" + locale.toString(), title);
                }

                sub.setProperty("j:url", value);
            }
            currentCtx.peek().pushSkip();
        } else {
            throw new SAXException("Unexpected" + localName);
        }
    }

    private boolean setPropertyField(String baseName, String localName, String value) throws RepositoryException {
        String propertyName = mapping.getMappedField(baseName, localName);
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
            propertyDefinition = node.getApplicablePropertyDefinition(propertyName);
        } catch (ConstraintViolationException e) {
            System.out.println("Ignore/not found here : " + propertyName);
            return false;
        }
        if (propertyDefinition.isProtected()) {
            System.out.println("protected : " + propertyName);
            return false;
        }
        Node n = getCurrentContentNode();
        if (propertyDefinition.isInternationalized()) {
            n = node.getOrCreateI18N(locale);
            propertyName = propertyName + "_" + locale.toString();
        }
        System.out.println("setting "+propertyName);
        switch (propertyDefinition.getSelector()) {
            case SelectorType.DATEPICKER:
            case SelectorType.DATETIMEPICKER: {
                if (value != null && value.length() != 0 && !value.equals("<empty>")) {
                    GregorianCalendar cal = new GregorianCalendar();
                    try {
                        DateFormat df = new SimpleDateFormat(ImportExportService.DATE_FORMAT);
                        Date d = df.parse(value);
                        cal.setTime(d);
                        n.setProperty(propertyName, cal);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case SelectorType.RICHTEXT: {
                if (value == null || value.length() == 0 || value.equals("<empty>")) {
                } else {
                    n.setProperty(propertyName, value);
                }
                break;
            }
            case SelectorType.FILEPICKER: {
                try {
                    if (value != null) {
                        if (value.startsWith("/")) {
                            try {
                                if (pathMapping != null) {
                                    for (String map : pathMapping.keySet()) {
                                        if (value.startsWith(map)) {
                                            value = pathMapping.get(map) + value.substring(map.length());
                                            break;
                                        }
                                    }
                                }
                                JCRNodeWrapper file = JCRStoreService.getInstance().getSessionFactory().getCurrentUserSession().getNode(value);
                                n.setProperty(propertyName, file.getIdentifier());
                            } catch (PathNotFoundException e) {

                            }
                        } else {
                            try {
                                String providerKey = StringUtils.substringBefore(value, ":");
                                String uuid = StringUtils.substringAfter(value, ":");
                                if (!uuid.equals("/")) {
                                    JCRNodeWrapper file = JCRStoreService.getInstance().getSessionFactory().getCurrentUserSession().getNodeByUUID(providerKey, uuid);
                                    n.setProperty(propertyName, file.getIdentifier());
                                }
                            } catch (ItemNotFoundException e) {
                            } catch (UnsupportedRepositoryOperationException e) {
                            }
                        }
                    }
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
                break;
            }
            case SelectorType.CATEGORY: {
                if (propertyDefinition.isMultiple()) {
                    String[] strings = value.split("\\$\\$\\$");
                    List<Value> values = new ArrayList<Value>();
                    for (String s : strings) {
                        Value v = createCategoryValue(s);
                        if (v != null) {
                            values.add(v);
                        }
                    }
                    n.setProperty(propertyName, values.toArray(new Value[values.size()]));
                } else {
                    Value v = createCategoryValue(value);
                    if (v != null) {
                        n.setProperty(propertyName, v);
                    }
                }
            }
            case SelectorType.PORTLET: {
//                    try {
//                        JCRNodeWrapper portlet = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(value);
//                        if (portlet != null) {
//                            n.setProperty(propertyName, portlet.getIdentifier());
//                        }
//                    } catch (RepositoryException e) {
//                    }
                break;
            }
            default: {
//                    if (def.getCtnType().equals(definition.getName() + " jcr_primaryType")) {
//                        return;
//                    }
                if (value != null && value.length() != 0 && !value.equals("<empty>")) {
                    String[] vcs = propertyDefinition.getValueConstraints();
                    List<String> constraints = Arrays.asList(vcs);
                    if (!propertyDefinition.isMultiple()) {
                        if (value.startsWith("<jahia-resource")) {
                            value = ResourceBundleMarker.parseMarkerValue(value).getResourceKey();
                            if (value.startsWith(propertyDefinition.getResourceBundleKey())) {
                                value = value.substring(propertyDefinition.getResourceBundleKey().length() + 1);
                            }
                            value = mapping.getMappedPropertyValue(baseName, localName, value);
                        }
                        if (constraints.isEmpty() || constraints.contains(value)) {
                            n.setProperty(propertyName, value);
                        }
                    } else {
                        String[] strings = value.split("\\$\\$\\$");
                        List<Value> values = new ArrayList<Value>();
                        for (int i = 0; i < strings.length; i++) {
                            String string = strings[i];

                            if (string.startsWith("<jahia-resource")) {
                                string = ResourceBundleMarker.parseMarkerValue(string).getResourceKey();
                                if (string.startsWith(propertyDefinition.getResourceBundleKey())) {
                                    string = string.substring(propertyDefinition.getResourceBundleKey().length() + 1);
                                }
                                value = mapping.getMappedPropertyValue(baseName, localName, value);
                            }
                            if (constraints.isEmpty() || constraints.contains(value)) {
                                values.add(new ValueImpl(string, propertyDefinition.getRequiredType()));
                            }
                        }
                        ;
                        n.setProperty(propertyName, values.toArray(new Value[values.size()]));
                    }
                }
                break;
            }


        }
        return true;
    }

    private Value createCategoryValue(String s) throws ValueFormatException {
        try {
            List<Category> c = ServicesRegistry.getInstance().getCategoryService().getCategory(s);
            if (c.isEmpty()) {
                logger.warn("Cannot find category : " + s);
            } else {
                Value v = new ValueImpl(c.get(0).getID(), PropertyType.REFERENCE);
                if (c.size() > 1) {
                    logger.warn("Multiple category match : " + s);
                }
                return v;
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

        PageContext(JCRNodeWrapper page, ExtendedNodeType pageType) {
            contents.push(page);
            contentsType.push(pageType);
            propertyNames.push(null);
            ctx.push(CTX_PAGE);
        }

        void pushList(JCRNodeWrapper node, ExtendedNodeType type) {
            contents.push(node);
            contentsType.push(type);
            propertyNames.push(null);
            ctx.push(CTX_LIST);
        }

        void pushContainer(JCRNodeWrapper node, ExtendedNodeType type) {
            contents.push(node);
            contentsType.push(type);
            propertyNames.push(null);
            ctx.push(CTX_CTN);
        }

        void pushField(String propertyName) {
            contents.push(contents.peek());
            contentsType.push(contentsType.peek());
            propertyNames.push(propertyName);
            ctx.push(CTX_FIELD);
        }

        void pushBox(ExtendedNodeType t) {
            contents.push(contents.peek());
            contentsType.push(t);
            propertyNames.push(null);
            if (ctx.peek() == CTX_LIST) {
                ctx.push(CTX_BOX);
            } else {
                ctx.push(CTX_LIST);
            }
        }

//        void pushMerge(ExtendedNodeType type) {
//            contents.push(contents.peek());
//            contentsType.push(type);
//            if (ctx.peek() == CTX_CTN) {
//                ctx.push(CTX_MERGED);
//            } else {
//                ctx.push(CTX_CTN);
//            }
//        }

        void pushSkip() {
            contents.push(contents.peek());
            contentsType.push(contentsType.peek());
            propertyNames.push(null);
            ctx.push(CTX_SKIP);
        }

        void pop() {
            contents.pop();
            contentsType.pop();
            propertyNames.pop();
            ctx.pop();
        }

    }
}
