package org.jahia.modules.newsletter;

import org.jahia.bin.ActionResult;
import org.jahia.bin.Action;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.notification.SubscriptionService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 7, 2010
 * Time: 11:07:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfirmAction extends Action {
    private SubscriptionService subscriptionService;
    private String confirmationPagePath;

    public void setSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public void setConfirmationPagePath(String confirmationPagePath) {
        this.confirmationPagePath = confirmationPagePath;
    }

    public ActionResult doExecute(final HttpServletRequest req, final RenderContext renderContext, final Resource resource,
                                  JCRSessionWrapper session, final Map<String, List<String>> parameters, final URLResolver urlResolver) throws Exception {

        return JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", new JCRCallback<ActionResult>() {
            public ActionResult doInJCR(JCRSessionWrapper session) throws RepositoryException {
                String key = req.getParameter("key");
                String action = req.getParameter("exec");
                JCRNodeWrapper sub = subscriptionService.getSubscriptionFromKey(key, session);
                if (sub != null) {
                    if ("add".equals(action)) {
                        sub.setProperty("j:confirmed", true);
                        sub.getProperty("j:confirmationKey").remove();
                        session.save();
                        req.setAttribute("subscribed", Arrays.asList(resource.getNode()));
                        return new ActionResult(SC_OK, resource.getNode().getResolveSite().getPath() +
                                confirmationPagePath);
                    } else if ("rem".equals(action)) {
                        sub.remove();
                        session.save();
                        req.setAttribute("unsubscribed", Arrays.asList(resource.getNode()));
                        return new ActionResult(SC_OK, resource.getNode().getResolveSite().getPath() + confirmationPagePath);
                    }
                }

                 req.setAttribute("subscribed", Arrays.asList(resource.getNode()));
                        return new ActionResult(SC_OK, resource.getNode().getResolveSite().getPath() +
                                confirmationPagePath);
//                return new ActionResult(SC_BAD_REQUEST);
            }
        });
    }
}
