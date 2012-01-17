/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflow;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowType;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineContainer;
import org.jahia.ajax.gwt.client.widget.contentengine.EnginePanel;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineWindow;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowActionDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:58:56 PM
 */
@SuppressWarnings("serial")
public class PublishActionItem extends BaseActionItem {
    
    /**
     * Returns <code>true</code> if the selected item (single-selection case) is locked by the marked for deletion operation on its parent.
     * In a multi-selection mode, the condition should be fulfilled for all selected items. If at least one of the selected items is not in
     * marked for deletion state this method returns false.
     * 
     * @param selection
     *            the current selection context
     * @return <code>true</code> if the selected item (single-selection case) is locked by the marked for deletion operation on its parent.
     *         In a multi-selection mode, the condition should be fulfilled for all selected items. If at least one of the selected items is
     *         not in marked for deletion state this method returns false.
     */
    public static boolean isChildOfMarkedForDeletion(LinkerSelectionContext selection) {
        if (selection.getMultipleSelection().size() == 0) {
            return false;
        }

        boolean markedForDeletion = false;
        for (GWTJahiaNode node : selection.getMultipleSelection()) {
            markedForDeletion = node.getNodeTypes().contains("jmix:markedForDeletion")
                    && !node.getNodeTypes().contains("jmix:markedForDeletionRoot");
            if (!markedForDeletion) {
                break;
            }
        }

        return markedForDeletion;
    }
    
    protected transient boolean allSubTree = false;
    protected transient GWTJahiaNode gwtJahiaNode;

    protected transient GWTJahiaWorkflow wf;

    public void handleNewLinkerSelection() {
        LinkerSelectionContext ctx = linker.getSelectionContext();
        if (ctx.getMultipleSelection() != null
                && ctx.getMultipleSelection().size() > 1) {
            if (!isChildOfMarkedForDeletion(ctx)) {
                setEnabled(true);
                updateTitle(Messages.get("label.publish.selected.items"));
            }
        } else {
            gwtJahiaNode = ctx.getSingleSelection();
            if (gwtJahiaNode != null && gwtJahiaNode.getWorkflowInfo() != null && !isChildOfMarkedForDeletion(ctx) && Boolean.TRUE.equals(gwtJahiaNode.get("supportsPublication"))) {
                wf = gwtJahiaNode.getWorkflowInfo().getActiveWorkflows().get(new GWTJahiaWorkflowType("publish"));
                if (wf != null) {
                    if (!wf.getAvailableTasks().isEmpty()) {
                        setEnabled(true);
                        updateTitle(wf.getAvailableTasks().get(0).getDisplayName() + " : " + gwtJahiaNode.getDisplayName());
                    } else {
                        setEnabled(false);
                        updateTitle(Messages.get("label.workflow.started", "Workflow started") + " : " +wf.getDefinition().getDisplayName());
                    }
                } else {
                    wf = null;
                    GWTJahiaPublicationInfo info = gwtJahiaNode.getAggregatedPublicationInfo();

                    setEnabled(GWTJahiaPublicationInfo.canPublish(gwtJahiaNode, info,
                            JahiaGWTParameters.getLanguage()));
                    if(gwtJahiaNode.isFile() || gwtJahiaNode.isNodeType("nt:folder")) {
                        updateTitle(getGwtToolbarItem().getTitle() + " " + gwtJahiaNode.getDisplayName());
                    } else {
                        updateTitle(getGwtToolbarItem().getTitle() + " " + gwtJahiaNode.getDisplayName() + " - " +
                                    JahiaGWTParameters.getLanguageDisplayName());
                    }
                }
            } else {
                setEnabled(false);
            }
        }
    }

    /**
     * Init the action item.
     *
     * @param gwtToolbarItem
     * @param linker
     */
    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        setEnabled(false);
    }

    public void onComponentSelection() {
        if (wf == null) {
            if (gwtJahiaNode != null) {
                final List<String> uuids = new ArrayList<String>();
                List<GWTJahiaNode> jahiaNodes = linker.getSelectionContext().getMultipleSelection();
                if (jahiaNodes.size() > 1) {
                    for (GWTJahiaNode jahiaNode : jahiaNodes) {
                        uuids.add(jahiaNode.getUUID());
                    }
                } else {
                    uuids.add(gwtJahiaNode.getUUID());
                }
                linker.loading(Messages.get("label.gettingPublicationInfo", "Getting publication information"));
                JahiaContentManagementService.App.getInstance().getPublicationInfo(uuids, allSubTree, false, new BaseAsyncCallback<List<GWTJahiaPublicationInfo>>() {
                            public void onApplicationFailure(Throwable caught) {
                                linker.loaded();
                                Window.alert("Cannot get status: " + caught.getMessage());
                            }

                            public void onSuccess(List<GWTJahiaPublicationInfo> result) {
                                linker.loaded();
                                if (result.isEmpty()) {
                                    MessageBox.info(Messages.get("label.publish", "Publication"), Messages.get("label.publication.nothingToPublish", "Nothing to publish"), null);
                                } else {
                                    PublicationWorkflow.create(result, linker);
                                }
                            }
                        });
            }
        } else {
            EngineContainer container;
            if (linker instanceof ManagerLinker) {
                container = new EngineWindow();
            } else {
                container = new EnginePanel();
            }
            new WorkflowActionDialog(wf, wf.getAvailableTasks().get(0), linker, wf.getCustomWorkflowInfo(), container);
            container.showEngine();
        }
    }
}
