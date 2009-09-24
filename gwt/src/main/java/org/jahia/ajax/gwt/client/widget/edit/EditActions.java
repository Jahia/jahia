package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
                    com.google.gwt.user.client.Window.alert("-->" + throwable.getMessage());
                }

                public void onSuccess(GWTJahiaNodeType gwtJahiaNodeType) {
                    new EditContentEngine(linker, linker.getMainNode(), gwtJahiaNodeType, null, false, true).show();
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
            new EditContentEngine(linker.getMainNode(),linker).show();
        }
    }


    /**
     * Publish selected content
     *
     * @param linker
     */
    public static void publish(final Linker linker) {
        if (linker.getSelectedNode() != null) {
            JahiaContentManagementService.App.getInstance().getPublicationInfo(linker.getSelectedNode().getPath(), true,
                    new AsyncCallback<GWTJahiaPublicationInfo>() {
                        public void onSuccess(GWTJahiaPublicationInfo result) {
                            new PublicationStatusWindow(linker, result).show();
                        }

                        public void onFailure(Throwable caught) {
                            com.google.gwt.user.client.Window.alert("Cannot get status: " + caught.getMessage());
                        }
                    }
            );

        }
    }

    /**
     * Unpublish selected content
     *
     * @param linker
     */
    public static void unpublish(final Linker linker) {
        if (linker.getSelectedNode() != null) {
            JahiaContentManagementService.App.getInstance().unpublish(linker.getSelectedNode().getPath(), new AsyncCallback() {
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
                    com.google.gwt.user.client.Window.alert("-->" + throwable.getMessage());
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
        if (linker.getSelectedNode() != null) {
            List<String> paths = new ArrayList<String>(1);
            paths.add(linker.getSelectedNode().getPath());
            JahiaContentManagementService.App.getInstance().deletePaths(paths, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Log.error("", throwable);
                    com.google.gwt.user.client.Window.alert("-->" + throwable.getMessage());
                }

                public void onSuccess(Object o) {
                    linker.refresh();
                    linker.select(null);
                }
            });
        }
    }


    private static Map<LayoutContainer, Module> containers = new HashMap<LayoutContainer, Module>();

    public static void viewPublishedStatus(final Linker linker) {
        if (!containers.isEmpty()) {
            for (LayoutContainer ctn : containers.keySet()) {
                RootPanel.get().remove(ctn);
            }
            containers.clear();
            return;
        }
        final Map<String, List<Module>> modulesByPath = ModuleHelper.getModulesByPath();
        ArrayList<String> list = new ArrayList<String>();
        for (String s : modulesByPath.keySet()) {
            if (!s.endsWith("*") && !(modulesByPath.get(s) instanceof TextModule)) {
                list.add(s);
            }
        }

        Listener<ComponentEvent> removeListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                for (LayoutContainer ctn : containers.keySet()) {
                    RootPanel.get().remove(ctn);
                }
                containers.clear();
            }
        };

        for (String path : list) {
            for (Module module : modulesByPath.get(path)) {
                GWTJahiaPublicationInfo info = module.getNode().getPublicationInfo();
                if (info.getStatus() == GWTJahiaPublicationInfo.MODIFIED || info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED) {

                    El el;
                    LayoutContainer container = module.getContainer();

                    if (container instanceof ContentPanel) {
                        el = ((ContentPanel) container).getHeader().el();
                    } else {
                        el = container.el();
                    }

                    LayoutContainer ctn = new LayoutContainer();
                    ctn.setBorders(true);

//                            if (info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED) {
                    ctn.setStyleAttribute("background-color", "red");
//                            } else {
//                                ctn.setStyleAttribute("background-color", "orange");
//                            }
                    ctn.setStyleAttribute("opacity", "0.2");
                    RootPanel.get().add(ctn);
                    ctn.el().makePositionable(true);
                    ctn.setPosition(container.getAbsoluteLeft(), container.getAbsoluteTop());
                    ctn.setSize(el.getWidth(), el.getHeight());
                    ctn.show();
                    containers.put(ctn, module);
                    ctn.sinkEvents(Event.ONCLICK);
                    ctn.addListener(Events.OnClick, removeListener);
                }
            }
            ((EditLinker)linker).getMainModule().addScrollListener(new ScrollListener() {
                @Override
                public void widgetScrolled(ComponentEvent ce) {
                    for (LayoutContainer container : containers.keySet()) {
                        LayoutContainer parentCtn = containers.get(container).getContainer();
                        container.setPosition(parentCtn.getAbsoluteLeft(), parentCtn.getAbsoluteTop());
                    }
                    super.widgetScrolled(ce);
                }
            });
        }

    }

    private static class PublicationStatusWindow extends Window {

        private PublicationStatusWindow(final Linker linker, GWTJahiaPublicationInfo info) {
            setScrollMode(Style.Scroll.AUTO);
            setHeading("Publish");
            setSize(800, 500);
            setResizable(false);
            ButtonBar buttons = new ButtonBar() ;

            setModal(true);

            final FormPanel form = new FormPanel() ;
            form.setFrame(false);
            form.setHeaderVisible(false);
            form.setBodyBorder(false);
            form.setBorders(false);

            final TextArea area = new TextArea();
            area.setName("comments");
            area.setFieldLabel(Messages.getResource("publication_publicationComments"));
            form.add(area);


            List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
            
            CheckBoxSelectionModel<GWTJahiaPublicationInfo> sm = new CheckBoxSelectionModel<GWTJahiaPublicationInfo>();
            sm.setSelectionMode(SelectionMode.MULTI);
            configs.add(sm.getColumn()); 
            
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
                        case GWTJahiaPublicationInfo.UNPUBLISHED:
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
                store.add((GWTJahiaPublicationInfo) data);
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
            g.setSelectionModel(sm);
            g.addPlugin(sm);
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
                    List<String> selectedPaths = new ArrayList<String>();
                    for (GWTJahiaPublicationInfo info : g.getSelectionModel().getSelectedItems()) {
	                    selectedPaths.add(info.getPath());
                    }
                    JahiaContentManagementService.App.getInstance().publish(selectedPaths, new AsyncCallback() {
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
            buttons.add(ok) ;
            buttons.add(cancel) ;
            setButtonAlign(Style.HorizontalAlignment.CENTER);
            setBottomComponent(buttons);
            add(form);
        }
    }

}
