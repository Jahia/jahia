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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowType;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineCards;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineContainer;
import org.jahia.ajax.gwt.client.widget.contentengine.EnginePanel;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineWindow;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.publication.UnpublicationWorkflow;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowActionDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:58:58 PM
 */
public class UnpublishActionItem extends BaseActionItem {
    public void onComponentSelection() {
        final GWTJahiaNode selectedNode = linker.getSelectionContext().getSingleSelection();
        if (selectedNode != null) {
            linker.loading(Messages.get("label.content.unpublishing", "Unpublishing"));
            GWTJahiaWorkflowInfo workflowInfo = selectedNode.getWorkflowInfo();
            if (workflowInfo != null) {
                final GWTJahiaWorkflowDefinition workflowDefinition = workflowInfo.getPossibleWorkflows().get(
                        new GWTJahiaWorkflowType("unpublish"));

                final List<String> uuids = new ArrayList<String>();
                List<GWTJahiaNode> jahiaNodes = linker.getSelectionContext().getMultipleSelection();
                if (jahiaNodes.size() > 1) {
                    for (GWTJahiaNode jahiaNode : jahiaNodes) {
                        uuids.add(jahiaNode.getUUID());
                    }
                } else {
                    uuids.add(selectedNode.getUUID());
                }
                linker.loading(Messages.get("label.gettingPublicationInfo", "Getting publication information"));
                JahiaContentManagementService.App.getInstance().getPublicationInfo(uuids, true, true, new BaseAsyncCallback<List<GWTJahiaPublicationInfo>>() {
                            public void onSuccess(List<GWTJahiaPublicationInfo> result) {
                                linker.loaded();
                                EngineContainer container;
                                if (linker instanceof ManagerLinker) {
                                    container = new EngineWindow();
                                } else {
                                    container = new EnginePanel();
                                }
                                EngineContainer cards = new EngineCards(container, linker);
                                new WorkflowActionDialog(selectedNode.getPath(), Messages.getWithArgs("label.workflow.start.message",
                                        "{0} started by {1} on {2} - {3} content items involved",
                                        new Object[]{workflowDefinition.getDisplayName(),JahiaGWTParameters.getCurrentUser(), DateTimeFormat.getFormat(
                                                DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT).format(new Date()),result.size()})
                                         , workflowDefinition, linker,
                                        new UnpublicationWorkflow(result), cards);
                                cards.showEngine();
                            }

                            public void onApplicationFailure(Throwable caught) {
                                linker.loaded();
                                Window.alert("Cannot get status: " + caught.getMessage());
                            }
                        });


            } else {
                JahiaContentManagementService.App.getInstance().unpublish(Arrays.asList(selectedNode.getUUID()),
                        new BaseAsyncCallback<Object>() {
                            public void onApplicationFailure(Throwable caught) {
                                linker.loaded();
                                Log.error("Cannot publish", caught);
                                com.google.gwt.user.client.Window.alert("Cannot unpublish " + caught.getMessage());
                            }

                            public void onSuccess(Object result) {
                                linker.loaded();
                                Info.display(Messages.get("label.content.unpublished"), Messages.get(
                                        "label.content.unpublished"));
                                linker.refresh(EditLinker.REFRESH_ALL);
                            }
                        });
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
        super.init(gwtToolbarItem,
                linker);    //To change body of overridden methods use File | Settings | File Templates.
        setEnabled(false);
    }

    public void handleNewLinkerSelection() {
        GWTJahiaNode gwtJahiaNode = linker.getSelectionContext().getSingleSelection();
        if (gwtJahiaNode != null && gwtJahiaNode.getAggregatedPublicationInfos() != null && Boolean.TRUE.equals(gwtJahiaNode.get("supportsPublication"))) {
            GWTJahiaPublicationInfo info = gwtJahiaNode.getAggregatedPublicationInfo();
            setEnabled(!info.isLocked() && info.isCanPublish() &&
                       (info.getStatus() == GWTJahiaPublicationInfo.PUBLISHED ||
                        info.getStatus() == GWTJahiaPublicationInfo.MODIFIED));
            updateTitle(getGwtToolbarItem().getTitle() + " " + gwtJahiaNode.getDisplayName() + " - " +
                        JahiaGWTParameters.getLanguageDisplayName());
        } else {
            setEnabled(false);
        }
    }
}
