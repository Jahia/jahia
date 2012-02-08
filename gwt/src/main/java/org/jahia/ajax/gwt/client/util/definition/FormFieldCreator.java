/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.util.definition;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ColorPalette;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.menu.ColorMenu;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Accessibility;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaFieldInitializer;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTBitSet;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.ckeditor.CKEditorConfig;
import org.jahia.ajax.gwt.client.widget.content.ColorPickerField;
import org.jahia.ajax.gwt.client.widget.content.ContentPickerField;
import org.jahia.ajax.gwt.client.widget.content.CronField;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
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
     * @param initializer
     * @return
     */
    public static Field<?> createField(GWTJahiaItemDefinition definition, GWTJahiaNodeProperty property,
                                       GWTJahiaFieldInitializer initializer, boolean displayHiddenProperty, GWTBitSet permissions) {
        Field<?> field = null;
        if (definition.isHidden() && !displayHiddenProperty) {
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
                            field = new TextField<String>();
                            break;
                    }
                    if (definition.getSelectorOptions().get("password") != null) {
                        ((TextField)field).setPassword(true);
                    }
                    break;
                case GWTJahiaNodeSelectorType.TEXTAREA:
                    field = new TextArea();
                    break;
                case GWTJahiaNodeSelectorType.RICHTEXT:
                    CKEditorConfig config = new CKEditorConfig();
                    if (PermissionsUtils.isPermitted("view-full-wysiwyg-editor",permissions) || PermissionsUtils.isPermitted("studioModeAccess",permissions)) {
                        config.setToolbarSet("Full");
                    } else if (PermissionsUtils.isPermitted("view-basic-wysiwyg-editor" ,permissions)) {
                        config.setToolbarSet("Basic");
                    } else {
                        config.setToolbarSet("Light");
                    }
                    field = new CKEditorField(config);
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
                    field = new ColorPickerField();
                    break;
                case GWTJahiaNodeSelectorType.CRON:
                    field = new CronField();
                    break;
                case GWTJahiaNodeSelectorType.CATEGORY:
                    field = new ContentPickerField(definition.getSelectorOptions(),
                            propDefinition.getValueConstraints(), null, null,
                            ManagerConfigurationFactory.CATEGORYPICKER, propDefinition.isMultiple());
                    break;
                case GWTJahiaNodeSelectorType.PICKER:
                    field = new ContentPickerField(definition.getSelectorOptions(),
                            propDefinition.getValueConstraints(), getSelectorOptionAsList(definition, "filters"), getSelectorOptionAsList(definition, "mime"),
                            definition.getSelectorOptions().get("type") != null ?
                                    definition.getSelectorOptions().get("type")+"picker" :
                                    ManagerConfigurationFactory.EDITORIALCONTENTPICKER, propDefinition.isMultiple());
                    break;

                case GWTJahiaNodeSelectorType.CHOICELIST:
                    ListStore<GWTJahiaValueDisplayBean> store = new ListStore<GWTJahiaValueDisplayBean>();
                    if (initializer != null) {
                        store.add(initializer.getDisplayValues());
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
                        store.setSortDir(Style.SortDir.ASC);
                        store.setSortField("display");
                        ListField<GWTJahiaValueDisplayBean> to = lists.getToList();
                        to.setDisplayField("display");
                        ListStore<GWTJahiaValueDisplayBean> tostore = new ListStore<GWTJahiaValueDisplayBean>();
                        to.setStore(tostore);
                        tostore.setSortDir(Style.SortDir.ASC);
                        tostore.setSortField("display");
                        field = lists;
                    } else {
                        ComboBox<GWTJahiaValueDisplayBean> combo = new ComboBox<GWTJahiaValueDisplayBean>() {
                            @Override
                            public void expand() {
                                super.expand();
                                Timer t = new Timer() {
                                    public void run() {
                                        restrict();
                                    }
                                };
                                t.schedule(500);
                            }
                        };
                        combo.setStore(store);
                        combo.setEmptyText(""); // todo: allow to set the default value
                        combo.setDisplayField("display");
                        combo.setTypeAhead(true);
                        combo.setTriggerAction(TriggerAction.ALL);
                        combo.setForceSelection(true);
                        combo.setTemplate(getComboTemplate());
                        field = combo;
                    }

                    // if there is no values, the field is hidden
                    if (store.getModels().isEmpty()) {
                        field.setEmptyText(Messages.get("label.no.values"));
                    }

                    break;
                default:
                    if (propDefinition.getRequiredType() == GWTJahiaNodePropertyType.REFERENCE ||
                            propDefinition.getRequiredType() == GWTJahiaNodePropertyType.WEAKREFERENCE) {
                        field = new ContentPickerField(definition.getSelectorOptions(),
                                propDefinition.getValueConstraints(), getSelectorOptionAsList(definition, "filters"),
                                getSelectorOptionAsList(definition, "mime"), ManagerConfigurationFactory.EDITORIALCONTENTPICKER,
                                propDefinition.isMultiple());
                    } else {
                        field = new TextField<String>();
                    }
                    break;
            }
            if (propDefinition.isInternationalized()) {
                field.setLabelSeparator(" <img width='11px' height='11px' src='" + JahiaGWTParameters.getContextPath() +
                        "/css/images/sharedLang.gif'/>");
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
        if (field.getLabelSeparator() != null) {
            field.setLabelSeparator(field.getLabelSeparator() + " :");
        }
        if (property != null) {
            fillValue(field, definition, property, initializer);
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
    public static void setModifiers(final Field field, GWTJahiaItemDefinition definition) {
        if (field == null || definition == null) {
            return;
        }
        field.setName(definition.getName());
        field.setFieldLabel(definition.getLabel());
        if(!"".equals(definition.getTooltip())) {
            String separator = field.getLabelSeparator()!=null? field.getLabelSeparator():"";
            field.setLabelSeparator(separator + " <img width='16px' height='16px' src='" + JahiaGWTParameters.getContextPath() +
                    "/modules/default/images/icons/information.png' title='" + definition.getTooltip().replace("'", " ") + "'/>");
        }
        if (field.isReadOnly() || definition.isProtected()) {
            field.setReadOnly(true);
            if (field instanceof TriggerField) {
                ((TriggerField) field).setHideTrigger(true);
            }
        } else {
            field.setReadOnly(false);
        }
        if (field instanceof TextField) {
            TextField<?> tField = (TextField<?>) field;
            tField.setAllowBlank(!definition.isMandatory());
        }
        if (field instanceof CKEditorField) {
            CKEditorField tField = (CKEditorField) field;
            tField.setAllowBlank(!definition.isMandatory());
        }

        if (field instanceof CheckBox) {
            field.setHideLabel(true);
            ((CheckBox) field).setBoxLabel(field.getFieldLabel());
        }
        if (!definition.isNode()) {
            GWTJahiaPropertyDefinition propDefinition = (GWTJahiaPropertyDefinition) definition;
            switch (propDefinition.getRequiredType()) {
                case GWTJahiaNodePropertyType.STRING:
                case GWTJahiaNodePropertyType.URI:
                    if (GWTJahiaNodeSelectorType.CHOICELIST != definition.getSelector()
                            && propDefinition.getValueConstraints() != null
                            && propDefinition.getValueConstraints().size() == 1) {
                        String regex = propDefinition.getValueConstraints().get(0);
                        ((TextField<?>) field).setRegex(regex);
                        ((TextField<?>) field).getMessages().setRegexText(
                                propDefinition.getConstraintErrorMessage() != null
                                        && propDefinition.getConstraintErrorMessage().length() > 0 ?
                                        propDefinition.getConstraintErrorMessage():
                                        Messages.getWithArgs("failure.invalid.regexp.constraint.label",
                                                "The field does not match the following regular expression: {0}",
                                                new Object[] { regex }));

                    }
                    break;

                case GWTJahiaNodePropertyType.LONG:
                case GWTJahiaNodePropertyType.DOUBLE:
                case GWTJahiaNodePropertyType.DECIMAL:
                    if (propDefinition.getMaxValue() != null) {
                        ((NumberField) field).setMaxValue(Double.parseDouble(propDefinition.getMaxValue()));
                    }
                    if (propDefinition.getMinValue() != null) {
                        ((NumberField) field).setMinValue(Double.parseDouble(propDefinition.getMinValue()));
                    }
                    break;

                case GWTJahiaNodePropertyType.DATE:
                    if (propDefinition.getMaxValue() != null) {
                        ((DateField) field).setMaxValue(new Date(Long.parseLong(propDefinition.getMaxValue())));
                    }
                    if (propDefinition.getMinValue() != null) {
                        ((DateField) field).setMinValue(new Date(Long.parseLong(propDefinition.getMinValue())));
                    }
                    break;
            }
        }
    }

    /**
     * fill value
     *
     * @param field
     * @param definition
     * @param property
     */
    public static void fillValue(final Field field, GWTJahiaItemDefinition definition, GWTJahiaNodeProperty property, GWTJahiaFieldInitializer initializer) {
        List<GWTJahiaNodePropertyValue> values = property.getValues();
        if (initializer != null && values.size() == 0) {
            for (GWTJahiaValueDisplayBean v : initializer.getDisplayValues()) {
                if (v.get("defaultProperty") != null && (Boolean) v.get("defaultProperty")) {
                    values = Arrays.asList(new GWTJahiaNodePropertyValue(v.getValue()));
                }
            }
        }
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
                        if (propDefinition.getSelector() == GWTJahiaNodeSelectorType.PICKER) {
                            List<GWTJahiaNode> v = new ArrayList<GWTJahiaNode>();
                            for (GWTJahiaNodePropertyValue value : values) {
                                v.add(value.getNode());
                            }
                            field.setValue(v);
                        } else {
                            if (values.get(0).getString() != null) {
                                field.setValue(join(values));
                            }
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

    public static void copyValue(final GWTJahiaNodeProperty sourceProperty, final Field<?> f) {
        final Field<?> targetField;
        if (f instanceof PropertiesEditor.PropertyAdapterField) {
            targetField = ((PropertiesEditor.PropertyAdapterField)f).getField();
        } else {
            targetField = f;
        }
        if (targetField instanceof NumberField) {
            ((NumberField) targetField).setValue(sourceProperty.getValues().get(0).getLong());
        } else if (targetField instanceof CKEditorField) {
            ((CKEditorField) targetField).setValue(sourceProperty.getValues().get(0).getString());
            // Do it a second time later for some browsers .. ?
            Timer togglebuttonTimer = new Timer() {
                public void run() {
                    ((CKEditorField) targetField).setValue(sourceProperty.getValues().get(0).getString());
                }
            };

            togglebuttonTimer.schedule(100);
        } else if (targetField instanceof CalendarField) {
            ((CalendarField) targetField).setValue(sourceProperty.getValues().get(0).getDate());
        } else if (targetField instanceof DateField) {
            ((DateField) targetField).setValue(sourceProperty.getValues().get(0).getDate());
        } else if (targetField instanceof CheckBox) {
            ((CheckBox) targetField).setValue(sourceProperty.getValues().get(0).getBoolean());
        } else if (targetField instanceof ContentPickerField) {
            List<GWTJahiaNode> values = new ArrayList<GWTJahiaNode>();
            for (GWTJahiaNodePropertyValue value : sourceProperty.getValues()) {
                values.add(value.getNode());
            }
            ((ContentPickerField) targetField).setValue(values);
        } else if (targetField instanceof ComboBox) {
            // ?
        } else if (targetField instanceof TextField) {
            ((TextField<String>) targetField).setValue(sourceProperty.getValues().get(0).getString());
        }
    }



    private static native String getComboTemplate()  /*-{
        return  [
            '<tpl for=".">',
            '<div class="x-combo-list-item">',
            '<tpl if="image != &quot;&quot;">',
                '<img src="{image}"/> ',
            '</tpl>',
            '{display}</div>',
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

        @Override
        protected void onButtonLeft(IconButtonEvent be) {
            super.onButtonLeft(be);
            getFromList().getStore().sort("display", Style.SortDir.ASC);
        }

        @Override
        protected void onButtonAllLeft(IconButtonEvent be) {
            super.onButtonAllLeft(be);
            getFromList().getStore().sort("display", Style.SortDir.ASC);
        }

        @Override
        protected void onButtonAllRight(IconButtonEvent be) {
            super.onButtonAllRight(be);
            getToList().getStore().sort("display", Style.SortDir.ASC);
        }

        @Override
        protected void onButtonRight(IconButtonEvent be) {
            super.onButtonRight(be);
            getToList().getStore().sort("display", Style.SortDir.ASC);
        }

        @Override
        public void onComponentEvent(ComponentEvent ce) {
            super.onComponentEvent(ce);
            getToList().getStore().sort("display", Style.SortDir.ASC);
            getFromList().getStore().sort("display", Style.SortDir.ASC);
        }
    }
}
