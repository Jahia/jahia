package org.jahia.modules.defaultmodule;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

public class WorkspaceSwitchFilter extends AbstractFilter {

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String newWorkspace = resource.getNode().getProperty("workspace").getString();
        final HttpServletRequest request = renderContext.getRequest();
        if (!newWorkspace.equals(resource.getWorkspace())) {
            JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentUserSession(newWorkspace, resource.getNode().getSession().getLocale(), resource.getNode().getSession().getFallbackLocale());
            try {
                JCRNodeWrapper n = s.getNode(resource.getNode().getPath());
                chain.pushAttribute(request, "previousWorkspace", resource.getWorkspace());
                renderContext.setWorkspace(newWorkspace);
                resource.setNode(n);
                renderContext.getMainResource().setNode(s.getNode(renderContext.getMainResource().getNode().getPath()));
                request.setAttribute("workspace", newWorkspace);
                request.setAttribute("currentNode", n);
            } catch (PathNotFoundException e) {
                return "";
            }
        }
        return null;
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        final HttpServletRequest request = renderContext.getRequest();
        String previousWorkspace = (String) request.getAttribute("previousWorkspace");
        if (previousWorkspace != null) {
            renderContext.setWorkspace(previousWorkspace);
            JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentUserSession(previousWorkspace, resource.getNode().getSession().getLocale(), resource.getNode().getSession().getFallbackLocale());
            JCRNodeWrapper n = s.getNode(resource.getNode().getPath());
            resource.setNode(n);
            renderContext.getMainResource().setNode(s.getNode(renderContext.getMainResource().getNode().getPath()));
            request.setAttribute("workspace", previousWorkspace);
            request.setAttribute("currentNode", n);
        }
        return previousOut;
    }
}
