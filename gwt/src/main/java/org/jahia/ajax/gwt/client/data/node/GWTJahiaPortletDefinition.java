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
package org.jahia.ajax.gwt.client.data.node;

import com.extjs.gxt.ui.client.data.BaseModel;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 28, 2008
 * Time: 6:14:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaPortletDefinition extends BaseModel {

    private GWTJahiaNodeACL baseAcl;

    public GWTJahiaPortletDefinition() {
    }

    public GWTJahiaPortletDefinition(String appId, String name,String displayName, String portletType, GWTJahiaNodeACL baseAcl, String description, Integer exp, String cacheScope) {
        setContextName(appId);
        setDefinitionName(name);
        setDisplayName(displayName);
        setPortletType(portletType);
        setBaseAcl(baseAcl);
        setDescription(description);
        setExpirationTime(exp);
        setCacheScope(cacheScope);
    }

    public String getContextName() {
        return get("contextName");
    }

    public void setContextName(String appId) {
        set("contextName", appId);
    }

    public String getDefinitionName() {
        return get("name");
    }

    public void setDefinitionName(String name) {
        set("name", name);
    }

    public String getDisplayName() {
        return get("displayName");
    }

    public void setDisplayName(String name) {
        set("displayName", name);
    }

    public Integer getExpirationTime() {
        return get("expirationTime");
    }

    public void setExpirationTime(Integer expirationTime) {
        set("expirationTime", expirationTime);
    }

    public String getCacheScope() {
        return get("cacheScope");
    }

    public void setCacheScope(String cacheScope) {
        set("cacheScope", cacheScope);
    }

    public String getPortletType() {
        return get("portletType");
    }

    public void setPortletType(String portletType) {
        set("portletType", portletType);
    }

    public GWTJahiaNodeACL getBaseAcl() {
        return baseAcl;
    }

    public void setBaseAcl(GWTJahiaNodeACL baseAcl) {
        this.baseAcl = baseAcl;
    }

    public String getDescription() {
        return get("description");
    }

    public void setDescription(String description) {
        set("description", description);
    }

}
