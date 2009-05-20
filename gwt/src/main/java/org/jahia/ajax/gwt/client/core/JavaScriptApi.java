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
package org.jahia.ajax.gwt.client.core;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Info;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.data.GWTJahiaPortletOutputBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaInlineEditingResultBean;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.util.URL;

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

    static void onBlurEditableContent(final Element e, String containerID, String fieldID) {
        Log.info("Blurring field [" + containerID + "," + fieldID + "]: " + e.getInnerHTML() + " contentEditable" + e.getAttribute("contentEditable"));
        if ((e.getAttribute("contentEditable") != null) &&
            (e.getAttribute("contentEditable").equals("true"))) {
            String content = e.getInnerHTML();
            e.setAttribute("contentEditable", "false");
            e.setClassName("inlineEditingDeactivated");
            Integer containerIDInt = Integer.parseInt(containerID);
            Integer fieldIDInt = Integer.parseInt(fieldID);
            JahiaService.App.getInstance().inlineUpdateField(containerIDInt, fieldIDInt, content, new AsyncCallback<GWTJahiaInlineEditingResultBean>() {
                public void onSuccess(GWTJahiaInlineEditingResultBean result) {
                    if (result.isContentModified()) {
                        Info.display("Content updated", "Content saved.");
                        Log.info("Content successfully modified.");
                    }
                }

                public void onFailure(Throwable throwable) {
                    Log.error("Error modifying content", throwable);
                }
            });
        }
    }

    static void onClickEditableContent(final Element e, String containerID, String fieldID) {
        Log.info("Checking if inline editing is allowed for containerID=" + containerID + " fieldID=" + fieldID);
        Integer containerIDInt = Integer.parseInt(containerID);
        Integer fieldIDInt = Integer.parseInt(fieldID);
        JahiaService.App.getInstance().isInlineEditingAllowed(containerIDInt, fieldIDInt, new AsyncCallback<Boolean>() {
            public void onSuccess(Boolean result) {
                if (result.booleanValue()) {
                    Log.info("Inline editing is allowed.");
                    e.setAttribute("contentEditable", "true");
                    e.setClassName("inlineEditingActivated");
                } else {
                    Log.info("Inline editing is not allowed.");
                }
            }

            public void onFailure(Throwable throwable) {
                Log.error("Error modifying content", throwable);
            }
        });
    }


    static void renderPortlet(final Element e, String windowID, String entryPointInstanceID, String pathInfo, String queryString) {
        GWTJahiaPageContext page = new GWTJahiaPageContext(URL.getRelativeURL());
        page.setPid(JahiaGWTParameters.getPID());
        page.setMode(JahiaGWTParameters.getOperationMode());
        JahiaService.App.getInstance().drawPortletInstanceOutput(page, windowID, entryPointInstanceID, pathInfo, queryString, new AsyncCallback<GWTJahiaPortletOutputBean>() {
            public void onSuccess(GWTJahiaPortletOutputBean result) {
                e.setInnerHTML(result.getHtmlOutput());
                Log.info("Portlet successfully loaded.");
            }

            public void onFailure(Throwable throwable) {
                Log.error("Error loading portlet HTML", throwable);
            }
        });
    }


    static void releaseLock(String lockType) {
            JahiaService.App.getInstance().releaseLock(lockType, new AsyncCallback<Boolean>() {
                public void onSuccess(Boolean result) {
                    Log.info("Lock released successfully.");
                }

                public void onFailure(Throwable throwable) {
                    Log.warn("Error releasing lock", throwable);
                }
            });
    }

    private native void initJavaScriptApi() /*-{
        $wnd.onBlurEditableContent = function (element, containerID, fieldID) {@org.jahia.ajax.gwt.client.core.JavaScriptApi::onBlurEditableContent(Lcom/google/gwt/user/client/Element;Ljava/lang/String;Ljava/lang/String;)(element, containerID, fieldID); };
        $wnd.onClickEditableContent = function (element, containerID, fieldID) {@org.jahia.ajax.gwt.client.core.JavaScriptApi::onClickEditableContent(Lcom/google/gwt/user/client/Element;Ljava/lang/String;Ljava/lang/String;)(element, containerID, fieldID); };
        $wnd.renderPortlet = function (element, windowID, entryPointInstanceID, pathInfo, queryString) {@org.jahia.ajax.gwt.client.core.JavaScriptApi::renderPortlet(Lcom/google/gwt/user/client/Element;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(windowID, entryPointInstanceID, pathInfo, queryString); };

        if (!$wnd.jahia) {
            $wnd.jahia = new Object();
        }
        $wnd.jahia.request = function (url, options) {@org.jahia.ajax.gwt.client.core.JavaScriptApi::request(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(url, options); };
        $wnd.jahia.releaseLock = function (lockType) {@org.jahia.ajax.gwt.client.core.JavaScriptApi::releaseLock(Ljava/lang/String;)(lockType); };
    }-*/;
}
