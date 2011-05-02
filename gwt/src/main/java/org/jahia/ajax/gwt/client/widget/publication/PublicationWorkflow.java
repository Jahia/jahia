package org.jahia.ajax.gwt.client.widget.publication;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
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
    private List<GWTJahiaPublicationInfo> publicationInfos;

    private static final long serialVersionUID = -4916142720074054130L;

    public PublicationWorkflow() {
    }

    public PublicationWorkflow(List<GWTJahiaPublicationInfo> publicationInfos) {
        this.publicationInfos = publicationInfos;
    }

    public List<TabItem> getAdditionalTabs() {
        List tabs = new ArrayList<TabItem>();

        TabItem tab = new TabItem("Publication infos");
        tab.setLayout(new FitLayout());
        tabs.add(tab);

        PublicationStatusGrid g = new PublicationStatusGrid(publicationInfos, true);
        tab.add(g);

        return tabs;
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
                    JahiaContentManagementService.App.getInstance().publish(getAllUuids(), nodeProperties, null,
                            new BaseAsyncCallback() {
                                public void onApplicationFailure(Throwable caught) {
                                    WorkInProgressActionItem.removeStatus(status);
                                    Info.display("Cannot publish", "Cannot publish");
                                    com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                                }

                                public void onSuccess(Object result) {
                                    WorkInProgressActionItem.removeStatus(status);
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
                                        new Object[]{result.get(workflowDefinition),JahiaGWTParameters.getCurrentUser(),DateTimeFormat.getShortDateFormat().format(new Date()),infoList.size()})
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
