/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
                for (GWTJahiaLanguage language : JahiaGWTParameters.getSiteLanguages()) {
                    if (language.getLanguage().equals(JahiaGWTParameters.getLanguage())) {
                        mainComponent.setSelection(Arrays.asList(language));
                        break;
                    }
                }
            }
            events = true;
            if (!JahiaGWTParameters.getSiteLanguages().contains(mainComponent.getSelection().get(0))) {
                mainComponent.setSelection(Arrays.asList(JahiaGWTParameters.getSiteLanguages().get(0)));
            }
        }
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

    /**
     * LangSwithcing template
     *
     * @return
     */
    public static native String getLangEngineSwitchingTemplate()  /*-{
        return  [
            '<tpl for=".">',
            '<div class="x-combo-list-item"><img src="{image}"/> {displayName}</div>',
            '</tpl>'
        ].join("");
    }-*/;


}