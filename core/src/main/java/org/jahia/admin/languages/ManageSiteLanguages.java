/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.admin.languages;

import org.jahia.admin.AbstractAdministrationModule;
import org.jahia.bin.JahiaAdministration;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.CacheHelper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * <p>Title: Manage site languages</p>
 * <p>Description: Administration web user interface to manage the language
 * settings of a Jahia site.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class ManageSiteLanguages extends AbstractAdministrationModule {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ManageSiteLanguages.class);

    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

    private JahiaSite site;
    private JahiaUser user;
    private ServicesRegistry sReg;

    /**
     * @param request  Servlet request.
     * @param response Servlet response.
     * @author Alexandre Kraft
     */
    public void service(HttpServletRequest request, HttpServletResponse response) throws Exception {

        userRequestDispatcher(request, response, request.getSession());
    }

    /**
     * This method is used like a dispatcher for user requests.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  Servlet session for the current user.
     * @author Alexandre Kraft
     */
    private void userRequestDispatcher(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws Exception {
        String operation = request.getParameter("sub");

        sReg = ServicesRegistry.getInstance();

        // check if the user has really admin access to this site...
        user = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
        site = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);

        if (site != null && user != null && sReg != null) {

            // set the new site id to administer...
            request.setAttribute("site", site);

            if (operation.equals("display")) {
                displayLanguageList(request, response, session);
            } else if (operation.equals("commit")) {
                commitChanges(request, response, session);
            } else {
                displayLanguageList(request, response, session);
            }

        } else {
            String dspMsg = getMessage("message.generalError");
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "menu.jsp");
        }
    }

    private void displayLanguageList(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        request.setAttribute("mixLanguages", site.isMixLanguagesActive());
        Set<String> allLangs = new HashSet<String>(site.getLanguages());
        allLangs.addAll(site.getInactiveLanguages());
        request.setAttribute("languageSet", allLangs);
        request.setAttribute("inactiveLanguageSet", site.getInactiveLanguages());
        request.setAttribute("inactiveLiveLanguageSet", site.getInactiveLiveLanguages());
        request.setAttribute("mandatoryLanguageSet", site.getMandatoryLanguages());
        request.setAttribute("defaultLanguage", site.getDefaultLanguage());
        JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "manage_languages.jsp");
    }

    private void commitChanges(HttpServletRequest request, HttpServletResponse response, HttpSession session)

            throws IOException, ServletException {

        request.setAttribute("warningMsg", "");

        // first lets check if we have any operations to do on the list of
        // currently configured languages.
//            Map parameterMap = request.getParameterMap();

        // first let's check the language mix option
        String mixLanguages = request.getParameter("mixLanguages");
        boolean flushCache = mixLanguages == null && site.isMixLanguagesActive() ||
                mixLanguages != null && !site.isMixLanguagesActive();
        site.setMixLanguagesActive(mixLanguages != null);
        logger.debug("Setting language mix for site to "
                + (mixLanguages != null ? "active" : "disabled"));

        final String[] new_languages = request.getParameterValues("language_list");
        if (new_languages != null && new_languages.length > 0) {
            site.getLanguages().addAll(Arrays.asList(new_languages));
            flushCache = true;
        }
        
        final String[] activeLanguages = request.getParameterValues("activeLanguages");
        List<String> oldInactive = new LinkedList<String>(site.getInactiveLanguages());
        site.getInactiveLanguages().clear();
        site.getInactiveLanguages().addAll(Arrays.asList(request.getParameterValues("languages")));
        site.getInactiveLanguages().removeAll(Arrays.asList(activeLanguages));
        flushCache = flushCache || oldInactive.size() != site.getInactiveLanguages().size()
                || !oldInactive.containsAll(site.getInactiveLanguages());
        
        site.getLanguages().addAll(Arrays.asList(activeLanguages));
        site.getLanguages().removeAll(site.getInactiveLanguages());
        
        final String[] activeLiveLanguages = request.getParameterValues("activeLiveLanguages");
        List<String> oldInactiveLive = new LinkedList<String>(site.getInactiveLiveLanguages());
        site.getInactiveLiveLanguages().clear();
        site.getInactiveLiveLanguages().addAll(site.getLanguages());
        site.getInactiveLiveLanguages().removeAll(Arrays.asList(activeLiveLanguages));
        flushCache = flushCache || oldInactiveLive.size() != site.getInactiveLiveLanguages().size()
                || !oldInactiveLive.containsAll(site.getInactiveLiveLanguages());
        
        final String[] mandatoryLanguages = request.getParameterValues("mandatoryLanguages");
        final Set<String> mandatoryLanguagesSet = site.getMandatoryLanguages();
        mandatoryLanguagesSet.clear();
        if (mandatoryLanguages != null && mandatoryLanguages.length > 0) {
            mandatoryLanguagesSet.addAll(Arrays.asList(mandatoryLanguages));
        }
        site.setDefaultLanguage(request.getParameter("defaultLanguage"));
        try {
            JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
            service.updateSite(site);
            JahiaSite jahiaSite = service.getSiteByKey(JahiaSitesBaseService.SYSTEM_SITE_KEY);
            jahiaSite.getLanguages().addAll(site.getLanguages());
            service.updateSite(jahiaSite);
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        // finally the most complicated operation, let's delete the
        // languages. This is a two step process that requires a
        // confirmation by the user.
        if (request.getParameter("jahiaDisplayMessage") != null) {
            String dspMsg = getMessage("org.jahia.admin.JahiaDisplayMessage.changeCommitted.label");
            request.setAttribute("jahiaDisplayMessage", dspMsg);
        }
        if (flushCache) {
            CacheHelper.flushOutputCaches(true);
        }
        displayLanguageList(request, response, session);
    }

}