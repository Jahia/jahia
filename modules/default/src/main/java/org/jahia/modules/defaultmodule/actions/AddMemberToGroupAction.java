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

package org.jahia.modules.defaultmodule.actions;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Render action for adding specified principal to a group.
 * @author loom
 * Date: 17.02.11
 * Time: 09:26
 */
public class AddMemberToGroupAction extends Action {

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(AddMemberToGroupAction.class);

    private JahiaGroupManagerService jahiaGroupManagerService;
    private JahiaUserManagerService jahiaUserManagerService;
    private JahiaSitesService jahiaSitesService;

    public void setJahiaGroupManagerService(JahiaGroupManagerService jahiaGroupManagerService) {
        this.jahiaGroupManagerService = jahiaGroupManagerService;
    }

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    public void setJahiaSitesService(JahiaSitesService jahiaSitesService) {
        this.jahiaSitesService = jahiaSitesService;
    }

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        String groupPath = resource.getNode().getPath();
        String[] splitGroupPath = groupPath.split("/");
        int siteID = 0;
        String groupName = null;
        // path to site group is something like /sites/siteKey/groups/groupName
        if (groupPath.startsWith("/sites")) {
            String siteKey = splitGroupPath[2];
            JahiaSite jahiaSite = jahiaSitesService.getSiteByKey(siteKey);
            siteID = jahiaSite.getID();
            groupName = splitGroupPath[4];
        } else {
            // path to general group is /groups/groupName
            groupName = splitGroupPath[2];
        }
        JahiaGroup targetJahiaGroup = jahiaGroupManagerService.lookupGroup(siteID, groupName);

        if (parameters.get("userKey") != null) {
            String userKey = parameters.get("userKey").get(0);
            JahiaUser jahiaUser = jahiaUserManagerService.lookupUserByKey(userKey);
            if (jahiaUser == null) {
                logger.warn("User " + userKey + " could not be found, will not add as member of group " + targetJahiaGroup.getGroupKey());
                return ActionResult.BAD_REQUEST;
            }
            if (!targetJahiaGroup.isMember(jahiaUser)) {
                targetJahiaGroup.addMember(jahiaUser);
            }
        } else if (parameters.get("groupKey") != null) {
            String groupKey = parameters.get("groupKey").get(0);
            JahiaGroup jahiaGroup = jahiaGroupManagerService.lookupGroup(groupKey);
            if (jahiaGroup == null) {
                logger.warn("Group " + groupKey + " could not be found, will not add as member of group " + targetJahiaGroup.getGroupKey());
                return ActionResult.BAD_REQUEST;
            }
            if (!targetJahiaGroup.isMember(jahiaGroup)) {
                targetJahiaGroup.addMember(jahiaGroup);
            }
        } else {
            return ActionResult.BAD_REQUEST;
        }
        return ActionResult.OK_JSON;
    }
}
