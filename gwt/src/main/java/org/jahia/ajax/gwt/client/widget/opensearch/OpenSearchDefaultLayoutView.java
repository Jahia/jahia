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
package org.jahia.ajax.gwt.client.widget.opensearch;

import org.jahia.ajax.gwt.client.widget.opensearch.JahiaOpenSearchTriPanel;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngineGroup;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngine;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.util.Margins;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 24 sept. 2008
 * Time: 12:51:10
 * To change this template use File | Settings | File Templates.
 */
public class OpenSearchDefaultLayoutView extends OpenSearchLayoutView {

    private LayoutContainer containerPanel;
    private ViewLayout activeLayout;
    private HashMap<String,ViewLayout> viewLayouts;

    private String currentGroupName;

    public OpenSearchDefaultLayoutView(JahiaOpenSearchTriPanel openSearchView) {
        super(openSearchView);
        initWidget();
    }

    public void clearTable(){
    }

    public Object getSelection(){
        return null;
    }

    public void refresh() {
    }

    public Component getComponent(){
        return containerPanel;
    }

    public void search(String searchString){
        /*
        Iterator<OpenSearchWidgetItem> it = this.openSearchWidgetItems.iterator();
        OpenSearchWidgetItem OpenSearchWidgetItem = null;
        while ( it.hasNext() ){
            OpenSearchWidgetItem = it.next();
            OpenSearchWidgetItem.search(searchString);
        }*/
        this.activeLayout.search(searchString);
    }

    public void add(OpenSearchWidgetItem WidgetItem){
        for (ViewLayout viewLayout : this.viewLayouts.values()){
            viewLayout.add(WidgetItem);
        }
    }

    public void remove(OpenSearchWidgetItem WidgetItem){
        for (ViewLayout viewLayout : this.viewLayouts.values()){
            viewLayout.remove(WidgetItem);
        }
    }

    public void onGroupSelectChange(GWTJahiaOpenSearchEngineGroup searchGroup,
                                    GWTJahiaOpenSearchEngine currentSelectedSearchEngine) {
        if (!searchGroup.getName().equals(this.currentGroupName)){
            this.currentGroupName = searchGroup.getName();
            openSearchWidgetItems = new ArrayList<OpenSearchWidgetItem>();
            for (GWTJahiaOpenSearchEngine searchEngine : searchGroup.getSearchEngines()){
                OpenSearchWidgetItem WidgetItem = new OpenSearchWidgetItem(searchEngine);
                WidgetItem.setRssFeedMode(this.openSearchView.isRssSearchMode());
                openSearchWidgetItems.add(WidgetItem);
            }
            initWidget();
        }
    }

    public void onViewTypeChange(String viewType) {
        if (!this.viewType.equals(viewType)){
            this.viewType = viewType;
            ViewLayout viewLayout = this.viewLayouts.get(viewType);
            if (viewLayout != null){
                this.activeLayout = viewLayout;
                this.containerPanel.removeAll();
                this.containerPanel.add(this.activeLayout.getWidget());
                this.containerPanel.layout();
            }
        }
    }

    public void onSearchEngineSelected(GWTJahiaOpenSearchEngine searchEngine) {
        for (ViewLayout viewLayout : this.viewLayouts.values()){
            viewLayout.onSearchEngineSelected(searchEngine);
        }
    }

    public void onSearchEngineChecked(GWTJahiaOpenSearchEngine searchEngine) {
        for (ViewLayout viewLayout : this.viewLayouts.values()){
            viewLayout.onSearchEngineChecked(searchEngine);
        }
    }

    private void initWidget(){

        if (containerPanel == null){
            containerPanel = new LayoutContainer();
            containerPanel.setLayout(new FitLayout());
        }

        if (viewLayouts == null){
            viewLayouts = new HashMap<String,ViewLayout>();
            //viewLayouts.put(TAB_VIEW,new TabViewLayout());
            viewLayouts.put(HORIZONTAL_VIEW,new ColumnViewLayout());
        }
        activeLayout = viewLayouts.get(this.viewType);

        for(ViewLayout viewLayout : viewLayouts.values()){
            viewLayout.initWidget(this.openSearchWidgetItems);
        }
        if (activeLayout != null){
            this.containerPanel.removeAll();
            this.containerPanel.add(activeLayout.getWidget());
        }
        this.containerPanel.layout();
    }

