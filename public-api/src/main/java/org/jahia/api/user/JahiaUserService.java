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

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 12 nov. 2007
 * Time: 12:16:35
 * To change this template use File | Settings | File Templates.
 */
public interface JahiaUserService {

    public boolean checkPassword(String username, String password);

    public boolean isServerAdmin(String username);

    public boolean isAdmin(String username, String site);

    public boolean isUserMemberOf(String username, String groupname, String site);

    public List getUserMembership(String username);

    public List getGroupMembers(String groupname);

}
