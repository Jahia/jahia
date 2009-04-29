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

import com.extjs.gxt.ui.client.widget.DataListItem;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngine;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 9 oct. 2008
 * Time: 17:30:10
 * To change this template use File | Settings | File Templates.
 */
public class SearchEngineListItem extends DataListItem {

    private GWTJahiaOpenSearchEngine searchEngine;
    private SearchEngineGroupPanel searchEngineGroupPanel;

    public SearchEngineListItem(GWTJahiaOpenSearchEngine searchEngine,
                                SearchEngineGroupPanel searchEngineGroupPanel) {
        super();
        this.searchEngine = searchEngine;
        this.searchEngineGroupPanel = searchEngineGroupPanel;
        if (this.searchEngine.getDescriptor() != null){
            this.setText(this.searchEngine.getDescriptor().getShortName());
        } else {
            this.setText(this.searchEngine.getName());
        }
    }

    public SearchEngineListItem(String text, GWTJahiaOpenSearchEngine searchEngine) {
        super(text);
        this.searchEngine = searchEngine;
    }

    public GWTJahiaOpenSearchEngine getSearchEngine() {
        return searchEngine;
    }

    public void setSearchEngine(GWTJahiaOpenSearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        this.searchEngine.setEnabled(this.searchEngineGroupPanel.getSearchEngineGroup().getName(),checked);
        this.searchEngineGroupPanel.onSearchEngineChecked(this);
    }

    public void setCheckedSkipGroupUpdate(boolean checked) {
        super.setChecked(checked);
        this.searchEngine.setEnabled(this.searchEngineGroupPanel.getSearchEngineGroup().getName(),checked);
    }

}
