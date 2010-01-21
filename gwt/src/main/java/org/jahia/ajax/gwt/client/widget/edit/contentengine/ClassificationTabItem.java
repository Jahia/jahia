package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.definition.ClassificationEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:44:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassificationTabItem extends EditEngineTabItem {
    private ClassificationEditor classificationEditor;

    public ClassificationTabItem(AbstractContentEngine engine) {
        super(Messages.get("ece_classification", "Classification"), engine);
        setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabClassification());
    }

    @Override
    public void create(GWTJahiaLanguage locale) {
        if (!engine.isExistingNode() || (engine.getNode() != null)) {
            setProcessed(true);
            classificationEditor = new ClassificationEditor(engine.getNode());
            add(classificationEditor);

        }
        layout();
    }

    public ClassificationEditor getClassificationEditor() {
        return classificationEditor;
    }

    public void updatePropertiesListWithClassificationEditorData(ClassificationEditor classificationEditor, List<GWTJahiaNodeProperty> list, List<String> mixin) {
        if (classificationEditor == null) {
            return;
        }

        GWTJahiaNodeProperty category = null;
        GWTJahiaNodeProperty tags = null;

        for (GWTJahiaNodeProperty property : list) {
            if (property.getName().equals("j:defaultCategory")) {
                category = property;
            } else if (property.getName().equals("j:tags")) {
                tags = property;
            }
        }

        List<GWTJahiaNode> gwtJahiaNodes = classificationEditor.getCatStore().getAllItems();
        List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>(gwtJahiaNodes.size());
        for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodes) {
            values.add(new GWTJahiaNodePropertyValue(gwtJahiaNode));
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

        gwtJahiaNodes = classificationEditor.getTagStore().getAllItems();
        values = new ArrayList<GWTJahiaNodePropertyValue>(gwtJahiaNodes.size());
        for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodes) {
            values.add(new GWTJahiaNodePropertyValue(gwtJahiaNode, GWTJahiaNodePropertyType.WEAKREFERENCE));
        }
        gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
        gwtJahiaNodeProperty.setMultiple(true);
        gwtJahiaNodeProperty.setValues(values);
        gwtJahiaNodeProperty.setName("j:tags");
        if (tags != null) {
            if (values.isEmpty()) {
                mixin.remove("jmix:tagged");
                list.remove(tags);
            } else {
                list.add(gwtJahiaNodeProperty);
            }
        } else {
            if (!values.isEmpty()) {
                mixin.add("jmix:tagged");
                list.add(gwtJahiaNodeProperty);
            }
        }
    }


}
