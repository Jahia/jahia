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

import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:03:48 PM
 *
 */
public class PlaceholderModule extends Module {
    private LayoutContainer panel;
    private LayoutContainer pasteButton;

//    private static LayoutContainer currentlyVisiblePanel = null;

    public PlaceholderModule(String id, String path, Element divElement, final MainModule mainModule) {
        super(id, path, divElement, mainModule, new FlowLayout());

        if (path.endsWith("*")) {
            setBorders(false);
        } else {
            setBorders(true);
        }
        if (mainModule.getConfig().isButtonsInLayer()) {
            final LayoutContainer visiblePanel = new LayoutContainer();
            //        panel.setHorizontalAlign(Style.HorizontalAlignment.CENTER);
            visiblePanel.addStyleName("x-small-editor");
            visiblePanel.addStyleName("x-panel-header");
            visiblePanel.addStyleName("x-panel-placeholder");

            visiblePanel.setHeight(10);
            visiblePanel.setStyleAttribute("position", "relative");

//            panel = new LayoutContainer() {
//                @Override
//                protected void onShow() {
//                    super.onShow();
//                    if (currentlyVisiblePanel != null) {
//                        currentlyVisiblePanel.hide();
//                    }
//                    currentlyVisiblePanel = this;
//
//                }
//
//                @Override
//                protected void onHide() {
//                    super.onHide();
//                    currentlyVisiblePanel = null;
//                }
//            };
//            panel.addStyleName("x-panel-buttons-layer");
//
            html = new HTML("");
//            html.setStyleName("label-placeholder");
//            panel.add(html);
//            panel.hide();
            add(visiblePanel);
        } else {
            panel = new LayoutContainer();
            panel.addStyleName("x-small-editor");
            panel.addStyleName("x-panel-header");
            panel.addStyleName("x-panel-placeholder");

            html = new HTML(Messages.get("label.add") + " : &nbsp;");
            html.setStyleName("label-placeholder");
            panel.add(html);
            add(panel);
        }

    }

    @Override
    public void onParsed() {
    }

