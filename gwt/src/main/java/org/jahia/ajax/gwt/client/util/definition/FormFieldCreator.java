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

package org.jahia.ajax.gwt.client.util.definition;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.widget.content.ContentPickerField;
import org.jahia.ajax.gwt.client.widget.form.CKEditorField;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;
import org.jahia.ajax.gwt.client.widget.form.FileUploadField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This class creates fields for a GXT form panel based on available jcr types and a specific mapping.
 */
public class FormFieldCreator {

    public static final DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm");

    /**
     * Create Field
     *
     * @param definition
     * @param property
     * @param initializerValues
     * @return
     */
    public static Field createField(GWTJahiaItemDefinition definition, GWTJahiaNodeProperty property,
                                    List<GWTJahiaValueDisplayBean> initializerValues) {
        Field field = null;
        if (definition.isHidden()) {
            return null;
        }
        if (definition.getName().equals("*")) {
            return null;
        }
        if (!definition.isNode()) {
            GWTJahiaPropertyDefinition propDefinition = (GWTJahiaPropertyDefinition) definition;
            switch (definition.getSelector()) {
                case GWTJahiaNodeSelectorType.SMALLTEXT:
                    switch (propDefinition.getRequiredType()) {
                        case GWTJahiaNodePropertyType.LONG:
                            field = new NumberField();
                            ((NumberField) field).setAllowDecimals(false);
                            ((NumberField) field).setPropertyEditorType(Long.class);
                            break;
                        case GWTJahiaNodePropertyType.DOUBLE:
                            field = new NumberField();
                            ((NumberField) field).setAllowDecimals(true);
                            break;
                        default:
                            if (propDefinition.getSelectorOptions().get("multiline") != null) {
                                field = new TextArea();
                            } else {
                                field = new TextField();
                            }
                            break;
                    }
                    break;
                case GWTJahiaNodeSelectorType.RICHTEXT:
                    field = new CKEditorField();
                    field.setAutoWidth(false);
                    field.setAutoHeight(false);
                    field.setHeight(300);
                    break;
                case GWTJahiaNodeSelectorType.DATETIMEPICKER:
                    field = new CalendarField();
                    if (propDefinition.getSelectorOptions().get("format") != null) {
                        ((CalendarField) field).getPropertyEditor()
                                .setFormat(DateTimeFormat.getFormat(propDefinition.getSelectorOptions().get("format")));
                    } else {
                        ((CalendarField) field).getPropertyEditor().setFormat(dateFormat);
                        ((CalendarField) field).setHideTrigger(propDefinition.isProtected());
                    }
                    break;
                case GWTJahiaNodeSelectorType.DATEPICKER:
                    field = new DateField();
                    if (propDefinition.getSelectorOptions().get("format") != null) {
                        ((DateField) field).getPropertyEditor()
                                .setFormat(DateTimeFormat.getFormat(propDefinition.getSelectorOptions().get("format")));
                    } else {
                        ((DateField) field).getPropertyEditor().setFormat(dateFormat);
                    }
                    ((DateField) field).setHideTrigger(propDefinition.isProtected());
                    break;
                case GWTJahiaNodeSelectorType.CHECKBOX:
                    field = new CheckBox();
                    // hack to align check box to rigth
                    ((CheckBox) field).setBoxLabel(definition.getLabel());

                    break;
                case GWTJahiaNodeSelectorType.COLOR:
                    break;
                case GWTJahiaNodeSelectorType.CATEGORY:
                    field = new ContentPickerField(definition.getSelectorOptions(), null, null,
                            ManagerConfigurationFactory.CATEGORYPICKER, propDefinition.isMultiple());
                    break;
                case GWTJahiaNodeSelectorType.PICKER:
                    field = new ContentPickerField(definition.getSelectorOptions(),
                            getSelectorOptionAsList(definition, "filters"), getSelectorOptionAsList(definition, "mime"),
                            definition.getSelectorOptions().get("type") != null ?
                                    definition.getSelectorOptions().get("type")+"picker" :
                                    ManagerConfigurationFactory.EDITORIALCONTENTPICKER, propDefinition.isMultiple());
                    break;

                case GWTJahiaNodeSelectorType.CHOICELIST:
                    ListStore<GWTJahiaValueDisplayBean> store = new ListStore<GWTJahiaValueDisplayBean>();
                    if (initializerValues != null) {
                        store.add(initializerValues);
                    } else if (propDefinition.getValueConstraints() != null) {
                        for (String s : propDefinition.getValueConstraints()) {
                            store.add(new GWTJahiaValueDisplayBean(s, s));
                        }
                    }
                    if (propDefinition.isMultiple()) {

                        final CustomDualListField<GWTJahiaValueDisplayBean> lists =
                                new CustomDualListField<GWTJahiaValueDisplayBean>();
                        ListField<GWTJahiaValueDisplayBean> from = lists.getFromList();
                        from.setStore(store);
                        from.setDisplayField("display");
                        ListField<GWTJahiaValueDisplayBean> to = lists.getToList();
                        to.setDisplayField("display");
                        ListStore<GWTJahiaValueDisplayBean> tostore = new ListStore<GWTJahiaValueDisplayBean>();
                        to.setStore(tostore);
                        field = lists;
                    } else {
                        ComboBox<GWTJahiaValueDisplayBean> combo = new ComboBox<GWTJahiaValueDisplayBean>();
                        combo.setStore(store);
                        combo.setDisplayField("display");
                        combo.setTypeAhead(true);
                        combo.setTriggerAction(TriggerAction.ALL);
                        combo.setForceSelection(true);
                        if (propDefinition.getSelectorOptions() != null &&
                                (propDefinition.getSelectorOptions().containsKey("image") ||
                                        propDefinition.getSelectorOptions().containsKey("moduleImage"))) {
                            combo.setTemplate(getComboTemplate());
                        }
                        field = combo;
                    }

                    // if there is no values, the field is hidden
                    if (store.getModels().isEmpty()) {
                        field.setVisible(false);
                    }

                    break;
                default:
                    if (propDefinition.getRequiredType() == GWTJahiaNodePropertyType.REFERENCE ||
                            propDefinition.getRequiredType() == GWTJahiaNodePropertyType.WEAKREFERENCE) {
                        field = new ContentPickerField(definition.getSelectorOptions(), getSelectorOptionAsList(definition, "filters"),
                                getSelectorOptionAsList(definition, "mime"), ManagerConfigurationFactory.EDITORIALCONTENTPICKER,
                                propDefinition.isMultiple());
                    } else {
                        field = new TextField();
                    }
                    break;
            }
            if (propDefinition.isInternationalized()) {
                field.setLabelSeparator(" <img width='11px' height='11px' src='" + JahiaGWTParameters.getContextPath() +
                        "/css/images/sharedLang.gif'/>:");
            }
        } else {
            GWTJahiaNodeDefinition nodeDefinition = (GWTJahiaNodeDefinition) definition;
            if (nodeDefinition.getName().equals("jcr:content") ||
                    nodeDefinition.getRequiredPrimaryTypes()[0].equals("nt:resource") ||
                    nodeDefinition.getRequiredPrimaryTypes()[0].equals("jnt:resource")) {
                field = new FileUploadField(definition.getName());
            }
        }
        if (field == null) {
            return null;
        }
        setModifiers(field, definition);
        if (property != null) {
            fillValue(field, definition, property);
        }
        return field;
    }

