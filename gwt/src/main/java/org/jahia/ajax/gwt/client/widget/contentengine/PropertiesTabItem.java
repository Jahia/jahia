/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.content.ContentPickerField;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.*;

/**
 *
 * Tab item that contains properties
 * @version 6.5
 * @author toto
 *
 */
public class PropertiesTabItem extends EditEngineTabItem {
    protected List<String> dataType;
    protected List<String> excludedTypes;

    protected transient String language;
    protected transient PropertiesEditor propertiesEditor;
    protected transient Map<String, PropertiesEditor> langPropertiesEditorMap;
    protected transient Map<String, Map<String,GWTJahiaNodeProperty>> changedProperties;
    protected boolean multiLang = true;
    protected boolean viewCopyToAllLangs = true;

    @Override
    public AsyncTabItem create(GWTEngineTab engineTab, NodeHolder engine) {
        AsyncTabItem tab = super.create(engineTab, engine);
        langPropertiesEditorMap = new HashMap<String, PropertiesEditor>();
        changedProperties = new HashMap<String, Map<String,GWTJahiaNodeProperty>>();
        tab.setLayout(new FitLayout());
        tab.setScrollMode(Style.Scroll.AUTO);
        return tab;
    }

    /**
     * Get properties editor of the default lang
     *
     * @return
     */
    public PropertiesEditor getPropertiesEditor() {
        return propertiesEditor;
    }

    public Map<String, PropertiesEditor> getLangPropertiesEditorMap() {
        return langPropertiesEditorMap;
    }

    /**
     * Get properties editor by langCode
     *
     * @param language
     * @return
     */
    public PropertiesEditor getPropertiesEditorByLang(String language) {
        if (language == null) {
            Log.error("Locale is null");
            return null;
        }
        return langPropertiesEditorMap.get(language);
    }

    /**
     * set properties editor by lang
     *
     * @param language
     */
    private void setPropertiesEditorByLang(String language) {
        if (langPropertiesEditorMap == null || language == null) {
            return;
        }
        langPropertiesEditorMap.put(language, propertiesEditor);
    }

