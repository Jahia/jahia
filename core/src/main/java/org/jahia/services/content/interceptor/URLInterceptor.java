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
package org.jahia.services.content.interceptor;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.services.content.*;
import org.jahia.services.content.interceptor.url.URLReplacer;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.ContextPlaceholdersReplacer;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.jahia.api.Constants.JAHIAMIX_REFERENCES_IN_FIELD;
import static org.jahia.api.Constants.JAHIA_REFERENCE_IN_FIELD_PREFIX;

/**
 * URL Interceptor catches internal URLs inside richtext, and transform them to store references to the pointed nodes
 * instead of paths. It also replaces the servlet context and servlet name by a placeholder so that the stored link
 * is not dependant of the deployment.
 * <p/>
 * Two types of links are detected : CMS links (like /cms/render/default/en/sites/ACME/home.html ) and files
 * links ( /files/sites/ACME/files/Pictures/BannerTeaser/img-home-fr.jpg ).
 * <p/>
 * File path are transformed with references placeholders like ##ref:link1##. References targets are stored in the
 * jmix:referenceInField child nodes.
 */
public class URLInterceptor extends BaseInterceptor implements InitializingBean {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(URLInterceptor.class);

    public static final String DOC_CONTEXT_PLACEHOLDER = "##doc-context##/";
    public static final String CMS_CONTEXT_PLACEHOLDER = "##cms-context##/";
    public static final Pattern DOC_CONTEXT_PLACEHOLDER_PATTERN = Pattern.compile(DOC_CONTEXT_PLACEHOLDER, Pattern.LITERAL);
    public static final Pattern CMS_CONTEXT_PLACEHOLDER_PATTERN = Pattern.compile(CMS_CONTEXT_PLACEHOLDER, Pattern.LITERAL);
    public static final String MISSING_IMAGE = "/missing-image.png";

    private String dmsContext;
    private String cmsContext;
    private Pattern cmsPattern;
    private Pattern cmsPatternWithContextPlaceholder;
    private Pattern refPattern;

    private List<URLReplacer> urlReplacers;

    private HtmlTagAttributeTraverser urlTraverser;

    /** Escape curly braces and allow percent-encoded curly braces. */
    private String escapeCurlyBraces(String s) {
        return s
                .replace("{", "(?:\\{|%7[bB])")
                .replace("}", "(?:\\}|%7[dD])");
    }

    /**
     * Initializes an instance of this class.
     *
     * @param urlTraverser the URL utility class to visit HTML tag attributes
     */
    public URLInterceptor(HtmlTagAttributeTraverser urlTraverser) {
        super();
        this.urlTraverser = urlTraverser;
    }

    public void beforeRemove(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition) throws VersionException, LockException, ConstraintViolationException, RepositoryException {
    	
        if (node.isNodeType(JAHIAMIX_REFERENCES_IN_FIELD)) {
            NodeIterator ni = node.getNodes(JAHIA_REFERENCE_IN_FIELD_PREFIX);
            if (definition.isInternationalized()) {
                name += "_" + node.getSession().getLocale();
            }
            while (ni.hasNext()) {
                JCRNodeWrapper ref = (JCRNodeWrapper) ni.next();
                if (name.equals(ref.getProperty("j:fieldName").getString())) {
                    ref.remove();
                }
            }
        }
    }

    /**
     * Transform user URL with servlet context and links placeholders for storage.
     * <p/>
     * Only URLs starting with /<context>/cms or /<context>/files are recognized.
     * <p/>
     * CMS URLs can use mode and language placeholders : /<context>/cms/render/default/en/sites/ACME/home.html and
     * /<context>/cms/##mode##/##lang##/sites/ACME/home.html are both recognized.
     * <p/>
     * If any link is invalid, a ConstraintViolationException is thrown.
     * <p/>
     * Add jmix:referencesInField mixin type to the parent node and j:referenceInField with the list of references
     * contained in the value.
     *
     * @param node
     * @param name
     * @param definition
     * @param originalValue Original value  @return Value to set, or null   @return
     * @throws ValueFormatException
     * @throws VersionException
     * @throws LockException
     * @throws ConstraintViolationException
     * @throws RepositoryException in case of JCR-related errors
     */
    public Value beforeSetValue(final JCRNodeWrapper node, String name, final ExtendedPropertyDefinition definition, Value originalValue) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        String content = originalValue.getString();
        
