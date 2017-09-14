/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

/**
 *
 * User: ktlili
 * Date: Jan 8, 2010
 * Time: 2:09:40 PM
 *
 */
public class SwitchModeActionItem extends NodeTypeAwareBaseActionItem {

    public void handleNewLinkerSelection() {
        Menu m = new Menu();
        MenuItem menuItem = new MenuItem(Messages.get("label.openInNewWindow","Open in new window"));
        menuItem.setIcon(ToolbarIconProvider.getInstance().getIcon("openWindow"));
        menuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                onComponentSelection(true);
            }
        });
        m.add(menuItem);
        setSubMenu(m);

        final String workspace = getPropertyValue(getGwtToolbarItem(), "workspace");
        if ((linker instanceof EditLinker && ((EditLinker) linker).isInSettingsPage()) ||
                !hasPermission(linker.getSelectionContext().getMainNode())) {
            setEnabled(false);
        } else {
            if (workspace.equalsIgnoreCase("live")) {
                final GWTJahiaNode node = linker.getSelectionContext().getMainNode();
                if (node == null) {
                    setEnabled(false);
                } else {
                    GWTJahiaPublicationInfo publicationInfo = node.getAggregatedPublicationInfo() != null ? node
                            .getAggregatedPublicationInfo() : node.getQuickPublicationInfo();

                    if (publicationInfo.getStatus() == GWTJahiaPublicationInfo.NOT_PUBLISHED
                            ||  publicationInfo.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED) {
                        setEnabled(false);
                    } else {
                        setEnabled(true);
                    }
                }
            } else {
                setEnabled(true);
            }
        }

        final LinkerSelectionContext lh = linker.getSelectionContext();
        setVisible(lh.getSingleSelection() != null && isNodeTypeAllowed(lh.getSingleSelection()));
    }

    @Override
    public void onComponentSelection() {
        onComponentSelection(false);
    }

    private void onComponentSelection(final boolean openWindow) {
        final String workspace = getPropertyValue(getGwtToolbarItem(), "workspace");
        final String urlParams = getPropertyValue(getGwtToolbarItem(), "urlParams");
        final String servlet = getPropertyValue(getGwtToolbarItem(), "servlet");
        final GWTJahiaNode node = linker.getSelectionContext().getMainNode();
        if (node != null) {
            String path = node.getPath();
            String locale = JahiaGWTParameters.getLanguage();
            JahiaContentManagementService.App.getInstance()
                    .getNodeURL(servlet, path, null, null, workspace, locale, false, new BaseAsyncCallback<String>() {
                        public void onSuccess(String url) {
                            String url1 = url + ((urlParams != null) ? "?" + urlParams : "");
                            if (openWindow) {
                                Window.open(url1, "_blank", "");
                            } else {
                                Window.Location.assign(url1);
                            }
                        }
                    });
        }
    }

}
