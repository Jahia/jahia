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
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.DualListField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
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
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaPropertyDefinition;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.data.wcag.WCAGValidationResult;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.AreaModule;
import org.jahia.ajax.gwt.client.widget.form.CKEditorField;
import org.jahia.ajax.gwt.client.widget.toolbar.action.LanguageSwitcherActionItem;

import java.util.*;

/**
 * 
 * User: toto
 * Date: Jan 7, 2010
 * Time: 1:57:03 PM
 * 
 */
public abstract class AbstractContentEngine extends LayoutContainer implements NodeHolder {
    public static final int BUTTON_HEIGHT = 24;

    protected static JahiaContentManagementServiceAsync contentService;
    protected List<GWTEngineTab> config;
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


    protected AbstractContentEngine(List<GWTEngineTab> config, Linker linker, String parentPath) {
        this.config = config;
        this.linker = linker;
        this.parentPath = parentPath;
        contentService = JahiaContentManagementService.App.getInstance();
        setId("JahiaGxtContentEngine");
    }

    protected void init(EngineContainer container) {
        this.container = container;
        setLayout(new FillLayout());

        buttonBar = new ButtonBar();

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
                GWTJahiaLanguage previous = language;
                language = event.getSelectedItem();
                onLanguageChange(previous);
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

        contentService.getFieldInitializerValues(nodeTypeName, propertyName, parentPath,
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
                                                        comboBox.clear();
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

    protected Map<String, CKEditorField> getFieldsForWCAGValidation() {
    	Map<String, CKEditorField> fieldsToValidate = new HashMap<String, CKEditorField>();
    	
        for (TabItem tab : tabs.getItems()) {
            EditEngineTabItem item = tab.getData("item");
            if (item instanceof PropertiesTabItem) {
                Map<String, PropertiesEditor> langPropertiesEditorMap = ((PropertiesTabItem) item).getLangPropertiesEditorMap();
                for (PropertiesEditor pe : langPropertiesEditorMap.values()) {
                    if (pe != null) {
                        for (PropertiesEditor.PropertyAdapterField adapterField : pe.getFieldsMap().values()) {
                            Field<?> field = adapterField.getField();
                            if ((field instanceof CKEditorField) && field.isEnabled() && !field.isReadOnly() && ((FieldSet)adapterField.getParent()).isExpanded()) {
                            	CKEditorField ckfield = (CKEditorField) field;
                            	if (ckfield.isIgnoreWcagWarnings()) {
                            		continue;
                            	}
                            	if (ckfield.getItemId() == null) {
                            		ckfield.setItemId("CKEditorField-" + (fieldsToValidate.size() + 1));
                            	}
                            	String text = ckfield.getRawValue();
                            	if (text != null && text.trim().length() > 0) {
                            		fieldsToValidate.put(field.getItemId(), ckfield);
                            	}
                            }
                        }
                    }
                }
            }
        }
        
        return fieldsToValidate;
    }
    
    protected void save(final boolean closeAfterSave) {
        mask(Messages.get("label.saving","Saving..."), "x-mask-loading");
        setButtonsEnabled(false);
        
    	if (validateData()) {
    	    Map<String, String> textForWCAGValidation = null;
    	    Map<String, CKEditorField> toValidate = null;
    	    
    	    if (node != null && node.isWCAGComplianceCheckEnabled() || node == null && targetNode.isWCAGComplianceCheckEnabled()) {
        		// validation passes, let's get WCAG texts to validate
        		toValidate = getFieldsForWCAGValidation();
        		textForWCAGValidation = new HashMap<String, String>(toValidate.size());
        		for (Map.Entry<String, CKEditorField> fieldEntry : toValidate.entrySet()) {
        			textForWCAGValidation.put(fieldEntry.getKey(), fieldEntry.getValue().getRawValue());
                }
    	    }
            if (textForWCAGValidation != null && !textForWCAGValidation.isEmpty()) {
                final Map<String, CKEditorField> fieldsForValidation = toValidate; 
            	// we have texts to validate against WCAG rules
            	contentService.validateWCAG(textForWCAGValidation, new BaseAsyncCallback<Map<String,WCAGValidationResult>>() {
    				public void onSuccess(Map<String, WCAGValidationResult> result) {
    	                boolean wcagOK = true;
    	                for (Map.Entry<String, WCAGValidationResult> wcagEntry : result.entrySet()) {
	                        if (!wcagEntry.getValue().isEmpty()) {
	                        	wcagOK = false;
	                        	CKEditorField fld = fieldsForValidation.get(wcagEntry.getKey());
	                        	if (fld != null) {
	                        		fld.setWcagValidationResult(wcagEntry.getValue());
	                        	}
	                        } 
                        }
    	                if (wcagOK) {
    	                	// WCAG checks are OK
    	                	prepareAndSave(closeAfterSave);
    	                } else {
    	                	validateData();
    	                }
                    }
    				@Override
    				public void onApplicationFailure(Throwable caught) {
    				    super.onApplicationFailure(caught);
    				    // unable to do WCAG check, skipping
	                	prepareAndSave(closeAfterSave);
    				}
    			});
            } else {
            	prepareAndSave(closeAfterSave);
            }
    	}
    }
    
    protected abstract void prepareAndSave(boolean closeAfterSave);
    
	protected boolean validateData() {
        boolean allValid = true;
        TabItem firstErrorTab = null;
        Field<?> firstErrorField = null;
        GWTJahiaLanguage firstErrorLang = null;
        for (TabItem tab : tabs.getItems()) {
            EditEngineTabItem item = tab.getData("item");
            if (item instanceof PropertiesTabItem) {
                PropertiesTabItem propertiesTabItem = (PropertiesTabItem) item;
                PropertiesEditor pe = ((PropertiesTabItem) item).getPropertiesEditor();
                if (pe != null) {
                    for (PropertiesEditor.PropertyAdapterField adapterField : pe.getFieldsMap().values()) {
                        Field<?> field = adapterField.getField();
                        if (field.isEnabled() && !field.isReadOnly() && !field.validate() && ((FieldSet)adapterField.getParent()).isExpanded()) {
                            if (allValid || tab.equals(tabs.getSelectedItem())
                                    && !tab.equals(firstErrorTab)) {
                                firstErrorTab = tab;
                                firstErrorField = field;
                            }
                            allValid = false;
                        }
                    }
                    if (!allValid) {
                        continue;
                    }
                }

                // handle multilang
                if (propertiesTabItem.isMultiLang()) {
                    // for now only contentTabItem  has multilang. properties
                    if (getSelectedLanguage() != null) {
                        final String lang = getSelectedLanguage();
                        for (String language : changedI18NProperties.keySet()) {
                            if (!lang.equals(language)) {
                                PropertiesEditor lpe = propertiesTabItem.getPropertiesEditorByLang(language);
                                if (lpe != null) {
                                    for (PropertiesEditor.PropertyAdapterField adapterField : lpe.getFieldsMap().values()) {
                                        Field<?> field = adapterField.getField();
                                        if (field.isEnabled() && !field.isReadOnly() && !field.validate() && ((FieldSet)adapterField.getParent()).isExpanded() && adapterField.getDefinition().isInternationalized()) {
                                            if (allValid || tab.equals(tabs.getSelectedItem())
                                                    && !tab.equals(firstErrorTab)) {
                                                firstErrorTab = tab;
                                                firstErrorField = field;
                                            }
                                            allValid = false;
                                        }
                                    }
                                    if (!allValid) {
                                        for (GWTJahiaLanguage gwtLang : languageSwitcher.getStore().getModels()) {
                                            if (language.equals(gwtLang.getLanguage())) {
                                                firstErrorLang = gwtLang;
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!allValid) {
			MessageBox.alert(Messages.get("label.error", "Error"),
			        Messages.get("failure.invalid.constraint.label",
			                "There are some validation errors!"
			                        + " Click on the information icon next to the"
			                        + " highlighted fields, correct the input and save again."),
			        null);
            if (firstErrorLang != null) {
                languageSwitcher.setValue(firstErrorLang);                
            }
            if (firstErrorTab != null && !tabs.getSelectedItem().equals(firstErrorTab)) {
                tabs.setSelection(firstErrorTab);
            }
            if (firstErrorField != null) {
                firstErrorField.focus();
            }
            firstErrorTab.layout();
            unmask();
            setButtonsEnabled(true);
            return false;
        } else {
        	return true;
        }
    }

	protected abstract void setButtonsEnabled(boolean doEnable);


}
