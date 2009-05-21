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
