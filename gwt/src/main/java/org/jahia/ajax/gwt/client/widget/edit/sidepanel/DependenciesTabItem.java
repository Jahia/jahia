/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.dnd.GridDropTarget;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;

import java.util.*;

/**
 * Tab item for modifying dependencies of a module.
 *
 * @author Quentin Lamerand
 */
@SuppressWarnings("serial")
class DependenciesTabItem extends SidePanelTabItem {

    protected transient Grid<GWTJahiaNode> availableModules;
    protected transient ToolBar buttonBar;
    protected transient Grid<GWTJahiaNode> dependencyModules;
    protected transient ListStore modulesStore;
    protected transient ListStore dependenciesStore;
    protected GWTJahiaNode curentModuleVersion;

    /**
     * Performs the creation of the tab item and populates its content. The tab contains two panes: one with the list of available modules
     * and the other one with the list of actual module dependencies. The modules can be moved between the panes to modify the dependencies.
     *
     * @param config
     *            the tab configuration
     * @return the created tab item
     */
    @Override
    public TabItem create(GWTSidePanelTab config) {
        super.create(config);
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        tab.setLayout(l);

        StoreSorter<GWTJahiaNode> storeSorter = new StoreSorter<GWTJahiaNode>(new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                if (o1 instanceof String && o2 instanceof String) {
                    String s1 = (String) o1;
                    String s2 = (String) o2;
                    return Collator.getInstance().localeCompare(s1, s2);
                } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
                    return ((Comparable) o1).compareTo(o2);
                }
                return 0;
            }
        });
        StoreSorter<GWTJahiaNode> availableModulestoreSorter = new StoreSorter<GWTJahiaNode>(new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                if (o1 instanceof String && o2 instanceof String) {
                    String s1 = (String) o1;
                    String s2 = (String) o2;
                    return Collator.getInstance().localeCompare(s1, s2);
                } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
                    return ((Comparable) o1).compareTo(o2);
                }
                return 0;
            }
        });
        modulesStore = new ListStore<GWTJahiaNode>();
        modulesStore.setStoreSorter(availableModulestoreSorter);
        modulesStore.setSortField("displayName");
        modulesStore.setSortDir(Style.SortDir.ASC);
        dependenciesStore = new ListStore<GWTJahiaNode>();
        dependenciesStore.setStoreSorter(storeSorter);
        dependenciesStore.setSortField("displayName");
        dependenciesStore.setSortDir(Style.SortDir.ASC);
        loadStores();

        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId("displayName");
        columnConfig.setHeaderHtml(Messages.get("label.module", "Module"));
        availableModules = new Grid<GWTJahiaNode>(modulesStore, new ColumnModel(Arrays.asList(columnConfig)));
        availableModules.setAutoExpandColumn("displayName");
        new GridDragSource(availableModules);
        new GridDropTarget(availableModules).addDNDListener(new DNDListener() {
            @Override
            public void dragDrop(DNDEvent e) {
                saveDependencies();
            }
        });
        LayoutContainer modulesContainer = new LayoutContainer();
        modulesContainer.setBorders(false);
        modulesContainer.setScrollMode(Style.Scroll.AUTO);
        modulesContainer.setLayout(new FitLayout());
        modulesContainer.add(availableModules);
        VBoxLayoutData modulesVBoxData = new VBoxLayoutData();
        modulesVBoxData.setFlex(1);
        tab.add(modulesContainer, modulesVBoxData);

        buttonBar = new ToolBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);
        Button up = new Button();
        up.setIcon(ToolbarIconProvider.getInstance().getIcon("sort_up_minus"));
        up.setToolTip(Messages.get("label.removeDependency", "Click to remove the dependency"));
        up.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                removeDependencies();
            }
        });
        buttonBar.add(up);
        Button down = new Button();
        down.setIcon(ToolbarIconProvider.getInstance().getIcon("sort_down_plus"));
        down.setToolTip(Messages.get("label.addDependency", "Click to add a dependency"));
        down.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                addDependencies();
            }
        });
        buttonBar.add(down);
        tab.add(buttonBar);

        columnConfig = new ColumnConfig();
        columnConfig.setId("displayName");
        columnConfig.setHeaderHtml(Messages.get("label.dependency", "Dependency"));
        dependencyModules = new Grid<GWTJahiaNode>(dependenciesStore, new ColumnModel(Arrays.asList(columnConfig)));
        dependencyModules.setAutoExpandColumn("displayName");
        new GridDragSource(dependencyModules);
        new GridDropTarget(dependencyModules).addDNDListener(new DNDListener() {
            @Override
            public void dragDrop(DNDEvent e) {
                saveDependencies();
            }
        });
        LayoutContainer dependenciesContainer = new LayoutContainer();
        dependenciesContainer.setBorders(false);
        dependenciesContainer.setScrollMode(Style.Scroll.AUTO);
        dependenciesContainer.setLayout(new FitLayout());
        dependenciesContainer.add(dependencyModules);
        VBoxLayoutData dependenciesVBoxData = new VBoxLayoutData();
        dependenciesVBoxData.setFlex(1);
        tab.add(dependenciesContainer, dependenciesVBoxData);

        dependencyModules.addListener(Events.CellDoubleClick,new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                removeDependencies();
            }
        });
        availableModules.addListener(Events.CellDoubleClick, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                addDependencies();
            }
        });
        return tab;
    }

    protected void loadStores() {
        final GWTJahiaNode currentModule = JahiaGWTParameters.getSiteNode();
        if (!currentModule.getNodeTypes().contains("jnt:module")) {
            return;
        }
        String version = currentModule.get("j:versionInfo");
        JahiaContentManagementService.App.getInstance().getNodes(Arrays.asList(currentModule.getPath() + "/" + version), Arrays.asList("j:dependencies"), new BaseAsyncCallback<List<GWTJahiaNode>>() {
            @Override
            public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                if (gwtJahiaNodes.isEmpty()) {
                    return;
                }
                curentModuleVersion = gwtJahiaNodes.get(0);
                List<String> dependencies = curentModuleVersion.get("j:dependencies");
                if (dependencies == null) {
                    return;
                }
                Collection<GWTJahiaNode> modules = JahiaGWTParameters.getSitesMap().values();
                for (GWTJahiaNode module : modules) {
                    if (currentModule.getName().equals(module.getName())) {
                        continue;
                    }
                    if (dependencies.contains(module.getName())) {
                        dependenciesStore.add(module);
                    } else {
                        modulesStore.add(module);
                    }
                }
            }
        });
    }

    protected void removeDependencies() {
        List<GWTJahiaNode> modules = dependencyModules.getSelectionModel().getSelectedItems();
        for (GWTJahiaNode module : modules) {
            dependencyModules.getStore().remove(module);
        }
        if (!modules.isEmpty()) {
            availableModules.getStore().add(modules);
            saveDependencies();
        }
    }

    protected void addDependencies() {
        List<GWTJahiaNode> modules = availableModules.getSelectionModel().getSelectedItems();
        for (GWTJahiaNode module : modules) {
            availableModules.getStore().remove(module);
        }
        if (!modules.isEmpty()) {
            dependencyModules.getStore().add(modules);
            saveDependencies();
        }
    }

    protected void saveDependencies() {
        List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();
        final GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
        gwtJahiaNodeProperty.setName("j:dependencies");

        final List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>();
        for (GWTJahiaNode node : dependencyModules.getStore().getModels()) {
            values.add(new GWTJahiaNodePropertyValue(node.getName(), GWTJahiaNodePropertyType.STRING));
        }
        gwtJahiaNodeProperty.setMultiple(true);
        gwtJahiaNodeProperty.setValues(values);
        properties.add(gwtJahiaNodeProperty);
        buttonBar.mask(Messages.get("label.saving", "Saving..."));
        JahiaContentManagementService.App.getInstance().saveProperties(Arrays.asList(curentModuleVersion), properties, null, new BaseAsyncCallback() {
            public void onSuccess(Object result) {
                buttonBar.unmask();
            }

            @Override
            public void onApplicationFailure(Throwable caught) {
                buttonBar.unmask();
            }
        });
    }

    @Override
    public boolean needRefresh(Map<String, Object> data) {
        return super.needRefresh(data);
    }

    @Override
    public void doRefresh() {
        modulesStore.removeAll();
        dependenciesStore.removeAll();
        loadStores();
    }
}
