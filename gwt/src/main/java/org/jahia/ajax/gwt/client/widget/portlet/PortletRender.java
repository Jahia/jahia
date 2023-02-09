/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.portlet;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaPortletOutputBean;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

/**
 *
 * User: loom
 * Date: Dec 4, 2008
 * Time: 3:09:02 PM
 *
 */
public class PortletRender extends HTML {
    private GWTJahiaPortletOutputBean gwtPortletOutputBean;

    public PortletRender(String windowID, String entryPointInstanceID, String pathInfo, String queryString) {
        setHTML("Loading...");
        JahiaContentManagementService.App.getInstance().drawPortletInstanceOutput(windowID, entryPointInstanceID, pathInfo, queryString, new BaseAsyncCallback<GWTJahiaPortletOutputBean>() {
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

                Log.info("Portlet HTML successfully retrieved.");

                onRender();
            }

            public void onApplicationFailure(Throwable throwable) {
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



}
