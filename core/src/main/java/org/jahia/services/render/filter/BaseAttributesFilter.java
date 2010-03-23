package org.jahia.services.render.filter;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.render.scripting.Script;

/**
 * Stores the required request parameters before evaluating the template and restores original after. 
 * User: toto
 * Date: Nov 26, 2009
 * Time: 3:28:13 PM
 */
public class BaseAttributesFilter extends AbstractFilter {
    public String execute(RenderContext context, Resource resource, RenderChain chain) throws Exception {
        JCRNodeWrapper node = resource.getNode();

        final HttpServletRequest request = context.getRequest();

        request.setAttribute("renderContext", context);

        final Script script = service.resolveScript(resource, context);

        chain.pushAttribute(request, "script", script);
        chain.pushAttribute(request, "scriptInfo", script.getTemplate().getInfo());
        chain.pushAttribute(request, "workspace", node.getSession().getWorkspace().getName());
        chain.pushAttribute(request, "currentResource", resource);

        if (resource.getModuleParams().get("isInclude") == null) {
            chain.pushAttribute(request, "currentNode", node);
            chain.pushAttribute(request, "url",new URLGenerator(context, resource, service.getStoreService()));
        }

        String out;
        out = chain.doFilter(context, resource);

        return out;
    }


}
