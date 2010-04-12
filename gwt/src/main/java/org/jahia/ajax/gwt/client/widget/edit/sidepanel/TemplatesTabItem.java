package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import org.jahia.ajax.gwt.client.data.GWTJahiaBasicDataBean;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;

import java.util.Arrays;

/**
 * Side panel tab item for browsing the pages tree.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:37 PM
 */
public class TemplatesTabItem extends SidePanelTabItem {

    protected LayoutContainer treeContainer;
    protected TreeGrid<GWTJahiaNode> tree;
    protected String path;
    protected GWTJahiaNodeTreeFactory factory;
    private ContentPanel informationPanel;
    private ComboBox<GWTJahiaBasicDataBean> templateBox;

    public TemplatesTabItem(GWTSidePanelTab config) {
        super(config);
        setIcon(ContentModelIconProvider.CONTENT_ICONS.tabPages());
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        setLayout(new FitLayout());
        treeContainer = new LayoutContainer(new RowLayout(Style.Orientation.VERTICAL));
    }

    private void initTree() {
        ColumnConfig columnConfig = new ColumnConfig("displayName", "Name", 80);
        columnConfig.setRenderer(new TreeGridCellRenderer<GWTJahiaNode>());
        ColumnConfig author = new ColumnConfig("createdBy", "Author", 40);

        GWTJahiaNodeTreeFactory factory = new GWTJahiaNodeTreeFactory(JCRClientUtils.TEMPLATES_REPOSITORY);
        factory.setNodeTypes("jnt:virtualsite,jnt:page,jnt:templatesFolder,jnt:templatesSetFolder");
        this.factory = factory;
        this.factory.setSelectedPath(path);

        tree = factory.getTreeGrid(new ColumnModel(Arrays.asList(columnConfig, author)));

        tree.setAutoExpandColumn("displayName");
        tree.getTreeView().setRowHeight(25);
        tree.getTreeView().setForceFit(true);
        tree.setHeight("100%");
        tree.setIconProvider(ContentModelIconProvider.getInstance());

        this.tree.setSelectionModel(new TreeGridSelectionModel<GWTJahiaNode>() {
            @Override
            protected void handleMouseClick(GridEvent<GWTJahiaNode> e) {
                super.handleMouseClick(e);
                if (!getSelectedItem().getPath().equals(editLinker.getMainModule().getPath())) {
                    if (!getSelectedItem().getNodeTypes().contains("jnt:templatesFolder") &&
                            !getSelectedItem().getNodeTypes().contains("jnt:virtualsite")) {
                        editLinker.getMainModule().goTo(getSelectedItem().getPath(), null);
                    }
                }
            }
        });
        this.tree.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);

        tree.setContextMenu(createContextMenu(config.getTreeContextMenu(), tree.getSelectionModel()));

