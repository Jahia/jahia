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

package org.jahia.admin.htmlsettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jahia.admin.JspForwardAdministrationModule;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.services.sites.SitesSettings;

/**
 * Administration module for managing HTML settings.
 * 
 * @author Sergiy Shyrkov
 */
public class ManageHtmlSettings extends JspForwardAdministrationModule {
    

    private JahiaSitesBaseService sitesService;

    private void doDisplay(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        JahiaSite site = getSite(request);
        
            request.setAttribute("wcagCompliance", site.isWCAGComplianceCheckEnabled());

            request.setAttribute("doTagFiltering", site.isHtmlMarkupFilteringEnabled());

            request.setAttribute("filteredTags", site.getHtmlMarkupFilteringTags());

        super.service(request, response);
    }

    private void doUpdate(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        request.setAttribute("jahiaDisplayInfo", getMessage("label.changeSaved"));

        JahiaSite site = getSite(request);
        site.getSettings().put(SitesSettings.WCAG_COMPLIANCE_CHECKING_ENABLED, Boolean.valueOf(request.getParameter("wcagCompliance")));
        site.getSettings().put(SitesSettings.HTML_MARKUP_FILTERING_ENABLED, Boolean.valueOf(request.getParameter("doTagFiltering")));
        site.getSettings().put(SitesSettings.HTML_MARKUP_FILTERING_TAGS, request.getParameter("filteredTags"));

        sitesService.updateSite(site);
        
        doDisplay(request, response);
    }

    private JahiaSite getSite(HttpServletRequest request) throws JahiaException {
        JahiaSite site = (JahiaSite) request.getSession().getAttribute(ProcessingContext.SESSION_SITE);
        if (site == null) {
            site = sitesService.getSites().next();
        }
        
        sitesService.invalidateCache(site);
        
        return sitesService.getSite(site.getID());
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String operation = request.getParameter("sub");
        if (StringUtils.equals("update", operation)) {
            doUpdate(request, response);
        } else {
            doDisplay(request, response);
        }

    }

    public void setSitesService(JahiaSitesBaseService sitesService) {
        this.sitesService = sitesService;
    }

}
