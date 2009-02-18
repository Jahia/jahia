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
