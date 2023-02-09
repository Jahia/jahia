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
package org.jahia.ajax.gwt.client.data;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;
import java.util.List;

/**
 *
 * User: rincevent
 * Date: 8 janv. 2009
 * Time: 16:43:36
 *
 */
public class GWTJahiaSite extends BaseModelData implements Serializable {
    private List<String> installedModules;

    public String getSiteName () {
        return get("siteName");
    }

    public void setSiteName (String siteName) {
        set("siteName",siteName);
    }

    public String getSiteKey () {
        return get("siteKey");
    }

    public void setSiteKey (String siteKey) {
        set("siteKey",siteKey);
    }

    public String getTemplateFolder () {
        return get("templateFolder");
    }

    public void setTemplateFolder (String siteKey) {
        set("templateFolder",siteKey);
    }

    public String getTemplatePackageName () {
        return get("templatePackageName");
    }

    public void setTemplatePackageName (String siteKey) {
        set("templatePackageName",siteKey);
    }

    public List<String> getInstalledModules() {
        return installedModules;
    }

    public void setInstalledModules(List<String> installedModules) {
        this.installedModules = installedModules;
    }
}
