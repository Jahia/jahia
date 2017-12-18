/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
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
package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarMenu;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbarMenu;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * User: toto
 * Date: Nov 19, 2010
 * Time: 10:33:40 AM
 * 
 */
public class ToolbarHeader extends Header {
    private HorizontalPanel horizontalPanel;
    private List<ActionItem> actionItems = new ArrayList<ActionItem>();
    private HorizontalPanel leftWidgetPanel;

    public ToolbarHeader() {
        super();
        setHeight("22");
        leftWidgetPanel = new HorizontalPanel();
        leftWidgetPanel.setVerticalAlign(Style.VerticalAlignment.MIDDLE);
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        leftWidgetPanel.addStyleName("x-panel-toolbar");
        leftWidgetPanel.setLayoutOnChange(true);
        leftWidgetPanel.setStyleAttribute("float", "left");
        leftWidgetPanel.getAriaSupport().setPresentation(true);
        leftWidgetPanel.setVisible(true);

        leftWidgetPanel.render(getElement());
        getElement().insertFirst(leftWidgetPanel.getElement());
    }

    public void removeAllTools() {
        if (horizontalPanel != null) {
            super.removeTool(horizontalPanel);
            horizontalPanel = null;
        }
        if (leftWidgetPanel != null) {
            leftWidgetPanel.removeAll();
        }
        if (actionItems != null) {
            actionItems.clear();
        }
    }

    @Override
    public void addTool(Component tool) {
        addTool(tool, false);
    }

    public void addTool(Component tool, boolean left) {
        if (left) {
            leftWidgetPanel.add(tool);
            leftWidgetPanel.layout();
        } else {
            if (horizontalPanel == null) {
                horizontalPanel = new HorizontalPanel();
                horizontalPanel.addStyleName("x-toolbar-header");
                horizontalPanel.setVerticalAlign(Style.VerticalAlignment.MIDDLE);
            }
            horizontalPanel.add(tool);
            horizontalPanel.layout();
        }
    }


    @Override
    public void removeTool(Component tool) {
        horizontalPanel.remove(tool);
        horizontalPanel.layout();
    }

    public void attachTools() {
        if (horizontalPanel != null && horizontalPanel.getParent() == null) {
            super.addTool(horizontalPanel);
        }
    }

    public void addItem(Linker linker, GWTJahiaToolbarItem gwtToolbarItem) {
        boolean left = gwtToolbarItem.getProperties().get("position") != null && gwtToolbarItem.getProperties().get("position").getValue().equals("left");
        addItem(linker, gwtToolbarItem, left);
    }

    private void addItem(Linker linker, GWTJahiaToolbarItem gwtToolbarItem, boolean left) {
        if (gwtToolbarItem instanceof GWTJahiaToolbarMenu) {
            GWTJahiaToolbarMenu gwtToolbarMenu = (GWTJahiaToolbarMenu) gwtToolbarItem;
            ActionToolbarMenu menu = new ActionToolbarMenu(linker);
            menu.setActionItems(actionItems);

            for (GWTJahiaToolbarItem subItem : gwtToolbarMenu.getGwtToolbarItems()) {
                menu.addItem(subItem);
            }

            Button menuToolItem = new Button(gwtToolbarMenu.getItemsGroupTitle());
            menuToolItem.setBorders(false);
            String minIconStyle = gwtToolbarMenu.getIcon();
            if (minIconStyle != null) {
                menuToolItem.setIcon(ToolbarIconProvider.getInstance().getIcon(minIconStyle));
            }
            menuToolItem.setMenu(menu);
            addTool(menuToolItem, left);
        } else {
            final ActionItem actionItem = gwtToolbarItem.getActionItem();
            actionItems.add(actionItem);
            if (actionItem != null) {
                actionItem.init(gwtToolbarItem, linker);
                if (actionItem.getCustomItem() != null) {
                    addTool(actionItem.getCustomItem(), left);
                } else {
                    addTool(actionItem.getTextToolItem(), left);
                }
            }
        }
    }

    public void handleNewLinkerSelection() {
        for (ActionItem item : actionItems) {
            try {
                item.handleNewLinkerSelection();
            } catch (Exception e) {
            }
        }
    }

    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        for (ActionItem item : actionItems) {
            try {
                item.handleNewMainNodeLoaded(node);
            } catch (Exception e) {
            }
        }
    }
}
