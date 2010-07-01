package org.jahia.services.render.filter;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.*;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

/**
 * WrapperFilter
 * <p/>
 * Looks for all registered wrappers in the resource and calls the associated scripts around the output.
 * Output is made available to the wrapper script through the "wrappedContent" request attribute.
 */
public class TemplateNodeFilter extends AbstractFilter {
    private static Logger logger = Logger.getLogger(TemplateNodeFilter.class);

    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (renderContext.getRequest().getAttribute("skipWrapper") == null) {
            Template template = null;
            Template previousTemplate = null;
            if (renderContext.getRequest().getAttribute("wrapperSet") == null) {
                template = pushBodyWrappers(resource);
                renderContext.getRequest().setAttribute("wrapperSet", Boolean.TRUE);
            } else {
                previousTemplate = (Template) renderContext.getRequest().getAttribute("previouswrapper");
                if (previousTemplate != null) {
                    template = previousTemplate.next;
                }
            }

            if (template != null && !renderContext.isAjaxRequest()) {
                try {
                    JCRNodeWrapper wrapperNode = template.node;
                    renderContext.getRequest().setAttribute("previouswrapper", template);
//                chain.set(renderContext.getRequest(), "previouswrapper", wrapper);
                    renderContext.getRequest().setAttribute("wrappedResource", resource);
                    renderContext.getRequest().setAttribute("wrapperNode", wrapperNode);
                    Resource wrapperResource = new Resource(wrapperNode,
                            resource.getTemplateType().equals("edit") ? "html" : resource.getTemplateType(), template.templateName, Resource.CONFIGURATION_WRAPPER);
                    if (service.hasTemplate(wrapperNode.getPrimaryNodeType(), template.templateName)) {
                        chain.pushAttribute(renderContext.getRequest(), "inWrapper", Boolean.TRUE);
                        String output = RenderService.getInstance().render(wrapperResource, renderContext);
                        if (renderContext.isEditMode()) {
                            output = "<div jahiatype=\"linkedContentInfo\" linkedNode=\"" +
                                    resource.getNode().getIdentifier() + "\"" +

                                    " node=\"" + wrapperNode.getIdentifier() + "\" type=\"template\">" + output +
                                    "</div>";
                        }

                        renderContext.getRequest().setAttribute("previouswrapper", previousTemplate);

                        return output;
                    } else {
                        logger.warn("Cannot get wrapper " + template);
                    }
                } catch (TemplateNotFoundException e) {
                    logger.debug("Cannot find wrapper " + template, e);
                } catch (RenderException e) {
                    logger.error("Cannot execute wrapper " + template, e);
                }
            }
        }
        chain.pushAttribute(renderContext.getRequest(), "inWrapper",
                (renderContext.isAjaxRequest()) ? Boolean.TRUE : Boolean.FALSE);
        return null;
    }


    public Template pushBodyWrappers(Resource resource) {
        final JCRNodeWrapper node = resource.getNode();
        String templateName = resource.getTemplate();
        if ("default".equals(templateName)) {
            templateName = null;
        }
        JCRNodeWrapper current = node;

        Template template = null;
        try {
            if (current.isNodeType("jnt:derivedTemplate")) {
                current = current.getParent();
            }

            boolean mainTemplateFound = false;
            while (current != null) {
                if (current.hasProperty("j:templateNode")) {
                    current = (JCRNodeWrapper) current.getProperty("j:templateNode").getNode();
                } else {
                    current = current.getParent();
                    if (current.isNodeType("jnt:templatesFolder")) {
                        break;
                    }
                }
                if (current.isNodeType("jnt:masterTemplate")) {
                    if (!mainTemplateFound) {
                        mainTemplateFound = true;
                        Query q = current.getSession().getWorkspace().getQueryManager().createQuery(
                                "select * from [jnt:derivedTemplate] as w where ischildnode(w, ['" + current.getPath() + "'])",
                                Query.JCR_SQL2);
                        QueryResult result = q.execute();
                        NodeIterator ni = result.getNodes();
                        while (ni.hasNext()) {
                            final JCRNodeWrapper wrapperNode = (JCRNodeWrapper) ni.nextNode();
                            template = addTemplate(node, templateName, template, wrapperNode);
                        }
                        templateName = null;
                        if (template == null) {
                            template = addTemplate(node, templateName, template, current);                            
                        }
                    } else {
                        template = addTemplate(node, templateName, template, current);
                    }
                }

            }
        } catch (ItemNotFoundException e) {
            // default
            template = new Template("bodywrapper", node, null);
        } catch (RepositoryException e) {
            logger.error("Cannot find wrapper", e);
        }
        return template;
    }

    private Template addTemplate(JCRNodeWrapper node, String templateName, Template template, JCRNodeWrapper wrapperNode)
            throws RepositoryException {
        boolean ok = true;
        if (wrapperNode.hasProperty("j:applyOn")) {
            ok = false;
            Value[] values = wrapperNode.getProperty("j:applyOn").getValues();
            for (Value value : values) {
                if (node.isNodeType(value.getString())) {
                    ok = true;
                    break;
                }
            }
            if (values.length == 0) {
                ok = true;
            }
        }
        if (templateName == null) {
            ok &= !wrapperNode.hasProperty("j:templateKey");
        } else {
            ok &= wrapperNode.hasProperty("j:templateKey") && templateName.equals(wrapperNode.getProperty("j:templateKey").getString());
        }
        if (ok) {
            template = new Template(
                    wrapperNode.hasProperty("j:template") ? wrapperNode.getProperty("j:template").getString() :
                            "default", wrapperNode, template);
        }
        return template;
    }

    public class Template {
        public String templateName;
        public JCRNodeWrapper node;
        public Template next;

        Template(String templateName, JCRNodeWrapper node, Template next) {
            this.templateName = templateName;
            this.node = node;
            this.next = next;
        }

        @Override
        public String toString() {
            return templateName + " for node " + node.getPath();
        }
    }

}