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
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflow;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineCards;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineContainer;
import org.jahia.ajax.gwt.client.widget.contentengine.EnginePanel;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineWindow;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.toolbar.action.WorkInProgressActionItem;
import org.jahia.ajax.gwt.client.widget.workflow.CustomWorkflow;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowActionDialog;

import java.util.*;

/**
 * User: toto
 * Date: Sep 10, 2010
 * Time: 3:32:00 PM
 */
public class PublicationWorkflow implements CustomWorkflow {
    public static final List<Integer> STATUS = Arrays.asList(GWTJahiaPublicationInfo.MARKED_FOR_DELETION,
            GWTJahiaPublicationInfo.MODIFIED,
            GWTJahiaPublicationInfo.NOT_PUBLISHED,
            GWTJahiaPublicationInfo.UNPUBLISHED,
            GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE,
            GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_VALID);

    private List<GWTJahiaPublicationInfo> publicationInfos;

    private static final long serialVersionUID = -4916142720074054130L;

    public PublicationWorkflow() {
    }

    public PublicationWorkflow(List<GWTJahiaPublicationInfo> publicationInfos) {
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
        TabItem tab = new TabItem("Publication infos");
        tab.setLayout(new FitLayout());

        PublicationStatusGrid g = new PublicationStatusGrid(publicationInfos, true, dialog.getLinker(), dialog.getContainer());
        tab.add(g);

        dialog.getTabPanel().add(tab);

        TabItem p = dialog.getTabPanel().getItem(0);
        LayoutContainer layoutContainer = new LayoutContainer(new RowLayout());
        layoutContainer.setStyleAttribute("margin","5px");

        Map<Integer, Integer> results = new HashMap<Integer, Integer>();
        for (GWTJahiaPublicationInfo info : publicationInfos) {
            Integer status = info.getStatus();
            if (status == GWTJahiaPublicationInfo.DELETED) {
                status = GWTJahiaPublicationInfo.MARKED_FOR_DELETION;
            }
            if (!results.containsKey(status)) {
                results.put(status, 1);
            } else {
                results.put(status, results.get(status) + 1);
            }
        }
        int i = 0;
        for (Integer status : STATUS) {
            if (results.containsKey(status)) {
                i++;
                HorizontalPanel h = new HorizontalPanel();
                h.add(GWTJahiaPublicationInfo.renderPublicationStatusImage(status));
                final String labelKey = GWTJahiaPublicationInfo.statusToLabel.get(status);
                h.add(new Text("&nbsp;" + Messages.get("label.publication." + labelKey, labelKey) + " : " +
                        results.get(status) + " " +
                        (results.get(status) > 1 ? Messages.get("label.items","Items") : Messages.get("label.item","Item"))));
                layoutContainer.add(h);
            }
        }
        if (i > 0) {
            p.add(layoutContainer, new BorderLayoutData(Style.LayoutRegion.NORTH, 10 + i * 15));
        }
    }

