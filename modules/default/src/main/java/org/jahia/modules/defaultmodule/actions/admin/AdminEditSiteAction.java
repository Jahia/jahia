/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.modules.defaultmodule.actions.admin;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.ActionResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.sites.JahiaSite;
import org.jahia.utils.Url;
import org.jahia.utils.i18n.Messages;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Edit site properties
 */
public class AdminEditSiteAction extends AdminSiteAction {
    private static Logger logger = LoggerFactory.getLogger(AdminEditSiteAction.class);

    @Override
    public String getMessage(Locale locale, String key) {
        String message = Messages.get("resources.JahiaServerSettings", key, locale);
        return StringUtils.isEmpty(message)?super.getMessage(locale, key):message;
    }


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
            session.checkout((Node) site);
            // check validity...
            if (siteTitle != null && (siteTitle.trim().length() > 0) && siteServerName != null &&
                    (siteServerName.trim().length() > 0)) {
                if (!isServerNameValid(siteServerName)) {
                    result.put("warn", getMessage(renderContext.getUILocale(), "serverSettings.manageWebProjects.warningMsg.invalidServerName"));
                    return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
                } else if (!site.getServerName().equals(siteServerName)) {
                    if (!Url.isLocalhost(siteServerName) && sitesService.getSite(siteServerName) != null) {
                        result.put("warn", getMessage(renderContext.getUILocale(), "serverSettings.manageWebProjects.warningMsg.chooseAnotherServerName"));
                        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
                    }
                }
            } else {
                result.put("warn", getMessage(renderContext.getUILocale(), "serverSettings.manageWebProjects.warningMsg.completeRequestInfo"));
                return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(result));
            }

            // save modified informations...
            site.setTitle(siteTitle);
            site.setServerName(siteServerName);
            site.setDescription(siteDescr);

            sitesService.updateSystemSitePermissions(site);

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
            session.save();
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
