package org.jahia.modules.googleAnalytics.actions;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.data.analytics.AccountFeed;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.utils.EncryptionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 2/8/11
 * Time: 5:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleAccountAction extends Action {

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        session.getWorkspace().getVersionManager().checkout(renderContext.getSite().getPath());
        session.getWorkspace().getVersionManager().checkout(resource.getNode().getPath());
        String action = req.getParameter("action");
        JCRNodeWrapper siteNode = session.getNode(renderContext.getSite().getPath());
        String login = req.getParameter("login");
        String password = req.getParameter("password");
        String webPropertyID = req.getParameter("webPropertyID");
        req.getSession().removeAttribute("addGoogleAccountError");
        req.getSession().removeAttribute("addWebPropertyError");
        if ("deleteWebPropertyID".equals(action)) {
            if (siteNode.isNodeType("jmix:googleAnalytics")) {
                //remove property
                if (siteNode.hasProperty("webPropertyID")) {
                    siteNode.getProperty("webPropertyID").remove();
                }
                siteNode.removeMixin("jmix:googleAnalytics");
            }
        } else if ("addWebPropertyID".equals(action)){
            if (webPropertyID != null && webPropertyID.trim().length() > 0) {
                siteNode.addMixin("jmix:googleAnalytics");
                siteNode.setProperty("webPropertyID", webPropertyID);
            } else {
                req.setAttribute("addWebPropertyError","addWebPropertyError.label");
            }
        } else if ("addGoogleAccount".equals(action)) {
            if (login != null && password != null && login.contains("@")) {
                AnalyticsService analyticsService = new AnalyticsService("gaExportAPI_acctSample_v1.0");
                try {
                    analyticsService.setUserCredentials(login,password);
                    JCRNodeWrapper gasNode = siteNode.getNode("googleAnalytics");
                    JCRNodeWrapper gaNode = gasNode.addNode(login.substring(0,login.indexOf("@")).replace(".",""),"jnt:googleAnalyticsAccount");
                    gaNode.setProperty("login",login);
                    gaNode.setProperty("password", EncryptionUtils.passwordBaseEncrypt(password));
                } catch (GoogleService.InvalidCredentialsException e) {
                    req.getSession().setAttribute("addGoogleAccountError","addGoogleAccount.label");
                }
            } else {
                req.getSession().setAttribute("addGoogleAccountError","addGoogleAccount.label");
            }
        }
        session.save();
        return new ActionResult(HttpServletResponse.SC_OK);
    }
}
