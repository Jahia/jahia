/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.content.util;

import com.google.gwt.core.client.JsArray;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * User: ktlili
 * Date: Aug 21, 2009
 * Time: 2:01:49 PM
 *
 */
public class ContentHelper {

    public static List<GWTJahiaNode> getSelectedContentNodesFromHTML() {
        List<GWTJahiaNode> selectedContentNodes = new ArrayList<GWTJahiaNode>();
        JsArray<ContentNodeJavaScriptObject> contentNodeJavaScriptObjectJsArray = getContentNodeOverlayTypes();

        if (contentNodeJavaScriptObjectJsArray != null) {
            for (int i = 0; i < contentNodeJavaScriptObjectJsArray.length(); i++) {
                ContentNodeJavaScriptObject contentNodeJavaScriptObject = contentNodeJavaScriptObjectJsArray.get(i);
                GWTJahiaNode gwtJahiaNode = new GWTJahiaNode();
                gwtJahiaNode.setUUID(contentNodeJavaScriptObject.getUUID());
                gwtJahiaNode.setPath(contentNodeJavaScriptObject.getPath());
                gwtJahiaNode.setName(contentNodeJavaScriptObject.getName());
                gwtJahiaNode.setDisplayName(contentNodeJavaScriptObject.getDisplayName());
                selectedContentNodes.add(gwtJahiaNode);
            }
        }
        return selectedContentNodes;
    }

    public static native String getContentNodeLocale() /*-{
        return $wnd.sLocale;
    }-*/;

    public static native String getAutoSelectParent() /*-{
        return $wnd.sAutoSelectParent;
    }-*/;

    private static native JsArray<ContentNodeJavaScriptObject> getContentNodeOverlayTypes() /*-{
        // Get a reference to the first customer in the JSON array from earlier
        return $wnd.sContentNodes;
    }-*/;

    public static native void sendContentModificationEvent(String nodeUuid, String nodePath, String nodeName, String operation, String nodeType) /*-{
        if ($wnd.contentModificationEventHandlers) {
            $wnd.contentModificationEventHandlers.forEach(function(func) {
                func.call(null, nodeUuid, nodePath, nodeName, operation, nodeType);
            });
        }
    }-*/;
}
