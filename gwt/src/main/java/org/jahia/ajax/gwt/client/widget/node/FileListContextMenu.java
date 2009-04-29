/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.node;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.Events;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.nodes.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.nodes.actions.FileActionItemGroup;
import org.jahia.ajax.gwt.client.util.nodes.actions.FileActionItemItf;

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
