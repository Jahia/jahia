/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.core;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.data.GWTJahiaPortletOutputBean;
import org.jahia.ajax.gwt.client.service.JahiaService;

/**
 * Exposes GWT methods into the page scope to be used from native JavaScript functions.
 *
 * @author ktlili
 *         Date: 30 sept. 2008
 *         Time: 09:38:15
 */
public class JavaScriptApi {

    private static boolean initialized;
    
    private JavaScriptApi() {
        super();
    }

    public static void init() {
        if (!initialized) {
            new JavaScriptApi().initJavaScriptApi();
            initialized = true;
        }
    }
    
    static void request(String url, JavaScriptObject options) {
        AjaxRequest.perfom(url, options);
    }

    static void renderPortlet(final Element e, String windowID, String entryPointInstanceID, String pathInfo, String queryString) {
        JahiaService.App.getInstance().drawPortletInstanceOutput(windowID, entryPointInstanceID, pathInfo, queryString, new BaseAsyncCallback<GWTJahiaPortletOutputBean>() {
            public void onSuccess(GWTJahiaPortletOutputBean result) {
                e.setInnerHTML(result.getHtmlOutput());
                Log.info("Portlet successfully loaded.");
            }
        });
    }

    private native void initJavaScriptApi() /*-{
        $wnd.renderPortlet = function (element, windowID, entryPointInstanceID, pathInfo, queryString) {@org.jahia.ajax.gwt.client.core.JavaScriptApi::renderPortlet(Lcom/google/gwt/user/client/Element;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(windowID, entryPointInstanceID, pathInfo, queryString); };

        if (!$wnd.jahia) {
            $wnd.jahia = new Object();
        }
        $wnd.jahia.request = function (url, options) {@org.jahia.ajax.gwt.client.core.JavaScriptApi::request(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(url, options); };
    }-*/;
}
