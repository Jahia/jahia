/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.ajax.gwt.client.EmptyLinker;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
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
    private static final long serialVersionUID = -4916142720074054130L;
    private static transient boolean doRefresh;
    protected List<GWTJahiaPublicationInfo> publicationInfos;

    public PublicationWorkflow() {
    }

    public PublicationWorkflow(List<GWTJahiaPublicationInfo> publicationInfos) {
        this.publicationInfos = publicationInfos;
    }

    private static void closeDialog(WorkflowActionDialog dialog) {
        ((EngineCards) dialog.getContainer()).closeEngine(dialog);
    }

    public static List<String> getAllUuids(List<GWTJahiaPublicationInfo> publicationInfos) {
        return getAllUuids(publicationInfos, false, false);
    }

    public static List<String> getAllUuids(List<GWTJahiaPublicationInfo> publicationInfos, boolean onlyAllowedToPublishWithoutWorkflow, boolean unpublish) {
        List<String> l = new ArrayList<>();
        for (GWTJahiaPublicationInfo info : publicationInfos) {
            if (info.getStatus() != GWTJahiaPublicationInfo.DELETED && (unpublish || info.isPublishable()) && (!onlyAllowedToPublishWithoutWorkflow || info.isAllowedToPublishWithoutWorkflow())) {
                if (info.getUuid() != null) {
                    l.add(info.getUuid());
                }
                if (info.getI18nUuid() != null) {
                    l.add(info.getI18nUuid());
                }
                if (info.getDeletedI18nUuid() != null) {
                    l.addAll(Arrays.asList(info.getDeletedI18nUuid().split(" ")));
                }
            }
        }
        return l;
    }

    public static void create(final List<GWTJahiaPublicationInfo> all, final Linker linker, final boolean unpublish) {

        final SortedMap<String, List<GWTJahiaPublicationInfo>> infosListByWorflowGroup = new TreeMap<>();

        List<String> keys = new ArrayList<>();

        for (GWTJahiaPublicationInfo info : all) {
            String workflowGroupKey = info.getWorkflowGroup();
            infosListByWorflowGroup.computeIfAbsent(workflowGroupKey, k -> new ArrayList<>()).add(info);
            if (info.getWorkflowDefinition() != null && !keys.contains(info.getWorkflowDefinition())) {
                keys.add(info.getWorkflowDefinition());
            }
        }
        if (!keys.isEmpty()) {
            JahiaContentManagementService.App.getInstance().getWorkflowDefinitions(keys,
                    new BaseAsyncCallback<Map<String, GWTJahiaWorkflowDefinition>>() {

                        @Override
                        public void onSuccess(Map<String, GWTJahiaWorkflowDefinition> definitions) {
                            PublicationWorkflow.create(infosListByWorflowGroup, definitions, linker, unpublish);
                        }
                    }
            );
        } else {
            create(infosListByWorflowGroup, new HashMap<>(), linker, unpublish);
        }
    }

    private static void create(SortedMap<String, List<GWTJahiaPublicationInfo>> infosListByWorflowGroup, Map<String, GWTJahiaWorkflowDefinition> definitions, final Linker linker, boolean unpublish) {
        EngineContainer container;
        if (linker instanceof ManagerLinker || linker instanceof EmptyLinker) {
            container = new EngineWindow();
        } else {
            container = new EnginePanel();
        }
        final EngineCards cards = new EngineCards(container, linker);
        if (infosListByWorflowGroup.entrySet().isEmpty()) {
            new PublicationStatusWindow(linker, null, null, cards, unpublish);
        }
        boolean showStartWfButton = false;
        for (Map.Entry<String, List<GWTJahiaPublicationInfo>> entry : infosListByWorflowGroup.entrySet()) {
            final List<GWTJahiaPublicationInfo> infoList = entry.getValue();

            boolean entries = parseEntries(definitions, linker, unpublish, cards, infoList);
            showStartWfButton = showStartWfButton || entries;
        }
        if (showStartWfButton) {
            cards.addGlobalButton(getStartAllWorkflows(cards, linker, unpublish));
        }
        cards.addGlobalButton(getBypassAllWorkflowsButton(cards, linker, unpublish));
        Button button = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent event) {
                cards.closeAllEngines();
                if (doRefresh) {
                    Map<String, Object> data = new HashMap<>();
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

    private static boolean parseEntries(Map<String, GWTJahiaWorkflowDefinition> definitions, Linker linker, boolean unpublish, EngineCards cards, List<GWTJahiaPublicationInfo> infoList) {
        String workflowDefinition = infoList.get(0).getWorkflowDefinition();
        if (workflowDefinition != null) {
            final PublicationWorkflow custom = unpublish ? new UnpublicationWorkflow(infoList) : new PublicationWorkflow(infoList);
            new WorkflowActionDialog(infoList.get(0).getMainPath(), Messages.getWithArgs("label.workflow.start.message",
                    "{0} started by {1} on {2} - {3} content items involved",
                    new Object[]{definitions.get(workflowDefinition).getDisplayName(), JahiaGWTParameters.getCurrentUser(), DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT).format(new Date()), infoList.size()}),
                    definitions.get(workflowDefinition),
                    linker, custom, cards, infoList.get(0).getLanguage()
            );
            return true;
        } else {
            // No Workflow defined
            final PublicationWorkflow custom = unpublish ? new UnpublicationWorkflow(infoList) : new PublicationWorkflow(infoList);
            new WorkflowActionDialog(infoList.get(0).getMainPath(), Messages.get("label.engineTab.publication", "Publication"),
                    null,
                    linker, custom, cards, infoList.get(0).getLanguage()
            );
        }
        return false;
    }

    private static Button getBypassAllWorkflowsButton(final EngineCards cards, final Linker linker, final boolean unpublish) {
        if (!isHasBypassAll(cards)) {
            return null;
        }

        final Button button;
        if (unpublish) {
            button = new Button(Messages.get((cards.getComponents().size() == 1 ? "label.bypassUnpublishWorkflow" : "label.bypassUnpublishWorkflow.all"), (cards.getComponents().size() == 1 ? "Unpublish" : "Unpublish all")));
        } else {
            button = new Button(Messages.get((cards.getComponents().size() == 1 ? "label.bypassWorkflow" : "label.bypassWorkflow.all"), (cards.getComponents().size() == 1 ? "Publish" : "Publish all")));
        }

        button.addStyleName("button-bypassworkflow");

        button.addSelectionListener(new BypassAllWorkflowListener(cards, linker));
        return button;
    }

    private static boolean isHasBypassAll(EngineCards cards) {
        for (Component component : cards.getComponents()) {
            if (component instanceof WorkflowActionDialog) {
                final List<GWTJahiaPublicationInfo> thisWFInfo = ((PublicationWorkflow) ((WorkflowActionDialog) component).getCustomWorkflow()).getPublicationInfos();
                if (thisWFInfo.get(0).isAllowedToPublishWithoutWorkflow()) {
                    return true;
                }
            } else if (component instanceof PublicationStatusWindow) {
                return true;
            }
        }
        return false;
    }

    private static Button getStartAllWorkflows(final EngineCards cards, final Linker linker, final boolean unpublish) {
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

        final Button button;
        if (unpublish) {
            button = new Button(Messages.get((cards.getComponents().size() == 1 ? "label.workflow.unpublish.start" : "label.workflow.unpublish.start.all"), (cards.getComponents().size() == 1 ? "Request unpublication" : "Request unpublication for all")));
        } else {
            button = new Button(Messages.get((cards.getComponents().size() == 1 ? "label.workflow.start" : "label.workflow.start.all"), (cards.getComponents().size() == 1 ? "Request publication" : "Request publication for all")));
        }

        button.addStyleName("button-start");

        button.addSelectionListener(new StartAllWorkflowsListener(cards, linker));
        return button;
    }

    private static BaseAsyncCallback<Object> getCallback(final EngineCards cards, final int[] nbWF, final String successMessage, final String errorMessage, final String statusMessage, final Linker linker, final Map<String, Object> refreshData) {
        return new BaseAsyncCallback<Object>() {

            @Override
            public void onApplicationFailure(Throwable caught) {
                close(cards, nbWF, errorMessage, statusMessage, linker, refreshData);
                Log.error(errorMessage, caught);
                Window.alert(errorMessage + caught.getMessage());
            }

            @Override
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
            if (refreshData != null) {
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

    public static void openPublicationWorkflow(List<String> uuids, final Linker linker, final boolean allSubTree, final boolean allLanguages, final boolean checkForUnpublication) {
        BaseAsyncCallback<List<GWTJahiaPublicationInfo>> asyncCallback = new OpenPublicationWorkflowCallback(linker, checkForUnpublication);
        // todo
        if (!allLanguages) {
            JahiaContentManagementService.App.getInstance().getPublicationInfo(uuids, allSubTree, checkForUnpublication, asyncCallback);
        } else {
            Set<String> languages = new HashSet<>();
            for (GWTJahiaLanguage gwtJahiaLanguage : JahiaGWTParameters.getSiteLanguages()) {
                if (Boolean.TRUE.equals(gwtJahiaLanguage.isActive())) {
                    languages.add(gwtJahiaLanguage.getLanguage());
                }
            }
            JahiaContentManagementService.App.getInstance().getPublicationInfo(uuids, allSubTree, checkForUnpublication, languages, asyncCallback);
        }
    }

    @Override
    public void initStartWorkflowDialog(GWTJahiaWorkflowDefinition workflow, WorkflowActionDialog dialog) {
        initDialog(dialog);
        if (dialog.getButtonsBar().getItemCount() > 0) {
            dialog.getButtonsBar().remove(dialog.getButtonsBar().getItem(0));
        }
        Button button = getBypassWorkflowButton(dialog);
        if (button != null) {
            dialog.getButtonsBar().insert(button, 0);
        }
        if (workflow != null) {
            button = getStartWorkflowButton(workflow, dialog);
            if (button != null) {
                dialog.getButtonsBar().insert(button, 0);
            }
        }
    }

    @Override
    public void initExecuteActionDialog(GWTJahiaWorkflow workflow, WorkflowActionDialog dialog) {
        initDialog(dialog);
    }

    protected void initDialog(WorkflowActionDialog dialog) {
        TabItem tab = new TabItem("Publication infos");
        tab.setLayout(new FitLayout());
        tab.setStyleName("workflow-dialog-publication-tab");
        doRefresh = false;

        PublicationStatusGrid g = new PublicationStatusGrid(publicationInfos, true, dialog.getLinker(), dialog.getContainer());
        tab.add(g);

        dialog.getTabPanel().add(tab);

        TabItem p = dialog.getTabPanel().getItem(0);
        LayoutContainer layoutContainer = new LayoutContainer(new RowLayout());
        layoutContainer.setStyleAttribute("margin", "5px");

        Map<Integer, Integer> results = new HashMap<>();
        Map<Integer, Integer> pageResults = new HashMap<>();
        for (GWTJahiaPublicationInfo info : publicationInfos) {
            Integer status = info.getStatus();
            if (status == GWTJahiaPublicationInfo.DELETED) {
                status = GWTJahiaPublicationInfo.MARKED_FOR_DELETION;
            }

            if (Boolean.TRUE.equals(info.get("isPage"))) {
                if (!pageResults.containsKey(status)) {
                    pageResults.put(status, 1);
                } else {
                    pageResults.put(status, pageResults.get(status) + 1);
                }
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
                String text = "&nbsp;" + Messages.get("label.publication." + labelKey, labelKey) + " : ";
                if (status == GWTJahiaPublicationInfo.MARKED_FOR_DELETION && results.get(status) > 10) {
                    text += "<span style=\"color:red\">" + results.get(status) + " " + (results.get(status) > 1 ? Messages.get("label.items", "Items") : Messages.get("label.item", "Item")) + "</span>";
                } else {
                    text += results.get(status) + " " + (results.get(status) > 1 ? Messages.get("label.items", "Items") : Messages.get("label.item", "Item"));
                }
                if (pageResults.get(status) != null) {
                    if (pageResults.get(status) > 1) {
                        text += " " + Messages.get("label.including", "including") + " <span style=\"color:red\">" + pageResults.get(status) + " " + Messages.get("label.pages", "pages") + "</span>";
                    } else {
                        text += " " + Messages.get("label.including", "including") + " " + pageResults.get(status) + " " + Messages.get("label.page", "page");
                    }
                }
                Html w = new Html(text);
                h.add(w);

                layoutContainer.add(h);
            }
        }
        if (i > 0) {
            p.add(layoutContainer, new BorderLayoutData(Style.LayoutRegion.NORTH, 5 + i * 20));
        }
    }

    public Button getStartWorkflowButton(final GWTJahiaWorkflowDefinition wf, final WorkflowActionDialog dialog) {
        final boolean unpublish = this instanceof UnpublicationWorkflow;
        final Button button = new Button(Messages.get(unpublish ? "label.workflow.unpublish.start" : "label.workflow.start",
                unpublish ? "Request unpublication" : "Request publication"));
        button.addStyleName("button-start");
        button.addSelectionListener(new StartWorkflowListener(dialog, unpublish, wf));
        return button;
    }

    public Button getBypassWorkflowButton(final WorkflowActionDialog dialog) {
        if (!publicationInfos.isEmpty() && Boolean.TRUE.equals(publicationInfos.get(0).isAllowedToPublishWithoutWorkflow())) {
            final Button button = new Button(Messages.get(this instanceof UnpublicationWorkflow ? "label.bypassUnpublishWorkflow" : "label.bypassWorkflow", this instanceof UnpublicationWorkflow ? "Unpublish" : "Publish"));
            button.addStyleName("button-bypassworkflow");
            button.addSelectionListener(new BypassWorkflowListener(dialog));
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
        JahiaContentManagementService.App.getInstance().publish(allUuids, nodeProperties, null, new BaseAsyncCallback<Object>() {

            @Override
            public void onApplicationFailure(Throwable caught) {
                WorkInProgressActionItem.removeStatus(status);
                Info.display("Cannot publish", "Cannot publish");
                Window.alert("Cannot publish " + caught.getMessage());
            }

            @Override
            public void onSuccess(Object result) {
                WorkInProgressActionItem.removeStatus(status);
            }
        });
    }

    public List<String> getAllUuids() {
        return getAllUuids(publicationInfos);
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

        return publicationInfos != null ? publicationInfos.equals(that.publicationInfos) : that.publicationInfos == null;
    }

    @Override
    public int hashCode() {
        int result = publicationInfos != null ? publicationInfos.hashCode() : 0;
        return result;
    }

    private static class BypassAllWorkflowListener extends SelectionListener<ButtonEvent> {
        private final EngineCards cards;
        private final Linker linker;

        public BypassAllWorkflowListener(EngineCards cards, Linker linker) {
            this.cards = cards;
            this.linker = linker;
        }

        @Override
        public void componentSelected(ButtonEvent ce) {
            final String status = Messages.get("label.publication.task", "Publishing content");
            Info.display(status, status);
            WorkInProgressActionItem.setStatus(status);

            final List<Component> components = new ArrayList<>(cards.getComponents());
            final int[] nbWF = {components.size()};
            for (Component component : components) {
                if (component instanceof WorkflowActionDialog) {
                    final WorkflowActionDialog dialog = (WorkflowActionDialog) component;
                    dialog.disableButtons();
                    List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<>();
                    if (!fillDialogProperties(dialog, nodeProperties)) {
                        return;
                    }
                    dialog.getContainer().closeEngine();
                    final PublicationWorkflow customWorkflow = (PublicationWorkflow) dialog.getCustomWorkflow();
                    final List<GWTJahiaPublicationInfo> thisWFInfo = customWorkflow.getPublicationInfos();
                    handleWorkflowActionDialog(status, nbWF, dialog, nodeProperties, customWorkflow, thisWFInfo);
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

        private void handleWorkflowActionDialog(String status, int[] nbWF, WorkflowActionDialog dialog, List<GWTJahiaNodeProperty> nodeProperties, PublicationWorkflow customWorkflow, List<GWTJahiaPublicationInfo> thisWFInfo) {
            if (thisWFInfo.get(0).isAllowedToPublishWithoutWorkflow()) {
                if (customWorkflow instanceof UnpublicationWorkflow) {
                    JahiaContentManagementService.App.getInstance().unpublish(getAllUuids(thisWFInfo, false, true),
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
        }
    }

    private static class StartAllWorkflowsListener extends SelectionListener<ButtonEvent> {
        private final EngineCards cards;
        private final Linker linker;

        public StartAllWorkflowsListener(EngineCards cards, Linker linker) {
            this.cards = cards;
            this.linker = linker;
        }

        @Override
        public void componentSelected(ButtonEvent buttonEvent) {

            Info.display(Messages.get("label.workflow.start", "Request publication"), Messages.get(
                    "message.workflow.starting", "Starting publication workflow"));
            final String status = Messages.get("label.workflow.task", "Executing workflow task");
            WorkInProgressActionItem.setStatus(status);

            final List<Component> components = new ArrayList<>(cards.getComponents());
            final int[] nbWF = {components.size()};

            final Map<String, Object> refreshData = new HashMap<>();
            refreshData.put(Linker.REFRESH_MAIN, true);
            refreshData.put("event", "workflowStarted");

            for (Component component : components) {
                if (component instanceof WorkflowActionDialog) {
                    final WorkflowActionDialog dialog = (WorkflowActionDialog) component;
                    dialog.disableButtons();
                    boolean unpublish = dialog.getCustomWorkflow() instanceof UnpublicationWorkflow;
                    List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<>();
                    if (!fillDialogProperties(dialog, nodeProperties)) {
                        return;
                    }
                    final PublicationWorkflow customWorkflow = (PublicationWorkflow) dialog.getCustomWorkflow();
                    final List<GWTJahiaPublicationInfo> thisWFInfo = customWorkflow.getPublicationInfos();

                    final Map<String, Object> map = new HashMap<>();
                    map.put("customWorkflowInfo", customWorkflow);
                    String workflowGroup = thisWFInfo.get(0).getWorkflowGroup();
                    String locale = workflowGroup.substring(0, workflowGroup.indexOf("/"));
                    JahiaContentManagementService.App.getInstance().startWorkflow(getAllUuids(thisWFInfo, false, unpublish), dialog.getWfDefinition(), nodeProperties,
                            dialog.getComments(), map, locale, getCallback(cards, nbWF, Messages.get("label.workflow.start", "Start Workflow"),
                                    Messages.get("label.workflow.cannotStart", "Cannot start workflow"), status, linker, refreshData)
                    );
                } else {
                    close(cards, nbWF, Messages.get("label.workflow.start", "Request publication"), status, linker, refreshData);
                }
            }
        }
    }

    private static class OpenPublicationWorkflowCallback extends BaseAsyncCallback<List<GWTJahiaPublicationInfo>> {

        private final Linker linker;
        private final boolean checkForUnpublication;

        public OpenPublicationWorkflowCallback(Linker linker, boolean checkForUnpublication) {
            this.linker = linker;
            this.checkForUnpublication = checkForUnpublication;
        }

        @Override
        public void onApplicationFailure(Throwable caught) {
            if (linker != null) {
                linker.loaded();
            }
            MessageBox.alert(
                    Messages.get(checkForUnpublication ? "label.unpublish" : "label.publish", checkForUnpublication ? "Unpublish" : "Publish"),
                    Messages.get(checkForUnpublication ? "message.content.unpublished.error" : "message.content.published.error", checkForUnpublication ? "Cannot be unpublished" : "Cannot be published"),
                    null
            );
        }

        @Override
        public void onSuccess(final List<GWTJahiaPublicationInfo> result) {
            if (linker != null) {
                linker.loaded();
            }
            if (result.isEmpty()) {
                MessageBox.info(
                        Messages.get(checkForUnpublication ? "label.unpublish" : "label.publish", checkForUnpublication ? "Unpublish" : "Publish"),
                        Messages.get(checkForUnpublication ? "label.publication.nothingToUnpublish" : "label.publication.nothingToPublish", checkForUnpublication ? "Nothing to unpublish" : "Nothing to publish"),
                        null
                );
            } else {
                List<GWTJahiaPublicationInfo> unpublishable = new ArrayList<>();
                for (GWTJahiaPublicationInfo info : result) {
                    Integer status = info.getStatus();
                    if (status == GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE || status == GWTJahiaPublicationInfo.CONFLICT) {
                        unpublishable.add(info);
                    }
                }

                result.removeAll(unpublishable);

                if (unpublishable.isEmpty()) {
                    PublicationWorkflow.create(result, linker, checkForUnpublication);
                } else {
                    StringBuilder message = new StringBuilder();

                    Map<Integer, List<String>> unpublishableMap = new HashMap<>();
                    for (GWTJahiaPublicationInfo info : unpublishable) {
                        Integer status = info.getStatus();
                        if (!unpublishableMap.containsKey(status)) {
                            unpublishableMap.put(status, new ArrayList<>());
                        }
                        unpublishableMap.get(status).add("<span class=\"info-publication-label\"><strong>" + info.getTitle() + "</strong>" +
                                "</span><span class=\"info-publication-path\">(" + info.getPath() + ")</span>");
                    }

                    for (Map.Entry<Integer, List<String>> entry : unpublishableMap.entrySet()) {
                        Integer status = entry.getKey();

                        Set<String> values = new HashSet<>(entry.getValue());
                        if (values.size() >= 10) {
                            values = new LinkedHashSet<>(new ArrayList<String>(values).subList(0, 10));
                            values.add("<span class=\"info-publication-more\">...</span>");
                        }

                        final String labelKey = GWTJahiaPublicationInfo.statusToLabel.get(status);

                        message.append("<div class=\"info-publication-header\">").
                                append(Messages.get("label.publication." + labelKey, labelKey)).
                                append("</div>");
                        message.append("<ul class=\"info-publication-list\">");

                        Iterator<String> valuesIterator = values.iterator();
                        while (valuesIterator.hasNext()) {
                            String value = valuesIterator.next();
                            message.append("<li>").append(value).append("</li>");
                        }
                        message.append("</ul>");
                    }
                    if (!result.isEmpty()) {
                        message.append("<div class=\"info-publication-continue\">").append(Messages.get("message.continue")).append("</div>");
                        MessageBox.confirm(Messages.get("label.publish", "Publication"), message.toString(), be -> {
                            if (be.getButtonClicked().getItemId().equalsIgnoreCase(Dialog.YES)) {
                                PublicationWorkflow.create(result, linker, checkForUnpublication);
                            }
                        });
                    } else {
                        MessageBox.info(Messages.get("label.publish", "Publication"), message.toString(), null);
                    }
                }
            }
        }
    }

    private class StartWorkflowListener extends SelectionListener<ButtonEvent> {
        private final WorkflowActionDialog dialog;
        private final boolean unpublish;
        private final GWTJahiaWorkflowDefinition wf;

        public StartWorkflowListener(WorkflowActionDialog dialog, boolean unpublish, GWTJahiaWorkflowDefinition wf) {
            this.dialog = dialog;
            this.unpublish = unpublish;
            this.wf = wf;
        }

        @Override
        public void componentSelected(ButtonEvent buttonEvent) {
            dialog.disableButtons();
            List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<>();
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
            closeDialog(dialog);
            Info.display(Messages.get(unpublish ? "label.workflow.unpublish.start" : "label.workflow.start",
                    unpublish ? "Request unpublication" : "Request publication"), Messages.get(
                    "message.workflow.starting", "Starting publication workflow"));
            final String status = Messages.get("label.workflow.task", "Executing workflow task");
            WorkInProgressActionItem.setStatus(status);

            final Map<String, Object> map = new HashMap<>();
            map.put("customWorkflowInfo", PublicationWorkflow.this);

            String workflowGroup = publicationInfos.get(0).getWorkflowGroup();
            String locale = workflowGroup.substring(0, workflowGroup.indexOf("/"));

            JahiaContentManagementService.App.getInstance().startWorkflow(getAllUuids(publicationInfos, false, true), wf, nodeProperties,
                    dialog.getComments(), map, locale, new StartWorkflowCallback(status)
            );
        }

        private class StartWorkflowCallback extends BaseAsyncCallback<Object> {

            private final String status;

            public StartWorkflowCallback(String status) {
                this.status = status;
            }

            @Override
            public void onApplicationFailure(Throwable caught) {
                WorkInProgressActionItem.removeStatus(status);
                Log.error(Messages.get("label.workflow.cannotStart", "Cannot start workflow"), caught);
                Window.alert(Messages.get("label.workflow.cannotStart", "Cannot start workflow") + caught.getMessage());
            }

            @Override
            public void onSuccess(Object result) {
                Info.display(Messages.get(unpublish ? "label.workflow.unpublish.start" : "label.workflow.start",
                        unpublish ? "Request unpublication" : "Request publication"), Messages.get(
                        "message.workflow.started", "Workflow started"));
                WorkInProgressActionItem.removeStatus(status);
                // if one wf has been started, do a refresh even on cancel
                doRefresh = true;
                // refresh only if there is no more wf
                if (dialog.getContainer() instanceof EngineCards && ((EngineCards) dialog.getContainer()).getComponents().isEmpty()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put(Linker.REFRESH_MAIN, true);
                    data.put("event", "workflowStarted");
                    dialog.getLinker().refresh(data);
                }
            }
        }
    }

    private class BypassWorkflowListener extends SelectionListener<ButtonEvent> {

        private final WorkflowActionDialog dialog;

        public BypassWorkflowListener(WorkflowActionDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void componentSelected(ButtonEvent ce) {
            dialog.disableButtons();
            List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<>();
            if (dialog.getPropertiesEditor() != null) {
                nodeProperties = dialog.getPropertiesEditor().getProperties();
            }
            closeDialog(dialog);
            doPublish(nodeProperties, dialog);
        }
    }
}
