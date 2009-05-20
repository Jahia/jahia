/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;

/**
 * User: rfelden
 * Date: 22 oct. 2008 - 12:48:46
 */
public class GWTJahiaValueDisplayBean extends BaseModelData implements Serializable {

    String value ;
    String display ;

    public GWTJahiaValueDisplayBean() {
        super() ;
    }

    public GWTJahiaValueDisplayBean(String value, String display) {
        super() ;
        set("value", value);
        set("display", display);
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
}
