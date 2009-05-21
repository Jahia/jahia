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
