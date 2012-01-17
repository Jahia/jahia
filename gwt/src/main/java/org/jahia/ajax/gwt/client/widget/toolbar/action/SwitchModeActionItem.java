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
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
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

/**
 * 
 * User: ktlili
 * Date: Jan 8, 2010
 * Time: 2:09:40 PM
 *
 */
public class SwitchModeActionItem extends BaseActionItem {

    public void handleNewLinkerSelection() {
        final String workspace = getPropertyValue(getGwtToolbarItem(), "workspace");
        if (workspace.equalsIgnoreCase("live")) {
            final GWTJahiaNode node = linker.getSelectionContext().getMainNode();
            if (node == null || node.getAggregatedPublicationInfo().getStatus() == GWTJahiaPublicationInfo.NOT_PUBLISHED
                    || node.getAggregatedPublicationInfo().getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED) {
                setEnabled(false);
            } else {
                setEnabled(true);
            }
        }
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
                    .getNodeURL(servlet, path, null, null, workspace, locale, new BaseAsyncCallback<String>() {
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

    @Override
    public Component getTextToolItem() {
        Component c = super.getTextToolItem();
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
        c.setContextMenu(m);
        return c;
    }
}