    private interface ViewLayout {

        public abstract Widget getWidget();

        public abstract void initWidget(List<OpenSearchWidgetItem> WidgetItems);

        public abstract void add(OpenSearchWidgetItem WidgetItem);

        public abstract void remove(OpenSearchWidgetItem WidgetItem);
        
        public abstract void onSearchEngineChecked(GWTJahiaOpenSearchEngine searchEngine);

        public abstract void onSearchEngineSelected(GWTJahiaOpenSearchEngine searchEngine);

        public abstract void search(String searchTerm);
    }

    private class TabViewLayout extends TabPanel implements ViewLayout {

        private List<SearchWidgetTabItem> items;
        private HashMap<String,SearchWidgetTabItem> itemsMap;

        public TabViewLayout() {
            super();
            items = new ArrayList<SearchWidgetTabItem>();
            itemsMap = new HashMap<String,SearchWidgetTabItem>();
        }

        public Widget getWidget() {
            return this;
        }

        public void initWidget(List<OpenSearchWidgetItem> openSearchWidgetItems){

            items.clear();
            itemsMap.clear();
            removeAll();

            synchronized(openSearchWidgetItems){
                Iterator<OpenSearchWidgetItem> iterator = openSearchWidgetItems.iterator();
                OpenSearchWidgetItem osWidgetItem = null;
                SearchWidgetTabItem selectedTabItem = null;

                while (iterator.hasNext()) {
                    osWidgetItem = iterator.next();
                    SearchWidgetTabItem tabItem = new SearchWidgetTabItem(osWidgetItem);
                    if (selectedTabItem == null){
                        selectedTabItem = tabItem;
                    } else if (!selectedTabItem.getOpenSearchWidget().getSearchEngine().isEnabled(currentGroupName)
                            && osWidgetItem.getSearchEngine().isEnabled(currentGroupName)){
                        selectedTabItem = tabItem;
                    }
                    if (itemsMap.get(osWidgetItem.getName())==null){
                        if (osWidgetItem.getSearchEngine().isEnabled(currentGroupName)){
                            add(tabItem);
                        }
                        items.add(tabItem);
                        tabItem.setPos(items.size()-1);
                        itemsMap.put(osWidgetItem.getName(),tabItem);
                    }
                }
                if (selectedTabItem != null && !selectedTabItem.getOpenSearchWidget().getSearchEngine()
                        .isEnabled(currentGroupName)){
                    selectedTabItem.getOpenSearchWidget().getSearchEngine().setEnabled(currentGroupName,true);
                    setSelection(selectedTabItem);
                }
            }
        }

        public void add(OpenSearchWidgetItem WidgetItem) {
            if (itemsMap.get(WidgetItem.getName())==null){
                SearchWidgetTabItem newItem = new SearchWidgetTabItem(WidgetItem);
                itemsMap.put(WidgetItem.getName(),newItem);
                int tabCount = getItemCount();
                SearchWidgetTabItem item = null;
                int insertPos = 0;
                for (int i=0;i<tabCount; i++){
                    item = (SearchWidgetTabItem) getWidget(i);
                    if (item.getOpenSearchWidget().getIndex()>insertPos){
                        break;
                    }
                }
                insert(newItem,insertPos);
            }
        }

        public void remove(OpenSearchWidgetItem WidgetItem) {
            SearchWidgetTabItem tabItem = this.itemsMap.get(WidgetItem.getName());
            if (tabItem != null){
                this.remove(tabItem);
                this.items.remove(tabItem);
                this.itemsMap.remove(WidgetItem.getName());
            }
        }

        public void onSearchEngineSelected(GWTJahiaOpenSearchEngine searchEngine) {
            SearchWidgetTabItem item = this.itemsMap.get(searchEngine.getName());
            if (item != null){
                setSelection(item);
            }
        }

