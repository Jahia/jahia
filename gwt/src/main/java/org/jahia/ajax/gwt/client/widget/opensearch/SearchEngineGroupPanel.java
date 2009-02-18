/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.opensearch;

import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.Event;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngine;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngineGroup;
import org.jahia.ajax.gwt.client.widget.opensearch.JahiaOpenSearchTriPanel;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 9 oct. 2008
 * Time: 17:28:43
 * To change this template use File | Settings | File Templates.
 */
public class SearchEngineGroupPanel extends ContentPanel {

    private JahiaOpenSearchTriPanel openSearchTriPanel; 
    private GWTJahiaOpenSearchEngineGroup searchEngineGroup;
    private DataList searchEngines = new DataList();
    private int index;

    public SearchEngineGroupPanel(GWTJahiaOpenSearchEngineGroup searchEngineGroup, JahiaOpenSearchTriPanel openSearchTriPanel) {
        super(new FitLayout());
        this.searchEngineGroup = searchEngineGroup;
        this.openSearchTriPanel = openSearchTriPanel;
        this.setHeading(this.searchEngineGroup.getName());
        init();
    }

    public SearchEngineGroupPanel(Layout layout, GWTJahiaOpenSearchEngineGroup searchEngineGroup,
                                  JahiaOpenSearchTriPanel openSearchTriPanel) {
        super(layout);
        this.searchEngineGroup = searchEngineGroup;
        this.openSearchTriPanel = openSearchTriPanel;
        this.setHeading(this.searchEngineGroup.getName());
        init();
    }

    public DataList getSearchEngines() {
        return searchEngines;
    }

    public GWTJahiaOpenSearchEngine getSelectedSearchEngine() {
        if (searchEngines != null){
            SearchEngineListItem listItem = (SearchEngineListItem)searchEngines.getSelectedItem();
            if (listItem != null){
                return listItem.getSearchEngine();
            }
        }
        return null;
    }

    public SearchEngineListItem addSearchEngine(GWTJahiaOpenSearchEngine searchEngine){
        if (searchEngine == null){
            return null;
        }
        SearchEngineListItem listItem = new SearchEngineListItem(searchEngine,this);
        listItem.setEnabled(true);
        if (!this.openSearchTriPanel.isRssSearchMode() ||
                searchEngine.supportRssSearchMode()){
            this.searchEngines.add(listItem);
        }
        return listItem;
    }

    public GWTJahiaOpenSearchEngineGroup getSearchEngineGroup() {
        return searchEngineGroup;
    }

    public void onSearchEngineSelected(SearchEngineListItem se){
        this.openSearchTriPanel.onSearchEngineSelected(se.getSearchEngine());
    }

    public void onSearchEngineChecked(SearchEngineListItem se){
        this.openSearchTriPanel.onSearchEngineChecked(se.getSearchEngine());
    }

    /**
     * the index order of this search group related to the other search groups.
     * @return
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set the index order of this search group related to the other search groups.
     * @return
     */
    public void setIndex(int index) {
        this.index = index;
    }

    private void init(){
        this.searchEngines = new DataList() {
            @Override
            public void onSelectChange(DataListItem dataListItem, boolean b) {
                super.onSelectChange(dataListItem, b);
                if (b){
                    onSearchEngineSelected((SearchEngineListItem)dataListItem);
                }
            }
        };
        this.searchEngines.setCheckable(true);
        this.searchEngines.setSelectionMode(Style.SelectionMode.MULTI);
        List<GWTJahiaOpenSearchEngine> gwtSearchEngines = this.searchEngineGroup.getSearchEngines();
        boolean hasEnabledSearchEngine = false;
        for (GWTJahiaOpenSearchEngine searchEngine : gwtSearchEngines){
            this.addSearchEngine(searchEngine);
            hasEnabledSearchEngine = hasEnabledSearchEngine || searchEngine.isEnabled(this.searchEngineGroup.getName());
        }
        if (!hasEnabledSearchEngine && this.searchEngines.getItemCount()>0){
            SearchEngineListItem listItem = (SearchEngineListItem)this.searchEngines.getItem(0);
            listItem.setCheckedSkipGroupUpdate(true);
        }
        this.add(this.searchEngines);

        this.searchEngines.addListener(Event.ONDBLCLICK, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                SearchEngineListItem listItem = (SearchEngineListItem)searchEngines.getSelectedItem();
                if (listItem != null){
                    listItem.setChecked(listItem.isChecked());
                    onSearchEngineChecked(listItem);
                }
            }
        });
    }
}
