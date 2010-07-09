package org.jahia.modules.social;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jahia.bin.ActionResult;
import org.jahia.bin.BaseAction;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

/**
 * Action to remove a friend from a user's connection.
 * 
 * @author Serge Huber
 */
public class RemoveSocialConnectionAction extends BaseAction {

    private SocialService socialService;

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
            Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {

        final String fromUserId = req.getParameter("fromUserId");
        final String toUserId = req.getParameter("toUserId");
        final String connectionType = req.getParameter("connectionType");

        socialService.removeSocialConnection(fromUserId, toUserId, connectionType);

        return ActionResult.OK_JSON;
    }

    public void setSocialService(SocialService socialService) {
        this.socialService = socialService;
    }
}