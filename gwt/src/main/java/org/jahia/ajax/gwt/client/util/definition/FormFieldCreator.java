/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.util.definition;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Accessibility;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTChoiceListInitializer;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTBitSet;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.ckeditor.CKEditorConfig;
import org.jahia.ajax.gwt.client.widget.content.*;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.form.CKEditorField;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;
import org.jahia.ajax.gwt.client.widget.form.FileUploadField;
import org.jahia.ajax.gwt.client.widget.form.tag.TagField;

import java.util.*;

/**
 * This class creates fields for a GXT form panel based on available jcr types and a specific mapping.
 */
public class FormFieldCreator {

    private static final int DUAL_LIST_ITEM_COUNT_TO_FILTER = 5;

    public static final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm");
    public static final DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd.MM.yyyy");

    private static final int FILTER_FIELD_HEIGHT = 22;

    /**
     * Create Field
     *
     * @param definition
     * @param property
     * @param initializer
     * @return
     */
    public static Field<?> createField(GWTJahiaItemDefinition definition, GWTJahiaNodeProperty property,
                                       GWTChoiceListInitializer initializer, boolean displayHiddenProperty,
                                       GWTBitSet permissions, List<GWTJahiaNodePropertyValue> defaultValues) {
        Field<?> field = null;
        String className = "";
        if (definition.isHidden() && !displayHiddenProperty) {
            return null;
        }
        if (definition.getName().equals("*")) {
            return null;
        }
        if (!definition.isNode()) {
            GWTJahiaPropertyDefinition propDefinition = (GWTJahiaPropertyDefinition) definition;
            String emptyText = "";
            boolean isMultiple = ((GWTJahiaPropertyDefinition) definition).isMultiple();
            switch (definition.getSelector()) {
                case GWTJahiaNodeSelectorType.SMALLTEXT:
                    if (!definition.isProtected() && isMultiple) {
                        switch (propDefinition.getRequiredType()) {
                            case GWTJahiaNodePropertyType.LONG:
                                field = new MultipleNumberField<Long>();
                                ((MultipleNumberField) field).setAllowDecimals(false);
                                ((MultipleNumberField) field).setType(Long.class);
                                break;
                            case GWTJahiaNodePropertyType.DOUBLE:
                                field = new MultipleNumberField<Double>();
                                break;
                            default:
                                field = new MultipleTextField<String>();
                                break;
                        }
                    } else {
                        switch (propDefinition.getRequiredType()) {
                            case GWTJahiaNodePropertyType.LONG:
                                if (definition.isProtected() && isMultiple) {
                                    // protected field are displayed but not editable,
                                    // we need a textField to display all Long values comma separated
                                    field = new TextFieldWithClass();

                                } else {
                                    field = new NumberField();
                                    ((NumberField) field).setAllowDecimals(false);
                                    ((NumberField) field).setPropertyEditorType(Long.class);
                                }
                                break;
                            case GWTJahiaNodePropertyType.DOUBLE:
                                if (definition.isProtected() && isMultiple) {
                                    // protected field are displayed but not editable,
                                    // we need a textField to display all Double values comma separated
                                    field = new TextFieldWithClass();
                                } else {
                                    field = new NumberField();
                                    ((NumberField) field).setAllowDecimals(true);
                                }
                                break;
                            case GWTJahiaNodePropertyType.DECIMAL:
                                field = new NumberField();
                                ((NumberField) field).setAllowDecimals(true);
                                break;
                            default:
                                final TextField<String> f = new TextFieldWithClass();

                                f.addListener(Events.Change, new Listener<ComponentEvent>() {
                                    public void handleEvent(ComponentEvent event) {
                                        String s = f.getValue();
                                        if (s != null) {
                                            f.setValue(s.trim());
                                        }
                                    }
                                });
                                field = f;
                                if (definition.getSelectorOptions().get("password") != null) {
                                    ((TextField)field).setPassword(true);
                                }
                                break;
                        }
                    }
                    break;
                case GWTJahiaNodeSelectorType.TEXTAREA:
                    field = new TextArea();
                    final String height = propDefinition.getSelectorOptions().get("height");
                    if (height != null) {
                        field.setSize(Integer.toString(field.getWidth()), height);
                    }
                    break;
                case GWTJahiaNodeSelectorType.RICHTEXT:
                    field = new CKEditorField(getCKEditorConfig(propDefinition, permissions));
                    field.setAutoWidth(false);
                    field.setAutoHeight(false);
                    field.setHeight(300);
                    break;
                case GWTJahiaNodeSelectorType.DATETIMEPICKER:
                    if (isMultiple) {
                        // multiple datetimepicker are not supported, do not display the field
                        return null;
                    }

                    field = new CalendarField();
                    if (propDefinition.getSelectorOptions().get("format") != null) {
                        ((CalendarField) field).getPropertyEditor()
                                .setFormat(DateTimeFormat.getFormat(propDefinition.getSelectorOptions().get("format")));
                    } else {
                        ((CalendarField) field).getPropertyEditor().setFormat(dateTimeFormat);
                        ((CalendarField) field).setHideTrigger(propDefinition.isProtected());
                    }
                    break;
                case GWTJahiaNodeSelectorType.DATEPICKER:
                    if (isMultiple) {
                        // multiple datepicker are not supported, do not display the field
                        return null;
                    }

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
                    if (isMultiple) {
                        // multiple checkbox are not supported, do not display the field
                        return null;
                    }

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
                            if (propDefinition.getRequiredType() != GWTJahiaNodePropertyType.STRING && s.contains("[") && s.contains(",")) {
                                s = s.substring(s.indexOf("[") + 1,s.indexOf(","));
                            }
                            store.add(new GWTJahiaValueDisplayBean(s, s));
                        }
                    }
                    if (propDefinition.isMultiple()) {

                        final CustomDualListField<GWTJahiaValueDisplayBean> lists =
                                new CustomDualListField<GWTJahiaValueDisplayBean>();
                        ListField<GWTJahiaValueDisplayBean> from = lists.getFromList();
                        from.setStore(store);
                        from.setDisplayField("display");
                        store.sort("display", Style.SortDir.ASC);
                        if (store.getCount() > DUAL_LIST_ITEM_COUNT_TO_FILTER) {
                            StoreFilterField<GWTJahiaValueDisplayBean> filterField = new StoreFilterField<GWTJahiaValueDisplayBean>() {
                                @Override
                                protected boolean doSelect(Store<GWTJahiaValueDisplayBean> store, GWTJahiaValueDisplayBean parent,
                                                           GWTJahiaValueDisplayBean record, String property, String filter) {

                                    String s = filter.toLowerCase();
                                    return record.getValue().toLowerCase().contains(s)
                                            || record.getDisplay().toLowerCase().contains(s);
                                }
                            };
                            filterField.bind(store);
                            filterField.setHeight(FILTER_FIELD_HEIGHT);
                            filterField.setEmptyText(Messages.get("label.type.here.to.filter","Type here to filter the list"));
                            lists.setFilterField(filterField);
                        }
                        ListField<GWTJahiaValueDisplayBean> to = lists.getToList();
                        to.setDisplayField("display");
                        ListStore<GWTJahiaValueDisplayBean> tostore = new ListStore<GWTJahiaValueDisplayBean>();
                        to.setStore(tostore);
                        tostore.sort("display", Style.SortDir.ASC);
                        field = lists;
                        className = "list-store-field";
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

                            @Override
                            public void markInvalid(String msg) {
                                super.markInvalid(msg);
                                if (errorIcon != null) {
                                    errorIcon.addStyleName("invalid-icon");
                                }
                            }
                        };
                        combo.setStore(store);
                        combo.setDisplayField("display");
                        combo.setTypeAhead(true);
                        combo.setTriggerAction(TriggerAction.ALL);
                        combo.setForceSelection(true);
                        combo.setTemplate(getComboTemplate());

                        field = combo;
                        className = "combo-box-field";
                    }

