/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowType;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
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
     * @param selection the current selection context
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

    protected transient String workflowType = "publish";
    protected transient boolean checkForUnpublication = false;
    protected transient boolean allSubTree = false;

    public void handleNewLinkerSelection() {
        setEnabled(false);
        LinkerSelectionContext ctx = linker.getSelectionContext();
        if (ctx.getMultipleSelection() != null
                && ctx.getMultipleSelection().size() > 1 && hasPermission(ctx.getSelectionPermissions())) {
            if (!isChildOfMarkedForDeletion(ctx)) {
                setEnabled(true);
                updateTitle(getGwtToolbarItem().getTitle());
            }
        } else {
            GWTJahiaNode gwtJahiaNode = ctx.getSingleSelection();
            if (gwtJahiaNode != null && !isChildOfMarkedForDeletion(ctx) && Boolean.TRUE.equals(gwtJahiaNode.get("supportsPublication")) && hasPermission(gwtJahiaNode)) {
                setEnabled(true);

                if (checkForUnpublication) {
                    GWTJahiaPublicationInfo publicationInfo = gwtJahiaNode.getAggregatedPublicationInfo() != null ? gwtJahiaNode
                            .getAggregatedPublicationInfo() : gwtJahiaNode.getQuickPublicationInfo();
                    if (publicationInfo != null && !publicationInfo.isUnpublishable()) {
                        setEnabled(false);
                    }
                } else if (gwtJahiaNode.getAggregatedPublicationInfo() != null) {
                    GWTJahiaPublicationInfo info = gwtJahiaNode.getAggregatedPublicationInfo();
                    GWTJahiaWorkflowDefinition def = null;
                    if (gwtJahiaNode.getWorkflowInfo() != null) {
                        def = gwtJahiaNode.getWorkflowInfo().getPossibleWorkflows().get(new GWTJahiaWorkflowType(workflowType));
                    }

                    setEnabled(info.isPublishable() && (def != null || info.isAllowedToPublishWithoutWorkflow()));
                }

                if (gwtJahiaNode.isFile() || gwtJahiaNode.isNodeType("nt:folder")) {
                    updateTitle(getGwtToolbarItem().getTitle() + " " + gwtJahiaNode.getDisplayName());
                } else {
                    updateTitle(getGwtToolbarItem().getTitle() + " " + gwtJahiaNode.getDisplayName() + " - " +
                            JahiaGWTParameters.getLanguageDisplayName());
                }
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


    @Override
    public void onComponentSelection() {
        LinkerSelectionContext ctx = linker.getSelectionContext();
        if (!linker.getSelectionContext().getMultipleSelection().isEmpty()) {
            final List<String> uuids = new ArrayList<String>();
            List<GWTJahiaNode> jahiaNodes = linker.getSelectionContext().getMultipleSelection();
            if (jahiaNodes.size() > 1) {
                for (GWTJahiaNode jahiaNode : jahiaNodes) {
                    uuids.add(jahiaNode.getUUID());
                }
            } else {
                uuids.add(linker.getSelectionContext().getSingleSelection().getUUID());
            }
            linker.loading(Messages.get("label.gettingPublicationInfo", "Getting publication information"));
            JahiaContentManagementService.App.getInstance().getPublicationInfo(uuids, allSubTree, checkForUnpublication, new BaseAsyncCallback<List<GWTJahiaPublicationInfo>>() {
                public void onApplicationFailure(Throwable caught) {
                    linker.loaded();
                    com.google.gwt.user.client.Window.alert("Cannot get status: " + caught.getMessage());
                }

                public void onSuccess(List<GWTJahiaPublicationInfo> result) {
                    linker.loaded();
                    callback(result);
                }
            });
        }
    }


    protected void callback(List<GWTJahiaPublicationInfo> result) {
        if (result.isEmpty()) {
            MessageBox.info(Messages.get("label.publish", "Publication"), Messages.get("label.publication.nothingToPublish", "Nothing to publish"), null);
        } else {
            Map<Integer, List<String>> unpublishable = new HashMap<Integer, List<String>>();
            for (GWTJahiaPublicationInfo info : result) {
                Integer status = info.getStatus();
                if (status == GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE) {
                    if (!unpublishable.containsKey(status)) {
                        unpublishable.put(status, new ArrayList<String>());
                    }
                    unpublishable.get(status).add(info.getTitle());
                }
            }
            if (unpublishable.isEmpty()) {
                PublicationWorkflow.create(result, linker, checkForUnpublication);
            } else {
                String message = "";
                for (Map.Entry<Integer, List<String>> entry : unpublishable.entrySet()) {
                    Integer status = entry.getKey();
                    List<String> values = entry.getValue();
                    final String labelKey = GWTJahiaPublicationInfo.statusToLabel.get(status);
                    message += Messages.get("label.publication." + labelKey, labelKey) + " : " + values.get(0);
                    if (values.size() > 10) {
                        for (int i = 1; i < 10; i++) {
                            message += ", " + values.get(i);
                        }
                        message += ", ...";
                    } else {
                        for (int i = 1; i < values.size(); i++) {
                            message += ", " + values.get(i);
                        }
                    }
                }
                MessageBox.info(Messages.get("label.publish", "Publication"), message, null);
            }
        }
    }
}
