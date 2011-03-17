/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaSite;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Toolbar action item to copy template to selected site
 */
public class DeployTemplatesActionItem extends BaseActionItem {
    private static List<DeployTemplatesActionItem> instances = new ArrayList<DeployTemplatesActionItem>();

    private transient List<GWTJahiaSite> sites = new ArrayList<GWTJahiaSite>();

    private transient Map<String,List<GWTJahiaSite>> sitesMap = new HashMap<String, List<GWTJahiaSite>>();

    public void init(GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        super.init(gwtToolbarItem, linker);
        instances.add(this);
        setEnabled(false);

        JahiaContentManagementService.App.getInstance().getAvailableSites(new BaseAsyncCallback<List<GWTJahiaSite>>() {
            public void onSuccess(List<GWTJahiaSite> result) {
                for (GWTJahiaSite gwtJahiaSite : result) {
                    final String key = (String) gwtJahiaSite.get("templateFolder");
                    if (!sitesMap.containsKey(key)) {
                        sitesMap.put(key, new ArrayList<GWTJahiaSite>());
                    }
                    sitesMap.get(key).add(gwtJahiaSite);
                    sites.add(gwtJahiaSite);
                }

                refreshMenu(linker);
            }

            public void onApplicationFailure(Throwable caught) {

            }
        });
    }

    public static void refreshAllMenus(final Linker linker) {
        for (DeployTemplatesActionItem instance : instances) {
            instance.refreshMenu(linker);
        }
    }

    public void refreshMenu(Linker linker) {
        final Menu menu = new Menu();

        menu.removeAll();

        if ("templatesSet".equals(JahiaGWTParameters.getSiteNode().get("j:siteType"))) {
            if (sitesMap != null && sitesMap.containsKey(JahiaGWTParameters.getSiteKey())) {
                for (GWTJahiaSite site : sitesMap.get(JahiaGWTParameters.getSiteKey())) {
                    MenuItem item = new MenuItem(site.getSiteKey());
                    addDeployListener(item, linker, "/sites/" + site.getSiteKey());
                    item.setData("site",site);
                    menu.add(item);
                }
            }
        } else {
            List<String> dependencies = JahiaGWTParameters.getSiteNode().get("j:dependencies");
            for (GWTJahiaSite site : sites) {
                String label = site.getSiteKey();
                if (dependencies != null && dependencies.size() > 0) {
                    if (!site.getInstalledModules().containsAll(dependencies)) {
                        continue;
                    }
                }
                if (site.getInstalledModules().contains(JahiaGWTParameters.getSiteKey())) {
                    label += " *";
                }
                MenuItem item = new MenuItem(label);
                addDeployListener(item, linker, "/sites/" + site.getSiteKey());
                item.setData("site",site);
                menu.add(item);
            }
        }
        if (menu.getItems().isEmpty()) {
            MenuItem item = new MenuItem(Messages.get("label.nosites", "No target sites"));
            item.setEnabled(false);
            menu.add(item);
        }
        setSubMenu(menu);
        setEnabled(true);
    }

    private void addDeployListener(final MenuItem item, final Linker linker, final String destinationPath) {
        item.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                Info.display(Messages.get("label.templatesDeploy", "Deploy Templates"), Messages.get("org.jahia.admin.site.ManageTemplates.deploymentInProgress", "Your templates are being deployed..."));
                GWTJahiaNode node = linker.getSelectionContext().getMainNode();
                String nodePath = node.getPath();

                final String[] parts = nodePath.split("/");
                nodePath = "/" + parts[1] + "/" + parts[2];

                JahiaContentManagementService.App.getInstance()
                        .deployTemplates(nodePath, destinationPath, new BaseAsyncCallback() {
                            public void onApplicationFailure(Throwable caught) {
                                Info.display(Messages.get("label.templatesDeploy", "Deploy Templates"), Messages.get("org.jahia.admin.site.ManageTemplates.deploymentError", "Error during your templates deployment"));
                            }

                            public void onSuccess(Object result) {
                                GWTJahiaSite site = item.getData("site");
                                if (!site.getInstalledModules().contains(parts[2])) {
                                    site.getInstalledModules().add(parts[2]);
                                    item.setText(item.getText()+" *");
                                }
                                Info.display(Messages.get("label.templatesDeploy", "Deploy Templates"), Messages.get("org.jahia.admin.site.ManageTemplates.templatesDeployed", "Your templates deployment is successful"));
                            }
                        });
            }
        });
    }

}