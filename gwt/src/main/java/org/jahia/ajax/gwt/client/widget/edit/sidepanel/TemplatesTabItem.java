package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.Arrays;

/**
 * Side panel tab item for browsing the pages tree.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:37 PM
 */
public class TemplatesTabItem extends BrowseTabItem {

//    protected LayoutContainer treeContainer;
    protected LayoutContainer contentContainer;
    protected ListLoader<ListLoadResult<GWTJahiaNode>> listLoader;
    protected ListStore<GWTJahiaNode> contentStore;
    protected ListView<GWTJahiaNode> listView;
//    protected TreeGrid<GWTJahiaNode> tree;
//    protected String path;
//    private ContentPanel informationPanel;
//    private ComboBox<GWTJahiaBasicDataBean> templateBox;

    public TemplatesTabItem(GWTSidePanelTab config) {
        super(config);
        setIcon(StandardIconsProvider.STANDARD_ICONS.tabPages());

        this.tree.setSelectionModel(new TreeGridSelectionModel<GWTJahiaNode>() {
            @Override
            protected void handleMouseClick(GridEvent<GWTJahiaNode> e) {
                super.handleMouseClick(e);
                if (!getSelectedItem().getPath().equals(editLinker.getMainModule().getPath())) {
                    if (!getSelectedItem().getNodeTypes().contains("jnt:virtualsite") && !getSelectedItem().getNodeTypes().contains("jnt:templatesFolder")) {
                        editLinker.getMainModule().goTo(getSelectedItem().getPath(), null);
                        listView.mask("Loading");
                        listLoader.load(getSelectedItem());
                    }
                }
            }
        });
        this.tree.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);

        contentContainer = new LayoutContainer();
        contentContainer.setId("images-view");
        contentContainer.setBorders(true);
        contentContainer.setScrollMode(Style.Scroll.AUTOY);

        // data proxy
        RpcProxy<ListLoadResult<GWTJahiaNode>> listProxy = new RpcProxy<ListLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(final Object gwtJahiaFolder, final AsyncCallback<ListLoadResult<GWTJahiaNode>> listAsyncCallback) {
                JahiaContentManagementService.App.getInstance()
                        .getAllWrappers(((GWTJahiaNode)gwtJahiaFolder).getPath(), Arrays.asList(GWTJahiaNode.ICON, GWTJahiaNode.THUMBNAILS, GWTJahiaNode.TAGS, "j:applyOn", "j:key"), new BaseAsyncCallback<ListLoadResult<GWTJahiaNode>>() {
                            public void onApplicationFailure(Throwable caught) {
                                listAsyncCallback.onFailure(caught);
                                listView.unmask();
                            }

                            public void onSuccess(ListLoadResult<GWTJahiaNode> result) {
                                result.getData().add(0,(GWTJahiaNode) gwtJahiaFolder);
                                listAsyncCallback.onSuccess(result);
                                listView.unmask();
                            }
                        });
            }
        };

        listLoader = new BaseListLoader<ListLoadResult<GWTJahiaNode>>(listProxy);
        listLoader.addLoadListener(new LoadListener() {
            @Override
            public void loaderLoad(LoadEvent le) {
                if (!le.isCancelled()) {
                    contentContainer.unmask();
                }
            }
        });

        contentStore = new ListStore<GWTJahiaNode>(listLoader);

        listView = new ListView<GWTJahiaNode>() {
            @Override
            protected GWTJahiaNode prepareData(GWTJahiaNode model) {
                String s = model.getName();
                if (model.getNodeTypes().contains("jnt:wrapper")) {
                    model.set("type", "Wrapper");
                } else {
                    model.set("type", "Page");
                }
                model.set("shortName", Format.ellipse(s, 14));
                model.set("nameLabel", Messages.get("label.name", "Name"));
                model.set("authorLabel", Messages.get("versioning_author", "Auhor"));
                model.set("tagsLabel", Messages.get("org.jahia.jcr.edit.tags.tab", "tags"));
                model.set("template", model.get("j:template"));
                model.set("applyOn", model.get("j:applyOn"));
                model.set("key", model.get("j:key"));

                return model;
            }

        };
        listView.setStyleAttribute("overflow-x", "hidden");
        listView.setStore(contentStore);
        listView.setTemplate(getTemplate());
        listView.setItemSelector("div.thumb-wrap");
        listView.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        listView.getSelectionModel().addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent<GWTJahiaNode>>() {
                    public void handleEvent(SelectionChangedEvent<GWTJahiaNode> se) {
                        editLinker.getMainModule().goTo(se.getSelectedItem().getPath(), null);
                    }
                });

        contentStore.setSortField("display");
        contentContainer.add(listView);

        tree.setContextMenu(createContextMenu(config.getTreeContextMenu(), tree.getSelectionModel()));
        listView.setContextMenu(createContextMenu(config.getTableContextMenu(), listView.getSelectionModel()));

        VBoxLayoutData contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(1);
        add(contentContainer, contentVBoxData);
    }

