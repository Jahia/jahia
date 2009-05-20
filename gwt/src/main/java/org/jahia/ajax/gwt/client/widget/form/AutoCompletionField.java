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
package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
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
                DataList l = (DataList) ce.component;
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
            datas.addStoreListener(new StoreListener(){
                public void storeDataChanged(StoreEvent event){
                    Iterator<GWTJahiaValueDisplayBean> it = datas.getModels().iterator();
                    GWTJahiaValueDisplayBean data;
                    while (it.hasNext()){
                        data = it.next();
                        choices.add((String)data.get(completionItems.getValueKey()));
                    }
                    if (choices.iterator().hasNext()){
                        menu.show(wrap.dom, "tl-bl?");
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
