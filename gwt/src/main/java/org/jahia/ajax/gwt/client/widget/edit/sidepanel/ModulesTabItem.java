/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbar;
import java.util.Comparator;
import java.util.Map;


public class ModulesTabItem extends BrowseTabItem {
    private static final long serialVersionUID = 7656741991148114672L;
    protected transient ListLoader<ListLoadResult<GWTJahiaNode>> listLoader;
    protected transient ListStore<GWTJahiaNode> contentStore;
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
                        MainModule.staticGoTo(getSelectedItem().getPath(), null);
                    }
                }
            }
        });
        this.tree.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        this.tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                listLoader.load(se.getSelectedItem());
            }
        });


        // data proxy
        RpcProxy<ListLoadResult<GWTJahiaNode>> listProxy = new RpcProxy<ListLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(final Object gwtJahiaFolder, final AsyncCallback<ListLoadResult<GWTJahiaNode>> listAsyncCallback) {
            }
        };

        listLoader = new BaseListLoader<ListLoadResult<GWTJahiaNode>>(listProxy);

        contentStore = new ListStore<GWTJahiaNode>(listLoader);
        contentStore.setStoreSorter(new StoreSorter<GWTJahiaNode>(new Comparator<Object>() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public int compare(Object o1, Object o2) {
                if (o1 instanceof String && o2 instanceof String) {
                    String s1 = (String) o1;
                    String s2 = (String) o2;
                    return Collator.getInstance().localeCompare(s1,s2);
                } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
                    return ((Comparable) o1).compareTo(o2);
                }
                return 0;
            }
        }));
        contentStore.setSortField("display");

        tree.setContextMenu(createContextMenu(config.getTreeContextMenu(), tree.getSelectionModel()));

        return tab;
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        toolbar = new ActionToolbar(config.getTableContextMenu(), linker);
        toolbar.createToolBar();
        layoutContainer.add(toolbar);
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
        if (data.containsKey("node")) {
            GWTJahiaNode node = (GWTJahiaNode) data.get("node");
            if (node.getInheritedNodeTypes().contains("jnt:editableFile")
                    || node.getNodeTypes().contains("jnt:definitionFile")
                    || node.getInheritedNodeTypes().contains("jnt:nodeType")
                    || node.getNodeTypes().contains("jnt:resourceBundleFile")) {
                return true;
            }
        }
        if (data.containsKey("event") && "commit".equals(data.get("event"))) {
            return true;
        }
        return super.needRefresh(data);
    }

    @Override
    public void doRefresh() {
        tree.getTreeStore().removeAll();
        GWTJahiaNode siteNode = JahiaGWTParameters.getSiteNode();
        if (siteNode.get("j:sourcesFolder") == null) {
            tree.mask("Sources required - Get them from source control");
        } else {
            tree.unmask();
            tree.getTreeStore().getLoader().load();
            listLoader.load();
        }
    }

    @Override
    protected boolean acceptNode(GWTJahiaNode node) {
        return true;
    }
}
