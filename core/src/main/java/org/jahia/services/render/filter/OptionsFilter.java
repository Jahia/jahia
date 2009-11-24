package org.jahia.services.render.filter;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.render.*;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 24, 2009
 * Time: 12:31:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class OptionsFilter extends AbstractFilter {
    private static Logger logger = Logger.getLogger(OptionsFilter.class);

    public String doFilter(RenderContext renderContext, Resource resource, String output, RenderChain chain) throws IOException, RepositoryException {
        JCRNodeWrapper node = resource.getNode();

        ExtendedNodeType[] mixinNodeTypes = null;

        HttpServletRequest request = renderContext.getRequest();
        if (request.getAttribute("renderOptions") == null || !request.getAttribute("renderOptions").equals("none")) {
            mixinNodeTypes = node.getMixinNodeTypes();
            if (mixinNodeTypes != null && mixinNodeTypes.length > 0) {
                for (ExtendedNodeType mixinNodeType : mixinNodeTypes) {
                    final String[] supertypeNames = mixinNodeType.getDeclaredSupertypeNames();
                    for (String supertypeName : supertypeNames) {
                        if(supertypeName.equals("jmix:option") && service.hasTemplate(mixinNodeType, "hidden.options.wrapper"))  {
                            resource.addOption("hidden.options.wrapper",mixinNodeType);
                        }
                    }
                }
            }
        }

        output = chain.doFilter(renderContext, resource, output);
        
        Script script;
        if (resource.hasOptions()) {
            List<Resource.Option> options = resource.getOptions();
            Collections.sort(options);
            for (Resource.Option option : options) {
                String wrapper = option.getWrapper();
                try {
                    Resource wrappedResource = new Resource(node, resource.getTemplateType(), null, wrapper);
                    wrappedResource.setWrappedMixinType(option.getNodeType());
                    script = service.resolveScript(wrappedResource, renderContext);
                    request.setAttribute("optionsAutoRendering", true);
                    if ("before".equals(request.getAttribute("renderOptions"))) {
                        output = output + script.execute();
                    } else {
                        output = script.execute() +output;
                    }
                } catch (IOException e) {
                    logger.error("Cannot execute wrapper " + wrapper, e);
                } catch (TemplateNotFoundException e) {
                    logger.debug("Cannot find wrapper " + wrapper, e);
                }
            }
        }

        return output;

    }

}
