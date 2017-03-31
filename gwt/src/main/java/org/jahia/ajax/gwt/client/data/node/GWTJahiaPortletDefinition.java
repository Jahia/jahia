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
package org.jahia.ajax.gwt.client.data.node;

import com.extjs.gxt.ui.client.data.BaseModel;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;

/**
 * 
 * User: toto
 * Date: Nov 28, 2008
 * Time: 6:14:28 PM
 * 
 */
public class GWTJahiaPortletDefinition extends BaseModel {

    private GWTJahiaNodeACL baseAcl;

    public GWTJahiaPortletDefinition() {
    }

    public GWTJahiaPortletDefinition(String appId,String contextName, String name,String displayName, String portletType, GWTJahiaNodeACL baseAcl, String description, Integer exp, String cacheScope) {
        setApplicationId(appId);
        setContextName(contextName);
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

    private void setApplicationId(String appId) {
        set("applicationId",appId);
    }
    
    public String getApplicationId() {
        return get("applicationId");
    }
}
