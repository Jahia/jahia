/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.usermanager;

import org.jahia.services.content.JCRContentUtils;

import java.util.List;

/**
 * @author rincevent
 * @since JAHIA 6.5
 */
public class DefaultJahiaUserSplittingRuleImpl implements JahiaUserSplittingRule {

    private static final int NUMBER_OF_SEGMENTS = 3;

    private static int round(float a) {
        // we use here the same code for Math.round() as it was in Java 6/7, because in the Java 8 the implementation has changed with the
        // side-effect of producing different results in some cases.
        return (int) Math.floor(a + 0.5f);
    }
    
    private String usersRootNode;

    private List<String> nonSplittedUsers;

    public void setUsersRootNode(String usersRootNode) {
        this.usersRootNode = usersRootNode;
    }

    public String getPathForUsername(String username) {
        return getPathForUsername(username, true);
    }

    public String getRelativePathForUsername(String username) {
        return getPathForUsername(username, false);
    }

    private String getPathForUsername(String username, boolean addUsersRootNode) {
        StringBuilder builder = new StringBuilder();
        if (addUsersRootNode) {
            builder.append(usersRootNode);
        }
        if (nonSplittedUsers.contains(username)) {
            return builder.append("/").append(username).toString();
        }
        int userNameHashcode = Math.abs(username.hashCode());
        String firstFolder = getFolderName(userNameHashcode).toLowerCase();
        // Warning : The useless call to Math.round() converts int to float and back to int but changes the value,
        // due to float low precision - removing this call would change path of all users !
        userNameHashcode = round(userNameHashcode/100);
        String secondFolder = getFolderName(userNameHashcode).toLowerCase();
        userNameHashcode = round(userNameHashcode/100);
        String thirdFolder = getFolderName(userNameHashcode).toLowerCase();
        return builder.append("/").append(firstFolder).append("/").append(secondFolder).append(
                "/").append(thirdFolder).append("/").append(JCRContentUtils.escapeLocalNodeName(
                username)).toString();
    }

    private String getFolderName(int userNameHashcode) {
        // Additional Math.abs just in case of userNameHashcode==Integer.MIN_VALUE
        int i = Math.abs(userNameHashcode % 100);
        return Character.toString((char) ('a' + round(i / 10)))+Character.toString((char)('a'+ (i%10)));
    }
    public int getNumberOfSegments() {
        return NUMBER_OF_SEGMENTS;
    }

    public void setNonSplittedUsers(List<String> nonSplittedUsers) {
        this.nonSplittedUsers = nonSplittedUsers;
    }
    
}
