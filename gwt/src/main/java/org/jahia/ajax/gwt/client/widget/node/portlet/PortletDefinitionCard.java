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
package org.jahia.ajax.gwt.client.widget.node.portlet;

import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaPortletDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
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
        super(Messages.getNotEmptyResource("mw_mashups","Mashup"));
        createUI();
        setHtmlText(Messages.getNotEmptyResource("mw_select_portlet_def","Select a portlet definition"));
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
        columns.add(new ColumnConfig("displayName", Messages.getNotEmptyResource("mw_name","Name"), 180));
        columns.add(new ColumnConfig("description", Messages.getNotEmptyResource("mw_description","Description"), 400));

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
