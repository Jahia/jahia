package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.List;

/**
 * User: toto
 * Date: Sep 4, 2009
 * Time: 4:14:11 PM
 */
public class EditActions {

    /**
     * Create page
     *
     * @param linker
     */
    public static void createPage(final Linker linker) {
        if (linker.getMainNode() != null) {
            JahiaContentDefinitionService.App.getInstance().getNodeType("jnt:page", new AsyncCallback<GWTJahiaNodeType>() {
                public void onFailure(Throwable throwable) {
                    Log.error("", throwable);
                    com.google.gwt.user.client.Window.alert("-create page->" + throwable.getMessage());
                }

                public void onSuccess(GWTJahiaNodeType gwtJahiaNodeType) {
                    new ContentTypeWindow(linker, linker.getMainNode(), gwtJahiaNodeType,true).show();
                }
            });
        }

    }


    /**
     * Dispay edit content window
     *
     * @param linker
     */
    public static void edit(Linker linker) {
        if (linker.getMainNode() != null) {
            new EditContentEngine(linker.getSelectedNode(),linker).show();
        }
    }


    /**
     * Publish selected content
     *
     * @param linker
     */
    public static void publish(final Linker linker) {
        GWTJahiaNode selectedNode = linker.getSelectedNode();
        if (selectedNode == null) {
            selectedNode = linker.getMainNode();
        }
        if (selectedNode != null) {
            final GWTJahiaNode s = selectedNode;
            JahiaContentManagementService.App.getInstance().getPublicationInfo(s.getPath(), true,
                    new AsyncCallback<GWTJahiaPublicationInfo>() {
                        public void onSuccess(GWTJahiaPublicationInfo result) {
                            new PublicationStatusWindow(linker, s, result).show();
                        }

                        public void onFailure(Throwable caught) {
                            com.google.gwt.user.client.Window.alert("Cannot get status: " + caught.getMessage());
                        }
                    }
            );

        }
    }

    /**
     * Publish all content under selected node
     *
     * @param linker
     */
    public static void publishAll(final Linker linker) {
        GWTJahiaNode selectedNode = linker.getSelectedNode();
        if (selectedNode == null) {
            selectedNode = linker.getMainNode();
        }
        if (selectedNode != null) {
            new PublishAllConfirmWindow(linker, selectedNode).show();
        }
    }

    /**
     * Unpublish selected content
     *
     * @param linker
     */
    public static void unpublish(final Linker linker) {
        GWTJahiaNode selectedNode = linker.getSelectedNode();
        if (selectedNode == null) {
            selectedNode = linker.getMainNode();
        }
        if (selectedNode != null) {
            JahiaContentManagementService.App.getInstance().unpublish(selectedNode.getPath(), new AsyncCallback() {
                public void onFailure(Throwable caught) {
                    Log.error("Cannot publish", caught);
                    com.google.gwt.user.client.Window.alert("Cannot unpublish " + caught.getMessage());
                }

                public void onSuccess(Object result) {
                    Info.display(Messages.getResource("publication_unpublished_title"), Messages.getResource("publication_unpublished_text"));
                    linker.refresh();
                }
            });
        }
    }


