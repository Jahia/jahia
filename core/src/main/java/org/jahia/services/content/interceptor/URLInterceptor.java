package org.jahia.services.content.interceptor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.SelectorType;
import org.jahia.services.render.filter.URLFilter;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.NodeIterator;
import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.htmlparser.jericho.*;

/**
 * URL Interceptor catches internal URLs inside richtext, and transform them to store references to the pointed nodes
 * instead of pathes. It also replaces the servlet context and servlet name by a placeholder so that the stored link
 * is not dependant of the deployement.
 *
 * Two types of links are detected : CMS links (like /cms/render/default/en/sites/ACME/home.html ) and files
 * links ( /files/sites/ACME/files/Pictures/BannerTeaser/img-home-fr.jpg ).
 *
 * File path are transformed with references placeholders like ##ref:link1##. References targets are stored in the
 * jmix:referenceInField child nodes.
 *
 */
public class URLInterceptor implements PropertyInterceptor, InitializingBean {
    private static Logger logger = Logger.getLogger(URLInterceptor.class);

    private String dmsContext;
    private String cmsContext;

    public static String DOC_CONTEXT_PLACEHOLDER = "##doc-context##/";
    public static String CMS_CONTEXT_PLACEHOLDER = "##cms-context##/";

    private Pattern cmsPattern;
    private Pattern cmsPatternWithContextPlaceholder;
    private Pattern refPattern;

    private String escape(String s) {
        s = s.replace("{","\\{");
        s = s.replace("}","\\}");
        return s;
    }

    public boolean canApplyOnProperty(JCRNodeWrapper node, ExtendedPropertyDefinition definition) throws RepositoryException {
        return definition.getRequiredType() == PropertyType.STRING && definition.getSelector() == SelectorType.RICHTEXT;
    }

    public void beforeRemove(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition) throws VersionException, LockException, ConstraintViolationException, RepositoryException {}

    /**
     * Transform user URL with servlet context and links placeholders for storage.
     *
     * Only URLs starting with /<context>/cms or /<context>/files are recognized.
     *
     * CMS URLs can use mode and language placeholders : /<context>/cms/render/default/en/sites/ACME/home.html and
     * /<context>/cms/##mode##/##lang##/sites/ACME/home.html are both recognized.
     *
     * If any link is invalid, a ConstraintViolationException is thrown.
     *
     * Add jmix:referencesInField mixin type to the parent node and j:referenceInField with the list of references
     * contained in the value.
     *
     * @param node
     * @param name
     *@param definition
     * @param originalValue Original value  @return Value to set, or null   @return
     * @throws ValueFormatException
     * @throws VersionException
     * @throws LockException
     * @throws ConstraintViolationException
     * @throws RepositoryException
     */


    public Value beforeSetValue(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value originalValue) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        String content = originalValue.getString();

        Map<String, Long> refs = new HashMap<String, Long>();

        logger.debug("Intercept setValue for "+node.getPath()+"/"+name);

        if (node.isNodeType("jmix:referencesInField")) {
            NodeIterator ni = node.getNodes("j:referenceInField");
            while (ni.hasNext()) {
                JCRNodeWrapper ref = (JCRNodeWrapper) ni.next();
                if (name.equals(ref.getProperty("j:fieldName").getString())) {
                    refs.put(ref.getProperty("j:reference").getString(), ref.getProperty("j:id").getLong());
                }
            }
        }

        Map<String, Long> newRefs = new HashMap<String, Long>();

        Source source = new Source(content);
        OutputDocument document = new OutputDocument(source);

        for (String[] tagAttrPair : URLFilter.TAG_ATTRIBUTE_PAIR) {
            List<StartTag> tags = source.getAllStartTags(tagAttrPair[0]);
            for (StartTag startTag : tags) {
                final Attributes attributes = startTag.getAttributes();
                final Attribute attribute = attributes.get(tagAttrPair[1]);
                if (attribute != null && !StringUtils.isEmpty(attribute.getValue())) {
                    newRefs.putAll(replaceRefsByPlaceholders(document, attribute, refs));
                }
            }
        }
        String result = document.toString();

        if (!newRefs.equals(refs)) {
            if (!newRefs.isEmpty() && !node.isNodeType("jmix:referencesInField")) {
                node.addMixin("jmix:referencesInField");
            }
            logger.debug("New references : "+newRefs);
            NodeIterator ni = node.getNodes("j:referenceInField");
            while (ni.hasNext()) {
                JCRNodeWrapper ref = (JCRNodeWrapper) ni.next();
                if (name.equals(ref.getProperty("j:fieldName").getString()) && !newRefs.containsKey(ref.getProperty("j:reference").getString())) {
                    ref.remove();
                }
            }

            for (Map.Entry<String,Long> entry : newRefs.entrySet()) {
                if (!refs.containsKey(entry.getKey())) {
                    JCRNodeWrapper ref = node.addNode("j:referenceInField", "jnt:referenceInField");
                    ref.setProperty("j:fieldName",name);
                    ref.setProperty("j:id", entry.getValue());
                    ref.setProperty("j:reference", entry.getKey());
                }
            }
        }


