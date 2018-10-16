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
