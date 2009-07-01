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
import com.extjs.gxt.ui.client.widget.form.*;
import com.google.gwt.i18n.client.DateTimeFormat;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;
import org.jahia.ajax.gwt.client.widget.form.FileUploadField ;
import org.jahia.ajax.gwt.client.widget.category.CategoryField;
import org.jahia.ajax.gwt.client.data.definition.*;

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
                    field = new TextArea();
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
                /*case GWTJahiaNodeSelectorType.PORTLET:
                    field = new ContentPickerField("/content/mashups", "jnt:portlet","","","","",false);
                    break;
                case GWTJahiaNodeSelectorType.PORTLETDEFINITION:
                    field = new PortletDefinitionField();
                    break;*/
                case GWTJahiaNodeSelectorType.CHECKBOX:
                    field = new CheckBox();
                    break;
                case GWTJahiaNodeSelectorType.COLOR:
                    break;
                case GWTJahiaNodeSelectorType.CATEGORY:
                    field = new CategoryField();
                    break;
                /*case GWTJahiaNodeSelectorType.FILE:
                    field = new ContentPickerField("/", "","","","","",false);
                    break;*/
                case GWTJahiaNodeSelectorType.CHOICELIST:
                    field = new SimpleComboBox<GWTJahiaNodeType>();
                    ((SimpleComboBox) field).add(propDefinition.getValueConstraints());
                    ((SimpleComboBox) field).setTypeAhead(true);
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
            // fill value in constrains case
            StringBuilder str;
            switch (propDefinition.getRequiredType()) {
                case GWTJahiaNodePropertyType.STRING:
                    str = new StringBuilder(values.get(0).getString());
                    for (int i = 1; i < values.size(); i++) {
                        str.append(", ");
                        str.append(values.get(i).getString());
                    }
                    field.setValue(str.toString());
                    break;
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
                    Log.debug("date :");
                    Date d = values.get(0).getDate();
                    Log.debug(d.toString());
                    field.setValue(d);
                    break;
                case GWTJahiaNodePropertyType.NAME:
                    str = new StringBuilder(values.get(0).getString());
                    for (int i = 1; i < values.size(); i++) {
                        str.append(", ");
                        str.append(values.get(i).getString());
                    }
                    field.setValue(str.toString());
                    break;
                case GWTJahiaNodePropertyType.PATH:
                    str = new StringBuilder(values.get(0).getString());
                    for (int i = 1; i < values.size(); i++) {
                        str.append(", ");
                        str.append(values.get(i).getString());
                    }
                    field.setValue(str.toString());
                    break;
                case GWTJahiaNodePropertyType.REFERENCE:
                    str = new StringBuilder(values.get(0).getString());
                    for (int i = 1; i < values.size(); i++) {
                        str.append(", ");
                        str.append(values.get(i).getString());
                    }
                    field.setValue(str.toString());
                    break;
                case GWTJahiaNodePropertyType.WEAKREFERENCE:
                    str = new StringBuilder(values.get(0).getString());
                    for (int i = 1; i < values.size(); i++) {
                        str.append(", ");
                        str.append(values.get(i).getString());
                    }
                    field.setValue(str.toString());
                    break;
                case GWTJahiaNodePropertyType.URI:
                    str = new StringBuilder(values.get(0).getString());
                    for (int i = 1; i < values.size(); i++) {
                        str.append(", ");
                        str.append(values.get(i).getString());
                    }
                    field.setValue(str.toString());
                    break;
                case GWTJahiaNodePropertyType.UNDEFINED:
                    str = new StringBuilder(values.get(0).getString());
                    for (int i = 1; i < values.size(); i++) {
                        str.append(", ");
                        str.append(values.get(i).getString());
                    }
                    field.setValue(str.toString());
                    break;
                default:
            }
        } else {
            GWTJahiaNodeDefinition nodeDefinition = (GWTJahiaNodeDefinition) definition;
            if (nodeDefinition.getName().equals("jcr:content") || nodeDefinition.getRequiredPrimaryTypes()[0].equals("nt:resource") || nodeDefinition.getRequiredPrimaryTypes()[0].equals("jnt:resource") || nodeDefinition.getRequiredPrimaryTypes()[0].equals("jnt:extraResource")) {
                field.setValue(values.get(0).getString());
            }
        }
    }

}
