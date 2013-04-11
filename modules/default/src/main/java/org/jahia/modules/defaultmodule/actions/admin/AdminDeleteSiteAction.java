package org.jahia.modules.defaultmodule.actions.admin;

import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.sites.JahiaSite;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Delete a site.
 */
public class AdminDeleteSiteAction extends AdminSiteAction {

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, JahiaSite site, JCRSessionWrapper session, Map<String, List<String>> parameters) throws Exception {
        // now let's check if this site is the default site, in which case
        // we need to change the default site to another one.
        JahiaSite defSite = sitesService.getDefaultSite();

        // first let's build a list of the all the sites except the
        // current one.
        List<JCRSiteNode> otherSites = new ArrayList<JCRSiteNode>();
        for (JCRSiteNode curSite: sitesService.getSitesNodeList()) {
            if (!curSite.getSiteKey().equals(site.getSiteKey())) {
                otherSites.add(curSite);
            }
        }
        if (defSite == null) {
            // no default site, let's assign once that isn't the current
            // one being deleted.
            if (otherSites.size() > 0) {
                sitesService.setDefaultSite(sitesService.getSite(otherSites.get(0).getName()));
            }
        } else if (defSite.getSiteKey().equals(site.getSiteKey())) {
            // the default site IS the site being deleted, let's set
            // another site as a default site.
            if (otherSites.size() > 0) {
                sitesService.setDefaultSite(sitesService.getSite(otherSites.get(0).getName()));
            } else {
                sitesService.setDefaultSite(null);
            }
        }

        // switch staging and versioning to false.
        sitesService.updateSite(site);

        //remove site definition
        sitesService.removeSite(site);

        return ActionResult.OK_JSON;
    }


}
