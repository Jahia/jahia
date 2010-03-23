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
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.data.ModelComparer;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Aug 21, 2009
 * Time: 9:58:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class PickedContentView extends BottomRightComponent  implements PickedContent{
    private String pickerType;
    private TabPanel m_component;
    private GroupingStore<GWTJahiaNode> store;
    private Grid<GWTJahiaNode> m_grid;
    private ThumbsListView m_thumbsListView;
    private ManagerConfiguration config;
    private boolean readOnly = false;
    private boolean multiple;
    private List<GWTJahiaNode> selectedNodes;
    private String emtypSelectionMessage = "No selection";
    private String selectionHeaderMessage = "Image selected: ";
    private TabItem itemPreview = new TabItem("Preview");

    public PickedContentView(String selectionLabel,String pickerType, List<GWTJahiaNode> selectedNodes, boolean multiple, final ManagerConfiguration config) {
        this.selectionHeaderMessage =  selectionLabel;
        this.config = config;
        this.pickerType = pickerType;
        this.selectedNodes = selectedNodes;
        this.multiple = multiple;
        createUI();
    }


    /**
     * Create UI
     */
    private void createUI() {
        m_component = new TabPanel();

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig();
        column.setWidth(20);
        column.setId("preview");
        column.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public Object render(GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                if (multiple) {
                    return "<img heigth='16px' width='32px' src=\"" + gwtJahiaNode.getPreview() + "\">";
                } else {
                    return "<img heigth='40px' width='60px' src=\"" + gwtJahiaNode.getPreview() + "\">";
                }
            }
        });
        column.setHeader(Messages.getResource("displayName"));
        configs.add(column);

        //name
        column = new ColumnConfig();
        column.setWidth(100);
        column.setAlignment(Style.HorizontalAlignment.LEFT);
        column.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public Object render(GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {

                String html = "<div class=\"details\"> \n" +
                        "      <b> Name:</b> \n" +
                        "      <span>" + gwtJahiaNode.getName() + "</span>\n" +
                        "      <b>Alt:</b> \n" +
                        "      <span>" + gwtJahiaNode.getName() + "</span>\n" +
                        "      <br/> <b>Path:</b> \n" +
                        "      <span>" + gwtJahiaNode.getPath() + "</span></div> \n" +
                        "      </div>";
                return html;
            }
        });
        column.setId("name");
        column.setHeader(Messages.getResource("name"));
        configs.add(column);

        // displaName
        column = new ColumnConfig();
        column.setId("displayName");
        column.setHidden(true);
        column.setHeader(Messages.getResource("displayName"));
        configs.add(column);

        // path
        column = new ColumnConfig();
        column.setId("path");
        column.setHeader(Messages.getResource("path"));
        configs.add(column);

        // remvove
        column = new ColumnConfig();
        column.setWidth(100);
        column.setAlignment(Style.HorizontalAlignment.RIGHT);
        column.setId("remove");
        column.setHeader("");
        column.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public Object render(final GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                final Button pickContentButton = new Button(Messages.getResource("fm_remove"));
                pickContentButton.setIcon(ContentModelIconProvider.getInstance().getMinusRound());
                pickContentButton.setIconStyle("gwt-icons-delete");
                pickContentButton.setBorders(false);
                pickContentButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    public void componentSelected(ButtonEvent buttonEvent) {
                        store.remove(gwtJahiaNode);
                    }
                });
                return pickContentButton;
            }
        });
        configs.add(column);

        // list loader
        store = new GroupingStore<GWTJahiaNode>();
        store.add(selectedNodes);
        store.setModelComparer(new ModelComparer<GWTJahiaNode>() {
            public boolean equals(GWTJahiaNode gwtJahiaNode, GWTJahiaNode gwtJahiaNode1) {
                String path = gwtJahiaNode.getPath();
                String path2 = gwtJahiaNode1.getPath();
                if (path == null && path2 == null) {
                    return true;
                } else if (path == null) {
                    return false;
                }
                return path.equalsIgnoreCase(path2);
            }
        });
        final ColumnModel columnModel = new ColumnModel(configs);
        store.sort("name", Style.SortDir.ASC);
        store.groupBy("path");

        // grid
        m_grid = new Grid<GWTJahiaNode>(store, columnModel);

        m_grid.setHideHeaders(true);
        m_grid.setBorders(true);


        // Grouping view
        GroupingView view = new GroupingView();
        view.setShowGroupedColumn(false);
        view.setForceFit(true);

        view.setEmptyText(emtypSelectionMessage);
        view.setGroupRenderer(new GridGroupRenderer() {
            public String render(GroupColumnData data) {
                return selectionHeaderMessage + ((GWTJahiaNode) data.models.get(0)).getPath();
            }
        });
        m_grid.setView(view);


        final TabItem item = new TabItem("Selection");
        item.setLayout(new FillLayout());
        item.add(m_grid);
        m_component.add(item);

        // display preview only for file
        if (pickerType != null && (pickerType.equalsIgnoreCase(ManagerConfigurationFactory.FILEPICKER))) {
            if (!pickerType.equalsIgnoreCase(ManagerConfigurationFactory.LINKPICKER)) {
                m_thumbsListView = new ThumbsListView(true);
                m_thumbsListView.setStore(store);
                m_thumbsListView.setTemplate(getThumbsListTemplate());
                m_thumbsListView.setItemSelector("div.thumb-wrap");
                m_thumbsListView.setOverStyle("x-view-over");

                itemPreview.setLayout(new FitLayout());
                ContentPanel previewPanel = new ContentPanel(new FitLayout());
                previewPanel.setHeaderVisible(false);
                previewPanel.setScrollMode(Style.Scroll.AUTO);
                previewPanel.setId("images-view");
                previewPanel.setBorders(true);
                previewPanel.setBodyBorder(false);
                previewPanel.add(m_thumbsListView);
                itemPreview.add(previewPanel);
            }
            m_component.add(itemPreview);
        }

        // case of a page
        if (multiple && pickerType != null && pickerType.equalsIgnoreCase(ManagerConfigurationFactory.LINKPICKER)){
            m_component.add(itemPreview);

        }

        //add(m_component);

    }

    public void clear() {
        store.removeAll();
    }

    /**
     * Set content
     *
     * @param root
     */
    public void fillData(Object root) {
        if (readOnly) {
            return;
        }
        if (root instanceof List) {
            setSelection((List<GWTJahiaNode>) root);
        } else {
            if (root instanceof GWTJahiaNode) {
                List<GWTJahiaNode> list = new ArrayList<GWTJahiaNode>();
                list.add((GWTJahiaNode) root);
                setSelection(list);
            }

            if (!multiple && pickerType.equalsIgnoreCase(ManagerConfigurationFactory.LINKPICKER)) {
                itemPreview.setUrl(((GWTJahiaNode) root).getPath());
            }
        }
    }


    /**
     * Get selected categories
     *
     * @return
     */
    public List<GWTJahiaNode> getSelection() {
        return store.getModels();
    }

    /**
     * Set selected contains
     *
     * @param selection
     */
    public void setSelection(final List<GWTJahiaNode> selection) {
        if (readOnly) {
            return;
        }
        String[] nt = config.getNodeTypes().split(",");
        if (selection != null && selection.size() > 0) {
            boolean found = false;
            for (String s : nt) {
                if (config.getNodeTypes().length() == 0 || selection.get(0).getNodeTypes().contains(s) || selection.get(0).getInheritedNodeTypes().contains(s)) {
                    found = true;
                    break;
                }
            }

            // handle multiple
            if (found) {
                if (multiple) {
                    for (GWTJahiaNode n : selection) {
                        if (!store.contains(n)) {
                            store.add(n);
                        }
                    }
                } else {
                    store.removeAll();
                    store.add(selection.get(0));
                }
            }
        }
    }

    /**
     * refresh
     */
    public void refresh() {
        Log.warn("Method refresh() no implemented");
    }

    /**
     * Get component
     *
     * @return
     */
    public Component getComponent() {
        return m_component;
    }

    /**
     * return selected content
     * @return
     */
    public List<GWTJahiaNode> getSelectedContent() {
        return store.getModels();
    }

    /**
     * return url od the selected content. 
     * @param rewrite is true, the url is rewrited (ie.: for url that will be used in big text)
     * @return
     */
    public List<String> getSelectedContentPath(final String jahiaContextPath,final String jahiaServletPath, final boolean rewrite){
        List<GWTJahiaNode>  selectedContents = getSelectedContent();
        if(selectedContents == null){
            return null;
        }else if(selectedContents.isEmpty()){
            return new ArrayList<String>();
        }else{
            List<String> pathes = new ArrayList<String>();
            for(GWTJahiaNode s:selectedContents){
                pathes.add(s.getUrl());
            }
            return pathes;
        }
    }


    public native String getThumbsListTemplate() /*-{
          return ['<tpl for=".">',
                '<div class="thumb-wrap" id="{name}">',
                '<div class="thumb"><img src="{preview}" title="{name}"></div>',
                '<span class="x-editable">{shortName}</span></div>',
                '</tpl>',
                '<div class="x-clear"></div>'].join("");
    }-*/;

}