    private static List<String> getSelectorOptionAsList(GWTJahiaItemDefinition definition, String name) {
        String s = definition.getSelectorOptions().get(name);
        if (s == null) {
            return null;
        }
        return Arrays.asList(s.split(","));
    }

    /**
     * set modifiers
     *
     * @param field
     * @param definition
     */
    public static void setModifiers(Field field, GWTJahiaItemDefinition definition) {
        if (field == null || definition == null) {
            return;
        }
        field.setName(definition.getName());
        field.setFieldLabel(definition.getLabel());
        field.setReadOnly(field.isReadOnly() || definition.isProtected());
        if (field instanceof TextField) {
            TextField tField = (TextField) field;
            tField.setAllowBlank(!definition.isMandatory());
        }

        if (field instanceof CheckBox) {
            field.setHideLabel(true);
            ((CheckBox) field).setBoxLabel(field.getFieldLabel());
        }
    }

    /**
     * fill value
     *
     * @param field
     * @param definition
     * @param property
     */
    public static void fillValue(final Field field, GWTJahiaItemDefinition definition, GWTJahiaNodeProperty property) {
        List<GWTJahiaNodePropertyValue> values = property.getValues();
        if (values.size() == 0) {
            return;
        }
        if (!definition.isNode()) {
            GWTJahiaPropertyDefinition propDefinition = (GWTJahiaPropertyDefinition) definition;
            if (propDefinition.getSelector() == GWTJahiaNodeSelectorType.CHOICELIST) {
                List<GWTJahiaValueDisplayBean> selection = new ArrayList<GWTJahiaValueDisplayBean>();
                if (propDefinition.isMultiple()) {
                    final CustomDualListField<GWTJahiaValueDisplayBean> list =
                            (CustomDualListField<GWTJahiaValueDisplayBean>) field;
                    final List<GWTJahiaValueDisplayBean> displayBeans =
                            new ArrayList<GWTJahiaValueDisplayBean>(list.getFromList().getStore().getModels());

                    for (GWTJahiaNodePropertyValue jahiaNodePropertyValue : values) {
                        String val = jahiaNodePropertyValue.getString();
                        if (val != null && val.length() > 0) {
                            for (GWTJahiaValueDisplayBean displayBean : displayBeans) {
                                if (displayBean.getValue().equals(val)) {
                                    selection.add(displayBean);
                                    list.getFromList().getStore().remove(displayBean);
                                }
                            }
                        }
                    }
                    final ListStore<GWTJahiaValueDisplayBean> store = list.getToList().getStore();
                    store.add(selection);
                    list.setCustomOriginalValue(selection);
                } else {
                    final ComboBox<GWTJahiaValueDisplayBean> combo = (ComboBox<GWTJahiaValueDisplayBean>) field;
                    String val = values.get(0).getString();
                    for (GWTJahiaValueDisplayBean displayBean : combo.getStore().getModels()) {
                        if (displayBean.getValue().equals(val)) {
                            selection.add(displayBean);
                        }
                    }
                    combo.setSelection(selection);
                }
            } else {
                switch (propDefinition.getRequiredType()) {
                    case GWTJahiaNodePropertyType.BINARY:
                        break;
                    case GWTJahiaNodePropertyType.BOOLEAN:
                        field.setValue(values.get(0).getBoolean());
                        break;
                    case GWTJahiaNodePropertyType.LONG:
                        field.setValue(values.get(0).getLong());
                        break;
                    case GWTJahiaNodePropertyType.DOUBLE:
                        field.setValue(values.get(0).getDouble());
                        break;
                    case GWTJahiaNodePropertyType.DECIMAL:
                        field.setValue(values.get(0).getDecimal());
                        break;
                    case GWTJahiaNodePropertyType.DATE:
                        Date d = values.get(0).getDate();
                        Log.debug("date: " + d);
                        field.setValue(d);
                        break;
                    case GWTJahiaNodePropertyType.STRING:
                    case GWTJahiaNodePropertyType.NAME:
                    case GWTJahiaNodePropertyType.PATH:
                    case GWTJahiaNodePropertyType.URI:
                    case GWTJahiaNodePropertyType.UNDEFINED:
                        if (values.get(0).getString() != null) {
                            field.setValue(join(values));
                        }
                        break;
                    case GWTJahiaNodePropertyType.REFERENCE:
                    case GWTJahiaNodePropertyType.WEAKREFERENCE:
                        List<GWTJahiaNode> v = new ArrayList<GWTJahiaNode>();
                        for (GWTJahiaNodePropertyValue value : values) {
                            v.add(value.getNode());
                        }
                        field.setValue(v);
                        break;
                    default:
                }
            }
        } else {
            GWTJahiaNodeDefinition nodeDefinition = (GWTJahiaNodeDefinition) definition;
            if (nodeDefinition.getName().equals("jcr:content") ||
                    nodeDefinition.getRequiredPrimaryTypes()[0].equals("nt:resource") ||
                    nodeDefinition.getRequiredPrimaryTypes()[0].equals("jnt:resource")) {
                field.setValue(values.get(0).getString());
            } else if (nodeDefinition.getRequiredPrimaryTypes()[0].equals("jmix:link")) {
                List<GWTJahiaNode> v = new ArrayList<GWTJahiaNode>();
                for (GWTJahiaNodePropertyValue value : values) {
                    v.add(value.getLinkNode());
                }
                field.setValue(v);
            }

        }
    }

    /**
     * Returns values in a string separetad by a ','
     *
     * @param values
     * @return
     */
    private static String join(List<GWTJahiaNodePropertyValue> values) {
        StringBuilder str = new StringBuilder(values.get(0).getString());
        for (int i = 1; i < values.size(); i++) {
            str.append(", ");
            str.append(values.get(i).getString());
        }

        return str.toString();
    }


    private static native String getComboTemplate()  /*-{
    return  [
    '<tpl for=".">',
    '<div class="x-combo-list-item"><img src="{image}"/> {display}</div>',
    '</tpl>'
    ].join("");
  }-*/;

    private static class CustomDualListField<D extends ModelData> extends DualListField<D> {
        private List<D> originalValue = new ArrayList<D>();

        public void setCustomOriginalValue(List<D> originalValue) {
            this.originalValue = originalValue;
        }

        @Override
        public boolean isDirty() {
            return !originalValue.equals(getToList().getStore().getModels());
        }

        @Override
        protected void onRender(Element target, int index) {
            super.onRender(target, index);
            getFromList().getListView().el().makePositionable();
            getToList().getListView().el().makePositionable();
        }
    }
}
