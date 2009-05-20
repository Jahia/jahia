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
