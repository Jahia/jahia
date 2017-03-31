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
