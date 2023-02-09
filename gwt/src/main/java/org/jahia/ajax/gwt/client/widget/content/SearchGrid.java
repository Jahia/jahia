/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
