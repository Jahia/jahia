/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
