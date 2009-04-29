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
package org.jahia.ajax.gwt.client.widget.portlet;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.GWTJahiaPortletOutputBean;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.core.JahiaPageEntryPoint;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Dec 4, 2008
 * Time: 3:09:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class PortletRender extends HTML {
    private GWTJahiaPortletOutputBean gwtPortletOutputBean;

    public PortletRender(GWTJahiaPageContext page, String windowID, String entryPointInstanceID, String pathInfo, String queryString) {
        setHTML("Loading...");
        JahiaService.App.getInstance().drawPortletInstanceOutput(page, windowID, entryPointInstanceID, pathInfo, queryString, new AsyncCallback<GWTJahiaPortletOutputBean>() {
            public void onSuccess(GWTJahiaPortletOutputBean result) {
                gwtPortletOutputBean = result;
                if (gwtPortletOutputBean.isInIFrame()) {
                    setHTML("");
                    Element iFrameElement = DOM.createElement("iframe");
                    DOM.setElementAttribute(iFrameElement, "frameBorder", "0");
                    DOM.setElementAttribute(iFrameElement, "border", "0");
                    DOM.setElementAttribute(iFrameElement, "scrolling", "no");
                    DOM.setElementAttribute(iFrameElement, "marginHeight", "0");
                    DOM.setElementAttribute(iFrameElement, "marginWidth", "0");
                    DOM.setElementAttribute(iFrameElement, "onload", "resizeIFrame(this)");
                    if (gwtPortletOutputBean.getIFrameWidth() != null) {
                        DOM.setElementAttribute(iFrameElement, "width", gwtPortletOutputBean.getIFrameWidth());
                    } else {
                        DOM.setElementAttribute(iFrameElement, "width", "100%");
                    }
                    if (gwtPortletOutputBean.getIFrameHeight() != null) {
                        DOM.setElementAttribute(iFrameElement, "height", gwtPortletOutputBean.getIFrameHeight());
                    } else {
                        DOM.setElementAttribute(iFrameElement, "height", "100");
                    }
                    getElement().appendChild(iFrameElement);
                    iFrameDocumentWrite(iFrameElement, gwtPortletOutputBean.getHtmlOutput(), Long.toString(gwtPortletOutputBean.getDelayedIFrameResizeTime()));
                } else {
                    setHTML(gwtPortletOutputBean.getHtmlOutput());
                    // we still need to execute the Javascript that can be generated in the HTML, as it is not executed
                    // by default.
                    // we must now make sure we execute all the scripts in the HTML.
                    for (String curScriptSrc : gwtPortletOutputBean.getScriptsWithSrc()) {
                        Element scriptElement = DOM.createElement("script");
                        DOM.setElementAttribute(scriptElement, "src", curScriptSrc);
                        getElement().appendChild(scriptElement);
                    }

                    for (String curScriptCode : gwtPortletOutputBean.getScriptsWithCode()) {
                        eval(curScriptCode);
                    }

                }

                if (gwtPortletOutputBean.isInContentPortlet()) {
                    JahiaPageEntryPoint.instance.loadJahiaModules(getElement());
                }

                Log.info("Portlet HTML successfully retrieved.");

                onRender();
            }

            public void onFailure(Throwable throwable) {
                setHTML("Unable to load portlet.");
                Log.error("Error modifying portlet HTML", throwable);
            }
        });

    }

    public static native void eval(String source) /*-{
       $wnd.eval(source);
    }-*/;

    public static native void iFrameDocumentWrite(Element iFrameElement, String content, String delayedIFrameResizeTime) /*-{
       if ($wnd.iFrameDocumentWrite) {
           $wnd.iFrameDocumentWrite(iFrameElement, content, delayedIFrameResizeTime);
       }
    }-*/;

    public void onRender() {

    }

    public void refresh() {

    }

}
