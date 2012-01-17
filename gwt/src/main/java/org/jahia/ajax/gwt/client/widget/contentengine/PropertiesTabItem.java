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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
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
    protected transient boolean multiLang = true;

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
                propertiesEditor = new PropertiesEditor(engine.getNodeTypes(), properties, dataType) {
                    @Override
                    public void copyToAllLanguages(GWTJahiaNodeProperty prop) {
                        for (GWTJahiaLanguage jahiaLanguage : JahiaGWTParameters.getSiteLanguages()) {
                            String l = jahiaLanguage.getLanguage();
                            if (!l.equals(PropertiesTabItem.this.language)) {
                                if (langPropertiesEditorMap.containsKey(l)) {
                                    Field<?> f = langPropertiesEditorMap.get(l).getFieldsMap().get(prop.getName()).getField();
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
                propertiesEditor.setInitializersValues(engine.getInitializersValues());
                // todo : handle translation permission for i18n fields ?
                propertiesEditor.setWriteable(!engine.isExistingNode() || (PermissionsUtils.isPermitted("jcr:modifyProperties", engine.getNode()) && !engine.getNode().isLocked()));
                propertiesEditor.setFieldSetGrouping(true);
                propertiesEditor.setExcludedTypes(excludedTypes);
                propertiesEditor.setViewCopyToAllLangs(true);
                propertiesEditor.setMultipleEdit(engine.isMultipleSelection());
                propertiesEditor.setDisplayHiddenProperties(engine.getLinker().isDisplayHiddenProperties());
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

                if (changedProperties.containsKey(language)) {
                    for (Map.Entry<String, GWTJahiaNodeProperty> entry : changedProperties.get(language).entrySet()) {
                        propertiesEditor.getFieldsMap().get(entry.getKey()).setDirty(true);
                    }
                }
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
                        FormFieldCreator.fillValue(fieldsMap.get(property.getName()).getField(), propertiesEditor.getGWTJahiaItemDefinition(property), property, null);
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
                for (String addedType : previousRemovedTypes) {
                    f.get(addedType).collapse();
                }
            }

            propertiesEditor.setVisible(true);

            tab.layout();
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

    public void setProcessed(boolean processed) {
        if (!processed && langPropertiesEditorMap != null) {
            langPropertiesEditorMap.clear();
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
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, GWTJahiaNodeACL acl) {
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