        public void onSearchEngineChecked(GWTJahiaOpenSearchEngine searchEngine) {
            SearchWidgetTabItem tabItem = this.itemsMap.get(searchEngine.getName());
            if (tabItem != null){
                if (!searchEngine.isEnabled(currentGroupName)){
                    remove(tabItem);
                } else {
                    List<TabItem> tabItems = getItems();
                    if (tabItems.isEmpty()){
                        add(tabItem);
                    } else {
                        int pos = 0;
                        boolean alreadyAddedInTabPanel = false;
                        for (TabItem item : tabItems){
                            SearchWidgetTabItem swTabItem = (SearchWidgetTabItem)item;
                            if (swTabItem.getPos() == tabItem.getPos()){
                                alreadyAddedInTabPanel = true;
                                break;
                            } else if (swTabItem.getPos() > tabItem.getPos()){
                                insert(tabItem,pos);
                                alreadyAddedInTabPanel = true;
                                break;
                            }
                            pos++;
                        }
                        if (!alreadyAddedInTabPanel){
                            add(tabItem);
                        }
                    }
                    setSelection(tabItem);
                }
            }
            selectAtleastOneTab();
        }

        public void search(String searchTerm){
            Iterator<TabItem> tabItemsIterator = this.iterator();
            SearchWidgetTabItem swTabItem = null;
            while (tabItemsIterator.hasNext()){
                swTabItem = (SearchWidgetTabItem)tabItemsIterator.next();
                swTabItem.getOpenSearchWidget().search(searchTerm);
            }
        }

        private void selectAtleastOneTab(){
            if (getSelectedItem()==null && getItemCount()>0){
                SearchWidgetTabItem tabItem = (SearchWidgetTabItem) getItem(0);
                setSelection(tabItem);
            }
        }

    }

    private class ColumnViewLayout extends LayoutContainer implements ViewLayout {

        private LayoutContainer rootWidget;
        private List<SearchWidgetColumnItem> items;
        private HashMap<String,SearchWidgetColumnItem> itemsMap;

        public ColumnViewLayout() {
            super();
            setLayout(new BorderLayout());
            items = new ArrayList<SearchWidgetColumnItem>();
            itemsMap = new HashMap<String,SearchWidgetColumnItem>();
            rootWidget = this;
        }

        public Widget getWidget() {
            if (rootWidget.getItemCount()==0){
                ContentPanel contentPanel = new ContentPanel();
                contentPanel.setHeaderVisible(false);
                rootWidget = contentPanel;
            }
            return rootWidget;
        }

        public void initWidget(List<OpenSearchWidgetItem> WidgetItems){

            items.clear();
            itemsMap.clear();
            this.rootWidget = this;
            this.rootWidget.removeAll();
            synchronized(WidgetItems){
                List<OpenSearchWidgetItem> osWidgetWidgetItems = WidgetItems;
                Iterator<OpenSearchWidgetItem> iterator = osWidgetWidgetItems.iterator();
                OpenSearchWidgetItem osWidgetWidgetItem = null;
                SearchWidgetColumnItem selectedItem = null;

                float nbVisibleItem = 0;
                float widthPercent = 0;
                for (OpenSearchWidgetItem item : osWidgetWidgetItems){
                    if (item.getSearchEngine().isEnabled(currentGroupName)){
                        nbVisibleItem++;
                    }
                }
                if (nbVisibleItem >2){
                    widthPercent = 100 / (nbVisibleItem * 100);
                }

                while (iterator.hasNext()) {
                    osWidgetWidgetItem = iterator.next();
                    SearchWidgetColumnItem item = new SearchWidgetColumnItem(Style.LayoutRegion.EAST, osWidgetWidgetItem);
                    item.setSplit(true);
                    item.setFloatable(false);
                    item.setMargins(new Margins(0));
                    item.setMinSize(50);
                    if (widthPercent != 0){
                        item.setSize(widthPercent);
                    }
                    if (selectedItem == null){
                        selectedItem = item;
                    } else if (!selectedItem.getOpenSearchWidget().getSearchEngine().isEnabled(currentGroupName)
                            && osWidgetWidgetItem.getSearchEngine().isEnabled(currentGroupName)){
                        selectedItem = item;
                    }

                    if (itemsMap.get(osWidgetWidgetItem.getName())==null){
                        if (osWidgetWidgetItem.getSearchEngine().isEnabled(currentGroupName)){
                            if (this.rootWidget.getItemCount()==0){
                                item.setRegion(Style.LayoutRegion.CENTER);
                                this.rootWidget.add(item.getContentPanel(),item);
                            } else if (this.rootWidget.getItemCount()<2){
                                this.rootWidget.add(item.getContentPanel(),item);
                            } else {
                                LayoutContainer layoutContainer = new LayoutContainer(new BorderLayout());
                                BorderLayoutData centerData = new BorderLayoutData(Style.LayoutRegion.CENTER);
                                centerData.setFloatable(false);
                                centerData.setSplit(true);
                                centerData.setMinSize(50);
                                centerData.setSize(1 - widthPercent);
                                layoutContainer.add(this.rootWidget,centerData);
                                layoutContainer.add(item.getContentPanel(),item);
                                this.rootWidget = layoutContainer;
                            }
                        }
                        items.add(item);
                        item.setPos(items.size()-1);
                        itemsMap.put(osWidgetWidgetItem.getName(),item);
                    }
                }
                /*
                if (selectedItem != null && !selectedItem.getOpenSearchWidget().getSearchEngine().isEnabled()){
                    selectedItem.getOpenSearchWidget().getSearchEngine().setEnabled(true);
                    // @todo : change item selection style
                }*/
            }
        }

