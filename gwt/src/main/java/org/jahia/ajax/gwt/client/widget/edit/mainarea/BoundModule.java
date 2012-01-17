/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

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

public class BoundModule extends SimpleModule {
    private String property = "j:bindedComponent";
    private String headerText ;
    private Boolean linked = null;

    public BoundModule(String id, String path, Element divElement, final MainModule mainModule) {
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
                                .saveProperties(Arrays.asList(node), properties, null, new BaseAsyncCallback() {
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
                                            .saveProperties(Arrays.asList(node), properties, null, new BaseAsyncCallback() {
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
