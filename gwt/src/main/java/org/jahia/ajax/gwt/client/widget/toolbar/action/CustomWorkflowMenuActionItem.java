/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowType;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineContainer;
import org.jahia.ajax.gwt.client.widget.contentengine.EnginePanel;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowActionDialog;

import java.util.Map;

/**
 * 
 * User: ktlili
 * Date: Jan 20, 2010
 * Time: 1:51:18 PM
 */
public class CustomWorkflowMenuActionItem extends BaseActionItem {

    public void init(GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        super.init(gwtToolbarItem, linker);
        setEnabled(false);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        final GWTJahiaNode singleSelection = lh.getSingleSelection();
        setEnabled(false);
        if (singleSelection != null) {

            GWTJahiaWorkflowInfo workflowInfo = singleSelection.getWorkflowInfo();
            Map<GWTJahiaWorkflowType, GWTJahiaWorkflowDefinition> possibleWorkflows = workflowInfo.getPossibleWorkflows();

            final Menu menu = new Menu();
            for (Map.Entry<GWTJahiaWorkflowType, GWTJahiaWorkflowDefinition> entry : possibleWorkflows.entrySet()) {
                if (!entry.getKey().getName().toLowerCase().endsWith("publish")) {
                    final GWTJahiaWorkflowDefinition value = entry.getValue();
                    MenuItem item = new MenuItem(value.getDisplayName());
                    item.addSelectionListener(new SelectionListener<MenuEvent>() {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            EngineContainer container = new EnginePanel();
                            new WorkflowActionDialog(singleSelection.getPath(),
                                    value.getDisplayName(), value, linker, null, container);
                            container.showEngine();
                        }
                    });
                    menu.add(item);
                }
            }
            if (menu.getItemCount() > 0) {
                setSubMenu(menu);
                setEnabled(true);
            }
        }
    }
}
