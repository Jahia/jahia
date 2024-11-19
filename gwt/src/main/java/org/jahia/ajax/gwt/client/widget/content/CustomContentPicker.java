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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomContentPicker extends LayoutContainer {
    private CustomPickerConfiguration config;
    private List<String> values;

    private transient boolean initialized = false;

    public CustomContentPicker(CustomPickerConfiguration config, List<GWTJahiaNode> value, GWTJahiaNode siteNode) {
        super(new FillLayout());
        this.config = config;
        this.values = new ArrayList<String>();
        for (GWTJahiaNode gwtJahiaNode : value) {
            this.values.add(gwtJahiaNode.getPath());
        }

        JahiaGWTParameters.setSiteNode(siteNode);

        setId("JahiaGxtCustomContentPicker");
        setHeight("100%");
        setWidth("100%");

        if (!initialized) {
            Element element = null;

            String methodName = config.getInitMethodName();
            element = doCall(methodName, null).cast();

            if (element != null) {
                HTML widget = new HTML();
                widget.getElement().appendChild(element);
                this.add(widget);
            }
            initialized = true;
        }

    }

    public static native JavaScriptObject doCall(String key, Object param) /*-{
        return eval('$wnd.' + key)(param);
    }-*/;

    public void loadData() {
        doCall(config.getLoadFieldValueMethodName(),values);
    }

    public List<String> getValues() {
        return Arrays.asList(((JsArrayString) doCall(config.getGetFieldValueFromPickerMethodName(),null).cast()).join().split(","));
    }
}
