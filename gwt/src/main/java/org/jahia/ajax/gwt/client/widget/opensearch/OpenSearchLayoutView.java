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

import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.widget.opensearch.JahiaOpenSearchTriPanel;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngineGroup;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngine;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 24 sept. 2008
 * Time: 12:51:10
 * To change this template use File | Settings | File Templates.
 */
public abstract class OpenSearchLayoutView extends TopRightComponent {

    public static final String TAB_VIEW = "TAB_VIEW";
    public static final String PORTAL_VIEW = "PORTAL_VIEW";
    public static final String HORIZONTAL_VIEW = "HORIZONTAL_VIEW";

    protected JahiaOpenSearchTriPanel openSearchView;
    protected List<OpenSearchWidgetItem> openSearchWidgetItems;
    protected Object content;
    protected String viewType;

    public OpenSearchLayoutView(JahiaOpenSearchTriPanel openSearchView) {
        this.openSearchView = openSearchView;
        this.openSearchWidgetItems = new ArrayList<OpenSearchWidgetItem>();
        this.viewType = openSearchView.getViewType();
    }

    public void add(OpenSearchWidgetItem WidgetItem){
        synchronized(openSearchWidgetItems){
            openSearchWidgetItems.add(WidgetItem);
        }
    }

    public void remove(OpenSearchWidgetItem WidgetItem){
        synchronized(openSearchWidgetItems){
            openSearchWidgetItems.remove(WidgetItem);
        }
    }

    public void setContent(Object root){
        this.content = root;
    }

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    public JahiaOpenSearchTriPanel getOpenSearchView() {
        return openSearchView;
    }

    public abstract void search(String searchString);

    /**
     *
     * @param searchGroup
     * @param selectedSearchEngine
     */
    public abstract void onGroupSelectChange(GWTJahiaOpenSearchEngineGroup searchGroup,
                                             GWTJahiaOpenSearchEngine selectedSearchEngine);

    public abstract void onSearchEngineSelected(GWTJahiaOpenSearchEngine searchEngine);

    public abstract void onSearchEngineChecked(GWTJahiaOpenSearchEngine searchEngine);

    public abstract void onViewTypeChange(String viewType);

}
