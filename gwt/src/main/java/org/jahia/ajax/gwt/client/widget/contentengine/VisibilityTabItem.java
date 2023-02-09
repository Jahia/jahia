/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.DualListField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaCreateEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaEditEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.*;


/**
 * Display information about node visibility and allows to add/edit
 * visibility conditions on that node
 */
public class VisibilityTabItem extends EditEngineTabItem {

    private transient Map<String, PropertiesEditor> propertiesEditorMap = new HashMap<String, PropertiesEditor>();
    private transient ListStore<GWTJahiaNode> conditionsStore;
    private transient List<GWTJahiaNode> deleted;
    private transient CheckBox allConditionsMatch;

    private transient boolean changed;
    private transient boolean oneTrue;
    private transient boolean oneFalse;
    private transient boolean oneUnknown;

    private transient StatusBar statusBar;

    public VisibilityTabItem() {
        setHandleCreate(false);
    }

    @Override
    public void init(NodeHolder engine, AsyncTabItem tab, String language) {
        if (engine.getNode() == null) {
            return;
        }
        final GWTJahiaNode node = engine.getNode();
        tab.setLayout(new RowLayout());
        tab.setProcessed(true);

        final boolean editable = (!engine.isExistingNode() || (PermissionsUtils.isPermitted("jcr:modifyProperties", engine.getNode()) && !engine.getNode().isLocked()));

        LayoutContainer header = new LayoutContainer(new FillLayout(Style.Orientation.VERTICAL));
        tab.add(header, new RowData(1, 25, new Margins(5)));

        final HorizontalPanel timeZonePanel = new HorizontalPanel();
        timeZonePanel.setVerticalAlign(Style.VerticalAlignment.MIDDLE);
        timeZonePanel.add(new HTML(Messages.get("label.visibility.timeZone", "All the visibility conditions are using the server timezone") +
                ":&nbsp;" + JahiaGWTParameters.getServerDisplayableTimeZone()));
        header.add(timeZonePanel);

        LayoutContainer top = new LayoutContainer(new FillLayout(Style.Orientation.VERTICAL));
        tab.add(top, new RowData(1, 60, new Margins(5)));

        final HorizontalPanel statusPanel = new HorizontalPanel();
        statusPanel.setVerticalAlign(Style.VerticalAlignment.MIDDLE);
        top.add(statusPanel);
        statusBar = new StatusBar(node, statusPanel, editable);

        HorizontalPanel addPanel = new HorizontalPanel();
        addPanel.setVerticalAlign(Style.VerticalAlignment.MIDDLE);
        if (editable) {
            top.add(addPanel);
        }

        addPanel.add(new HTML(Messages.get("label.visibility.allConditionsMatch", "All conditions should match") + ":&nbsp;"));
        allConditionsMatch = new CheckBox();
        allConditionsMatch.setEnabled(editable);
        allConditionsMatch.addListener(Events.Change, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                changed = true;
                refreshConditionsList();
                statusBar.update();
            }
        });
        addPanel.add(allConditionsMatch);
        addPanel.add(new HTML("&nbsp;&nbsp;&nbsp;" + Messages.get("label.visibility.addCondition", "Add new condition") + ":&nbsp;"));
        final ListStore<GWTJahiaNodeType> typesStore = new ListStore<GWTJahiaNodeType>();
        final ComboBox<GWTJahiaNodeType> types = new ComboBox<GWTJahiaNodeType>();
        types.setDisplayField("label");
        types.setStore(typesStore);
        types.setTypeAhead(true);
        types.setTriggerAction(ComboBox.TriggerAction.ALL);
        types.setForceSelection(true);
        types.setEditable(false);
        types.setWidth(250);
        types.setEnabled(editable);
        addPanel.add(types);

        final Map<String, GWTJahiaNodeType> typesMap = new HashMap<String, GWTJahiaNodeType>();

        final Button add = new Button();
        add.setIcon(StandardIconsProvider.STANDARD_ICONS.plusRound());
        add.setEnabled(false);
        addPanel.add(add);

        conditionsStore = new ListStore<GWTJahiaNode>();
        deleted = new ArrayList<GWTJahiaNode>();

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig name = new ColumnConfig("name", Messages.get("label.title"), 500);
        name.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public Object render(GWTJahiaNode condition, String property, com.extjs.gxt.ui.client.widget.grid.ColumnData config, int rowIndex, int colIndex,
                                 ListStore<GWTJahiaNode> store, Grid<GWTJahiaNode> grid) {
                String typeName = condition.getNodeTypes().get(0);
                XTemplate tpl = typesMap.get(typeName).get("compiledTemplate");
                convertStringToDate(condition);
                return tpl.applyTemplate(com.extjs.gxt.ui.client.util.Util.getJsObject(condition));
            }
        });
        name.setFixed(true);
        configs.add(name);

        ColumnConfig conditionStatus = new ColumnConfig("status", Messages.get("label.status"), 100);
        conditionStatus.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public Object render(GWTJahiaNode model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                Boolean match = model.get("conditionMatch");
                if (match != null) {
                    if (match.equals(Boolean.TRUE)) {
                        AbstractImagePrototype icon = ToolbarIconProvider.getInstance().getIcon("visibilityStatusGreen");
                        return icon.getHTML();
                    } else {
                        AbstractImagePrototype icon = ToolbarIconProvider.getInstance().getIcon("visibilityStatusRed");
                        return icon.getHTML();
                    }
                }
                AbstractImagePrototype icon = ToolbarIconProvider.getInstance().getIcon("visibilityStatusUnknown");
                return icon.getHTML();
            }
        });
        configs.add(conditionStatus);

        if (editable) {
            ColumnConfig remove = new ColumnConfig("remove", Messages.get("label.remove"), 100);
            remove.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                public Object render(final GWTJahiaNode condition, String property, com.extjs.gxt.ui.client.widget.grid.ColumnData config, final int rowIndex, final int colIndex, ListStore<GWTJahiaNode> listStore, final Grid<GWTJahiaNode> grid) {
                    final Button button;
                    if (condition.get("node-removed") == null || condition.isMarkedForDeletion()) {
                        button = new Button(Messages.get("label.remove"));
                        button.addStyleName("button-remove");
                        button.setIcon(StandardIconsProvider.STANDARD_ICONS.minusRound());
                    } else {
                        button = new Button(Messages.get("label.undelete"));
                        button.addStyleName("button-undelete");
                        button.setIcon(StandardIconsProvider.STANDARD_ICONS.restore());
                    }
                    button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                        @Override
                        public void componentSelected(ButtonEvent buttonEvent) {
                            conditionsStore.remove(condition);
                            propertiesEditorMap.remove(condition.getPath());
                            if (condition.get("new-node") == null) {
                                deleted.add(condition);
                                condition.set("node-removed", Boolean.TRUE);
                            }
                            changed = true;
                            refreshConditionsList();
                            statusBar.update();
                        }
                    });
                    return button;
                }
            });
            configs.add(remove);
        }

