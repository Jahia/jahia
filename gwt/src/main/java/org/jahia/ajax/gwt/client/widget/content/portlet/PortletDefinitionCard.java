/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.content.portlet;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaPortletDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ktlili
 * Date: 25 nov. 2008
 * Time: 10:25:59
 */
public class PortletDefinitionCard extends PortletWizardCard {
    private Grid<GWTJahiaPortletDefinition> grid;

    public PortletDefinitionCard() {
        super(Messages.get("org.jahia.engines.PortletsManager.wizard.portletdef.label", "Portlet"), Messages.get("org.jahia.engines.PortletsManager.wizard.portletdef.label", "Select a portlet definition"));
        createUI();
    }

    public void createUI() {
        removeAll();
        final ListStore<GWTJahiaPortletDefinition> store = new ListStore<GWTJahiaPortletDefinition>();
        JahiaContentManagementService.App.getInstance().searchPortlets(null, new BaseAsyncCallback<List<GWTJahiaPortletDefinition>>() {
            public void onSuccess(List<GWTJahiaPortletDefinition> result) {
                store.add(result);
            }
        });


        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("displayName", Messages.get("label.portletName", "Name"), 180));
        columns.add(new ColumnConfig("description", Messages.get("label.portletDescription", "Description"), 400));

        ColumnModel cm = new ColumnModel(columns);
        grid = new Grid<GWTJahiaPortletDefinition>(store, cm);
        grid.setBorders(true);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent be) {
                getWizardWindow().doNext();
            }
        });

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

    protected void onButtonPressed(Button button) {

    }

    public void next() {
        final GWTJahiaPortletDefinition selectedJahiaPortletDefinition = getSelectedPortletDefinition();
        // selection has change --> reset all
        if (selectedJahiaPortletDefinition != null) {
            if (!selectedJahiaPortletDefinition.equals(getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition())) {
                getPortletWizardWindow().resetCards(0);
            }
        } else {
            if (getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition() != null) {
                getPortletWizardWindow().resetCards(0);
            }
        }

        getGwtJahiaNewPortletInstance().setGwtJahiaPortletDefinition(selectedJahiaPortletDefinition);
    }

    public GWTJahiaPortletDefinition getSelectedPortletDefinition() {
        GWTJahiaPortletDefinition selectedJahiaPortletDefinition = grid.getSelectionModel().getSelectedItem();
        return selectedJahiaPortletDefinition;
    }

}