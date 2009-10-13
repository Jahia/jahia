/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.definition;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;
import org.jahia.ajax.gwt.client.widget.content.ContentPickerField;

import java.util.*;


/**
 * This is a property editor that allows to edit properties of a JCR node.
 */
public class PropertiesEditor extends FormPanel {

    private List<GWTJahiaNodeType> nodeTypes = null;
    private Map<String, GWTJahiaNodeProperty> currentProperties = null;
    private Map<String, GWTJahiaNodeProperty> originalProperties = null;
    private Map<String, Field<?>> fields;
//    private ComboBox<GWTJahiaNodeType> combo;
//    private GWTJahiaNodeType nodeType;
    private boolean isMultipleEdit = false;
    private boolean viewInheritedItems = false;
    private List<String> excludedItems;
    private List<String> excludedTypes;
    private String dataType;
    private boolean isWriteable;
    private boolean fieldSetGrouping;

    public PropertiesEditor(List<GWTJahiaNodeType> types, Map<String, GWTJahiaNodeProperty> properties, boolean isMultipleEdit, boolean viewInheritedItems, String datatype, List<String> excludedItems, List<String> excludedTypes) {
        this(types, properties, isMultipleEdit, viewInheritedItems, datatype, excludedItems, excludedTypes, true, false);
    }

    public PropertiesEditor(List<GWTJahiaNodeType> types, Map<String, GWTJahiaNodeProperty> properties, boolean isMultipleEdit, boolean viewInheritedItems, String datatype, List<String> excludedItems, List<String> excludedTypes, boolean isWriteable, boolean fieldSetGrouping) {
        super();
        nodeTypes = types;
        this.isMultipleEdit = isMultipleEdit;
        this.dataType = datatype;
        originalProperties = properties;
        cloneProperties();
        this.viewInheritedItems = viewInheritedItems;
        this.excludedItems = excludedItems;
        this.excludedTypes = excludedTypes;
        this.isWriteable = isWriteable;
        this.fieldSetGrouping = fieldSetGrouping;
        renderNewFormPanel();
    }

    public PropertiesEditor(List<GWTJahiaNodeType> types, boolean isMultipleEdit, boolean viewInheritedItems) {
        super();
        nodeTypes = types;
        if (nodeTypes == null) {
            nodeTypes = new ArrayList<GWTJahiaNodeType>();
        }
        this.isMultipleEdit = isMultipleEdit;
        cloneProperties();
        this.viewInheritedItems = viewInheritedItems;
        this.isWriteable = true;
        renderNewFormPanel();
    }

    public void renderNewFormPanel() {
        if (!fieldSetGrouping) {
            setFieldWidth(550);
            setLabelWidth(180);
        }
        setFrame(true);
        setBorders(false);
        setBodyBorder(false);
        setHeaderVisible(false);
        setScrollMode(Style.Scroll.AUTO);
        setButtonAlign(Style.HorizontalAlignment.CENTER);

//        final ListStore<GWTJahiaNodeType> listStore = new ListStore<GWTJahiaNodeType>();
//        listStore.add(nodeTypes);
//        combo = new ComboBox<GWTJahiaNodeType>();
//        combo.setEmptyText("Select a state...");
//        combo.setDisplayField("label");
//        combo.setStore(listStore);
//        combo.setTypeAhead(true);
//        combo.setTriggerAction(ComboBox.TriggerAction.ALL);
//        combo.setWidth(200);
//        combo.addStyleName("gxt-fix-topcomponent-position"); // fix static position given by setTopComponent

//        combo.addListener(Events.SelectionChange, new Listener<SelectionChangedEvent<GWTJahiaNodeType>>() {
//            public void handleEvent(SelectionChangedEvent<GWTJahiaNodeType> event) {
//                Log.debug("event !");
//
//                GWTJahiaNodeType selectedNodeType = event.getSelectedItem();
//                Log.debug("changed ? ");
//                if (nodeType != selectedNodeType) {
//                    nodeType = selectedNodeType;
//                    Log.debug("changed");
//                    renderForm();
//                    layout();
////                    m_callback.rerenderProperties();
//                    Log.debug("end");
//                }
//                Log.debug("out");
//
//            }
//        });
//
//
//        List<GWTJahiaNodeType> s = new ArrayList<GWTJahiaNodeType>();
//        if (nodeTypes.size() > 0) {
//            nodeType = nodeTypes.get(0);
//            s.add(nodeType);
//        }
//        combo.setSelection(s);
//
//        ToolBar toolBar = new ToolBar();
//        toolBar.setVisible(true);
//
//        if (nodeTypes.size() > 1) {
//            toolBar.add(combo);
//            toolBar.setVisible(true);
//        }
//
        setTopComponent(new ToolBar());
        renderForm();
    }

