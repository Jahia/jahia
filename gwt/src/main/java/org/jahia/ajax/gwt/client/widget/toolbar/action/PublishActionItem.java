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

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflow;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowType;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.publication.PublicationStatusWindow;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowActionDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:58:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublishActionItem extends BaseActionItem {
    protected transient GWTJahiaWorkflow wf;
    protected transient boolean allSubTree = false;
    protected transient GWTJahiaNode gwtJahiaNode;

    public void onComponentSelection() {
        if (wf == null) {
            if (gwtJahiaNode != null) {
                final List<String> uuids = new ArrayList<String>();
//                for (GWTJahiaNode selectedNode : selectedNodes) {
                uuids.add(gwtJahiaNode.getUUID());
                //                if (ModuleHelper.getLinkedContentInfo().containsKey(selectedNode.getUUID())) {
                //                    uuids.addAll(ModuleHelper.getLinkedContentInfo().get(selectedNode.getUUID()));
                //                }
//                }
                linker.loading(Messages.get("label.gettingPublicationInfo", "Getting publication information"));
                JahiaContentManagementService.App.getInstance()
                        .getPublicationInfo(uuids, allSubTree, new BaseAsyncCallback<List<GWTJahiaPublicationInfo>>() {
                            public void onSuccess(List<GWTJahiaPublicationInfo> result) {
                                linker.loaded();

                                List<GWTJahiaPublicationInfo> filteredList = new ArrayList<GWTJahiaPublicationInfo>();
                                for (GWTJahiaPublicationInfo info : result) {
                                    if (info.getStatus() > GWTJahiaPublicationInfo.PUBLISHED) {
                                        filteredList.add(info);
                                    }
                                }
                                if (filteredList.isEmpty()) {
                                    MessageBox.info(Messages.get("label.publication", "Publication"),
                                            Messages.get("label.nothingToPublish", "Nothing to publish"), null);
                                } else {
                                    GWTJahiaWorkflowDefinition def =
                                            gwtJahiaNode.getWorkflowInfo().getPossibleWorkflows()
                                                    .get(new GWTJahiaWorkflowType("publish"));
                                    if (def != null) {
                                        WorkflowActionDialog wad = new WorkflowActionDialog(gwtJahiaNode, linker);
                                        wad.setCustom(new PublicationWorkflow(filteredList, uuids, allSubTree,
                                                gwtJahiaNode.getLanguageCode()));
                                        wad.initStartWorkflowDialog(def);
                                        wad.show();
                                    } else {
                                        new PublicationStatusWindow(linker, uuids, filteredList, allSubTree).show();
                                    }
                                }
                            }

                            public void onApplicationFailure(Throwable caught) {
                                linker.loaded();
                                Window.alert("Cannot get status: " + caught.getMessage());
                            }
                        });
            }
        } else {
            WorkflowActionDialog dialog = new WorkflowActionDialog(gwtJahiaNode, wf, linker);
            dialog.setCustom(wf.getCustomWorkflowInfo());
            dialog.initExecuteActionDialog(wf.getAvailableTasks().get(0));
            dialog.show();
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

        gwtJahiaNode = linker.getSelectionContext().getSingleSelection();
        if (gwtJahiaNode != null) {
            wf = gwtJahiaNode.getWorkflowInfo().getActiveWorkflows().get(new GWTJahiaWorkflowType("publish"));
            if (wf != null && !wf.getAvailableTasks().isEmpty()) {
                wf.getAvailableTasks().get(0).getDisplayName();
                setEnabled(true);
                updateTitle(wf.getAvailableTasks().get(0).getDisplayName() + " : " + gwtJahiaNode.getName());
            } else {
                wf = null;
                GWTJahiaPublicationInfo info = gwtJahiaNode.getPublicationInfo();

                setEnabled(GWTJahiaPublicationInfo.canPublish(gwtJahiaNode, info, JahiaGWTParameters.getLanguage()));

                updateTitle(getGwtToolbarItem().getTitle() + " " + gwtJahiaNode.getName() + " - " + JahiaGWTParameters.getLanguageDisplayName());
            }
        }
    }
}
