package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 4, 2009
 * Time: 4:23:46 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class EditActionItem {

    private Button textToolitem = null ;
    private MenuItem menuItem = null ;
    private MenuItem contextMenuItem = null ;

    public EditActionItem(String text, String style) {
        textToolitem = new Button(text);
        textToolitem.setIconStyle(style);
        textToolitem.setToolTip(text);
        textToolitem.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                onSelection();
            }
        });
        SelectionListener<MenuEvent> selectionListener = new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent event) {
                onSelection();
            }
        };
        menuItem = new MenuItem(text, selectionListener);
        menuItem.setIconStyle(style);
        contextMenuItem = new MenuItem(text, selectionListener);
        contextMenuItem.setIconStyle(style);
    }

    public void setEnabled(boolean enabled) {
        Formatter.setButtonEnabled(textToolitem, enabled);
        Formatter.setMenuItemEnabled(menuItem, enabled);
        Formatter.setMenuItemEnabled(contextMenuItem, enabled);
    }

    public Button getTextToolitem() {
        return textToolitem;
    }

    public Item getMenuItem() {
        return menuItem;
    }

    public Item getContextMenuItem() {
        return contextMenuItem;
    }

    public abstract void onSelection() ;

    public abstract void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) ;

}