    private void renderForm() {
//        if (combo.getSelection().size() > 0) {
            removeAll();
            fields = new HashMap<String, Field<?>>();

        for (GWTJahiaNodeType nodeType : nodeTypes) {
            if (viewInheritedItems) {
                addItems(nodeType.getInheritedItems());
            }
            addItems(nodeType.getItems());
        }
//        }
    }

    private void addItems(List<GWTJahiaItemDefinition> items) {

        FieldSet fieldSet = null;
        FormPanel sub = this;

        for (GWTJahiaItemDefinition definition : items) {
            if ((excludedTypes != null && excludedTypes.contains(definition.getDeclaringNodeType())) ||
                    (excludedItems != null && excludedItems.contains(definition.getName()))) {
                continue;
            }
            if (dataType != null && !dataType.equals(definition.getDataType())) {
                continue;
            }
            if (!currentProperties.containsKey(definition.getName())) {
                GWTJahiaNodeProperty property = new GWTJahiaNodeProperty();
                property.setName(definition.getName());
                property.setValues(new ArrayList<GWTJahiaNodePropertyValue>());
                if (!definition.isNode()) {
                    property.setMultiple(((GWTJahiaPropertyDefinition) definition).isMultiple());
                }
                if (!definition.isNode() && ((GWTJahiaPropertyDefinition)definition).getDefaultValues() != null) {
                    property.setValues(((GWTJahiaPropertyDefinition)definition).getDefaultValues());
                }
                currentProperties.put(definition.getName(), property);
            }

            final Field field = FormFieldCreator.createField(definition, currentProperties.get(definition.getName()));
            if (field != null) {
                if (fieldSetGrouping && (fieldSet == null || !fieldSet.getId().equals(definition.getDeclaringNodeType()))) {
                    fieldSet = new FieldSet();
                    fieldSet.setId(definition.getDeclaringNodeType());
                    fieldSet.add(field);
                    fieldSet.setCollapsible(true);
                    fieldSet.setHeading(definition.getDeclaringNodeType());
                    fieldSet.setStyleAttribute("padding", "0");
                    sub = new FormPanel();
                    sub.setFieldWidth(500);
                    sub.setLabelWidth(140);
                    sub.setHeaderVisible(false);
                    sub.setFrame(false);
                    setBorders(false);
                    setBodyBorder(false);
                    fieldSet.add(sub);
                    add(fieldSet);
                }
                if (isMultipleEdit && !definition.isProtected()) {
                    field.setEnabled(false);
                    final CheckBox checkbox = new CheckBox();
                    checkbox.addListener(Events.Change, new Listener<ComponentEvent>() {
                        public void handleEvent(ComponentEvent event) {
                            if (checkbox.getValue()) {
                                Log.debug("add ");
                                field.setEnabled(true);
                            } else {
                                Log.debug("remove ");
                                field.setEnabled(false);
                            }
                        }

                    });
                    checkbox.setHideLabel(true);
                    add(checkbox);
                }
                field.setEnabled(isWriteable);
                sub.add(field);
                fields.put(field.getName(), field);
            }
        }
    }

