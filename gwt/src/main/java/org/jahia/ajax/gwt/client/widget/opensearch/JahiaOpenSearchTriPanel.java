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

import com.google.gwt.user.client.ui.*;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import org.jahia.ajax.gwt.client.core.JahiaModule;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngineGroup;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngine;
import org.jahia.ajax.gwt.client.widget.tripanel.TriPanelBrowserLayout;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomBar;

public class JahiaOpenSearchTriPanel extends TriPanelBrowserLayout {

    private JahiaModule jahiaModule;
    private RootPanel rootPanel;

    private String viewType = OpenSearchLayoutView.HORIZONTAL_VIEW;
    private boolean rssSearchMode;

    private OpenSearchLayoutView layoutView;
    private OpenSearchLeftPanel leftPanel;
    private OpenSearchToolBar toolBar;
    
    public JahiaOpenSearchTriPanel(RootPanel rootPanel, JahiaModule jahiaModule) {
        super();
        this.rootPanel = rootPanel;
        this.jahiaModule = jahiaModule;
        this.rssSearchMode = true;
        setWidth("100%");
        setHeight("500px");
        init();
    }

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        boolean viewTypeChanged = (this.viewType != null && !this.viewType.equals(viewType));
        this.viewType = viewType;
        if (viewTypeChanged){
            this.layoutView.onViewTypeChange(this.viewType);
        }
    }

    public boolean isRssSearchMode() {
        return rssSearchMode;
    }


    public void setRssSearchMode(boolean rssSearchMode) {
        boolean switchRssSearchMode = this.rssSearchMode != rssSearchMode;
        this.rssSearchMode = rssSearchMode;
        if (switchRssSearchMode){
            this.linker.refreshAll();
        }
    }

    /**
     * This is the entry point method.
     */
    public void init() {
        buildGUI();
    }

    private void buildGUI(){

        // construction of the UI components
        leftPanel = new OpenSearchLeftPanel(this);

        layoutView = new OpenSearchDefaultLayoutView(this);

        toolBar = new OpenSearchToolBar(this);

        BottomBar statusBar = null;

        // setup widgets in layout
        initWidgets(leftPanel.getComponent(),
                    layoutView.getComponent(),
                    null,
                    toolBar.getComponent(),
                    null);

        // linker initializations
        linker.registerComponents(leftPanel, layoutView, null, toolBar, statusBar) ;

        layoutView.initContextMenu();

        linker.handleNewSelection();

    }

    public void search(String searchString){
        if ( searchString != null && !"".equals(searchString.trim()) ) {
            layoutView.search(searchString);
        }
    }

    public void onOpenSearchEngineStandAloneWindowClick(ComponentEvent event){
        GWTJahiaOpenSearchEngine openSearchEngine = this.leftPanel.getCurrentSelectedSearchEngine();
        if (openSearchEngine != null){
            String searchTerms = toolBar.getSearchTerms();
            OpenSearchPanel openSearchPanel = new OpenSearchPanel(openSearchEngine,searchTerms,this.rssSearchMode);
            StandAloneOpenSearchWindow w = new StandAloneOpenSearchWindow(openSearchPanel);
            w.show();
        } else {
            MessageBox.alert("alert","no search engine selected",null);
        }
    }

    public void onGroupSelectChange(GWTJahiaOpenSearchEngineGroup searchGroup){
        GWTJahiaOpenSearchEngine currentSelectedSearchEngine = this.leftPanel.getCurrentSelectedSearchEngine();
        this.layoutView.onGroupSelectChange(searchGroup, currentSelectedSearchEngine);
        this.toolBar.onGroupSelectChange(searchGroup, currentSelectedSearchEngine);
    }

    public void onSearchEngineSelected(GWTJahiaOpenSearchEngine searchEngine) {
        this.layoutView.onSearchEngineSelected(searchEngine);
        this.toolBar.onSearchEngineSelected(searchEngine);
    }

    public void onSearchEngineChecked(GWTJahiaOpenSearchEngine searchEngine) {
        this.layoutView.onSearchEngineChecked(searchEngine);
        this.toolBar.onSearchEngineChecked(searchEngine);
    }

    public OpenSearchLayoutView getLayoutView() {
        return layoutView;
    }

    public void setLayoutView(OpenSearchLayoutView layoutView) {
        this.layoutView = layoutView;
    }

    /**
     *
     * @param key
     * @return
     */
    public String getResource(String key){
       return jahiaModule.getResource(key);
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getResource(String key, String defaultValue){
       return jahiaModule.getResource(key,defaultValue);
    }


}