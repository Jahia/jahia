/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.contentengine;

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaPortletDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

/**
 * Represents a dedicated tab for viewing the list of portlet in a deployed Web
 * application.
 * 
 * @author Sergiy Shyrkov
 */
public class PortletsTabItem extends EditEngineTabItem {

    @Override
    public void init(NodeHolder engine, AsyncTabItem tab, String locale) {
        if (engine.getNode() == null || tab.isProcessed()) {
            return;
        }

        final ListStore<GWTJahiaPortletDefinition> store = new ListStore<GWTJahiaPortletDefinition>();
        JahiaContentManagementService.App.getInstance().searchPortlets(engine.getNode().getUUID(),
                new BaseAsyncCallback<List<GWTJahiaPortletDefinition>>() {
                    public void onSuccess(List<GWTJahiaPortletDefinition> result) {
                        store.add(result);
                    }
                });

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>(2);
        columns.add(new ColumnConfig("displayName", Messages.get("label.portletName", "Name"), 180));
        columns.add(new ColumnConfig("description", Messages.get("label.portletDescription", "Description"), 400));

        ColumnModel cm = new ColumnModel(columns);
        Grid<GWTJahiaPortletDefinition> grid = new Grid<GWTJahiaPortletDefinition>(store, cm);
        grid.setSize(700, 300);
        grid.setBorders(true);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        tab.add(grid);

        tab.setProcessed(true);
    }

}