    /**
     * Clone hashmap in order to have a modifiable one.
     */
    private void cloneProperties() {
        currentProperties = new HashMap<String, GWTJahiaNodeProperty>();
        if (originalProperties != null) {
            for (String aProp : originalProperties.keySet()) {
                currentProperties.put(aProp, originalProperties.get(aProp).cloneObject());
            }
        }
    }

    public List<GWTJahiaNodeProperty> getProperties() {
        List<GWTJahiaNodeProperty> newProps = new ArrayList<GWTJahiaNodeProperty>();

        for (GWTJahiaNodeType nodeType : nodeTypes) {
            List<GWTJahiaItemDefinition> allItems = new ArrayList<GWTJahiaItemDefinition>();
            if (viewInheritedItems) {
                allItems.addAll(nodeType.getInheritedItems());
            }
            allItems.addAll(nodeType.getItems());
            
            for (GWTJahiaItemDefinition definition : allItems) {

                if (!definition.isProtected()) {
                    Field f = fields.get(definition.getName());
                    GWTJahiaNodeProperty prop = currentProperties.get(definition.getName());
                    if (f != null && f.isDirty()) {
                        Log.debug("Set value for " + prop.getName());
                        prop.setValues(getPropertyValues(f, definition));
                    }
                    newProps.add(prop);
                }
            }
        }
        return newProps;
    }

    public void resetForm() {
        cloneProperties();
        renderForm();
        layout();
    }
    
    /**
     * Returns a list with property values, populated from the field value depending on its type.
     * 
     * @param fld
     *            the form field, holding the value
     * @param itemDef
     *            the definition of the corresponding node/property
     * @return a list with property values, populated from the field value depending on its type
     */
    private List<GWTJahiaNodePropertyValue> getPropertyValues(Field fld, GWTJahiaItemDefinition itemDef) {
        List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>();
        if (itemDef.isNode()) {
            values.add(new GWTJahiaNodePropertyValue(fld.getValue().toString(), GWTJahiaNodePropertyType.ASYNC_UPLOAD));
        } else {
            GWTJahiaPropertyDefinition propDef = (GWTJahiaPropertyDefinition) itemDef;
            if (fld instanceof ListField) {
                List<GWTJahiaValueDisplayBean> selection = ((ListField<GWTJahiaValueDisplayBean>)fld).getSelection();
                for (GWTJahiaValueDisplayBean valueDisplayBean : selection) {
                    GWTJahiaNodePropertyValue propertyValue = getPropertyValue(valueDisplayBean, propDef.getRequiredType());
                    if (propertyValue != null) {
                        values.add(propertyValue);
                    }
                }
            } else if (fld instanceof ContentPickerField) {
                List<GWTJahiaNode> selection = ((ContentPickerField)fld).getValue();
                for (GWTJahiaNode node : selection) {
                    GWTJahiaNodePropertyValue propertyValue = new GWTJahiaNodePropertyValue(node);
                    values.add(propertyValue);
                }
            } else {
                values.add(getPropertyValue(fld.getValue(), propDef.getRequiredType()));
            }
        }

        return values;
    }
    
    /**
     * Converts the field value into its string representation, considering
     * field type.
     * 
     * @param fieldValue
     *            the form field value
     * @param requiredType
     *            the expected field type
     * @return string representation of the field value, converted based on its
     *         type
     */
    private GWTJahiaNodePropertyValue getPropertyValue(Object fieldValue,
            int requiredType) {
        String propValueString = null;
        if (fieldValue != null) {
            if (fieldValue instanceof Date) {
                propValueString = String.valueOf(((Date) fieldValue).getTime());
            } else if (fieldValue instanceof GWTJahiaValueDisplayBean) {
                propValueString = ((GWTJahiaValueDisplayBean) fieldValue)
                        .getValue();
            } else {
                propValueString = fieldValue.toString();
            }
        }
        return new GWTJahiaNodePropertyValue(propValueString, requiredType);
    }

    public Map<String, Field<?>> getFieldsMap() {
        return fields;
    }
}