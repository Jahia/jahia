/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
