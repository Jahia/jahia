/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowType;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineCards;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineContainer;
import org.jahia.ajax.gwt.client.widget.contentengine.EnginePanel;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowActionDialog;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
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
            JahiaContentManagementService.App.getInstance().getNodes(Arrays.asList(singleSelection.getPath()),
                    Arrays.asList(GWTJahiaNode.WORKFLOW_INFOS), new BaseAsyncCallback<List<GWTJahiaNode>>() {
                        public void onSuccess(List<GWTJahiaNode> result) {
                            Map<String, GWTJahiaWorkflowInfo> workflowInfos = result.get(0).getWorkflowInfos();
                            if (workflowInfos != null && workflowInfos.keySet().size() > 0) {
                                Set<String> workflowTypes = workflowInfos.keySet();
                                final Menu menu = new Menu();
                                for (String locale : workflowTypes) {
                                    if (locale.equals(JahiaGWTParameters.getLanguage())) {
                                        GWTJahiaWorkflowInfo workflowInfo = workflowInfos.get(locale);
                                        Map<GWTJahiaWorkflowType, GWTJahiaWorkflowDefinition> possibleWorkflows = workflowInfo.getPossibleWorkflows();
                                        for (Map.Entry<GWTJahiaWorkflowType, GWTJahiaWorkflowDefinition> entry : possibleWorkflows.entrySet()) {
                                            if (!entry.getKey().getName().toLowerCase().endsWith("publish")) {
                                                final GWTJahiaWorkflowDefinition value = entry.getValue();
                                                MenuItem item = new MenuItem(value.getDisplayName());
                                                item.addSelectionListener(new SelectionListener<MenuEvent>() {
                                                    @Override
                                                    public void componentSelected(MenuEvent ce) {
                                                        EngineContainer container = new EnginePanel();
                                                        EngineContainer cards = new EngineCards(container, linker);
                                                        new WorkflowActionDialog(singleSelection.getPath(),
                                                                value.getDisplayName(), value, linker, null, cards);
                                                        cards.showEngine();
                                                    }
                                                });
                                                menu.add(item);
                                            }
                                        }
                                    }
                                }
                                if (menu.getItemCount() > 0) {
                                    setSubMenu(menu);
                                    setEnabled(true);
                                }
                            }
                        }
                    });
        }
    }
}
