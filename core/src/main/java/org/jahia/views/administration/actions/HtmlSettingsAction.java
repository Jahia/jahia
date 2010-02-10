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
import java.security.Principal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
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
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.hibernate.model.JahiaAclEntryPK;
import org.jahia.hibernate.model.JahiaAclName;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.acl.ACLInfo;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Action handler for the HTML settings administration part.
 * 
 * @author Sergiy Shyrkov
 */
public class HtmlSettingsAction extends AdminAction {

    private static final Logger logger = Logger
            .getLogger(HtmlSettingsAction.class);

    private static ActionMessages validateMarkupTags(
            String markupFilteringTags, ProcessingContext ctx) {
        ActionMessages errors = null;
        String[] tags = StringUtils.split(markupFilteringTags.toLowerCase(),
                ", ;/<>");
        for (String tag : tags) {
            if (!StringUtils.isAlpha(tag)) {
                errors = new ActionMessages();
                errors
                        .add(
                                "markupFilteringTags",
                                new ActionMessage(
                                        JahiaResourceBundle.getJahiaInternalResource(
                                                        "org.jahia.admin.htmlsettings.markupFiltering.notValidMarkupName.warning",
                                                        ctx.getLocale()),
                                        false));
                break;
            }
        }
        return errors;
    }

    /**
     * Displays the main dialog for HTML settings.
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
        request.setAttribute("htmlCleanupEnabled", site.isHtmlCleanupEnabled());
        request.setAttribute("markupFilteringEnabled", site
                .isHtmlMarkupFilteringEnabled());
        request.setAttribute("markupFilteringTags", site
                .getHtmlMarkupFilteringTags());

        prepareToolbarData(site.getID(), request);

        return mapping.getInputForward();
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        JahiaData jData = (JahiaData) request
                .getAttribute("org.jahia.data.JahiaData");
        if (!jData.getProcessingContext().getUser().isPermitted("admin/html-editors-admin",
                        jData.getProcessingContext().getSiteKey())) {

            throw new JahiaForbiddenAccessException();
        }
        return super.execute(mapping, form, request, response);
    }

    private List<Principal> getToolbarPricipals(String toolbarName, int siteId,
            HttpServletRequest request) {
        List<Principal> members = null;
        String aclEntries = request.getParameter("toolbar" + toolbarName);
        if (aclEntries != null) {
            String[] entries = StringUtils.split(aclEntries, ",");
            if (entries.length > 0) {
                members = new LinkedList<Principal>();
            }
            for (String entry : entries) {
                String principalName = entry.substring(1);
                if (entry.charAt(0) == 'g') {
                    members.add(ServicesRegistry.getInstance()
                            .getJahiaGroupManagerService().lookupGroup(siteId, 
                                    principalName));
                } else {
                    members.add(ServicesRegistry.getInstance()
                            .getJahiaUserManagerService().lookupUser(
                                    principalName));
                }
            }
        }

        return members != null ? members : Collections.EMPTY_LIST;
    }

    private void prepareToolbarData(int siteId, HttpServletRequest request) {

//        request.setAttribute("toolbarFullAclId", aclService.getJahiaAclName(
//                "org.jahia.actions.sites." + siteId
//                        + ".htmlsettings.toolbar.Full", siteId).getAcl()
//                .getId());
//        request.setAttribute("toolbarBasicAclId", aclService.getJahiaAclName(
//                "org.jahia.actions.sites." + siteId
//                        + ".htmlsettings.toolbar.Basic", siteId).getAcl()
//                .getId());
//        request.setAttribute("toolbarLightAclId", aclService.getJahiaAclName(
//                "org.jahia.actions.sites." + siteId
//                        + ".htmlsettings.toolbar.Light", siteId).getAcl()
//                .getId());
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

        boolean htmlCleanup = request.getParameter("htmlCleanup") != null;
        boolean markupFiltering = request.getParameter("markupFiltering") != null;
        String markupFilteringTags = request
                .getParameter("markupFilteringTags");
        ActionMessages errors = validateMarkupTags(markupFilteringTags, ctx);
        if (errors != null) {
            saveErrors(request, errors);
            request.setAttribute("markupFilteringTags", markupFilteringTags);
        } else {

            ActionMessages messages = new ActionMessages();
            if (site.isHtmlCleanupEnabled() != htmlCleanup
                    || site.isHtmlMarkupFilteringEnabled() != markupFiltering
                    || !markupFilteringTags.equals(site
                            .getHtmlMarkupFilteringTags())) {
                site.setHtmlCleanupEnabled(htmlCleanup);
                site.setHtmlMarkupFilteringEnabled(markupFiltering);
                site.setHtmlMarkupFilteringTags(markupFilteringTags);
                markupFilteringTags = site.getHtmlMarkupFilteringTags();
                try {
                    ServicesRegistry.getInstance().getJahiaSitesService()
                            .updateSiteProperties(site, site.getSettings());
                } catch (JahiaException e) {
                    logger.error(e.getMessage(), e);
                    errors = new ActionMessages();
                    errors.add("htmlsettings", new ActionMessage(
                            e.getMessage(), false));
                    saveErrors(request, errors);
                }
            }
            if (errors == null) {
                updateToolbarPermissions("Full", site.getID(), request);
                updateToolbarPermissions("Basic", site.getID(), request);
                updateToolbarPermissions("Light", site.getID(), request);
                messages.add("default", new ActionMessage(JahiaResourceBundle
                        .getJahiaInternalResource(
                                "org.jahia.admin.warningMsg.changSaved.label",
                                ctx.getLocale()), false));
            }

            if (!messages.isEmpty()) {
                saveMessages(request, messages);
            }
        }

        request.setAttribute("htmlCleanupEnabled", htmlCleanup);
        request.setAttribute("markupFilteringEnabled", markupFiltering);
        request.setAttribute("markupFilteringTags", markupFilteringTags);
        prepareToolbarData(site.getID(), request);

        return mapping.getInputForward();
    }

    private void updateToolbarPermissions(String toolbarName, int siteId,
            HttpServletRequest request) {
        List<Principal> members = getToolbarPricipals(toolbarName, siteId,
                request);
//        JahiaACLManagerService aclService = ServicesRegistry.getInstance()
//                .getJahiaACLManagerService();
//        JahiaAclName aclName = aclService.getJahiaAclName(
//                "org.jahia.actions.sites." + siteId + ".htmlsettings.toolbar."
//                        + toolbarName, siteId);
//        aclName.getAcl().clearEntries(ACLInfo.GROUP_TYPE_ENTRY);
//        aclName.getAcl().clearEntries(ACLInfo.USER_TYPE_ENTRY);
//        for (Principal principal : members) {
//            if (principal instanceof JahiaGroup) {
//                JahiaGroup group = (JahiaGroup) principal;
//                JahiaAclEntry aclEntry = new JahiaAclEntry(new JahiaAclEntryPK(
//                        aclName.getAcl(), Integer
//                                .valueOf(ACLInfo.GROUP_TYPE_ENTRY), group
//                                .getGroupKey()), 0, 0);
//                aclEntry.setPermission(JahiaBaseACL.READ_RIGHTS,
//                        JahiaAclEntry.ACL_YES);
//                aclName.getAcl().setGroupEntry(group, aclEntry);
//            } else if (principal instanceof JahiaUser) {
//                JahiaUser user = (JahiaUser) principal;
//                JahiaAclEntry aclEntry = new JahiaAclEntry(new JahiaAclEntryPK(
//                        aclName.getAcl(), Integer
//                                .valueOf(ACLInfo.GROUP_TYPE_ENTRY), user
//                                .getUserKey()), 0, 0);
//                aclEntry.setPermission(JahiaBaseACL.READ_RIGHTS,
//                        JahiaAclEntry.ACL_YES);
//                aclName.getAcl().setUserEntry(user, aclEntry);
//            }
//        }
//        aclService.updateCache(aclName.getAcl());
    }
}
