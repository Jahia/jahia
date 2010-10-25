/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
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
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;

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

    public LinkerModule(String id, String path, Element divElement, MainModule m) {
        super(id, path, divElement, m);
        property = DOM.getElementAttribute(divElement, "property");
        mixinType = DOM.getElementAttribute(divElement, "mixinType");
        setBorders(false);
        panel = new HorizontalPanel();
        panel.setHorizontalAlign(Style.HorizontalAlignment.CENTER);
        panel.addStyleName("x-small-editor");
        panel.addStyleName("x-panel-header");
        panel.addStyleName("x-panel-linker");
//        html = new HTML("<img src=\""+JahiaGWTParameters.getContextPath() + "/modules/default/images/add.png"+"\" /> Add new content here");
        html = new HTML("<p class=\"linkAction\">Click this to link<br/></p>");
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
                                            EngineLoader.showEditEngine(mainModule.getEditLinker(), node);
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
        async.getProperties(node.getPath(), null, new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
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