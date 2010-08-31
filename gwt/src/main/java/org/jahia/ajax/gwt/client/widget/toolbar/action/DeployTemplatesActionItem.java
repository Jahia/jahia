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
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaSite;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Toolbar action item to copy template to selected site
 */
public class DeployTemplatesActionItem extends BaseActionItem {

    private transient List<GWTJahiaSite> sites;

    public void init(GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        super.init(gwtToolbarItem, linker);
        setEnabled(false);

        JahiaService.App.getInstance().getAvailableSites(new BaseAsyncCallback<List<GWTJahiaSite>>() {
            public void onSuccess(List<GWTJahiaSite> result) {
                sites = result;
                final Menu menu = new Menu();

                menu.removeAll();

                if (sites != null) {
                    for (GWTJahiaSite site : sites) {
                        MenuItem item = new MenuItem(site.getSiteKey());
                        item.setData("site", site);
                        item.addSelectionListener(new SelectionListener<MenuEvent>() {
                            @Override
                            public void componentSelected(MenuEvent ce) {
                                Info.display("Deploy Templates", "Your templates are being deployed...");
                                GWTJahiaNode node = linker.getMainNode();
                                GWTJahiaSite site = ce.getItem().getData("site");
                                String nodePath = node.getPath();

                                final String[] parts = nodePath.split("/");
                                String destinationPath = "/sites/" + site.getSiteKey();
                                nodePath = "/" + parts[1] + "/" + parts[2];
                                Map<String, String> pathsToSyncronize = new LinkedHashMap<String, String>();
                                pathsToSyncronize.put(nodePath, destinationPath);

                                JahiaContentManagementService.App.getInstance()
                                        .synchro(pathsToSyncronize, new BaseAsyncCallback() {
                                            public void onApplicationFailure(Throwable caught) {
                                                Info.display("Deploy Templates", "Error during your templates deployment");
                                            }

                                            public void onSuccess(Object result) {
                                                Info.display("Deploy Templates", "Your templates deployment is successful");
                                            }
                                        });
                            }
                        });
                        menu.add(item);
                    }
                }
                setSubMenu(menu);
                setEnabled(true);
            }

            public void onApplicationFailure(Throwable caught) {

            }
        });
    }

}