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
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

/**
 * Edit engine custom configuration
 * Used when calling edit engine from JNSI exposed functions
 */
public class EditEngineJSConfig extends JavaScriptObject {

    protected EditEngineJSConfig() {}

    public static native EditEngineJSConfig getDefaultJSConfig() /*-{ return {}; }-*/;

    /**
     * hide the WIP buttons
     * Default value "false"
     */
    public final native boolean hideWip() /*-{ return !!this.hideWip; }-*/;

    /**
     * hide the tabs header
     * Default value "false"
     */
    public final native boolean hideHeaders() /*-{ return !!this.hideHeaders; }-*/;

    /**
     * skip lock
     * Default value "false"
     */
    public final native boolean skipLock() /*-{ return !!this.skipLock; }-*/;

    /**
     * Check if the given tab should be displayed or not
     * @param tabId the given tab
     * @return true if the tab should be displayed
     */
    public final boolean isTabDisplayed(String tabId) {
        JsArrayString tabs = getDisplayedTabs();
        return tabs == null || MainModule.convertArray(tabs).contains(tabId);
    }

    private native JsArrayString getDisplayedTabs() /*-{ return this.displayedTabs; }-*/;
}
