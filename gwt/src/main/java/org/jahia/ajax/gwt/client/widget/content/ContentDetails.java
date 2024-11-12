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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTChoiceListInitializer;
import org.jahia.ajax.gwt.client.data.GWTJahiaEditEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.contentengine.ContentTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.EditEngineTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineValidation;
import org.jahia.ajax.gwt.client.widget.contentengine.NodeHolder;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;

import java.util.*;


/**
 *
 *
 * @author rfelden
 * @version 23 juin 2008 - 16:15:46
 */
public class ContentDetails extends BottomRightComponent implements NodeHolder {
    private GWTManagerConfiguration config;
    private final ManagerLinker linker;
    private ContentPanel m_component;
    private TabPanel tabs;
    private List<GWTJahiaNodeType> types;
    private List<GWTJahiaNodeType> mixin;
    private Map<String, GWTChoiceListInitializer> initializersValues;
    protected Map<String, Map<String, List<GWTJahiaNodePropertyValue>>> defaultValues;
    private Map<String, GWTJahiaNodeProperty> properties = new HashMap<String, GWTJahiaNodeProperty>();
    private GWTJahiaLanguage language;
    private GWTJahiaNodeACL acl;
    private String defaultLanguageCode;
    protected Map<String, Set<String>> referencesWarnings;

    private List<GWTJahiaNode> selectedNodes = null;


    private final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
    private Button ok;

