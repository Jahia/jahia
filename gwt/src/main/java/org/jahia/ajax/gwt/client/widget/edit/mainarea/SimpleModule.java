/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
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
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.contentengine.EditContentEnginePopupListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.edit.ModuleSelectionListener;

import java.util.*;

/**
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:25:19 PM
 */
public class SimpleModule extends Module {
    protected boolean hasDragDrop = true;
    protected boolean canHover = true;
    protected boolean editable = true;
    protected boolean addIconInHeader = true;

    protected boolean bindable = false;
    protected Boolean bound = null;
    private String boundProperty = "j:bindedComponent";

    public SimpleModule(String id, String path, Element divElement, MainModule mainModule) {
        super(id, path, divElement, mainModule);
        editable = !"false".equals(DOM.getElementAttribute(divElement, "editable"));
    }

    public SimpleModule(String id, final String path, Element divElement, final MainModule mainModule, boolean header) {
        super(id, path, divElement, mainModule);

        hasDragDrop = !"false".equals(DOM.getElementAttribute(divElement, "dragdrop"));
        editable = !"false".equals(DOM.getElementAttribute(divElement, "editable"));
        bindable = "true".equals(DOM.getElementAttribute(divElement, "bindable"));

        if ((header || bindable) && editable) {
            head = new Header();
            add(head);
            setHeaderText(path.substring(path.lastIndexOf('/') + 1));
            head.addStyleName("x-panel-header");
            head.addStyleName("x-panel-header-simplemodule");
            setBorders(false);
        }

        html = new HTML(divElement.getInnerHTML());
        add(html);


        if (bindable) {
            head.addTool(new ToolButton("x-tool-pin", new BindSelectionListener(mainModule)));
        }
    }

    public void onParsed() {
        Log.debug("Add drag source for simple module " + path);

        if (mainModule.getConfig().isEnableDragAndDrop()) {
            if (hasDragDrop) {
                DragSource source = new SimpleModuleDragSource(this);
                source.addDNDListener(mainModule.getEditLinker().getDndListener());
                DropTarget target = new ModuleDropTarget(this, EditModeDNDListener.SIMPLEMODULE_TYPE);
                target.setAllowSelfAsSource(true);
                target.addDNDListener(mainModule.getEditLinker().getDndListener());
            } else {
                new DropTarget(this) {
                    @Override
                    protected void onDragEnter(DNDEvent event) {
                        event.getStatus().setStatus(false);
                    }
                };

            }
        }
        if (editable) {
            sinkEvents(Event.ONCLICK + Event.ONDBLCLICK + Event.ONMOUSEOVER + Event.ONMOUSEOUT + Event.ONCONTEXTMENU);

            Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    if (selectable) {
                        Log.info("click" + path + " : " + scriptInfo);
                        mainModule.setCtrlActive(ce.isControlKey());
                        if (!ce.isRightClick() || !mainModule.getSelections().containsKey(SimpleModule.this)) {
                            mainModule.getEditLinker().onModuleSelection(SimpleModule.this);
                        }
                    }
                }
            };
            addListener(Events.OnClick, listener);
            addListener(Events.OnContextMenu, listener);
            addListener(Events.OnDoubleClick, new EditContentEnginePopupListener(this, mainModule.getEditLinker()));

