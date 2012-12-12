package org.jahia.modules.defaultmodule.actions.admin;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.ActionResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.sites.JahiaSite;
import org.jahia.utils.Url;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Edit site properties
 */
public class AdminEditSiteAction extends AdminSiteAction {
    private static Logger logger = LoggerFactory.getLogger(AdminEditSiteAction.class);

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, JahiaSite site, JCRSessionWrapper session, Map<String, List<String>> parameters) throws Exception {
        logger.debug(" process edit site started ");

        // get form values...
        String siteTitle = StringUtils.left(StringUtils.defaultString(getParameter(parameters, "siteTitle")).trim(), 100);
        String siteServerName = StringUtils.left(StringUtils.defaultString(getParameter(parameters, "siteServerName")).trim(), 200);
        String siteDescr = StringUtils.left(StringUtils.defaultString(getParameter(parameters, "siteDescr")).trim(), 250);

        boolean defaultSite = (getParameter(parameters, "defaultSite") != null);

        Map<String, String> result = new HashMap<String, String>();

        try {
            // check validity...
            if (siteTitle != null && (siteTitle.trim().length() > 0) && siteServerName != null &&
                    (siteServerName.trim().length() > 0)) {
                if (!isServerNameValid(siteServerName)) {
                    result.put("warn", getMessage(renderContext.getUILocale(),"org.jahia.admin.warningMsg.invalidServerName.label"));
                    return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
                } else if (!site.getServerName().equals(siteServerName)) {
                    if (!Url.isLocalhost(siteServerName) && sitesService.getSite(siteServerName) != null) {
                        result.put("warn", getMessage(renderContext.getUILocale(),"org.jahia.admin.warningMsg.chooseAnotherServerName.label"));
                        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
                    }
                }
            } else {
                result.put("warn", getMessage(renderContext.getUILocale(),"org.jahia.admin.warningMsg.completeRequestInfo.label"));
                return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
            }

            // save modified informations...
            site.setTitle(siteTitle);
            site.setServerName(siteServerName);
            site.setDescr(siteDescr);

            sitesService.updateSite(site);

            JahiaSite defSite = sitesService.getDefaultSite();
            if (defaultSite) {
                if (defSite == null) {
                    sitesService.setDefaultSite(site);
                } else if (!defSite.getSiteKey().equals(site.getSiteKey())) {
                    sitesService.setDefaultSite(site);
                }
            } else {
                if (defSite != null && defSite.getSiteKey().equals(site.getSiteKey())) {
                    sitesService.setDefaultSite(null);
                }
            }

            // redirect...
            return ActionResult.OK_JSON;
        } catch (JahiaException ex) {
            logger.warn("Error while processing site edition", ex);
            result.put("warn", getMessage(renderContext.getUILocale(),"label.error.processingRequestError"));
            return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
        }
    }

    public boolean isServerNameValid(String serverName) {
        return StringUtils.isNotEmpty(serverName) && !serverName.contains(" ") && !serverName.contains(":");
    }


}
