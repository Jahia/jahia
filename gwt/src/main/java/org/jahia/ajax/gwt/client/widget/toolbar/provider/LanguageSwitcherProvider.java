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
package org.jahia.ajax.gwt.client.widget.toolbar.provider;

import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.service.JahiaServiceAsync;
import org.jahia.ajax.gwt.client.widget.language.LanguageSelectedListener;
import org.jahia.ajax.gwt.client.widget.language.LanguageSwitcher;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 27 nov. 2008
 * Time: 11:35:12
 * To change this template use File | Settings | File Templates.
 */
public class LanguageSwitcherProvider extends JahiaToolItemProvider {
    public Widget createWidget (GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, GWTJahiaToolbarItem gwtToolbarItem) {
        return null;
    }

    public ToolItem createToolItem (GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, GWTJahiaToolbarItem gwtToolbarItem) {
        TextToolItem item = new TextToolItem("loading...") ;
        item.setEnabled(false);
        new LanguageSwitcher(true, true, false,true, JahiaGWTParameters.getLanguage(), false, new LanguageSelectedListener() {
            private final JahiaServiceAsync jahiaServiceAsync = JahiaService.App.getInstance();
            public void onLanguageSelected (String languageSelected) {
                jahiaServiceAsync.getLanguageURL(languageSelected,new AsyncCallback<String>() {
                    public void onFailure (Throwable throwable) {}

                    public void onSuccess (String s) {
                        com.google.gwt.user.client.Window.Location.assign(s);
                    }
                });
            }
        }).init(item);
        return item ;
    }

    public Item createMenuItem (GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, GWTJahiaToolbarItem gwtToolbarItem) {
        return null;
    }

    public DataListItem createDataListItem (DataList itemsList, GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, GWTJahiaToolbarItem gwtToolbarItem) {
        return null;
    }

    public TabItem createTabItem (TabPanel tabPanel, GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, GWTJahiaToolbarItem gwtToolbarItem) {
        return null;
    }
}
