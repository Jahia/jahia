/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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

        this.tree.setSelectionModel(new TreeGridSelectionModel<GWTJahiaNode>() {
            @Override
            protected void handleMouseClick(GridEvent<GWTJahiaNode> e) {
                super.handleMouseClick(e);
                if (!getSelectedItem().getPath().equals(editLinker.getMainModule().getPath())) {
                    if (!getSelectedItem().getNodeTypes().contains("jnt:virtualsite")
                            && !getSelectedItem().getNodeTypes().contains("jnt:folder")
                            && !getSelectedItem().getInheritedNodeTypes().contains("jnt:folder")
                            && !getSelectedItem().getNodeTypes().contains("jnt:templatesFolder")) {
                        MainModule.staticGoTo(getSelectedItem().getPath(), null, "generic", "");
                    }
                }
            }
        });
        this.tree.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);

        final Menu contextMenu = createContextMenu(config.getTreeContextMenu(), tree.getSelectionModel());
        tree.setContextMenu(contextMenu);

        tree.getColumnModel().getColumn(0).setRenderer(new TreeGridCellRenderer<GWTJahiaNode>() {
            @Override
            protected String getText(TreeGrid<GWTJahiaNode> gwtJahiaNodeTreeGrid, GWTJahiaNode node, String property, int rowIndex, int colIndex) {
                String v = super.getText(gwtJahiaNodeTreeGrid, node, property, rowIndex, colIndex);
                if (v != null) {
                    v = SafeHtmlUtils.htmlEscape(v);
                }
                String classes = "";
                if (node.getNodeTypes().contains("jmix:markedForDeletion")) {
                    classes += "markedForDeletion ";
                }
                if (node.getNodeTypes().contains("jmix:moduleImportFile")) {
                    classes += "notPublished ";
                }
                if (!PermissionsUtils.isPermitted("editModeAccess", JahiaGWTParameters.getSiteNode()) && !PermissionsUtils.isPermitted("jcr:write_default", node)) {
                    classes += "accessForbidden ";
                }
                if (classes.length() > 0) {
                    v = "<span class=\"" + classes + "\">" + v + "</span>";
                }
                return v;
            }
        });
        this.tree.getSelectionModel().addListener(Events.BeforeSelect, new Listener<SelectionEvent<GWTJahiaNode>>() {
            @Override
            public void handleEvent(SelectionEvent<GWTJahiaNode> be) {
                if (be.getModel().getNodeTypes().contains("jmix:moduleImportFile")) {
                    tree.setContextMenu(null);
                    be.setCancelled(true);
                } else {
                    tree.setContextMenu(contextMenu);
                }
            }
        });

        return tab;
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        toolbar = new ActionToolbar(config.getTableContextMenu(), linker);
        toolbar.createToolBar();
        layoutContainer.add(toolbar);
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
        if (data.containsKey("event") && ("commit".equals(data.get("event")) || "update".equals(data.get("event")))) {
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
        tree.getTreeStore().removeAll();
        if (checkIfCurrentNodeIsModule()) {
            tree.getTreeStore().getLoader().load();
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