        // if the node is a translated node, then take the parent to have the references
        JCRNodeWrapper nodeWithReferences = node.isNodeType(Constants.JAHIANT_TRANSLATION) ? node.getParent() : node;

        if (definition.isInternationalized()) {
            Locale locale = node.getSession().getLocale();
            if (locale == null) {
                // This might happen under publication
                if (node.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                    name += "_" + node.getProperty("jcr:language").getString();
                }
            } else {
                name += "_" + locale;
            }
        }

        final Map<String, Long> refs = new HashMap<String, Long>();

        if (logger.isDebugEnabled()) {
            logger.debug("Intercept setValue for " + node.getPath() + "/" + name);
        }

        if (nodeWithReferences.isNodeType(JAHIAMIX_REFERENCES_IN_FIELD)) {
            NodeIterator ni = nodeWithReferences.getNodes(JAHIA_REFERENCE_IN_FIELD_PREFIX);
            while (ni.hasNext()) {
                JCRNodeWrapper ref = (JCRNodeWrapper) ni.next();
                if (name.equals(ref.getProperty("j:fieldName").getString()) && ref.hasProperty("j:reference")) {
                    refs.put(ref.getProperty("j:reference").getString(), Long.valueOf(StringUtils.substringAfterLast(ref.getName(), "_")));
                }
            }
        }

        final Map<String, Long> newRefs = new HashMap<String, Long>();

