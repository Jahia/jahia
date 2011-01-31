/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

import org.jahia.ajax.gwt.client.data.GWTJahiaFieldInitializer;
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
    private Map<String, GWTJahiaFieldInitializer> initializersValues;
    private Map<String, GWTJahiaNodeProperty> currentProperties = null;
    private Map<String, GWTJahiaNodeProperty> originalProperties = null;
    private Map<String, Field<?>> fields;
    private Map<String, FieldSet> fieldSets;
    private List<FieldSet> orderingListFieldSet = new ArrayList<FieldSet>();
    private Map<String, GWTJahiaItemDefinition> propertyDefinitions = new HashMap<String, GWTJahiaItemDefinition>();
    private boolean isMultipleEdit = false;
    private boolean viewInheritedItems = true;
    private List<String> excludedItems;
    private List<String> excludedTypes;
    private List<String> dataType;
    private boolean isWriteable = true;
    private boolean isNonI18NWriteable = true;
    private boolean fieldSetGrouping = false;
    private Set<String> addedTypes = new HashSet<String>();
    private Set<String> removedTypes = new HashSet<String>();
    private Set<String> externalMixin = new HashSet<String>();

    public PropertiesEditor(List<GWTJahiaNodeType> nodeTypes, Map<String, GWTJahiaNodeProperty> properties,
                            List<String> datatype) {
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

    public void setInitializersValues(Map<String, GWTJahiaFieldInitializer> initializersValues) {
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

    public void setWriteable(boolean writeable) {
        isWriteable = writeable;
    }

    public void setNonI18NWriteable(boolean nonI18NWriteable) {
        isNonI18NWriteable = nonI18NWriteable;
    }

    public void setFieldSetGrouping(boolean fieldSetGrouping) {
        this.fieldSetGrouping = fieldSetGrouping;
    }

    public void renderNewFormPanel() {
        setLabelWidth(180);
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
        removeAll();
        fields = new HashMap<String, Field<?>>();
        fieldSets = new HashMap<String, FieldSet>();
        List<String> supertypes = new ArrayList<String>();
        for (GWTJahiaNodeType nodeType : nodeTypes) {
            supertypes.addAll(nodeType.getSuperTypes());
            if (mixin != null && mixin.contains(nodeType)) {
                continue;
            }

            if (viewInheritedItems) {
                addItems(nodeType, nodeType.getInheritedItems(), false, fieldSetGrouping, null);
            }
            addItems(nodeType, nodeType.getItems(), false, fieldSetGrouping, null);
        }
        if (mixin != null) {
            for (GWTJahiaNodeType mix : mixin) {
                if (supertypes.contains(mix.getName()) || mix.getSuperTypes().contains("jmix:templateMixin")) {
                    continue;
                }

                if (viewInheritedItems) {
                    addItems(mix, mix.getInheritedItems(), true, fieldSetGrouping, null);
                }

                addItems(mix, mix.getItems(), true, fieldSetGrouping, null);

            }
        }
    }


    /**
     * Add item
     *
     * @param nodeType
     * @param items
     * @param optional
     * @param fieldSetGrouping
     */
    private void addItems(GWTJahiaNodeType nodeType, List<GWTJahiaItemDefinition> items,
                          boolean optional, boolean fieldSetGrouping, FieldSet remoteFieldSet) {

        FieldSet fieldSet = null;
        if (remoteFieldSet != null) {
            fieldSet = remoteFieldSet;
        }
        for (final GWTJahiaItemDefinition definition : items) {

            final FormLayout fl = new FormLayout();
            fl.setLabelWidth(0);

            if ((excludedTypes != null && excludedTypes.contains(definition.getDeclaringNodeType())) ||
                    (excludedItems != null && excludedItems.contains(definition.getName()))) {
                continue;
            }
            if (dataType != null && !dataType.isEmpty() && !dataType.contains(definition.getDataType())) {
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
            GWTJahiaFieldInitializer fieldInitializer = initializersValues != null ?
                    initializersValues.get(definition.getOverrideDeclaringNodeType() + "." + definition.getName()) : null;
            Field<?> field = FormFieldCreator.createField(definition, gwtJahiaNodeProperty, fieldInitializer != null ? fieldInitializer.getDisplayValues() : null);
            propertyDefinitions.put(gwtJahiaNodeProperty.getName(), definition);
            if (field != null) {
                if (fieldSet == null || fieldSetGrouping &&
                        (!fieldSet.getId().equals(definition.getDeclaringNodeTypeLabel()))) {
                    fieldSet = new FieldSet();
                    fieldSet.setId(definition.getDeclaringNodeTypeLabel());
                    fieldSet.setHeading(definition.getDeclaringNodeTypeLabel());
                    fieldSet.setLayout(fl);
                    fieldSets.put(definition.getDeclaringNodeType(), fieldSet);
                    add(fieldSet);
                }
                if (!isWriteable || (!isNonI18NWriteable && !definition.isNode() && !((GWTJahiaPropertyDefinition)definition).isInternationalized())) {
                    field.setReadOnly(true);
                } else if (isMultipleEdit && !definition.isProtected()) {
                    field.setEnabled(false);
                    final CheckBox checkbox = new CheckBox();

                    final Field f = field;
                    checkbox.addListener(Events.Change, new Listener<ComponentEvent>() {
                        public void handleEvent(ComponentEvent event) {
                            if (checkbox.getValue()) {
                                Log.debug("add ");
                                f.setEnabled(true);
                            } else {
                                Log.debug("remove ");
                                f.setEnabled(false);
                            }
                        }

                    });
                    checkbox.setHideLabel(true);
                    add(checkbox);
                    final HBoxLayout hBoxLayout = new HBoxLayout();
                    hBoxLayout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
                    final LayoutContainer panel = new LayoutContainer(hBoxLayout);
                    panel.add(checkbox, new HBoxLayoutData());
                    final HBoxLayoutData data = new HBoxLayoutData();
                    panel.add(field, data);
                    field = new AdapterField(panel) {
                        public Object getValue() {
                            return f.getValue();
                        }

                        public boolean isDirty() {
                            return f.isDirty() && checkbox.getValue();
                        }

                        public void setVisible(boolean visible) {
                            super.setVisible(visible);
                            f.setVisible(visible);
                        }

                        public void setEnabled(boolean enabled) {
                            super.setEnabled(enabled);
                            f.setEnabled(enabled);
                        }
                    };
                    field.setName(f.getName());
                    field.setFieldLabel(f.getFieldLabel());
                }
                field.setWidth("98%");
                field.setStyleAttribute("padding-left", "0");
                fields.put(field.getName(), field);
                FormData fd = new FormData("98%");
                fd.setMargins(new Margins(0));
                fieldSet.add(field, fd);
                fieldSet.layout();
                if (optional) {
                    boolean isOrderingList = "jmix:orderedList".equalsIgnoreCase(definition.getDeclaringNodeType());
                    if (isWriteable) {
                        fieldSet.setCollapsible(true);
                        if (!isOrderingList) {
                            fieldSet.setCheckboxToggle(true);
                            if ((nodeTypes.contains(nodeType) && !removedTypes.contains(nodeType.getName())) || addedTypes.contains(nodeType.getName())) {
                                fieldSet.setExpanded(true);
                            } else {
                                fieldSet.setExpanded(false);
                            }
                            fieldSet.addListener(Events.Collapse, new Listener<ComponentEvent>() {
                                public void handleEvent(ComponentEvent componentEvent) {
                                    removedTypes.add(definition.getDeclaringNodeType());
                                    addedTypes.remove(definition.getDeclaringNodeType());
                                    final FieldSet  fs = (FieldSet) ((FieldSetEvent) componentEvent).getBoxComponent();
                                    for (Component component : fs.getItems()) {
                                        component.setData("addedField", null);
                                    }
                                }
                            });
                            fieldSet.addListener(Events.Expand, new Listener<ComponentEvent>() {
                                public void handleEvent(ComponentEvent componentEvent) {
                                    addedTypes.add(definition.getDeclaringNodeType());
                                    removedTypes.remove(definition.getDeclaringNodeType());
                                    final FieldSet fs = (FieldSet) ((FieldSetEvent) componentEvent).getBoxComponent();
                                    final List<Component> w = new ArrayList<Component>();
                                    w.addAll(fs.getItems());
                                    fs.removeAll();
                                    DeferredCommand.addCommand(new Command() {
                                        public void execute() {
                                            for (Component component : w) {
                                                component.setWidth("98%");
                                                fs.add(component);
                                                component.setData("addedField", "true");
                                            }
                                            fs.layout();
                                        }
                                    });
                                }
                            });
                        }
                    } else {
                        if (!nodeTypes.contains(nodeType)) {
                            fieldSet.setVisible(false);
                        }
                    }
                    if (isOrderingList) {
                        orderingListFieldSet.add(fieldSet);
                    }
                }
                if (field instanceof ComboBox) {
                    final ComboBox<GWTJahiaValueDisplayBean> c = (ComboBox<GWTJahiaValueDisplayBean>) field;
                    c.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaValueDisplayBean>() {
                        public void selectionChanged(SelectionChangedEvent<GWTJahiaValueDisplayBean> event) {
                            setExternalMixin(c, true);
                        }
                    });
                    if (c.getValue() != null) {
                        setExternalMixin(c, false);
                    }
                }
            }
        }
    }

    /**
     * Set template
     *
     */
    private void setExternalMixin(ComboBox<GWTJahiaValueDisplayBean> c, boolean b) {
        String addMixin = null;
        if (c.getValue() != null) {
            addMixin = c.getValue().get("addMixin");
        }
        if (addMixin != null && mixin != null) {
            for (GWTJahiaNodeType mix : mixin) {
                if (mix.getName().equals(addMixin)) {
                    if (!b || !externalMixin.contains(addMixin)) {
                        externalMixin.add(mix.getName());
                        addItems(mix, mix.getItems(), false, false, (FieldSet) c.getParent());
                    }
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
                if (externalMixin.contains(type.getName())) {
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
                            (dataType != null && (dataType.isEmpty() || dataType.contains(definition.getDataType())))) {
                        if (!definition.isProtected()) {
                            Field<?> f = fields.get(definition.getName());
                            GWTJahiaNodeProperty prop = currentProperties.get(definition.getName());
                            if (!doNotCheckFieldsValues && f != null &&
                                    (f.isDirty() || !modifiedOnly || f.getData("addedField") != null)) {
                                Log.debug("Set value for " + prop.getName());
                                prop.setValues(getPropertyValues(f, definition));
                                newProps.add(prop);
//                            } else {
//                                newProps.add(prop);
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

    public Set<String> getExternalMixin() {
        return externalMixin;
    }

    public List<GWTJahiaNodeType> getNodeTypes() {
        return nodeTypes;
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

    public Map<String, FieldSet> getFieldSetsMap() {
        return fieldSets;
    }

    /**
     * retrieve  GWTJahiaItemDefinition
     *
     * @param prop
     * @return
     */
    public GWTJahiaItemDefinition getGWTJahiaItemDefinition(GWTJahiaNodeProperty prop) {
        return getGWTJahiaItemDefinition(prop.getName());
    }
    
    public GWTJahiaItemDefinition getGWTJahiaItemDefinition(String propName) {
        return propertyDefinitions.get(propName);
    }

    public List<FieldSet> getOrderingListFieldSet() {
        return orderingListFieldSet;
    }
}