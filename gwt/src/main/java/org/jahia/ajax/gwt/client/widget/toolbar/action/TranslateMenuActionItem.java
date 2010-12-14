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

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.contentengine.TranslateContentEngine;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jan 20, 2010
 * Time: 1:51:18 PM
 * 
 */
public class TranslateMenuActionItem extends BaseActionItem {
    private List<GWTJahiaLanguage> languages;
    private String siteKey;

    public void init(GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        super.init(gwtToolbarItem, linker);
        setEnabled(false);
        siteKey = JahiaGWTParameters.getSiteKey();
        initMenu(linker);
    }

    private void initMenu(final Linker linker) {
        JahiaContentManagementService.App.getInstance().getSiteLanguages(
                new BaseAsyncCallback<List<GWTJahiaLanguage>>() {
                    public void onSuccess(List<GWTJahiaLanguage> result) {
                        languages = result;
                        final Menu menu = new Menu();

                        menu.removeAll();
                        final String uiLanguage = JahiaGWTParameters.getUILanguage();
                        if (languages != null) {
                            for (final GWTJahiaLanguage language : languages) {
                                for (final GWTJahiaLanguage jahiaLanguage : languages) {
                                    if (!jahiaLanguage.getDisplayName().equals(language.getDisplayName()) &&
                                        (jahiaLanguage.getLanguage().equals(uiLanguage) ||
                                         language.getLanguage().equals(uiLanguage))) {
                                        MenuItem item = new MenuItem(
                                                language.getDisplayName() + "->" + jahiaLanguage.getDisplayName());
                                        item.addSelectionListener(new SelectionListener<MenuEvent>() {
                                            @Override
                                            public void componentSelected(MenuEvent ce) {
                                                LinkerSelectionContext lh = linker.getSelectionContext();
                                                if (lh.getSingleSelection() != null) {
                                                    new TranslateContentEngine(lh.getSingleSelection(), linker,
                                                            language, jahiaLanguage).show();
                                                }
                                            }
                                        });
                                        menu.add(item);
                                    }
                                }
                            }
                        }
                        setSubMenu(menu);
                        setEnabled(true);
                    }

                    public void onApplicationFailure(Throwable caught) {

                    }
                });
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.getSingleSelection() != null && lh.isWriteable());
        if(!JahiaGWTParameters.getSiteKey().equals(siteKey)){
            siteKey=JahiaGWTParameters.getSiteKey();
            initMenu(linker);
        }
    }
}

