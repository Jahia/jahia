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