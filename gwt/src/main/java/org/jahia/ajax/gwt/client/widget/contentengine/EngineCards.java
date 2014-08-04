/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: toto
 * Date: Nov 16, 2010
 * Time: 2:11:21 PM
 */
public class EngineCards extends LayoutContainer implements EngineContainer {
    private EngineContainer mainContainer;
    private LayoutContainer cardsContainer;
    private Grid<BaseModelData> list;

    private List<Component> components = new ArrayList<Component>();
    private List<String> headers = new ArrayList<String>();
    private List<ButtonBar> bars = new ArrayList<ButtonBar>();
    private List<List<Component>> barItems = new ArrayList<List<Component>>();
    private List<Button> globalButtons = new ArrayList<Button>();
    private ButtonBar bar;
    private int i = 0;

    public EngineCards(EngineContainer mainContainer, Linker linker) {
        super(new BorderLayout());
        setId("JahiaGxtEngineCards");

        final ListStore<BaseModelData> store = new ListStore<BaseModelData>();
        ColumnConfig lang = new ColumnConfig("lang", 50);
        ColumnConfig title = new ColumnConfig("header", 800);
        ColumnConfig actions = new ColumnConfig("action", 200);
        actions.setRenderer(new GridCellRenderer() {
            @Override
            public Object render(final ModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore store, Grid grid) {
                return model.get("action");
            }
        });
        ColumnModel header = new ColumnModel(Arrays.asList(lang, title, actions));

        list = new Grid<BaseModelData>(store, header);
        list.setAutoExpandColumn("header");
        list.setHideHeaders(true);
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

        this.mainContainer = mainContainer;
        this.bar = new ButtonBar();
        this.bar.setAlignment(Style.HorizontalAlignment.CENTER);
        mainContainer.setEngine(this, "", bar, null, linker);
    }

    public ContentPanel getPanel() {
        return mainContainer.getPanel();
    }

    public void setEngine(Component component, String header, ButtonBar buttonsBar, GWTJahiaLanguage language, Linker linker) {
        components.add(component);
        bars.add(buttonsBar);
        headers.add(header);
        BaseModelData data = new BaseModelData();
        data.set("header", header);
        data.set("action", buttonsBar);
        if (language != null) {
            data.set("lang", "<img src=\"" + language.getImage() + "\"/>&nbsp;");
        }
        list.getStore().add(data);
        cardsContainer.add(component);
    }

    public void showEngine() {
        for (ButtonBar buttonBar : bars) {
            barItems.add(new ArrayList<Component>(buttonBar.getItems()));
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

        ((CardLayout) cardsContainer.getLayout()).setActiveItem(components.get(i));
        mainContainer.getPanel().setHeadingHtml(headers.get(i));
    }

    public void addGlobalButton(Button button) {
        if (button != null) {
            bar.add(button);
        }
    }

    public Component getCurrentComponent() {
        return components.get(i);
    }

    public List<Component> getComponents() {
        return components;
    }

    public void closeAllEngines() {
        components.clear();
        headers.clear();
        bars.clear();
        barItems.clear();
        list.getStore().removeAll();

        mainContainer.closeEngine();

    }
}
