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
package org.jahia.ajax.gwt.linkchecker.server;

import org.jahia.admin.AdministrationModule;
import org.jahia.ajax.gwt.commons.server.AbstractJahiaGWTServiceImpl;
import org.jahia.ajax.gwt.client.service.linkchecker.LinkCheckerService;
import org.jahia.ajax.gwt.client.data.linkchecker.GWTJahiaCheckedLink;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.acl.JahiaBaseACL;

import java.util.List;

/**
 * User: romain
 * Date: 11 juin 2009
 * Time: 15:09:20
 */
public class LinkCheckerServiceImpl extends AbstractJahiaGWTServiceImpl implements LinkCheckerService {

    public Boolean checkLinks() {
        ProcessingContext ctx = retrieveParamBean();
        if (ctx.getUser().isAdminMember(ctx.getSiteID()) || hasAccess(ctx.getUser(), ctx.getSiteID())) {
            LinkChecker.getInstance().startCheckingLinks(ctx.getSiteID());
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    private boolean hasAccess(JahiaUser user, int siteID) {
        return ServicesRegistry.getInstance().getJahiaACLManagerService().getSiteActionPermission(
                ((AdministrationModule)SpringContextSingleton.getBean("linkCheckerAdministrationModule")).getPermissionName(),
                user,
                JahiaBaseACL.READ_RIGHTS,
                siteID) > 0;
    }

    /**
     * This methods retrieve the content of the checked links list. If it returns null, this means the process is over.
     *
     * @return the checked links or null if it is over
     */
    public List<GWTJahiaCheckedLink> lookForCheckedLinks() {
        LinkChecker checker = LinkChecker.getInstance();
        List<GWTJahiaCheckedLink> links = checker.getLinks();
        return !links.isEmpty() || checker.getStatus().isActive() ? links : null;
    }

    public void stopCheckingLinks() {
        LinkChecker.getInstance().stopCheckingLinks();
    }
    
}
