package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.Script;

import javax.jcr.AccessDeniedException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 8, 2009
 * Time: 11:54:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class TemplatePermissionCheckFilter extends AbstractFilter {
    protected String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        Script script = (Script) renderContext.getRequest().getAttribute("script");
        String requirePermissions = script.getTemplate().getProperties().getProperty("requirePermissions");
        if (requirePermissions != null) {
            String[] perms = requirePermissions.split(" ");
            for (String perm : perms) {
                if (!resource.getNode().hasPermission("jcr:"+perm)) {
                    throw new AccessDeniedException();
                }
            }
        }
        return chain.doFilter(renderContext, resource);
    }
}
