/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
