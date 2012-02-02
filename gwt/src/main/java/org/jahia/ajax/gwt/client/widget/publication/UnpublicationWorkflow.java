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

package org.jahia.ajax.gwt.client.widget.publication;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflow;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.action.WorkInProgressActionItem;
import org.jahia.ajax.gwt.client.widget.workflow.CustomWorkflow;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowActionDialog;

import java.util.*;

/**
 * User: toto
 * Date: Sep 10, 2010
 * Time: 3:32:00 PM
 */
public class UnpublicationWorkflow implements CustomWorkflow {
    private List<GWTJahiaPublicationInfo> publicationInfos;

    private static final long serialVersionUID = -4916142720074054130L;

    public UnpublicationWorkflow() {
    }

    public UnpublicationWorkflow(List<GWTJahiaPublicationInfo> publicationInfos) {
        this.publicationInfos = publicationInfos;
    }

    public void initStartWorkflowDialog(GWTJahiaWorkflowDefinition workflow, WorkflowActionDialog dialog) {
        initDialog(dialog);

        dialog.getButtonsBar().remove(dialog.getButtonsBar().getItem(0));
        Button button = getBypassWorkflowButton(workflow, dialog);
        if(button!=null) {
            dialog.getButtonsBar().insert(button, 0);
        }
        button = getStartWorkflowButton(workflow, dialog);
        if (button!=null) {
            dialog.getButtonsBar().insert(button, 0);
        }
    }

    public void initExecuteActionDialog(GWTJahiaWorkflow workflow, WorkflowActionDialog dialog) {
        initDialog(dialog);
    }

    private void initDialog(WorkflowActionDialog dialog) {
        TabItem tab = new TabItem("Unpublication infos");
        tab.setLayout(new FitLayout());

        PublicationStatusGrid g = new PublicationStatusGrid(publicationInfos, true, dialog.getLinker(), dialog.getContainer());
        tab.add(g);

        dialog.getTabPanel().add(tab);
    }

    public Button getStartWorkflowButton(final GWTJahiaWorkflowDefinition wf, final WorkflowActionDialog dialog) {
        final Button button = new Button(Messages.get("label.workflow.start", "Start Workflow") + ": " +
                                         wf.getDisplayName());
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                dialog.disableButtons();
                List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<GWTJahiaNodeProperty>();
                if (dialog.getPropertiesEditor() != null) {
                    nodeProperties = dialog.getPropertiesEditor().getProperties();
                }
                dialog.getContainer().closeEngine();
                Info.display(Messages.get("label.workflow.start", "Start Workflow"), Messages.get(
                        "message.workflow.starting", "Starting unpublication workflow"));
                final String status = Messages.get("label.workflow.task", "Executing workflow task");
                WorkInProgressActionItem.setStatus(status);

                final HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("customWorkflowInfo", UnpublicationWorkflow.this);

                JahiaContentManagementService.App.getInstance().startWorkflow(getAllUuids(), wf, nodeProperties,
                        dialog.getComments(), map, new BaseAsyncCallback() {
                            public void onApplicationFailure(Throwable caught) {
                                WorkInProgressActionItem.removeStatus(status);
                                Log.error("Cannot publish", caught);
                                com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                            }

                            public void onSuccess(Object result) {
                                Info.display(Messages.get("label.workflow.start", "Start Workflow"), Messages.get(
                                        "message.workflow.started", "Unublication workflow started"));
                                WorkInProgressActionItem.removeStatus(status);
                                dialog.getLinker().refresh(Linker.REFRESH_MAIN + Linker.REFRESH_PAGES);
                            }
                        });
            }
        });
        return button;
    }

    public Button getBypassWorkflowButton(final GWTJahiaWorkflowDefinition wf, final WorkflowActionDialog dialog) {
        final Button button = new Button(Messages.get("label.bypassWorkflow", "Bypass workflow"));

        if (PermissionsUtils.isPermitted("publish", dialog.getLinker().getSelectionContext().getSingleSelection())) {
            button.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    dialog.disableButtons();
                    List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<GWTJahiaNodeProperty>();
                    if (dialog.getPropertiesEditor() != null) {
                        nodeProperties = dialog.getPropertiesEditor().getProperties();
                    }
                    dialog.getContainer().closeEngine();
                    final String status = Messages.get("label.publication.unpublished.task", "Unpublishing content");
                    Info.display(status, status);
                    WorkInProgressActionItem.setStatus(status);
                    JahiaContentManagementService.App.getInstance().unpublish(getAllUuids(),
                            new BaseAsyncCallback() {
                                public void onApplicationFailure(Throwable caught) {
                                    WorkInProgressActionItem.removeStatus(status);
                                    Info.display("Cannot unpublish", "Cannot unpublish");
                                    com.google.gwt.user.client.Window.alert("Cannot unpublish " + caught.getMessage());
                                }

                                public void onSuccess(Object result) {
                                    WorkInProgressActionItem.removeStatus(status);
                                    Info.display(Messages.get("label.content.unpublished"), Messages.get(
                                            "label.content.unpublished"));
                                    dialog.getLinker().refresh(Linker.REFRESH_MAIN + Linker.REFRESH_PAGES);
                                }
                            });
                }
            });
            return button;
        } else {
            return null;
        }

    }

    public List<String> getAllUuids() {
        return getAllUuids(publicationInfos);
    }

    public static List<String> getAllUuids(List<GWTJahiaPublicationInfo> publicationInfos) {
        List<String> l = new ArrayList<String>();
        for (GWTJahiaPublicationInfo info : publicationInfos) {
            l.add(info.getUuid());
            if (info.getI18nUuid() != null) {
                l.add(info.getI18nUuid());
            }
        }
        return l;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UnpublicationWorkflow)) {
            return false;
        }

        UnpublicationWorkflow that = (UnpublicationWorkflow) o;

        if (publicationInfos != null ? !publicationInfos.equals(that.publicationInfos) :
            that.publicationInfos != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = publicationInfos != null ? publicationInfos.hashCode() : 0;
        return result;
    }
}
