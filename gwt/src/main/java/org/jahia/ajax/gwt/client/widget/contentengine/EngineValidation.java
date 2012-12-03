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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.service.GWTConstraintViolationException;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class used to collect validation result for engine decoration.
 *
 * @since : JAHIA 6.7
 */
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

    /**
     * Generate {ValidateResult} based on the GWT fields validation.
     *
     * @return the {ValidateResult}.
     */
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

    /**
     * Generate {ValidateResult} based on the exception returned by the JCR session.
     *
     * @return the {ValidateResult}.
     */
    public ValidateResult getValidationFromException(List<GWTConstraintViolationException> errors) {
        Map<String, GWTConstraintViolationException> errorMap = new HashMap<String, GWTConstraintViolationException>();
        for (GWTConstraintViolationException error : errors) {
            if (error.getPropertyName() != null) {
                errorMap.put(error.getPropertyName(), error);
            }
        }

        ValidateResult validateResult = new ValidateResult();

        for (TabItem tab : tabs.getItems()) {
            EditEngineTabItem item = tab.getData("item");
            if (item instanceof PropertiesTabItem) {
                PropertiesTabItem propertiesTabItem = (PropertiesTabItem) item;
                PropertiesEditor pe = ((PropertiesTabItem) item).getPropertiesEditor();
                if (pe != null) {
                    Map<String, PropertiesEditor.PropertyAdapterField> fieldsMap = pe.getFieldsMap();
                    for (String fieldName : fieldsMap.keySet()) {
                        if (errorMap.containsKey(fieldName)) {
                            Field<?> field = fieldsMap.get(fieldName).getField();
                            GWTConstraintViolationException error = errorMap.get(fieldName);
                            field.markInvalid(error.getConstraintMessage());
                            if (validateResult.allValid || tab.equals(tabs.getSelectedItem())
                                    && !tab.equals(validateResult.firstErrorTab)) {
                                validateResult.firstErrorTab = tab;
                                validateResult.firstErrorField = field;
                                validateResult.firstErrorLang = error.getLocale();
                            }
                            validateResult.allValid = false;
                        }
                    }
                    if (!validateResult.allValid) {
                        continue;
                    }
                }
            }
        }
        return validateResult;
    }


}