    public ContentDetails(GWTManagerConfiguration config, ManagerLinker linker) {
        super();
        setComponentType(ManagerLinker.MANAGER);
        this.config = config;
        this.linker = linker;
        m_component = new ContentPanel(new FitLayout());
        m_component.setBodyBorder(false);
        m_component.setBorders(true);
        m_component.setHeaderVisible(false);
        tabs = new TabPanel();
        tabs.setBodyBorder(false);
        tabs.setBorders(false);
        tabs.setAnimScroll(true);
        tabs.setTabScroll(true);

        // properties
        initTabs();

        tabs.addListener(Events.Select, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent event) {
                if (selectedNodes != null && selectedNodes.size() > 0) {
                    fillCurrentTab();
                }
            }
        });

        m_component.add(tabs);
        final ButtonBar buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.RIGHT);

        ok = new Button(Messages.get("label.save"));
        ok.addStyleName("button-save");
        ok.setEnabled(false);
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        ok.addSelectionListener(new SaveSelectionListener());
        buttonBar.add(ok);

        m_component.setBottomComponent(buttonBar);
    }


    /**
     * Creates and initializes all window tabs.
     */
    protected void initTabs() {
        for (GWTEngineTab tabConfig : ((GWTManagerConfiguration)config).getManagerEngineTabs()) {
            EditEngineTabItem tabItem = tabConfig.getTabItem();
            tabs.add(tabItem.create(tabConfig, this));
        }
    }

    @Override
    public Component getComponent() {
        return m_component;
    }

    @Override
    public void clear() {
        m_component.setHeadingHtml("&nbsp;");
        selectedNodes = null;
        for (TabItem item : tabs.getItems()) {
            ((AsyncTabItem) item).setProcessed(false);
            item.setEnabled(false);
        }
    }

    @Override
    public void emptySelection() {
        fillData(new ArrayList<GWTJahiaNode>());
    }

    @Override
    public void close() {
        fillData(selectedNodes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fillData(Object selectedItem) {
        clear();

        properties = null;
        types = null;
        mixin = null;

        ok.setEnabled(false);

        if (selectedItem != null) {
            if (selectedItem instanceof GWTJahiaNode) {
                selectedNodes = new ArrayList<GWTJahiaNode>();
                selectedNodes.add((GWTJahiaNode) selectedItem);
            } else {
                selectedNodes = (List<GWTJahiaNode>) selectedItem;
            }
            StringBuilder heading = new StringBuilder();
            if (selectedNodes.size() == 0) {
                heading.append("&nbsp;");
            } else {
                heading.append("");
            }
            for (GWTJahiaNode node : selectedNodes) {
                if (heading.length() + node.getName().length() < 100) {
                    heading.append(node.getName()).append(",");
                } else {
                    heading.append("... ");
                    break;
                }
            }
            if (selectedNodes.size() > 0) {
                heading.deleteCharAt(heading.length() - 1);
            }
            m_component.setHeadingText(heading.toString());
            if (selectedNodes.size() == 1) {
                final GWTJahiaNode node = selectedNodes.get(0);
                service.initializeEditEngine(node.getPath(), false,
                        new BaseAsyncCallback<GWTJahiaEditEngineInitBean>() {

                            @Override
                            public void onSuccess(GWTJahiaEditEngineInitBean result) {
                                if (result== null || selectedNodes == null || !selectedNodes.contains(result.getNode())) {
                                    return;
                                }
                                types = result.getNodeTypes();
                                properties = result.getProperties();
                                language = result.getCurrentLocale();
                                defaultLanguageCode = result.getDefaultLanguageCode();

                                mixin = result.getMixin();
                                initializersValues = result.getInitializersValues();
                                defaultValues = result.getDefaultValues();
                                ok.setEnabled(true);
                                acl = result.getAcl();
                                referencesWarnings = result.getReferencesWarnings();
                                if (config.isAllowRootNodeEditing() || node.get("isRootNode") == null) {
                                    for (TabItem item : tabs.getItems()) {
                                        EditEngineTabItem tabItem = (EditEngineTabItem) item.getData("item");
                                        if ((tabItem.getHideForTypes().isEmpty() || !result.getNode().isNodeType(tabItem.getHideForTypes())) &&
                                                (tabItem.getShowForTypes().isEmpty() || result.getNode().isNodeType(tabItem.getShowForTypes())) &&
                                                (tabItem.getGwtEngineTab().getRequiredPermission() == null || tabItem.getGwtEngineTab().getRequiredPermission() != null && PermissionsUtils.isPermitted(tabItem.getGwtEngineTab().getRequiredPermission(), JahiaGWTParameters.getSiteNode()))) {
                                            item.setEnabled(true);
                                        }
                                    }
                                } else {
                                    ok.setEnabled(false); //disable save button
                                }

                                fillCurrentTab();
                            }
                        });
            } else if (selectedNodes.size() > 1) {
                List<String> paths = new ArrayList<String>();
                for (GWTJahiaNode node : selectedNodes) {
                    paths.add(node.getPath());
                }
                service.initializeEditEngine(paths, false, new BaseAsyncCallback<GWTJahiaEditEngineInitBean>() {

                    @Override
                    public void onSuccess(GWTJahiaEditEngineInitBean result) {
                        if (result == null) {
                            return;
                        }
                        types = result.getNodeTypes();
                        properties = result.getProperties();
                        language = result.getCurrentLocale();

                        mixin = result.getMixin();
                        initializersValues = result.getInitializersValues();
                        defaultValues = result.getDefaultValues();
                        ok.setEnabled(true);
                        for (TabItem item : tabs.getItems()) {
                            EditEngineTabItem editItem = (EditEngineTabItem) item.getData("item");
                            if (((EditEngineTabItem) item.getData("item")).isHandleMultipleSelection() &&
                                    (editItem.getGwtEngineTab().getRequiredPermission() == null || editItem.getGwtEngineTab().getRequiredPermission() != null && PermissionsUtils.isPermitted(editItem.getGwtEngineTab().getRequiredPermission(), JahiaGWTParameters.getSiteNode()))) {
                                item.setEnabled(true);
                            }
                            if (!tabs.getSelectedItem().equals(item)) {
                                if (editItem instanceof ContentTabItem) {
                                    if (((ContentTabItem) editItem).isNodeNameFieldDisplayed()) {
                                        ((ContentTabItem) editItem).getName().setValue(getNodeName());
                                    }
                                }
                            }
                        }
                        fillCurrentTab();
                    }
                });
            }
        }
    }

    private void fillCurrentTab() {
        TabItem currentTab = tabs.getSelectedItem();

        Object currentTabItem = currentTab.getData("item");
        if (currentTabItem instanceof EditEngineTabItem) {
            EditEngineTabItem engineTabItem = (EditEngineTabItem) currentTabItem;

            if (!((AsyncTabItem)currentTab).isProcessed()) {
                engineTabItem.init(this, (AsyncTabItem) currentTab, language.getLanguage());
                m_component.layout();
            }
        }
    }

    @Override
    public List<GWTJahiaNodeType> getNodeTypes() {
        return types;
    }

    @Override
    public List<GWTJahiaNodeType> getMixin() {
        return mixin;
    }

    @Override
    public Map<String, GWTChoiceListInitializer> getChoiceListInitializersValues() {
        return initializersValues;
    }

    @Override
    public GWTJahiaNode getNode() {
        return selectedNodes != null ? selectedNodes.get(0) : null;
    }

    @Override
    public List<GWTJahiaNode> getNodes() {
        return selectedNodes;
    }

    @Override
    public String getNodeName() {
        return getNode().getName();
    }

    @Override
    public String getDefaultLanguageCode() {
        return defaultLanguageCode;
    }

    @Override
    public GWTJahiaNodeACL getAcl() {
        return acl;
    }

    @Override
    public List<GWTJahiaNode> getSelection() {
        return selectedNodes;
    }

    @Override
    public Map<String, Set<String>> getReferencesWarnings() {
        return referencesWarnings;
    }

    @Override
    public GWTJahiaNode getTargetNode() {
        return null;
    }

    @Override
    public boolean isExistingNode() {
        return true;
    }

    @Override
    public boolean isMultipleSelection() {
        return selectedNodes != null && selectedNodes.size() > 1;
    }

    @Override
    public Map<String, GWTJahiaNodeProperty> getProperties() {
        return properties;
    }

    @Override
    public Map<String, GWTJahiaNodeProperty> getPresetProperties() {
        return new HashMap<String, GWTJahiaNodeProperty>();
    }

    @Override
    public Map<String, Map<String, List<GWTJahiaNodePropertyValue>>> getDefaultValues() {
        return defaultValues;
    }

    /**
     * Save selection listener
     */
    private class SaveSelectionListener extends SelectionListener<ButtonEvent> {

        public SaveSelectionListener() {
        }

        @Override
        public void componentSelected(ButtonEvent event) {

            // general properties
            final List<GWTJahiaNodeProperty> changedProperties = new ArrayList<GWTJahiaNodeProperty>();

            final Set<String> addedTypes = new HashSet<String>();
            final Set<String> removedTypes = new HashSet<String>();
            // general properties
            final Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties =
                    new HashMap<String, List<GWTJahiaNodeProperty>>();

            for (TabItem tab : tabs.getItems()) {
                EditEngineTabItem item = (EditEngineTabItem) tab.getData("item");
                // case of contentTabItem
                if (item instanceof ContentTabItem) {
                    if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                        String nodeName = ((ContentTabItem) item).getName().getValue();
                        getNode().setName(nodeName);
                    }
                }

                item.doSave(getNode(), changedProperties, changedI18NProperties, addedTypes, removedTypes, null, acl);
            }

            getNode().getNodeTypes().removeAll(removedTypes);
            getNode().getNodeTypes().addAll(addedTypes);

            EngineValidation e = new EngineValidation(ContentDetails.this, tabs, defaultLanguageCode, changedI18NProperties);
            boolean valid = e.validateData(new EngineValidation.ValidateCallback() {

                @Override
                public void handleValidationResult(EngineValidation.ValidateResult result) {
                    if (result.errorTab != null && !tabs.getSelectedItem().equals(result.errorTab)) {
                        tabs.setSelection(result.errorTab);
                    }
                    if (result.errorField != null) {
                        result.errorField.focus();
                    }
                    if (result.errorTab != null) {
                        result.errorTab.layout();
                    }
                }

                @Override
                public void saveAnyway() {
                    save(changedProperties, removedTypes, changedI18NProperties);
                }

                @Override
                public void close() {

                }
            });

            if (valid) {
                save(changedProperties, removedTypes, changedI18NProperties);
            }
        }

    }

    private void save(List<GWTJahiaNodeProperty> changedProperties, Set<String> removedTypes, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties) {
        // we temporarily deactivate the button to prevent double clicks while saving...
        final boolean okEnabled = ok.isEnabled();
        ok.setEnabled(false);

        // Ajax call to update values
        AsyncCallback callback = new BaseAsyncCallback() {

            @Override
            public void onApplicationFailure(Throwable throwable) {
                String message = throwable.getMessage();
                if (message.contains("Invalid link")) {
                    message = Messages.get("label.error.invalidlink", "Invalid link") + " : " + message.substring(message.indexOf(":")+1);
                }
                com.google.gwt.user.client.Window.alert(Messages.get("failure.properties.save", "Properties save failed") + "\n\n"
                        + message);
                Log.error("failed", throwable);
                ok.setEnabled(okEnabled);
            }

            @Override
            public void onSuccess(Object o) {
                ok.setEnabled(okEnabled);
                Info.display(Messages.get("label.information", "Information"), Messages.get("saved_prop", "Properties saved\n\n"));
                if (getNodes().contains(linker.getSelectionContext().getMainNode())) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put(Linker.REFRESH_ALL, true);
                    linker.refresh(data);
                } else {
                    linker.refreshTable();
                }
            }
        };

        if (isMultipleSelection()) {
            JahiaContentManagementService.App.getInstance().savePropertiesAndACL(getNodes(), null, changedI18NProperties, changedProperties, removedTypes, callback);

        } else {
            JahiaContentManagementService.App.getInstance()
                    .saveNode(getNode(), acl, changedI18NProperties, changedProperties,
                            removedTypes, callback);
        }
    }
}
