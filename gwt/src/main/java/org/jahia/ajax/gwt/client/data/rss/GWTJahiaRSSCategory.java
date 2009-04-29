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
 * Time: 10:29:55
 */
public class GWTJahiaRSSCategory extends BaseModel implements Serializable {
    public GWTJahiaRSSCategory() {
    }

    public String getName() {
        return get("name");
    }

    public void setName(String s) {
        set("name",s);
    }

    public String getTaxonomyUri() {
        return get("taxonomyUri");
    }

    public void setTaxonomyUri(String s) {
        set("taxonomyUri",s);
    }
}
