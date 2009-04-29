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
package org.jahia.ajax.gwt.client.data.rss;

import com.extjs.gxt.ui.client.data.BaseModel;

import java.io.Serializable;


/**
 * User: ktlili
 * Date: 19 nov. 2008
 * Time: 10:11:13
 */
public class GWTJahiaRSSPerson extends BaseModel  implements Serializable {
    public GWTJahiaRSSPerson() {
    }

    public String getName() {
        return get("name");
    }

    public void setName(String s) {
        set("name", s);
    }

    public String getUri() {
        return get("uri");
    }

    public void setUri(String s) {
        set("uri", s);
    }

    public String getEmail() {
        return get("email");
    }

    public void setEmail(String s) {
        set("email", s);
    }
}
