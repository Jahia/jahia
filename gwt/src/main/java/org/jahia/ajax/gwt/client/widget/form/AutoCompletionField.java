/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.google.gwt.user.client.ui.KeyboardListener;
import org.jahia.ajax.gwt.client.widget.menu.AutoCompletionMenu;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 23 juil. 2008
 * Time: 11:23:03
 * To change this template use File | Settings | File Templates.
 */
public class AutoCompletionField extends TriggerField<String> {

    private AutoCompletionMenu menu;
    private CompletionItems completionItems;
    private GWTJahiaPageContext jahiaPage;
    public AutoCompletionField() {
        super();
        this.enableEvents(true);

        addListener(Events.KeyUp, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if ( ce.getKeyCode() == KeyboardListener.KEY_DOWN ){
                    if (menu.isVisible()){
                        menu.focus();
                    }
                } else {
                    String value = getValue();
                    if (value != null && value.length() >= 2) {
                        updateChoices(value);
                    }
                }
            }
        });
        menu = new AutoCompletionMenu();

        menu.getChoices().addListener(Events.SelectionChange, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                focusValue = getValue();
                DataList l = (DataList) ce.getComponent();
                DataListItem data = l.getSelectedItem();
                if (data != null){
                    setValue(data.getText());
                    fireChangeEvent(focusValue, getValue());
                    menu.hide();
                }
        }});

        jahiaPage = new GWTJahiaPageContext();
        jahiaPage.setPid(JahiaGWTParameters.getPID());
        jahiaPage.setMode(JahiaGWTParameters.getOperationMode());

    }

    public AutoCompletionField(CompletionItems completionItems) {
        this();
        this.completionItems = completionItems;
    }

    public void updateChoices(String match) {
        if (completionItems == null) {
            return;
        }
        final DataList choices = menu.getChoices();
        choices.removeAll();
        final ListStore<GWTJahiaValueDisplayBean> datas = completionItems.getCompletionItems(jahiaPage,match+"*");

        if (datas != null && datas.getLoader() != null) {
            datas.getLoader().addLoadListener(new LoadListener(){
                public void loaderLoad(LoadEvent event) {

                }
            });
            datas.addStoreListener(new StoreListener<GWTJahiaValueDisplayBean>(){
                public void storeDataChanged(StoreEvent event){
                    Iterator<GWTJahiaValueDisplayBean> it = datas.getModels().iterator();
                    GWTJahiaValueDisplayBean data;
                    while (it.hasNext()){
                        data = it.next();
                        choices.add((String)data.get(completionItems.getValueKey()));
                    }
                    if (choices.iterator().hasNext()){
                        // Todo fix this for migration 2.0.2
                        //menu.show(wrap.dom, "tl-bl?");
                        focus();
                    } else {
                        menu.hide();
                    }
                }
            });
            datas.getLoader().load();
        } else {
            menu.hide();
        }
    }
}
