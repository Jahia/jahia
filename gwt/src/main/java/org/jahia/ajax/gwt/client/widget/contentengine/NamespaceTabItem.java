package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

import java.util.List;

/**
 * Definition Tab Item
 * used to edit definition properties
 * same as ContentTabItem but change the behavior of the name field
 * User: david
 * Date: 5/13/13
 * Time: 11:05 AM
 */
public class NamespaceTabItem extends ContentTabItem {

    @Override
    protected void setNameField(NodeHolder engine, AsyncTabItem tab) {
        if (!engine.isMultipleSelection()) {
            tab.setLayout(new RowLayout());
            final FormLayout fl = new FormLayout();
            fl.setLabelWidth(0);
            final HBoxLayout hBoxLayout = new HBoxLayout();
            hBoxLayout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
            final LayoutContainer panel = new LayoutContainer(hBoxLayout);
            if (nameText == null) {
                nameFieldSet = new FieldSet();
                nameFieldSet.setHeading(Messages.get("label.type.name", "Type name"));
                nameFieldSet.setLayout(fl);
                nameText = new NameField();
                nameText.addStyleName("JahiaGxtField_systemName");
                nameText.setWidth("250");
                ((NameField) nameText).setMaxLength(maxNameSize);
                ((NameField) nameText).setAllowBlank(false);
                nameText.setStyleAttribute("padding-left", "0");
                nameText.setFireChangeEventOnSetValue(true);
                panel.add(this.nameText, new HBoxLayoutData(0, 5, 0, 5));

                name = new AdapterField(panel);
                name.setFieldLabel(Messages.get("label.type.name", "Type name"));

                FormData fd = new FormData("98%");
                fd.setMargins(new Margins(0));
                nameFieldSet.add(name, fd);
                if (engine.getNode() != null && engine.getNode().isLocked()) {
                    nameText.setReadOnly(true);
                    if (autoUpdateName != null) {
                        autoUpdateName.setEnabled(false);
                    }
                }
            }

            String nodeName = engine.getNodeName();
            if (nodeName != null) {
                if (nodeName.equals("primary-nodetype")) {
                    nodeName = "jnt:newNodeType";
                } else if (nodeName.equals("mixin-nodetype")) {
                    nodeName = "jmix:newMixin";
                }
            }

            nameText.setValue(nodeName);
        }
    }

    private class NameField extends MultiField<String> {
        private transient SimpleComboBox<String> namespaces;
        private transient TextField<String> name;

        public NameField() {
            namespaces = new SimpleComboBox<String>();
            namespaces.setTriggerAction(ComboBox.TriggerAction.ALL);
            name = new TextField<String>();
            JahiaContentManagementService.App.getInstance().getNamespaces(
                    new AsyncCallback<List<String>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            //it does not work
                        }

                        @Override
                        public void onSuccess(List<String> result) {
                            result.remove(namespaces.getSimpleValue());
                            namespaces.add(result);
                        }
                    }
            );
            add(namespaces);
            add(name);
        }

        @Override
        public String getValue() {
            return namespaces.getSimpleValue() == null || namespaces.getSimpleValue().trim().equals("")?name.getValue():namespaces.getSimpleValue() + ":" + name.getValue();
        }

        @Override
        public void setValue(String value) {
            if (value != null && value.contains(":")) {
                String[] s = value.split(":");
                namespaces.add(s[0]);
                namespaces.setSimpleValue(s[0]);
                namespaces.select(namespaces.getStore().findModel(s[0]));
                name.setValue(s[1]);
            } else {
                name.setValue(value);
            }
        }

        public void setMaxLength(int maxLength) {
            name.setMaxLength(maxLength);
        }

        public void setAllowBlank(boolean allowBlank) {
            name.setAllowBlank(allowBlank);
        }
    }
}