    /**
     * Switch lock
     *
     * @param linker
     */
    public static void switchLock(final Linker linker) {
        if (linker.getSelectedNode() != null) {
            List<String> paths = new ArrayList<String>(1);
            paths.add(linker.getSelectedNode().getPath());
            final boolean isLock = !linker.getSelectedNode().isLocked();
            JahiaContentManagementService.App.getInstance().setLock(paths, isLock, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Log.error("", throwable);
                    com.google.gwt.user.client.Window.alert("-switch lock->" + throwable.getMessage());
                }

                public void onSuccess(Object o) {
                    linker.refreshMainComponent();
                }
            });
        }

    }


    /**
     * Delete content
     *
     * @param linker
     */
    public static void delete(final Linker linker) {
        if (linker.getSelectedNodes() != null && !linker.getSelectedNodes().isEmpty()) {
            MessageBox.confirm("", Messages.get("fm_confRemove", "Do you really want to continue?"), new Listener<MessageBoxEvent>() {
                public void handleEvent(MessageBoxEvent be) {
                    if (be.getButtonClicked().getText().equalsIgnoreCase(Dialog.YES)) {
                        List<String> paths = new ArrayList<String>();
                        for (GWTJahiaNode node : linker.getSelectedNodes()) {
                            paths.add(node.getPath());
                        }
                        JahiaContentManagementService.App.getInstance().deletePaths(paths, new AsyncCallback() {
                            public void onFailure(Throwable throwable) {
                                Log.error("", throwable);
                                com.google.gwt.user.client.Window.alert("-delete->" + throwable.getMessage());
                            }

                            public void onSuccess(Object o) {
                                linker.refresh();
                                linker.select(null);
                            }
                        });
                    }
                }
            });
        }
    }



    private static class PublishAllConfirmWindow extends Window {

        private PublishAllConfirmWindow(final Linker linker, final GWTJahiaNode selectedNode) {
            setScrollMode(Style.Scroll.AUTO);
            setHeading("Publish");
            setSize(800, 500);
            setResizable(false);

            setModal(true);

            final FormPanel form = new FormPanel() ;
            form.setFrame(false);
            form.setHeaderVisible(false);
            form.setBodyBorder(false);
            form.setBorders(false);

            final TextArea comments = new TextArea();
            comments.setName("comments");
            comments.setFieldLabel(Messages.getResource("publication_publicationComments"));
            form.add(comments);

            final Button cancel = new Button(Messages.getResource("fm_cancel"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    hide() ;
                }
            });
            final Button ok = new Button(Messages.getResource("publication_publish")) ;
            SelectionListener<ButtonEvent> selectionListener = new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    ok.setEnabled(false);
                    cancel.setEnabled(false);
                    List<String> selectedPaths = new ArrayList<String>();
                    JahiaContentManagementService.App.getInstance().publish(selectedNode.getPath(), null, true, comments.getValue(), new AsyncCallback() {
                        public void onFailure(Throwable caught) {
                            Log.error("Cannot publish", caught);
                            com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                            hide();
                        }

                        public void onSuccess(Object result) {
                            Info.display(Messages.getResource("publication_published_title"), Messages.getResource("publication_published_text"));
                            linker.refresh();
                            hide();
                        }
                    });

                }
            };
            ok.addSelectionListener(selectionListener);
            setButtonAlign(Style.HorizontalAlignment.CENTER);
            addButton(ok);
            addButton(cancel);
            add(form);
        }
    }

    private static class PublicationStatusWindow extends Window {

        private PublicationStatusWindow(final Linker linker, final GWTJahiaNode selectedNode, GWTJahiaPublicationInfo info) {
            setScrollMode(Style.Scroll.AUTO);
            setHeading("Publish");
            setSize(800, 500);
            setResizable(false);

            setModal(true);

            final FormPanel form = new FormPanel() ;
            form.setFrame(false);
            form.setHeaderVisible(false);
            form.setBodyBorder(false);
            form.setBorders(false);

            final TextArea comments = new TextArea();
            comments.setName("comments");
            comments.setFieldLabel(Messages.getResource("publication_publicationComments"));
            form.add(comments);


            List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
            
//            CheckBoxSelectionModel<GWTJahiaPublicationInfo> sm = new CheckBoxSelectionModel<GWTJahiaPublicationInfo>();
//            sm.setSelectionMode(SelectionMode.MULTI);
//            configs.add(sm.getColumn());
            
            ColumnConfig column = new ColumnConfig();  
            column.setId("path");
            column.setHeader(Messages.getResource("publication_path"));
            column.setWidth(400);
            column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
                @Override
                public Object render(GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                    return model.get(property);
                }
            });
            configs.add(column);

            column = new ColumnConfig();
            column.setId("status");
            column.setHeader(Messages.getResource("publication_currentStatus"));
            column.setWidth(150);
            column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
                @Override
                public Object render(GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                    switch (model.getStatus()) {
                        case GWTJahiaPublicationInfo.NOT_PUBLISHED:
                            return Messages.getResource("publication_status_notyetpublished");
                        case GWTJahiaPublicationInfo.PUBLISHED:
                            return Messages.getResource("publication_status_published");
                        case GWTJahiaPublicationInfo.MODIFIED:
                            return Messages.getResource("publication_status_modified");
                        case GWTJahiaPublicationInfo.UNPUBLISHABLE:
                            return Messages.getResource("publication_status_notyetpublished");
                    }
                    return "";
                }
            });
            configs.add(column);

            column = new ColumnConfig();
            column.setId("canPublish");
            column.setHeader(Messages.getResource("publication_publicationAllowed"));
            column.setWidth(150);
            column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
                @Override
                public Object render(GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                    return model.isCanPublish().toString();
                }
            });
            configs.add(column);

            ListStore<GWTJahiaPublicationInfo> store = new ListStore<GWTJahiaPublicationInfo>();
            store.add(info);
            for (ModelData data : info.getChildren()) {
                if (!((GWTJahiaPublicationInfo) data).isCanPublish() || ((GWTJahiaPublicationInfo) data).getStatus() != GWTJahiaPublicationInfo.PUBLISHED) {
                    store.add((GWTJahiaPublicationInfo) data);
                }
            }
//            store.add((List<GWTJahiaPublicationInfo>) info.getChildren(), true);
            ColumnModel cm = new ColumnModel(configs);

            final Grid<GWTJahiaPublicationInfo> g = new Grid<GWTJahiaPublicationInfo>(store, cm) {

				@Override
                protected void afterRenderView() {
	                super.afterRenderView();
                    getSelectionModel().selectAll();
                }
            	
            };
            g.setStripeRows(true);
//            g.setSelectionModel(sm);
//            g.addPlugin(sm);
            add(g);

            final Button cancel = new Button(Messages.getResource("fm_cancel"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    hide() ;
                }
            });
            final Button ok = new Button(Messages.getResource("publication_publish")) ;
            SelectionListener<ButtonEvent> selectionListener = new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    ok.setEnabled(false);
                    cancel.setEnabled(false);
                    JahiaContentManagementService.App.getInstance().publish(selectedNode.getPath(), null, false, comments.getValue(), new AsyncCallback() {
                        public void onFailure(Throwable caught) {
                            Log.error("Cannot publish", caught);
                            com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                            hide();
                        }

                        public void onSuccess(Object result) {
                            Info.display(Messages.getResource("publication_published_title"), Messages.getResource("publication_published_text"));
                            linker.refresh();
                            hide();
                        }
                    });

                }
            };
            ok.addSelectionListener(selectionListener);
            setButtonAlign(Style.HorizontalAlignment.CENTER);
            addButton(ok);
            addButton(cancel);
            add(form);
        }
    }

}
