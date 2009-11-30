package org.jahia.services.content.interceptor;

import net.htmlparser.jericho.*;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.SelectorType;
import org.jahia.services.render.filter.URLFilter;
import org.springframework.web.context.ServletContextAware;

import javax.jcr.NodeIterator;
import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 27, 2009
 * Time: 11:31:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class URLInterceptor implements PropertyInterceptor, ServletContextAware {
    private ServletConfig servletConfig;
    private ServletContext servletContext;

    private String context;
    private String dmsContext;
    private String cmsContext;

    public static String DOC_CONTEXT_PLACEHOLDER = "##doc-context##/";
    public static String CMS_CONTEXT_PLACEHOLDER = "##cms-context##/";


    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        context = servletContext.getInitParameter("contextPath");
        if (context.equals("/")) {
            dmsContext = "/files/";
            cmsContext = "/cms/";
        } else {
            dmsContext = context + "/files/";
            cmsContext = context + "/cms/";
        }
    }

    public boolean canApplyOnProperty(JCRNodeWrapper node, ExtendedPropertyDefinition definition) throws RepositoryException {
        return definition.getRequiredType() == PropertyType.STRING && definition.getSelector() == SelectorType.RICHTEXT;
    }

    public Value beforeSetValue(JCRNodeWrapper node, ExtendedPropertyDefinition definition, Value originalValue) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        String content = originalValue.getString();

        Map<String, Long> refs = new HashMap<String, Long>();

        if (node.isNodeType("jmix:referencesInField")) {
            NodeIterator ni = node.getNodes("j:referenceInField");
            while (ni.hasNext()) {
                JCRNodeWrapper ref = (JCRNodeWrapper) ni.next();
                if (definition.getName().equals(ref.getProperty("j:fieldName").getString())) {
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
                newRefs.putAll(replaceRefsByPlaceholders(document, attribute, refs));
            }
        }
        String result = document.toString();

        if (!newRefs.equals(refs)) {
            if (!newRefs.isEmpty() && node.isNodeType("jmix:referencesInField")) {
                node.addMixin("jmix:referencesInField");
            }

            NodeIterator ni = node.getNodes("j:referenceInField");
            while (ni.hasNext()) {
                JCRNodeWrapper ref = (JCRNodeWrapper) ni.next();
                if (definition.getName().equals(ref.getProperty("j:fieldName").getString()) && !newRefs.containsKey(ref.getProperty("j:reference").getString())) {
                    ref.remove();
                }
            }

            for (Map.Entry<String,Long> entry : newRefs.entrySet()) {
                if (!refs.containsKey(entry.getKey())) {
                    JCRNodeWrapper ref = node.addNode("j:referenceInField", "jnt:referenceInField");
                    ref.setProperty("j:fieldName",definition.getName());
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

    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue) throws ValueFormatException, RepositoryException {
        String content = storedValue.getString();

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
                replacePlaceholdersByRefs(document, attribute, refs);
            }
        }
        String result = document.toString();

        if (!result.equals(content)) {
            return property.getSession().getValueFactory().createValue(result);
        }
        return storedValue;
    }

    Map<String, Long> replaceRefsByPlaceholders(final OutputDocument document, final Attribute attr, final Map<String, Long> oldRefs) throws RepositoryException {
        final HashMap<String, Long> refs = new HashMap<String, Long>();
        final String originalValue = attr.getValue();

        String pathPart = originalValue;
        final boolean isCmsContext;

        if (pathPart.startsWith(dmsContext)) {
            // Remove DOC context part
            pathPart = StringUtils.substringAfter(pathPart, dmsContext);
            isCmsContext = false;
        } else if (pathPart.startsWith(cmsContext)) {
            // Remove CMS context part
            Pattern p = Pattern.compile(cmsContext + "(((render)|(edit)/[a-zA-Z]+)|" + URLFilter.CURRENT_CONTEXT_PLACEHOLDER + ")/([a-zA-Z_]+|" + URLFilter.LANG_PLACEHOLDER + ")/(.*)");
            Matcher m = p.matcher(pathPart);
            m.matches();
            pathPart = m.group(6);
            isCmsContext = true;
        } else {
            return refs;
        }

        final String path = "/" + pathPart;

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
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
                                throw new PathNotFoundException("not found");
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
                        reference = session.getNode(currentPath);
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
                return null;
            }
        });

        return refs;
    }


    void replacePlaceholdersByRefs(final OutputDocument document, final Attribute attr, final Map<Long, String> refs) throws RepositoryException {
        final String originalValue = attr.getValue();

        String pathPart = originalValue;
        final boolean isCmsContext;

        if (pathPart.startsWith(DOC_CONTEXT_PLACEHOLDER)) {
            // Remove DOC context part
            pathPart = StringUtils.substringAfter(pathPart, DOC_CONTEXT_PLACEHOLDER);
            isCmsContext = false;
        } else if (pathPart.startsWith(CMS_CONTEXT_PLACEHOLDER)) {
            // Remove CMS context part
            Pattern p = Pattern.compile(CMS_CONTEXT_PLACEHOLDER + "(((render)|(edit)/[a-zA-Z]+)|" + URLFilter.CURRENT_CONTEXT_PLACEHOLDER + ")/([a-zA-Z_]+|" + URLFilter.LANG_PLACEHOLDER + ")/(.*)");
            Matcher m = p.matcher(pathPart);
            m.matches();
            pathPart = m.group(6);
            isCmsContext = true;
        } else {
            return;
        }

        final String path = "/" + pathPart;

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    Matcher matcher = Pattern.compile("/##ref:link([0-9]+)##(.*)").matcher(path);
                    matcher.matches();
                    String id = matcher.group(1);
                    String ext = matcher.group(2);
                    String uuid = refs.get(new Long(id));
                    String nodePath = session.getNodeByUUID(uuid).getPath();
                    String value = originalValue.replace(path, nodePath + ext);
                    if (isCmsContext) {
                        value = value.replace(CMS_CONTEXT_PLACEHOLDER, URLInterceptor.this.cmsContext);
                    } else {
                        value = value.replace(DOC_CONTEXT_PLACEHOLDER, URLInterceptor.this.dmsContext);
                    }
                    document.replace(attr.getValueSegment(), value);
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                return null;
            }
        });


    }


}
