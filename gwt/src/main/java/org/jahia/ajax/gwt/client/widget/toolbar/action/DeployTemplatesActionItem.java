/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.GWTJahiaSite;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Toolbar action item to copy template to selected site
 *
 */
public class DeployTemplatesActionItem extends BaseActionItem {
    private boolean ok = false;

    public void handleNewLinkerSelection() {
        if (!ok) {
            final Menu menu = new Menu();

            menu.removeAll();


            JahiaService.App.getInstance().getAvailableSites(new AsyncCallback<List<GWTJahiaSite>>() {
                public void onSuccess(List<GWTJahiaSite> result) {
                    for (GWTJahiaSite site : result) {
                        MenuItem item = new MenuItem(site.getSiteKey());
                        item.setData("site",site);
                        item.addSelectionListener(new SelectionListener<MenuEvent>() {
                            @Override
                            public void componentSelected(MenuEvent ce) {
                                Info.display("Deploy Templates","Your templates are being deployed...");
                                GWTJahiaNode node = linker.getSelectedNode();
                                GWTJahiaSite site = ce.getItem().getData("site");
                                String nodePath = node.getPath();
                                String originalPath;
                                if(nodePath.substring(0,nodePath.lastIndexOf("/")).equals("/templatesSet")) {
                                    originalPath = nodePath+"/defaultSite/templates";
                                } else if (nodePath.contains("/defaultSite/templates")){
                                    originalPath = nodePath;
                                } else {
                                    MessageBox.alert("Error","Error could not deploy the selected path, please select a particular template or the root level of your templateSet",null);
                                    return;
                                }
                                String s = "/" + (originalPath.substring(originalPath.lastIndexOf(
                                        "/defaultSite/") + "/defaultSite/".length()));
                                String destinationPath = "/sites/" + site.getSiteKey() + s;
                                Map<String, String> pathsToSyncronize = new LinkedHashMap<String, String>();
                                pathsToSyncronize.put(originalPath,destinationPath);
                                destinationPath = "/sites/" + site.getSiteKey() + "/contents";
                                originalPath = originalPath.substring(0, originalPath.indexOf(
                                        "/defaultSite/templates")) + "/defaultSite/contents";
                                pathsToSyncronize.put(originalPath,destinationPath);
                                JahiaContentManagementService.App.getInstance().synchro(pathsToSyncronize,new AsyncCallback() {
                                    public void onFailure(Throwable caught) {
                                        Info.display("Deploy Templates","Error during your templates deployment");
                                    }

                                    public void onSuccess(Object result) {
                                        Info.display("Deploy Templates","Your templates deployment is successful");
                                    }
                                });
                            }
                        });
                        menu.add(item);

                    }
                }

                public void onFailure(Throwable caught) {
                    Log.error("Unable to load available sites", caught);
                }

            });

            setSubMenu(menu);
            
            ok = true;
        }

        setEnabled(true);
    }

}