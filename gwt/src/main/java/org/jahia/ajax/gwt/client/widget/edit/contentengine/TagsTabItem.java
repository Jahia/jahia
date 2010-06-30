package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.definition.TagsEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Jun 30, 2010
 * Time: 10:04:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class TagsTabItem extends EditEngineTabItem {
    private TagsEditor tagsEditor;

    public TagsTabItem(NodeHolder engine) {
        super(Messages.get("label.engineTab.tags", "Tags"), engine);
    }

    public void create(GWTJahiaLanguage locale) {
        if (!engine.isExistingNode() || (engine.getNode() != null)) {
            setProcessed(true);
            tagsEditor = new TagsEditor(engine.getNode());
            add(tagsEditor);

        }
        layout();
    }

    public TagsEditor getTagsEditor() {
        return tagsEditor;
    }
    
    public void updateProperties(TagsEditor tagsEditor, List<GWTJahiaNodeProperty> list, List<String> mixin) {
        if (tagsEditor == null) {
            return;
        }

        GWTJahiaNodeProperty tags = null;

        for (GWTJahiaNodeProperty property : list) {
            if (property.getName().equals("j:tags")) {
                tags = property;
            }
        }
        List<GWTJahiaNode> gwtJahiaNodes = tagsEditor.getTagStore().getAllItems();
        List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>(gwtJahiaNodes.size());
        for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodes) {
            values.add(new GWTJahiaNodePropertyValue(gwtJahiaNode, GWTJahiaNodePropertyType.WEAKREFERENCE));
        }

        GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
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
