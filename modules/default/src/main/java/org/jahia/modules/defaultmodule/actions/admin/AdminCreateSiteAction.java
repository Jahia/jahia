package org.jahia.modules.defaultmodule.actions.admin;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.ActionResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.sites.*;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Url;
import org.jahia.utils.i18n.Messages;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Delete a site.
 */
public class AdminCreateSiteAction extends AdminAction {
    private static Logger logger = LoggerFactory.getLogger(AdminCreateSiteAction.class);

    protected JahiaSitesService sitesService;

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    @Override
    public String getMessage(Locale locale, String key) {
        String message = Messages.get("resources.JahiaServerSettings",key,locale);
        return StringUtils.isEmpty(message)?super.getMessage(locale, key):message;
    }

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRNodeWrapper node = resource.getNode();

        if (!node.isNodeType("jnt:virtualsitesFolder")  || !node.getPath().equals("/sites")) {
            return ActionResult.BAD_REQUEST;
        }

        logger.debug("started");

        // get form values...
        String siteTitle = StringUtils.left(StringUtils.defaultString(getParameter(parameters, "siteTitle")).trim(), 100);
        String siteServerName = StringUtils.left(StringUtils.defaultString(getParameter(parameters, "siteServerName")).trim(), 200);
        String siteKey = StringUtils.left(StringUtils.defaultString(getParameter(parameters, "siteKey")).trim(), 50);
        String siteDescr = StringUtils.left(StringUtils.defaultString(getParameter(parameters, "siteDescr")).trim(), 250);

        Map<String, String> result = new HashMap<String, String>();

        JahiaSite site = null;
        // create jahia site object if checks are in green light...
        try {
            // check validity...
            if (siteTitle != null && (siteTitle.length() > 0) && siteServerName != null &&
                    (siteServerName.length() > 0) && siteKey != null && (siteKey.length() > 0)) {
                if (!sitesService.isSiteKeyValid(siteKey)) {
                    result.put("warn", getMessage(renderContext.getUILocale(), "serverSettings.manageWebProjects.warningMsg.onlyLettersDigitsUnderscore"));
                    return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
                } else if (siteKey.equals("site")) {
                    result.put("warn", getMessage(renderContext.getUILocale(), "serverSettings.manageWebProjects.warningMsg.chooseAnotherSiteKey"));
                    return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
                } else if (!sitesService.isServerNameValid(siteServerName)) {
                    result.put("warn", getMessage(renderContext.getUILocale(), "serverSettings.manageWebProjects.warningMsg.invalidServerName"));
                    return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
                } else if (siteServerName.equals("default")) {
                    result.put("warn", getMessage(renderContext.getUILocale(), "serverSettings.manageWebProjects.warningMsg.chooseAnotherServerName"));
                    return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
                } else if (!Url.isLocalhost(siteServerName) && sitesService.getSite(siteServerName) != null) {
                    result.put("warn", getMessage(renderContext.getUILocale(), "serverSettings.manageWebProjects.warningMsg.chooseAnotherServerName"));
                    return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
                } else if (sitesService.getSiteByKey(siteKey) != null) {
                    result.put("warn", getMessage(renderContext.getUILocale(), "serverSettings.manageWebProjects.warningMsg.chooseAnotherSiteKey"));
                    return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
                }
            } else {
                result.put("warn", getMessage(renderContext.getUILocale(), "serverSettings.manageWebProjects.warningMsg.completeRequestInfo"));
                return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
            }

            Locale selectedLocale = resource.getLocale();
            String lang = getParameter(parameters, "language");
            if (lang != null) {
                selectedLocale = LanguageCodeConverters.getLocaleFromCode(lang);
            }

            // add the site in siteManager...
            site = sitesService.addSite(session.getUser(), siteTitle, siteServerName, siteKey, siteDescr,
                    selectedLocale, getParameter(parameters,"templatesSet"),
                    null,null, null,null, false, null, null);

            if (getParameter(parameters, "mixLanguage", "false").equals("true") || getParameter(parameters, "allowsUnlistedLanguages", "false").equals("true")) {
                site.setMixLanguagesActive(getParameter(parameters, "mixLanguage", "false").equals("true"));
                site.setAllowsUnlistedLanguages(getParameter(parameters, "allowsUnlistedLanguages", "false").equals("true"));
                sitesService.updateSystemSitePermissions(site);
            }

            if (site != null) {
                JahiaSite systemSite = sitesService.getSiteByKey(JahiaSitesService.SYSTEM_SITE_KEY);
                // update the system site only if it does not yet contain at least one of the site languages
                if (!systemSite.getLanguages().containsAll(site.getLanguages())) {
                    systemSite.getLanguages().addAll(site.getLanguages());
                    sitesService.updateSystemSitePermissions(systemSite);
                }
            } else {
                result.put("warn", getMessage(renderContext.getUILocale(), "label.error.processingRequestError"));
                return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
            }
        } catch (JahiaException ex) {
            try {
                if (site != null) {
                    sitesService.removeSite(site);
                }
            } catch (Exception t) {
                logger.error("Error while cleaning site", t);
            }

            logger.error("Error while adding site", ex);

            result.put("warn", getMessage(renderContext.getUILocale(), "label.error.processingRequestError"));
            return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
        }

        return ActionResult.OK_JSON;
    }


}
