package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.List;
import java.util.Map;

public class EngineValidation {
    private TabPanel tabs;
    private String selectedLanguage;
    private Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties;

    public EngineValidation(TabPanel tabs, String selectedLanguage, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties) {
        this.tabs = tabs;
        this.selectedLanguage = selectedLanguage;
        this.changedI18NProperties = changedI18NProperties;
    }

    public class ValidateResult {
        public boolean allValid = true;
        public TabItem firstErrorTab = null;
        public Field<?> firstErrorField = null;
        public String firstErrorLang = null;
    }

    public ValidateResult validateData() {
        ValidateResult validateResult = new ValidateResult();

        for (TabItem tab : tabs.getItems()) {
            EditEngineTabItem item = tab.getData("item");
            if (item instanceof PropertiesTabItem) {
                PropertiesTabItem propertiesTabItem = (PropertiesTabItem) item;
                PropertiesEditor pe = ((PropertiesTabItem) item).getPropertiesEditor();
                if (pe != null) {
                    for (PropertiesEditor.PropertyAdapterField adapterField : pe.getFieldsMap().values()) {
                        Field<?> field = adapterField.getField();
                        if (field.isEnabled() && !field.isReadOnly() && !field.validate() && ((FieldSet)adapterField.getParent()).isExpanded()) {
                            if (validateResult.allValid || tab.equals(tabs.getSelectedItem())
                                    && !tab.equals(validateResult.firstErrorTab)) {
                                validateResult.firstErrorTab = tab;
                                validateResult.firstErrorField = field;
                            }
                            validateResult.allValid = false;
                        }
                    }
                    if (!validateResult.allValid) {
                        continue;
                    }
                }

                // handle multilang
                if (propertiesTabItem.isMultiLang()) {
                    // for now only contentTabItem  has multilang. properties
                    if (selectedLanguage != null) {
                        final String lang = selectedLanguage;
                        for (String language : changedI18NProperties.keySet()) {
                            if (!lang.equals(language)) {
                                PropertiesEditor lpe = propertiesTabItem.getPropertiesEditorByLang(language);
                                if (lpe != null) {
                                    for (PropertiesEditor.PropertyAdapterField adapterField : lpe.getFieldsMap().values()) {
                                        Field<?> field = adapterField.getField();
                                        if (field.isEnabled() && !field.isReadOnly() && !field.validate() && ((FieldSet)adapterField.getParent()).isExpanded() && adapterField.getDefinition().isInternationalized()) {
                                            if (validateResult.allValid || tab.equals(tabs.getSelectedItem())
                                                    && !tab.equals(validateResult.firstErrorTab)) {
                                                validateResult.firstErrorTab = tab;
                                                validateResult.firstErrorField = field;
                                            }
                                            validateResult.allValid = false;
                                        }
                                    }
                                    if (!validateResult.allValid) {
                                        validateResult.firstErrorLang = language;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return validateResult;
    }


}
