package org.jahia.services.content.interceptor;

import net.htmlparser.jericho.*;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.SelectorType;
import org.jahia.services.render.filter.URLFilter;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.ServletConfigAware;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

        List<String> refs = new ArrayList<String>();

        if (node.isNodeType("jmix:references")) {
            Value[] v = node.getProperty("j:references").getValues();
            for (Value value : v) {
                refs.add(value.getString());
            }
        }

        List<String> newRefs = new ArrayList<String>();

        Source source = new Source(content);
        OutputDocument document = new OutputDocument(source);

        for (String[] tagAttrPair : URLFilter.TAG_ATTRIBUTE_PAIR) {
            List<StartTag> tags = source.getAllStartTags(tagAttrPair[0]);
            for (StartTag startTag : tags) {
                final Attributes attributes = startTag.getAttributes();
                final Attribute attribute = attributes.get(tagAttrPair[1]);
                replaceRefs(document, attribute, newRefs, false);
            }
        }
        String result = document.toString();

        if (!newRefs.equals(refs)) {
            node.addMixin("jmix:references");
            Value[] values = new Value[newRefs.size()];
            int i = 0;
            for (String ref : newRefs) {
                values[i++] = node.getSession().getValueFactory().createValue(ref, PropertyType.WEAKREFERENCE);
            }
            node.setProperty("j:references", values);
        }


        if (!result.equals(content)) {
            return node.getSession().getValueFactory().createValue(result);
        }
        return originalValue;
    }

    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue) throws ValueFormatException, RepositoryException {
        String content = storedValue.getString();

        List<String> refs = new ArrayList<String>();

        if (property.getParent().isNodeType("jmix:references")) {
            Value[] v = property.getParent().getProperty("j:references").getValues();
            for (Value value : v) {
                refs.add(value.getString());
            }
        }

        Source source = new Source(content);
        OutputDocument document = new OutputDocument(source);

        for (String[] tagAttrPair : URLFilter.TAG_ATTRIBUTE_PAIR) {
            List<StartTag> tags = source.getAllStartTags(tagAttrPair[0]);
            for (StartTag startTag : tags) {
                final Attributes attributes = startTag.getAttributes();
                final Attribute attribute = attributes.get(tagAttrPair[1]);
                replaceRefs(document, attribute, refs, true);
            }
        }
        String result = document.toString();

        if (!result.equals(content)) {
            return property.getSession().getValueFactory().createValue(result);
        }
        return storedValue;
    }

    void replaceRefs(OutputDocument document, Attribute attr, final List<String> refs, final boolean reverse) throws RepositoryException {
        String originalValue = attr.getValue();

        String pathPart = originalValue;
        final boolean isCmsContext;

        if (pathPart.startsWith(dmsContext)) {
            // Remove DOC context part
            pathPart = StringUtils.substringAfter(pathPart, dmsContext);
            isCmsContext = false;
        } else if (pathPart.startsWith(cmsContext)) {
            // Remove CMS context part
            Pattern p = Pattern.compile(cmsContext+"(((render)|(edit)/[a-zA-Z]+)|"+ URLFilter.CURRENT_CONTEXT_PLACEHOLDER +")/([a-zA-Z_]+|"+URLFilter.LANG_PLACEHOLDER+")/(.*)");
            Matcher m = p.matcher(pathPart);
            m.matches();
            pathPart = m.group(6);
            isCmsContext = true;
        } else {
            return;
        }

        final String path = "/" + pathPart;

        String link = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<String>() {
            public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                if (!reverse) {
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
                        } else {
                            reference = session.getNode(currentPath);
                        }
                    } catch (PathNotFoundException e) {
                        throw new ConstraintViolationException("Invalid link", e);
                    }

                    if (!refs.contains(reference.getIdentifier())) {
                        refs.add(reference.getIdentifier());
                    }
                    int index = refs.indexOf(reference.getIdentifier());
                    String link = "/##ref:link"+index+"##";
                    if (tpl != null) {
                        link += "." + tpl;
                    }
                    if (ext != null) {
                        link += "." + ext;
                    }
                    return link;
                } else {
                    try {
                        Matcher matcher = Pattern.compile("/##ref:link([0-9]+)##(.*)").matcher(path);
                        matcher.matches();
                        String id = matcher.group(1);
                        String ext = matcher.group(2);
                        String uuid = refs.get(Integer.parseInt(id));
                        String nodePath = session.getNodeByUUID(uuid).getPath();
                        return nodePath + ext;
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    return  path;
                }
            }
        });


        document.replace(attr.getValueSegment(), originalValue.replace(path, link));
    }



}
