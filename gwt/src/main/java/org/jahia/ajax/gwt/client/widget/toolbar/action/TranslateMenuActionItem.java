/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.contentengine.TranslateContentEngine;

/**
 *
 * User: ktlili
 * Date: Jan 20, 2010
 * Time: 1:51:18 PM
 *
 */
public class TranslateMenuActionItem extends BaseActionItem {
    private transient Menu menu;

    public void init(GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        super.init(gwtToolbarItem, linker);
        setEnabled(false);
        menu = new Menu();
    }

    private void initMenu(final Linker linker) {
        boolean notEmpty = false;
        menu.removeAll();
        final String currentLanguage = JahiaGWTParameters.getLanguage();
        for (final GWTJahiaLanguage sourceLang : JahiaGWTParameters.getSiteLanguages()) {
            for (final GWTJahiaLanguage destLang : JahiaGWTParameters.getSiteLanguages()) {
                if (!destLang.getDisplayName().equals(sourceLang.getDisplayName()) &&
                    (destLang.getLanguage().equals(currentLanguage) ||
                     sourceLang.getLanguage().equals(currentLanguage))) {
                    final LinkerSelectionContext lh = linker.getSelectionContext();
                    if (PermissionsUtils.isPermitted("jcr:modifyProperties_" + JahiaGWTParameters.getWorkspace() + "_" + destLang.getLanguage(), lh.getSelectionPermissions())) {
                        final GWTJahiaNode selection = lh.getSingleSelection();
                        if (selection != null && (!selection.isLocked() || !selection.getLockInfos().containsKey(destLang.getLanguage()))) {
                            MenuItem item = new MenuItem(
                                    sourceLang.getDisplayName() + "->" + destLang.getDisplayName());

                            item.addSelectionListener(new SelectionListener<MenuEvent>() {
                                @Override
                                public void componentSelected(MenuEvent ce) {
                                    new TranslateContentEngine(selection, linker, sourceLang,
                                            destLang).show();
                                }
                            });
                            item.addStyleName("toolbar-item-translatemenu-item");
                            menu.add(item);
                            notEmpty = true;
                        }
                    }
                }
            }
        }
        if (notEmpty) {
            setSubMenu(menu);
        }
        setEnabled(notEmpty);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        boolean isEnabled = lh.getSingleSelection() != null && hasPermission(lh.getSingleSelection()) && !lh.isRootNode();
        setEnabled(isEnabled);
        if (isEnabled) {
            initMenu(linker);
        }
    }
}

