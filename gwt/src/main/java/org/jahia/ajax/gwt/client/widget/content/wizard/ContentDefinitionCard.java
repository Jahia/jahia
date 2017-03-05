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
package org.jahia.ajax.gwt.client.widget.content.wizard;

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.content.wizard.AddContentWizardWindow.ContentWizardCard;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * Wizard card responsible for displaying a list of available content
 * definitions.
 * 
 * @author Sergiy Shyrkov
 */
public class ContentDefinitionCard extends ContentWizardCard {

    private String baseType;

    private Grid<GWTJahiaNodeType> grid;

    private GWTJahiaNode parentNode;

    /**
     * Initializes an instance of this class.
     */
    public ContentDefinitionCard() {
        this(null);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param parentNode
     *            the parent node, where the wizard was called
     */
    public ContentDefinitionCard(GWTJahiaNode parentNode) {
        this(null, parentNode);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param baseType
     *            the base node type to use for displaying sub-types
     * @param parentNode
     *            the parent node, where the wizard was called
     */
    public ContentDefinitionCard(String baseType, GWTJahiaNode parentNode) {
        super(Messages.get("org.jahia.engines.contentmanager.addContentWizard.defsCard.title",
                "Content definitions"), Messages.get(
                "org.jahia.engines.contentmanager.addContentWizard.defsCard.text",
                "Select a definition to create your content:"));
        this.baseType = baseType;
        this.parentNode = parentNode;
        setLayout(new FitLayout());
        createUI();
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.ajax.gwt.client.widget.wizard.WizardCard#createUI()
     */
    @Override
    public void createUI() {
        removeAll();
        final ListStore<GWTJahiaNodeType> store = new ListStore<GWTJahiaNodeType>();
        /*JahiaContentDefinitionService.App.getInstance().getNodeSubtypes(
                baseType, parentNode,
                new BaseAsyncCallback<List<GWTJahiaNodeType>>() {
                    public void onFailure(Throwable caught) {
                        MessageBox.alert(Messages.get("label.error", "Error"),"Unable to load content definitions for base type '"
                                        + baseType
                                        + "' and parent node '"
                                        + parentNode.getPath()
                                        + "'. Cause: "
                                        + caught.getLocalizedMessage(),null);
                    }

                    public void onSuccess(List<GWTJahiaNodeType> result) {
                        store.add(result);
                    }
                });*/

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("name", Messages.get(
                "label.user", "Name"), 180));
        columns.add(new ColumnConfig("label", Messages.get(
                "org.jahia.engines.contentmanager.addContentWizard.column.label", "Label"), 400));

        ColumnModel cm = new ColumnModel(columns);
        grid = new Grid<GWTJahiaNodeType>(store, cm);
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
        setUiCreated(true);
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.ajax.gwt.client.widget.wizard.WizardCard#next()
     */
    @Override
    public void next() {
        GWTJahiaNodeType selectedType = grid.getSelectionModel()
                .getSelectedItem();
        if (selectedType != null) {
            if (!selectedType.equals(getWizardData().getNodeType())) {
                getWizardWindow().resetCards(0);
            }
        } else {
            if (getWizardData().getNodeType() != null) {
                getWizardWindow().resetCards(0);
            }
        }

        getWizardData().setNodeType(selectedType);
    }

}
