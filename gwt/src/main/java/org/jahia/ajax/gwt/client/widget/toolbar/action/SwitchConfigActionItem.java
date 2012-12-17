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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditPanelViewport;

import java.util.HashSet;
import java.util.Set;

public class SwitchConfigActionItem extends BaseActionItem {
    private String configurationName;
    private transient GWTEditConfiguration config;

    private transient Set<String> nonEditableTypes;

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem,linker);
        final RootPanel panel = RootPanel.get("editmode");
        if (panel != null) {
            final String path = DOM.getElementAttribute(panel.getElement(), "path");

            JahiaContentManagementService.App.getInstance().getEditConfiguration(path, configurationName, new BaseAsyncCallback<GWTEditConfiguration>() {
                public void onSuccess(GWTEditConfiguration gwtEditConfiguration) {
                    config = gwtEditConfiguration;
                }

                public void onApplicationFailure(Throwable throwable) {
                    Log.error("Error when loading EditConfiguration", throwable);
                }
            });
        }

        nonEditableTypes = linker.getConfig().getNonEditableTypes();
    }

    @Override
    public void onComponentSelection() {
        ((EditLinker)linker).switchConfig(config);
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    @Override
    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        if (node.get("compatibilityMode") != Boolean.TRUE) {
            setEnabled(true);
            linker.getConfig().setNonEditableTypes(nonEditableTypes);
            linker.refresh(Linker.REFRESH_COMPONENTS,null);
        } else {
            setEnabled(false);
            linker.getConfig().setNonEditableTypes(null);
            linker.refresh(Linker.REFRESH_COMPONENTS,null);
        }

    }
}