    public void onNodeTypesLoaded() {
        if (mainModule.getConfig().isEnableDragAndDrop()) {
            DropTarget target = new ModuleDropTarget(this, EditModeDNDListener.PLACEHOLDER_TYPE);
            target.setOperation(DND.Operation.COPY);
            target.setFeedback(DND.Feedback.INSERT);

            target.addDNDListener(mainModule.getEditLinker().getDndListener());
        }

        if (getParentModule().getChildCount() >= getParentModule().getListLimit() && getParentModule().getListLimit() != -1) {
            return;
        }

        if (mainModule.getConfig().isButtonsInLayer()) {
//            panel.setWidth(getParentModule().getContainer().getWidth());
//            panel.setHeight(getParentModule().getContainer().getHeight());
//            getParentModule().getContainer().setStyleAttribute("position","relative");
//            getParentModule().getContainer().add(panel);
//            getParentModule().getContainer().layout();
//
//            final LayoutContainer tool = new LayoutContainer();
//            tool.setHeight(16);
//            tool.setWidth(16);
//            tool.setStyleAttribute("position", "relative");
//            tool.addStyleName("x-panel-placeholder");
//            tool.addListener(Events.OnClick, new Listener<BaseEvent>() {
//                @Override
//                public void handleEvent(BaseEvent be) {
//                    panel.show();
//                }
//            });
//
//            panel.addListener(Events.OnClick, new Listener<BaseEvent>() {
//                @Override
//                public void handleEvent(BaseEvent be) {
//                    panel.hide();
//                }
//            });
//
//            getParentModule().getHeader().addTool(tool);
        }


        String headerText;
        if (parentModule.path != null && parentModule.path.contains("/")) {
            headerText =  parentModule.path.substring(parentModule.path.lastIndexOf('/') + 1);
        } else {
            headerText =   parentModule.path;
        }
        if (getWidth() > 300) {
            html.setHTML("<div class=\"label-placeholder\">"+Messages.get("label.addTo") + "&nbsp;" + headerText + " : &nbsp;"+"</div>");
        }

        if (getParentModule() instanceof AreaModule && getParentModule().getChildCount() == 0 && ((AreaModule) getParentModule()).editable) {
            AbstractImagePrototype icon =  ToolbarIconProvider.getInstance().getIcon("disableArea");
            final LayoutContainer p = new HorizontalPanel();
            p.add(icon.createImage());
            if (!mainModule.getConfig().isButtonsInLayer()) {
                if (getWidth() > 150) {
                    p.add(new Text(Messages.get("label.areaDisable", "Disable area")));
                }
            }
            p.sinkEvents(Event.ONCLICK);
            p.addStyleName("button-placeholder");
            p.addListener(Events.OnClick, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent be) {
                    JahiaContentManagementService.App.getInstance().deletePaths(Arrays.asList(parentModule.path), new BaseAsyncCallback<GWTJahiaNode>() {
                        public void onSuccess(GWTJahiaNode result) {
                            mainModule.getEditLinker().refresh(EditLinker.REFRESH_MAIN, null);
                        }
                    });
                }
            });
            if (mainModule.getConfig().isButtonsInLayer()) {
                getParentModule().getHeader().addTool(p);
            } else {
                panel.add(p);
            }
        }

        String[] nodeTypesArray = null;
        if (getParentModule() != null && getParentModule().getNodeTypes() != null) {
            nodeTypesArray = getParentModule().getNodeTypes().split(" ");
        }
        if ((getNodeTypes() != null) && (getNodeTypes().length() > 0)) {
            nodeTypesArray = getNodeTypes().split(" ");
        }
        if (nodeTypesArray != null) {
            List filter = null;
            if (nodeTypes != null && nodeTypes.length()>0) {
                filter = Arrays.asList(nodeTypes.split(" "));
            }
            final Set<String> displayedNodeTypes = new HashSet<String>(Arrays.asList(nodeTypesArray));
            for (final String s : nodeTypesArray) {
                if (filter != null && !filter.contains(s)) {
                    continue;
                }
                GWTJahiaNodeType nodeType = ModuleHelper.getNodeType(s);
                if (nodeType != null) {
                    Boolean canUseComponentForCreate = (Boolean) nodeType.get("canUseComponentForCreate");
                    if (canUseComponentForCreate != null && !canUseComponentForCreate) {
                        continue;
                    }
                }
                AbstractImagePrototype icon = ContentModelIconProvider.getInstance().getIcon(nodeType);
                LayoutContainer p = new HorizontalPanel();
                p.add(icon.createImage());
                if (!mainModule.getConfig().isButtonsInLayer()) {
                    if (getWidth() > 150) {
                        p.add(new Text(nodeType != null ? nodeType.getLabel() : s));
                    }
                }
                p.sinkEvents(Event.ONCLICK);
                p.addStyleName("button-placeholder");
                p.addListener(Events.OnClick, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent be) {
                        final GWTJahiaNode parentNode = getParentModule().getNode();
                        if (parentNode != null && PermissionsUtils.isPermitted("jcr:addChildNodes", parentNode) && !parentNode.isLocked()) {
                            String nodeName = null;
                            if ((path != null) && !"*".equals(path) && !path.startsWith("/")) {
                                nodeName = path;
                            }
                            ContentActions.showContentWizard(mainModule.getEditLinker(), s, parentNode, nodeName, true, displayedNodeTypes);
                        }
                    }
                });
                if (mainModule.getConfig().isButtonsInLayer()) {
                    getParentModule().getHeader().addTool(p);
                } else {
                    panel.add(p);
                }
            }

            AbstractImagePrototype icon = ToolbarIconProvider.getInstance().getIcon("paste");
            pasteButton = new HorizontalPanel();
            pasteButton.add(icon.createImage());
            if (!mainModule.getConfig().isButtonsInLayer()) {
                if (getWidth() > 150) {
                    pasteButton.add(new Text(Messages.get("label.paste", "Paste")));
                }
            }
            pasteButton.sinkEvents(Event.ONCLICK);
            pasteButton.addStyleName("button-placeholder");

            pasteButton.addListener(Events.OnClick, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent be) {
                    GWTJahiaNode parentNode = getParentModule().getNode();
                    if (parentNode != null && PermissionsUtils.isPermitted("jcr:addChildNodes", parentNode) && !parentNode.isLocked()) {
                        CopyPasteEngine.getInstance().paste(parentNode, mainModule.getEditLinker());
                    }
                }
            });
            CopyPasteEngine.getInstance().addPlaceholder(this);
            updatePasteButton();

            if (mainModule.getConfig().isButtonsInLayer()) {
                getParentModule().getHeader().addTool(pasteButton);
            } else {
                panel.add(pasteButton);
                panel.layout();
            }
        }
    }

    public boolean isDraggable() {
        return false;
    }

    public void setParentModule(Module parentModule) {
        this.parentModule = parentModule;
    }

    public void updatePasteButton() {
        if (CopyPasteEngine.getInstance().getCopiedPaths() != null && /*CopyPasteEngine.getInstance().canCopyTo(parentModule.getNode()) &&*/ CopyPasteEngine.getInstance().checkNodeType(parentModule.getNodeTypes())) {
            pasteButton.setVisible(true);
        } else {
            pasteButton.setVisible(false);
        }
    }

}
