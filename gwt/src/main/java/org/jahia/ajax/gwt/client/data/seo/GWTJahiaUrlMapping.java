/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.data.seo;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * Represent a single vanity URL.
 *
 * @author Sergiy Shyrkov
 *
 */
public class GWTJahiaUrlMapping extends BaseModel {

    private static final long serialVersionUID = -5139885507643055506L;

    /**
     * Initializes an instance of this class.
     */
    public GWTJahiaUrlMapping() {
        super();
    }

    public GWTJahiaUrlMapping(String url, String language, boolean isDefault, boolean isActive) {
        this();
        set("url", url);
        set("language", language);
        set("default", Boolean.valueOf(isDefault));
        set("active", Boolean.valueOf(isActive));
    }

    public GWTJahiaUrlMapping(String path, String url, String language, boolean isDefault, boolean isActive) {
        this(url, language, isDefault, isActive);
        set("path", path);
    }

    public String getPath() {
        return get("path");
    }

    public String getLanguage() {
        return get("language");
    }

    public String getUrl() {
        return get("url");
    }

    public boolean isActive() {
        return ((Boolean) get("active")).booleanValue();
    }

    public boolean isDefault() {
        return ((Boolean) get("default")).booleanValue();
    }

    public void setDefault(boolean isDefault) {
        set("default", Boolean.valueOf(isDefault));
    }

}