            Listener<ComponentEvent> hoverListener = new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    if (canHover) {
                        Hover.getInstance().addHover(SimpleModule.this);
                    }
                }
            };
            Listener<ComponentEvent> outListener = new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    Hover.getInstance().removeHover(SimpleModule.this);
                }
            };

            addListener(Events.OnMouseOver, hoverListener);
            addListener(Events.OnMouseOut, outListener);
        }
    }

    public void setNode(GWTJahiaNode node) {
        super.setNode(node);

        if (head != null && addIconInHeader) {
            GWTJahiaNodeType nodeType = ModuleHelper.getNodeType(node.getNodeTypes().get(0));
            head.setIcon(ContentModelIconProvider.getInstance().getIcon(nodeType));
        }

        if (node.isShared()) {
            this.setToolTip(new ToolTipConfig(Messages.get("info_important", "Important"), Messages.get("info_sharednode", "This is a shared node")));
        }

        HTML overlayLabel = null;
        if (node.getNodeTypes().contains("jmix:markedForDeletionRoot")) {
            overlayLabel = new HTML(Messages.get("label.deleted", "Deleted"));
        } else if (node.getInvalidLanguages() != null && node.getInvalidLanguages().contains(getMainModule().getEditLinker().getLocale())) {
            overlayLabel = new HTML(Messages.get("label.validLanguages.overlay",
                    "Not visible content"));
        }

        if (overlayLabel != null) {
            setStyleAttribute("position", "relative");

            insert(overlayLabel, 0);
            overlayLabel.addStyleName("deleted-overlay");
            overlayLabel.setHeight(Integer.toString(html.getOffsetHeight()) + "px");
            overlayLabel.setWidth(Integer.toString(html.getOffsetWidth()) + "px");

            DOM.setStyleAttribute(html.getElement(), "opacity", "0.4");

            layout();
        }

        if (bindable) {
            final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
            async.getProperties(node.getPath(), null, new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
                public void onSuccess(GWTJahiaGetPropertiesResult gwtJahiaGetPropertiesResult) {
                    if (gwtJahiaGetPropertiesResult.getProperties().containsKey(boundProperty)) {
                        final GWTJahiaNodeProperty o = gwtJahiaGetPropertiesResult.getProperties().get(boundProperty);
                        if (o.getValues().get(0).getNode().getPath().equals(mainModule.getPath())) {
                            setHeaderText(headerText + " - Linked to: main resource");
                            bound = Boolean.FALSE;
                        } else {
                            setHeaderText(headerText + " - Linked to: " + o.getValues().get(0).getNode().getName());
                            bound = Boolean.TRUE;
                        }
                    } else {
                        bound = Boolean.FALSE;
                        setHeaderText(head.getText() + " - Linked to: main resource");
                    }
                }
            });

        }
    }

    private class BindSelectionListener extends SelectionListener<IconButtonEvent> {
        private final MainModule mainModule;

        public BindSelectionListener(MainModule mainModule) {
            this.mainModule = mainModule;
        }

        public void componentSelected(IconButtonEvent event) {
            if (bound != null && bound) {
                List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();
//                            final List<GWTJahiaNode> srcNodes = e.getStatus().getData(SOURCE_NODES);
                final GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty(boundProperty,
                        new GWTJahiaNodePropertyValue((String) null,
                                GWTJahiaNodePropertyType.WEAKREFERENCE));
                properties.add(gwtJahiaNodeProperty);
                JahiaContentManagementService.App.getInstance()
                        .saveProperties(Arrays.asList(node), properties, null, new BaseAsyncCallback() {
                            public void onSuccess(Object o) {
                                Map<String, Object> data = new HashMap<String, Object>();
                                data.put(Linker.REFRESH_MAIN, true);
                                getMainModule().getEditLinker().refresh(data);
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
                mainModule.getInnerElement().getStyle().setProperty("cursor",
                        "url('" + s + "/gwt/resources/images/xtheme-jahia/link.cur'), pointer");

                mainModule.getEditLinker().setSelectionListener(new ModuleSelectionListener() {
                    public void onModuleSelection(Module selection) {
                        mainModule.getInnerElement().getStyle().setProperty("cursor", "");
                        mainModule.getEditLinker().setSelectionListener(null);
                        List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();
                        final GWTJahiaNodeProperty gwtJahiaNodeProperty;
                        if (selection.getNode() != null && selection.getNode() != node) {
                            gwtJahiaNodeProperty = new GWTJahiaNodeProperty(boundProperty, new GWTJahiaNodePropertyValue(selection.getNode(), GWTJahiaNodePropertyType.WEAKREFERENCE));
                        } else if (selection instanceof AreaModule) {
                            String areaHolder = ((AreaModule) selection).getAreaHolder();
                            gwtJahiaNodeProperty = new GWTJahiaNodeProperty(boundProperty, new GWTJahiaNodePropertyValue(areaHolder, GWTJahiaNodePropertyType.STRING));
                        } else {
                            return;
                        }
                        properties.add(gwtJahiaNodeProperty);
                        JahiaContentManagementService.App.getInstance()
                                .saveProperties(Arrays.asList(node), properties, null, new BaseAsyncCallback() {
                                    public void onSuccess(Object o) {
                                        Map<String, Object> data = new HashMap<String, Object>();
                                        data.put(Linker.REFRESH_MAIN, true);
                                        getMainModule().getEditLinker().refresh(data);
                                    }

                                    public void onApplicationFailure(Throwable throwable) {
                                        Window.alert("Failed : " + throwable);
                                    }
                                });

                    }
                });
            }
        }
    }
}
