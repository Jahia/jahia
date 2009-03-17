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

package org.jahia.ajax.gwt.client.widget.node;

import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;
import org.jahia.ajax.gwt.client.util.nodes.actions.*;
import org.jahia.ajax.gwt.client.util.nodes.CopyPasteEngine;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 7 juil. 2008 - 14:17:12
 */
public class FileToolbar extends TopBar {

    private LayoutContainer m_component;

    private ManagerConfiguration configuration ;

    public FileToolbar(ManagerConfiguration config) {
        m_component = new LayoutContainer(new RowLayout());
        configuration = config ;

        ToolBar menus = new ToolBar();
        ToolBar shortcuts = new ToolBar();
        //menus.setHeight(21);

        // refresh item not bound to any configuration
        FileActionItem refresh = new FileActionItem(Messages.getResource("fm_refresh"), "fm-refresh") {
            public void onSelection() {
                getLinker().refreshTable();
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                // always enabled
                // setEnabled(treeSelection);
            }
        };

        for (FileActionItemItf item: config.getItems()) {
            shortcuts.add(item.getTextToolitem()) ;
        }
        shortcuts.add(new SeparatorToolItem()) ;
        shortcuts.add(refresh.getTextToolitem()) ;

        if (config.isEnableTextMenu() && config.getGroupedItems().size() > 0) {
            for (FileActionItemGroup group: config.getGroupedItems()) {
                Menu menu = new Menu() ;
                for (FileActionItemItf item: group.getItems()) {
                    menu.add(item.getMenuItem()) ;
                }
                TextToolItem mMenu = new TextToolItem(group.getGroupLabel()) ;
                mMenu.setMenu(menu);
                menus.add(mMenu) ;
            }

            // add the views menu (not part of the config)
            MenuItem list = new MenuItem(Messages.getResource("fm_list"), "fm-tableview", new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    setListView();
                }
            });
            MenuItem thumbs = new MenuItem(Messages.getResource("fm_icons"), "fm-iconview", new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    setThumbView();
                }
            });

            MenuItem detailedThumbs = new MenuItem(Messages.getResource("fm_icons_detailed"), "fm-iconview-detailed", new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    setDetailedThumbView();
                }
            });
            Menu menu = new Menu() ;
            menu.add(refresh.getMenuItem()) ;
            menu.add(new SeparatorMenuItem()) ;
            menu.add(list) ;
            menu.add(thumbs) ;
            menu.add(detailedThumbs) ;
            TextToolItem mMenu = new TextToolItem(Messages.getResource("fm_viewMenu")) ;
            mMenu.setMenu(menu);
            menus.add(mMenu) ;
            m_component.add(menus) ;
        }

        m_component.add(shortcuts);
        //menus.add(new FillToolItem());
    }

    // override to handle view switching
   /* protected void switchView(TextToolItem switchView) {
    }*/

    protected void setListView() {
    }

    protected void setThumbView() {
    }

    protected void setDetailedThumbView(){
    }

    public void handleNewSelection(Object leftTreeSelection, Object topTableSelectionEl) {
        List<GWTJahiaNode> topTableSelection = (List<GWTJahiaNode>) topTableSelectionEl;

        boolean isTreeSelection = leftTreeSelection != null ;
        boolean isParentWriteable = (isTreeSelection) ? ((GWTJahiaNode) leftTreeSelection).isWriteable() : false;
        boolean isWritable = false;
        boolean isLockable = false;
        boolean isSingleFile = false;
        boolean isSingleFolder = false;
        boolean isPasteAllowed = isTreeSelection ? CopyPasteEngine.getInstance().canCopyTo((GWTJahiaNode) leftTreeSelection) : false ;
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
                if (isSingleFolder) {
                    isPasteAllowed = CopyPasteEngine.getInstance().canCopyTo(topTableSelection.get(0)) ;
                } else {
                    isPasteAllowed = CopyPasteEngine.getInstance().canCopyTo(topTableSelection.get(0).getParent()) ;
                }
            }
            int extIndex = topTableSelection.get(0).getName().lastIndexOf(".") ;
            if (extIndex > 0 && topTableSelection.get(0).getName().substring(extIndex).equalsIgnoreCase(".zip")) {
                isZip = true ;
            }
            isImage = topTableSelection.get(0).getNodeTypes().contains("jmix:image") ;
        }
        for (FileActionItemGroup group: configuration.getGroupedItems()) {
            for (FileActionItemItf item: group.getItems()) {
                item.enableOnConditions(isTreeSelection, isTableSelection, isWritable, isParentWriteable, isSingleFile, isSingleFolder, isPasteAllowed, isLockable, isZip, isImage, isMount);
            }
        }
    }

    public Component getComponent() {
        return m_component;
    }
}
