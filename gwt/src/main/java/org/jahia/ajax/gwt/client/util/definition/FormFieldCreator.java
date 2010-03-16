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
package org.jahia.ajax.gwt.client.util.definition;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.i18n.client.DateTimeFormat;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;
import org.jahia.ajax.gwt.client.widget.form.FileUploadField;
import org.jahia.ajax.gwt.client.widget.form.CKEditorField;
import org.jahia.ajax.gwt.client.widget.content.ContentPickerField;
import org.jahia.ajax.gwt.client.widget.ckeditor.CKEditor;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class creates fields for a GXT form panel based on available jcr types and a specific mapping.
 */
public class FormFieldCreator {

    public static class Selector {
        public static final int SMALLTEXT = 1;
        public static final int RICHTEXT = 2;
        public static final int DATETIMEPICKER = 3;
        public static final int DATEPICKER = 4;
        public static final int FILE = 9;
        public static final int APPLICATION = 10;
        public static final int CHECKBOX = 11;
        public static final int COLOR = 12;
        public static final int CATEGORY = 13;
        public static final int CHOICELIST = 14;
    }

    public static final DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm");

    /**
     * Create Field
     * @param definition
     * @param property
     * @return
     */
    public static Field createField(GWTJahiaItemDefinition definition, GWTJahiaNodeProperty property) {
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
                    field.setWidth(550);
                    break;
                case GWTJahiaNodeSelectorType.DATETIMEPICKER:
                    field = new CalendarField();
                    if (propDefinition.getSelectorOptions().get("format") != null) {
                        ((CalendarField) field).getPropertyEditor().setFormat(DateTimeFormat.getFormat(propDefinition.getSelectorOptions().get("format")));
                    } else {
                        ((CalendarField) field).getPropertyEditor().setFormat(dateFormat);
                        ((CalendarField) field).setHideTrigger(propDefinition.isProtected());
                    }
                    break;
                case GWTJahiaNodeSelectorType.DATEPICKER:
                    field = new DateField();
                    if (propDefinition.getSelectorOptions().get("format") != null) {
                        ((DateField) field).getPropertyEditor().setFormat(DateTimeFormat.getFormat(propDefinition.getSelectorOptions().get("format")));
                    } else {
                        ((DateField) field).getPropertyEditor().setFormat(dateFormat);
                    }
                    ((DateField) field).setHideTrigger(propDefinition.isProtected());
                    break;
                case GWTJahiaNodeSelectorType.PORTLET:
                    field = new ContentPickerField(Messages.get("picker_mashup_header", "Mashup picker"), Messages.get("picker_mashup_selection", "Mashup selected:"), definition.getSelectorOptions(), definition.getSelectorOptions().get("folder") != null ? definition.getSelectorOptions().get("folder") : "/", "jnt:portlet", "", "", ManagerConfigurationFactory.MASHUPPICKER, propDefinition.isMultiple(), false);
                    break;
                /* case GWTJahiaNodeSelectorType.PORTLETDEFINITION:
                field = new PortletDefinitionField();
                break;*/
                case GWTJahiaNodeSelectorType.CHECKBOX:
                    field = new CheckBox();
                    // hack to align check box to rigth
                    ((CheckBox)field).setBoxLabel("");
                    break;
                case GWTJahiaNodeSelectorType.COLOR:
                    break;
                case GWTJahiaNodeSelectorType.CATEGORY:
                    field = new ContentPickerField(Messages.get("picker_cat_header", "Categories picker"), Messages.get("picker_mashup_selection", "Category selected: "), definition.getSelectorOptions(), definition.getSelectorOptions().get("root") != null ? definition.getSelectorOptions().get("root") : "/categories", "", "", "", ManagerConfigurationFactory.CATEGORYPICKER, propDefinition.isMultiple(), false);
                    break;
                case GWTJahiaNodeSelectorType.FILE:
                    field = new ContentPickerField(Messages.get("picker_file_header", "Files picker"), Messages.get("picker_mashup_selection", "File selected: "), definition.getSelectorOptions(), definition.getSelectorOptions().get("folder") != null ? definition.getSelectorOptions().get("folder") : "/", "", definition.getSelectorOptions().get("filters"), definition.getSelectorOptions().get("mime"), ManagerConfigurationFactory.FILEPICKER, propDefinition.isMultiple(), false);
                    break;

                case GWTJahiaNodeSelectorType.CHOICELIST:
                    ListStore<GWTJahiaValueDisplayBean> store = new ListStore<GWTJahiaValueDisplayBean>();
                    store.add(propDefinition.getValueConstraints());
                    if (propDefinition.isMultiple()) {
                        ListField<GWTJahiaValueDisplayBean> list = new ListField<GWTJahiaValueDisplayBean>();
                        list.setStore(store);
                        list.setDisplayField("display");
                        field = list;
                    } else {
                        ComboBox<GWTJahiaValueDisplayBean> combo = new ComboBox<GWTJahiaValueDisplayBean>();
                        combo.setStore(store);
                        combo.setDisplayField("display");
                        combo.setTypeAhead(true);
                        combo.setTriggerAction(TriggerAction.ALL);
                        combo.setForceSelection(true);
                        if (propDefinition.getValueConstraints().size()>0 &&
                            propDefinition.getValueConstraints().get(0).getPropertyNames().contains("image")) {
                            combo.setTemplate(getComboTemplate());
                        }
                        field = combo;
                    }

                    // if there is no values, the field is hidden
                    if(propDefinition.getValueConstraints() !=null && propDefinition.getValueConstraints().isEmpty()){
                        field.setVisible(false);
                    }

                    break;
                default:
                    field = new TextField();
                    break;
            }
            if(propDefinition.isInternationalized()){
                field.setLabelSeparator(" <img width='11px' height='11px' src='"+ JahiaGWTParameters.getContextPath()+"/css/images/sharedLang.gif'/>:");
            }
        } else {
            GWTJahiaNodeDefinition nodeDefinition = (GWTJahiaNodeDefinition) definition;
            if (nodeDefinition.getName().equals("jcr:content") || nodeDefinition.getRequiredPrimaryTypes()[0].equals("nt:resource") || nodeDefinition.getRequiredPrimaryTypes()[0].equals("jnt:resource") || nodeDefinition.getRequiredPrimaryTypes()[0].equals("jnt:extraResource")) {
                field = new FileUploadField(definition.getName());
            }

            // case of page piker
            if (nodeDefinition.getRequiredPrimaryTypes()[0].equals("jmix:link")) {
                field = new ContentPickerField(Messages.get("picker_link_header", "Page picker"), Messages.get("picker_link_selection", "Selected page"), definition.getSelectorOptions(), definition.getSelectorOptions().get("folder") != null ? definition.getSelectorOptions().get("folder") : "/", "", definition.getSelectorOptions().get("filters"), "", ManagerConfigurationFactory.LINKPICKER, false, false);
            }
        }
        if (field == null) {
            return null;
        }
        setModifiers(field, definition);
        if (property != null) {
            fillValue(field, definition, property);
        }
        field.setWidth(550);
        return field;
    }

    /**
     * set modifiers
     * @param field
     * @param definition
     */
    public static void setModifiers(Field field, GWTJahiaItemDefinition definition) {
        if (field == null || definition == null) {
            return;
        }
        field.setName(definition.getName());
        field.setFieldLabel(definition.getLabel());
        field.setReadOnly(definition.isProtected());
        if (field instanceof TextField) {
            TextField tField = (TextField) field;
            tField.setAllowBlank(!definition.isMandatory());
        }
    }

    /**
     * fill value
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
                final List<GWTJahiaValueDisplayBean> displayBeans = propDefinition.getValueConstraints();
                if (propDefinition.isMultiple()) {
                    for (GWTJahiaNodePropertyValue jahiaNodePropertyValue : values) {
                        String val = jahiaNodePropertyValue.getString();
                        if (val != null && val.length() > 0) {
                            for (GWTJahiaValueDisplayBean displayBean : displayBeans) {
                                if(displayBean.getValue().equals(val)) {
                                    selection.add(displayBean);
                                }
                            }
                        }
                    }
                    ((ListField<GWTJahiaValueDisplayBean>) field).setSelection(selection);
                } else {
                    String val = values.get(0).getString();
                    for (GWTJahiaValueDisplayBean displayBean : displayBeans) {
                                if(displayBean.getValue().equals(val)) {
                                    selection.add(displayBean);
                                }
                            }
                    ((ComboBox<GWTJahiaValueDisplayBean>) field).setSelection(selection);
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
                        field.setValue(join(values));
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
            if (nodeDefinition.getName().equals("jcr:content") || nodeDefinition.getRequiredPrimaryTypes()[0].equals("nt:resource") || nodeDefinition.getRequiredPrimaryTypes()[0].equals("jnt:resource") || nodeDefinition.getRequiredPrimaryTypes()[0].equals("jnt:extraResource")) {
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
}
