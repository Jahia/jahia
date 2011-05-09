/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.usermanager;

import org.jahia.services.content.JCRContentUtils;

import java.util.List;

/**
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 4/13/11
 */
public class DefaultJahiaUserSplittingRuleImpl implements JahiaUserSplittingRule {

    private String usersRootNode;

    private List<String> nonSplittedUsers;

    public void setUsersRootNode(String usersRootNode) {
        this.usersRootNode = usersRootNode;
    }

    public String getPathForUsername(String username) {
        StringBuilder builder = new StringBuilder();
        if (nonSplittedUsers.contains(username)) {
            return builder.append(usersRootNode).append("/").append(username).toString();
        }
        int userNameHashcode = Math.abs(username.hashCode());
        String firstFolder = getFolderName(userNameHashcode);
        userNameHashcode = Math.round(userNameHashcode/100);
        String secondFolder = getFolderName(userNameHashcode);
        userNameHashcode = Math.round(userNameHashcode/100);
        String thirdFolder = getFolderName(userNameHashcode);
        return builder.append(usersRootNode).append("/").append(firstFolder).append("/").append(secondFolder).append(
                "/").append(thirdFolder).append("/").append(JCRContentUtils.escapeLocalNodeName(
                username)).toString().toLowerCase();
    }

    private String getFolderName(int userNameHashcode) {
        int i = (userNameHashcode % 100);
        return Character.toString((char) ('a' + Math.round(i / 10)))+Character.toString((char)('a'+ (i%10)));
    }

    public void setNonSplittedUsers(List<String> nonSplittedUsers) {
        this.nonSplittedUsers = nonSplittedUsers;
    }
}
