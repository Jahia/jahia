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
package org.jahia.ajax.gwt.client.data.category;

import java.io.Serializable;

/**
 * User: ktlili
 * Date: 25 sept. 2008
 * Time: 17:44:48
 */
public class GWTJahiaCategoryTitle implements Serializable {
    private String locale;
    private String titleValue;

    public GWTJahiaCategoryTitle() {
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTitleValue() {
        return titleValue;
    }

    public void setTitleValue(String titleValue) {
        this.titleValue = titleValue;
    }
}
