/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.bin;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * Result of the action handler execution.
 * User: toto
 * Date: Mar 18, 2010
 * Time: 3:14:41 PM
 */
public class ActionResult {
    
    public static final ActionResult BAD_REQUEST = new ActionResult(HttpServletResponse.SC_BAD_REQUEST);
    public static final ActionResult INTERNAL_ERROR = new ActionResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    public static final ActionResult INTERNAL_ERROR_JSON = new ActionResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, new JSONObject());
    public static final ActionResult OK = new ActionResult(HttpServletResponse.SC_OK);
    public static final ActionResult OK_JSON = new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject());
    public static final ActionResult SERVICE_UNAVAILABLE = new ActionResult(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

    private JSONObject json;

    private int resultCode;

    private String url;

    private boolean absoluteUrl;

    public ActionResult(int resultCode) {
        this(resultCode, null);
    }

    public ActionResult(int resultCode, String url) {
        this(resultCode, url, null);
    }

    public ActionResult(int resultCode, String url, JSONObject json) {
        this(resultCode, url, false, json);
    }

    public ActionResult(int resultCode, String url, boolean absoluteUrl, JSONObject json) {
        super();
        this.resultCode = resultCode;
        this.url = url;
        this.json = json;
        this.absoluteUrl = absoluteUrl;
    }

    public JSONObject getJson() {
        return json;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getUrl() {
        return url;
    }

    public boolean isAbsoluteUrl() {
        return absoluteUrl;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
