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

package org.jahia.ajax.gwt.client.widget.node.portlet;

import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaPortletDefinition;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ktlili
 * Date: 25 nov. 2008
 * Time: 10:25:59
 */
public class PortletDefinitionCard extends MashupWizardCard {
    private Grid<GWTJahiaPortletDefinition> grid;

    public PortletDefinitionCard() {
        super("Mashup");
        createUI();
        setHtmlText("Select a portlet definition");
    }


    public void createUI() {
        removeAll();
        final ListStore<GWTJahiaPortletDefinition> store = new ListStore<GWTJahiaPortletDefinition>();
        JahiaNodeService.App.getInstance().searchPortlets(null, new AsyncCallback<List<GWTJahiaPortletDefinition>>() {
            public void onSuccess(List<GWTJahiaPortletDefinition> result) {
                store.add(result);
            }

            public void onFailure(Throwable caught) {
            }
        });


        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("name", "Name", 180));
        columns.add(new ColumnConfig("description", "Description", 400));

        /*XTemplate tpl = XTemplate.create("<p><b>Name:</b> {name}</p><br><p><b>Description:</b> {description}</p>");
        RowExpander expander = new RowExpander();
        expander.setTemplate(tpl);
        columns.add(expander);*/


        ColumnModel cm = new ColumnModel(columns);
        grid = new Grid<GWTJahiaPortletDefinition>(store, cm);
        grid.setBorders(true);

        ContentPanel panel = new ContentPanel();
        panel.setLayout(new FitLayout());
        panel.setHeaderVisible(false);
        panel.setBodyBorder(false);
        panel.setBorders(false);
        panel.setFrame(false);
        panel.setCollapsible(false);
        panel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        panel.add(grid);
        add(panel);
    }

    public void next() {
        GWTJahiaPortletDefinition selectedJahiaPortletDefinition = getSelectedPortletDefinition();
        // selection has change --> reset all
        if (selectedJahiaPortletDefinition != null) {
            if (!selectedJahiaPortletDefinition.equals(getGwtPortletInstanceWizard().getGwtJahiaPortletDefinition())) {
                getPortletWizardWindow().resetCards(0);
            }
        } else {
            if (getGwtPortletInstanceWizard().getGwtJahiaPortletDefinition() != null) {
                getPortletWizardWindow().resetCards(0);
            }
        }
        getGwtPortletInstanceWizard().setGwtJahiaPortletDefinition(selectedJahiaPortletDefinition);
    }

    public GWTJahiaPortletDefinition getSelectedPortletDefinition() {
        GWTJahiaPortletDefinition selectedJahiaPortletDefinition = grid.getSelectionModel().getSelectedItem();
        return selectedJahiaPortletDefinition;
    }

}
