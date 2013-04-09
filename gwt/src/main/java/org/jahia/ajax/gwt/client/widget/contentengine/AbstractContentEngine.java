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
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.DualListField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaFieldInitializer;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.AreaModule;
import org.jahia.ajax.gwt.client.widget.toolbar.action.LanguageSwitcherActionItem;

import java.util.*;

/**
 * Abstract Method for Content Engine
 * User: toto
 * Date: Jan 7, 2010
 * Time: 1:57:03 PM
 */
public abstract class AbstractContentEngine extends LayoutContainer implements NodeHolder {

    protected GWTEngineConfiguration config;
    protected Linker linker = null;
    protected List<GWTJahiaNodeType> nodeTypes;
    protected List<GWTJahiaNodeType> mixin;
    protected Map<String, GWTJahiaFieldInitializer> initializersValues;
    protected Map<String, GWTJahiaNodeProperty> properties = new HashMap<String, GWTJahiaNodeProperty>();
    protected Map<String, GWTJahiaNodeProperty> presetProperties = new HashMap<String, GWTJahiaNodeProperty>();
    protected TabPanel tabs;
    protected boolean existingNode = true;
    protected GWTJahiaNode node;
    protected String nodeName;
    protected GWTJahiaNode targetNode;
    protected GWTJahiaLanguage currentLanguageBean;
    protected String defaultLanguageCode;
    protected ComboBox<GWTJahiaLanguage> languageSwitcher;
    protected ButtonBar buttonBar;
    protected String heading;
    protected EngineContainer container;
    protected GWTJahiaNodeACL acl;
    protected Map<String,Set<String>> referencesWarnings;
    protected GWTJahiaLanguage language;

    // general properties
    protected final List<GWTJahiaNodeProperty> changedProperties = new ArrayList<GWTJahiaNodeProperty>();

    // general properties
    protected final Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties = new HashMap<String, List<GWTJahiaNodeProperty>>();

    protected String parentPath;


    protected AbstractContentEngine(GWTEngineConfiguration config, Linker linker, String parentPath) {
        this.config = config;
        this.linker = linker;
        this.parentPath = parentPath;
        setId("JahiaGxtContentEngine");
    }

    protected void init(EngineContainer container) {
        this.container = container;
        setLayout(new FillLayout());

        buttonBar = new ButtonBar();
        buttonBar.addStyleName("JahiaEditEngineButtonBar");

        container.setEngine(this, heading, buttonBar, this.getLinker());

        // init language switcher
        initLanguageSwitcher();
        // init tabs
        tabs = new TabPanel();

        tabs.setBodyBorder(false);
        tabs.setBorders(true);

        add(tabs);

        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        initFooter();

        container.getPanel().setFooter(true);
        loading();
    }

    /**
     * Called when the engine is loaded
     */
    public void loaded() {
        unmask();
    }

    /**
     * Called when the engine start to be loaded
     */
    public void loading() {
        mask(Messages.get("label.loading", "Loading..."), "x-mask-loading");
    }

