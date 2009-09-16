package org.jahia.ajax.gwt.client.widget.edit;

import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Label;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.util.SwallowEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 4, 2009
 * Time: 4:14:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditActions {

    /**
     * Create page
     *
     * @param editLinker
     */
    public static void createPage(final EditLinker editLinker) {
        if (editLinker.getMainModule().getNode() != null) {
            JahiaContentDefinitionService.App.getInstance().getNodeType("jnt:page", new AsyncCallback<GWTJahiaNodeType>() {
                public void onFailure(Throwable throwable) {
                    Log.error("", throwable);
                    com.google.gwt.user.client.Window.alert("-->" + throwable.getMessage());
                }

                public void onSuccess(GWTJahiaNodeType gwtJahiaNodeType) {
                    new EditContentEngine(editLinker, editLinker.getMainModule().getNode(), gwtJahiaNodeType, null, false, true).show();
                }
            });
        }

    }


    /**
     * Dispay edit content window
     *
     * @param editLinker
     */
    public static void edit(EditLinker editLinker) {
        if (editLinker.getMainModule() != null) {
            new EditContentEngine(editLinker.getMainModule().getNode()).show();
        }
    }


    /**
     * Publish selected content
     *
     * @param editLinker
     */
    public static void publish(final EditLinker editLinker) {
        if (editLinker.getSelectedModule().getNode() != null) {
            JahiaContentManagementService.App.getInstance().getPublicationInfo(editLinker.getSelectedModule().getNode().getPath(), true,
                    new AsyncCallback<GWTJahiaPublicationInfo>() {
                        public void onSuccess(GWTJahiaPublicationInfo result) {
                            new PublicationStatusWindow(editLinker, result).show();
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
     * @param editLinker
     */
    public static void unpublish(final EditLinker editLinker) {
        if (editLinker.getSelectedModule().getNode() != null) {
            JahiaContentManagementService.App.getInstance().unpublish(editLinker.getSelectedModule().getNode().getPath(), new AsyncCallback() {
                public void onFailure(Throwable caught) {
                    Log.error("Cannot publish", caught);
                    com.google.gwt.user.client.Window.alert("Cannot unpublish " + caught.getMessage());
                }

                public void onSuccess(Object result) {
                    Info.display("Content unpublished", "Content unpublished.");
                    editLinker.refresh();
                }
            });
        }
    }


    /**
     * Switch lock
     *
     * @param editLinker
     */
    public static void switchLock(final EditLinker editLinker) {
        if (editLinker.getSelectedModule() != null) {
            List<String> paths = new ArrayList<String>(1);
            paths.add(editLinker.getSelectedModule().getNode().getPath());
            final boolean isLock = !editLinker.getSelectedModule().getNode().isLocked();
            JahiaContentManagementService.App.getInstance().setLock(paths, isLock, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Log.error("", throwable);
                    com.google.gwt.user.client.Window.alert("-->" + throwable.getMessage());
                }

                public void onSuccess(Object o) {
                    editLinker.getMainModule().refresh();
                }
            });
        }

    }


    /**
     * Delete content
     *
     * @param editLinker
     */
    public static void delete(final EditLinker editLinker) {
        if (editLinker.getSelectedModule() != null) {
            List<String> paths = new ArrayList<String>(1);
            paths.add(editLinker.getSelectedModule().getNode().getPath());
            JahiaContentManagementService.App.getInstance().deletePaths(paths, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Log.error("", throwable);
                    com.google.gwt.user.client.Window.alert("-->" + throwable.getMessage());
                }

                public void onSuccess(Object o) {
                    editLinker.refresh();
                    editLinker.onModuleSelection(null);
                }
            });
        }
    }


    private static Map<LayoutContainer, Module> containers = new HashMap<LayoutContainer, Module>();

    public static void viewPublishedStatus(final EditLinker editLinker) {
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
                if (module instanceof MainModule) {
                    continue;
                }
                GWTJahiaPublicationInfo info = module.getNode().getPublicationInfo();
                if (info.getStatus() == GWTJahiaPublicationInfo.MODIFIED || info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED) {

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
                    ctn.setPosition(module.getContainer().getAbsoluteLeft(), module.getContainer().getAbsoluteTop());
                    ctn.setSize(module.getContainer().getWidth(), module.getContainer().getHeight());
                    ctn.show();
                    containers.put(ctn, module);
                    ctn.sinkEvents(Event.ONCLICK);
                    ctn.addListener(Events.OnClick, removeListener);
                }
            }
            editLinker.getMainModule().addScrollListener(new ScrollListener() {
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
        private EditLinker editLinker;

        private PublicationStatusWindow(final EditLinker editLinker, GWTJahiaPublicationInfo info) {
            this.editLinker = editLinker;
            setScrollMode(Style.Scroll.AUTO);
            setHeading("Publish");
            setSize(500, 150);
            setResizable(false);
            ButtonBar buttons = new ButtonBar() ;

            setModal(true);

            final FormPanel form = new FormPanel() ;
            form.setFrame(false);
            form.setHeaderVisible(false);
            form.setBorders(false);

            final TextArea area = new TextArea();
            area.setName("comments");
            area.setFieldLabel("Comments");
            form.add(area);

            for (String path : info.getReferences().keySet()) {
                add(new Label(path+ " / "+ info.getReferences().get(path).getStatus()));
            }
            

            Button cancel = new Button(Messages.getResource("fm_cancel"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    hide() ;
                }
            });
            Button ok = new Button("Publish", new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    JahiaContentManagementService.App.getInstance().publish(editLinker.getSelectedModule().getNode().getPath(), new AsyncCallback() {
                        public void onFailure(Throwable caught) {
                            Log.error("Cannot publish", caught);
                            com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                            hide();
                        }

                        public void onSuccess(Object result) {
                            Info.display("Content published", "Content published.");
                            editLinker.refresh();
                            hide();
                        }
                    });

                }
            }) ;
            buttons.add(ok) ;
            buttons.add(cancel) ;
            setButtonAlign(Style.HorizontalAlignment.CENTER);
            setBottomComponent(buttons);
            add(form);
        }
    }

}
