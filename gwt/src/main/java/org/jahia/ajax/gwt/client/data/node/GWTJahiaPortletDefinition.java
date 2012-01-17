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
