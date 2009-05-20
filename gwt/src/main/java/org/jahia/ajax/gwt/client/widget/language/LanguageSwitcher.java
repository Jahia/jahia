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
package org.jahia.ajax.gwt.client.widget.language;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguageSwitcherBean;
import org.jahia.ajax.gwt.client.data.GWTLanguageSwitcherLocaleBean;
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
public class LanguageSwitcher {
    private JahiaServiceAsync service = JahiaService.App.getInstance();
    private boolean displayFlag;
    private boolean displayIsoCode;
    private boolean displayLanguage;
    private boolean displayWorkflowStates;
    private String currentLanguage;
    private final boolean inEngine;
    private LanguageSelectedListener languageSelectedListener;

    public LanguageSwitcher(final boolean displayFlag, final boolean displayLanguage, final boolean displayIsoCode, final boolean displayWorkflowStates, final String currentLanguage, boolean inEngine, final LanguageSelectedListener languageSelectedListener) {
        this.displayFlag = displayFlag;
        this.displayIsoCode = displayIsoCode;
        this.displayLanguage = displayLanguage;
        this.displayWorkflowStates = displayWorkflowStates;
        this.currentLanguage = currentLanguage;
        this.inEngine = inEngine;
        this.languageSelectedListener = languageSelectedListener;
    }

    public void init(final TextToolItem parentItem) {
        service.getAvailableLanguagesAndWorkflowStates(displayIsoCode, displayLanguage, inEngine, new AsyncCallback<GWTJahiaLanguageSwitcherBean>() {
            public void onFailure (Throwable throwable) {}

            public void onSuccess (GWTJahiaLanguageSwitcherBean bean) {
                Map<String, GWTLanguageSwitcherLocaleBean> locales = bean.getAvailableLanguages();
                generateMenu(parentItem, locales, bean.getWorkflowStates());
            }
        });
    }

    private void generateMenu (final TextToolItem parentItem, final Map<String, GWTLanguageSwitcherLocaleBean> locales, final Map<String, String> workflowStates) {
        final GWTLanguageSwitcherLocaleBean localeBean = locales.get(currentLanguage);
        LanguageCodeDisplay.formatToolItem(parentItem, localeBean, displayFlag, workflowStates.get(currentLanguage), displayWorkflowStates);
        if (locales.size() > 1) {
            Menu menu = new Menu();
            for (final String locale : locales.keySet()) {
                if (!locale.equals(currentLanguage)) {
                    MenuItem item = new MenuItem() ;
                    LanguageCodeDisplay.formatMenuItem(item, locales.get(locale), displayFlag, workflowStates.get(locale), displayWorkflowStates);
                    item.addSelectionListener(new SelectionListener<ComponentEvent>() {
                        public void componentSelected(ComponentEvent componentEvent) {
                            languageSelectedListener.onLanguageSelected(locale);
                        }
                    });
                    menu.add(item);
                }
            }
            parentItem.setMenu(menu);
            parentItem.el().child("tr").addStyleName("x-btn-with-menu");
            parentItem.setEnabled(true);
        } else {
            parentItem.setEnabled(false);
            parentItem.el().child("tr").removeStyleName("x-btn-with-menu");
        }
    }
}
