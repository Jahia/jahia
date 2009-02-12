/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.filemanagement.client.ui;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.Events;
import org.jahia.ajax.gwt.tripanelbrowser.client.BrowserLinker;
import org.jahia.ajax.gwt.filemanagement.client.model.GWTJahiaNode;
import org.jahia.ajax.gwt.filemanagement.client.util.CopyPasteEngine;
import org.jahia.ajax.gwt.filemanagement.client.util.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.filemanagement.client.util.actions.FileActionItemGroup;
import org.jahia.ajax.gwt.filemanagement.client.util.actions.FileActionItemItf;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 8 juil. 2008 - 11:42:43
 */
public class FileListContextMenu extends Menu {

    public FileListContextMenu(final BrowserLinker linker, final ManagerConfiguration config) {
        super() ;

        // add all items found in the defined menus
        for (int i=0; i<config.getGroupedItems().size(); i++) {
            FileActionItemGroup group = config.getGroupedItems().get(i) ;
            if (i > 0) {
                add(new SeparatorMenuItem()) ;
            }
            for (FileActionItemItf item: group.getItems()) {
                add(item.getContextMenuItem()) ;
            }
        }

        addListener(Events.BeforeShow, new Listener() {
            public void handleEvent(BaseEvent baseEvent) {
                GWTJahiaNode leftTreeSelection = null ;
                List<GWTJahiaNode> topTableSelection = null ;
                if (linker != null) {
                    leftTreeSelection = (GWTJahiaNode) linker.getTreeSelection() ;
                    topTableSelection = (List<GWTJahiaNode>) linker.getTableSelection() ;
                }
                boolean isTreeSelection = leftTreeSelection != null ;
                boolean isParentWriteable = (isTreeSelection) ? leftTreeSelection.isWriteable() : false;
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
                    isTableSelection = true ;
                    isWritable = true;
                    isLockable = true;
                    for (GWTJahiaNode gwtJahiaNode : topTableSelection) {
                        isWritable &= gwtJahiaNode.isWriteable();
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
                for (FileActionItemGroup group: config.getGroupedItems()) {
                    for (FileActionItemItf item: group.getItems()) {
                        item.enableOnConditions(isTreeSelection, isTableSelection, isWritable, isParentWriteable, isSingleFile, isSingleFolder, isPasteAllowed, isLockable, isZip, isImage, isMount);
                    }
                }
            }
        });
    }

}
