/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
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
        addStyleName("toolbar-header");
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        leftWidgetPanel.addStyleName("x-panel-toolbar");
        leftWidgetPanel.addStyleName("toolbar-left-container");
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
                horizontalPanel.addStyleName("toolbar-right-container");
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
            menu.addStyleName("action-bar-menu");
            menu.addStyleName("menu-"+gwtToolbarMenu.getClassName());
            menu.setActionItems(actionItems);

            for (GWTJahiaToolbarItem subItem : gwtToolbarMenu.getGwtToolbarItems()) {
                menu.addItem(subItem);
            }

            Button menuToolItem = new Button(gwtToolbarMenu.getItemsGroupTitle());
            menuToolItem.addStyleName("action-bar-menu-item");
            gwtToolbarItem.addClasses(menuToolItem);

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
