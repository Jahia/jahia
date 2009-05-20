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
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 8 janv. 2009
 * Time: 16:43:36
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaSite extends BaseModelData implements Serializable {
    public int getSiteId () {
        return (Integer) get("siteId");
    }

    public void setSiteId (int siteId) {
        set("siteId", new Integer(siteId));
    }

    public String getSiteName () {
        return get("siteName");
    }

    public void setSiteName (String siteName) {
        set("siteName",siteName);
    }

    public String getSiteKey () {
        return get("siteKey");
    }

    public void setSiteKey (String siteKey) {
        set("siteKey",siteKey);
    }
}