        treeContainer.add(tree, new RowData(1, 0.65));
        RowLayout rowLayout = new RowLayout(Style.Orientation.VERTICAL);
        informationPanel = new ContentPanel(rowLayout);
        RowData data = new RowData(1, 0.3);
        Text widget = new Text("Information");
        widget.setBorders(true);
        informationPanel.add(widget, data);
        Text widget1 = new Text(path);
        widget1.setBorders(true);
        informationPanel.add(widget1, data);
        informationPanel.setHeight("30%");
        treeContainer.add(informationPanel, new RowData(1, 0.35));
        add(treeContainer);
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        path = linker.getMainModule().getPath();
        initTree();
    }

    @Override
    public void refresh(int flag) {
        tree.getTreeStore().removeAll();
        tree.getTreeStore().getLoader().load();
    }

    public void addOpenPath(String path) {
        factory.setOpenPath(path);
    }


    public GWTJahiaNode getRootTemplate() {
        GWTJahiaNode jahiaNode = tree.getSelectionModel().getSelectedItem();
        while (jahiaNode.getParent() != null) {
            jahiaNode = (GWTJahiaNode) jahiaNode.getParent();
        }
        return jahiaNode;
    }

    public void handleNewModuleSelection(Module selectedModule) {
        informationPanel.removeAll();
        if (selectedModule != null) {
            informationPanel.setLayout(new RowLayout(Style.Orientation.VERTICAL));
            RowData data = new RowData(1, 0.05);

            Text widget = new Text("Name: " + selectedModule.getNode().getDisplayName());
            widget.setToolTip("Path " + selectedModule.getNode().getPath());
            final String style = "x-info-mc";
            widget.setStyleName(style);
            informationPanel.add(widget, data);

            widget = new Text("Node type: " + selectedModule.getNode().getNodeTypes().get(0));
            widget.setStyleName(style);
            informationPanel.add(widget, data);

            boolean contributionActivated = selectedModule.getNode().getNodeTypes().contains("jmix:contributeMode");
            widget = new Text("Contribution Mode: " + (contributionActivated ? "activated" : "disabled"));
            widget.setStyleName(style);
            widget.setStyleAttribute("font-weight", "bold");
            informationPanel.add(widget, data);

            widget = new Text(contributionActivated ? "This object is editable in contribution mode" :
                    "This object is not editable in contribution mode");
            widget.setStyleName(style);
            informationPanel.add(widget, data);

            widget = new Text("Content types allowed: ");
            widget.setStyleName(style);
            widget.setStyleAttribute("font-weight", "bold");
            informationPanel.add(widget, data);

            widget = new Text(!"".equals(selectedModule.getNodeTypes()) ? selectedModule.getNodeTypes() : "All");
            widget.setStyleName(style);
            informationPanel.add(widget, data);

            FormPanel formPanel = generateFormPanel(selectedModule, style);
            data = new RowData(1, 0.5);
            informationPanel.add(formPanel, data);
        }
        informationPanel.layout();
    }

    private FormPanel generateFormPanel(Module selectedModule, String style) {
        Text widget;
        FormPanel formPanel = new FormPanel();
        formPanel.setFrame(false);
        formPanel.setHeaderVisible(false);
        formPanel.setLabelAlign(FormPanel.LabelAlign.LEFT);
        RadioGroup radioGroup = new RadioGroup("Locked");
        radioGroup.setFieldLabel("Locked");
        Radio radio = new Radio();
        radio.setBoxLabel("Yes");
        radio.setValue(selectedModule.isLocked());
        radioGroup.add(radio);
        radio = new Radio();
        radio.setBoxLabel("No");
        radio.setValue(!selectedModule.isLocked());
        radioGroup.add(radio);
        radio.addListener(Events.Change, new Listener<FieldEvent>() {
            /**
             * Sent when an event that the listener has registered for occurs.
             *
             * @param be the event which occurred
             */
            public void handleEvent(FieldEvent be) {
                Info.display("Test", (Boolean) be.getValue() ? "Unlocking" : "Locking");
                if ((Boolean) be.getValue()) {
                    // Unlocking
                    ContentActions.switchTemplateLocked(editLinker, false);
                } else {
                    // Locking
                    ContentActions.switchTemplateLocked(editLinker, true);
                }
            }
        });
        formPanel.add(radioGroup);
        widget = new Text((selectedModule.isLocked() ? "Users are not allowed to update this node" :
                "Users are allowed to update this node"));
        widget.setStyleName(style);
        formPanel.add(widget);
        radioGroup = new RadioGroup("Shared");
        radioGroup.setFieldLabel("Shared");
        radio = new Radio();
        radio.setBoxLabel("Yes");
        radio.setValue(selectedModule.isShared());
        radioGroup.add(radio);
        radio = new Radio();
        radio.setBoxLabel("No");
        radio.setValue(!selectedModule.isShared());
        radioGroup.add(radio);
        radio.addListener(Events.Change, new Listener<FieldEvent>() {
            /**
             * Sent when an event that the listener has registered for occurs.
             *
             * @param be the event which occurred
             */
            public void handleEvent(FieldEvent be) {
                Info.display("Test", (Boolean) be.getValue() ? "Unsharing" : "Sharing");
                if ((Boolean) be.getValue()) {
                    // Unlocking
                    ContentActions.switchTemplateShared(editLinker, false);
                } else {
                    // Locking
                    ContentActions.switchTemplateShared(editLinker, true);
                }
            }
        });
        formPanel.add(radioGroup);
        widget = new Text((selectedModule.isLocked() ?
                "All instances of this content are linked any change will apply on all instances" :
                "All instances of this content are unlinked any change will apply only on this instance"));
        widget.setStyleName(style);
        formPanel.add(widget);
        return formPanel;
    }
}