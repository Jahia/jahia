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
package org.jahia.ajax.gwt.client.widget.definition;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTChoiceListInitializer;
import org.jahia.ajax.gwt.client.data.GWTJahiaEditEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.toolbar.action.LanguageSwitcherActionItem;

import java.util.*;

/**
 *
 * editor for multi languages properties
 *
 */
public class LangPropertiesEditor extends LayoutContainer {
    private static JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();

    // ui vars
    private PropertiesEditor displayedPropertiesEditor;
    private Map<String, PropertiesEditor> langPropertiesEditorMap;
    private List<String> excludedTypes;

    // current node contex variables
    private List<String> dataType;
    private List<GWTJahiaNodeType> nodeTypes;
    private List<GWTJahiaNodeType> mixin;
    private Map<String, GWTChoiceListInitializer> initializersValues;
    private Map<String, GWTJahiaNodeProperty> properties;
    private GWTJahiaNode node;
    private ComboBox<GWTJahiaLanguage> languageSwitcher;
    private LayoutContainer mainPanel;
    private boolean editable = true;
    private GWTJahiaLanguage displayedLocale = null;
    private LayoutContainer topBar;
    private boolean translationEnabled;
    private LangPropertiesEditor translationSource;
    private CallBack callback;
    // flag used when translationSource is used and is not fully loaded when we try to read data from it.
    private boolean needRefresh = false;

    public LangPropertiesEditor(GWTJahiaNode node, List<String> dataType, boolean editable,
                                GWTJahiaLanguage displayedLanguage, CallBack callback) {
        this(node, dataType, editable, displayedLanguage, null, callback);
    }

    public LangPropertiesEditor(GWTJahiaNode node, List<String> dataType, boolean editable,
                                GWTJahiaLanguage displayedLanguage, LangPropertiesEditor translationSource, CallBack callBack) {
        this.node = node;
        this.dataType = dataType;
        langPropertiesEditorMap = new HashMap<String, PropertiesEditor>();
        this.editable = editable;
        this.translationSource = translationSource;
        this.callback = callBack;

        setScrollMode(Style.Scroll.NONE);
        setBorders(false);
        setLayout(new BorderLayout());

        LayoutContainer top = new LayoutContainer(new FlowLayout());
        topBar = new HorizontalPanel();

        // add switching form
        languageSwitcher = createLanguageSwitcher();
        topBar.add(languageSwitcher);

        top.add(topBar, new FlowData(5));
        add(top, new BorderLayoutData(Style.LayoutRegion.NORTH, 25));

        // update node info
        loadEngine(displayedLanguage);
    }

    public LayoutContainer getTopBar() {
        return topBar;
    }

    public GWTJahiaLanguage getDisplayedLocale() {
        return displayedLocale;
    }

    /**
     * Get properties editor by langCode
     *
     * @param locale
     * @return
     */
    public PropertiesEditor getPropertiesEditorByLang(String locale) {
        if (locale == null) {
            return null;
        }
        return langPropertiesEditorMap.get(locale);
    }

    /**
     * set properties editor by lang
     *
     * @param locale
     */
    private void setPropertiesEditorByLang(PropertiesEditor displayedPropertiesEditor, String locale) {
        if (langPropertiesEditorMap == null || locale == null) {
            return;
        }
        langPropertiesEditorMap.put(locale, displayedPropertiesEditor);
    }

    /**
     * Display properties
     */
    public void updatePropertiesComponent(String locale) {
        if (mixin != null) {
            List<GWTJahiaNodeProperty> previousNon18nProperties = null;
            boolean addSharedLangLabel = true;
            if (displayedPropertiesEditor != null) {
                displayedPropertiesEditor.setVisible(false);
                previousNon18nProperties = displayedPropertiesEditor.getProperties(false, true, false);

            }


            if (locale == null) {
                locale = displayedLocale.getLanguage();
            }

            PropertiesEditor langPropertiesEditor = getPropertiesEditorByLang(locale);
            if (langPropertiesEditor == null) {
                if (addSharedLangLabel && node.isShared()) {
                    Label label = new Label(
                            "Important : This is a shared node, editing it will modify its value for all its usages");
                    label.setStyleAttribute("color", "rgb(200,80,80)");
                    label.setStyleAttribute("font-size", "14px");
                    add(label);
                }
                addSharedLangLabel = false;


                //create and update properties editor
                langPropertiesEditor = new PropertiesEditor(nodeTypes, properties, dataType);
                langPropertiesEditor.setLocale(locale);
                langPropertiesEditor.setMixin(mixin);
                langPropertiesEditor.setChoiceListInitializersValues(initializersValues);
                langPropertiesEditor.setNonI18NWriteable(false);
                langPropertiesEditor.setWriteable(editable);
                langPropertiesEditor.setFieldSetGrouping(true);
                langPropertiesEditor.setExcludedTypes(excludedTypes);
                if (node != null) {
                    langPropertiesEditor.setPermissions(node.getPermissions());
                }
                if (translationEnabled && translationSource != null) {
                    langPropertiesEditor.setTranslationSource(translationSource);
                    langPropertiesEditor.setTranslationTarget(this);
                }
                langPropertiesEditor.renderNewFormPanel();
                setPropertiesEditorByLang(langPropertiesEditor, locale);
                mainPanel.add(langPropertiesEditor);
            } else {
                langPropertiesEditor.setVisible(true);
            }

            // synch non18n properties
            if (previousNon18nProperties != null && !previousNon18nProperties.isEmpty()) {
                Map<String, PropertiesEditor.PropertyAdapterField> fieldsMap = langPropertiesEditor.getFieldsMap();
                for (GWTJahiaNodeProperty property : previousNon18nProperties) {
                    FormFieldCreator.fillValue(fieldsMap.get(property.getName()).getField(),
                            langPropertiesEditor.getGWTJahiaItemDefinition(property), property, null, null);
                }
            }


            // update displayed properties
            displayedPropertiesEditor = langPropertiesEditor;

            layout();
        } else {
            Log.debug("mixin is not set");
        }
    }


