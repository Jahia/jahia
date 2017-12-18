/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;

/**
 * 
 * User: toto
 * Date: Sep 11, 2008
 * Time: 2:09:18 PM
 * 
 */
public class GWTJahiaNodeUsage extends BaseModelData implements Serializable {

    public GWTJahiaNodeUsage() {
    }

    public GWTJahiaNodeUsage(String identifier, String url) {
        setPath(url);
        setIdentifier(identifier);
    }

    public void setIdentifier(String identifier) {
        set("identifier", identifier);
    }

    public String getIdentifier() {
        return get("identifier");
    }

    public int getId() {
        Integer idInt = get("id");
        if (idInt != null) {
            return idInt.intValue();
        } else {
            return -1;
        }
    }

    public void setId(int id) {
        set("id", Integer.valueOf(id));
    }

    public String getNodeName() {
        return get("nodeName");
    }

    public String getNodeTitle() {
        return get("nodeName");
    }

    public void setNodeName(String nodeName) {
        set("nodeName", nodeName);
    }

    public void setNodeTitle(String nodeTitle) {
        set("nodeTitle", nodeTitle);
    }

    public int getVersion() {
        Integer idInt = get("version");
        if (idInt != null) {
            return idInt.intValue();
        } else {
            return -1;
        }
    }

    public void setVersion(int version) {
        set("version", Integer.valueOf(version));
    }

    public String getLanguage() {
        return get("lang");
    }

    public void setLanguage(String lang) {
        set("lang", lang);
    }

    public String getPageTitle() {
        return get("pageTitle");
    }

    public void setPageTitle(String pageTitle) {
        set("pageTitle", pageTitle);
    }

    public String getPath() {
        return get("path");
    }

    public void setPath(String url) {
        set("path", url);
    }

    public String getPagePath() {
        return get("pagePath");
    }

    public void setPagePath(String url) {
        set("pagePath", url);
    }

    public String getVersionName() {
        return get("versionName");
    }

    public void setVersionName(String versionName) {
        set("versionName", versionName);
    }

    public void setType(String type) {
        set("type",type);
    }

    public String getType() {
        return get("type");
    }
}
