/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

import java.util.*;

public class CustomEditEngineTabItem extends EditEngineTabItem {

    private boolean handleMultipleSelection;
    private String onInitMethodName;
    private String onLanguageChangeMethodName;
    private String doSaveMethodName;
    private String doValidateMethodName;

    private transient boolean inited = false;
    private transient String languageChanged;

    @Override
    public void init(NodeHolder engine, final AsyncTabItem tab, String language) {
        if (!inited || languageChanged != null) {
            Element element = null;

            JsArrayString types = JsArrayString.createArray().cast();
            for (GWTJahiaNodeType type : engine.getNodeTypes()) {
                types.push(type.getName());
            }

            if (languageChanged == null) {
                languageChanged =  JahiaGWTParameters.getLanguage();
            }
            String methodName = inited ? onLanguageChangeMethodName : onInitMethodName;

            JavaScriptObject param = null;

            if (engine.isExistingNode() && !engine.isMultipleSelection()) {
                param = convertExistingNode(engine.getNode(), engine.getProperties(), types, language);
            } else if (!engine.isExistingNode() && engine instanceof CreateContentEngine) {
                param = convertNewNode(((CreateContentEngine) engine).getParentPath(), engine.getNodeName(), types, language);
            }

            element = doCall(methodName, param).cast();

            if (element != null) {
                while (tab.getElement().getChildCount() > 0) {
                    tab.getElement().removeChild(tab.getElement().getChild(0));
                }
                tab.getElement().appendChild(element);
            }
            inited = true;
            languageChanged = null;
        }

    }

    @Override
    public void setProcessed(boolean processed) {
        if (!processed) {
            this.inited = false;
            this.languageChanged = null;
        }
        super.setProcessed(processed);
    }

    @Override
    public void onLanguageChange(String language, TabItem tabItem) {
        languageChanged = language;
    }

    @Override
    public void doValidate(List<EngineValidation.ValidateResult> validateResult, NodeHolder engine, TabItem tab, String selectedLanguage, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, TabPanel tabs) {
        if (doValidateMethodName != null) {
            doCall(doValidateMethodName, getValidateOperations(validateResult, engine, tab, selectedLanguage, changedI18NProperties));
        }
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, List<GWTJahiaNode> children, GWTJahiaNodeACL acl) {
        if (doSaveMethodName != null) {
            Map<String,GWTJahiaNode> childrenMap = new LinkedHashMap<String, GWTJahiaNode>();
            doCall(doSaveMethodName, getWriteOperations(node, changedProperties, changedI18NProperties, addedTypes, removedTypes, childrenMap));
            if (!childrenMap.isEmpty()) {
                if (node != null) {
                    if(node.getChildren() != null && node.getChildren().size() > 0) {
                        List<GWTJahiaNode> newChildren = new LinkedList<GWTJahiaNode>();
                        for (ModelData child : node.getChildren()) {
                            GWTJahiaNode alreadyExistChild = (GWTJahiaNode) child;
                            if(childrenMap.get(alreadyExistChild.getUUID()) != null) {
                                newChildren.add(childrenMap.get(alreadyExistChild.getUUID()));
                                childrenMap.remove(alreadyExistChild.getUUID());
                            } else {
                                newChildren.add(alreadyExistChild);
                            }
                        }
                        if (childrenMap.size() > 0) {
                            for (GWTJahiaNode child : childrenMap.values()) {
                                newChildren.add(child);
                            }
                        }
                        for (GWTJahiaNode child : newChildren) {
                            node.add(child);
                        }
                    } else {
                        for (GWTJahiaNode child : childrenMap.values()) {
                            node.add(child);
                        }
                    }
                    node.set(GWTJahiaNode.INCLUDE_CHILDREN, Boolean.TRUE);
                } else {
                    children.addAll(childrenMap.values());
                }
            }
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

    private static void setChild(String uuid, String name, Map<String,GWTJahiaNode> children) {
        GWTJahiaNode node = new GWTJahiaNode();
        node.setUUID(uuid);
        node.setName(name);
        node.setNodeTypes(new ArrayList<String>());
        node.set("nodeLangCodeProperties", new HashMap<String, List<GWTJahiaNodeProperty>>());
        node.set("nodeProperties", new ArrayList<GWTJahiaNodeProperty>());
        children.put(uuid, node);
    }

    public static void addChildMixin(String uuid, String mixin, Map<String,GWTJahiaNode> children) {
        children.get(uuid).getNodeTypes().add(mixin);
    }

    public static void setChildProperty(String uuid, String name, String value, int type, Map<String,GWTJahiaNode> children) {
        ((List<GWTJahiaNodeProperty>)children.get(uuid).get("nodeProperties")).add(new GWTJahiaNodeProperty(name, value, type));
    }

    public static void addValidationError(List<EngineValidation.ValidateResult> validateResult, TabItem tab, String message) {
        EngineValidation.ValidateResult result = new EngineValidation.ValidateResult();
        result.canIgnore = false;
        result.errorTab = tab;
        result.message = message;
        validateResult.add(result);
    }

    public static native JavaScriptObject convertExistingNode(GWTJahiaNode node, Map<String,GWTJahiaNodeProperty> properties, JsArrayString types, String language) /*-{
        var jsnode = {
            'isNewNode':false,
            'node': node,
            'getTypes' : function() {
                return types;
            },
            'getLanguage' : function() {
                return language;
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

    public static native JavaScriptObject convertNewNode(String parentPath, String name, JsArrayString types, String language) /*-{
        var jsnode = {
            'isNewNode':true,
            'getName' : function() {
                return name;
            },
            'getTypes' : function() {
                return types;
            },
            'getLanguage' : function() {
                return language;
            },
            'getParentPath': function () {
                return parentPath;
            }
        }
        return jsnode;
    }-*/;

    public static native JavaScriptObject getValidateOperations(List<EngineValidation.ValidateResult> validateResult, NodeHolder engine, TabItem tab, String selectedLanguage, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties) /*-{
        var jsnode = {
            'addValidationError': function (message) {
                @org.jahia.ajax.gwt.client.widget.contentengine.CustomEditEngineTabItem::addValidationError(Ljava/util/List;Lcom/extjs/gxt/ui/client/widget/TabItem;Ljava/lang/String;)(validateResult, tab, message);
            }
        }
        return jsnode;
    }-*/;

    public static native JavaScriptObject getWriteOperations(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, Map<String,GWTJahiaNode> children) /*-{
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
            },
            'child': function(uuid,name) {
                @org.jahia.ajax.gwt.client.widget.contentengine.CustomEditEngineTabItem::setChild(Ljava/lang/String;Ljava/lang/String;Ljava/util/Map;)(uuid, name, children);
                var subnode = {
                    'addMixin': function(mixin) {
                        @org.jahia.ajax.gwt.client.widget.contentengine.CustomEditEngineTabItem::addChildMixin(Ljava/lang/String;Ljava/lang/String;Ljava/util/Map;)(uuid, mixin, children);
                    },
                    'setProperty': function (property, value, type) {
                        @org.jahia.ajax.gwt.client.widget.contentengine.CustomEditEngineTabItem::setChildProperty(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/util/Map;)(uuid, property, value, type, children);
                    }
                }
                return subnode;
            }
        }
        return jsnode;
    }-*/;


}
