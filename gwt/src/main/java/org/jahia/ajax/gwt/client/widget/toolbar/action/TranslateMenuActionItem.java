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

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.contentengine.TranslateContentEngine;

import java.util.List;

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
                && !lh.isRootNode());
        menu.removeAll();
        initMenu(linker);
    }
}

