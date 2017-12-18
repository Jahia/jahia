/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
        setEnabled(lh.getSingleSelection() != null
                && hasPermission(lh.getSingleSelection())
                && !lh.isRootNode());
        menu.removeAll();
        initMenu(linker);
    }
}

