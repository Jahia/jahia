/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:19:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class LanguageSwitcherActionItem extends BaseActionItem {
    private transient ComboBox<GWTJahiaLanguage> mainComponent;
    private List<GWTJahiaLanguage> gwtJahiaLanguages;
    private GWTJahiaLanguage selectedLang;
    private String siteKey;

    public LanguageSwitcherActionItem() {

    }

    public void setLanguages(List<GWTJahiaLanguage> gwtJahiaLanguages) {
        this.gwtJahiaLanguages = gwtJahiaLanguages;
    }


    public void setSelectedLang(GWTJahiaLanguage selectedLang) {
        this.selectedLang = selectedLang;
    }

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        initMainComponent();
    }

    @Override
    public void handleNewLinkerSelection() {
        super.handleNewLinkerSelection();
        if (linker.getMainNode()!= null && !linker.getMainNode().getSiteUUID().equalsIgnoreCase(siteKey)) {
            siteKey = linker.getMainNode().getSiteUUID();
            JahiaContentManagementService.App.getInstance().getSiteLanguages(new BaseAsyncCallback<List<GWTJahiaLanguage>>() {
                public void onSuccess(List<GWTJahiaLanguage> languages) {
                    gwtJahiaLanguages = languages;
                    mainComponent.getStore().removeAll();
                    mainComponent.getStore().add(languages);
                }

                public void onApplicationFailure(Throwable throwable) {
                    mainComponent.getStore().removeAll();
                }
            });
        }

    }

    /**
     * init main component
     */
    private void initMainComponent() {
        siteKey = JahiaGWTParameters.getSiteUUID();        
        mainComponent = new ComboBox<GWTJahiaLanguage>();
        mainComponent.setStore(new ListStore<GWTJahiaLanguage>());
        mainComponent.getStore().add(gwtJahiaLanguages);
        mainComponent.setDisplayField("displayName");
        mainComponent.setTemplate(getLangSwitchingTemplate());
        mainComponent.setTypeAhead(true);
        mainComponent.setTriggerAction(ComboBox.TriggerAction.ALL);
        mainComponent.setForceSelection(true);
        mainComponent.setValue(selectedLang);
        mainComponent.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaLanguage>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaLanguage> event) {
                ((EditLinker) linker).getMainModule().switchLanguage(event.getSelectedItem().getLanguage());
            }
        });
        setEnabled(true);
    }


    @Override
    public Component getCustomItem() {
        return mainComponent;
    }


    @Override
    public void setEnabled(boolean enabled) {
        mainComponent.setEnabled(enabled);
    }

    /**
     * LangSwithcing template
     *
     * @return
     */
    private static native String getLangSwitchingTemplate()  /*-{
    return  [
    '<tpl for=".">',
    '<div class="x-combo-list-item"><img src="{image}"/> {displayName}</div>',
    '</tpl>'
    ].join("");
  }-*/;


}