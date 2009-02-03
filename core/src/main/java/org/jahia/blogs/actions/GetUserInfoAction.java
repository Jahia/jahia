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

package org.jahia.blogs.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jahia.blogs.model.UserInfo;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Action used to get a user's information from the Jahia content repository.
 * Compliant with Blogger API's getUserInfo method.
 *
 * @author Xavier Lawrence
 */
public class GetUserInfoAction extends AbstractAction {
    
    // log4j logger
    static Logger log = Logger.getLogger(GetUserInfoAction.class);
    
    /** Creates a new instance of GetUserInfoAction */
    public GetUserInfoAction(String appKey, String userName, String password) {
        super.appKey = appKey;
        super.userName = userName;
        super.password = password;
    }

    /**
     * Authenticates a user and returns basic user info (nickName, firstName,
     * lastName, url and email).
     *
     * @return A Map containing the user information
     */
    public Object execute() throws JahiaException {
        
        // Create commmon resources
        super.init();
        
        // First check that the user is registered to this site.
        JahiaUser user = super.checkLogin();
        
        if (user == null) {
            throw new JahiaException("User: "+userName+
                    " does not exist", "User: "+userName+ " does not exist",
                    JahiaException.ENTRY_NOT_FOUND,
                    JahiaException.WARNING_SEVERITY);
        }
        
        Map userInfo = new HashMap(6);
        
        userInfo.put(UserInfo.USER_ID, user.getUsername());
        userInfo.put(UserInfo.NICKNAME, user.getUsername());
        userInfo.put(UserInfo.URL, "");
        userInfo.put(UserInfo.EMAIL, user.getUserProperty("email").getValue());
        userInfo.put(UserInfo.LASTNAME, user.getUserProperty("lastname").getValue());
        userInfo.put(UserInfo.FIRSTNAME, user.getUserProperty("firstname").getValue());
        
        log.debug("Returning userInfo: "+userInfo);
        return userInfo;
    }
}
