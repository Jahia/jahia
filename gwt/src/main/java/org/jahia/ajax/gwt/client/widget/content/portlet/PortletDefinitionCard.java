/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
