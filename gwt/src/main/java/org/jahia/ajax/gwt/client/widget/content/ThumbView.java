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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

import java.util.List;
import java.util.ArrayList;

/**
 * User: rfelden
 * Date: 9 sept. 2008 - 17:49:57
 */
public class ThumbView extends TopRightComponent {

    private ContentPanel m_component;
    private ListStore<GWTJahiaNode> store;
    private ThumbsListView view;
    private SimpleComboBox<String> sort;
    private ToggleButton sortOrder ;

    private ManagerConfiguration configuration;

    public ThumbView(final ManagerConfiguration config, boolean detailed) {
        configuration = config;

        m_component = new ContentPanel(new FitLayout());
        m_component.setHeaderVisible(false);
        m_component.setScrollMode(Style.Scroll.AUTO);
        m_component.setId("images-view");
        m_component.setBorders(true);
        m_component.setBodyBorder(false);
        if (GXT.isIE) {
            m_component.setHeight(400); // media gallery fix
        }

        ToolBar bar = new ToolBar();
        bar.add(new LabelToolItem(Messages.getResource("fm_thumbFilter")));

        store = new ListStore<GWTJahiaNode>();

        StoreFilterField<GWTJahiaNode> field = new StoreFilterField<GWTJahiaNode>() {
            @Override
            protected boolean doSelect(Store<GWTJahiaNode> store, GWTJahiaNode parent, GWTJahiaNode record, String property, String filter) {
                String name = record.getName().toLowerCase();
                return name.indexOf(filter.toLowerCase()) != -1;
            }

            @Override
            protected void onFilter() {
                super.onFilter();
//                view.getSelectionModel().select(0);
            }
        };
        field.setWidth(200);
        field.bind(store);

        bar.add(field);
        bar.add(new SeparatorToolItem());
        bar.add(new LabelToolItem(Messages.getResource("fm_thumbSort")));

        // please keep same order as in sort() method
        List<String> sorts = new ArrayList<String>();
        sorts.add(Messages.getResource("fm_thumbSortName"));
        if (config.isDisplaySize()) {
            sorts.add(Messages.getResource("fm_thumbSortSize"));
        }
        if (config.isDisplayDate()) {
            sorts.add(Messages.getResource("fm_thumbSortLastModif"));
        }

        sort = new SimpleComboBox<String>();
        sort.setTriggerAction(ComboBox.TriggerAction.ALL);
        sort.setEditable(false);
        sort.setForceSelection(true);
        sort.setWidth(250);
        sort.add(sorts);
        sort.setSimpleValue(sorts.get(0));
        sort.addListener(Events.SelectionChange, new Listener<SelectionChangedEvent>() {
            public void handleEvent(SelectionChangedEvent be) {
                sort();
            }
        });
        sortOrder = new ToggleButton(Messages.getResource("fm_invertSort"));
        sortOrder.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent componentEvent) {
                sort();
            }
        });

        bar.add(sort);
        bar.add(sortOrder);

        m_component.setTopComponent(bar);

        view = new ThumbsListView(detailed);
        view.setStore(store);

        view.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> gwtJahiaNodeSelectionChangedEvent) {
                getLinker().onTableItemSelected();
                TopRightComponent topRight = getLinker().getTopRightObject() ;
                if (topRight instanceof ContentPickerBrowser) {
                    ((ContentPickerBrowser) topRight).handleNewSelection();
                }
            }
        });
        view.addListener(Events.DoubleClick, new Listener<ListViewEvent>() {
            public void handleEvent(ListViewEvent event) {
                List<GWTJahiaNode> selection = (List<GWTJahiaNode>) getLinker().getTableSelection();
                if (selection != null && selection.size() > 0) {
                    GWTJahiaNode selected = selection.get(0);
                    if (selected.isFile()) {
                        if (config.isEnableFileDoubleClick()) {
                            if (selected.isDisplayable()) {
                                ImagePopup.popImage(selected);
                            } else {
                                ContentActions.download(getLinker());
                            }
                        }
                    } else {
                        if (!config.isHideLeftPanel()) {
                            getLinker().onTableItemDoubleClicked(selected);
                        } else {
                            Log.debug("Double click disabled if the hide panel is hidden");
                        }
                    }
                }
            }
        });
        /*view.getSelectionModel().addListener(Events.OnDoubleClick, new Listener<SelectionEvent<GWTJahiaNode>>() {
            public void handleEvent(SelectionEvent<GWTJahiaNode> be) {
                getLinker().onTableItemDoubleClicked(be.model);
            }
        });*/
        m_component.add(view);
    }

    private void sort() {
        int index = sort.getSelectedIndex();
        if (index == 0) {
            store.sort("name", sortOrder.isPressed() ? Style.SortDir.DESC : Style.SortDir.ASC);
        } else if (index == 1) {
            store.sort("size", sortOrder.isPressed() ? Style.SortDir.DESC : Style.SortDir.ASC);
        } else if (index == 2) {
            store.sort("date", sortOrder.isPressed() ? Style.SortDir.DESC : Style.SortDir.ASC);
        }
    }

    public void setContextMenu(Menu menu) {
        view.setContextMenu(menu);
    }

    public void setContent(Object root) {
        clearTable();
        if (root != null) {
            final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
            if (getLinker() != null) {
                getLinker().loading("listing directory content...");
            }
            service.ls((GWTJahiaNode) root, configuration.getNodeTypes(), configuration.getMimeTypes(), configuration.getFilters(), !configuration.isAllowCollections(), new AsyncCallback<List<GWTJahiaNode>>() {
                public void onFailure(Throwable throwable) {
                    Window.alert("Element list retrieval failed :\n" + throwable.getLocalizedMessage());
                    if (getLinker() != null) {
                        getLinker().loaded();
                    }
                }

                public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                    if (gwtJahiaNodes != null) {
                        store.add(gwtJahiaNodes);
                        sort();
                    } else {
                        Window.alert("null list");
                    }
                    if (getLinker() != null) {
                        getLinker().loaded();
                    }
                    getLinker().onTableItemSelected();
                }
            });
        }
    }

    public void setProcessedContent(Object content) {
        clearTable();
        if (content != null) {
            List<GWTJahiaNode> gwtJahiaNodes = (List<GWTJahiaNode>) content;
            store.add(gwtJahiaNodes);
            getLinker().onTableItemSelected();
            sort();
        }
    }

    public void clearTable() {
        store.removeAll();
    }

    public Object getSelection() {
        return view.getSelectionModel().getSelectedItems();
    }

    public void refresh() {
        setContent(getLinker().getTreeSelection());
    }

    public Component getComponent() {
        return m_component;
    }

}
