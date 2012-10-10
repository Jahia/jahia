package org.jahia.modules.defaultmodule.actions.admin;

import org.jahia.admin.sites.ManageSites;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Base class for site admin action
 */
public abstract class AdminSiteAction extends AdminAction {

    protected JahiaSitesService sitesService;

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        List<String> sites = parameters.get("sitebox");
        if (sites == null) {
            JCRNodeWrapper node = resource.getNode();

            if (!node.isNodeType("jnt:virtualsite")  || !node.getParent().getPath().equals("/sites")) {
                return ActionResult.BAD_REQUEST;
            }

            JahiaSite site = sitesService.getSiteByKey(node.getName());

            return doExecute(req,renderContext, site,session, parameters);
        } else {
            JCRNodeWrapper node = resource.getNode();

            if (!node.isNodeType("jnt:virtualsitesFolder")  || !node.getPath().equals("/sites")) {
                return ActionResult.BAD_REQUEST;
            }

            for (String site : sites) {
                doExecute(req,renderContext, sitesService.getSiteByKey(site), session, parameters);
            }
        }
        return ActionResult.OK_JSON;
    }

    public abstract ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, JahiaSite site, JCRSessionWrapper session, Map<String, List<String>> parameters) throws Exception;
}