                    // if there is no values, the field is hidden
                    if (store.getModels().isEmpty()) {
                        emptyText = Messages.get("label.no.values");
                    }

                    break;
                case GWTJahiaNodeSelectorType.TAG:
                    if((propDefinition.getRequiredType() == GWTJahiaNodePropertyType.STRING) && ((GWTJahiaPropertyDefinition) definition).isMultiple()){
                        field = new TagField(definition.getSelectorOptions().get("separator"), definition.getSelectorOptions().get("autocomplete"));
                    }else {
                        return null;
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
            // moved here due to IE problem reported in BOUYGUES-40
            field.setEmptyText(emptyText); // todo: allow to set the default value

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
            fillValue(field, definition, property, initializer, defaultValues);
        }
        //field.setId("JahiaGxtField"+ "_" + field.getName().replace(":","_")+"_"+locale);
        field.getElement().setAttribute("dir","auto");

        className = "".equals(className) ? field.getClass().getName().substring(field.getClass().getName().lastIndexOf(".") + 1) : className;
        className = className.replaceAll("([a-z])([A-Z]+)", "$1-$2").toLowerCase();
        field.addStyleName(className);
        return field;
    }

    private static CKEditorConfig getCKEditorConfig(GWTJahiaPropertyDefinition propDefinition,
                                                    GWTBitSet permissions) {
        CKEditorConfig config = new CKEditorConfig();
        boolean toolbarDefined = false;
        for (Map.Entry<String, String> option : propDefinition.getSelectorOptions()
                .entrySet()) {
            if (!option.getKey().startsWith("ckeditor.")) {
                continue;
            }
            String key = option.getKey().substring("ckeditor.".length());
            String value = option.getValue();
            if (value != null && value.contains("$context")) {
                value = value.replace("$context", JahiaGWTParameters.getContextPath());
            }
            config.set(key, value);
            if ("toolbar".equals(key) || "customConfig".equals(key) ) {
                toolbarDefined = true;
            }
        }
        if (!toolbarDefined) {
            String toolbar = "Light";
            if (PermissionsUtils.isPermitted("view-full-wysiwyg-editor", JahiaGWTParameters.getSiteNode()) || PermissionsUtils.isPermitted("studioModeAccess",permissions)) {
                toolbar = "Full";
            } else if (PermissionsUtils.isPermitted("view-basic-wysiwyg-editor", JahiaGWTParameters.getSiteNode())) {
                toolbar = "Basic";
            }
            config.setDefaultToolbar(toolbar);
        }
        return config;
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

        if (field instanceof AbstractMultipleField) {
            ((AbstractMultipleField) field).setAllowBlank(!definition.isMandatory());
        }

        if (field instanceof CheckBox) {
            field.setHideLabel(true);
            ((CheckBox) field).setBoxLabel(field.getFieldLabel());
        }
        if (field instanceof CustomDualListField) {
            CustomDualListField<GWTJahiaValueDisplayBean> lists = (CustomDualListField<GWTJahiaValueDisplayBean>) field;
            lists.setAllowBlank(!definition.isMandatory());
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
                        if (field instanceof TextField<?>) {
                            ((TextField<?>) field).setRegex(regex);
                            ((TextField<?>) field).getMessages().setRegexText(
                                    propDefinition.getConstraintErrorMessage() != null
                                            && propDefinition.getConstraintErrorMessage().length() > 0 ?
                                            propDefinition.getConstraintErrorMessage() :
                                            Messages.getWithArgs("failure.invalid.regexp.constraint.label",
                                                    "The field does not match the following regular expression: {0}",
                                                    new Object[]{regex}));
                        } else if (field instanceof MultipleTextField<?>) {
                            ((MultipleTextField<?>) field).setRegex(regex);
                            ((MultipleTextField<?>) field).setRegexText(
                                    propDefinition.getConstraintErrorMessage() != null
                                            && propDefinition.getConstraintErrorMessage().length() > 0 ?
                                            propDefinition.getConstraintErrorMessage() :
                                            Messages.getWithArgs("failure.invalid.regexp.constraint.label",
                                                    "The field does not match the following regular expression: {0}",
                                                    new Object[]{regex}));
                        }

                    }
                    break;

                case GWTJahiaNodePropertyType.LONG:
                case GWTJahiaNodePropertyType.DOUBLE:
                case GWTJahiaNodePropertyType.DECIMAL:
                    if (GWTJahiaNodeSelectorType.CHOICELIST != definition.getSelector()) {
                        if (propDefinition.getMaxValue() != null) {
                            ((NumberField) field).setMaxValue(Double.parseDouble(propDefinition.getMaxValue()));
                        }
                        if (propDefinition.getMinValue() != null) {
                            ((NumberField) field).setMinValue(Double.parseDouble(propDefinition.getMinValue()));
                        }
                    }
                    break;
                case GWTJahiaNodePropertyType.DATE:
                    if (propDefinition.getMaxValue() != null) {
                        Date maxValue = new Date(Long.parseLong(propDefinition.getMaxValue()));
                        if (field instanceof DateField) {
                            ((DateField) field).setMaxValue(maxValue);
                        } else if (field instanceof CalendarField) {
                            ((CalendarField) field).setMaxValue(maxValue);
                        }
                    }
                    if (propDefinition.getMinValue() != null) {
                        Date minValue = new Date(Long.parseLong(propDefinition.getMinValue()));
                        if (field instanceof DateField) {
                            ((DateField) field).setMinValue(minValue);
                        } else if (field instanceof CalendarField) {
                            ((CalendarField) field).setMinValue(minValue);
                        }

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
    public static void fillValue(final Field field, GWTJahiaItemDefinition definition, GWTJahiaNodeProperty property,
                                 GWTChoiceListInitializer initializer, List<GWTJahiaNodePropertyValue> defaultValues) {
        List<GWTJahiaNodePropertyValue> values = property.getValues();
        if (defaultValues != null && values.size() == 0) {
            values = defaultValues;
        }
        if (initializer != null && values.size() == 0) {
            for (GWTJahiaValueDisplayBean v : initializer.getDisplayValues()) {
                Object defaultProp = v.get("defaultProperty");
                if ((defaultProp instanceof String) && Boolean.parseBoolean((String) defaultProp)) {
                    values = Arrays.asList(new GWTJahiaNodePropertyValue(v.getValue()));
                } else if ((defaultProp instanceof Boolean) && Boolean.TRUE.equals(defaultProp)) {
                    values = Arrays.asList(new GWTJahiaNodePropertyValue(v.getValue()));
                }
            }
        }
        if (!definition.isNode()) {
            GWTJahiaPropertyDefinition propDefinition = (GWTJahiaPropertyDefinition) definition;
            if (values.size() == 0 && !propDefinition.isMultiple()) {
                return;
            }
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
                        String value = displayBean.getValue();
                        if (propDefinition.getRequiredType() == GWTJahiaNodePropertyType.LONG) {
                            value = Long.toString(new Double(value).longValue());
                        }

                        if (value.equals(val)) {
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
                        if (propDefinition.isMultiple()) {
                            // multiple not supported for boolean field
                            return;
                        }
                        field.setValue(values.get(0).getBoolean());
                        break;
                    case GWTJahiaNodePropertyType.LONG:
                        if (propDefinition.isMultiple()) {
                            if (!propDefinition.isProtected()) {
                                List<Long> v = new ArrayList<Long>();
                                for (GWTJahiaNodePropertyValue value : values) {
                                    v.add(value.getLong());
                                }
                                field.setValue(v);
                            } else {
                                if (values.size() > 0) {
                                    field.setValue(join(values));
                                }
                            }
                        } else {
                            field.setValue(values.get(0).getLong());
                        }
                        break;
                    case GWTJahiaNodePropertyType.DOUBLE:
                        if (propDefinition.isMultiple()) {
                            if (!propDefinition.isProtected()) {
                                List<Double> v = new ArrayList<Double>();
                                for (GWTJahiaNodePropertyValue value : values) {
                                    v.add(value.getDouble());
                                }
                                if (!v.isEmpty()) {
                                    field.setValue(v);
                                }
                            } else {
                                if (values.size() > 0) {
                                    field.setValue(join(values));
                                }
                            }
                        } else {
                            field.setValue(values.get(0).getDouble());
                        }
                        break;
                    case GWTJahiaNodePropertyType.DECIMAL:
                        if (propDefinition.isMultiple()) {
                            // multiple not supported for decimal field
                            return;
                        }
                        field.setValue(values.get(0).getDecimal());
                        break;
                    case GWTJahiaNodePropertyType.DATE:
                        if (propDefinition.isMultiple()) {
                            // multiple not supported for date field
                            return;
                        }
                        Date d = values.get(0).getDate(((DateTimePropertyEditor) field.getPropertyEditor()).getFormat());
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
                        } else if (!propDefinition.isProtected() && propDefinition.isMultiple()) {
                            List<String> v = new ArrayList<String>();
                            for (GWTJahiaNodePropertyValue value : values) {
                                v.add(value.getString());
                            }
                            if (!v.isEmpty()) {
                                field.setValue(v);
                            }
                        } else {
                            if (values.size() > 0) {
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
                        break;
                }
            }
        } else {
            if (values.size() == 0) {
                return;
            }
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
        if (sourceProperty.getValues().isEmpty()) {
            return;
        }
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
            ((CalendarField) targetField).setValue(sourceProperty.getValues().get(0).getDate(((DateTimePropertyEditor) f.getPropertyEditor()).getFormat()));
        } else if (targetField instanceof DateField) {
            ((DateField) targetField).setValue(sourceProperty.getValues().get(0).getDate(((DateTimePropertyEditor) f.getPropertyEditor()).getFormat()));
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
        } else if (targetField instanceof MultipleTextField) {
            List<String> v = new ArrayList<String>();
            for (GWTJahiaNodePropertyValue value : sourceProperty.getValues()) {
                v.add(value.getString());
            }
            ((MultipleTextField<String>) targetField).setValue(v);
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

    public static class CustomDualListField<D extends ModelData> extends DualListField<D> {
        private List<D> originalValue = new ArrayList<D>();

        private StoreFilterField<D> filterField;
        private boolean allowBlank;

        public void setCustomOriginalValue(List<D> originalValue) {
            this.originalValue = originalValue;
        }

        /**
         * Returns the field's allow blank state.
         *
         * @return true if toList can be empty
         */

        public boolean isAllowBlank() {
            return allowBlank;
        }

        /**
         * Returns the field's allow blank state.
         *
         * @return true if toList can be empty
         */
        public void setAllowBlank(boolean allowBlank) {
            this.allowBlank = allowBlank;
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
            if (GXT.isAriaEnabled()) {
                if (!allowBlank) {
                    Accessibility.setState(getToList().getElement(),"aria-required", "true");
                }
            }

            toField.getStore().addListener(ListStore.Add, new Listener<BaseEvent>() {
                public void handleEvent(BaseEvent be) {
                    validate();
                }
            });
            toField.setName("to-" + name);
            fromField.setName("from-" + name);
            setHeight(200);
        }

        public StoreFilterField<D> getFilterField() {
            return filterField;
        }

        public void setFilterField(StoreFilterField<D> filterField) {
            this.filterField = filterField;
            VerticalPanel vp = new VerticalPanel();
            vp.add(filterField);
            vp.add(fields.remove(0));
            fields.add(0, new AdapterField(vp));
        }

        @Override
        protected void onResize(int width, int height) {
            super.onResize(width, height);
            if (filterField != null) {
                fromField.setHeight(fromField.getHeight() - (FILTER_FIELD_HEIGHT / 2));
                filterField.setWidth(fromField.getWidth());
            }
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            if (readOnly) {
                buttonBar.disable();
                fromField.disable();
            } else {
                buttonBar.enable();
                fromField.enable();
            }
            super.setReadOnly(readOnly);
        }

        @Override
        protected boolean validateValue(String value) {
            boolean b = !allowBlank && toField.getStore().getCount() == 0;
            if (b) {
                toField.markInvalid(getMessages().getInvalidText());
                toField.getListView().setBorders(true);
                toField.getListView().removeStyleName("x-combo-list");
                toField.getListView().removeStyleName("x-border");
                toField.getListView().addStyleName("x-form-invalid");
                return false;
            }
            toField.getListView().addStyleName("x-combo-list");
            toField.getListView().addStyleName("x-border");
            toField.getListView().setBorders(false);
            toField.getListView().removeStyleName("x-form-invalid");
            toField.clearInvalid();
            return true;
        }
    }

    public static class TextFieldWithClass extends TextField<String> {
        @Override
        public void markInvalid(String msg) {
            super.markInvalid(msg);
            if (errorIcon != null) {
                errorIcon.addStyleName("invalid-icon");
            }
        }
    }
}
