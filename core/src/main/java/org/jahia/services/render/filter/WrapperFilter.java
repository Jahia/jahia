package org.jahia.services.render.filter;

import org.jahia.services.render.*;
import org.jahia.services.content.JCRNodeWrapper;
import org.apache.log4j.Logger;
import org.jahia.services.render.scripting.Script;

/**
 * WrapperFilter
 *
 * Looks for all registered wrappers in the resource and calls the associated scripts around the output.
 * Output is made available to the wrapper script through the "wrappedContent" request attribute.
 *
 */
public class WrapperFilter extends AbstractFilter {
    private static Logger logger = Logger.getLogger(WrapperFilter.class);

    public String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {

        String output = chain.doFilter(renderContext, resource);
        if (renderContext.getRequest().getParameter("ajaxcall") != null) {
            return output;
        }
        while (resource.hasWrapper()) {
            Resource.Wrapper wrapper = resource.popWrapper();
            try {
                JCRNodeWrapper wrapperNode = wrapper.node;
                Resource wrappedResource = new Resource(wrapperNode, resource.getTemplateType().equals("edit")?"html":resource.getTemplateType(), null, wrapper.template,
                        Resource.CONFIGURATION_WRAPPER);
                if (service.hasTemplate(wrapperNode.getPrimaryNodeType(), wrapper.template)) {
                    Object wrappedContent = renderContext.getRequest().getAttribute("wrappedContent");
                    try {
                        renderContext.getRequest().setAttribute("wrappedContent", output);
                        output = RenderService.getInstance().render(wrappedResource, renderContext);
                    } finally {
                        renderContext.getRequest().setAttribute("wrappedContent", wrappedContent);
                    }
                } else {
                    logger.warn("Cannot get wrapper "+wrapper);
                }
            } catch (TemplateNotFoundException e) {
                logger.debug("Cannot find wrapper "+wrapper,e);
            } catch (RenderException e) {
                logger.error("Cannot execute wrapper "+wrapper,e);
            }
        }
        return output;
    }
}
