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
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.SearchField;

import java.util.List;

/**
 * User: rfelden
 * Date: 21 oct. 2008 - 16:53:43
 */
public class SearchGrid extends ContentPanel {

    //private ContentPanel m_component;
    //private Grid<GWTJahiaNode> grid;
    private ThumbsListView listView;
    private ListStore<GWTJahiaNode> store;

    private GWTManagerConfiguration config;
    private boolean multiple = false;

    public SearchGrid(GWTManagerConfiguration config, boolean multiple) {
        this(config);
        this.multiple = multiple;
    }

    public SearchGrid(GWTManagerConfiguration config) {
        this.config = config;
        setLayout(new FitLayout());
        setBorders(false);
        setBodyBorder(false);
        setId("images-view");
        SearchField searchField = new SearchField(Messages.get("label_search","Search")+": ", false) {
            public void onFieldValidation(String value) {
                setSearchContent(value,listView);
            }

            public void onSaveButtonClicked(String value) {
                // ... no save button
            }
        };
        setHeaderVisible(false);
        setTopComponent(searchField);
        store = new ListStore<GWTJahiaNode>();
        listView = new ThumbsListView(true);
        listView.setStore(store);
        listView.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                if (event != null && event.getSelectedItem() != null) {
                    onContentPicked(event.getSelectedItem());
                }
            }
        });
        /*grid = new Grid<GWTJahiaNode>(store, getHeaders());
        grid.getView().setForceFit(true);
        grid.setBorders(false);
        grid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                if (event != null && event.getSelectedItem() != null) {
                    onContentPicked(event.getSelectedItem());
                }
            }
        });
        add(grid);*/


        listView = new ThumbsListView(true);
        listView.setStore(store);
        listView.setBorders(false);
        listView.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        listView.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                if (event != null && event.getSelectedItem() != null) {
                    onContentPicked(event.getSelectedItem());
                }
            }
        });


        add(listView);
    }

    public void setSearchContent(final String text,final Component parent) {
        clearTable();
        parent.mask(Messages.get("label_search","Search"));
        if (text != null && text.length() > 0) {
            final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();

            service.search(text, 0, config.getNodeTypes(), config.getMimeTypes(), config.getFilters(), new BaseAsyncCallback<List<GWTJahiaNode>>() {
                public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                    if (gwtJahiaNodes != null) {
                        Log.debug("Found " + gwtJahiaNodes.size() + " results with text: " + text);
                        setProcessedContent(gwtJahiaNodes);
                    } else {
                        Log.debug("No result found in search");
                    }
                    parent.unmask();
                }

                public void onApplicationFailure(Throwable throwable) {
                    Window.alert("Element list retrieval failed :\n" + throwable.getLocalizedMessage());
                    parent.unmask();
                }


            });
        } else {
            refresh();
        }
    }

    public void setContent(Object root) {
        // do nothing, content is set with setProcessedContent
    }

    public void setProcessedContent(Object content) {
        clearTable();
        store.add((List<GWTJahiaNode>) content);
        if (store.getSortState().getSortField() != null && store.getSortState().getSortDir() != null) {
            store.sort(store.getSortState().getSortField(), store.getSortState().getSortDir());
        } else {
            store.sort("date", Style.SortDir.DESC);
        }
    }

    public void clearTable() {
        store.removeAll();
    }

    public Object getSelection() {
        return listView.getSelectionModel().getSelectedItems();
    }

    public void refresh() {
        // nothing here
    }


    public void clearSelection() {
        listView.getSelectionModel().deselectAll();
    }

    /**
     * Override this method to customize "add" button behavirou
     *
     * @param gwtJahiaNode
     */
    public void onContentPicked(final GWTJahiaNode gwtJahiaNode) {

    }
}
