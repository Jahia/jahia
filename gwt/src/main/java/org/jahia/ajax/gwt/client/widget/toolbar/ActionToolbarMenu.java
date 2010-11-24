package org.jahia.ajax.gwt.client.widget.toolbar;

import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarMenu;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;

import java.util.ArrayList;
import java.util.List;

/**
* Created by IntelliJ IDEA.
* User: toto
* Date: Aug 31, 2010
* Time: 7:35:40 PM
* 
*/
public class ActionToolbarMenu extends Menu implements ToolbarGroup {
    protected Linker linker;
    protected List<ActionItem> actionItems;

    public ActionToolbarMenu(Linker linker) {
        this.linker = linker;
    }

    public void setActionItems(List<ActionItem> actionItems) {
        this.actionItems = actionItems;
    }

    public void addItem(GWTJahiaToolbarItem gwtToolbarItem) {
        if (gwtToolbarItem instanceof GWTJahiaToolbarMenu) {
            MenuItem subMenu = new MenuItem();
            if (gwtToolbarItem.getIcon() != null) {
                subMenu.setIcon(ToolbarIconProvider.getInstance().getIcon(gwtToolbarItem.getIcon()));
            }
            subMenu.setText(gwtToolbarItem.getTitle());

            GWTJahiaToolbarMenu gwtToolbarMenu = (GWTJahiaToolbarMenu) gwtToolbarItem;

            ActionToolbarMenu menu = new ActionToolbarMenu(linker);
            menu.setActionItems(actionItems);
            for (GWTJahiaToolbarItem subItem : gwtToolbarMenu.getGwtToolbarItems()) {
                menu.addItem(subItem);
            }
            subMenu.setSubMenu(menu);

            add(subMenu);
        } else {
            final ActionItem actionItem = gwtToolbarItem.getActionItem();
            actionItems.add(actionItem);
            if (actionItem != null) {
                actionItem.init(gwtToolbarItem, linker);
                if (actionItem.getCustomItem() != null) {
                    add(actionItem.getCustomItem());
                } else if (ActionToolbar.isSeparator(gwtToolbarItem)) {
                    add(new SeparatorMenuItem());
                } else {
                    add(createActionItem(actionItem));
                }
            }
        }

    }

    protected Item createActionItem(ActionItem actionItem) {
        return actionItem.getMenuItem();
    }
}
