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
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Engine displaying multiple sub container as multiple cards, with their own buttons/actions per card.
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
    private ButtonBar bar;
    private int i = 0;

    public EngineCards(EngineContainer mainContainer, Linker linker) {
        super(new BorderLayout());
        setId("JahiaGxtEngineCards");
        addStyleName("engine-cards");

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

    /**
     * Add a component as a card
     * @param component the component to be drawn
     * @param header header to be displayed
     * @param buttonsBar buttons bar to displayed for this card
     * @param language if not null will be displayed in the language column
     * @param linker the current linker from GWT
     */
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

    /**
     * show the first engine in the main area
     */
    public void showEngine() {
        for (ButtonBar buttonBar : bars) {
            barItems.add(new ArrayList<Component>(buttonBar.getItems()));
        }
        list.getSelectionModel().select(0, false);
        mainContainer.showEngine();
    }

    /**
     * Close the current engine
     */
    public void closeEngine() {
        components.remove(i);
        headers.remove(i);
        bars.remove(i);
        barItems.remove(i);
        list.getStore().remove(i);

        if (list.getStore().getCount() == 0) {
            closeAllEngines();
        } else {
            if (i >= list.getStore().getCount()) {
                i = list.getStore().getCount() - 1;
            }
            list.getSelectionModel().select(i, false);
        }
    }

    private void updateView() {
        String name = components.get(i).getClass().getName();
        name = name.substring(name.lastIndexOf('.')+1).toLowerCase();
        removeStyleName(name+ "-ctn");
        mainContainer.getPanel().removeStyleName(name + "-card");
        if (list.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        i = list.getStore().indexOf(list.getSelectionModel().getSelectedItem());

        name = components.get(i).getClass().getName();
        name = name.substring(name.lastIndexOf('.')+1).toLowerCase();
        addStyleName(name+ "-ctn");
        mainContainer.getPanel().addStyleName(name + "-card");

        ((CardLayout) cardsContainer.getLayout()).setActiveItem(components.get(i));
        mainContainer.getPanel().setHeadingHtml(headers.get(i));
    }

    /**
     * a a global button that will be displayed for all cards
     * @param button the button to be added to all cards
     */
    public void addGlobalButton(Button button) {
        if (button != null) {
            bar.add(button);
        }
    }

    public Component getCurrentComponent() {
        return components.get(i);
    }

    /**
     * get the List of <Component/> in this engine
     * @return the List of <Component/> in this engine
     */
    public List<Component> getComponents() {
        return components;
    }

    /**
     * Empty the engine of all its cards.
     */
    public void closeAllEngines() {
        components.clear();
        headers.clear();
        bars.clear();
        barItems.clear();
        list.getStore().removeAll();
        mainContainer.closeEngine();

    }
}
