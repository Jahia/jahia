/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTChoiceListInitializer;
import org.jahia.ajax.gwt.client.data.GWTJahiaEditEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
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
    protected Map<String,Set<String>> referencesWarnings;

    private List<GWTJahiaNode> selectedNodes = null;
    private boolean workInProgress = false;


    private final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
    private Button ok;
    private CheckBox wip;

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
            public void handleEvent(ComponentEvent event) {
                if (selectedNodes != null && selectedNodes.size() > 0) {
                    fillCurrentTab();
                }
            }
        });

        m_component.add(tabs);
        final ButtonBar buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.RIGHT);

        if (config.isShowWorkInProgress()) {
            wip = new CheckBox();
            wip.setValue(false);
            workInProgress = wip.getValue();

            wip.addListener(Events.Change, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent event) {
                    workInProgress = wip.getValue();
                }
            });
            wip.setBoxLabel(Messages.get("label.saveAsWIP", "Save as work in progress"));
            wip.setToolTip(Messages.get("label.saveAsWIP.information", "If checked, this content will ne be part of publication process"));
            buttonBar.add(wip);
        }

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

    public Component getComponent() {
        return m_component;
    }


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

    public void close() {
        fillData(selectedNodes);
    }

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
            m_component.setHeadingHtml(heading.toString());
            if (selectedNodes.size() == 1) {
                final GWTJahiaNode node = selectedNodes.get(0);
                service.initializeEditEngine(node.getPath(), false,
                        new BaseAsyncCallback<GWTJahiaEditEngineInitBean>() {
                            public void onSuccess(GWTJahiaEditEngineInitBean result) {
                                if (selectedNodes == null || !selectedNodes.contains(result.getNode())) {
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
                                if (config.isShowWorkInProgress()) {
                                    wip.setEnabled(true);
                                    wip.setValue(result.getNode() != null && result.getNode().get("j:workInProgress") != null && (Boolean) result.getNode().get("j:workInProgress"));
                                }

                                fillCurrentTab();
                            }
                        });
            } else if (selectedNodes.size() > 1) {
                List<String> paths = new ArrayList<String>();
                for (GWTJahiaNode node : selectedNodes) {
                    paths.add(node.getPath());
                }
                service.initializeEditEngine(paths,false, new BaseAsyncCallback<GWTJahiaEditEngineInitBean>() {
                    public void onSuccess(GWTJahiaEditEngineInitBean result) {
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
                        if (config.isShowWorkInProgress()) {
                            wip.setEnabled(false);
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

    public List<GWTJahiaNodeType> getNodeTypes() {
        return types;
    }

    public List<GWTJahiaNodeType> getMixin() {
        return mixin;
    }

    public Map<String, GWTChoiceListInitializer> getChoiceListInitializersValues() {
        return initializersValues;
    }

    public GWTJahiaNode getNode() {
        return selectedNodes != null ? selectedNodes.get(0) : null;
    }

    public List<GWTJahiaNode> getNodes() {
        return selectedNodes;
    }

    public String getNodeName() {
        return getNode().getName();
    }

    public String getDefaultLanguageCode() {
        return defaultLanguageCode;
    }

    public GWTJahiaNodeACL getAcl() {
        return acl;
    }

    @Override
    public List<GWTJahiaNode> getSelection() {
        return selectedNodes;
    }

    public Map<String, Set<String>> getReferencesWarnings() {
        return referencesWarnings;
    }

    public GWTJahiaNode getTargetNode() {
        return null;
    }

    public boolean isExistingNode() {
        return true;
    }

    public boolean isMultipleSelection() {
        return selectedNodes != null && selectedNodes.size() > 1;
    }

    public Map<String, GWTJahiaNodeProperty> getProperties() {
        return properties;
    }

    public Map<String, GWTJahiaNodeProperty> getPresetProperties() {
        return new HashMap<String, GWTJahiaNodeProperty>();
    }

    public Map<String, Map<String, List<GWTJahiaNodePropertyValue>>> getDefaultValues() {
        return defaultValues;
    }

    /**
     * Save selection listener
     */
    private class SaveSelectionListener extends SelectionListener<ButtonEvent> {
        public SaveSelectionListener() {
        }

        public void componentSelected(ButtonEvent event) {

            // general properties
            final List<GWTJahiaNodeProperty> changedProperties = new ArrayList<GWTJahiaNodeProperty>();

            if (getNode() != null && getNode().isNodeType("jmix:lastPublished")) {
                for (GWTJahiaNodeProperty property : changedProperties) {
                    if (property.getName().equals("j:workInProgress")) {
                        if (workInProgress) {
                            property.setValue(new GWTJahiaNodePropertyValue(Boolean.toString(workInProgress), GWTJahiaNodePropertyType.BOOLEAN));
                        } else {
                            property.setValue(new GWTJahiaNodePropertyValue((String) null, GWTJahiaNodePropertyType.BOOLEAN));
                        }
                        return;
                    }
                }
            }

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
            if (config.isShowWorkInProgress()) {
                if (changedI18NProperties.containsKey(language.getLanguage())) {
                    changedI18NProperties.get(language.getLanguage()).add(new GWTJahiaNodeProperty("j:workInProgress", Boolean.toString(workInProgress), GWTJahiaNodePropertyType.BOOLEAN));
                } else {
                    changedI18NProperties.put(language.getLanguage(), Arrays.asList(new GWTJahiaNodeProperty("j:workInProgress", Boolean.toString(workInProgress), GWTJahiaNodePropertyType.BOOLEAN)));
                }
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
