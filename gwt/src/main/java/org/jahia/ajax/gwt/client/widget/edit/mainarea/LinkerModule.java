package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.edit.ModuleSelectionListener;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EditContentEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:03:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinkerModule extends Module {
    private HorizontalPanel panel;
    private String property;
    private String mixinType;

    public LinkerModule(String id, String path, String property, String mixinType, MainModule m) {
        super(id, path, null, null, null, null, m);
        this.id = id;
        this.path = path;
        this.property = property;
        this.mixinType = mixinType;
        this.mainModule = m;
        setBorders(false);
        panel = new HorizontalPanel();
        panel.setHorizontalAlign(Style.HorizontalAlignment.CENTER);
        panel.addStyleName("x-small-editor");
        panel.addStyleName("x-panel-header");
        panel.addStyleName("x-panel-linker");
//        html = new HTML("<img src=\""+JahiaGWTParameters.getContextPath() + "/modules/default/images/add.png"+"\" /> Add new content here");
        html = new HTML("<p class=\"linkAction\">Drop this to be linked<br/></p>");
        panel.add(html);
        add(panel);
    }

    @Override
    public void onParsed() {
        sinkEvents(Event.ONCLICK + Event.ONMOUSEOVER + Event.ONMOUSEOUT + Event.ONCONTEXTMENU);

        Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if (selectable) {
                    String s = JahiaGWTParameters.getContextPath();
                    if (s.equals("/")) {
                        s = "";
                    }
                    mainModule.setStyleAttribute("cursor",
                            "url('" + s + "/gwt/resources/images/xtheme-jahia-andromeda/panel/link.cur'), pointer");
                    mainModule.getEditLinker().setSelectionListener(new ModuleSelectionListener() {
                        public void onModuleSelection(Module selection) {
                            mainModule.setStyleAttribute("cursor", "");
                            mainModule.getEditLinker().setSelectionListener(null);

                            if (mixinType != null && !mixinType.equals("")) {
                                selection.getNode().getNodeTypes().add(mixinType);
                                JahiaContentManagementService.App.getInstance()
                                        .saveProperties(Arrays.asList(selection.getNode()),
                                                new ArrayList<GWTJahiaNodeProperty>(), new BaseAsyncCallback() {
                                                    public void onSuccess(Object o) {
                                                    }
                                                });
                            }
                            List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();
//                            final List<GWTJahiaNode> srcNodes = e.getStatus().getData(SOURCE_NODES);
                            final GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty(property,
                                    new GWTJahiaNodePropertyValue(selection.getNode(),
                                            GWTJahiaNodePropertyType.WEAKREFERENCE));
                            properties.add(gwtJahiaNodeProperty);
                            JahiaContentManagementService.App.getInstance()
                                    .saveProperties(Arrays.asList(node), properties, new BaseAsyncCallback() {
                                        public void onSuccess(Object o) {
                                            new EditContentEngine(node, mainModule.getEditLinker()).show();
                                        }

                                        public void onApplicationFailure(Throwable throwable) {
                                            Window.alert("Failed : " + throwable);
                                        }
                                    });

                        }
                    });
                }
            }
        };
        addListener(Events.OnClick, listener);
        addListener(Events.OnContextMenu, listener);
//        addListener(Events.OnDoubleClick, new EditContentEnginePopupListener(this, mainModule.getEditLinker()));

        Listener<ComponentEvent> hoverListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                Hover.getInstance().addHover(LinkerModule.this);
            }
        };
        Listener<ComponentEvent> outListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                Hover.getInstance().removeHover(LinkerModule.this);
            }
        };

        addListener(Events.OnMouseOver, hoverListener);
        addListener(Events.OnMouseOut, outListener);
    }

    @Override
    public void setNode(GWTJahiaNode node) {
        super.setNode(node);
        final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
        async.getProperties(node.getPath(), new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
            public void onSuccess(GWTJahiaGetPropertiesResult gwtJahiaGetPropertiesResult) {
                if (gwtJahiaGetPropertiesResult.getProperties().containsKey(property)) {
                    final GWTJahiaNodeProperty o = gwtJahiaGetPropertiesResult.getProperties().get(property);
                    panel.removeAll();
                    html = new HTML("<p class=\"linkPath\">Linked to: " + o.getValues().get(0).getNode().getName() +
                            "</p><p class=\"linkAction\">Click this to link</p>");
                    panel.add(html);
                    panel.layout();
                }
            }
        });
    }
}