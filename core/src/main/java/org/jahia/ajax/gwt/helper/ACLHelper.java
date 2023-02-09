/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.helper;

import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.usermanager.JahiaGroupManagerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: toto
 * Date: Nov 26, 2008 - 7:40:56 PM
 */
public class ACLHelper {

    private JahiaGroupManagerService jahiaGroupManagerService;

    public void setJahiaGroupManagerService(JahiaGroupManagerService jahiaGroupManagerService) {
        this.jahiaGroupManagerService = jahiaGroupManagerService;
    }

    public GWTJahiaNodeACE createUsersGroupACE(List<String> permissions, boolean grand, JCRSiteNode site) {
        JCRGroupNode usersGroup = jahiaGroupManagerService.lookupGroup(site.getSiteKey(), JahiaGroupManagerService.USERS_GROUPNAME);
        GWTJahiaNodeACE ace = new GWTJahiaNodeACE();
        ace.setPrincipalType('g');
        ace.setPrincipal(usersGroup.getName());
        ace.setPrincipalKey(usersGroup.getPath());
        Map<String, Boolean> permissionsMap = new HashMap<String, Boolean>();
        for (String perm : permissions) {
            if (grand) {
                permissionsMap.put(perm, true);
            } else {
                permissionsMap.put(perm, false);
            }
        }
        ace.setRoles(permissionsMap);
        ace.setInheritedRoles(new HashMap<String, Boolean>());
        ace.setInherited(false);
        return ace;
    }


}
