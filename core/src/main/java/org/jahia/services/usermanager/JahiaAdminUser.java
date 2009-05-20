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
 package org.jahia.services.usermanager;

import org.jahia.registries.ServicesRegistry;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 27 oct. 2005
 * Time: 15:21:14
 * To change this template use File | Settings | File Templates.
 */
public class JahiaAdminUser {

    public static Map adminUser = new HashMap();

    public static synchronized JahiaUser getAdminUser(int siteId){
        JahiaUser user = (JahiaUser)adminUser.get(new Integer(siteId));
        if ( user == null ){
            JahiaGroup adminGroup = ServicesRegistry.getInstance().getJahiaGroupManagerService()
                    .lookupGroup(siteId, JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
            Set members = adminGroup.getRecursiveUserMembers();
            if ( members.iterator().hasNext() ){
                user = (JahiaUser)members.iterator().next();
                adminUser.put(new Integer(siteId), user);
            }
        }
        return user;
    }


}
