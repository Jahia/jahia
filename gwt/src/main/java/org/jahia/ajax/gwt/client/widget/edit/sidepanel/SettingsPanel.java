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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTColumn;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;

import java.io.Serializable;
import java.util.*;

/**
 * Panel that display settings
 */
public class SettingsPanel extends SidePanelTabItem {

    private transient ContentPanel settingsPanel;
    private String settingPath;
    private String settingTemplateRoot;
    private String label;
    private String requiredPermission;
    private transient TreeLoader<GWTJahiaNode> settingsLoader;
    private transient TreeStore<GWTJahiaNode> settingsStore;
    private transient List<String> paths;
    private transient Map<String, Set<GWTJahiaNode>> nodesBySettingsPath;

    @Override
    public TabItem create(GWTSidePanelTab config) {
        super.create(config);
        tab.setLayout(new FitLayout());
        settingsPanel = new ContentPanel();
        settingsPanel.setAnimCollapse(false);
        settingsPanel.setHeading(Messages.get(label));
        settingsPanel.setLayout(new FitLayout());
        tab.add(settingsPanel);
        return tab;
    }

    @Override
    public void handleNewMainNodeLoaded(final GWTJahiaNode mainNode) {
        super.handleNewMainNodeLoaded(mainNode);
        LayoutContainer treeContainer = new LayoutContainer();
        treeContainer.setBorders(false);
        treeContainer.setScrollMode(Style.Scroll.AUTO);
        treeContainer.setLayout(new FitLayout());
        // resolve paths from dependencies

        paths = new ArrayList<String>();
        nodesBySettingsPath = new HashMap<String, Set<GWTJahiaNode>>();

        NodeColumnConfigList columns = new NodeColumnConfigList(Arrays.asList(new GWTColumn("displayName", "", -1)));
        columns.init();
        columns.get(0).setRenderer(new TreeGridCellRenderer<GWTJahiaNode>() {
            @Override
            protected String getText(TreeGrid<GWTJahiaNode> gwtJahiaNodeTreeGrid, GWTJahiaNode node, String property, int rowIndex, int colIndex) {
                String v = super.getText(gwtJahiaNodeTreeGrid, node, property, rowIndex, colIndex);
                if (v != null) {
                    v = SafeHtmlUtils.htmlEscape(v);
                }
                List<String> requiredPermissions = node.get("j:requiredPermissions");
                if (requiredPermissions != null) {
                    boolean access = true;
                    for (String p : requiredPermissions) {
                        if (!PermissionsUtils.isPermitted(p.substring(p.lastIndexOf('/') + 1), mainNode)) {
                            access = false;
                            break;
                        }
                    }
                    if (!access) {
                        v = "<span class=\"accessForbidden\">" + v + "</span>";
                    }
                }
                return v;
            }
        });
        RpcProxy<List<GWTJahiaNode>> proxy = new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object loadConfig, final AsyncCallback<List<GWTJahiaNode>> callback) {
                List<String> fields = new ArrayList<String>();
                fields.add(GWTJahiaNode.LOCKS_INFO);
                fields.add(GWTJahiaNode.PERMISSIONS);
                fields.add(GWTJahiaNode.CHILDREN_INFO);
                fields.add(GWTJahiaNode.ICON);
                fields.add(GWTJahiaNode.LOCKS_INFO);
                fields.add("j:requiredPermissions");
                JahiaContentManagementService.App.getInstance()
                        .getRoot(paths, Arrays.asList("jnt:template"), null, null, fields, null, null, true,
                                false, null, null, true, new AsyncCallback<List<GWTJahiaNode>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                callback.onFailure(caught);
                            }

                            @Override
                            public void onSuccess(List<GWTJahiaNode> nodes) {
                                List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
                                for (GWTJahiaNode node : nodes) {
                                    String settingsPath = getSettingsPath(node);
                                    Set<GWTJahiaNode> nodeSet = nodesBySettingsPath.get(settingsPath);
                                    if (nodeSet == null) {
                                        nodeSet = new HashSet<GWTJahiaNode>();
                                        nodesBySettingsPath.put(settingsPath, nodeSet);
                                    }
                                    nodeSet.add(node);
                                    String nodeName = node.getName();
                                    boolean add = true;
                                    for (GWTJahiaNode resultNode : result) {
                                        if (resultNode.getName().equals(nodeName)) {
                                            add = false;
                                            break;
                                        }
                                    }
                                    if (add) {
                                        result.add(node);
                                    }
                                }
                                callback.onSuccess(result);
                            }
                        });

            }
        };

        settingsLoader = new BaseTreeLoader<GWTJahiaNode>(proxy) {
            @Override
            public boolean hasChildren(GWTJahiaNode parent) {
                paths.clear();
                for (GWTJahiaNode n : nodesBySettingsPath.get(getSettingsPath(parent))) {
                    paths.add(n.getPath()+ "/*");
                }
                return !parent.isNodeType("jnt:contentTemplate");
            }
        };
        settingsStore = new TreeStore<GWTJahiaNode>(settingsLoader);
        settingsStore.setStoreSorter(new StoreSorter<GWTJahiaNode>(new Comparator<Object>() {
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
        }));
        TreeGrid<GWTJahiaNode> settingsTree = new TreeGrid<GWTJahiaNode>(settingsStore,new ColumnModel(columns));


        settingsTree.setAutoExpandColumn("displayName");
        settingsTree.getTreeView().setRowHeight(25);
        settingsTree.getTreeView().setForceFit(true);
        settingsTree.setHeight("100%");
        settingsTree.setIconProvider(ContentModelIconProvider.getInstance());

        treeContainer.add(settingsTree);

        settingsTree.setHideHeaders(true);
        settingsTree.setAutoExpand(false);
        final String path = settingPath.replaceAll("\\$site",JahiaGWTParameters.getSiteNode().getPath());
        // get List of site settings
        settingsTree.setSelectionModel(new TreeGridSelectionModel<GWTJahiaNode>() {
            @Override
            protected void handleMouseClick(GridEvent<GWTJahiaNode> e) {
                super.handleMouseClick(e);
                if (e.getModel().isNodeType("jnt:contentTemplate")) {
                    MainModule.staticGoTo(path,getSelectedItem().getName());
                }
            }
        });

        VBoxLayoutData treeVBoxData = new VBoxLayoutData();
        treeVBoxData.setFlex(1);

        settingsPanel.add(treeContainer, treeVBoxData);
        doRefresh();
    }

    public void setSettingsTemplateRoot(String settingTemplateRoot) {
        this.settingTemplateRoot = settingTemplateRoot;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    /**
     * setter for setting path.
     * @param settingPath path used to display the setting, $site for current site path
     */
    public void setSettingPath(String settingPath) {
        this.settingPath = settingPath;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    @Override
    public void doRefresh() {
        paths.clear();
        if (JahiaGWTParameters.getSiteNode() != null && JahiaGWTParameters.getSiteNode().getProperties().get("j:installedModules") != null) {
            for (String module : ((List<String>) JahiaGWTParameters.getSiteNode().get("j:installedModules"))) {
                paths.add("/modules/" + module + "/$moduleversion/templates/" + settingTemplateRoot + "/*");
            }
        }

        settingsStore.removeAll();
        settingsLoader.load();
    }

    private String getSettingsPath(GWTJahiaNode node) {
        String nodePath = node.getPath();
        return nodePath.substring(nodePath.indexOf(settingTemplateRoot) + settingTemplateRoot.length());
    }

}