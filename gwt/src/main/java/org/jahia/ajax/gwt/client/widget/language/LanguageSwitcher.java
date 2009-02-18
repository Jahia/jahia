/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.language;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.AdapterMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguageSwitcherBean;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.service.JahiaServiceAsync;

import java.util.Map;

/**
 * This class will build a drop down menu displaying all the languages available for the current site
 * and their workflow states optionally.
 * User: rincevent
 * Date: 6 nov. 2008
 * Time: 15:46:46
 * To change this template use File | Settings | File Templates.
 */
public class LanguageSwitcher extends HorizontalPanel {
    private JahiaServiceAsync service = JahiaService.App.getInstance();
    private boolean displayFlag;
    private boolean displayIsoCode;
    private boolean displayLanguage;
    private boolean displayWorkflowStates;
    private String currentLanguage;
    private final boolean inEngine;
    private LanguageSelectedListener languageSelectedListener;

    public LanguageSwitcher (final boolean displayFlag, final boolean displayLanguage, final boolean displayIsoCode,
                             final boolean displayWorkflowStates, final String currentLanguage,
                             boolean inEngine, final LanguageSelectedListener languageSelectedListener) {
        super();
        this.displayFlag = displayFlag;
        this.displayIsoCode = displayIsoCode;
        this.displayLanguage = displayLanguage;
        this.displayWorkflowStates = displayWorkflowStates;
        this.currentLanguage = currentLanguage;
        this.inEngine = inEngine;
        this.languageSelectedListener = languageSelectedListener;
    }

    @Override
    protected void beforeRender () {
        init();
        super.beforeRender();
    }

    public void init () {
        final SelectionListener languageListener = new SelectionListener<ComponentEvent>() {
            public void componentSelected (ComponentEvent ce) {
                Component component = ce.component;
                String text;
                if (component instanceof Button) {
                    Button btn = (Button) component;
                    text = btn.getData(btn.getText());
                } else {
                    MenuItem menuItem = (MenuItem) ((MenuEvent) ce).item;
                    text = menuItem.getData(menuItem.getText());
                }
                languageSelectedListener.onLanguageSelected(text);
            }
        };
        final SelectionListener workflowListener = new SelectionListener<ComponentEvent>() {
            public void componentSelected (ComponentEvent ce) {
                Component component = ce.component;
                String text="";
                if (component instanceof IconButton) {
                    IconButton btn = (IconButton) component;
                    text = btn.getData("language");
                }
                languageSelectedListener.onWorkflowSelected(text);
            }
        };
        service.getAvailableLanguagesAndWorkflowStates(displayIsoCode, displayLanguage, inEngine, new AsyncCallback<GWTJahiaLanguageSwitcherBean>() {
            public void onFailure (Throwable throwable) {
            }

            public void onSuccess (GWTJahiaLanguageSwitcherBean bean) {
                Map<String, String> locales = bean.getAvailableLanguages();
                generateMenu(locales, languageListener, workflowListener, bean.getWorkflowStates());
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    private void generateMenu (Map<String, String> locales, SelectionListener listener, SelectionListener workflowListener, Map<String, String> workflowStates) {
        if (locales.size() > 1) {
            Menu menu = new Menu();
            TextToolItem textToolItem = new TextToolItem(locales.get(currentLanguage));
            if (displayFlag) {
                textToolItem.setIconStyle("flag_" + currentLanguage);
            }

            for (String locale : locales.keySet()) {
                if (!locale.equals(currentLanguage)) {
                    final String s1 = locales.get(locale);
                    HorizontalPanel horizontalPanel = new HorizontalPanel();
                    Button menuItem = new Button(s1);
                    menuItem.setData(s1, locale);
                    if (displayFlag) {
                        menuItem.setIconStyle("flag_" + locale);
                    }
                    menuItem.addSelectionListener(listener);
                    horizontalPanel.add(menuItem);
                    if (displayWorkflowStates) {
                        IconButton iconButton = new IconButton("&nbsp;&nbsp;&nbsp;");
                        String s = workflowStates.get(locale);
                        iconButton.setData("language",locale);
                        if (s == null) {
                            s = "000";
                        }
                        iconButton.setStyleName("workflow-" + s);
                        iconButton.setStylePrimaryName("workflow-" + s);

                        iconButton.addSelectionListener(workflowListener);
                        horizontalPanel.add(iconButton);
                    }
                    menu.add(new AdapterMenuItem(horizontalPanel));
                }
            }
            textToolItem.setMenu(menu);
            add(textToolItem);
            if (displayWorkflowStates) {
                IconButton iconButton = new IconButton("&nbsp;&nbsp;&nbsp;");
                iconButton.setData("language",currentLanguage);
                String s = workflowStates.get(currentLanguage);
                if (s == null) {
                    s = "000";
                }
                iconButton.setStyleName("workflow-" + s);
                iconButton.setStylePrimaryName("workflow-" + s);

                iconButton.addSelectionListener(workflowListener);
                add(iconButton);
            }
        } else {
            TextToolItem textToolItem = new TextToolItem(locales.get(currentLanguage));
            if(displayFlag)
            textToolItem.setIconStyle("flag_" + currentLanguage);
            this.setEnabled(false);
            add(textToolItem);
            if (displayWorkflowStates) {
                IconButton iconButton = new IconButton("&nbsp;&nbsp;&nbsp;");
                iconButton.setData("language",currentLanguage);
                String s = workflowStates.get(currentLanguage);
                if (s == null) {
                    s = "000";
                }
                iconButton.setStyleName("workflow-" + s);
                iconButton.setStylePrimaryName("workflow-" + s);

                iconButton.addSelectionListener(workflowListener);
                add(iconButton);
            }
        }
        doAttachChildren();
        doLayout();
    }
}