//    private void initTree() {
//
//        treeContainer.add(tree, new RowData(1, 0.65));
//        RowLayout rowLayout = new RowLayout(Style.Orientation.VERTICAL);
//        informationPanel = new ContentPanel(rowLayout);
//        RowData data = new RowData(1, 0.3);
//        Text widget = new Text("Information");
//        widget.setBorders(true);
//        informationPanel.add(widget, data);
//        Text widget1 = new Text(path);
//        widget1.setBorders(true);
//        informationPanel.add(widget1, data);
//        informationPanel.setHeight("30%");
//        treeContainer.add(informationPanel, new RowData(1, 0.35));
//        add(treeContainer);
//    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
//        path = linker.getMainModule().getPath();
//        initTree();
    }

    @Override
    public void refresh(int flag) {
        if ((flag & Linker.REFRESH_PAGES) != 0) {
            tree.getTreeStore().removeAll();
            listView.getStore().removeAll();
            tree.getTreeStore().getLoader().load();

        }
    }

    public GWTJahiaNode getRootTemplate() {
        return tree.getStore().getAt(0);
    }

//    public void handleNewModuleSelection(Module selectedModule) {
//        informationPanel.removeAll();
//        if (selectedModule != null) {
//            informationPanel.setLayout(new RowLayout(Style.Orientation.VERTICAL));
//            RowData data = new RowData(1, 0.05);
//
//            Text widget = new Text("Name: " + selectedModule.getNode().getDisplayName());
//            widget.setToolTip("Path " + selectedModule.getNode().getPath());
//            final String style = "x-info-mc";
//            widget.setStyleName(style);
//            informationPanel.add(widget, data);
//
//            widget = new Text("Node type: " + selectedModule.getNode().getNodeTypes().get(0));
//            widget.setStyleName(style);
//            informationPanel.add(widget, data);
//
//            boolean contributionActivated = selectedModule.getNode().getNodeTypes().contains("jmix:contributeMode");
//            widget = new Text("Contribution Mode: " + (contributionActivated ? "activated" : "disabled"));
//            widget.setStyleName(style);
//            widget.setStyleAttribute("font-weight", "bold");
//            informationPanel.add(widget, data);
//
//            widget = new Text(contributionActivated ? "This object is editable in contribution mode" :
//                    "This object is not editable in contribution mode");
//            widget.setStyleName(style);
//            informationPanel.add(widget, data);
//
//            widget = new Text("Content types allowed: ");
//            widget.setStyleName(style);
//            widget.setStyleAttribute("font-weight", "bold");
//            informationPanel.add(widget, data);
//
//            widget = new Text(!"".equals(selectedModule.getNodeTypes()) ? selectedModule.getNodeTypes() : "All");
//            widget.setStyleName(style);
//            informationPanel.add(widget, data);
//
//            FormPanel formPanel = generateFormPanel(selectedModule, style);
//            data = new RowData(1, 0.5);
//            informationPanel.add(formPanel, data);
//        }
//        informationPanel.layout();
//    }
//
//    private FormPanel generateFormPanel(Module selectedModule, String style) {
//        Text widget;
//        FormPanel formPanel = new FormPanel();
//        formPanel.setFrame(false);
//        formPanel.setHeaderVisible(false);
//        formPanel.setLabelAlign(FormPanel.LabelAlign.LEFT);
//        RadioGroup radioGroup = new RadioGroup("Locked");
//        radioGroup.setFieldLabel("Locked");
//        Radio radio = new Radio();
//        radio.setBoxLabel("Yes");
//        radio.setValue(selectedModule.isLocked());
//        radioGroup.add(radio);
//        radio = new Radio();
//        radio.setBoxLabel("No");
//        radio.setValue(!selectedModule.isLocked());
//        radioGroup.add(radio);
//        radio.addListener(Events.Change, new Listener<FieldEvent>() {
//            /**
//             * Sent when an event that the listener has registered for occurs.
//             *
//             * @param be the event which occurred
//             */
//            public void handleEvent(FieldEvent be) {
//                Info.display("Test", (Boolean) be.getValue() ? "Unlocking" : "Locking");
//                if ((Boolean) be.getValue()) {
//                    // Unlocking
//                    ContentActions.switchTemplateLocked(editLinker, false);
//                } else {
//                    // Locking
//                    ContentActions.switchTemplateLocked(editLinker, true);
//                }
//            }
//        });
//        formPanel.add(radioGroup);
//        widget = new Text((selectedModule.isLocked() ? "Users are not allowed to update this node" :
//                "Users are allowed to update this node"));
//        widget.setStyleName(style);
//        formPanel.add(widget);
//        radioGroup = new RadioGroup("Shared");
//        radioGroup.setFieldLabel("Shared");
//        radio = new Radio();
//        radio.setBoxLabel("Yes");
//        radio.setValue(selectedModule.isShared());
//        radioGroup.add(radio);
//        radio = new Radio();
//        radio.setBoxLabel("No");
//        radio.setValue(!selectedModule.isShared());
//        radioGroup.add(radio);
//        radio.addListener(Events.Change, new Listener<FieldEvent>() {
//            /**
//             * Sent when an event that the listener has registered for occurs.
//             *
//             * @param be the event which occurred
//             */
//            public void handleEvent(FieldEvent be) {
//                Info.display("Test", (Boolean) be.getValue() ? "Unsharing" : "Sharing");
//                if ((Boolean) be.getValue()) {
//                    // Unlocking
//                    ContentActions.switchTemplateShared(editLinker, false);
//                } else {
//                    // Locking
//                    ContentActions.switchTemplateShared(editLinker, true);
//                }
//            }
//        });
//        formPanel.add(radioGroup);
//        widget = new Text((selectedModule.isLocked() ?
//                "All instances of this content are linked any change will apply on all instances" :
//                "All instances of this content are unlinked any change will apply only on this instance"));
//        widget.setStyleName(style);
//        formPanel.add(widget);
//        return formPanel;
//    }


    @Override
    protected boolean acceptNode(GWTJahiaNode node) {
        return node.getNodeTypes().contains("jnt:page");
    }

    public native String getTemplate() /*-{
    return ['<tpl for=".">',
        '<div style="padding: 5px ;border-bottom: 1px solid #D9E2F4;float: left;width: 100%;" class="thumb-wrap" id="{name}">',
        '<div><b>{type}: </b>{name}</div>',
        '<div><b>Template: </b>{template}</div>',
        '<div><b>Key: </b>{key}</div>',
        '<div><b>Apply on : </b>{applyOn}</div>',
        '<div style="padding-left: 10px; padding-top: 10px; clear: left">{description}</div></div></tpl>',
        '<div class="x-clear"></div>'].join("");
    }-*/;

}