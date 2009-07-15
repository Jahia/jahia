/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.workflow;

import org.jahia.ajax.gwt.client.widget.tripanel.LeftComponent;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowElement;
import org.jahia.ajax.gwt.client.service.workflow.WorkflowServiceAsync;
import org.jahia.ajax.gwt.client.service.workflow.WorkflowService;
import org.jahia.ajax.gwt.client.util.tree.CustomTreeLoader;
import org.jahia.ajax.gwt.client.util.tree.PreviousPathsOpener;
import org.jahia.ajax.gwt.client.util.tree.CustomTreeBinder;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.CheckChangedListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 16 juil. 2008 - 16:30:16
 */
public class WorkflowTree extends LeftComponent {

    private ContentPanel m_component;

    private TreePanel<GWTJahiaWorkflowElement> m_tree;
    private TreeLoader<GWTJahiaWorkflowElement> loader;
    private TreeStore<GWTJahiaWorkflowElement> store;

    private PreviousPathsOpener<GWTJahiaWorkflowElement> previousPathsOpener = null;
    private ModelData lastSelection = null;
    private String rootPage;

    public WorkflowTree(String siteKey, String startPage) {
        m_component = new ContentPanel(new FitLayout());
        m_component.setHeading(siteKey);
        m_component.setScrollMode(Style.Scroll.AUTO);

        rootPage = startPage;
        final WorkflowServiceAsync service = WorkflowService.App.getInstance();

        // data proxy
        RpcProxy<List<GWTJahiaWorkflowElement>> proxy = new RpcProxy<List<GWTJahiaWorkflowElement>>() {
            protected void load(Object gwtJahiaFolder, AsyncCallback<List<GWTJahiaWorkflowElement>> listAsyncCallback) {
                service.getSubElements((GWTJahiaWorkflowElement) gwtJahiaFolder, listAsyncCallback);
            }
        };

        // tree loader
        loader = new CustomTreeLoader<GWTJahiaWorkflowElement>(proxy) {
            @Override
            public boolean hasChildren(GWTJahiaWorkflowElement parent) {
                return parent.hasChildren();
            }

            @Override
            protected void onLoadSuccess(Object parent, List<GWTJahiaWorkflowElement> children) {
                super.onLoadSuccess(parent, children);
                if (openPreviousPaths) {
                    openPreviousPaths = false;
                    // expandPreviousPaths();
                }
            }

            protected void expandPreviousPaths() {
                //expandWorkflowPreviousPaths() ;
            }
        };

        // tree store
        store = new TreeStore<GWTJahiaWorkflowElement>(loader);

        // tree panel
        m_tree = new TreePanel<GWTJahiaWorkflowElement>(store);
        m_tree.setCaching(true);
        m_tree.setDisplayProperty("title");


        /*
        GXT 2.0: To do - add icon provider
        m_tree.setIconProvider(new ModelIconProvider<GWTJahiaWorkflowElement>() {
            public AbstractImagePrototype getIcon(GWTJahiaWorkflowElement gwtJahiaWorkflowElement) {
                // TO DO: replaced by ImageBubdle impl
                return IconHelper.create(getStringValue(gwtJahiaWorkflowElement));
            }


            private String getStringValue(GWTJahiaWorkflowElement modelData) {
                return "/images/icons/icon-" + modelData.getObjectType();
            }
        });*/

        m_tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaWorkflowElement>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaWorkflowElement> event) {
                Log.debug("selection change listener");
                ModelData newSelection = m_tree.getSelectionModel().getSelectedItem();
                if (lastSelection != newSelection) {
                    lastSelection = newSelection;
                    getLinker().onTreeItemSelected();
                }
            }
        });

        m_component.add(m_tree);
    }

    @Override
    public void initWithLinker(BrowserLinker linker) {
        super.initWithLinker(linker);
        loader.load(new GWTJahiaWorkflowElement(rootPage));
    }

    private void expandWorkflowPreviousPaths() {
        if (previousPathsOpener == null) {
            previousPathsOpener = new PreviousPathsOpener<GWTJahiaWorkflowElement>(m_tree, store);
        }
        previousPathsOpener.expandPreviousPaths();
    }

    public void openAndSelectItem(Object item) {
        // not needed here
    }

    public void refresh() {
        List<GWTJahiaWorkflowElement> selection = m_tree.getSelectionModel().getSelection();
        if (selection != null && selection.size() > 0) {
            loader.loadChildren(selection.get(0));
        }

    }

    public Object getSelectedItem() {
        List<GWTJahiaWorkflowElement> selection = m_tree.getSelectionModel().getSelection();
        if (selection != null && selection.size() > 0) {
            return selection.get(0);
        } else {
            return null;
        }
    }

    public Component getComponent() {
        return m_component;
    }

}