    @Override
    public void init(final NodeHolder engine, final AsyncTabItem tab, String language) {
        // do not re-process the view if it's already done and the tabItem is not multilang
        if (!isMultiLang() && tab.isProcessed()) {
            return;
        }
        tab.mask(Messages.get("label.loading","Loading..."), "x-mask-loading");
        if (engine.getMixin() != null) {
            tab.unmask();
            boolean addSharedLangLabel = true;
            List<GWTJahiaNodeProperty> previousNon18nProperties = null;
            this.language = language;
            Set<String> previousAddedTypes = null;
            Set<String> previousRemovedTypes = null;

            if (propertiesEditor != null) {
                if (propertiesEditor == getPropertiesEditorByLang(language)) {
                    return;
                }
                addSharedLangLabel = false;
                propertiesEditor.setVisible(false);
                // keep track of the old values
                previousNon18nProperties = propertiesEditor.getProperties(false, true, false);
                previousAddedTypes = propertiesEditor.getAddedTypes();
                previousRemovedTypes = propertiesEditor.getRemovedTypes();
            }
            if (!isMultiLang()) {
                setProcessed(true);
            }
            propertiesEditor = getPropertiesEditorByLang(language);

            if (propertiesEditor == null) {
                if (engine.isExistingNode() && engine.getNode().isShared()) {
                    // this label is shared among languages.
                    if (addSharedLangLabel) {
                        Label label = new Label(Messages.get("warning.sharedNode", "Important : This is a shared node, editing it will modify its value for all its usages"));
                        label.setStyleAttribute("color", "rgb(200,80,80)");
                        label.setStyleAttribute("font-size", "14px");
                        tab.add(label);
                    }
                }

                Map<String, GWTJahiaNodeProperty> properties = engine.getProperties();
                if (changedProperties.containsKey(language)) {
                    properties.putAll(changedProperties.get(language));
                }
                if (engine.getPresetProperties() != null && !engine.getPresetProperties().isEmpty()) {
                    properties.putAll(engine.getPresetProperties());
                }
                propertiesEditor = new PropertiesEditor(engine.getNodeTypes(), properties, dataType, engine) {
                    @Override
                    public void copyToAllLanguages(GWTJahiaNodeProperty prop, Field<?> remoteField) {
                        for (GWTJahiaLanguage jahiaLanguage : JahiaGWTParameters.getSiteLanguages()) {
                            String l = jahiaLanguage.getLanguage();
                            if (!l.equals(PropertiesTabItem.this.language)) {
                                PropertiesEditor langPropEditor = langPropertiesEditorMap.get(l);
                                if (langPropertiesEditorMap.containsKey(l)) {
                                    if (langPropEditor.getFieldsMap().get(prop.getName()) == null) {
                                        // if the property editor is set but the field is not present, create it ..
                                        PropertyAdapterField remote = (PropertyAdapterField) remoteField;
                                        PropertyAdapterField field = langPropEditor.getFieldsMap().get(remote.getDefinition().getName());
                                        ((Field) (field.getField())).setValue(remote.getField().getValue());
                                        langPropEditor.setExternalMixin(field, true);
                                    }
                                    Field<?> f = langPropEditor.getFieldsMap().get(prop.getName()).getField();
                                    FormFieldCreator.copyValue(prop, f);
                                } else {
                                    if (!changedProperties.containsKey(l)) {
                                        changedProperties.put(l, new HashMap<String,GWTJahiaNodeProperty>());
                                    }
                                    changedProperties.get(l).put(prop.getName(), prop.cloneObject());
                                }
                            }
                        }
                    }
                };
                propertiesEditor.setLocale(language);
                propertiesEditor.setMixin(engine.getMixin());
                propertiesEditor.setChoiceListInitializersValues(engine.getChoiceListInitializersValues());
                propertiesEditor.setDefaultValues(engine.getDefaultValues().get(language));
                // todo : handle translation permission for i18n fields ?
                propertiesEditor.setWriteable(!engine.isExistingNode() || (PermissionsUtils.isPermitted("jcr:modifyProperties", engine.getNode()) && !engine.getNode().isLocked()));
                propertiesEditor.setFieldSetGrouping(true);
                propertiesEditor.setExcludedTypes(excludedTypes);
                propertiesEditor.setViewCopyToAllLangs(viewCopyToAllLangs);
                propertiesEditor.setMultipleEdit(engine.isMultipleSelection());
                propertiesEditor.setDisplayHiddenProperties(engine.getLinker().isDisplayHiddenProperties());
                propertiesEditor.addStyleName("JahiaGxtEditEnginePanel-" + gwtEngineTab.getId() + "-"+language);
                if (engine.getNode() != null) {
                    propertiesEditor.setPermissions(engine.getNode().getPermissions());
                } else if (engine.getTargetNode() != null ) {
                    propertiesEditor.setPermissions(engine.getTargetNode().getPermissions());
                }
                propertiesEditor.renderNewFormPanel();
                for (final Field field : propertiesEditor.getFields()) {
                    if (field instanceof ContentPickerField) {
                        final String labelSep = field.getLabelSeparator();
                        if (engine.getReferencesWarnings() != null && engine.getReferencesWarnings().containsKey(field.getName())) {
                            field.setLabelSeparator(labelSep + " <img width='11px' height='11px' src='" + JahiaGWTParameters
                                    .getContextPath() + "/gwt/resources/images/default/shared/warning.gif'/> Warning : these users/groups might not view the reference "+engine.getReferencesWarnings().get(field.getName()));
                            field.setFieldLabel(field.getFieldLabel());
                        }
                        field.setFireChangeEventOnSetValue(true);
                        field.addListener(Events.Change, new Listener<FieldEvent>() {
                            public void handleEvent(FieldEvent be) {
                                final List<GWTJahiaNode> selectedNodes = (List<GWTJahiaNode>) be.getValue();
                                if (selectedNodes != null && !selectedNodes.isEmpty()) {
                                    JahiaContentManagementService.App.getInstance().compareAcl(engine.getAcl(),
                                            selectedNodes, new BaseAsyncCallback<Set<String>>() {
                                        public void onSuccess(Set<String> result) {
                                            if (!result.isEmpty()) {
                                                field.setLabelSeparator(labelSep != null ? labelSep : "" + " <img width='11px' height='11px' src='" + JahiaGWTParameters
                                                        .getContextPath() + "/gwt/resources/images/default/shared/warning.gif'/> Warning : these users/groups might not view the reference "+result);
                                                field.setFieldLabel(field.getFieldLabel());
                                            } else {
                                                if (labelSep != null) {
                                                    field.setLabelSeparator(labelSep);
                                                }
                                                field.setFieldLabel(field.getFieldLabel());
                                            }
                                        }
                                    });
                                } else {
                                    if (labelSep != null) {
                                        field.setLabelSeparator(labelSep);
                                    }
                                    field.setFieldLabel(field.getFieldLabel());
                                }
                            }
                        });
                    }
                }
                setPropertiesEditorByLang(language);

                if (engine.getPresetProperties() != null && !engine.getPresetProperties().isEmpty()) {
                    for (String k : engine.getPresetProperties().keySet()) {
                        if (propertiesEditor.getFieldsMap().containsKey(k))  {
                            propertiesEditor.getFieldsMap().get(k).setDirty(true);
                        }
                    }
                }

                attachPropertiesEditor(engine, tab);
                if (propertiesEditor.getFieldsMap().containsKey("jcr:title")) {
                    Field title = propertiesEditor.getFieldsMap().get("jcr:title");
                    title.focus();
                }
            }

            // synch non18n properties
            if (previousNon18nProperties != null && !previousNon18nProperties.isEmpty()) {
                Map<String, PropertiesEditor.PropertyAdapterField> fieldsMap = propertiesEditor.getFieldsMap();
                for (GWTJahiaNodeProperty property : previousNon18nProperties) {
                    if (fieldsMap.containsKey(property.getName()))  {
                        FormFieldCreator.fillValue(fieldsMap.get(property.getName()).getField(), propertiesEditor.getGWTJahiaItemDefinition(property), property, null, null);
                    }
                }
            }
            if (previousAddedTypes != null) {
                Map<String, FieldSet> f = propertiesEditor.getFieldSetsMap();
                propertiesEditor.getAddedTypes().addAll(previousAddedTypes);
                propertiesEditor.getRemovedTypes().addAll(previousRemovedTypes);
                for (String addedType : previousAddedTypes) {
                    f.get(addedType).expand();
                }
                for (String removedType : previousRemovedTypes) {
                    if (f.containsKey(removedType)) {
                        f.get(removedType).collapse();
                    }
                }
            }

            if (changedProperties.containsKey(language)) {
                for (Map.Entry<String, GWTJahiaNodeProperty> entry : changedProperties.get(language).entrySet()) {
                    if (propertiesEditor.getFieldsMap().containsKey(entry.getKey())) {
                        propertiesEditor.getFieldsMap().get(entry.getKey()).setDirty(true);
                    }
                }
                changedProperties.get(language).clear();
            }

            propertiesEditor.setVisible(true);
            tab.layout();
        }
    }


