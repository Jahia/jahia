package org.jahia.modules.social;

import org.apache.log4j.Logger;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Action to remove a friend from a user's connection.
 *
 * @author loom
 *         Date: Jun 22, 2010
 *         Time: 9:24:09 AM
 */
public class RemoveSocialConnectionAction implements Action {

    private static Logger logger = Logger.getLogger(RemoveSocialConnectionAction.class);

    private String name;
    private SocialService socialService;

    public SocialService getSocialService() {
        return socialService;
    }

    public void setSocialService(SocialService socialService) {
        this.socialService = socialService;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {

        JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(
                resource.getWorkspace(), resource.getLocale());

        final JCRNodeWrapper node = resource.getNode();

        final String fromUserId = req.getParameter("fromUserId");
        final String toUserId = req.getParameter("toUserId");
        final String connectionType = req.getParameter("connectionType");

        socialService.removeSocialConnection(jcrSessionWrapper, fromUserId, toUserId, connectionType);

        /* TODO to be implemented */
        JSONObject results = new JSONObject();

        return new ActionResult(HttpServletResponse.SC_OK, null, results);
    }
}