        String result;
        try {
            result = urlTraverser.traverse(content, new HtmlTagAttributeVisitor() {
                public String visit(String value, RenderContext context, String tagName, String attrName, Resource resource) {
                    if (StringUtils.isNotEmpty(value)) {
                        try {
                            for (URLReplacer urlReplacer : urlReplacers) {
                                if (urlReplacer.canHandle(tagName, attrName)) {
                                    value = urlReplacer.replaceRefsByPlaceholders(value, newRefs, refs, node.getSession().getWorkspace().getName(), node.getSession().getLocale(), node, definition);
                                    break;
                                }
                            }
                        } catch (RepositoryException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return value;
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof RepositoryException) {
                throw (RepositoryException) e.getCause();
            } else {
                throw e;
            }
        }

        Set<String> refsToRemove = new HashSet<>(refs.size());
        if (!newRefs.equals(refs)) {
            if (!newRefs.isEmpty() && !nodeWithReferences.isNodeType(JAHIAMIX_REFERENCES_IN_FIELD)) {
                nodeWithReferences.addMixin(JAHIAMIX_REFERENCES_IN_FIELD);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("New references : " + newRefs);
            }

            NodeIterator ni = nodeWithReferences.getNodes(JAHIA_REFERENCE_IN_FIELD_PREFIX);
            while (ni.hasNext()) {
                JCRNodeWrapper ref = (JCRNodeWrapper) ni.next();
                if (name.equals(ref.getProperty("j:fieldName").getString()) && (!ref.hasProperty("j:reference") || !newRefs.containsKey(ref.getProperty("j:reference").getString()))) {
                    refsToRemove.add(ref.getName());
                }
            }

            for (Map.Entry<String, Long> entry : newRefs.entrySet()) {
                if (!refs.containsKey(entry.getKey())) {
                    JCRNodeWrapper ref = nodeWithReferences.addNode("j:referenceInField_" + Text.escapeIllegalJcrChars(name) + "_" + entry.getValue(), "jnt:referenceInField");
                    ref.setProperty("j:fieldName", name);
                    ref.setProperty("j:reference", entry.getKey());
                }
            }
        }

        for (String refToRemove : refsToRemove) {
            nodeWithReferences.getNode(refToRemove).remove();
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
     * @throws javax.jcr.lock.LockException
     * @throws javax.jcr.nodetype.ConstraintViolationException
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
     * @throws RepositoryException in case of JCR-related errors
     */
    public Value afterGetValue(final JCRPropertyWrapper property, Value storedValue) throws ValueFormatException, RepositoryException {
        String content = storedValue.getString();
        if (content == null || !content.contains(DOC_CONTEXT_PLACEHOLDER) && !content.contains(CMS_CONTEXT_PLACEHOLDER)) {
            return storedValue;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Intercept getValue for " + property.getPath());
        }

        final Map<Long, String> refs = new HashMap<Long, String>();

        final ExtendedPropertyDefinition definition = (ExtendedPropertyDefinition) property.getDefinition();
        String name = definition.getName();
        JCRNodeWrapper parent = property.getParent();
        if (definition.isInternationalized()) {
            name += "_" + property.getLocale();
        }

        if (parent.isNodeType(Constants.JAHIANT_TRANSLATION)) {
            parent = parent.getParent();
        }
        if (parent.isNodeType(JAHIAMIX_REFERENCES_IN_FIELD)) {
            NodeIterator ni = parent.getNodes(JAHIA_REFERENCE_IN_FIELD_PREFIX);
            while (ni.hasNext()) {
                JCRNodeWrapper ref = (JCRNodeWrapper) ni.next();
                if (name.equals(ref.getProperty("j:fieldName").getString()) && ref.hasProperty("j:reference")) {
                    try {
                        refs.put(Long.valueOf(StringUtils.substringAfterLast(ref.getName(), "_")), ref.getProperty("j:reference").getString());
                    } catch (PathNotFoundException e) {
                        logger.warn(
                                "Unable to get j:reference field on the node {}. Skipping reference.",
                                ref.getPath());
                    }
                }
            }
        }

        String result;
        try {
            result = urlTraverser.traverse(content, new HtmlTagAttributeVisitor() {
                public String visit(String value, RenderContext context, String tagName, String attrName, Resource resource) {
                    if (StringUtils.isNotEmpty(value)) {
                        try {
                            for (URLReplacer urlReplacer : urlReplacers) {
                                if (urlReplacer.canHandle(tagName, attrName)) {
                                    value = urlReplacer.replacePlaceholdersByRefs(value, refs, property.getSession().getWorkspace().getName(), property.getSession().getLocale(), property.getParent());
                                    break;
                                }
                            }

                            if ("#".equals(value) && (tagName.toLowerCase().equals("img") || tagName.toLowerCase().equals("source"))) {
                                value = MISSING_IMAGE;
                            }
                        } catch (RepositoryException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return value;
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof RepositoryException) {
                throw (RepositoryException) e.getCause();
            } else {
                throw e;
            }
        }

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

    public void afterPropertiesSet() throws Exception {
        dmsContext = Jahia.getContextPath() + "/files/";
        cmsContext = Jahia.getContextPath() + "/cms/";

        String pattern = "(((render|edit|live|contribute)/[a-zA-Z]+)|" +
                escapeCurlyBraces(ContextPlaceholdersReplacer.CURRENT_CONTEXT_PLACEHOLDER) + ")/([a-zA-Z_]+|" +
                escapeCurlyBraces(ContextPlaceholdersReplacer.LANG_PLACEHOLDER) + ")/(.*)";

        refPattern = Pattern.compile("/##ref:link([0-9]+)##(.*)");
        cmsPattern = Pattern.compile(cmsContext + pattern);
        cmsPatternWithContextPlaceholder = Pattern.compile(CMS_CONTEXT_PLACEHOLDER + pattern);
    }

    public void setUrlReplacers(List<URLReplacer> urlReplacers) {
        this.urlReplacers = urlReplacers;
    }

    public String getDmsContext() {
        return dmsContext;
    }

    public String getCmsContext() {
        return cmsContext;
    }

    public Pattern getCmsPattern() {
        return cmsPattern;
    }

    public Pattern getCmsPatternWithContextPlaceholder() {
        return cmsPatternWithContextPlaceholder;
    }

    public Pattern getRefPattern() {
        return refPattern;
    }
}
