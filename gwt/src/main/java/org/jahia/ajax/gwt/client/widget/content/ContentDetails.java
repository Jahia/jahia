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
import org.jahia.ajax.gwt.client.data.GWTJahiaEditEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaFieldInitializer;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
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
import org.jahia.ajax.gwt.client.widget.contentengine.*;
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
    private Map<String, GWTJahiaFieldInitializer> initializersValues;
    private Map<String, GWTJahiaNodeProperty> properties = new HashMap<String, GWTJahiaNodeProperty>();
    private GWTJahiaLanguage language;
    private GWTJahiaNodeACL acl;
    private String defaultLanguageCode;
    protected Map<String,Set<String>> referencesWarnings;

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
            public void handleEvent(ComponentEvent event) {
                if (selectedNodes != null && selectedNodes.size() > 0) {
                    fillCurrentTab();
                }
            }
        });

        m_component.add(tabs);
        final ButtonBar buttonBar = new ButtonBar();

        ok = new Button(Messages.get("label.save"));
        ok.setEnabled(false);
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        ok.addSelectionListener(new SaveSelectionListener());
        buttonBar.setAlignment(Style.HorizontalAlignment.RIGHT);
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
        m_component.setHeading("&nbsp;");
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
            String heading;
            if (selectedNodes.size() == 0) {
                heading = "&nbsp;";
            } else {
                heading = "";
            }
            for (GWTJahiaNode node : selectedNodes) {
                if (heading.length() + node.getName().length() < 100) {
                    heading += node.getName() + ",";
                } else {
                    heading += "... ";
                    break;
                }
            }
            if (selectedNodes.size() > 0) {
                heading = heading.substring(0, heading.length() - 1);
            }
            m_component.setHeading(heading);
            if (selectedNodes.size() == 1) {
                final GWTJahiaNode node = selectedNodes.get(0);
                service.initializeEditEngine(node.getPath(), false,
                        new BaseAsyncCallback<GWTJahiaEditEngineInitBean>() {
                            public void onSuccess(GWTJahiaEditEngineInitBean result) {
                                types = result.getNodeTypes();
                                properties = result.getProperties();
                                language = result.getCurrentLocale();
                                defaultLanguageCode = result.getDefaultLanguageCode();

                                mixin = result.getMixin();
                                initializersValues = result.getInitializersValues();
                                ok.setEnabled(true);
                                acl = result.getAcl();
                                referencesWarnings = result.getReferencesWarnings();
                                if (config.isAllowRootNodeEditing() || node.get("isRootNode") == null) {
                                    for (TabItem item : tabs.getItems()) {
                                        EditEngineTabItem tabItem = (EditEngineTabItem) item.getData("item");
                                        if ((tabItem.getHideForTypes().isEmpty() || !result.getNode().isNodeType(tabItem.getHideForTypes())) &&
                                                (tabItem.getShowForTypes().isEmpty() || result.getNode().isNodeType(tabItem.getShowForTypes())) &&
                                                (tabItem.getGwtEngineTab().getRequiredPermission() == null || tabItem.getGwtEngineTab().getRequiredPermission() != null && PermissionsUtils.isPermitted(tabItem.getGwtEngineTab().getRequiredPermission(), selectedNodes.get(0).getPermissions()))) {
                                            item.setEnabled(true);
                                        }
                                    }
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
                        ok.setEnabled(true);
                        for (TabItem item : tabs.getItems()) {
                            EditEngineTabItem editItem = (EditEngineTabItem) item.getData("item");
                            if (((EditEngineTabItem) item.getData("item")).isHandleMultipleSelection() && 
                                    (editItem.getGwtEngineTab().getRequiredPermission() == null || editItem.getGwtEngineTab().getRequiredPermission() != null && PermissionsUtils.isPermitted(editItem.getGwtEngineTab().getRequiredPermission(), selectedNodes.get(0).getPermissions()))) {
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

    public List<GWTJahiaNodeType> getNodeTypes() {
        return types;
    }

    public List<GWTJahiaNodeType> getMixin() {
        return mixin;
    }

    public Map<String, GWTJahiaFieldInitializer> getInitializersValues() {
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

    /**
     * Save selection listener
     */
    private class SaveSelectionListener extends SelectionListener<ButtonEvent> {
        public SaveSelectionListener() {
        }

        public void componentSelected(ButtonEvent event) {

            // general properties
            final List<GWTJahiaNodeProperty> changedProperties = new ArrayList<GWTJahiaNodeProperty>();
            final Set<String> addedTypes = new HashSet<String>();
            final Set<String> removedTypes = new HashSet<String>();
            // general properties
            final Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties =
                    new HashMap<String, List<GWTJahiaNodeProperty>>();

            // node
            List<GWTJahiaNode> orderedChildrenNodes = null;

            for (TabItem tab : tabs.getItems()) {
                EditEngineTabItem item = (EditEngineTabItem) tab.getData("item");
                // case of contentTabItem
                if (item instanceof ContentTabItem) {
                    if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                        String nodeName = ((ContentTabItem) item).getName().getValue();
                        getNode().setName(nodeName);
                    }
                }

                if (item instanceof ListOrderingContentTabItem) {

                    // if the manual ranking was activated update new ranking
                    orderedChildrenNodes = ((ListOrderingContentTabItem) item).getNewManualOrderedChildrenList();
                }

                item.doSave(getNode(), changedProperties, changedI18NProperties, addedTypes, removedTypes, acl);
            }

            getNode().getNodeTypes().removeAll(removedTypes);
            getNode().getNodeTypes().addAll(addedTypes);

            // Ajax call to update values
            AsyncCallback callback = new BaseAsyncCallback() {
                public void onApplicationFailure(Throwable throwable) {
                    String message = throwable.getMessage();
                    if (message.contains("Invalid link")) {
                        message = Messages.get("label.error.invalidlink", "Invalid link") + " : " + message.substring(message.indexOf(":")+1);
                    }
                    com.google.gwt.user.client.Window.alert(Messages.get("label.error.invalidlink", "Properties save failed") + "\n\n"
                            + message);
                    Log.error("failed", throwable);
                }

                public void onSuccess(Object o) {
                    Info.display(Messages.get("label.information", "Information"), Messages.get("saved_prop", "Properties saved\n\n"));
                    if (getNodes().contains(linker.getSelectionContext().getMainNode())) {
                        linker.refresh(Linker.REFRESH_ALL);
                    } else {
                        linker.refreshTable();
                    }
                }
            };

            if (isMultipleSelection()) {
                JahiaContentManagementService.App.getInstance().savePropertiesAndACL(getNodes(), null, changedI18NProperties, changedProperties, removedTypes, callback);

            } else {
                JahiaContentManagementService.App.getInstance()
                        .saveNode(getNode(), orderedChildrenNodes, acl, changedI18NProperties, changedProperties,
                                removedTypes, callback);
            }
        }

    }

}
