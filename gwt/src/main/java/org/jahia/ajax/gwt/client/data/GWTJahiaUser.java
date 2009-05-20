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
package org.jahia.ajax.gwt.client.data;

import java.io.Serializable;

/**
 *
 *
 * User: hollis
 * Date: 7 ao�t 2008 - 15:54:17
 */
public class GWTJahiaUser extends GWTJahiaValueDisplayBean implements Serializable {
    
    public GWTJahiaUser() {
        super();
    }

    public GWTJahiaUser(String username, String userKey) {
        super(userKey,username);
        setUsername(username);
        setUserKey(userKey);
    }

    public String getUsername() {
        return get("userName") ;
    }

    public void setUsername(String username) {
        set("userName", username) ;
    }

    public String getUserKey() {
        return get("userKey") ;
    }

    public void setUserKey(String userKey) {
        set("userKey", userKey) ;
    }

    public void setSiteName (String serverName) {
        set("siteName",serverName);
    }

    public String getSiteName(){
        return get("siteName");
    }

    public void setSiteId (Integer siteId) {
        set("siteId",siteId);
    }

    public Integer getSiteId(){
        return get("siteId");
    }

    public void setProvider (String provider) {
        set("provider",provider);
    }

    public String getProvider(){
        return get("provider");
    }
}
