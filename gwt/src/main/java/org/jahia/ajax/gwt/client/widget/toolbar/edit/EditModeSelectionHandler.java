package org.jahia.ajax.gwt.client.widget.toolbar.edit;

import org.jahia.ajax.gwt.client.widget.edit.Module;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.allen_sauer.gwt.log.client.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 * <p/>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * <p/>
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * <p/>
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
public class EditModeSelectionHandler {
    private Map<String, Component> registeredOnModuleSelectionComponents = new HashMap<String, Component>();
    private Map<Component, String> registeredEnableOnConditionComponents = new HashMap<Component, String>();


    /**
     * Handle new module selection
     *
     * @param selectedModule
     */
    public void handleNewModuleSelection(Module selectedModule) {
        for (Component button : registeredEnableOnConditionComponents.keySet()) {
            String condition = registeredEnableOnConditionComponents.get(button);
            enableOnConditions(button, condition, selectedModule, null);
        }
        final Component createPage = registeredOnModuleSelectionComponents.get("createPage");
        final Component publish = registeredOnModuleSelectionComponents.get("publish");
        final Component unpublish = registeredOnModuleSelectionComponents.get("unpublish");
        final Component lock = registeredOnModuleSelectionComponents.get("lock");
        final Component unlock = registeredOnModuleSelectionComponents.get("unlock");
        final Component edit = registeredOnModuleSelectionComponents.get("createPage");
        final Component delete = registeredOnModuleSelectionComponents.get("edit");
        final Button status = (Button) registeredOnModuleSelectionComponents.get("status");


        if (selectedModule != null) {
            final String s = selectedModule.getNode().getPath();
            status.setText(s);
            JahiaContentManagementService.App.getInstance().getPublicationInfo(s, new AsyncCallback<GWTJahiaPublicationInfo>() {
                public void onFailure(Throwable caught) {

                }

                public void onSuccess(GWTJahiaPublicationInfo result) {
                    Log.debug("GWTJahiaPublicationInfo: " + result.getStatus());
                    switch (result.getStatus()) {
                        case GWTJahiaPublicationInfo.MODIFIED:
                            if (publish != null) {
                                publish.setEnabled(true);
                            }
                            if (unpublish != null) {
                                unpublish.setEnabled(true);
                            }
                            if (status != null) {
                                status.setText("status : " + s + " : modified");
                            }
                            break;
                        case GWTJahiaPublicationInfo.PUBLISHED:
                            if (publish != null) {
                                publish.setEnabled(false);
                            }
                            if (unpublish != null) {
                                unpublish.setEnabled(true);
                            }
                            if (status != null) {
                                status.setText("status : " + s + " : published");
                            }
                            break;
                        case GWTJahiaPublicationInfo.UNPUBLISHED:
                            if (publish != null) {
                                publish.setEnabled(true);
                            }
                            if (unpublish != null) {
                                unpublish.setEnabled(false);
                            }
                            if (status != null) {
                                status.setText("status : " + s + " : unpublished");
                            }
                            break;
                        case GWTJahiaPublicationInfo.UNPUBLISHABLE:
                            if (publish != null) {
                                publish.setEnabled(false);
                            }
                            if (unpublish != null) {

                                unpublish.setEnabled(false);
                            }
                            if (status != null) {
                                status.setText("status : " + s + " : unpublishable / publish parent first");
                            }
                            break;
                    }
                }
            });
        }

    }

    /**
     * Handle new Side panel selection
     *
     * @param node
     */
    public void handleNewSidePanelSelection(GWTJahiaNode node) {

    }

    /**
     * Register component
     *
     * @param component
     */
    public void registerComponent(GWTJahiaToolbarItem gwtToolbarItem, Component component) {
        // register selection handler
        String editSelectionHandler = getPropertyValue(gwtToolbarItem, ToolbarConstants.EDIT_SELECTION_HANDLER);
        if (editSelectionHandler != null) {
            registeredOnModuleSelectionComponents.put(editSelectionHandler, component);

        }

        String enableOnConditions = getPropertyValue(gwtToolbarItem, ToolbarConstants.ENABLE_ON_CONDITIONS);
        if (editSelectionHandler != null) {
            registeredEnableOnConditionComponents.put(component, enableOnConditions);

        }
    }

    /**
     * Enable on condition
     *
     * @param selectedModule
     * @param selectedNode
     */
    public void enableOnConditions(Component component, String condition, Module selectedModule, GWTJahiaNode selectedNode) {
        boolean nodeWitable = selectedModule != null && selectedModule.getNode().isWriteable();
        boolean nodeLocked = selectedModule != null && selectedModule.getNode().isLockable() && selectedModule.getNode().isLocked();

        if (condition.equalsIgnoreCase("true")) {
            component.setEnabled(true);
            return;
        }

        if (condition.equalsIgnoreCase("nodeWitable")) {
            component.setEnabled(nodeWitable);
            return;
        }

        if (condition.equalsIgnoreCase("nodeLocked")) {
            component.setEnabled(nodeLocked);
            return;
        }

        if (condition.equalsIgnoreCase("nodeUnLocked")) {
            component.setEnabled(!nodeLocked);
            return;
        }

    }


    /**
     * Het property value
     *
     * @param gwtToolbarItem
     * @param propertyName
     * @return
     */
    public String getPropertyValue(GWTJahiaToolbarItem gwtToolbarItem, String propertyName) {
        Map properties = gwtToolbarItem.getProperties();
        GWTJahiaProperty property = properties != null ? (GWTJahiaProperty) properties
                .get(propertyName)
                : null;
        return property != null ? property.getValue() : null;
    }


}
