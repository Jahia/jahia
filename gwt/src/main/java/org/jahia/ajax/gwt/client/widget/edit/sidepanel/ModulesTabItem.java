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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbar;
import org.jahia.api.Constants;

import java.util.List;
import java.util.Map;


public class ModulesTabItem extends BrowseTabItem {
    private static final long serialVersionUID = 7656741991148114672L;
    protected transient ButtonBar buttonBar;
    protected transient LayoutContainer layoutContainer;
    protected transient ActionToolbar toolbar;

    public TabItem create(GWTSidePanelTab config) {
        super.create(config);
        VBoxLayoutData treeVBoxData = new VBoxLayoutData();
        treeVBoxData.setFlex(0);
        layoutContainer = new LayoutContainer();
        layoutContainer.setHeight(24);
        tab.insert(layoutContainer, 0, treeVBoxData);

        handleMouseClick();
        this.tree.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);

        final Menu contextMenu = createContextMenu(config.getTreeContextMenu(), tree.getSelectionModel());
        tree.setContextMenu(contextMenu);

        tree.getColumnModel().getColumn(0).setRenderer(new TreeGridCellRenderer<GWTJahiaNode>() {
            @Override
            protected String getText(TreeGrid<GWTJahiaNode> gwtJahiaNodeTreeGrid, GWTJahiaNode node, String property, int rowIndex, int colIndex) {
                String v = node.get(property);
                if (v != null) {
                    v = SafeHtmlUtils.htmlEscape(v);
                }
                String classes = "";
                if (node.isMarkedForDeletion()) {
                    classes += "markedForDeletion ";
                }
                if (node.getNodeTypes().contains("jmix:moduleImportFile")) {
                    classes += "notPublished ";
                }
                if (!isPermitted(node)) {
                    classes += "accessForbidden ";
                }
                if (classes.length() > 0) {
                    v = "<span class=\"" + classes + "\">" + v + "</span>";
                }
                return v;
            }
        });
        handleEvent(contextMenu);

        return tab;
    }

    private boolean isPermitted(GWTJahiaNode node) {
        return PermissionsUtils.isPermitted("pageComposerAccess", JahiaGWTParameters.getSiteNode())
                || PermissionsUtils.isPermitted("jContentAccess", JahiaGWTParameters.getSiteNode())
                || PermissionsUtils.isPermitted("jcr:write_default", node);
    }

    private void handleMouseClick() {
        this.tree.setSelectionModel(new TreeGridSelectionModel<GWTJahiaNode>() {
            @Override
            protected void handleMouseClick(GridEvent<GWTJahiaNode> e) {
                super.handleMouseClick(e);
                if (!getSelectedItem().getPath().equals(editLinker.getMainModule().getPath())
                        && !getSelectedItem().getNodeTypes().contains(Constants.JAHIANT_VIRTUALSITE)
                        && !getSelectedItem().getNodeTypes().contains(Constants.JAHIANT_FOLDER)
                        && !getSelectedItem().getInheritedNodeTypes().contains(Constants.JAHIANT_FOLDER)
                        && !getSelectedItem().getNodeTypes().contains(Constants.JAHIANT_TEMPLATESFOLDER)) {
                    MainModule.staticGoTo(getSelectedItem().getPath(), null, "generic", "");
                }
            }
        });
    }

    private void handleEvent(Menu contextMenu) {
        this.tree.getSelectionModel().addListener(Events.BeforeSelect, new Listener<SelectionEvent<GWTJahiaNode>>() {
            @Override
            public void handleEvent(SelectionEvent<GWTJahiaNode> be) {
                if (be.getModel().getNodeTypes().contains("jmix:moduleImportFile")) {
                    tree.setContextMenu(null);
                    be.setCancelled(true);
                } else {
                    tree.setContextMenu(contextMenu);
                    contextMenu.show();
                }
            }
        });
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        toolbar = new ActionToolbar(config.getTableContextMenu(), linker);
        toolbar.createToolBar();
        layoutContainer.add(toolbar);

        tab.remove(layoutContainer);
        VBoxLayoutData treeVBoxData = new VBoxLayoutData();
        treeVBoxData.setFlex(0);
        tab.insert(layoutContainer, 0, treeVBoxData);

        checkIfCurrentNodeIsModule();
    }

    @Override
    public void handleNewMainSelection(String path) {
        super.handleNewMainSelection(path);
        toolbar.handleNewLinkerSelection();
    }

    @Override
    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        GWTJahiaNode siteNode = JahiaGWTParameters.getSiteNode();
        if (siteNode.get("j:sourcesFolder") != null) {
            super.handleNewMainNodeLoaded(node);
        }
        toolbar.handleNewMainNodeLoaded(node);
    }

    @Override
    public boolean needRefresh(Map<String, Object> data) {
        data.put(Linker.REFRESH_MAIN, true);
        if (data.containsKey("node")) {
            GWTJahiaNode node = (GWTJahiaNode) data.get("node");
            List<String> inheritedNodeTypes = node.getInheritedNodeTypes();
            List<String> nodeTypes = node.getNodeTypes();
            if (inheritedNodeTypes.contains("jnt:editableFile")
                    || nodeTypes.contains("jnt:editableFile")
                    || nodeTypes.contains("jnt:definitionFile")
                    || inheritedNodeTypes.contains("jnt:nodeType")
                    || nodeTypes.contains("jnt:resourceBundleFile")
                    || nodeTypes.contains("jnt:folder")
                    || nodeTypes.contains("jnt:file")) {
                return true;
            }
        }
        if (data.containsKey("event") && ("commit".equals(data.get("event"))
                || "update".equals(data.get("event")))
                || (data.containsKey(Linker.MAIN_DELETED) && (Boolean) data.get(Linker.MAIN_DELETED))) {
            return true;
        }
        return super.needRefresh(data);
    }


    @Override
    public void markForAutoRefresh(Map<String, Object> data) {
        checkIfCurrentNodeIsModule();
        super.markForAutoRefresh(data);
    }

    @Override
    public void doRefresh() {
        List<String> selectedPath = factory.getSelectedPath();
        factory.getStore().removeAll();
        factory.setSelectedPath(selectedPath);
        if (checkIfCurrentNodeIsModule()) {
            factory.getStore().getLoader().load();
        }
    }

    private boolean checkIfCurrentNodeIsModule() {
        GWTJahiaNode siteNode = JahiaGWTParameters.getSiteNode();
        if (siteNode.get("j:sourcesFolder") == null) {
            ((TabPanel) tab.getParent()).mask();
        } else {
            ((TabPanel) tab.getParent()).unmask();
        }
        return siteNode.get("j:sourcesFolder") != null;
    }

    @Override
    protected boolean acceptNode(GWTJahiaNode node) {
        return true;
    }
}
