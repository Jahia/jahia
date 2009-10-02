package org.jahia.ajax.gwt.client.widget;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;

import java.util.List; /**
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
 **/

/**
 * User: ktlili
 * Date: Oct 1, 2009
 * Time: 3:38:07 PM
 */
public class LinkerSelectionContext {
    private GWTJahiaNode selectedTreeNode;
    private List<GWTJahiaNode> selectedNodes;
    private boolean leftTreeSelection;
    private boolean tableSelection;
    private boolean writeable;
    private boolean deleteable;
    private boolean parentWriteable;
    private boolean singleFile;
    private boolean singleFolder;
    private boolean pasteAllowed;
    private boolean lockable;
    private boolean locked;
    private boolean zip;
    private boolean image;
    public boolean mount;


    public void setTreeNodeSelection(GWTJahiaNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
    }

    public void setSelectedNodes(List<GWTJahiaNode> selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

    public void refresh() {
        leftTreeSelection = selectedTreeNode != null;
        parentWriteable = (leftTreeSelection) ? (selectedTreeNode).isWriteable() && !selectedTreeNode.isLocked() : false;
        writeable = false;
        deleteable = false;
        lockable = false;
        locked = false;
        singleFile = false;
        singleFolder = false;
        pasteAllowed = leftTreeSelection ? CopyPasteEngine.getInstance().canCopyTo(selectedTreeNode) : false;
        zip = false;
        image = false;
        tableSelection = false;
        mount = false;
        if (selectedNodes != null && selectedNodes.size() > 0) {
            if (selectedTreeNode != null) {
                leftTreeSelection = true;
            }
            if (!leftTreeSelection) {
                GWTJahiaNode parent = (GWTJahiaNode) selectedNodes.get(0).getParent();
                if (parent != null) {
                    parentWriteable = parent.isWriteable();
                }
            }
            tableSelection = true;
            writeable = true;
            deleteable = true;
            lockable = true;
            locked = true;
            for (GWTJahiaNode gwtJahiaNode : selectedNodes) {
                writeable &= gwtJahiaNode.isWriteable() && !gwtJahiaNode.isLocked();
                deleteable &= gwtJahiaNode.isDeleteable() && !gwtJahiaNode.isLocked();
                lockable &= gwtJahiaNode.isLockable();
                locked &= gwtJahiaNode.isLocked();
            }
            if (selectedNodes.size() == 1) {
                singleFile = selectedNodes.get(0).isFile();
                singleFolder = !singleFile;
            }
            if (singleFolder) {
                mount = selectedNodes.get(0).getInheritedNodeTypes().contains("jnt:mountPoint") || selectedNodes.get(0).getNodeTypes().contains("jnt:mountPoint");
            }
            if (!leftTreeSelection) {
                if (singleFolder) {
                    pasteAllowed = CopyPasteEngine.getInstance().canCopyTo((selectedNodes).get(0));
                } else {
                    pasteAllowed = CopyPasteEngine.getInstance().canCopyTo((GWTJahiaNode) (selectedNodes).get(0).getParent());
                }
            }
            int extIndex = selectedNodes.get(0).getName().lastIndexOf(".");
            if (extIndex > 0 && selectedNodes.get(0).getName().substring(extIndex).equalsIgnoreCase(".zip")) {
                zip = true;
            }
            image = selectedNodes.get(0).getNodeTypes().contains("jmix:image");
        }

    }

    public GWTJahiaNode getSelectedTreeNode() {
        return selectedTreeNode;
    }

    public List<GWTJahiaNode> getSelectedNodes() {
        return selectedNodes;
    }

    public boolean isLeftTreeSelection() {
        return leftTreeSelection;
    }

    public boolean isTableSelection() {
        return tableSelection;
    }

    public boolean isWriteable() {
        return writeable;
    }

    public boolean isDeleteable() {
        return deleteable;
    }

    public boolean isParentWriteable() {
        return parentWriteable;
    }

    public boolean isSingleFile() {
        return singleFile;
    }

    public boolean isSingleFolder() {
        return singleFolder;
    }

    public boolean isPasteAllowed() {
        return pasteAllowed;
    }

    public boolean isLockable() {
        return lockable;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isZip() {
        return zip;
    }

    public boolean isImage() {
        return image;
    }

    public boolean isMount() {
        return mount;
    }
}
