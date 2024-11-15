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
package org.jahia.ajax.gwt.client.data;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;

/**
 * Simple bean representing data having a value and a display name
 *
 * @author Xavier Lawrence
 */
public class GWTJahiaBasicDataBean extends BaseModelData implements Serializable {

    public static final String DISPLAY_NAME = "displayName";
    public static final String VALUE = "value";

    public GWTJahiaBasicDataBean() {
    }

    public GWTJahiaBasicDataBean(String value, String displayName) {
        setValue(value);
        setDisplayName(displayName);
    }

    public String getValue() {
        return get(VALUE);
    }

    public void setValue(String value) {
        set(VALUE, value);
    }

    public String getDisplayName() {
        return get(DISPLAY_NAME);
    }

    public void setDisplayName(String displayName) {
        set(DISPLAY_NAME, displayName);
    }

    public String toString() {
        return get(VALUE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GWTJahiaBasicDataBean that = (GWTJahiaBasicDataBean) o;

        return getValue() == null && that.getValue() == null || getValue() != null && that.getValue() != null
                && getValue().equals(that.getValue());
    }

    @Override
    public int hashCode() {
        return getValue() != null ? getValue().hashCode() : 0;
    }
}
