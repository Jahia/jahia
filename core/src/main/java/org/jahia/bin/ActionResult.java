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
