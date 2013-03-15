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

package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
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

import java.util.*;

/**
 * Search tab item for the side panel for performing simple queries in the content repository.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 3:14:11 PM
 */
class DependenciesTabItem extends SidePanelTabItem {

    protected transient Grid<GWTJahiaNode> availableModules;
    protected transient HorizontalPanel buttonBar;
    protected transient Grid<GWTJahiaNode> dependencyModules;
    protected transient ListStore modulesStore;
    protected transient ListStore dependenciesStore;
    protected GWTJahiaNode curentModuleVersion;

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
        modulesStore = new ListStore<GWTJahiaNode>();
        modulesStore.setStoreSorter(storeSorter);
        modulesStore.setSortField("displayName");
        modulesStore.setSortDir(Style.SortDir.ASC);
        dependenciesStore = new ListStore<GWTJahiaNode>();
        dependenciesStore.setStoreSorter(storeSorter);
        dependenciesStore.setSortField("displayName");
        dependenciesStore.setSortDir(Style.SortDir.ASC);
        loadStores();

        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId("displayName");
        columnConfig.setHeader(Messages.get("label.module", "Module"));
        availableModules = new Grid<GWTJahiaNode>(modulesStore, new ColumnModel(Arrays.asList(columnConfig)));
        availableModules.setAutoExpandColumn("displayName");
        LayoutContainer modulesContainer = new LayoutContainer();
        modulesContainer.setBorders(false);
        modulesContainer.setScrollMode(Style.Scroll.AUTO);
        modulesContainer.setLayout(new FitLayout());
        modulesContainer.add(availableModules);
        VBoxLayoutData modulesVBoxData = new VBoxLayoutData();
        modulesVBoxData.setFlex(1);
        tab.add(modulesContainer, modulesVBoxData);

        buttonBar = new HorizontalPanel();
        buttonBar.setTableWidth("100%");
        buttonBar.setBorders(true);
        buttonBar.setHorizontalAlign(Style.HorizontalAlignment.CENTER);
        IconButton up = new IconButton("arrow-up");
        up.setToolTip(Messages.get("label.removeDependency", "Click to remove the dependency"));
        up.addListener(Events.Select, new Listener<IconButtonEvent>() {
            public void handleEvent(IconButtonEvent be) {
                removeDependencies();
            }
        });
        buttonBar.add(up);
        IconButton down = new IconButton("arrow-down");
        down.setToolTip(Messages.get("label.addDependency", "Click to add a dependency"));
        down.addListener(Events.Select, new Listener<IconButtonEvent>() {
            public void handleEvent(IconButtonEvent be) {
                addDependencies();
            }
        });
        buttonBar.add(down);
        tab.add(buttonBar);

        columnConfig = new ColumnConfig();
        columnConfig.setId("displayName");
        columnConfig.setHeader(Messages.get("label.dependency", "Dependency"));
        dependencyModules = new Grid<GWTJahiaNode>(dependenciesStore, new ColumnModel(Arrays.asList(columnConfig)));
        dependencyModules.setAutoExpandColumn("displayName");
        LayoutContainer dependenciesContainer = new LayoutContainer();
        dependenciesContainer.setBorders(false);
        dependenciesContainer.setScrollMode(Style.Scroll.AUTO);
        dependenciesContainer.setLayout(new FitLayout());
        dependenciesContainer.add(dependencyModules);
        VBoxLayoutData dependenciesVBoxData = new VBoxLayoutData();
        dependenciesVBoxData.setFlex(1);
        tab.add(dependenciesContainer, dependenciesVBoxData);

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