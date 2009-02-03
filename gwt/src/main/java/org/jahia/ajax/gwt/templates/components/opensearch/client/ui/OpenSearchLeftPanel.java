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

package org.jahia.ajax.gwt.templates.components.opensearch.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jahia.ajax.gwt.templates.components.opensearch.client.GWTOpenSearchService;
import org.jahia.ajax.gwt.templates.components.opensearch.client.GWTOpenSearchServiceAsync;
import org.jahia.ajax.gwt.templates.components.opensearch.client.JahiaOpenSearchTriPanel;
import org.jahia.ajax.gwt.templates.components.opensearch.client.model.GWTOpenSearchEngine;
import org.jahia.ajax.gwt.templates.components.opensearch.client.model.GWTOpenSearchEngineGroup;
import org.jahia.ajax.gwt.tripanelbrowser.client.components.LeftComponent;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 25 sept. 2008
 * Time: 10:43:55
 * To change this template use File | Settings | File Templates.
 */
public class OpenSearchLeftPanel extends LeftComponent {

    private JahiaOpenSearchTriPanel openSearchTriPanel;
    private ContentPanel component;
    private HashMap<String,SearchEngineGroupPanel> searchGroupsMap;
    private List<SearchEngineGroupPanel> searchGroupPanels;
    private SearchEngineGroupPanel currentSelectedGroup;

    public OpenSearchLeftPanel(JahiaOpenSearchTriPanel openSearchTriPanel) {
        this.openSearchTriPanel = openSearchTriPanel;
        buildGUI();
    }

    public void openAndSelectItem(Object item) {
    }

    public void refresh() {
        this.buildGUI();
    }

    public Object getSelectedItem() {
        if (currentSelectedGroup != null){
            return currentSelectedGroup.getSearchEngineGroup();
        }
        return null;
    }

    public GWTOpenSearchEngine getCurrentSelectedSearchEngine() {
        if (currentSelectedGroup != null){
            return currentSelectedGroup.getSelectedSearchEngine();
        }
        return null;
    }

    public Component getComponent() {
        return component;
    }

    private void buildGUI(){
        if (component != null){
            component.removeAll();
        }
        if (searchGroupsMap == null){
            searchGroupsMap = new HashMap<String,SearchEngineGroupPanel>();
        } else {
            searchGroupsMap.clear();
        }
        if (searchGroupPanels == null){
            searchGroupPanels = new ArrayList<SearchEngineGroupPanel>();
        } else {
            searchGroupsMap.clear();
        }
        AccordionLayout layout = new AccordionLayout();
        component = new ContentPanel(layout);
        loadSearchGroups();
    }

    private void loadSearchGroups (){
        final GWTOpenSearchServiceAsync service = GWTOpenSearchService.App.getInstance() ;

        service.getSearchEngineGroups(new AsyncCallback(){
            public void onFailure(java.lang.Throwable throwable) {
                Log.debug("Exception occured loading search engines groups", throwable);
            }

            public void onSuccess(java.lang.Object result) {
                List<GWTOpenSearchEngineGroup>groups = (List<GWTOpenSearchEngineGroup>)result;
                if (groups != null){
                    GWTOpenSearchEngineGroup group = null;
                    Iterator<GWTOpenSearchEngineGroup> iterator = groups.iterator();
                    while(iterator.hasNext()){
                        group = iterator.next();
                        SearchEngineGroupPanel sep = new SearchEngineGroupPanel(group,openSearchTriPanel){
                            @Override
                            public void expand() {
                                super.expand();
                                onSearchEngineGroupExpanded();
                            }
                            public void collapse() {
                                super.collapse();
                                if (this.getIndex()==0){
                                    // it's the first group so the last group will be the active one
                                    SearchEngineGroupPanel sep = searchGroupPanels.get(searchGroupPanels.size()-1);
                                    if (sep.getItemCount()==0){
                                        // as the last search group has no search engine,
                                        // we have to remove all search engines panel in the table view
                                        // and set the current selected group as this last one
                                        currentSelectedGroup = sep;
                                        onSearchEngineGroupExpanded();
                                    }
                                }
                            }
                        };
                        sep.setScrollMode(Style.Scroll.AUTO);
                        sep.setIndex(searchGroupPanels.size());
                        searchGroupPanels.add(sep);
                        searchGroupsMap.put(group.getName(),sep);
                        component.add(sep);
                    }
                }
                component.layout();
                onSearchEngineGroupExpanded();
            }
        });
    }

    private void onSearchEngineGroupExpanded(){
        for (SearchEngineGroupPanel searchGroupPanel : searchGroupPanels){
            if (searchGroupPanel.isExpanded()){
                this.currentSelectedGroup = searchGroupPanel;
            }
        }
        if (this.currentSelectedGroup != null){
            this.openSearchTriPanel.onGroupSelectChange(this.currentSelectedGroup.getSearchEngineGroup());
        }
    }
}
