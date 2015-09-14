package org.jahia.services.render.webflow;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class WebflowAction extends Action {

    private RenderService renderService;

    public void setRenderService(RenderService renderService) {
        this.renderService = renderService;
    }

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        Enumeration<?> parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String s = (String) parameterNames.nextElement();
            if (s.startsWith("webflowexecution")) {
                String id = s.substring("webflowexecution".length()).replace('_','-');
                String view = "default";
                if (id.contains(".")) {
                    view = StringUtils.substringAfter(id, ".");
                    id = StringUtils.substringBefore(id,".");
                }
                JCRNodeWrapper n = JCRTemplate.getInstance().getSessionFactory().getCurrentUserSession(renderContext.getWorkspace(), renderContext.getMainResourceLocale()).getNodeByUUID(id);
                renderService.render(new Resource(n, urlResolver.getResource().getTemplateType(), view , Resource.CONFIGURATION_MODULE), renderContext);
                return new ActionResult(HttpServletResponse.SC_OK, renderContext.getRedirect(), true, null );
            }
        }
        return ActionResult.OK;
    }
}
