package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomEditEngineTabItem extends EditEngineTabItem {

    private boolean handleMultipleSelection;

    private String onInitMethodName;
    private String onLanguageChangeMethodName;
    private String doSaveMethodName;
    private String doValidateMethodName;

    @Override
    public void init(NodeHolder engine, final AsyncTabItem tab, String language) {
        tab.setProcessed(true);

        Element element = null;

        JsArrayString types = JsArrayString.createArray().cast();
        for (GWTJahiaNodeType type : engine.getNodeTypes()) {
            types.push(type.getName());
        }

        if (engine.isExistingNode() && !engine.isMultipleSelection()) {
            element = onInit(convertExistingNode(engine.getNode(), engine.getProperties(), types));
        } else if (!engine.isExistingNode() && engine instanceof CreateContentEngine) {
            element = onInit(convertNewNode(((CreateContentEngine) engine).getParentPath(), engine.getNodeName(), types));
        }
        if (element != null) {
            tab.getElement().appendChild(element);
        }
    }

    public com.google.gwt.user.client.Element onInit(JavaScriptObject param) {
        return doCall(onInitMethodName, param).cast();
    }

    @Override
    public void onLanguageChange(String language, TabItem tabItem) {
        if (onLanguageChangeMethodName != null) {
            doCall(onLanguageChangeMethodName, language);
        }
    }

    @Override
    public void doValidate(List<EngineValidation.ValidateResult> validateResult, NodeHolder engine, TabItem tab, String selectedLanguage, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, TabPanel tabs) {
        if (doValidateMethodName != null) {
            JavaScriptObject javaScriptObject = getValidateOperations(validateResult, engine, selectedLanguage, changedI18NProperties);
            doCall(doValidateMethodName, null);
        }
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, List<GWTJahiaNode> chidren, GWTJahiaNodeACL acl) {
        if (doSaveMethodName != null) {
            doCall(doSaveMethodName, getWriteOperations(changedProperties, changedI18NProperties, addedTypes, removedTypes));
        }
    }

    public static native JavaScriptObject doCall(String key, Object param) /*-{
        return eval('$wnd.' + key)(param);
    }-*/;

    @Override
    public boolean isHandleMultipleSelection() {
        return handleMultipleSelection;
    }

    public void setHandleMultipleSelection(boolean handleMultipleSelection) {
        this.handleMultipleSelection = handleMultipleSelection;
    }

    public void setOnInitMethodName(String onInitMethodName) {
        this.onInitMethodName = onInitMethodName;
    }

    public void setOnLanguageChangeMethodName(String onLanguageChangeMethodName) {
        this.onLanguageChangeMethodName = onLanguageChangeMethodName;
    }

    public void setDoSaveMethodName(String doSaveMethodName) {
        this.doSaveMethodName = doSaveMethodName;
    }

    public void setDoValidateMethodName(String doValidateMethodName) {
        this.doValidateMethodName = doValidateMethodName;
    }

    public static void setProperty(List<GWTJahiaNodeProperty> changedProperties, String name, String value, int type) {
        changedProperties.add(new GWTJahiaNodeProperty(name, value, type));
    }

    public static Object getProperty(Map<String,GWTJahiaNodeProperty> properties, String name) {
        if (properties.containsKey(name)) {
            GWTJahiaNodeProperty property = properties.get(name);
            if (property.isMultiple()) {
                JsArrayString array = JsArrayString.createArray().cast();
                for (GWTJahiaNodePropertyValue value : property.getValues()) {
                    array.push(value.getString());
                }
                return array;
            } else {
                return property.getValues().get(0).getString();
            }
        }
        return null;
    }

    public static void setI18NProperty(Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, String lang, String name, String value, int type) {
        if (!changedI18NProperties.containsKey(lang)) {
            changedI18NProperties.put(lang, new ArrayList<GWTJahiaNodeProperty>());
        }
        changedI18NProperties.get(lang).add(new GWTJahiaNodeProperty(name, value, type));
    }

    public static native JavaScriptObject convertExistingNode(GWTJahiaNode node, Map<String,GWTJahiaNodeProperty> properties, JsArrayString types) /*-{
        var jsnode = {
            'isNewNode':false,
            'node': node,
            'getTypes' : function() {
                return types;
            },
            'getProperty': function (property) {
                return  @org.jahia.ajax.gwt.client.widget.contentengine.CustomEditEngineTabItem::getProperty(Ljava/util/Map;Ljava/lang/String;)(properties, property);
            },
            'getName': function () {
                return node.@org.jahia.ajax.gwt.client.data.node.GWTJahiaNode::getName()()
            },
            'getPath': function () {
                return node.@org.jahia.ajax.gwt.client.data.node.GWTJahiaNode::getPath()()
            },
            'getUUID': function () {
                return node.@org.jahia.ajax.gwt.client.data.node.GWTJahiaNode::getUUID()()
            }
        }
        return jsnode;
    }-*/;

    public static native JavaScriptObject convertNewNode(String parentPath, String name, JsArrayString types) /*-{
        var jsnode = {
            'isNewNode':true,
            'getName' : function() {
                return name;
            },
            'getTypes' : function() {
                return types;
            },
            'getParentPath': function () {
                return parentPath;
            }
        }
        return jsnode;
    }-*/;

    public static native JavaScriptObject getValidateOperations(List<EngineValidation.ValidateResult> validateResult, NodeHolder engine, String selectedLanguage, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties) /*-{
        var jsnode = {

        }
        return jsnode;
    }-*/;

    public static native JavaScriptObject getWriteOperations(List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes) /*-{
        var jsnode = {
            'addMixin': function (mixin) {
                addedTypes.@java.util.Set::add(Ljava/lang/Object;)(mixin);
                removedTypes.@java.util.Set::remove(Ljava/lang/Object;)(mixin);
            },
            'removeMixin': function (mixin) {
                addedTypes.@java.util.Set::remove(Ljava/lang/Object;)(mixin);
                removedTypes.@java.util.Set::add(Ljava/lang/Object;)(mixin)
            },
            'setProperty': function (property, value, type) {
                @org.jahia.ajax.gwt.client.widget.contentengine.CustomEditEngineTabItem::setProperty(Ljava/util/List;Ljava/lang/String;Ljava/lang/String;I)(changedProperties, property, value, type);
            },
            'setI18NProperty': function (lang, property, value, type) {
                @org.jahia.ajax.gwt.client.widget.contentengine.CustomEditEngineTabItem::setI18NProperty(Ljava/util/Map;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)(changedI18NProperties, lang, property, value, type)
            }
        }
        return jsnode;
    }-*/;


}
