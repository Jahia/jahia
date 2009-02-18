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

package org.jahia.ajax.gwt.client.util.nodes.actions;

import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import org.jahia.ajax.gwt.client.util.Formatter;

/**
 * This represents an item of the menubar, toolbar, or contextmenu.
 * The action to trigger has to be called within the onSelection method.
 *
 * User: rfelden
 * Date: 7 janv. 2009 - 12:25:42
 */
public abstract class FileActionItem implements FileActionItemItf {

    private TextToolItem textToolitem = null ;
    private MenuItem menuItem = null ;
    private MenuItem contextMenuItem = null ;

    public FileActionItem(String text, String style) {
        textToolitem = new TextToolItem();
        textToolitem.setIconStyle(style);
        textToolitem.setToolTip(text);
        SelectionListener<ComponentEvent> selectionListener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                onSelection();
            }
        };
        textToolitem.addSelectionListener(selectionListener);
        menuItem = new MenuItem(text, style, selectionListener);
        contextMenuItem = new MenuItem(text, style, selectionListener);
    }

    public void setEnabled(boolean enabled) {
        Formatter.setTextToolItemEnabled(textToolitem, enabled);
        Formatter.setMenuItemEnabled(menuItem, enabled);
        Formatter.setMenuItemEnabled(contextMenuItem, enabled);
    }

    public ToolItem getTextToolitem() {
        return textToolitem;
    }

    public Item getMenuItem() {
        return menuItem;
    }

    public Item getContextMenuItem() {
        return contextMenuItem;
    }

    public abstract void onSelection() ;

    public abstract void enableOnConditions(boolean treeSelection,
                                            boolean tableSelection,
                                            boolean writable,
                                            boolean parentWritable,
                                            boolean singleFile,
                                            boolean singleFolder,
                                            boolean pasteAllowed,
                                            boolean lockable,
                                            boolean isZip,
                                            boolean isImage,
                                            boolean isMount) ;

}