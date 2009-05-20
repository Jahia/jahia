/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.opensearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jahia.ajax.gwt.client.service.opensearch.GWTOpenSearchService;
import org.jahia.ajax.gwt.client.service.opensearch.GWTOpenSearchServiceAsync;
import org.jahia.ajax.gwt.client.widget.opensearch.JahiaOpenSearchTriPanel;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngine;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngineGroup;
import org.jahia.ajax.gwt.client.widget.tripanel.LeftComponent;

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

    public GWTJahiaOpenSearchEngine getCurrentSelectedSearchEngine() {
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
                List<GWTJahiaOpenSearchEngineGroup>groups = (List<GWTJahiaOpenSearchEngineGroup>)result;
                if (groups != null){
                    GWTJahiaOpenSearchEngineGroup group = null;
                    Iterator<GWTJahiaOpenSearchEngineGroup> iterator = groups.iterator();
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
