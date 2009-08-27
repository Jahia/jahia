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
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.i18n.client.DateTimeFormat;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;
import org.jahia.ajax.gwt.client.widget.form.FileUploadField ;
import org.jahia.ajax.gwt.client.widget.content.ContentPickerField;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;

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
                    field = new HtmlEditor();
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
                    field = new ContentPickerField(definition.getSelectorOptions().get("folder") != null ? definition.getSelectorOptions().get("folder") : "/content", "jnt:portlet","","",ManagerConfigurationFactory.MASHUPPICKER,propDefinition.isMultiple(), false);
                    break;
                /*case GWTJahiaNodeSelectorType.PORTLETDEFINITION:
                    field = new PortletDefinitionField();
                    break;*/
                case GWTJahiaNodeSelectorType.CHECKBOX:
                    field = new CheckBox();
                    break;
                case GWTJahiaNodeSelectorType.COLOR:
                    break;
                case GWTJahiaNodeSelectorType.CATEGORY:
                    field = new ContentPickerField(definition.getSelectorOptions().get("root") != null ? definition.getSelectorOptions().get("root") : "/content/categories", "", "","", ManagerConfigurationFactory.CATEGORYPICKER, propDefinition.isMultiple(), false);
                    break;
                case GWTJahiaNodeSelectorType.FILE:
                    field = new ContentPickerField(definition.getSelectorOptions().get("folder") != null ? definition.getSelectorOptions().get("folder") : "/content", "", definition.getSelectorOptions().get("filters"), definition.getSelectorOptions().get("mime"), ManagerConfigurationFactory.FILEPICKER, propDefinition.isMultiple(), false);
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
                        if (definition.getSelectorOptions().containsKey("image")) {
                            combo.setTemplate(getComboTemplate());
                        }
                        field = combo;
                    }
                    break;
                default:
            }
        } else {
            GWTJahiaNodeDefinition nodeDefinition = (GWTJahiaNodeDefinition) definition;
            if (nodeDefinition.getName().equals("jcr:content") || nodeDefinition.getRequiredPrimaryTypes()[0].equals("nt:resource") || nodeDefinition.getRequiredPrimaryTypes()[0].equals("jnt:resource") || nodeDefinition.getRequiredPrimaryTypes()[0].equals("jnt:extraResource")) {
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
        field.setWidth(550);
        return field;
    }

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

    public static void fillValue(final Field field, GWTJahiaItemDefinition definition, GWTJahiaNodeProperty property) {
        Log.debug("Setting field value for " + property.getName() + " in " + field.getName());
        List<GWTJahiaNodePropertyValue> values = property.getValues();
        if (values.size() == 0) {
            return;
        }
        if (!definition.isNode()) {
            GWTJahiaPropertyDefinition propDefinition = (GWTJahiaPropertyDefinition) definition;
            Log.debug("selector : " + propDefinition.getSelector());
            if (propDefinition.getSelector() == GWTJahiaNodeSelectorType.CHOICELIST) {
                List<GWTJahiaValueDisplayBean> selection = new ArrayList<GWTJahiaValueDisplayBean>();
                if (propDefinition.isMultiple()) {
                    for (GWTJahiaNodePropertyValue jahiaNodePropertyValue : values) {
                        String val = jahiaNodePropertyValue.getString();
                        if (val != null && val.length() > 0) {
                            selection.add(new GWTJahiaValueDisplayBean(val, val));
                        }
                    }
                    ((ListField<GWTJahiaValueDisplayBean>) field).setSelection(selection);
                } else {
                    String val = values.get(0).getString();
                    selection.add(new GWTJahiaValueDisplayBean(val, val));
                    ((ComboBox<GWTJahiaValueDisplayBean>) field).setSelection(selection);
                }
            } else {
                switch (propDefinition.getRequiredType()) {
                    case GWTJahiaNodePropertyType.BINARY:
                        break;
                    case GWTJahiaNodePropertyType.BOOLEAN:
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
            }
        }
    }

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
