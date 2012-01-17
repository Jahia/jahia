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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.google.gwt.dom.client.EventTarget;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * User: toto
 * Date: Nov 16, 2010
 * Time: 2:11:21 PM
 * 
 */
public class EngineCards extends LayoutContainer implements EngineContainer {
    private Linker linker;
    private EngineContainer mainContainer;
    private LayoutContainer cardsContainer;
    private Grid<BaseModelData> list;

    private List<Component> components = new ArrayList<Component>();
    private List<String> headers = new ArrayList<String>();
    private List<ButtonBar> bars = new ArrayList<ButtonBar>();
    private List<List<Component>> barItems = new ArrayList<List<Component>>();
    private ButtonBar bar;
    private int i = 0;

    public EngineCards(EngineContainer mainContainer, Linker linker) {
        super(new BorderLayout());
        setId("JahiaGxtEngineCards");

        final ListStore<BaseModelData> store = new ListStore<BaseModelData>();
        ColumnModel header = new ColumnModel(Arrays.asList(new ColumnConfig("header", Messages.get("label.workflow.multipleWorkflowsToStart","Warning : There are multiple workflow involved in this action"), 300)));

        list = new Grid<BaseModelData>(store, header);
        list.setAutoExpandColumn("header");
        list.setAutoExpandMax(1200);
        list.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<BaseModelData>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<BaseModelData> se) {
                updateView();
            }
        });

        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.NORTH, 150);
        data.setCollapsible(true);
        data.setSplit(true);
        data.setFloatable(true);

        add(list, data);

        cardsContainer = new LayoutContainer(new CardLayout());
        add(cardsContainer, new BorderLayoutData(Style.LayoutRegion.CENTER));

        this.linker = linker;
        this.mainContainer = mainContainer;
        this.bar = new ButtonBar();
        this.bar.setAlignment(Style.HorizontalAlignment.CENTER);
        mainContainer.setEngine(this, "", bar, linker);
    }

    public ContentPanel getPanel() {
        return mainContainer.getPanel();
    }

    public void setEngine(Component component, String header, ButtonBar buttonsBar, Linker linker) {
        components.add(component);
        bars.add(buttonsBar);
        headers.add(header);
        BaseModelData data = new BaseModelData();
        data.set("header", header);
        list.getStore().add(data);
        cardsContainer.add(component);
    }

    public void showEngine() {
        for (ButtonBar buttonBar : bars) {
            barItems.add(new ArrayList<Component>(buttonBar.getItems()));
        }
        if (components.size() == 1) {
            list.setVisible(false);
        }
        list.getSelectionModel().select(0, false);
        mainContainer.showEngine();
    }

    public void closeEngine() {
        components.remove(i);
        headers.remove(i);
        bars.remove(i);
        barItems.remove(i);
        list.getStore().remove(i);

        if (list.getStore().getCount() == 0) {
            mainContainer.closeEngine();
        } else {
            if (i >= list.getStore().getCount()) {
                i = list.getStore().getCount() - 1;
            }
            list.getSelectionModel().select(i, false);
        }
    }

    private void updateView() {
        if (list.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        i = list.getStore().indexOf(list.getSelectionModel().getSelectedItem());

        ((CardLayout)cardsContainer.getLayout()).setActiveItem(components.get(i));
        mainContainer.getPanel().setHeading(headers.get(i));
        bar.removeAll();
        List<Component> items = barItems.get(i);
        for (Component component : items) {
            bar.add(component);
        }
    }


}