    public Button getStartWorkflowButton(final GWTJahiaWorkflowDefinition wf, final WorkflowActionDialog dialog) {
        final Button button = new Button(Messages.get("label.workflow.start", "Start Workflow") + ": " +
                                         wf.getDisplayName());
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                dialog.disableButtons();
                List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<GWTJahiaNodeProperty>();
                PropertiesEditor propertiesEditor = dialog.getPropertiesEditor();
                if (propertiesEditor != null) {
                    for (PropertiesEditor.PropertyAdapterField adapterField : propertiesEditor.getFieldsMap().values()) {
                        Field<?> field = adapterField.getField();
                        if (field.isEnabled() && !field.isReadOnly() && !field.validate() && ((FieldSet)adapterField.getParent()).isExpanded()) {
                            final String status = Messages.get("label.workflow.form.error", "Your form is not valid");
                            Info.display(status,status);
                            dialog.enableButtons();
                            return;
                        }
                    }
                    nodeProperties = propertiesEditor.getProperties();
                }
                dialog.getContainer().closeEngine();
                Info.display(Messages.get("label.workflow.start", "Start Workflow"), Messages.get(
                        "message.workflow.starting", "Starting publication workflow"));
                final String status = Messages.get("label.workflow.task", "Executing workflow task");
                WorkInProgressActionItem.setStatus(status);

                final HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("customWorkflowInfo", PublicationWorkflow.this);

                JahiaContentManagementService.App.getInstance().startWorkflow(getAllUuids(), wf, nodeProperties,
                        dialog.getComments(), map, new BaseAsyncCallback() {
                            public void onApplicationFailure(Throwable caught) {
                                WorkInProgressActionItem.removeStatus(status);
                                Log.error("Cannot publish", caught);
                                com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                            }

                            public void onSuccess(Object result) {
                                Info.display(Messages.get("label.workflow.start", "Start Workflow"), Messages.get(
                                        "message.workflow.started", "Publication workflow started"));
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
                    final String status = Messages.get("label.publication.task", "Publishing content");
                    Info.display(status, status);
                    WorkInProgressActionItem.setStatus(status);
                    final List<String> allUuids = getAllUuids();
                    JahiaContentManagementService.App.getInstance().publish(allUuids, nodeProperties, null,
                            new BaseAsyncCallback() {
                                public void onApplicationFailure(Throwable caught) {
                                    WorkInProgressActionItem.removeStatus(status);
                                    Info.display("Cannot publish", "Cannot publish");
                                    com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                                }

                                public void onSuccess(Object result) {
                                    WorkInProgressActionItem.removeStatus(status);
                                    if(allUuids.size() < 20) {
                                        dialog.getLinker().refresh(Linker.REFRESH_MAIN + Linker.REFRESH_PAGES);
                                    }
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
            if (info.getStatus() != GWTJahiaPublicationInfo.DELETED) {
                l.add(info.getUuid());
                if (info.getI18nUuid() != null) {
                    l.add(info.getI18nUuid());
                }
            }
        }
        return l;
    }

    public static void create(List<GWTJahiaPublicationInfo> all, final Linker linker) {
        final TreeMap<String, List<GWTJahiaPublicationInfo>> infosListByWorflowGroup = new TreeMap<String, List<GWTJahiaPublicationInfo>>();

        List<String> keys = new ArrayList<String>();

        for (GWTJahiaPublicationInfo info : all) {
            String workflowGroupKey = info.getWorkflowGroup();
            if (!infosListByWorflowGroup.containsKey(workflowGroupKey)) {
                infosListByWorflowGroup.put(workflowGroupKey, new ArrayList<GWTJahiaPublicationInfo>());
            }
            infosListByWorflowGroup.get(workflowGroupKey).add(info);
            if (info.getWorkflowDefinition() != null) {
                if (!keys.contains(info.getWorkflowDefinition())) {
                    keys.add(info.getWorkflowDefinition());
                }
            }
        }

        JahiaContentManagementService.App.getInstance().getWorkflowDefinitions(keys,
                new BaseAsyncCallback<Map<String, GWTJahiaWorkflowDefinition>>() {
                    public void onSuccess(Map<String, GWTJahiaWorkflowDefinition> result) {
                        EngineContainer container;
                        if (linker instanceof ManagerLinker) {
                            container = new EngineWindow();
                        } else {
                            container = new EnginePanel();
                        }
                        EngineContainer cards = new EngineCards(container, linker);
                        if (infosListByWorflowGroup.entrySet().isEmpty()) {
                            new PublicationStatusWindow(linker, null, null, cards);
                        }
                        for (Map.Entry<String, List<GWTJahiaPublicationInfo>> entry : infosListByWorflowGroup.entrySet()) {
                            final List<GWTJahiaPublicationInfo> infoList = entry.getValue();

                            String workflowDefinition = infoList.get(0).getWorkflowDefinition();
                            if (workflowDefinition != null) {
                                final PublicationWorkflow custom = new PublicationWorkflow(infoList);
                                new WorkflowActionDialog(infoList.get(0).getMainPath(),Messages.getWithArgs("label.workflow.start.message",
                                        "{0} started by {1} on {2} - {3} content items involved",
                                        new Object[]{result.get(workflowDefinition).getDisplayName(),JahiaGWTParameters.getCurrentUser(),DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT).format(new Date()),infoList.size()})
                                         , result.get(workflowDefinition),
                                        linker, custom, cards);
                            } else {
                                // Workflow defined
                                new PublicationStatusWindow(linker, getAllUuids(infoList), infoList, cards);
                            }
                        }

                        cards.showEngine();
                    }

                });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PublicationWorkflow)) {
            return false;
        }

        PublicationWorkflow that = (PublicationWorkflow) o;

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