    /**
     * init language switcher
     */
    private void initLanguageSwitcher() {
        languageSwitcher = new ComboBox<GWTJahiaLanguage>();
        languageSwitcher.setStore(new ListStore<GWTJahiaLanguage>());
        languageSwitcher.setDisplayField("displayName");
        languageSwitcher.setVisible(false);
        languageSwitcher.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaLanguage>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaLanguage> event) {
                if(!event.getSelectedItem().getPropertyNames().contains("active") || event.getSelectedItem().isActive()) {
                    GWTJahiaLanguage previous = language;
                    language = event.getSelectedItem();
                    onLanguageChange(previous);
                } else {
                    languageSwitcher.select(language);
                    languageSwitcher.setSelection(Arrays.asList(language));
                    languageSwitcher.getView().refresh();
                }
            }
        });
        languageSwitcher.setTemplate(LanguageSwitcherActionItem.getLangSwitchingTemplate());
        languageSwitcher.setTypeAhead(true);
        languageSwitcher.setTriggerAction(ComboBox.TriggerAction.ALL);
        languageSwitcher.setForceSelection(true);
        container.getPanel().getHeader().addTool(languageSwitcher);
    }

    /**
     * Called when a new language has been selected
     * @param previous
     */
    protected void onLanguageChange(GWTJahiaLanguage previous) {

    }

    /**
     * Set availableLanguages
     *
     * @param languages
     */
    protected void setAvailableLanguages(List<GWTJahiaLanguage> languages) {
        if (languageSwitcher != null && !languageSwitcher.isVisible()) {
            //languageSwitcher.getStore().removeAll();
            if (languages != null && !languages.isEmpty()) {
                languageSwitcher.getStore().add(languages);
                List<GWTJahiaLanguage> selected = new ArrayList<GWTJahiaLanguage>();
                selected.add(currentLanguageBean);
                languageSwitcher.setSelection(selected);
                if (languages.size() > 1) {
                    languageSwitcher.setVisible(true);
                }
            } else {
                languageSwitcher.setVisible(false);
            }
        } else {
            Log.debug("Language switcher disabled.");
        }
    }

    /**
     * init footer
     */
    protected abstract void initFooter();

    /**
     * fill current tab
     */
    protected void fillCurrentTab() {
        TabItem currentTab = tabs.getSelectedItem();
        if (currentTab != null) {
            Object currentTabItem = currentTab.getData("item");
            if (currentTabItem != null && currentTabItem instanceof EditEngineTabItem) {
                EditEngineTabItem engineTabItem = (EditEngineTabItem) currentTabItem;

                if (!((AsyncTabItem)currentTab).isProcessed()) {
                    boolean isNewPropertiesEditor = false;
                
                    if (engineTabItem instanceof PropertiesTabItem) {
                        isNewPropertiesEditor = (((PropertiesTabItem) engineTabItem).getPropertiesEditorByLang(getSelectedLanguage()) == null);
                    }

                    if (node != null && linker instanceof EditLinker && ((EditLinker) linker).getSelectedModule() instanceof AreaModule &&
                            node.equals(((EditLinker) linker).getSelectedModule().getNode())) {
                        // Editing an area, no rename allowed here
                        for (TabItem tabItem : tabs.getItems()) {
                            if (engineTabItem instanceof ContentTabItem) {
                                ((ContentTabItem) engineTabItem).setNameEditable(false);
                            }
                        }
                    }

                    engineTabItem.init(this, (AsyncTabItem) currentTab, getSelectedLanguage());
                    if (isNewPropertiesEditor) {
                        initDynamicListInitializers(engineTabItem);
                    }
                }
                focusFirstField();
            }
        }
    }

    protected void focusFirstField() {
        TabItem currentTab = tabs.getSelectedItem();
        if (currentTab != null) {
            Object currentTabItem = currentTab.getData("item");
            if (currentTabItem != null && currentTabItem instanceof EditEngineTabItem) {
                EditEngineTabItem engineTabItem = (EditEngineTabItem) currentTabItem;
                if (engineTabItem instanceof PropertiesTabItem) {
                    ((PropertiesTabItem) engineTabItem).focusFirstField();
                }
            }
        }
    }

    protected void initDynamicListInitializers(EditEngineTabItem tabItem) {
        if (initializersValues != null && tabItem instanceof PropertiesTabItem) {
            PropertiesEditor pe = ((PropertiesTabItem) tabItem).getPropertiesEditor();
            for (Map.Entry<String, GWTJahiaFieldInitializer> initializer : initializersValues
                    .entrySet()) {
                if (initializer.getValue().getDependentProperties() != null) {
                    for (String dependentProperty : initializer.getValue().getDependentProperties()) {
                        if (pe.getFieldsMap().containsKey(dependentProperty)) {
                            final Field<?> dependentField = pe.getFieldsMap().get(dependentProperty).getField();
                            if (dependentField != null) {
                                initDynamicInitializer(dependentField, initializer.getKey(), pe, initializer.getValue().getDependentProperties());
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected void initDynamicInitializer(final Field<?> dependentField, final String propertyId,
            final PropertiesEditor pe, final List<String> dependentProperties) {
        if (dependentField instanceof DualListField<?>) {
            ((DualListField<GWTJahiaValueDisplayBean>) dependentField).getToList().getStore()
                    .addStoreListener(new StoreListener<GWTJahiaValueDisplayBean>() {
                        public void handleEvent(StoreEvent<GWTJahiaValueDisplayBean> event) {
                            refillDependantListWidgetOn(dependentField, propertyId,
                                    dependentProperties);
                        }
                    });
        } else {
            dependentField.addListener(Events.Change, new Listener<FieldEvent>() {
                public void handleEvent(FieldEvent event) {
                    refillDependantListWidgetOn(dependentField, propertyId, dependentProperties);
                }
            });
        }
    }
    
    protected void refillDependantListWidgetOn(final Field<?> dependentField, final String propertyId,
            final List<String> dependentProperties) {
        final String nodeTypeName = propertyId.substring(0, propertyId.indexOf('.'));
        final String propertyName = propertyId.substring(propertyId.indexOf('.') + 1);
        Map<String, List<GWTJahiaNodePropertyValue>> dependentValues = new HashMap<String, List<GWTJahiaNodePropertyValue>>();
        for (TabItem tab : tabs.getItems()) {
            EditEngineTabItem item = tab.getData("item");
            if (item instanceof PropertiesTabItem) {
                for (PropertiesEditor pe : ((PropertiesTabItem) item).getLangPropertiesEditorMap().values()) {
                    if (pe != null) {
                        for (Field<?> field : pe.getFields()) {
                            if (dependentProperties.contains(field.getName())) {
                                dependentValues.put(field.getName(), PropertiesEditor
                                        .getPropertyValues(field, pe
                                                .getGWTJahiaItemDefinition(field.getName())));
                            }
                        }
                    }
                }
            }
        }

        JahiaContentManagementService.App.getInstance().getFieldInitializerValues(nodeTypeName, propertyName, parentPath,
                dependentValues, new BaseAsyncCallback<GWTJahiaFieldInitializer>() {
                    public void onSuccess(GWTJahiaFieldInitializer result) {
                        initializersValues.put(propertyId, result);
                        if (result.getDisplayValues() != null) {
                            for (TabItem tab : tabs.getItems()) {
                                EditEngineTabItem item = tab.getData("item");
                                if (item instanceof PropertiesTabItem) {
                                    PropertiesEditor pe = ((PropertiesTabItem) item)
                                            .getPropertiesEditor();
                                    if (pe != null) {
                                        for (Field<?> field : pe.getFields()) {
                                            if (field instanceof PropertiesEditor.PropertyAdapterField) {
                                                field = ((PropertiesEditor.PropertyAdapterField)field).getField();
                                            }
                                            if (propertyName.equals(field.getName())) {
                                                if (field instanceof DualListField<?>) {
                                                    DualListField<GWTJahiaValueDisplayBean> dualListField = (DualListField<GWTJahiaValueDisplayBean>) field;
                                                    ListStore<GWTJahiaValueDisplayBean> store = dualListField.getToField().getStore();
                                                    for (GWTJahiaValueDisplayBean toValue : store.getModels()) {
                                                        if (!result.getDisplayValues()
                                                                .contains(toValue)) {
                                                            store.remove(toValue);
                                                        }
                                                    }
                                                    dualListField.getToField().getListView().refresh();
                                                    
                                                    store = dualListField.getFromField().getStore();
                                                    store.removeAll();
                                                    store.add(result.getDisplayValues());
                                                    dualListField.getFromField().getListView().refresh();
                                                } else if (field instanceof ComboBox<?>) {
                                                    ComboBox<GWTJahiaValueDisplayBean> comboBox = (ComboBox<GWTJahiaValueDisplayBean>) field;
                                                    if (comboBox.getValue() != null
                                                            && !result
                                                                    .getDisplayValues()
                                                                    .contains(
                                                                            comboBox.getValue())) {
                                                    	try {
                                                           comboBox.clear();
                                                    	}catch(Exception ex) {
                                                    		/*
                                                    		 * it could happen that the combobox is empty and so exception is thrown
                                                    		 * and combobox isn't reinitialized
                                                    		 */
                                                    	}
                                                    }
                                                    ListStore<GWTJahiaValueDisplayBean> store = new ListStore<GWTJahiaValueDisplayBean>();
                                                    store.add(result.getDisplayValues());
                                                    comboBox.setStore(store);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    public void onApplicationFailure(Throwable caught) {
                        Log.error("Unable to load avalibale mixin", caught);
                    }
                });

    }

    public Linker getLinker() {
        return linker;
    }

    public List<GWTJahiaNodeType> getNodeTypes() {
        return nodeTypes;
    }

    public List<GWTJahiaNodeType> getMixin() {
        return mixin;
    }

    public Map<String, GWTJahiaFieldInitializer> getInitializersValues() {
        return initializersValues;
    }

    public Map<String, GWTJahiaNodeProperty> getProperties() {
        return properties;
    }

    public Map<String, GWTJahiaNodeProperty> getPresetProperties() {
        return presetProperties;
    }

    public GWTJahiaNode getNode() {
        return node;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getDefaultLanguageCode() {
        return defaultLanguageCode;
    }

    public GWTJahiaNodeACL getAcl() {
        return acl;
    }

    public Map<String, Set<String>> getReferencesWarnings() {
        return referencesWarnings;
    }

    public GWTJahiaNode getTargetNode() {
        return targetNode;
    }

    public boolean isExistingNode() {
        return existingNode;
    }

    public boolean isMultipleSelection() {
        return false;
    }

    public List<GWTJahiaNode> getNodes() {
        return Arrays.asList(node);
    }

    /**
     * Get Selected Lang
     *
     * @return
     */
    public String getSelectedLanguage() {
        if (language != null) {
            return language.getLanguage();
        } else {
            return JahiaGWTParameters.getLanguage();
        }
    }

    public abstract void setButtonsEnabled(boolean doEnable);

    /**
     * Get the language switcher combo box associated with this engine.
     * @return the language switcher combo box instance.
     */
    public ComboBox<GWTJahiaLanguage> getLanguageSwitcher() {
        return languageSwitcher;
    }

    public TabPanel getTabs() {
        return tabs;
    }

    public List<GWTJahiaNodeProperty> getChangedProperties() {
        return changedProperties;
    }

    public Map<String, List<GWTJahiaNodeProperty>> getChangedI18NProperties() {
        return changedI18NProperties;
    }

    public String getParentPath() {
        return parentPath;
    }

}
