package org.jahia.services.render.filter;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.render.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * OptionsFilter
 *
 * Filter that executes option mixin added to the current resource.
 *
 * The filter first fills in the available options in the resource, and add the remaining options (that were not
 * displayed with the OptionTag) to the output. Depending on the value of the renderOption parameter, options will be
 * added before or after the output.
 *
 */
public class OptionsFilter extends AbstractFilter {
    private static Logger logger = Logger.getLogger(OptionsFilter.class);

    public String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
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

        String output = chain.doFilter(renderContext, resource);
        
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
                        output = script.execute() + output;
                    } else {
                        output = output + script.execute();
                    }
                } catch (TemplateNotFoundException e) {
                    logger.debug("Cannot find wrapper " + wrapper, e);
                } catch (RenderException e) {
                    logger.error("Cannot execute wrapper " + wrapper, e);
                }
            }
        }

        return output;

    }

}