    /**
     * Create language switcher component
     *
     * @return
     */
    private ComboBox<GWTJahiaLanguage> createLanguageSwitcher() {
        ComboBox<GWTJahiaLanguage> languageSwitcher = new ComboBox<GWTJahiaLanguage>();
        languageSwitcher.setStore(new ListStore<GWTJahiaLanguage>());
        languageSwitcher.setDisplayField("displayName");
        languageSwitcher.setVisible(false);
        languageSwitcher.setTemplate(LanguageSwitcherActionItem.getLangSwitchingTemplate());
        languageSwitcher.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaLanguage>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaLanguage> event) {
                if (!event.getSelectedItem().getLanguage().equalsIgnoreCase(displayedLocale.getLanguage())) {
                    onLanguageSelectionChanged(event.getSelectedItem().getLanguage());
                }
                displayedLocale = event.getSelectedItem();
            }
        });
        languageSwitcher.setTypeAhead(true);
        languageSwitcher.setTriggerAction(ComboBox.TriggerAction.ALL);
        languageSwitcher.setForceSelection(true);
        return languageSwitcher;
    }

    private Button createSuggestTranslationButton() {
        Button button = new Button(Messages.get("label.translate.suggest", "Suggest translation"));
        button.addStyleName("button-suggest");
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                MessageBox.confirm(
                        Messages.get("label.translate.suggest", "Suggest translation"),
                        Messages.get("label.translate.suggest.confirm", "Do you want to replace the content by an automatic translation of it?"),
                        new Listener<MessageBoxEvent>() {
                            public void handleEvent(MessageBoxEvent be) {
                                if (Dialog.YES.equalsIgnoreCase(be.getButtonClicked().getItemId())) {
                                    PropertiesEditor sourcePropertiesEditor = translationSource.getPropertiesEditorByLang(translationSource.getDisplayedLocale().getLanguage());
                                    List<GWTJahiaNodeProperty> props = new ArrayList<GWTJahiaNodeProperty>();
                                    List<GWTJahiaItemDefinition> defs = new ArrayList<GWTJahiaItemDefinition>();
                                    for (GWTJahiaNodeProperty prop : sourcePropertiesEditor.getProperties()) {
                                        GWTJahiaItemDefinition def = sourcePropertiesEditor.getGWTJahiaItemDefinition(prop);
                                        if (((GWTJahiaPropertyDefinition)def).getRequiredType() == GWTJahiaNodePropertyType.STRING
                                                && def.isInternationalized() && !def.isHidden() && !def.isProtected()
                                                && !((GWTJahiaPropertyDefinition) def).isConstrained()) {
                                            props.add(prop);
                                            defs.add(def);
                                        }
                                    }
                                    String srcLanguage = translationSource.getDisplayedLocale().getLanguage();
                                    int i = srcLanguage.indexOf("_");
                                    if (i > -1) {
                                        srcLanguage = srcLanguage.substring(0, i);
                                    }
                                    String destLanguage = displayedLocale.getLanguage();
                                    i = destLanguage.indexOf("_");
                                    if (i > -1) {
                                        destLanguage = destLanguage.substring(0, i);
                                    }
                                    JahiaContentManagementService.App.getInstance().translate(props, defs, srcLanguage, destLanguage, JahiaGWTParameters.getSiteUUID(), new BaseAsyncCallback<List<GWTJahiaNodeProperty>>() {
                                        public void onApplicationFailure(Throwable throwable) {
                                            com.google.gwt.user.client.Window.alert(Messages.get("failure.properties.translation", "Properties translation failed") + "\n\n"
                                                    + throwable.getMessage());
                                            Log.error("Failed to translate properties", throwable);
                                        }

                                        public void onSuccess(List<GWTJahiaNodeProperty> newProps) {
                                            Map<String, PropertiesEditor.PropertyAdapterField> fieldsMap = getPropertiesEditorByLang(displayedLocale.getLanguage()).getFieldsMap();
                                            for (final GWTJahiaNodeProperty prop : newProps) {
                                                final Field<?> f = fieldsMap.get(prop.getName()).getField();
                                                FormFieldCreator.copyValue(prop, f);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                );
            }
        });
        return button;
    }

    /**
     * On language selection changed
     */
    private void onLanguageSelectionChanged(String locale) {
        updateNodeInfo(locale);
    }

    /**
     * refresh data for current displayed language
     */
    public void refresh() {
        setNeedRefresh(false);
        setPropertiesEditorByLang(null, getDisplayedLocale().getLanguage());
        updateNodeInfo(getDisplayedLocale().getLanguage());
    }

    /**
     * load local node
     *
     * @param locale
     */
    private void updateNodeInfo(final String locale) {
        Log.debug("update node info ,locale code = " + locale);
        contentService.getProperties(node.getPath(), locale, new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
            public void onSuccess(GWTJahiaGetPropertiesResult result) {
                node = result.getNode();
                nodeTypes = result.getNodeTypes();
                properties = result.getProperties();

                if (displayedLocale == null) {
                    displayedLocale = result.getCurrentLocale();
                    languageSwitcher.setVisible(true);
                    List<GWTJahiaLanguage> selected = new ArrayList<GWTJahiaLanguage>();
                    selected.add(result.getCurrentLocale());
                    languageSwitcher.getStore().add(result.getAvailabledLanguages());
                    languageSwitcher.setSelection(selected);
                }

                updatePropertiesComponent(locale);

                if (callback != null) {
                    callback.execute();
                }

            }

            public void onApplicationFailure(Throwable throwable) {
                Log.error("Cannot get properties", throwable);
            }
        });

    }

    /**
     * @param displayedLanguage
     */
    private void loadEngine(final GWTJahiaLanguage displayedLanguage) {
        contentService.initializeEditEngine(node.getPath(),false, new BaseAsyncCallback<GWTJahiaEditEngineInitBean>() {
            public void onSuccess(GWTJahiaEditEngineInitBean result) {
                if (result == null) {
                    return;
                }

                node = result.getNode();
                nodeTypes = result.getNodeTypes();
                properties = result.getProperties();

                mainPanel = new LayoutContainer();
                mainPanel.setScrollMode(Style.Scroll.AUTOY);
                add(mainPanel, new BorderLayoutData(Style.LayoutRegion.CENTER));
                if (displayedLocale == null) {
                    displayedLocale = result.getCurrentLocale();
                    languageSwitcher.setVisible(true);
                    List<GWTJahiaLanguage> selected = new ArrayList<GWTJahiaLanguage>();
                    selected.add(result.getCurrentLocale());
                    if (editable) {
                        for (GWTJahiaLanguage language : result.getAvailabledLanguages()) {
                            if (PermissionsUtils.isPermitted("jcr:modifyProperties_" + JahiaGWTParameters.getWorkspace() + "_" + language.getLanguage(), node.getPermissions())) {
                                languageSwitcher.getStore().add(language);
                            }
                        }
                    } else {
                        languageSwitcher.getStore().add(result.getAvailabledLanguages());
                    }
                    languageSwitcher.setSelection(selected);
                }
                translationEnabled = result.isTranslationEnabled();
                if (translationEnabled && translationSource != null) {
                    topBar.add(createSuggestTranslationButton());
                    topBar.layout();
                }

                mixin = result.getMixin();
                initializersValues = result.getInitializersValues();
                if(displayedLanguage!=null) {
                    List<GWTJahiaLanguage> selected = new ArrayList<GWTJahiaLanguage>();
                    selected.add(displayedLanguage);
                    languageSwitcher.setSelection(selected);
                    updateNodeInfo(displayedLanguage.getLanguage());
                } else {
                    updatePropertiesComponent(null);
                }
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.error("Cannot get properties", throwable);
            }
        });

    }

    /**
     * Get lang properties per map
     *
     * @return
     */
    public Map<String, List<GWTJahiaNodeProperty>> getLangPropertiesMap() {
        Map<String, List<GWTJahiaNodeProperty>> mapProperties = new HashMap<String, List<GWTJahiaNodeProperty>>();
        Iterator<String> langCodes = langPropertiesEditorMap.keySet().iterator();
        while (langCodes.hasNext()) {
            String langCode = langCodes.next();
            mapProperties.put(langCode, langPropertiesEditorMap.get(langCode).getProperties(true, false, true));
        }
        return mapProperties;
    }

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        this.needRefresh = needRefresh;
    }

    public interface CallBack {
        public void execute();
    }
}
