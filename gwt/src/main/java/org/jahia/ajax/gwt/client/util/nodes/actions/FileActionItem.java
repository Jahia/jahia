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