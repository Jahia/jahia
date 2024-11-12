/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

/**
 * Data object that represents a key in the resource bundle in multiple languages.
 *
 * @author Sergiy Shyrkov
 */
public class GWTResourceBundleEntry extends BaseModelData implements
        Comparable<GWTResourceBundleEntry> {

    private static final long serialVersionUID = 5279030130987007606L;

    public GWTResourceBundleEntry() {
        super();
        setKey("<empty>");
        BaseModelData data = new BaseModelData();
        data.setAllowNestedValues(false);
        setValues(data);
    }

    public GWTResourceBundleEntry(String key) {
        this();
        setKey(key);
    }

    public int compareTo(GWTResourceBundleEntry o) {
        return getKey().compareTo(o.getKey());
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && this.getClass() == obj.getClass()
                && getKey().equals(((GWTResourceBundleEntry) obj).getKey());
    }

    public String getKey() {
        return get("key");
    }

    public String getValue(String language) {
        return getValues().get(language);
    }

    public BaseModelData getValues() {
        return get("values");
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    public void setKey(String key) {
        set("key", key);
    }

    public void setValue(String language, String value) {
        getValues().set(language, value);
    }

    public void setValues(BaseModelData values) {
        set("values", values);
    }
}
