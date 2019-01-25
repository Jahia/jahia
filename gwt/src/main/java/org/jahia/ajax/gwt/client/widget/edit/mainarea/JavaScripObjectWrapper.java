package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.js.JsObject;
import com.google.gwt.core.client.JavaScriptObject;

class JavaScripObjectWrapper extends JsObject {
    public JavaScripObjectWrapper(JavaScriptObject obj) {
        this.jsObject = obj;
    }
}
