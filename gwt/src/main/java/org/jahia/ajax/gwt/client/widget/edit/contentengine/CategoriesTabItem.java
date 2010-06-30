package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.definition.CategoriesEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:44:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class CategoriesTabItem extends EditEngineTabItem {
    private CategoriesEditor categoriesEditor;

    public CategoriesTabItem(NodeHolder engine) {
        super(Messages.get("label.engineTab.categories", "Categories"), engine);
        //setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabClassification());
    }

    @Override
    public void create(GWTJahiaLanguage locale) {
        if (!engine.isExistingNode() || (engine.getNode() != null)) {
            setProcessed(true);
            categoriesEditor = new CategoriesEditor(engine.getNode());
            add(categoriesEditor);

        }
        layout();
    }

    public CategoriesEditor getCategoriesEditor() {
        return categoriesEditor;
    }

    public void updateProperties(CategoriesEditor categoriesEditor, List<GWTJahiaNodeProperty> list, List<String> mixin) {
        if (categoriesEditor == null) {
            return;
        }

        GWTJahiaNodeProperty category = null;
        GWTJahiaNodeProperty tags = null;

        for (GWTJahiaNodeProperty property : list) {
            if (property.getName().equals("j:defaultCategory")) {
                category = property;
            }
        }

        List<GWTJahiaNode> gwtJahiaNodes = categoriesEditor.getCatStore().getAllItems();
        List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>(gwtJahiaNodes.size());
        for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodes) {
            values.add(new GWTJahiaNodePropertyValue(gwtJahiaNode, GWTJahiaNodePropertyType.REFERENCE));
        }
        GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
        gwtJahiaNodeProperty.setMultiple(true);
        gwtJahiaNodeProperty.setValues(values);
        gwtJahiaNodeProperty.setName("j:defaultCategory");
        if (category != null) {
            if (values.isEmpty()) {
                mixin.remove("jmix:categorized");
                list.remove(category);
            } else {
                list.add(gwtJahiaNodeProperty);
            }
        } else {
            if (!values.isEmpty()) {
                mixin.add("jmix:categorized");
                list.add(gwtJahiaNodeProperty);
            }
        }
    }
}
