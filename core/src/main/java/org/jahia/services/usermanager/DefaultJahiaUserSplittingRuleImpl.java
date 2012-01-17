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
        String firstFolder = getFolderName(userNameHashcode).toLowerCase();
        userNameHashcode = Math.round(userNameHashcode/100);
        String secondFolder = getFolderName(userNameHashcode).toLowerCase();
        userNameHashcode = Math.round(userNameHashcode/100);
        String thirdFolder = getFolderName(userNameHashcode).toLowerCase();
        return builder.append(usersRootNode).append("/").append(firstFolder).append("/").append(secondFolder).append(
                "/").append(thirdFolder).append("/").append(JCRContentUtils.escapeLocalNodeName(
                username)).toString();
    }

    private String getFolderName(int userNameHashcode) {
        int i = (userNameHashcode % 100);
        return Character.toString((char) ('a' + Math.round(i / 10)))+Character.toString((char)('a'+ (i%10)));
    }

    public void setNonSplittedUsers(List<String> nonSplittedUsers) {
        this.nonSplittedUsers = nonSplittedUsers;
    }
}
