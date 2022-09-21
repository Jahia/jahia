/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
import java.util.Date;

/**
 * 
 * User: loom
 * Date: Oct 5, 2010
 * Time: 3:29:38 PM
 * 
 */
public class GWTJahiaContentHistoryEntry extends BaseModelData implements Serializable, Comparable<GWTJahiaContentHistoryEntry> {

    private static final long serialVersionUID = -3193096751387080238L;

    public GWTJahiaContentHistoryEntry() {
        super();
    }

    public GWTJahiaContentHistoryEntry(Date date, String action, String propertyName, String userKey, String path, String message, String languageCode) {
        super();
        setDate(date);
        setAction(action);
        setPropertyName(propertyName);
        setUserKey(userKey);
        setPath(path);
        setMessage(message);
        setLanguageCode(languageCode);
    }

    public Date getDate() {
        return get("date");
    }

    public void setDate(Date date) {
        set("date", date);
    }

    public String getAction() {
        return get("action");
    }

    public void setAction(String action) {
        set("action", action);
    }

    public String getPropertyName() {
        return get("propertyName");
    }

    public void setPropertyName(String propertyName) {
        set("propertyName", propertyName);
    }

    public String getUserKey() {
        return get("userKey");
    }

    public void setUserKey(String userKey) {
        set("userKey", userKey);
    }

    public String getPath() {
        return get("path");
    }

    public void setPath(String path) {
        set("path", path);
    }

    public String getMessage() {
        return get("message");
    }

    public void setMessage(String message) {
        set("message", message);
    }

    public String getLanguageCode() {
        return get("languageCode");
    }

    public void setLanguageCode(String languageCode) {
        set("languageCode", languageCode);
    }

    public int compareTo(GWTJahiaContentHistoryEntry o) {
        return getDate().compareTo(o.getDate());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GWTJahiaContentHistoryEntry that = (GWTJahiaContentHistoryEntry) o;

        return getDate().equals(that.getDate());
    }
    
    @Override
    public int hashCode() {
        return getDate() != null ? getDate().hashCode() : 0;
    }    
}