        public void add(OpenSearchWidgetItem WidgetItem) {
            int insertPos = 0;
            for (OpenSearchWidgetItem osw : openSearchWidgetItems){
                if (osw.getIndex()> WidgetItem.getIndex()){
                    openSearchWidgetItems.add(insertPos, WidgetItem);
                    insertPos = -1;
                    break;
                }
                insertPos++;
            }
            if (insertPos != -1){
                openSearchWidgetItems.add(WidgetItem);
            }
            this.initWidget(openSearchWidgetItems);
        }

        public void remove(OpenSearchWidgetItem WidgetItem) {
            List<OpenSearchWidgetItem> osList = new ArrayList<OpenSearchWidgetItem>();
            osList.addAll(openSearchWidgetItems);
            osList.remove(WidgetItem);
            this.initWidget(osList);
            this.layout();
        }

        public void onSearchEngineSelected(GWTJahiaOpenSearchEngine searchEngine) {
            SearchWidgetColumnItem item = this.itemsMap.get(searchEngine.getName());
            if (item != null){
                // @todo: change selection style
            }
        }

        public void onSearchEngineChecked(GWTJahiaOpenSearchEngine searchEngine) {
            SearchWidgetColumnItem columnItem = this.itemsMap.get(searchEngine.getName());
            if (columnItem != null){
                this.initWidget(openSearchWidgetItems);
                if (activeLayout == this){
                    containerPanel.removeAll();
                    containerPanel.add(getWidget());
                    containerPanel.layout();
                }
            }
        }

        public void search(String searchTerm){
            for (OpenSearchWidgetItem osw : openSearchWidgetItems){
                if (osw.getSearchEngine().isEnabled(currentGroupName)){
                    osw.search(searchTerm);
                }
            }
        }
    }

    private class SearchWidgetTabItem extends TabItem {

        private OpenSearchWidgetItem openSearchWidgetItem;
        private int pos;

        public SearchWidgetTabItem(OpenSearchWidgetItem openSearchWidgetItem) {
            super(openSearchWidgetItem.getTitle());
            this.openSearchWidgetItem = openSearchWidgetItem;
            this.add(this.openSearchWidgetItem);
        }

        public OpenSearchWidgetItem getOpenSearchWidget() {
            return openSearchWidgetItem;
        }

        public int getPos() {
            return pos;
        }

        public void setPos(int pos) {
            this.pos = pos;
        }
    }

    private class SearchWidgetColumnItem extends BorderLayoutData {

        private OpenSearchWidgetItem WidgetItem;
        private ContentPanel contentPanel;
        private int pos;

        private SearchWidgetColumnItem(Style.LayoutRegion region, OpenSearchWidgetItem WidgetItem) {
            super(region);
            this.WidgetItem = WidgetItem;
            contentPanel = new ContentPanel();
            contentPanel.setHeading(WidgetItem.getTitle());
            contentPanel.setScrollMode(Style.Scroll.NONE);
            contentPanel.add(this.WidgetItem);
        }

        public OpenSearchWidgetItem getOpenSearchWidget() {
            return WidgetItem;
        }

        public int getPos() {
            return pos;
        }

        public void setPos(int pos) {
            this.pos = pos;
        }

        public ContentPanel getContentPanel() {
            return contentPanel;
        }
    }

}