/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
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
    private List<GWTJahiaNodeType> mixin = null;
    private Map<String, List<GWTJahiaValueDisplayBean>> initializersValues;
    private Map<String, GWTJahiaNodeProperty> currentProperties = null;
    private Map<String, GWTJahiaNodeProperty> originalProperties = null;
    private Map<String, Field<?>> fields;
    private List<FieldSet> orderingListFieldSet = new ArrayList<FieldSet>();
    private Map<String, GWTJahiaItemDefinition> propertyDefinitions = new HashMap<String, GWTJahiaItemDefinition>();
    private boolean isMultipleEdit = false;
    private boolean viewInheritedItems = true;
    private List<String> excludedItems;
    private List<String> excludedTypes;
    private String dataType;
    private boolean isWriteable = true;
    private boolean fieldSetGrouping = false;
    private Set<String> addedTypes = new HashSet<String>();
    private Set<String> removedTypes = new HashSet<String>();
    private ComboBox<GWTJahiaValueDisplayBean> templateField;
    private Set<String> templateTypes = new HashSet<String>();
    private FieldSet fieldSet = null;
    private ContentPanel commonFields = null;
    private FormPanel form = this;
    private static final int FIELD_SIZE = 620;

    public PropertiesEditor(List<GWTJahiaNodeType> nodeTypes, Map<String, GWTJahiaNodeProperty> properties,
                            String datatype) {
        super();
        this.nodeTypes = nodeTypes;
        this.dataType = datatype;
        originalProperties = properties;
        cloneProperties();
    }

    public void setNodeTypes(List<GWTJahiaNodeType> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void setMixin(List<GWTJahiaNodeType> mixin) {
        this.mixin = mixin;
    }

    public void setInitializersValues(Map<String, List<GWTJahiaValueDisplayBean>> initializersValues) {
        this.initializersValues = initializersValues;
    }

    public void setMultipleEdit(boolean multipleEdit) {
        isMultipleEdit = multipleEdit;
    }

    public void setViewInheritedItems(boolean viewInheritedItems) {
        this.viewInheritedItems = viewInheritedItems;
    }

    public void setExcludedItems(List<String> excludedItems) {
        this.excludedItems = excludedItems;
    }

    public void setExcludedTypes(List<String> excludedTypes) {
        this.excludedTypes = excludedTypes;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setWriteable(boolean writeable) {
        isWriteable = writeable;
    }

    public void setFieldSetGrouping(boolean fieldSetGrouping) {
        this.fieldSetGrouping = fieldSetGrouping;
    }

    public void renderNewFormPanel() {
        if (!fieldSetGrouping) {
            // setFieldWidth(550);
            setLabelWidth(180);
        }
        setFieldWidth(FIELD_SIZE);
        setLabelAlign(LabelAlign.TOP);
        setPadding(5);
        setCollapsible(false);
        setFrame(false);
        setAnimCollapse(false);
        setBorders(false);
        setBodyBorder(false);
        setHeaderVisible(false);
        setScrollMode(Style.Scroll.AUTO);
        setButtonAlign(Style.HorizontalAlignment.CENTER);

        renderForm();
    }

    private void renderForm() {
        removeAll();
        fields = new HashMap<String, Field<?>>();
        List<String> supertypes = new ArrayList<String>();
        for (GWTJahiaNodeType nodeType : nodeTypes) {
            supertypes.addAll(nodeType.getSuperTypes());
            if (mixin != null && mixin.contains(nodeType)) {
                continue;
            }

            if (viewInheritedItems) {
                addItems(form, nodeType, nodeType.getInheritedItems(), false, fieldSetGrouping);
            }
            addItems(form, nodeType, nodeType.getItems(), false, fieldSetGrouping);
        }
        if (mixin != null) {
            for (GWTJahiaNodeType mix : mixin) {
                if (supertypes.contains(mix.getName()) || mix.getSuperTypes().contains("jmix:templateMixin")) {
                    continue;
                }

                if (viewInheritedItems) {
                    addItems(form, mix, mix.getInheritedItems(), true, fieldSetGrouping);
                }

                addItems(form, mix, mix.getItems(), true, fieldSetGrouping);

            }
        }
        if (templateField != null && templateField.getValue() != null) {
            setTemplate((FormPanel) templateField.getParent());
        }


    }


    /**
     * Add item
     *
     * @param form
     * @param nodeType
     * @param items
     * @param optional
     * @param fieldSetGrouping
     */
    private void addItems(FormPanel form, GWTJahiaNodeType nodeType, List<GWTJahiaItemDefinition> items,
                          boolean optional, boolean fieldSetGrouping) {


        for (final GWTJahiaItemDefinition definition : items) {
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
                if (!definition.isNode() && ((GWTJahiaPropertyDefinition) definition).getDefaultValues() != null) {
                    property.setValues(((GWTJahiaPropertyDefinition) definition).getDefaultValues());
                }
                currentProperties.put(definition.getName(), property);
            }


            final GWTJahiaNodeProperty gwtJahiaNodeProperty = currentProperties.get(definition.getName());
            List<GWTJahiaValueDisplayBean> values = initializersValues != null ?
                    initializersValues.get(definition.getDeclaringNodeType() + "." + definition.getName()) : null;
            final Field<?> field = FormFieldCreator.createField(definition, gwtJahiaNodeProperty, values);
            propertyDefinitions.put(gwtJahiaNodeProperty.getName(), definition);
            if (definition.getName().equals("j:template")) {
                templateField = (ComboBox<GWTJahiaValueDisplayBean>) field;
                templateField.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaValueDisplayBean>() {
                    public void selectionChanged(SelectionChangedEvent<GWTJahiaValueDisplayBean> event) {
                        FormPanel form = (FormPanel) templateField.getParent();
                        form.removeAll();
                        form.add(templateField);
                        setTemplate(form);
                        form.layout();
                    }
                });
            }

            if (field != null) {
                if (optional && fieldSetGrouping &&
                        (fieldSet == null || !fieldSet.getId().equals(definition.getDeclaringNodeTypeLabel()))) {
                    setPadding(0);

                    boolean isOrderingList = "jmix:orderedList".equalsIgnoreCase(definition.getDeclaringNodeType());
                    fieldSet = new FieldSet();
                    fieldSet.setId(definition.getDeclaringNodeTypeLabel());
                    fieldSet.add(field);
                    if (isWriteable) {
                        fieldSet.setCollapsible(true);
                        if (!isOrderingList) {
                            fieldSet.setCheckboxToggle(true);
                            if (nodeTypes.contains(nodeType)) {
                                fieldSet.setExpanded(true);
                            } else {
                                fieldSet.setExpanded(false);
                            }
                            fieldSet.addListener(Events.Collapse, new Listener<ComponentEvent>() {
                                public void handleEvent(ComponentEvent componentEvent) {
                                    removedTypes.add(definition.getDeclaringNodeType());
                                    addedTypes.remove(definition.getDeclaringNodeType());
                                    final FormPanel thisForm =
                                            (FormPanel) ((FieldSet) ((FieldSetEvent) componentEvent).getBoxComponent())
                                                    .getItem(0);
                                    for (Component component : thisForm.getItems()) {
                                        component.setData("addedField", null);
                                    }
                                }
                            });
                            fieldSet.addListener(Events.Expand, new Listener<ComponentEvent>() {
                                public void handleEvent(ComponentEvent componentEvent) {
                                    addedTypes.add(definition.getDeclaringNodeType());
                                    removedTypes.remove(definition.getDeclaringNodeType());
                                    final FormPanel thisForm =
                                            (FormPanel) ((FieldSet) ((FieldSetEvent) componentEvent).getBoxComponent())
                                                    .getItem(0);
                                    for (Component component : thisForm.getItems()) {
                                        component.setData("addedField", "true");
                                    }
                                }
                            });
                        }
                    } else {
                        if (!nodeTypes.contains(nodeType)) {
                            fieldSet.setVisible(false);
                        }
                    }
                    fieldSet.setHeading(definition.getDeclaringNodeTypeLabel());
                    //fieldSet.setStyleAttribute("padding", "0");
                    form = new FormPanel();
                    form.setLabelAlign(LabelAlign.TOP);
                    form.setFieldWidth(FIELD_SIZE);
                    form.setLabelWidth(80);
                    form.setHeaderVisible(false);
                    form.setFrame(false);
                    setBorders(false);
                    setBodyBorder(false);
                    fieldSet.add(form);
                    add(fieldSet);

                    if (isOrderingList) {
                        orderingListFieldSet.add(fieldSet);
                    }

                    this.form = form;
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
                if (!isWriteable) {
                    field.setReadOnly(!isWriteable);
                }

                if (optional) {
                    form.add(field);
                } else {
                    addCommonFieldSet(field);
                }
                fields.put(field.getName(), field);
            }
        }
    }

    /**
     * Add to common field set
     *
     * @param field
     */
    private void addCommonFieldSet(Field<?> field) {
        // field set
        if (commonFields == null) {
            FieldSet commonFieldSet = new FieldSet();
            FormLayout layout = new FormLayout(LabelAlign.TOP);
            layout.setLabelWidth(80);
            layout.setDefaultWidth(FIELD_SIZE);

            commonFields = new ContentPanel(layout);
            commonFields.setHeaderVisible(false);
            commonFields.setFrame(false);
            commonFields.setBorders(false);
            commonFields.setBodyBorder(false);

            commonFieldSet.add(commonFields);
            add(commonFieldSet);
//            commonForm = new FormPanel();
//            commonForm.setHeaderVisible(false);
//            commonForm.setFrame(false);
//            commonForm.setBorders(false);
//            commonForm.setBodyBorder(false);
//            commonForm.setLabelAlign(LabelAlign.TOP);
//            commonForm.setFieldWidth(FIELD_SIZE);
//            commonForm.setLabelWidth(80);
//            commonFieldSet.add(commonForm);
//            add(commonFieldSet);
//          add(commonFieldSet);
        }
        commonFields.add(field);

    }

    /**
     * Set template
     *
     * @param form
     */
    private void setTemplate(FormPanel form) {
        String addMixin = templateField.getValue().get("addMixin");
        templateTypes.clear();
        if (addMixin != null && mixin != null) {
            for (GWTJahiaNodeType mix : mixin) {
                if (mix.getName().equals(addMixin)) {
                    templateTypes.add(mix.getName());
                    addItems(form, mix, mix.getItems(), false, false);
                }
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

    /**
     * Get properties
     *
     * @return
     */
    public List<GWTJahiaNodeProperty> getProperties() {
        return getProperties(true, true, false);
    }

    /**
     * Get properties
     *
     * @return
     */
    public List<GWTJahiaNodeProperty> getProperties(boolean includeI18N, boolean includeNonI18N, boolean modifiedOnly) {
        return getProperties(includeI18N, includeNonI18N, modifiedOnly, false);
    }

    public List<GWTJahiaNodeProperty> getProperties(boolean includeI18N, boolean includeNonI18N, boolean modifiedOnly,
                                                    boolean doNotCheckFieldsValues) {
        List<GWTJahiaNodeProperty> newProps = new ArrayList<GWTJahiaNodeProperty>();

        List<GWTJahiaNodeType> l = new ArrayList<GWTJahiaNodeType>(nodeTypes);
        if (mixin != null) {
            for (GWTJahiaNodeType type : mixin) {
                if (addedTypes.contains(type.getName())) {
                    l.add(type);
                }
                if (templateTypes.contains(type.getName())) {
                    l.add(type);
                }
                if (removedTypes.contains(type.getName())) {
                    l.remove(type);
                }
            }
        }
        for (GWTJahiaNodeType nodeType : l) {
            List<GWTJahiaItemDefinition> allItems = new ArrayList<GWTJahiaItemDefinition>();
            if (viewInheritedItems) {
                allItems.addAll(nodeType.getInheritedItems());
            }
            allItems.addAll(nodeType.getItems());

            for (GWTJahiaItemDefinition definition : allItems) {
                boolean i18nProp = (definition instanceof GWTJahiaPropertyDefinition &&
                        ((GWTJahiaPropertyDefinition) definition).isInternationalized());
                if ((includeI18N && i18nProp) || (includeNonI18N && !i18nProp)) {
                    if ((definition.isHidden() && originalProperties.get(definition.getName()) != null) ||
                            (dataType != null && dataType.equals(definition.getDataType()))) {
                        if (!definition.isProtected()) {
                            Field<?> f = fields.get(definition.getName());
                            GWTJahiaNodeProperty prop = currentProperties.get(definition.getName());
                            if (!doNotCheckFieldsValues && f != null &&
                                    (f.isDirty() || !modifiedOnly || f.getData("addedField") != null)) {
                                Log.debug("Set value for " + prop.getName());
                                prop.setValues(getPropertyValues(f, definition));
                                newProps.add(prop);
                            } else {
                                newProps.add(prop);
                            }
                        }
                    }
                }
            }
        }
        return newProps;
    }

    public Set<String> getAddedTypes() {
        return addedTypes;
    }

    public Set<String> getRemovedTypes() {
        return removedTypes;
    }

    public Set<String> getTemplateTypes() {
        return templateTypes;
    }

    public List<GWTJahiaNodeType> getNodeTypes() {
        return nodeTypes;
    }

    /**
     * Reset form
     */
    public void resetForm() {
        cloneProperties();
        renderForm();
        layout();
    }

    /**
     * Returns a list with property values, populated from the field value depending on its type.
     *
     * @param fld     the form field, holding the value
     * @param itemDef the definition of the corresponding node/property
     * @return a list with property values, populated from the field value depending on its type
     */
    public static List<GWTJahiaNodePropertyValue> getPropertyValues(Field<?> fld, GWTJahiaItemDefinition itemDef) {
        List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>();
        if (itemDef.isNode()) {
            // case of a new link node
            if (fld instanceof ContentPickerField) {
                ContentPickerField pck = (ContentPickerField) fld;
                List<GWTJahiaNode> selection = pck.getValue();
                for (GWTJahiaNode node : selection) {
                    values.add(new GWTJahiaNodePropertyValue(node, GWTJahiaNodePropertyType.PAGE_LINK));
                }
            }
            // case of a file upload
            else {
                values.add(new GWTJahiaNodePropertyValue(fld.getValue().toString(),
                        GWTJahiaNodePropertyType.ASYNC_UPLOAD));
            }
        } else {
            GWTJahiaPropertyDefinition propDef = (GWTJahiaPropertyDefinition) itemDef;
            // case of a list property
            if (fld instanceof DualListField) {
                List<GWTJahiaValueDisplayBean> selection =
                        ((DualListField<GWTJahiaValueDisplayBean>) fld).getToList().getStore().getModels();
                for (GWTJahiaValueDisplayBean valueDisplayBean : selection) {
                    GWTJahiaNodePropertyValue propertyValue =
                            getPropertyValue(valueDisplayBean, propDef.getRequiredType());
                    if (propertyValue != null) {
                        values.add(propertyValue);
                    }
                }
            }
            // case of a reference
            else if (fld instanceof ContentPickerField) {
                ContentPickerField pck = (ContentPickerField) fld;
                List<GWTJahiaNode> selection = pck.getValue();
                for (GWTJahiaNode node : selection) {
                    GWTJahiaNodePropertyValue propertyValue =
                            new GWTJahiaNodePropertyValue(node, propDef.getRequiredType());
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
     * @param fieldValue   the form field value
     * @param requiredType the expected field type
     * @return string representation of the field value, converted based on its
     *         type
     */
    private static GWTJahiaNodePropertyValue getPropertyValue(Object fieldValue, int requiredType) {
        String propValueString = null;
        if (fieldValue != null) {
            if (fieldValue instanceof Date) {
                propValueString = String.valueOf(((Date) fieldValue).getTime());
            } else if (fieldValue instanceof GWTJahiaValueDisplayBean) {
                propValueString = ((GWTJahiaValueDisplayBean) fieldValue).getValue();
            } else {
                propValueString = fieldValue.toString();
            }
        }
        return new GWTJahiaNodePropertyValue(propValueString, requiredType);
    }

    /**
     * Get fields map
     *
     * @return
     */
    public Map<String, Field<?>> getFieldsMap() {
        return fields;
    }

    /**
     * retrieve  GWTJahiaItemDefinition
     *
     * @param prop
     * @return
     */
    public GWTJahiaItemDefinition getGWTJahiaItemDefinition(GWTJahiaNodeProperty prop) {
        return propertyDefinitions.get(prop.getName());
    }

    public List<FieldSet> getOrderingListFieldSet() {
        return orderingListFieldSet;
    }
}