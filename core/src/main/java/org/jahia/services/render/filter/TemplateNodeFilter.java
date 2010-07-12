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
            if (renderContext.getRequest().getAttribute("templateSet") == null) {
                template = pushBodyWrappers(resource);
                renderContext.getRequest().setAttribute("templateSet", Boolean.TRUE);
            } else {
                previousTemplate = (Template) renderContext.getRequest().getAttribute("previousTemplate");
                if (previousTemplate != null) {
                    template = previousTemplate.next;
                }
            }

            if (template != null && !renderContext.isAjaxRequest()) {
                try {
                    JCRNodeWrapper templateNode = template.node;
                    renderContext.getRequest().setAttribute("previousTemplate", template);
                    renderContext.getRequest().setAttribute("wrappedResource", resource);
                    Resource wrapperResource = new Resource(templateNode,
                            resource.getTemplateType().equals("edit") ? "html" : resource.getTemplateType(), template.templateName, Resource.CONFIGURATION_WRAPPER);
                    if (service.hasTemplate(templateNode.getPrimaryNodeType(), template.templateName)) {
                        chain.pushAttribute(renderContext.getRequest(), "inWrapper", Boolean.TRUE);

                        Integer currentLevel =
                                (Integer) renderContext.getRequest().getAttribute("org.jahia.modules.level");
                        if (currentLevel != null) {
                            renderContext.getRequest().removeAttribute("areaNodeTypesRestriction" + (currentLevel));
                        }

                        String output = RenderService.getInstance().render(wrapperResource, renderContext);
                        if (renderContext.isEditMode()) {
                            output = "<div jahiatype=\"linkedContentInfo\" linkedNode=\"" +
                                    resource.getNode().getIdentifier() + "\"" +

                                    " node=\"" + templateNode.getIdentifier() + "\" type=\"template\">" + output +
                                    "</div>";
                        }

                        renderContext.getRequest().setAttribute("previousTemplate", previousTemplate);

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

            JCRNodeWrapper templateNode = null;

            while (template == null) {
                if (current.hasProperty("j:templateNode")) {
                    templateNode = (JCRNodeWrapper) current.getProperty("j:templateNode").getNode();
                    template = addDerivedTemplates(resource, template, templateNode);
                    if (template == null && current == node) {
                        template = new Template(templateNode.hasProperty("j:template") ? templateNode.getProperty("j:template").getString() :
                            "fullpage", templateNode, template);
                    }
                }
                if (template == null && current.hasProperty("j:defaultTemplateNode")) {
                    templateNode = (JCRNodeWrapper) current.getProperty("j:defaultTemplateNode").getNode();
                    template = addDerivedTemplates(resource, template, templateNode);
                    if (template == null) {
                        template = new Template(templateNode.hasProperty("j:template") ? templateNode.getProperty("j:template").getString() :
                            "fullpage", templateNode, template);
                    }
                }
                if (template == null && current.isNodeType("jnt:template")) {
                    templateNode = current;
                    break;
                }
                current = current.getParent();
            }
            templateNode = templateNode.getParent();
            while (!(templateNode.isNodeType("jnt:templatesFolder"))) {
                template = new Template(templateNode.hasProperty("j:template") ? templateNode.getProperty("j:template").getString() :
                            "fullpage", templateNode, template);
                templateNode = templateNode.getParent();
            }
        } catch (ItemNotFoundException e) {
            // default

            try {
                template = new Template("system", node.getSession().getNode("/systemTemplate"), null);
            } catch (RepositoryException e1) {
                logger.error("Cannot find default template", e);
            }
        } catch (RepositoryException e) {
            logger.error("Cannot find template", e);
        }
        return template;
    }

    private Template addDerivedTemplates(Resource resource, Template template,
                                         JCRNodeWrapper templateNode) throws RepositoryException {
        Query q = templateNode.getSession().getWorkspace().getQueryManager().createQuery(
                "select * from [jnt:derivedTemplate] as w where ischildnode(w, ['" + templateNode.getPath() + "'])",
                Query.JCR_SQL2);
        QueryResult result = q.execute();
        NodeIterator ni = result.getNodes();
        while (ni.hasNext()) {
            final JCRNodeWrapper derivedTemplateNode = (JCRNodeWrapper) ni.nextNode();
            template = addTemplate(resource, template, derivedTemplateNode);
        }
        return template;
    }

    private Template addTemplate(Resource resource, Template template, JCRNodeWrapper templateNode)
            throws RepositoryException {
        boolean ok = true;
        if (templateNode.hasProperty("j:applyOn")) {
            ok = false;
            Value[] values = templateNode.getProperty("j:applyOn").getValues();
            for (Value value : values) {
                if (resource.getNode().isNodeType(value.getString())) {
                    ok = true;
                    break;
                }
            }
            if (values.length == 0) {
                ok = true;
            }
        }
        if (ok) {
            if (resource.getTemplate() == null || resource.getTemplate().equals("default")) {
                ok = !templateNode.hasProperty("j:templateKey");
            } else {
                ok = templateNode.hasProperty("j:templateKey") && resource.getTemplate().equals(templateNode.getProperty("j:templateKey").getString());
                if (ok) {
                    resource.setTemplate(null);
                }
            }
        }
        if (ok) {
            template = new Template(
                    templateNode.hasProperty("j:template") ? templateNode.getProperty("j:template").getString() :
                            "default", templateNode, template);
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