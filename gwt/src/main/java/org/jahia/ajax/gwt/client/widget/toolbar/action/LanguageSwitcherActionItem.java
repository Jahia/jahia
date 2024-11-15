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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;

import java.util.Arrays;

/**
 * Language switcher toolbar item for all possible languages.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:19:51 PM
 */
public class LanguageSwitcherActionItem extends BaseLanguageAwareActionItem {
    private static final long serialVersionUID = 9115660301140902069L;
    private static LanguageSwitcherActionItem instance;
    protected transient ComboBox<GWTJahiaLanguage> mainComponent;
    protected boolean events = true;

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        initMainComponent();
        instance = this;
    }

    /**
     * init main component
     */
    private void initMainComponent() {
        mainComponent = new ComboBox<GWTJahiaLanguage>();
        mainComponent.addStyleName(getGwtToolbarItem().getClassName());
        mainComponent.addStyleName("action-bar-menu-item");
        mainComponent.setStore(new ListStore<GWTJahiaLanguage>());
        mainComponent.getStore().add(JahiaGWTParameters.getSiteLanguages());
        mainComponent.setDisplayField("displayName");
        mainComponent.setTemplate(getLangSwitchingTemplate());
        mainComponent.setTypeAhead(true);
        mainComponent.setTriggerAction(ComboBox.TriggerAction.ALL);
        mainComponent.setForceSelection(true);
        mainComponent.setEditable(false);
        mainComponent.setValue(selectedLang);
        mainComponent.getListView().setStyleAttribute("font-size","11px");
        mainComponent.getListView().addStyleName("menu-"+getGwtToolbarItem().getClassName());

        mainComponent.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaLanguage>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaLanguage> event) {
                if (events) {
                    if (linker instanceof EditLinker) {
                        ((EditLinker) linker).getMainModule().switchLanguage(event.getSelectedItem());
                    } else if (linker instanceof ManagerLinker) {
                        ((ManagerLinker) linker).switchLanguage(event.getSelectedItem());
                    }
                }
            }
        });
        setEnabled(true);
    }

    @Override
    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        super.handleNewMainNodeLoaded(node);
        updateSite();
    }

    @Override
    public void handleNewLinkerSelection() {
        super.handleNewLinkerSelection();
        updateSite();
    }

    private void updateSite() {
        if (!JahiaGWTParameters.getSiteLanguages().equals(mainComponent.getStore().getModels())) {
            events = false;
            mainComponent.getStore().removeAll();
            mainComponent.reset();
            mainComponent.getStore().add(JahiaGWTParameters.getSiteLanguages());
            mainComponent.getListView().getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
            if (mainComponent.getSelection().isEmpty() || !JahiaGWTParameters.getLanguage().equals(mainComponent.getSelection().get(0).getLanguage())) {

                // The getSelection() call above filters the store, so we explicitly clear the filters after that.
                mainComponent.getStore().clearFilters();

                for (GWTJahiaLanguage language : JahiaGWTParameters.getSiteLanguages()) {
                    if (language.getLanguage().equals(JahiaGWTParameters.getLanguage())) {
                        mainComponent.setSelection(Arrays.asList(language));
                        break;
                    }
                }
            }
            events = true;
            if (!JahiaGWTParameters.getSiteLanguages().contains(mainComponent.getSelection().get(0))) {
                mainComponent.setSelection(Arrays.asList((GWTJahiaLanguage) JahiaGWTParameters.getSiteNode().get("j:defaultLanguage")));
            }
        }
        if (!JahiaGWTParameters.getLanguage().equals(mainComponent.getSelection().get(0).getLanguage())) {
            events = false;
            GWTJahiaLanguage language = JahiaGWTParameters.getLanguage(JahiaGWTParameters.getLanguage());
            if (language != null) {
                mainComponent.setSelection(Arrays.asList(language));
            }
            events = true;
        }
    }

    public static void setLanguage(GWTJahiaLanguage language) {
        instance.mainComponent.setSelection(Arrays.asList(language));
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
    public static native String getLangSwitchingTemplate()  /*-{
    return  [
    '<tpl for=".">',
    '<tpl if="active">',
        '<div class="x-combo-list-item"><img src="{image}"/> {displayName}</div>',
    '</tpl>',
    '<tpl if="!active">',
        '<div class="x-combo-list-item" style="color:#BBBBBB"><img src="{image}"/> {displayName}</div>',
    '</tpl>',
    '</tpl>'
    ].join("");
  }-*/;

}
