package org.jahia.ajax.gwt.client.widget.definitionsmodeler;

import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.EditEngineTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.NodeHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseAttributesTabItem extends EditEngineTabItem {
    private static final long serialVersionUID = 1L;

    public BaseAttributesTabItem() {
    }

    @Override
    public void init(NodeHolder engine, AsyncTabItem tab, String language) {
        FormPanel form = new FormPanel();

        GWTJahiaNodeType type = engine.getNodeTypes().get(0);
        Map<String,GWTJahiaItemDefinition> definitionMap = new HashMap<String, GWTJahiaItemDefinition>();
        for (GWTJahiaItemDefinition definition : type.getItems()) {
            definitionMap.put(definition.getName(), definition);
        }
        Map<String, GWTJahiaNodeProperty> propertyMap = engine.getProperties();

        TextField<String> prefixText = new TextField<String>();
        prefixText.setName("prefix");
        prefixText.setFieldLabel(Messages.get("label.prefix"));
        prefixText.setWidth("250");
        prefixText.setAllowBlank(false);
        form.add(prefixText);

        TextField<String> nameText = new TextField<String>();
        nameText.setName("name");
        nameText.setFieldLabel(Messages.get("label.name"));
        nameText.setWidth("250");
        nameText.setAllowBlank(false);
        form.add(nameText);

        Field f = FormFieldCreator.createField(definitionMap.get("jcr:isMixin"), propertyMap.get("jcr:isMixin"), null, true, engine.getTargetNode().getPermissions(), language);
        form.add(f);

        tab.add(form);
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, GWTJahiaNodeACL acl) {
        super.doSave(node, changedProperties, changedI18NProperties, addedTypes, removedTypes, acl);
    }

    public void setProcessed(boolean processed) {
        if (!processed) {
        }

        super.setProcessed(processed);
    }

}
