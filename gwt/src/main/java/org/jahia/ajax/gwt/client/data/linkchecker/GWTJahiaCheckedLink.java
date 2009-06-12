/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data.linkchecker;

import com.extjs.gxt.ui.client.data.BaseModel;

import java.io.Serializable;

/**
 * User: romain
 * Date: 11 juin 2009
 * Time: 12:05:15
 */
public class GWTJahiaCheckedLink extends BaseModel implements Serializable {

    public GWTJahiaCheckedLink() {
        super();
    }

    public GWTJahiaCheckedLink(String link, String pageTitle, String pageUrl, int code) {
        super();
        setLink(link);
        setPageTitle(pageTitle);
        setPageUrl(pageUrl);
        setCode(code);
    }

    public String getLink() {
        return get("link");
    }

    public void setLink(String link) {
        set("link", link);
    }

    public String getPageUrl() {
        return get("pageUrl");
    }

    public void setPageUrl(String pageUrl) {
        set("pageUrl", pageUrl);
    }

    public String getPageTitle() {
        return get("pageTitle");
    }

    public void setPageTitle(String pageTitle) {
        set("pageTitle", pageTitle);
    }

    public int getCode() {
        Integer code = get("code");
        if (code != null) {
            return code.intValue();
        } else {
            return -1;
        }
    }

    public void setCode(int code) {
        set("code", Integer.valueOf(code));
    }
}
