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
package org.jahia.ajax.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.ajax.AjaxAction;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.JahiaSearchService;
import org.jahia.services.sites.JahiaSite;

/**
 * Ajax-based action handler for site indexing requests.
 * 
 * @author Sergiy Shyrkov
 */
public class IndexSiteAction extends AjaxAction {

    private static final transient Logger logger = Logger
            .getLogger(IndexSiteAction.class);

    private boolean doIndexSite(ProcessingContext ctx, int siteId)
            throws JahiaException {
        JahiaSearchService searchServ = ServicesRegistry.getInstance()
                .getJahiaSearchService();
        boolean result = searchServ.indexSite(siteId, ctx.getUser());
        searchServ.optimizeIndex(siteId);
        return result;
    }

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        long startTime = System.currentTimeMillis();

        setNoCacheHeaders(response);

        try {
            String id = getParameter(request, "id");
            int siteId = 0;
            if (StringUtils.isEmpty(id)) {
                throw new JahiaBadRequestException(
                        "Required parameter 'id' is missing.");
            }
            try {
                siteId = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                throw new JahiaBadRequestException("Illegal site ID value '"
                        + id + "'.", e);
            }
            JahiaSite site = ServicesRegistry.getInstance()
                    .getJahiaSitesService().getSite(siteId);
            if (site == null) {
                throw new JahiaBadRequestException(
                        "Unable to find site by ID '" + siteId + "'.");
            }

            ProcessingContext ctx = retrieveProcessingContext(request,
                    response, "/op/edit/pid/" + site.getHomePageID(), true);

            if (!ctx.getUser().isAdminMember(siteId)) {
                throw new JahiaForbiddenAccessException("User '"
                        + (ctx.getUser() != null ? ctx.getUser().getUsername()
                                : null)
                        + "' is not allowed to start site re-indexation"
                        + " for the site '" + site.getTitle() + "'");
            }

            doIndexSite(ctx, siteId);

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("IndexSiteAction.execute took: "
                    + (System.currentTimeMillis() - startTime));
        }
        return null;
    }

}
