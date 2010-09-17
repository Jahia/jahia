/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: toto
 * Date: Nov 26, 2008 - 7:40:56 PM
 */
public class ACLHelper {
    private final static Logger logger = Logger.getLogger(ACLHelper.class);

    private JahiaGroupManagerService jahiaGroupManagerService;
    private JahiaUserManagerService jahiaUserManagerService;

    public void setJahiaGroupManagerService(JahiaGroupManagerService jahiaGroupManagerService) {
        this.jahiaGroupManagerService = jahiaGroupManagerService;
    }

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    public GWTJahiaNodeACE createUsersGroupACE(List<String> permissions, boolean grand, JCRSiteNode site) {
        JahiaGroup usersGroup = jahiaGroupManagerService.lookupGroup(site.getID(), JahiaGroupManagerService.USERS_GROUPNAME);
        GWTJahiaNodeACE ace = new GWTJahiaNodeACE();
        ace.setPrincipalType('g');
        ace.setPrincipal(usersGroup.getGroupname());
        ace.setPrincipalKey(usersGroup.getGroupKey());
        Map<String, String> permissionsMap = new HashMap<String, String>();
        for (String perm : permissions) {
            if (grand) {
                permissionsMap.put(perm, "GRANT");
            } else {
                permissionsMap.put(perm, "DENY");
            }
        }
        ace.setPermissions(permissionsMap);
        ace.setInheritedPermissions(new HashMap<String,String>());
        ace.setInherited(false);
        return ace;
    }


}
