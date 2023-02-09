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

/**
 * Key-value bean for representing values in selection lists or boxes.
 * User: rfelden
 * Date: 22 oct. 2008 - 12:48:46
 */
public class GWTJahiaValueDisplayBean extends BaseModelData implements Serializable {

    public GWTJahiaValueDisplayBean() {
        super() ;
    }

    public GWTJahiaValueDisplayBean(String value, String display) {
        super() ;
        setAllowNestedValues(false);
        set("value", value);
        set("display", display);
        set("image", "");
    }

    public String getValue() {
        return get("value");
    }

    public void setValue(String value) {
        set("value", value) ;
    }

    public String getDisplay() {
        return get("display") ;
    }

    public void setDisplay(String display) {
        set("display", display) ;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || (obj != null && this.getClass() == obj.getClass()
                && ((getValue() == null && ((GWTJahiaValueDisplayBean) obj).getValue() == null)
                        || getValue() != null && getValue().equals(((GWTJahiaValueDisplayBean) obj).getValue())));
    }

    @Override
    public int hashCode() {
        return getValue() != null ? getValue().hashCode() : 0;
    }
}
