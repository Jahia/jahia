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

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 5, 2008
 * Time: 2:53:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaGroup extends BaseModelData implements Serializable {
    public GWTJahiaGroup() {
    }

    public GWTJahiaGroup(String groupName, String groupKey) {
        setGroupname(groupName);
        setGroupKey(groupKey) ;
    }

    public String getGroupname() {
        return get("groupname") ;
    }

    public void setGroupname(String username) {
        set("groupname", username) ;
    }

    public String getGroupKey() {
        return get("groupKey") ;
    }

    public void setGroupKey(String userKey) {
        set("groupKey", userKey) ;
    }

    public Collection<String> getMembers() {
        return get("members") ;
    }

    public void setMembers(Collection<String> members) {
        set("members", members) ;
    }

    public String getDisplayMembers() {
        return get("displaymembers") ;
    }

    public void setDisplayMembers(String displaymembers) {
        set("displaymembers", displaymembers) ;
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
