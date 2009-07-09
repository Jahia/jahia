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

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.MenuEvent;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActionItemGroup;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActionItemItf;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 8 juil. 2008 - 11:42:43
 */
public class ContentListContextMenu extends Menu {

    public ContentListContextMenu(final BrowserLinker linker, final ManagerConfiguration config) {
        super() ;

        // add all items found in the defined menus
        for (int i=0; i<config.getGroupedItems().size(); i++) {
            ContentActionItemGroup group = config.getGroupedItems().get(i) ;
            if (i > 0) {
                add(new SeparatorMenuItem()) ;
            }
            for (ContentActionItemItf item: group.getItems()) {
                add(item.getContextMenuItem()) ;
            }
        }

        addListener(Events.BeforeShow, new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent baseEvent) {
                GWTJahiaNode leftTreeSelection = null ;
                List<GWTJahiaNode> topTableSelection = null ;
                if (linker != null) {
                    leftTreeSelection = (GWTJahiaNode) linker.getTreeSelection() ;
                    topTableSelection = (List<GWTJahiaNode>) linker.getTableSelection() ;
                }
                boolean isTreeSelection = leftTreeSelection != null ;
                boolean isParentWriteable = (isTreeSelection) ? leftTreeSelection.isWriteable() && !leftTreeSelection.isLocked() : false;
                boolean isWritable = false;
                boolean isLockable = false;
                boolean isSingleFile = false;
                boolean isSingleFolder = false;
                boolean isPasteAllowed = isTreeSelection ? CopyPasteEngine.getInstance().canCopyTo(leftTreeSelection) : false ;
                boolean isZip = false ;
                boolean isImage = false ;
                boolean isTableSelection = false ;
                boolean isMount = false ;
                if (topTableSelection != null && topTableSelection.size() > 0) {
                    if (leftTreeSelection != null) {
                        isTreeSelection = true ;
                    }
                    if (!isTreeSelection) {
                        GWTJahiaNode parent = (GWTJahiaNode) topTableSelection.get(0).getParent();
                        if (parent != null) {
                            isParentWriteable = parent.isWriteable();
                        }
                    }
                    isTableSelection = true ;
                    isWritable = true;
                    isLockable = true;
                    for (GWTJahiaNode gwtJahiaNode : topTableSelection) {
                        isWritable &= gwtJahiaNode.isWriteable() && !gwtJahiaNode.isLocked();
                        isLockable &= gwtJahiaNode.isLockable();
                    }
                    if (topTableSelection.size() == 1) {
                        isSingleFile = topTableSelection.get(0).isFile();
                        isSingleFolder = !isSingleFile;
                    }
                    if (isSingleFolder) {
                        isMount = topTableSelection.get(0).getNodeTypes().contains("jnt:vfsMountPoint") ;
                    }
                    if (!isTreeSelection) {
                        isPasteAllowed = CopyPasteEngine.getInstance().canCopyTo(topTableSelection.get(0)) ;
                    }
                    int extIndex = topTableSelection.get(0).getName().lastIndexOf(".") ;
                    if (extIndex > 0 && topTableSelection.get(0).getName().substring(extIndex).equalsIgnoreCase(".zip")) {
                        isZip = true ;
                    }
                    isImage = topTableSelection.get(0).getNodeTypes().contains("jmix:image") ;
                }
                for (ContentActionItemGroup group: config.getGroupedItems()) {
                    for (ContentActionItemItf item: group.getItems()) {
                        item.enableOnConditions(isTreeSelection, isTableSelection, isWritable, isParentWriteable, isSingleFile, isSingleFolder, isPasteAllowed, isLockable, isZip, isImage, isMount);
                    }
                }
            }
        });
    }

}
