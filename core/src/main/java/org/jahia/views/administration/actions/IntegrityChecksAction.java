/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.views.administration.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.jahia.bin.AdminAction;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.sites.JahiaSite;

/**
 * Action handler for the Integrity Checks administration part.
 * 
 * @author Sergiy Shyrkov
 */
public class IntegrityChecksAction extends AdminAction {

    private static final Logger logger = Logger
            .getLogger(IntegrityChecksAction.class);

    /**
     * Displays the main dialog for integrity checks.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return the action forward
     * @throws IOException
     * @throws ServletException
     */
    public ActionForward display(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        JahiaSite site = (JahiaSite) request.getSession(true).getAttribute(
                ProcessingContext.SESSION_SITE);
        request.setAttribute("urlIntegrityCheckEnabled", site
                .isURLIntegrityCheckEnabled());
        request.setAttribute("waiComplianceCheckEnabled", site
                .isWAIComplianceCheckEnabled());
        request.setAttribute("fileLockingEnabled", site
                .isFileLockOnPublicationEnabled());


        return mapping.getInputForward();
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        JahiaData jData = (JahiaData) request
                .getAttribute("org.jahia.data.JahiaData");
        if (ServicesRegistry.getInstance().getJahiaACLManagerService()
                .getSiteActionPermission("admin.htmleditors.HtmlEditorsAdmin",
                        jData.getProcessingContext().getUser(),
                        JahiaBaseACL.READ_RIGHTS,
                        jData.getProcessingContext().getSiteID()) <= 0) {

            throw new JahiaForbiddenAccessException();
        }
        return super.execute(mapping, form, request, response);
    }

    /**
     * Saves the HTML settings.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return the action forward
     * @throws IOException
     * @throws ServletException
     */
    public ActionForward save(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        JahiaSite site = (JahiaSite) request.getSession(true).getAttribute(
                ProcessingContext.SESSION_SITE);
        ProcessingContext ctx = ((JahiaData) request
                .getAttribute("org.jahia.data.JahiaData"))
                .getProcessingContext();

        boolean urlIntegrityCheckEnabled = request
                .getParameter("urlIntegrityCheckEnabled") != null;
        boolean waiComplianceCheckEnabled = request
                .getParameter("waiComplianceCheckEnabled") != null;
        boolean fileLockingEnabled = request
        .getParameter("fileLockingEnabled") != null;
        if (site.isURLIntegrityCheckEnabled() != urlIntegrityCheckEnabled
                || site.isWAIComplianceCheckEnabled() != waiComplianceCheckEnabled || site.isFileLockOnPublicationEnabled() != fileLockingEnabled) {
            site.setURLIntegrityCheckEnabled(urlIntegrityCheckEnabled);
            site.setWAIComplianceCheckEnabled(waiComplianceCheckEnabled);
            site.setFileLockOnPublicationEnabled(fileLockingEnabled);
            try {
                ServicesRegistry.getInstance().getJahiaSitesService()
                        .updateSiteProperties(site, site.getSettings());
                ActionMessages messages = new ActionMessages();
                messages.add("default", new ActionMessage(JahiaResourceBundle.getJahiaInternalResource(
                                "org.jahia.admin.warningMsg.changSaved.label",
                                 ctx.getLocale()), false));
                saveMessages(request, messages);
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
                ActionMessages errors = new ActionMessages();
                errors.add("htmlsettings", new ActionMessage(e.getMessage(),
                        false));
                saveErrors(request, errors);
            }
        }

        request.setAttribute("urlIntegrityCheckEnabled",
                urlIntegrityCheckEnabled);
        request.setAttribute("waiComplianceCheckEnabled",
                waiComplianceCheckEnabled);
        request.setAttribute("fileLockingEnabled", fileLockingEnabled);

        return mapping.getInputForward();
    }
}
