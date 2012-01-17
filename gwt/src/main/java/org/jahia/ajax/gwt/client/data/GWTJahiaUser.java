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

package org.jahia.ajax.gwt.client.data;

import java.io.Serializable;

/**
 *
 *
 * User: hollis
 * Date: 7 ao�t 2008 - 15:54:17
 */
public class GWTJahiaUser extends GWTJahiaValueDisplayBean implements GWTJahiaPrincipal {
    
    public GWTJahiaUser() {
        super();
        setAllowNestedValues(false);
    }

    public GWTJahiaUser(String username, String userKey, String displayableName) {
        super(userKey,displayableName);
        setAllowNestedValues(false);
        setUsername(username);
        setUserKey(userKey);
    }

    public String getUsername() {
        return get("userName") ;
    }

    public String getName() {
        return get("name") ;
    }

    public void setUsername(String username) {
        set("userName", username) ;
        set("name", username) ;
    }

    public String getUserKey() {
        return get("userKey") ;
    }

    public String getKey() {
        return get("userKey") ;
    }

    public void setUserKey(String userKey) {
        set("userKey", userKey) ;
        set("key", userKey) ;
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GWTJahiaUser && getUserKey().equals(((GWTJahiaUser)obj).getUserKey());
    }
}
