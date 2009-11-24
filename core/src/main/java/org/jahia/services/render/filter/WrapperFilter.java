package org.jahia.services.render.filter;

import org.jahia.services.render.*;
import org.jahia.services.content.JCRNodeWrapper;
import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 24, 2009
 * Time: 12:30:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class WrapperFilter extends AbstractFilter {
    private static Logger logger = Logger.getLogger(WrapperFilter.class);

    public String doFilter(RenderContext renderContext, Resource resource, String output, RenderChain chain) throws IOException, RepositoryException {

        output = chain.doFilter(renderContext, resource, output);

        JCRNodeWrapper node = resource.getNode();
        while (resource.hasWrapper()) {
            String wrapper = resource.popWrapper();
            try {
                Resource wrappedResource = new Resource(node, resource.getTemplateType(), null, wrapper);
                if (service.hasTemplate(node.getPrimaryNodeType(), wrapper)) {
                    Script script = service.resolveScript(wrappedResource, renderContext);
                    renderContext.getRequest().setAttribute("wrappedContent", output);
                    output = script.execute();
                } else {
                    logger.warn("Cannot get wrapper "+wrapper);
                }
            } catch (IOException e) {
                logger.error("Cannot execute wrapper "+wrapper,e);
            } catch (TemplateNotFoundException e) {
                logger.debug("Cannot find wrapper "+wrapper,e);
            }
        }
        return output;
    }
}
