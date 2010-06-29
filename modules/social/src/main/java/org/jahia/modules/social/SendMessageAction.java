package org.jahia.modules.social;

import org.apache.log4j.Logger;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.usermanager.JahiaLDAPUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.json.JSONObject;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Action to send a message from one user to another.
 *
 * @author loom
 *         Date: Jun 22, 2010
 *         Time: 9:53:53 AM
 */
public class SendMessageAction implements Action {

    private static Logger logger = Logger.getLogger(SendMessageAction.class);

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

        final String toUserKey = req.getParameter("j:to");
        final String subject = req.getParameter("j:subject");
        final String body = req.getParameter("j:body");

        if (!socialService.sendMessage(node, toUserKey, subject, body)) {
            return new ActionResult(HttpServletResponse.SC_BAD_REQUEST, null, null);
        }

        JSONObject results = new JSONObject();

        return new ActionResult(HttpServletResponse.SC_OK, null, results);
    }

}