    public void focusFirstField() {
        if (propertiesEditor != null && propertiesEditor.getFields() !=null) {
            for (Field f : propertiesEditor.getFields()) {
                if (f instanceof PropertiesEditor.PropertyAdapterField && f.isEnabled() && f.isVisible()) {
                    Field field = ((PropertiesEditor.PropertyAdapterField) f).getField();
                    field.focus();
                    if (field instanceof TextField) {
                            TextField textField = (TextField) field;
                            textField.setCursorPos(textField.getValue()!=null?textField.getValue().toString().length():0);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Warning: this current layout is a FitLayout. That means that if you overide this method in order to add other subelement, you have to use a wrapper.
     * See ContentTabItem as an example of overriding
     *
     * call after created:
     * @param engine
     * @param tab
     */
    public void attachPropertiesEditor(NodeHolder engine, AsyncTabItem tab) {
        tab.add(propertiesEditor);
        tab.layout();
    }

    public boolean isMultiLang() {
        return multiLang;
    }

    public void setMultiLang(boolean multiLang) {
        this.multiLang = multiLang;
    }

    public void setViewCopyToAllLangs(boolean viewCopyToAllLangs) {
        this.viewCopyToAllLangs = viewCopyToAllLangs;
    }

    /**
     * Get lang properties per map
     *
     * @return
     * @param modifiedOnly
     */
    public List<GWTJahiaNodeProperty> getLanguageProperties(boolean modifiedOnly, String language) {
        if (langPropertiesEditorMap.containsKey(language)) {
            return langPropertiesEditorMap.get(language).getProperties(true, false, modifiedOnly);
        } else if (changedProperties.containsKey(language)) {
            return new ArrayList<GWTJahiaNodeProperty>(changedProperties.get(language).values());
        }

        return new ArrayList<GWTJahiaNodeProperty>();
    }

    @Override
    public void setProcessed(boolean processed) {
        if (!processed) {
            if (langPropertiesEditorMap != null) {
                langPropertiesEditorMap.clear();
            }
            if (changedProperties != null) {
                changedProperties.clear();
            }
            propertiesEditor = null;
        }
        super.setProcessed(processed);
    }

    @Override public boolean isHandleMultipleSelection() {
        return true;
    }

    public void setDataType(List<String> dataType) {
        this.dataType = dataType;
    }

    public void setExcludedTypes(List<String> excludedTypes) {
        this.excludedTypes = excludedTypes;
    }

    @Override
    public void doValidate(List<EngineValidation.ValidateResult> validateResult, NodeHolder engine, TabItem tab, String selectedLanguage, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, TabPanel tabs) {
        PropertiesEditor pe = getPropertiesEditor();
        if (pe != null) {
            for (PropertiesEditor.PropertyAdapterField adapterField : pe.getFieldsMap().values()) {
                Field<?> field = adapterField.getField();
                if (field.isEnabled() && !field.isReadOnly() && !field.validate() && adapterField.getParent() != null && ((FieldSet)adapterField.getParent()).isExpanded()) {
                    EngineValidation.ValidateResult result = new EngineValidation.ValidateResult();
                    result.errorTab = tab;
                    result.errorField = field;
                    result.canIgnore = field.getData("optionalValidation") != null && (Boolean) field.getData("optionalValidation");
                    validateResult.add(result);
                    break;
                }
            }
        }

        // handle multilang
        if (isMultiLang()) {
            // for now only contentTabItem  has multilang. properties
            if (selectedLanguage != null) {
                final String lang = selectedLanguage;
                for (String language : changedI18NProperties.keySet()) {
                    if (!lang.equals(language)) {
                        PropertiesEditor lpe = getPropertiesEditorByLang(language);
                        if (lpe != null) {
                            for (PropertiesEditor.PropertyAdapterField adapterField : lpe.getFieldsMap().values()) {
                                Field<?> field = adapterField.getField();
                                if (field.isEnabled() && !field.isReadOnly() && !field.validate() && ((FieldSet)adapterField.getParent()).isExpanded() && adapterField.getDefinition().isInternationalized()) {
                                    EngineValidation.ValidateResult result = new EngineValidation.ValidateResult();
                                    result.errorTab = tab;
                                    result.errorField = field;
                                    result.canIgnore = false;
                                    result.errorLang = language;
                                    validateResult.add(result);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, List<GWTJahiaNode> chidren, GWTJahiaNodeACL acl) {
        PropertiesTabItem propertiesTabItem = this;
        PropertiesEditor pe = propertiesTabItem.getPropertiesEditor();
//        if (pe != null && node != null) {
//            //properties.addAll(pe.getProperties());
//            node.getNodeTypes().removeAll(pe.getRemovedTypes());
//            node.getNodeTypes().addAll(pe.getAddedTypes());
//            node.getNodeTypes().addAll(pe.getExternalMixin());
//        }
        if (pe != null) {
            addedTypes.addAll(pe.getAddedTypes());
            addedTypes.addAll(pe.getExternalMixin());
        }

        if (isMultiLang()) {
            // for now only contentTabItem  has multilang. properties
            Set<String> set = new HashSet<String>(langPropertiesEditorMap.keySet());
            set.addAll(this.changedProperties.keySet());
            for (String lang : set) {
                if (!changedI18NProperties.containsKey(lang)) {
                    changedI18NProperties.put(lang, new ArrayList<GWTJahiaNodeProperty>());
                }
                changedI18NProperties.get(lang).addAll(getLanguageProperties(node != null, lang));
            }
            if (propertiesEditor != null) {
                changedProperties.addAll(propertiesEditor.getProperties(false, true, node != null));
            }
        } else {
            if (propertiesEditor != null) {
                changedProperties.addAll(propertiesEditor.getProperties(true, true, node != null));
            }
        }

        if (pe != null) {
            removedTypes.addAll(pe.getRemovedTypes());
        }

    }
}
