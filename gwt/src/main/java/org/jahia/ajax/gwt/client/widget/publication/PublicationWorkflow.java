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
package org.jahia.ajax.gwt.client.widget.publication;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflow;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
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
 * Implementation of CustomWorkflow for publication type workflow
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

    protected List<GWTJahiaPublicationInfo> publicationInfos;
    private static transient boolean doRefresh;

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
        if (button != null) {
            dialog.getButtonsBar().insert(button, 0);
        }
        button = getStartWorkflowButton(workflow, dialog);
        if (button != null) {
            dialog.getButtonsBar().insert(button, 0);
        }
    }

    public void initExecuteActionDialog(GWTJahiaWorkflow workflow, WorkflowActionDialog dialog) {
        initDialog(dialog);
    }

    protected void initDialog(WorkflowActionDialog dialog) {
        TabItem tab = new TabItem("Publication infos");
        tab.setLayout(new FitLayout());
        doRefresh = false;

        PublicationStatusGrid g = new PublicationStatusGrid(publicationInfos, true, dialog.getLinker(), dialog.getContainer());
        tab.add(g);

        dialog.getTabPanel().add(tab);

        TabItem p = dialog.getTabPanel().getItem(0);
        LayoutContainer layoutContainer = new LayoutContainer(new RowLayout());
        layoutContainer.setStyleAttribute("margin", "5px");

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
                h.add(new Html("&nbsp;" + Messages.get("label.publication." + labelKey, labelKey) + " : " +
                        results.get(status) + " " +
                        (results.get(status) > 1 ? Messages.get("label.items", "Items") : Messages.get("label.item", "Item"))));
                layoutContainer.add(h);
            }
        }
        if (i > 0) {
            p.add(layoutContainer, new BorderLayoutData(Style.LayoutRegion.NORTH, 5 + i * 20));
        }
    }

    public Button getStartWorkflowButton(final GWTJahiaWorkflowDefinition wf, final WorkflowActionDialog dialog) {
        final Button button = new Button(Messages.get("label.workflow.start", "Start workflow"));
        button.addStyleName("button-start");
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                dialog.disableButtons();
                List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<GWTJahiaNodeProperty>();
                PropertiesEditor propertiesEditor = dialog.getPropertiesEditor();
                if (propertiesEditor != null) {
                    for (PropertiesEditor.PropertyAdapterField adapterField : propertiesEditor.getFieldsMap().values()) {
                        Field<?> field = adapterField.getField();
                        if (field.isEnabled() && !field.isReadOnly() && !field.validate() && ((FieldSet) adapterField.getParent()).isExpanded()) {
                            final String status = Messages.get("label.workflow.form.error", "Your form is not valid");
                            Info.display(status, status);
                            dialog.enableButtons();
                            return;
                        }
                    }
                    nodeProperties = propertiesEditor.getProperties();
                }
                // enable buttons before close the engine to avoid layout side effect (the remaining button at the same index is disabled)
                dialog.enableButtons();
                dialog.getContainer().closeEngine();
                Info.display(Messages.get("label.workflow.start", "Start Workflow"), Messages.get(
                        "message.workflow.starting", "Starting publication workflow"));
                final String status = Messages.get("label.workflow.task", "Executing workflow task");
                WorkInProgressActionItem.setStatus(status);

                final Map<String, Object> map = new HashMap<String, Object>();
                map.put("customWorkflowInfo", PublicationWorkflow.this);

                String workflowGroup = publicationInfos.get(0).getWorkflowGroup();
                String locale = workflowGroup.substring(0, workflowGroup.indexOf("/"));

                JahiaContentManagementService.App.getInstance().startWorkflow(getAllUuids(), wf, nodeProperties,
                        dialog.getComments(), map, locale, new BaseAsyncCallback() {
                            public void onApplicationFailure(Throwable caught) {
                                WorkInProgressActionItem.removeStatus(status);
                                Log.error(Messages.get("label.workflow.cannotStart", "Cannot start workflow"), caught);
                                com.google.gwt.user.client.Window.alert(Messages.get("label.workflow.cannotStart", "Cannot start workflow") + caught.getMessage());
                            }

                            public void onSuccess(Object result) {
                                Info.display(Messages.get("label.workflow.start", "Start Workflow"), Messages.get(
                                        "message.workflow.started", "Workflow started"));
                                WorkInProgressActionItem.removeStatus(status);
                                // if one wf has been started, do a refresh even on cancel
                                doRefresh = true;
                                // refresh only if there is no more wf
                                if (dialog.getContainer() instanceof EngineCards && ((EngineCards) dialog.getContainer()).getComponents().isEmpty()) {
                                    Map<String, Object> data = new HashMap<String, Object>();
                                    data.put(Linker.REFRESH_MAIN, true);
                                    data.put("event", "workflowStarted");
                                    dialog.getLinker().refresh(data);
                                }
                            }
                        }
                );
            }
        });
        return button;
    }

    public Button getBypassWorkflowButton(final GWTJahiaWorkflowDefinition wf, final WorkflowActionDialog dialog) {
        if (!publicationInfos.isEmpty() && publicationInfos.get(0).isAllowedToPublishWithoutWorkflow()) {
            final Button button = new Button(Messages.get("label.bypassWorkflow", "Bypass selected workflow"));
            button.addStyleName("button-bypassworkflow");
            button.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    dialog.disableButtons();
                    List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<GWTJahiaNodeProperty>();
                    if (dialog.getPropertiesEditor() != null) {
                        nodeProperties = dialog.getPropertiesEditor().getProperties();
                    }
                    dialog.getContainer().closeEngine();
                    doPublish(nodeProperties, dialog);
                }
            });
            return button;
        } else {
            return null;
        }

    }

    protected void doPublish(List<GWTJahiaNodeProperty> nodeProperties, final WorkflowActionDialog dialog) {
        final String status = Messages.get("label.publication.task", "Publishing content");
        Info.display(status, status);
        WorkInProgressActionItem.setStatus(status);
        final List<String> allUuids = getAllUuids();
        BaseAsyncCallback callback = new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable caught) {
                WorkInProgressActionItem.removeStatus(status);
                Info.display("Cannot publish", "Cannot publish");
                Window.alert("Cannot publish " + caught.getMessage());
            }

            public void onSuccess(Object result) {
                WorkInProgressActionItem.removeStatus(status);
            }
        };
        JahiaContentManagementService.App.getInstance().publish(allUuids, nodeProperties, null, callback);
    }

    public List<String> getAllUuids() {
        return getAllUuids(publicationInfos);
    }

    public static List<String> getAllUuids(List<GWTJahiaPublicationInfo> publicationInfos) {
        return getAllUuids(publicationInfos, false);
    }

    public static List<String> getAllUuids(List<GWTJahiaPublicationInfo> publicationInfos, boolean onlyAllowedToPublishWithoutWorkflow) {
        List<String> l = new ArrayList<String>();
        for (GWTJahiaPublicationInfo info : publicationInfos) {
            if (info.getStatus() != GWTJahiaPublicationInfo.DELETED && (!onlyAllowedToPublishWithoutWorkflow || info.isAllowedToPublishWithoutWorkflow())) {
                if (info.getUuid() != null) {
                    l.add(info.getUuid());
                }
                if (info.getI18nUuid() != null) {
                    l.add(info.getI18nUuid());
                }
                if (info.getDeletedI18nUuid() != null) {
                    for (String s : info.getDeletedI18nUuid().split(" ")) {
                        l.add(s);
                    }
                }
            }
        }
        return l;
    }

    public static void create(final List<GWTJahiaPublicationInfo> all, final Linker linker, final boolean unpublish) {

        final SortedMap<String, List<GWTJahiaPublicationInfo>> infosListByWorflowGroup = new TreeMap<String, List<GWTJahiaPublicationInfo>>();

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
        if (keys.size() > 0) {
            JahiaContentManagementService.App.getInstance().getWorkflowDefinitions(keys,
                    new BaseAsyncCallback<Map<String, GWTJahiaWorkflowDefinition>>() {
                        public void onSuccess(Map<String, GWTJahiaWorkflowDefinition> definitions) {
                            PublicationWorkflow.create(infosListByWorflowGroup, definitions, linker, unpublish);
                        }
                    }
            );
        } else {
            create(infosListByWorflowGroup, new HashMap<String, GWTJahiaWorkflowDefinition>(), linker, unpublish);
        }
    }

    private static void create(SortedMap<String, List<GWTJahiaPublicationInfo>> infosListByWorflowGroup, Map<String, GWTJahiaWorkflowDefinition> definitions, final Linker linker, boolean unpublish) {
        EngineContainer container;
        if (linker instanceof ManagerLinker) {
            container = new EngineWindow();
        } else {
            container = new EnginePanel();
        }
        final EngineCards cards = new EngineCards(container, linker);
        if (infosListByWorflowGroup.entrySet().isEmpty()) {
            new PublicationStatusWindow(linker, null, null, cards, unpublish);
        }
        for (Map.Entry<String, List<GWTJahiaPublicationInfo>> entry : infosListByWorflowGroup.entrySet()) {
            final List<GWTJahiaPublicationInfo> infoList = entry.getValue();

            String workflowDefinition = infoList.get(0).getWorkflowDefinition();
            if (workflowDefinition != null) {
                final PublicationWorkflow custom = unpublish ? new UnpublicationWorkflow(infoList) : new PublicationWorkflow(infoList);
                new WorkflowActionDialog(infoList.get(0).getMainPath(), Messages.getWithArgs("label.workflow.start.message",
                        "{0} started by {1} on {2} - {3} content items involved",
                        new Object[]{definitions.get(workflowDefinition).getDisplayName(), JahiaGWTParameters.getCurrentUser(), DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT).format(new Date()), infoList.size()})
                        , definitions.get(workflowDefinition),
                        linker, custom, cards, infoList.get(0).getLanguage()
                );
            } else {
                // No Workflow defined
                new PublicationStatusWindow(linker, getAllUuids(infoList), infoList, cards, unpublish);
            }
        }
        cards.addGlobalButton(getStartAllWorkflows(cards, linker));
        cards.addGlobalButton(getBypassAllWorkflowsButton(cards, linker));
        Button button = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                cards.closeAllEngines();
                if (doRefresh) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put(Linker.REFRESH_MAIN, true);
                    data.put("event", "workflowStarted");
                    linker.refresh(data);
                }
            }
        });
        button.addStyleName("button-cancel");
        cards.addGlobalButton(button);
        cards.showEngine();
    }

    private static Button getBypassAllWorkflowsButton(final EngineCards cards, final Linker linker) {
        boolean hasBypassAll = false;
        for (Component component : cards.getComponents()) {
            if (component instanceof WorkflowActionDialog) {
                final List<GWTJahiaPublicationInfo> thisWFInfo = ((PublicationWorkflow) ((WorkflowActionDialog) component).getCustomWorkflow()).getPublicationInfos();
                if (thisWFInfo.get(0).isAllowedToPublishWithoutWorkflow()) {
                    hasBypassAll = true;
                    break;
                }
            } else if (component instanceof PublicationStatusWindow) {
                hasBypassAll = true;
                break;
            }
        }
        if (!hasBypassAll) {
            return null;
        }

        final Button button = new Button(Messages.get((cards.getComponents().size()==1?"label.bypassWorkflow":"label.bypassWorkflow.all"), (cards.getComponents().size()==1?"Bypass workflow":"Bypass all workflows")));
        button.addStyleName("button-bypassworkflow");

        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                final String status = Messages.get("label.publication.task", "Publishing content");
                Info.display(status, status);
                WorkInProgressActionItem.setStatus(status);

                final List<Component> components = new ArrayList<Component>(cards.getComponents());
                final int[] nbWF = {components.size()};
                for (Component component : components) {
                    if (component instanceof WorkflowActionDialog) {
                        final WorkflowActionDialog dialog = (WorkflowActionDialog) component;
                        dialog.disableButtons();
                        List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<GWTJahiaNodeProperty>();
                        if (!fillDialogProperties(dialog, nodeProperties)) {
                            return;
                        }
                        dialog.getContainer().closeEngine();
                        final PublicationWorkflow customWorkflow = (PublicationWorkflow) dialog.getCustomWorkflow();
                        final List<GWTJahiaPublicationInfo> thisWFInfo = customWorkflow.getPublicationInfos();
                        if (thisWFInfo.get(0).isAllowedToPublishWithoutWorkflow()) {
                            if (customWorkflow instanceof UnpublicationWorkflow) {
                                JahiaContentManagementService.App.getInstance().unpublish(getAllUuids(thisWFInfo),
                                        getCallback(cards, nbWF, Messages.get("message.content.unpublished", "Content unpublished"),
                                                Messages.get("message.content.unpublished.error", "Cannot unpublish"), status, linker, null));
                            } else {
                                JahiaContentManagementService.App.getInstance().publish(getAllUuids(thisWFInfo), nodeProperties, null,
                                        getCallback(cards, nbWF, Messages.get("message.content.published", "Content published"),
                                                Messages.get("message.content.published.error", "Cannot publish"), status, linker, null));
                            }
                        } else {
                            close(cards, nbWF, Messages.get("message.content.published", "Content published"), status, dialog.getLinker(), null);
                        }
                    } else if (component instanceof PublicationStatusWindow) {
                        final PublicationStatusWindow dialog = (PublicationStatusWindow) component;
                        if (dialog.isUnpublish()) {
                            JahiaContentManagementService.App.getInstance().unpublish(dialog.getUuids(),
                                    getCallback(cards, nbWF, Messages.get("message.content.unpublished", "Content unpublished"),
                                            Messages.get("message.content.unpublished.error", "Cannot unpublish"), status, linker, null));
                        } else {
                            JahiaContentManagementService.App.getInstance().publish(dialog.getUuids(), null, null,
                                    getCallback(cards, nbWF, Messages.get("message.content.published", "Content published"),
                                            Messages.get("message.content.published.error", "Cannot publish"), status, linker, null));
                        }
                    } else {
                        close(cards, nbWF, Messages.get("label.workflow.start", "Start Workflow"), status, linker, null);
                    }
                }

            }
        });
        return button;
    }

    private static Button getStartAllWorkflows(final EngineCards cards, final Linker linker) {
        boolean hasWorkflow = false;
        for (Component component : cards.getComponents()) {
            if (component instanceof WorkflowActionDialog) {
                hasWorkflow = true;
                break;
            }
        }
        if (!hasWorkflow) {
            return null;
        }

        final Button button = new Button(Messages.get((cards.getComponents().size()==1?"label.workflow.start":"label.workflow.start.all"), (cards.getComponents().size()==1?"Start workflow":"Start all workflows")));
        button.addStyleName("button-start");

        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {

                Info.display(Messages.get("label.workflow.start", "Start Workflow"), Messages.get(
                        "message.workflow.starting", "Starting publication workflow"));
                final String status = Messages.get("label.workflow.task", "Executing workflow task");
                WorkInProgressActionItem.setStatus(status);

                final List<Component> components = new ArrayList<Component>(cards.getComponents());
                final int[] nbWF = {components.size()};

                final Map<String, Object> refreshData = new HashMap<String, Object>();
                refreshData.put(Linker.REFRESH_MAIN, true);
                refreshData.put("event", "workflowStarted");

                for (Component component : components) {
                    if (component instanceof WorkflowActionDialog) {
                        final WorkflowActionDialog dialog = (WorkflowActionDialog) component;
                        dialog.disableButtons();
                        List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<GWTJahiaNodeProperty>();
                        if (!fillDialogProperties(dialog, nodeProperties)) {
                            return;
                        }
                        final PublicationWorkflow customWorkflow = (PublicationWorkflow) dialog.getCustomWorkflow();
                        final List<GWTJahiaPublicationInfo> thisWFInfo = customWorkflow.getPublicationInfos();

                        final Map<String, Object> map = new HashMap<String, Object>();
                        map.put("customWorkflowInfo", customWorkflow);
                        String workflowGroup = thisWFInfo.get(0).getWorkflowGroup();
                        String locale = workflowGroup.substring(0, workflowGroup.indexOf("/"));
                        JahiaContentManagementService.App.getInstance().startWorkflow(getAllUuids(thisWFInfo), dialog.getWfDefinition(), nodeProperties,
                                dialog.getComments(), map, locale, getCallback(cards, nbWF, Messages.get("label.workflow.start", "Start Workflow"),
                                        Messages.get("label.workflow.cannotStart", "Cannot start workflow"), status, linker, refreshData)
                        );
                    } else {
                        close(cards, nbWF, Messages.get("label.workflow.start", "Start Workflow"), status, linker, refreshData);
                    }
                }
            }
        });
        return button;
    }

    private static BaseAsyncCallback getCallback(final EngineCards cards, final int[] nbWF, final String successMessage, final String errorMessage, final String statusMessage, final Linker linker, final Map<String, Object> refreshData) {
        return new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable caught) {
                close(cards, nbWF, errorMessage, statusMessage, linker, refreshData);
                Log.error(errorMessage, caught);
                Window.alert(errorMessage + caught.getMessage());
            }

            public void onSuccess(Object result) {
                close(cards, nbWF, successMessage, statusMessage, linker, refreshData);
            }
        };
    }

    private static void close(EngineCards cards, int[] nbWF, String message, String statusMessage, Linker linker, Map<String, Object> refreshData) {
        nbWF[0]--;
        if (nbWF[0] == 0) {
            Info.display(message, message);
            WorkInProgressActionItem.removeStatus(statusMessage);
            if(refreshData != null) {
                linker.refresh(refreshData);
            }
            cards.closeAllEngines();
        }
    }

    private static boolean fillDialogProperties(WorkflowActionDialog dialog, List<GWTJahiaNodeProperty> nodeProperties) {
        PropertiesEditor propertiesEditor = dialog.getPropertiesEditor();
        if (propertiesEditor != null) {
            for (PropertiesEditor.PropertyAdapterField adapterField : propertiesEditor.getFieldsMap().values()) {
                Field<?> field = adapterField.getField();
                if (field.isEnabled() && !field.isReadOnly() && !field.validate() && ((FieldSet) adapterField.getParent()).isExpanded()) {
                    final String error = Messages.get("label.workflow.form.error", "Your form is not valid");
                    Info.display(error, error);
                    dialog.enableButtons();
                    return false;
                }
            }
            nodeProperties.addAll(propertiesEditor.getProperties());
        }
        return true;
    }

    public List<GWTJahiaPublicationInfo> getPublicationInfos() {
        return publicationInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
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
