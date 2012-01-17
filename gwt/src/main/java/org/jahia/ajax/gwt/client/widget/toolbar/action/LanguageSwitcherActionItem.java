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

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;

/**
 * Language switcher toolbar item for all possible languages. 
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:19:51 PM
 */
public class LanguageSwitcherActionItem extends BaseLanguageAwareActionItem {
    private static final long serialVersionUID = 9115660301140902069L;
	protected transient ComboBox<GWTJahiaLanguage> mainComponent;
    protected boolean events = true;

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        initMainComponent();
    }

    /**
     * init main component
     */
    private void initMainComponent() {
        mainComponent = new ComboBox<GWTJahiaLanguage>();
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