        if (!result.equals(content)) {
            return node.getSession().getValueFactory().createValue(result);
        }
        return originalValue;
    }

    /**
     * Called before setting the value on the property. Can throw an exception if the value is not valid, and transform
     * the value into another value.
     * <p/>
     * The interceptor can also directly operate on the property before the property is effectively set.
     * <p/>
     * Returns the value to set - or null if no property need to be set, but without sending an error.
     *
     * @param node
     * @param name
     * @param definition
     * @param originalValues Original value  @return Value to set, or null   @throws ValueFormatException
     * @throws javax.jcr.version.VersionException
     *
     * @throws javax.jcr.lock.LockException
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *
     */
    public Value[] beforeSetValues(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value[] originalValues) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value[] res = new Value[originalValues.length];

        for (int i = 0; i < originalValues.length; i++) {
            Value originalValue = originalValues[i];
            res[i] = beforeSetValue(node, name, definition, originalValue);
        }
        return res;
    }

    /**
     * Restore value by replace context ( ##doc-context## and ##cms-context## ) and references ( ##ref:link[0-9]+##
     * placeholders. Resolves reference node and put path instead to make a valid link. If referenced node is not found,
     * log an error and put # as a path.
     *
     * @param property
     * @param storedValue
     * @return
     * @throws ValueFormatException
     * @throws RepositoryException
     */
    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue) throws ValueFormatException, RepositoryException {
        String content = storedValue.getString();

        logger.debug("Intercept getValue for "+property.getPath());

        Map<Long, String> refs = new HashMap<Long, String>();

        if (property.getParent().isNodeType("jmix:referencesInField")) {
            NodeIterator ni = property.getParent().getNodes("j:referenceInField");
            while (ni.hasNext()) {
                JCRNodeWrapper ref = (JCRNodeWrapper) ni.next();
                if (property.getDefinition().getName().equals(ref.getProperty("j:fieldName").getString())) {
                    refs.put(ref.getProperty("j:id").getLong(), ref.getProperty("j:reference").getString());
                }
            }
        }

        Source source = new Source(content);
        OutputDocument document = new OutputDocument(source);

        for (String[] tagAttrPair : URLFilter.TAG_ATTRIBUTE_PAIR) {
            List<StartTag> tags = source.getAllStartTags(tagAttrPair[0]);
            for (StartTag startTag : tags) {
                final Attributes attributes = startTag.getAttributes();
                final Attribute attribute = attributes.get(tagAttrPair[1]);
                if (attribute != null && !StringUtils.isEmpty(attribute.getValue())) {
                    replacePlaceholdersByRefs(document, attribute, refs);
                }
            }
        }
        String result = document.toString();

        if (!result.equals(content)) {
            return property.getSession().getValueFactory().createValue(result);
        }
        return storedValue;
    }

    /**
     * Called after getting the value. Stored value is passed to the interceptor and can be transformed.
     *
     * @param property
     * @param storedValues
     * @return
     */
    public Value[] afterGetValues(JCRPropertyWrapper property, Value[] storedValues) throws ValueFormatException, RepositoryException {
        Value[] res = new Value[storedValues.length];

        for (int i = 0; i < storedValues.length; i++) {
            Value storedValue = storedValues[i];
            res[i] = afterGetValue(property, storedValue);
        }
        return res;
    }

    Map<String, Long> replaceRefsByPlaceholders(final OutputDocument document, final Attribute attr, final Map<String, Long> oldRefs) throws RepositoryException {
        final HashMap<String, Long> refs = new HashMap<String, Long>();
        final String originalValue = attr.getValue();

        logger.debug("Before replaceRefsByPlaceholders : "+originalValue);

        String pathPart = originalValue;
        final boolean isCmsContext;

        if (pathPart.startsWith(dmsContext)) {
            // Remove DOC context part
            pathPart = StringUtils.substringAfter(pathPart, dmsContext);
            isCmsContext = false;
        } else if (pathPart.startsWith(cmsContext)) {
            // Remove CMS context part
            Matcher m = cmsPattern.matcher(pathPart);
            if (!m.matches()) {
                throw new ConstraintViolationException("Invalid link "+pathPart);
            }
            pathPart = m.group(5);
            isCmsContext = true;
        } else {
            return refs;
        }

        final String path = "/" + pathPart;

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                String value = originalValue;
                String ext = null;
                String tpl = null;
                JCRNodeWrapper reference;
                try {
                    String currentPath = path;
                    if (isCmsContext) {
                        while (true) {
                            int i = currentPath.lastIndexOf('.');
                            if (i > currentPath.lastIndexOf('/')) {
                                if (ext == null) {
                                    ext = currentPath.substring(i + 1);
                                } else if (tpl == null) {
                                    tpl = currentPath.substring(i + 1);
                                } else {
                                    tpl = currentPath.substring(i + 1) + "." + tpl;
                                }
                                currentPath = currentPath.substring(0, i);
                            } else {
                                throw new PathNotFoundException("not found in "+path);
                            }
                            try {
                                reference = session.getNode(currentPath);
                                break;
                            } catch (PathNotFoundException e) {
                                // continue
                            }
                        }
                        value = CMS_CONTEXT_PLACEHOLDER + StringUtils.substringAfter(value, cmsContext);
                    } else {
                        // retrieve path
                        while (true) {
                            if (StringUtils.contains(currentPath,'/')) {
                                currentPath = StringUtils.substringAfter(currentPath,"/");
                            } else {
                                throw new PathNotFoundException("not found in "+path);
                            }
                            try {
                                reference = session.getNode("/"+currentPath);
                                break;
                            } catch (PathNotFoundException e) {
                                // continue
                            }
                        }
                        value = DOC_CONTEXT_PLACEHOLDER + StringUtils.substringAfter(value, dmsContext);
                    }
                } catch (PathNotFoundException e) {
                    throw new ConstraintViolationException("Invalid link", e);
                }
                String id = reference.getIdentifier();
                if (!refs.containsKey(id) && oldRefs.containsKey(id)) {
                    refs.put(id, oldRefs.get(id));
                }
                if (!refs.containsKey(id)) {
                    Long max = oldRefs.isEmpty() ? 0 : Collections.max(oldRefs.values());
                    refs.put(id, max + 1);
                }
                Long index = refs.get(id);
                String link = "/##ref:link" + index + "##";
                if (tpl != null) {
                    link += "." + tpl;
                }
                if (ext != null) {
                    link += "." + ext;
                }
                value = value.replace(path, link);
                document.replace(attr.getValueSegment(), value);
                logger.debug("After replaceRefsByPlaceholders : "+value);
                return null;
            }
        });

        return refs;
    }


    void replacePlaceholdersByRefs(final OutputDocument document, final Attribute attr, final Map<Long, String> refs) throws RepositoryException {
        final String originalValue = attr.getValue();

        String pathPart = originalValue;
        logger.debug("Before replacePlaceholdersByRefs : "+originalValue);
        final boolean isCmsContext;

        if (pathPart.startsWith(DOC_CONTEXT_PLACEHOLDER)) {
            // Remove DOC context part
            pathPart = StringUtils.substringAfter(pathPart, DOC_CONTEXT_PLACEHOLDER);
            isCmsContext = false;
        } else if (pathPart.startsWith(CMS_CONTEXT_PLACEHOLDER)) {
            // Remove CMS context part
            Matcher m = cmsPatternWithContextPlaceholder.matcher(pathPart);
            if (!m.matches()) {
                logger.error("Cannot match URL : "+pathPart);
                return;
            }
            pathPart = m.group(5);
            isCmsContext = true;
        } else {
            return;
        }

        final String path = "/" + pathPart;

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    Matcher matcher = refPattern.matcher(path);
                    if (!matcher.matches()) {
                        logger.error("Cannot match value, should contain ##ref : " + path);
                        return null;
                    }
                    String id = matcher.group(1);
                    String ext = matcher.group(2);
                    String uuid = refs.get(new Long(id));
                    String nodePath = null;
                    try {
                        nodePath = session.getNodeByUUID(uuid).getPath();
                    } catch (ItemNotFoundException infe) {
                        logger.warn("Cannot found referenced item : "+uuid);
                        nodePath = "/#";
                    }
                    String value = originalValue.replace(path, nodePath + ext);
                    if (isCmsContext) {
                        value = value.replace(CMS_CONTEXT_PLACEHOLDER, cmsContext);
                    } else {
                        value = value.replace(DOC_CONTEXT_PLACEHOLDER, dmsContext);
                    }
                    logger.debug("After replacePlaceholdersByRefs : "+value);
                    document.replace(attr.getValueSegment(), value);
                } catch (Exception e) {
                    logger.error("Exception when transforming placeholder for" + path,e);
                }
                return null;
            }
        });


    }

    public void afterPropertiesSet() throws Exception {
        dmsContext = Jahia.getContextPath() + "/files/";
        cmsContext = Jahia.getContextPath() + "/cms/";

        String pattern = "(((render|edit)/[a-zA-Z]+)|" + 
                escape(URLFilter.CURRENT_CONTEXT_PLACEHOLDER) + ")/([a-zA-Z_]+|" +
                escape(URLFilter.LANG_PLACEHOLDER) + ")/(.*)";

        refPattern = Pattern.compile("/##ref:link([0-9]+)##(.*)");
        cmsPattern = Pattern.compile(cmsContext + pattern);
        cmsPatternWithContextPlaceholder = Pattern.compile(escape(CMS_CONTEXT_PLACEHOLDER) + pattern);
    }


}
