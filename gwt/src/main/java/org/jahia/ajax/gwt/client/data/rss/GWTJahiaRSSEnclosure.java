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
 * Time: 10:01:57
 */
public class GWTJahiaRSSEnclosure extends BaseModel implements Serializable {
    public GWTJahiaRSSEnclosure() {
    }

    public String getUrl() {
        return get("url");
    }

    public void setUrl(String s) {
        set("url", s);
    }

    public String getLength() {
        return get("length");
    }

    public void setLength(String l) {
        set("length", l);
    }

    public String getType() {
        return get("type");
    }

    public void setType(String s) {
        set("type", s);
    }
}
