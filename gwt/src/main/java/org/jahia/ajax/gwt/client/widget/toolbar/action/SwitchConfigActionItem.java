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
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

public class SwitchConfigActionItem extends BaseActionItem {
    private String configurationName;
    private boolean updateSidePanel =true;
    private boolean updateToolbar =true;

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem,linker);
        final RootPanel panel = RootPanel.get("editmode");
        if (panel != null) {
            if (!MainModule.getInstance().getConfig().getName().equals(configurationName)) {
                getTextToolItem().setEnabled(true);
                getGwtToolbarItem().setSelected(false);
            } else {
                getGwtToolbarItem().setSelected(true);
            }
        }
    }

    public boolean isUpdateSidePanel() {
        return updateSidePanel;
    }

    public void setUpdateSidePanel(boolean updateSidePanel) {
        this.updateSidePanel = updateSidePanel;
    }

    public boolean isUpdateToolbar() {
        return updateToolbar;
    }

    public void setUpdateToolbar(boolean updateToolbar) {
        this.updateToolbar = updateToolbar;
    }

    @Override
    public void onComponentSelection() {
        linker.loading(Messages.get("label.loading", "Loading..."));
        JahiaContentManagementService.App.getInstance().getEditConfiguration(linker.getSelectionContext().getMainNode().getPath(), configurationName, new BaseAsyncCallback<GWTEditConfiguration>() {
            public void onSuccess(GWTEditConfiguration gwtEditConfiguration) {
                ((EditLinker)linker).switchConfig(gwtEditConfiguration, updateSidePanel, updateToolbar);
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.error("Error when loading EditConfiguration", throwable);
            }
        });

    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    @Override
    public Component createNewToolItem() {
        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setToggleGroup("switchConfig");
        if (getGwtToolbarItem().isSelected()) {
            toggleButton.setAllowDepress(false);
        }
        toggleButton.setEnabled(false);
        return toggleButton;
    }
}
