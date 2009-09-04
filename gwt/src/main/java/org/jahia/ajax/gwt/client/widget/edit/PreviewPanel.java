package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.GWTJahiaBasicDataBean;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 4, 2009
 * Time: 5:42:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class PreviewPanel extends ContentPanel {
    private EditLinker editLinker;


    private GWTJahiaNode node;
    private String template;
    private HTML html;

    private PreviewDragSource dragSource;

    private LayoutContainer layout;

    private ComboBox<GWTJahiaBasicDataBean> templateBox;
    private EditActionItem save;

    public PreviewPanel() {
        ToolBar toolbar = new ToolBar();
        ListStore<GWTJahiaBasicDataBean> templateListStore = new ListStore<GWTJahiaBasicDataBean>();
        templateBox = new ComboBox<GWTJahiaBasicDataBean>();
        templateBox.setStore(templateListStore);
        templateBox.setDisplayField(GWTJahiaBasicDataBean.DISPLAY_NAME);
        templateBox.clearSelections();
        templateBox.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaBasicDataBean>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaBasicDataBean> gwtJahiaNodeSelectionChangedEvent) {
                GWTJahiaBasicDataBean selectedItem = gwtJahiaNodeSelectionChangedEvent.getSelectedItem();
                updatePreview(selectedItem.getValue());
            }
        });

        toolbar.add(templateBox);

        save = new EditActionItem("save_template", "fm-save") {
            public void onSelection() {
                    JahiaContentManagementService.App.getInstance().saveNodeTemplate(node.getPath() , template,new AsyncCallback() {
                        public void onFailure(Throwable throwable) {
                            Log.error("", throwable);
                            com.google.gwt.user.client.Window.alert("-->" + throwable.getMessage());
                        }
                        public void onSuccess(Object o) {
                            editLinker.getMainModule().refresh();
                        }
                    });
            }

            public void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) {

            }
        };

        save.setEnabled(false);
        toolbar.add(save.getTextToolitem());

        setHeaderVisible(false);
        setTopComponent(toolbar);

        layout = new LayoutContainer();
        layout.sinkEvents(Event.ONDBLCLICK + Event.ONCLICK);
        layout.addListener(Events.OnDoubleClick, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if (html != null && node != null) {
                    Window w = new Window();
                    final String text = "Preview of " + node.getPath();
                    w.setHeading(text);
                    w.setScrollMode(Style.Scroll.AUTO);
                    w.setModal(true);
                    w.setClosable(true);
                    w.setSize(800, 600);
                    w.setBlinkModal(true);
                    w.setPlain(true);
                    w.setToolTip(text);
                    w.add(new HTML(html.getHTML()));
                    w.show();
                }
            }
        });

        add(layout);
        dragSource = new PreviewDragSource();
    }

    public void initWithLinker(EditLinker editLinker) {
        this.editLinker = editLinker;
        dragSource.addDNDListener(editLinker.getDndListener());
    }

    public HTML getHTML() {
        return html;
    }

    public void setHTML(HTML html) {
        this.html = html;
        layout.removeAll();
        if (html != null) {
            layout.add(html);
        }
    }

    public LayoutContainer getLayoutContainer() {
        return layout;
    }

    public void handleNewModuleSelection(Module selectedModule) {
        if (selectedModule != null) {
            this.node = selectedModule.getNode();
            this.template = selectedModule.getTemplate();
            save.setEnabled(true);
        } else {
            this.node = null;
            this.template = null;
            save.setEnabled(false);
        }
        updateTemplateBox(node);
        updatePreview(template);
    }

    public void handleNewSidePanelSelection(GWTJahiaNode node) {
        this.node = node;
        this.template = null;
        save.setEnabled(false);
        updateTemplateBox(node);
        updatePreview(template);
    }

    /**
     * Display the rendered html of the given node in the preview panel
     *
     * @param template the template to render
     */
    private void updatePreview(String template) {
        if (node != null) {
            JahiaContentManagementService.App.getInstance().getRenderedContent(node.getPath(), null, editLinker.getLocale(), template, false, new AsyncCallback<String>() {
                public void onSuccess(String result) {
                    HTML html = new HTML(result);
                    setHTML(html);
                    PreviewPanel.this.node = node;
                    layout();
                }

                public void onFailure(Throwable caught) {
                    Log.error("", caught);
                    com.google.gwt.user.client.Window.alert("-->" + caught.getMessage());
                }
            });
        } else {
            setHTML(null);
        }
        this.template = template;
    }


    /**
     * This will update the template conbo box based on the page selected item
     *
     * @param node the selected node (item on the page)
     */
    public void updateTemplateBox(GWTJahiaNode node) {
        templateBox.getStore().removeAll();
        templateBox.clearSelections();
        if (node != null) {
            JahiaContentManagementService.App.getInstance().getTemplatesPath(node.getPath(),new AsyncCallback<List<String[]>>() {
                public void onFailure(Throwable throwable) {
                    Log.error("", throwable);
                    com.google.gwt.user.client.Window.alert("-->" + throwable.getMessage());
                }
                public void onSuccess(List<String[]> strings) {
                    for(String[] template:strings) {
                        templateBox.getStore().add(new GWTJahiaBasicDataBean(template[0], template[1]));
                    }
                    templateBox.setValue(new GWTJahiaBasicDataBean(template,template));
                }
            });
        }
    }

    public class PreviewDragSource extends EditModeDragSource {
        public PreviewDragSource() {
            super(layout);
        }

        @Override
        protected void onDragStart(DNDEvent e) {
            e.setCancelled(false);
            List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(1);
            nodes.add(node);
            e.setData(nodes);
            e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.CONTENT_SOURCE_TYPE);

            List<GWTJahiaNode> list = (List<GWTJahiaNode>) e.getData();
            e.getStatus().setData("size", list.size());

            e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, list);
//            e.getStatus().setData(EditModeDNDListener.SOURCE_TEMPLATE, editManager.getEditLinker().getCurrentrySelectedTemplate());
            if (getStatusText() == null) {
                e.getStatus().update(DOM.clone(layout.getElement(), true));
            }
            super.onDragStart(e);
        }
    }


}
