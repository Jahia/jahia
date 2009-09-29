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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActionItemGroup;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbarLayoutContainer;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;
import org.jahia.ajax.gwt.client.widget.toolbar.handler.ManagerSelectionHandler;
import org.jahia.ajax.gwt.client.widget.tripanel.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;

import java.util.List;

/**
 * The action toolbar container.
 *
 * @author rfelden
 * @version 7 juil. 2008 - 14:17:12
 */
public class ContentToolbar extends TopBar {

    private ActionToolbarLayoutContainer m_component;

    private ManagerConfiguration configuration;

    public ContentToolbar(ManagerConfiguration config, ManagerLinker linker) {
        configuration = config;
        initWithLinker(linker);
        createDynamicUi();
    }

    private void createDynamicUi() {
        ActionToolbarLayoutContainer toolbarContainer = new ActionToolbarLayoutContainer(configuration.getToolbarGroup());
        toolbarContainer.initWithLinker(getLinker());
        toolbarContainer.init();
        m_component = toolbarContainer;
    }

    // override to handle view switching
    /* protected void switchView(Button switchView) {
    }*/

    protected void setListView() {
    }

    protected void setThumbView() {
    }

    protected void setDetailedThumbView() {
    }

    protected void setTemplateView() {
    }

    public void handleNewSelection(Object leftTreeSelection, Object topTableSelectionEl) {
        List<GWTJahiaNode> topTableSelection = (List<GWTJahiaNode>) topTableSelectionEl;

        boolean isTreeSelection = leftTreeSelection != null;
        boolean isParentWriteable = (isTreeSelection) ? (((GWTJahiaNode) leftTreeSelection).isWriteable() && !((GWTJahiaNode) leftTreeSelection).isLocked()) : false;
        boolean isWritable = false;
        boolean isDeleteable = false;
        boolean isLockable = false;
        boolean isLocked = false;
        boolean isSingleFile = false;
        boolean isSingleFolder = false;
        boolean isPasteAllowed = isTreeSelection ? CopyPasteEngine.getInstance().canCopyTo((GWTJahiaNode) leftTreeSelection) : false;
        boolean isZip = false;
        boolean isImage = false;
        boolean isTableSelection = false;
        boolean isMount = false;
        if (topTableSelection != null && topTableSelection.size() > 0) {
            if (leftTreeSelection != null) {
                isTreeSelection = true;
            }
            if (!isTreeSelection) {
                GWTJahiaNode parent = (GWTJahiaNode) topTableSelection.get(0).getParent();
                if (parent != null) {
                    isParentWriteable = parent.isWriteable();
                }
            }
            isTableSelection = true;
            isWritable = true;
            isDeleteable = true;
            isLockable = true;
            isLocked = true;
            for (GWTJahiaNode gwtJahiaNode : topTableSelection) {
                isWritable &= gwtJahiaNode.isWriteable() && !gwtJahiaNode.isLocked();
                isDeleteable &= gwtJahiaNode.isDeleteable() && !gwtJahiaNode.isLocked();
                isLockable &= gwtJahiaNode.isLockable();
                isLocked &= gwtJahiaNode.isLocked();
            }
            if (topTableSelection.size() == 1) {
                isSingleFile = topTableSelection.get(0).isFile();
                isSingleFolder = !isSingleFile;
            }
            if (isSingleFolder) {
                isMount = topTableSelection.get(0).getInheritedNodeTypes().contains("jnt:mountPoint") || topTableSelection.get(0).getNodeTypes().contains("jnt:mountPoint");
            }
            if (!isTreeSelection) {
                if (isSingleFolder) {
                    isPasteAllowed = CopyPasteEngine.getInstance().canCopyTo(topTableSelection.get(0));
                } else {
                    isPasteAllowed = CopyPasteEngine.getInstance().canCopyTo((GWTJahiaNode) topTableSelection.get(0).getParent());
                }
            }
            int extIndex = topTableSelection.get(0).getName().lastIndexOf(".");
            if (extIndex > 0 && topTableSelection.get(0).getName().substring(extIndex).equalsIgnoreCase(".zip")) {
                isZip = true;
            }
            isImage = topTableSelection.get(0).getNodeTypes().contains("jmix:image");
        }


        m_component.enableOnConditions(isTreeSelection, isTableSelection, isWritable, isDeleteable, isParentWriteable, isSingleFile, isSingleFolder, isPasteAllowed, isLockable, isLocked, isZip, isImage, isMount);

    }

    public Component getComponent() {
        return m_component;
    }

}
