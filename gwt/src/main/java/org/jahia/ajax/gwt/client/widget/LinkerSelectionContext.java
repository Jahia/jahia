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

package org.jahia.ajax.gwt.client.widget;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: ktlili
 * Date: Oct 1, 2009
 * Time: 3:38:07 PM
 */
public class LinkerSelectionContext {
    public static final int CONTENT_VIEWS_CONTEXT_MENU = 1;
    public static final int REPOSITORY_TABS_CONTEXT_MENU = 2;
    public static final int MAIN_AREA_CONTEXT_MENU = 3;

    private GWTJahiaNode mainNode;
    private List<GWTJahiaNode> selectedNodes;

    private GWTJahiaNode singleSelection;
    private List<GWTJahiaNode> multipleSelection;
    private GWTJahiaNode parent;

    private boolean pasteAllowed;

    private boolean writeable;
    private boolean parentWriteable;
    private boolean lockable;
    private boolean locked;

    private boolean file;
    private boolean zip;
    private boolean image;

    private int contextMenu;



    public void setMainNode(GWTJahiaNode selectedTreeNode) {
        this.mainNode = selectedTreeNode;
    }

    public void setSelectedNodes(List<GWTJahiaNode> selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

    public void setContextMenu(int contextMenu) {
        this.contextMenu = contextMenu;
    }

    public void refresh() {
        switch (contextMenu) {
            case CONTENT_VIEWS_CONTEXT_MENU:
                multipleSelection = selectedNodes;
                if (selectedNodes != null && selectedNodes.size() == 1) {
                    singleSelection = selectedNodes.get(0);
                } else {
                    singleSelection = null;
                }
                break;
            case REPOSITORY_TABS_CONTEXT_MENU:
                multipleSelection = new ArrayList<GWTJahiaNode>();
                if (mainNode != null) {
                    multipleSelection.add(mainNode);
                }
                singleSelection = mainNode;
                break;
            case MAIN_AREA_CONTEXT_MENU:
            default:
                if (selectedNodes != null) {
                    multipleSelection = selectedNodes;
                    if (selectedNodes.size() == 1) {
                        singleSelection = selectedNodes.get(0);
                    } else {
                        singleSelection = null;
                    }
                } else {
                    multipleSelection = new ArrayList<GWTJahiaNode>();
                    if (mainNode != null) {
                        multipleSelection.add(mainNode);
                    }
                    singleSelection = mainNode;
                }
        }


        writeable = true;
        parentWriteable = true;
        lockable = true;
        locked = true;
        file = true;
        zip = true;
        image = true;

        for (GWTJahiaNode node : multipleSelection) {
            writeable &= node.isWriteable();
            if (node.getParent() != null) {
                parentWriteable &= ((GWTJahiaNode) node.getParent()).isWriteable();
            } else {
                parentWriteable = false;
            }
            lockable &= node.isLockable();
            locked &= node.isLocked();

            file &= node.isFile();

            int extIndex = node.getName().lastIndexOf(".");
            if (extIndex <= 0 || !node.getName().substring(extIndex).equalsIgnoreCase(".zip")) {
                zip = false;
            }

            image = node.getNodeTypes().contains("jmix:image");
        }

        if (singleSelection != null) {
            pasteAllowed = CopyPasteEngine.getInstance().canCopyTo(singleSelection);
        } else {
            pasteAllowed = false;
        }
    }

    public GWTJahiaNode getMainNode() {
        return mainNode;
    }

    public GWTJahiaNode getSingleSelection() {
        return singleSelection;
    }

    public List<GWTJahiaNode> getMultipleSelection() {
        return multipleSelection;
    }

    public boolean isPasteAllowed() {
        return pasteAllowed;
    }

    public boolean isWriteable() {
        return writeable;
    }

    public boolean isParentWriteable() {
        return parentWriteable;
    }

    public boolean isLockable() {
        return lockable;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isFile() {
        return file;
    }

    public boolean isZip() {
        return zip;
    }

    public boolean isImage() {
        return image;
    }
}
