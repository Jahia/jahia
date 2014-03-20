/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.ajax.gwt.client.widget;

import org.jahia.ajax.gwt.client.data.node.GWTBitSet;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ktlili
 * Date: Oct 1, 2009
 * Time: 3:38:07 PM
 */
public class LinkerSelectionContext {
    public static final int BOTH = 1;
    public static final int MAIN_NODE_ONLY = 2;
    public static final int SELECTED_NODE_ONLY = 3;

    private GWTJahiaNode mainNode;
    private List<GWTJahiaNode> selectedNodes;

    private GWTJahiaNode singleSelection;
    private List<GWTJahiaNode> multipleSelection;
    private GWTJahiaNode parent;

    private boolean isSecondarySelection;

    private boolean pasteAllowed;

    private GWTBitSet permissions;
    private boolean isRootNode;
    private boolean parentWriteable;
    private boolean lockable;
    private boolean locked;

    private boolean file;
    private boolean zip;
    private boolean image;

    public void setMainNode(GWTJahiaNode selectedTreeNode) {
        this.mainNode = selectedTreeNode;
    }

    public void setSelectedNodes(List<GWTJahiaNode> selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

    public void refresh(int context) {
        switch (context) {
            case MAIN_NODE_ONLY:
                multipleSelection = new ArrayList<GWTJahiaNode>();
                if (mainNode != null) {
                    multipleSelection.add(mainNode);
                }
                singleSelection = mainNode;
                isSecondarySelection = false;
                break;
            case SELECTED_NODE_ONLY:
                if (selectedNodes != null) {
                    multipleSelection = selectedNodes;
                    if (selectedNodes.size() == 1) {
                        singleSelection = selectedNodes.get(0);
                    } else {
                        singleSelection = null;
                    }
                    isSecondarySelection = false;
                }
                break;
            case BOTH:
            default:
                if (selectedNodes != null) {
                    multipleSelection = selectedNodes;
                    if (selectedNodes.size() == 1) {
                        singleSelection = selectedNodes.get(0);
                    } else if (selectedNodes.size() == 0) {
                        singleSelection = mainNode;
                        if (mainNode != null) {
                            multipleSelection = new ArrayList<GWTJahiaNode>();                            
                            multipleSelection.add(mainNode);
                        }                        
                    } else {
                        singleSelection = null;
                    }
                    isSecondarySelection = false;
                } else {
                    multipleSelection = new ArrayList<GWTJahiaNode>();
                    if (mainNode != null) {
                        multipleSelection.add(mainNode);
                    }
                    singleSelection = mainNode;
                    isSecondarySelection = true;
                }
                break;                
        }


        permissions = null;
        isRootNode = false;
        parentWriteable = true;
        parent = null;
        lockable = true;
        locked = true;
        file = true;
        zip = true;
        image = true;

        for (GWTJahiaNode node : multipleSelection) {
            if (permissions == null) {
                permissions = node.getPermissions();
            } else {
                permissions = (GWTBitSet) permissions.clone();
                permissions.and(node.getPermissions());
            }
            if (node.getParent() != null) {
                parent = ((GWTJahiaNode) node.getParent());
            } else if (mainNode != null) {
                parent = mainNode;
            }
            parentWriteable = parent != null && PermissionsUtils.isPermitted("jcr:addChildNodes",  parent) && !parent.isLocked();
            isRootNode |= node.get("isRootNode") != null;
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

    public List<GWTJahiaNode> getSelectedNodes() {
        return selectedNodes;
    }

    public GWTJahiaNode getSingleSelection() {
        return singleSelection;
    }

    public List<GWTJahiaNode> getMultipleSelection() {
        return multipleSelection;
    }

    public boolean isSecondarySelection() {
        return isSecondarySelection;
    }

    public GWTJahiaNode getParent() {
        return parent;
    }

    public boolean isPasteAllowed() {
        return pasteAllowed;
    }

    public GWTBitSet getSelectionPermissions() {
        return permissions;
    }

    public boolean isRootNode() {
        return isRootNode;
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