//        ColumnConfig publicationInfo = new ColumnConfig("publish", Messages.get("label.publish"), 100);
//        publicationInfo.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
//            public Object render(final GWTJahiaNode conditionNode, String property, ColumnData config, int rowIndex, int colIndex,
//                                 ListStore<GWTJahiaNode> store, Grid<GWTJahiaNode> grid) {
//                final GWTJahiaPublicationInfo info = conditionNode.getAggregatedPublicationInfo();
//                HorizontalPanel p = new HorizontalPanel();
//                p.setVerticalAlign(Style.VerticalAlignment.MIDDLE);
//                Integer infoStatus;
//                if (info != null) {
//                    if (conditionNode.get("node-modified") != null) {
//                        infoStatus = GWTJahiaPublicationInfo.MODIFIED;
//                    } else {
//                        infoStatus = info.getStatus();
//                    }
//                } else {
//                    infoStatus = GWTJahiaPublicationInfo.NOT_PUBLISHED;
//                }
//                PropertiesEditor pe = propertiesEditorMap.get(conditionNode.getPath());
//                if (pe != null) {
//                    if (pe.getProperties(false, true, true).isEmpty()) {
//                        infoStatus = GWTJahiaPublicationInfo.MODIFIED;
//                    }
//                }
//
//                Image res = GWTJahiaPublicationInfo.renderPublicationStatusImage(infoStatus);
//                p.add(res);
//                final CheckBox checkbox = new CheckBox();
//                if (infoStatus == GWTJahiaPublicationInfo.PUBLISHED) {
//                    checkbox.setEnabled(false);
//                }
//                checkbox.addListener(Events.Change, new Listener<ComponentEvent>() {
//                    public void handleEvent(ComponentEvent event) {
//                        conditionNode.set("node-published", checkbox.getValue());
//                    }
//                });
//                p.add(checkbox);
//                return p;
//            }
//        });
//        configs.add(publicationInfo);

        ColumnModel cm = new ColumnModel(configs);

        final Grid<GWTJahiaNode> conditions = new Grid<GWTJahiaNode>(conditionsStore, cm);
        conditions.setAutoExpandColumn("name");

        tab.add(conditions, new RowData(1, 0.5));

        final LayoutContainer form = new LayoutContainer(new FitLayout());
        form.setEnabled(editable);
        tab.add(form, new RowData(1, 0.5));

        final GridSelectionModel<GWTJahiaNode> selectionModel = conditions.getSelectionModel();
        selectionModel.setSelectionMode(Style.SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                form.removeAll();
                final GWTJahiaNode conditionNode = selectionModel.getSelectedItem();
                if (conditionNode != null) {
                    PropertiesEditor pe = propertiesEditorMap.get(conditionNode.getPath());
                    if (pe != null) {
                        form.add(pe);
                        form.layout();
                    } else {
                        if (conditionNode.get("new-node") != null) {
                            final GWTJahiaNodeType type = types.getSelection().get(0);
                            JahiaContentManagementService.App.getInstance().initializeCreateEngine(type.getName(),
                                    node.getPath(), null, new BaseAsyncCallback<GWTJahiaCreateEngineInitBean>() {
                                public void onSuccess(GWTJahiaCreateEngineInitBean result) {
                                    PropertiesEditor pe = new PropertiesEditor(Arrays.asList(type), new HashMap<String, GWTJahiaNodeProperty>(), Arrays.asList("content"));
                                    pe.setChoiceListInitializersValues(result.getChoiceListInitializersValues());
                                    pe.renderNewFormPanel();
                                    propertiesEditorMap.put(conditionNode.getPath(), pe);
                                    form.add(pe);
                                    form.layout();
                                    addFieldListener(pe, conditions, conditionNode);
                                }
                            });
                        } else {
                            JahiaContentManagementService.App.getInstance().initializeEditEngine(conditionNode.getPath(), false, new BaseAsyncCallback<GWTJahiaEditEngineInitBean>() {
                                public void onSuccess(GWTJahiaEditEngineInitBean result) {
                                    if (result == null) {
                                        return;
                                    }

                                    PropertiesEditor pe = new PropertiesEditor(result.getNodeTypes(), result.getProperties(), Arrays.asList("content"));
                                    pe.setChoiceListInitializersValues(result.getInitializersValues());
                                    pe.renderNewFormPanel();
                                    propertiesEditorMap.put(conditionNode.getPath(), pe);
                                    form.add(pe);
                                    form.layout();
                                    addFieldListener(pe, conditions, conditionNode);
                                }
                            });
                        }
                    }
                }
            }
        });

        add.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                GWTJahiaNode newCondition = new GWTJahiaNode();
                List<GWTJahiaNodeType> nodeTypes = types.getSelection();
                String nodeTypeName = nodeTypes.get(0).getName();
                newCondition.setNodeTypes(Arrays.asList(nodeTypeName));
                String nodeName = nodeTypeName + new Date().getTime();
                newCondition.setName(nodeName);
                newCondition.setPath(node.getPath() + "/j:conditionalVisibility/" + nodeName);
                newCondition.set("new-node", Boolean.TRUE);
                conditionsStore.add(newCondition);
                selectionModel.select(Arrays.asList(newCondition), false);

                changed = true;
                refreshConditionsList();
                statusBar.update();
            }
        });

        if (editable) {
            types.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNodeType>() {
                @Override
                public void selectionChanged(SelectionChangedEvent<GWTJahiaNodeType> se) {
                    add.enable();
                }
            });
        }

        JahiaContentManagementService.App.getInstance().getVisibilityInformation(node.getPath(), new BaseAsyncCallback<ModelData>() {
            public void onSuccess(ModelData result) {
                List<GWTJahiaNodeType> l = (List<GWTJahiaNodeType>) result.get("types");
                typesStore.add(l);
                types.setSelection(Arrays.asList(l.get(0)));
                for (GWTJahiaNodeType type : l) {
                    typesMap.put(type.getName(), type);
                    XTemplate tpl = XTemplate.create((String) type.get("xtemplate"));
                    tpl.compile();
                    type.set("compiledTemplate", tpl);
                }

                conditionsStore.add((List<GWTJahiaNode>) result.get("conditions"));

                refreshConditionsList();

                allConditionsMatch.setOriginalValue(result.<Boolean>get("j:forceMatchAllConditions"));
                allConditionsMatch.setValue(result.<Boolean>get("j:forceMatchAllConditions"));

                statusBar.initStatusBar((GWTJahiaPublicationInfo) result.get("publicationInfo"), (Boolean) result.get("liveStatus"));
            }
        });

        tab.layout();
    }

    private void convertStringToDate(GWTJahiaNode condition) {
        for (Map.Entry<String, Object> entry : condition.getProperties().entrySet()) {
            if (entry.getValue() instanceof String) {
                try {
                    Date d = FormFieldCreator.dateTimeFormat.parseStrict((String) entry.getValue());
                    condition.set(entry.getKey(), d);
                } catch (IllegalArgumentException e) {
                    // not a date, ignore
                }
            }
        }
    }

    private void refreshConditionsList() {
        oneTrue = false;
        oneFalse = false;
        oneUnknown = false;
        for (GWTJahiaNode model : conditionsStore.getModels()) {
            Boolean match = model.get("conditionMatch");
            if (match != null) {
                if (match.equals(Boolean.TRUE)) {
                    oneTrue = true;
                } else {
                    oneFalse = true;
                }
            }
            if (model.get("new-node") != null) {
                oneUnknown = true;
            }
        }
    }


    public void addFieldListener(final PropertiesEditor pe, final Grid grid, final GWTJahiaNode conditionNode) {
        for (final Field<?> field : pe.getFields()) {
            final Field f;
            if (field instanceof PropertiesEditor.PropertyAdapterField) {
                f = ((PropertiesEditor.PropertyAdapterField) field).getField();
            } else {
                f = field;
            }
            if (f instanceof DualListField) {
                ((DualListField)f).getToList().getStore().addStoreListener(new StoreListener() {
                    @Override
                    public void handleEvent(StoreEvent e) {
                        Object value;
                        Object displayValue = new ArrayList();
                        List v = ((DualListField) f).getToList().getStore().getModels();
                        for (Object o : v) {
                            if (o instanceof GWTJahiaValueDisplayBean) {
                                ((List)displayValue).add(((GWTJahiaValueDisplayBean) o).getDisplay());
                            } else {
                                ((List)displayValue).add(o);
                            }
                        }
                        conditionNode.set(field.getName(), displayValue);
                        conditionNode.set("node-modified", Boolean.TRUE);
                        changed = true;
                        grid.getView().refresh(false);
                        statusBar.update();
                    }
                });
            } else {
                field.addListener(Events.Change, new Listener<BaseEvent>() {
                    public void handleEvent(BaseEvent be) {
                        Object value = field.getValue();
                        if (value instanceof GWTJahiaValueDisplayBean) {
                            value = ((GWTJahiaValueDisplayBean)value).getDisplay();
                        }
                        conditionNode.set(field.getName(), value);
                        conditionNode.set("node-modified", Boolean.TRUE);
                        changed = true;
                        grid.getView().refresh(false);
                        statusBar.update();
                    }
                });
            }
        }
    }

    @Override
    public void setProcessed(boolean processed) {
        if (!processed) {
            propertiesEditorMap = new HashMap<String, PropertiesEditor>();
            conditionsStore = null;
            deleted = null;
            allConditionsMatch = null;
            changed = false;
            oneTrue = false;
            oneFalse = false;
            oneUnknown = false;
            statusBar = null;
        }
        super.setProcessed(processed);
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, List<GWTJahiaNode> chidren, GWTJahiaNodeACL acl) {
        if (conditionsStore != null) {
            List<GWTJahiaNode> list = conditionsStore.getModels();
            for (GWTJahiaNode jahiaNode : list) {
                PropertiesEditor pe = propertiesEditorMap.get(jahiaNode.getPath());
                if (pe != null) {
                    jahiaNode.set("gwtproperties", pe.getProperties(false, true, false));
                }
            }
            list.addAll(deleted);
            node.set("visibilityConditions", list);
            if (allConditionsMatch.isDirty()) {
                node.set("node-visibility-forceMatchAllConditions", allConditionsMatch.getValue());
            }
        }
    }

    class StatusBar {
        private GWTJahiaNode node;
        private HorizontalPanel statusPanel;
        private boolean editable;
        private GWTJahiaPublicationInfo info;
        private LayoutContainer statusContainer;
        private CheckBox checkbox;
        private LayoutContainer publicationInfoContainer;

        StatusBar(GWTJahiaNode node, HorizontalPanel statusPanel, boolean editable) {
            this.node = node;
            this.statusPanel = statusPanel;
            this.editable = editable;
            statusPanel.add(new HTML(Messages.get("label.visibility.currentStatusInLive", "Current status in live") + ":&nbsp;"));
        }

        private void initStatusBar(final GWTJahiaPublicationInfo info, final Boolean liveStatus) {
            this.info = info;

            if (Boolean.TRUE.equals(liveStatus)) {
                statusPanel.add(ToolbarIconProvider.getInstance().getIcon("visibilityStatusGreen").createImage());
            } else if (Boolean.FALSE.equals(liveStatus)) {
                statusPanel.add(ToolbarIconProvider.getInstance().getIcon("visibilityStatusRed").createImage());
            } else {
                statusPanel.add(new HTML("not published"));
            }

            statusPanel.add(new HTML("&nbsp;&nbsp;&nbsp;" + Messages.get("label.visibility.currentConditionsResult", "Current conditions result") + ":&nbsp;"));
            statusContainer = new LayoutContainer(new FitLayout());
            statusPanel.add(statusContainer);

            GWTJahiaPublicationInfo aggregatedPublicationInfo = node.getAggregatedPublicationInfo();
            if (aggregatedPublicationInfo!=null && aggregatedPublicationInfo.getStatus() != GWTJahiaPublicationInfo.NOT_PUBLISHED) {
                statusPanel.add(new HTML("&nbsp;&nbsp;&nbsp;" + Messages.get("label.visibility.publicationStatus", "Publication status") + ":&nbsp;"));
                publicationInfoContainer = new LayoutContainer(new FitLayout());
                statusPanel.add(publicationInfoContainer);

                statusPanel.add(new HTML("&nbsp;&nbsp;&nbsp;" + Messages.get("label.visibility.publishOnSave", "Publish conditions on save") + ":&nbsp;"));

                checkbox = new CheckBox();
                checkbox.setEnabled(editable);
                checkbox.addListener(Events.Change, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent event) {
                        node.set("conditions-published", checkbox.getValue());
                    }
                });
                statusPanel.add(checkbox);

            }
            update();
        }

        private void update() {
            if (statusContainer != null) {
                statusContainer.removeAll();
                if (oneUnknown && (allConditionsMatch.getValue() || (!oneTrue && !oneFalse))) {
                    statusContainer.add(ToolbarIconProvider.getInstance().getIcon(
                            "visibilityStatusUnknown").createImage());
                } else if ((allConditionsMatch.getValue() && !oneFalse) ||
                           (!allConditionsMatch.getValue() && oneTrue) || (!oneTrue && !oneFalse)) {
                    statusContainer.add(ToolbarIconProvider.getInstance().getIcon(
                            "visibilityStatusGreen").createImage());
                } else {
                    statusContainer.add(ToolbarIconProvider.getInstance().getIcon("visibilityStatusRed").createImage());
                }
            }
            if (publicationInfoContainer != null) {
                int infoStatus;
                if (info != null) {
                    infoStatus = info.getStatus();
                    if (changed && infoStatus == GWTJahiaPublicationInfo.PUBLISHED) {
                        infoStatus = GWTJahiaPublicationInfo.MODIFIED;
                    }
                } else {
                    if (changed) {
                        infoStatus = GWTJahiaPublicationInfo.NOT_PUBLISHED;
                    } else {
                        infoStatus = GWTJahiaPublicationInfo.PUBLISHED;
                    }
                }
                Image res = GWTJahiaPublicationInfo.renderPublicationStatusImage(infoStatus);
                publicationInfoContainer.removeAll();
                publicationInfoContainer.add(res);
                checkbox.setEnabled(infoStatus != GWTJahiaPublicationInfo.PUBLISHED);
            }

            statusPanel.layout();
        }
    }


}
