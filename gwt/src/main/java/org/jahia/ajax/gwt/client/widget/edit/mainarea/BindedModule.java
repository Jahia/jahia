package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.ModuleSelectionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BindedModule extends SimpleModule {
    private String property = "j:bindedComponent";
    private String headerText ;
    private Boolean linked = null;

    public BindedModule(String id, String path, Element divElement, final MainModule mainModule) {
        super(id, path, divElement, mainModule, false);

        final HorizontalPanel leftWidgetPanel = new HorizontalPanel();

        head = new Header() {
            @Override
            protected void onRender(Element target, int index) {
                super.onRender(target, index);
                adopt(leftWidgetPanel);
                leftWidgetPanel.addStyleName("x-panel-toolbar");
                leftWidgetPanel.setLayoutOnChange(true);
                leftWidgetPanel.setStyleAttribute("float", "left");
                leftWidgetPanel.getAriaSupport().setPresentation(true);

                leftWidgetPanel.render(getElement());
                adopt(leftWidgetPanel);
            }

            @Override
            protected void doAttachChildren() {
                super.doAttachChildren();
                ComponentHelper.doAttach(leftWidgetPanel);
            }

            @Override
            protected void doDetachChildren() {
                super.doDetachChildren();
                ComponentHelper.doDetach(leftWidgetPanel);
            }

        };

        remove(html);
        add(head);
        headerText = Messages.get("label.content") + " : " + path.substring(path.lastIndexOf('/') + 1);
        setHeaderText(headerText);
        head.addStyleName("x-panel-header");
        head.addStyleName("x-panel-header-simplemodule");
        setBorders(false);

        add(html);


        ToolButton tool = new ToolButton("x-tool-pin", new SelectionListener<IconButtonEvent>() {
            public void componentSelected(IconButtonEvent event) {
                if (linked != null) {
                    if (linked) {
                        List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();
//                            final List<GWTJahiaNode> srcNodes = e.getStatus().getData(SOURCE_NODES);
                        final GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty(property,
                                new GWTJahiaNodePropertyValue((String) null,
                                        GWTJahiaNodePropertyType.WEAKREFERENCE));
                        properties.add(gwtJahiaNodeProperty);
                        JahiaContentManagementService.App.getInstance()
                                .saveProperties(Arrays.asList(node), properties, new BaseAsyncCallback() {
                                    public void onSuccess(Object o) {
                                        getMainModule().getEditLinker().refresh(EditLinker.REFRESH_MAIN);
                                    }

                                    public void onApplicationFailure(Throwable throwable) {
                                        Window.alert("Failed : " + throwable);
                                    }
                                });

                    } else {
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
                                if (selection.getNode() != node) {
                                    List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();
                                    final GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty(property,
                                            new GWTJahiaNodePropertyValue(selection.getNode(),
                                                    GWTJahiaNodePropertyType.WEAKREFERENCE));
                                    properties.add(gwtJahiaNodeProperty);
                                    JahiaContentManagementService.App.getInstance()
                                            .saveProperties(Arrays.asList(node), properties, new BaseAsyncCallback() {
                                                public void onSuccess(Object o) {
                                                    getMainModule().getEditLinker().refresh(EditLinker.REFRESH_MAIN);
                                                }

                                                public void onApplicationFailure(Throwable throwable) {
                                                    Window.alert("Failed : " + throwable);
                                                }
                                            });
                                }
                            }
                        });
                    }
                }
            }
        });

        leftWidgetPanel.add(tool);
    }

    @Override
    public void setNode(final GWTJahiaNode node) {
        super.setNode(node);
        final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
        async.getProperties(node.getPath(), null, new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
            public void onSuccess(GWTJahiaGetPropertiesResult gwtJahiaGetPropertiesResult) {
                if (gwtJahiaGetPropertiesResult.getProperties().containsKey(property)) {
                    final GWTJahiaNodeProperty o = gwtJahiaGetPropertiesResult.getProperties().get(property);
                    if (o.getValues().get(0).getNode().getPath().equals(mainModule.getPath())) {
                        setHeaderText(headerText + " - Linked to: main resource");
                        linked = Boolean.FALSE;
                    } else {
                        setHeaderText(headerText + " - Linked to: " + o.getValues().get(0).getNode().getName());
                        linked = Boolean.TRUE;
                    }
                } else {
                    linked = Boolean.FALSE;
                    setHeaderText(head.getText() + " - Linked to: main resource");
                }
            }
        });
    }

}
