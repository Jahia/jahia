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

package org.jahia.ajax.gwt.client.widget.definition;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.layout.*;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaEditEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaFieldInitializer;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
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
    private Map<String, GWTJahiaFieldInitializer> initializersValues;
    private Map<String, GWTJahiaNodeProperty> properties;
    private GWTJahiaNode node;
    private ComboBox<GWTJahiaLanguage> languageSwitcher;
    private LayoutContainer mainPanel;
    private boolean editable = true;
    private GWTJahiaLanguage displayedLocale = null;
    private LayoutContainer topBar;

    public LangPropertiesEditor(GWTJahiaNode node, List<String> dataType, boolean editable,
                                GWTJahiaLanguage displayedLanguage) {
        this.node = node;
        this.dataType = dataType;
        langPropertiesEditorMap = new HashMap<String, PropertiesEditor>();
        this.editable = editable;

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

        mainPanel = new LayoutContainer();
        mainPanel.setScrollMode(Style.Scroll.AUTOY);
        add(mainPanel, new BorderLayoutData(Style.LayoutRegion.CENTER));
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
                langPropertiesEditor.setInitializersValues(initializersValues);
                langPropertiesEditor.setNonI18NWriteable(false);
                langPropertiesEditor.setWriteable(editable);
                langPropertiesEditor.setFieldSetGrouping(true);
                langPropertiesEditor.setExcludedTypes(excludedTypes);
                if (node != null) {
                    langPropertiesEditor.setPermissions(node.getPermissions());
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
                            langPropertiesEditor.getGWTJahiaItemDefinition(property), property, null);
                }
            }


            // update displayed properties 
            displayedPropertiesEditor = langPropertiesEditor;

            mainPanel.layout();
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

    /**
     * On language selection changed
     */
    private void onLanguageSelectionChanged(String locale) {
        updateNodeInfo(locale);
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
                node = result.getNode();
                nodeTypes = result.getNodeTypes();
                properties = result.getProperties();

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

}
