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
package org.jahia.api.user;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 14 nov. 2007
 * Time: 18:35:35
 * To change this template use File | Settings | File Templates.
 */
public class JahiaUserServiceProxy implements JahiaUserService, Serializable {
    private JahiaUserService service;

    public void setService(JahiaUserService service) {
        this.service = service;
    }

    public boolean checkPassword(String username, String password) {
        return service.checkPassword(username, password);
    }

    public boolean isServerAdmin(String username) {
        return service.isServerAdmin(username);
    }

    public boolean isAdmin(String username, String site) {
        return service.isAdmin(username, site);
    }

    public boolean isUserMemberOf(String username, String groupname, String site) {
        return service.isUserMemberOf(username, groupname, site);
    }

    public List getUserMembership(String username) {
        return service.getUserMembership(username);
    }

    public List getGroupMembers(String groupname) {
        return service.getGroupMembers(groupname);
    }
}
