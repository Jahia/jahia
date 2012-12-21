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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import com.google.gwt.user.client.Element;

/**
 *
 * User: toto
 * Date: Aug 18, 2009
 * Time: 7:25:48 PM
 *
 */
public class AreaModule extends SimpleModule {

    private String moduleType;
    private String mockupStyle;
    private String areaType = "jnt:contentList";  // todo set the areatype
    private boolean missingList;

    public AreaModule(String id, String path, Element divElement, String moduleType, MainModule mainModule) {
        super(id, path, divElement, mainModule);
        hasDragDrop = false;
        head = new Header();

        if (editable || mainModule.getConfig().getName().equals("studiolayoutmode")) {
            add(head);
        }

        this.mockupStyle = DOM.getElementAttribute(divElement, "mockupStyle");
        this.missingList = "true".equals(DOM.getElementAttribute(divElement, "missingList"));

        this.moduleType = moduleType;
        String headerText;
        String areaTitle;
        if (path.contains("/")) {
            areaTitle = path.substring(path.lastIndexOf('/') + 1);
        } else {
            areaTitle = path;
        }
        headerText = Messages.get("label."+moduleType)+" : " + areaTitle;
        head.setId("JahiaGxtArea__" + areaTitle);

        head.setText(headerText);
//        setBodyBorder(false);
        head.addStyleName("x-panel-header");
        head.addStyleName("x-panel-header-"+moduleType+"module");

        html = new HTML(divElement.getInnerHTML());
        add(html);
    }

    LayoutContainer ctn;

    @Override public void onParsed() {
        super.onParsed();
        String headerText = head.getText();

        String areaTitle;
        if (path.contains("/")) {
            areaTitle = path.substring(path.lastIndexOf('/') + 1);
        } else {
            areaTitle = path;
        }

        if (missingList && editable) {
            addStyleName(mainModule.getConfig().getName()+"DisabledArea");
            headerText += " (" + Messages.get("label.notCreated", "not created")+ ")";
            if (mockupStyle != null) {
                addStyleName(mockupStyle);
            }
//            removeAll();

            ctn = new LayoutContainer();
            ctn.addText(headerText);

//            removeAll();

            LayoutContainer dash = new LayoutContainer();
            dash.addStyleName(mainModule.getConfig().getName()+"AreaTemplate");

            ctn = new LayoutContainer();
            ctn.addStyleName(moduleType+"Template");
            ctn.addText(headerText);

            dash.add(ctn);
            dash.add(html);

            add(dash);
        } else if (visibleChildCount == 0 && childCount > 0) {
            addStyleName(mainModule.getConfig().getName()+"BlockedArea");
            headerText += " ( content )";
            if (mockupStyle != null) {
                addStyleName(mockupStyle);
            }
//            removeAll();

            ctn = new LayoutContainer();
            ctn.addText(headerText);

//            removeAll();

            LayoutContainer dash = new LayoutContainer();
            dash.addStyleName(mainModule.getConfig().getName()+"AreaTemplate");

            ctn = new LayoutContainer();
            ctn.addStyleName(moduleType+"Template");
            ctn.addText(headerText);

            dash.add(ctn);
            dash.add(html);

            add(dash);
        } else {
            addStyleName(mainModule.getConfig().getName()+"Area");
            setBorders(false);
        }
    }

    @Override public void onNodeTypesLoaded() {
        if (missingList && editable) {
            AbstractImagePrototype icon =  ToolbarIconProvider.getInstance().getIcon("enableArea");
            LayoutContainer p = new HorizontalPanel();
            p.add(icon.createImage());
            if (getWidth() > 150) {
                p.add(new Text(Messages.get("label.areaEnable", "Enable area")));
            }
            p.sinkEvents(Event.ONCLICK);
            p.addStyleName("button-placeholder");
            p.addListener(Events.OnClick, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent be) {
                    createNode(new BaseAsyncCallback<GWTJahiaNode>() {
                        public void onSuccess(GWTJahiaNode result) {
                            mainModule.getEditLinker().refresh(EditLinker.REFRESH_MAIN, null);
                        }
                    });
                }
            });
            ctn.add(p);
            ctn.layout();
//        } else if (childCount == 0 && editable) {
//            ctn.add(p);
//
//            if (mainModule.getConfig().isEnableDragAndDrop()) {
//                DropTarget target = new ModuleDropTarget(this, node == null ? EditModeDNDListener.EMPTYAREA_TYPE : EditModeDNDListener.PLACEHOLDER_TYPE);
//                target.setOperation(DND.Operation.COPY);
//                target.setFeedback(DND.Feedback.INSERT);
//                target.addDNDListener(mainModule.getEditLinker().getDndListener());
//            }
//            if (getNodeTypes() != null) {
//                String[] nodeTypesArray = getNodeTypes().split(" ");
//                for (final String s : nodeTypesArray) {
//                    GWTJahiaNodeType nodeType = ModuleHelper.getNodeType(s);
//                    if (nodeType != null) {
//                        Boolean canUseComponentForCreate = (Boolean) nodeType.get("canUseComponentForCreate");
//                        if (canUseComponentForCreate != null && !canUseComponentForCreate) {
//                            continue;
//                        }
//                    }
//                    icon = ContentModelIconProvider.getInstance().getIcon(nodeType);
//                    p = new HorizontalPanel();
//                    p.add(icon.createImage());
//                    if (getWidth() > 150) {
//                        p.add(new Text(nodeType != null ? nodeType.getLabel() : s));
//                    }
//                    p.sinkEvents(Event.ONCLICK);
//                    p.addStyleName("button-placeholder");
//                    p.addListener(Events.OnClick, new Listener<ComponentEvent>() {
//                        public void handleEvent(ComponentEvent be) {
//                            createNode(new BaseAsyncCallback<GWTJahiaNode>() {
//                                public void onSuccess(GWTJahiaNode result) {
//                                    if (node != null && PermissionsUtils.isPermitted("jcr:addChildNodes", node) && !node.isLocked()) {
//                                        ContentActions.showContentWizard(mainModule.getEditLinker(), s, node, true);
//                                    }
//                                }
//                            });
//                        }
//                    });
//
//                    ctn.add(p);
//                }
//            }
//            ctn.layout();
        }
    }

    @Override public String getPath() {
        return "*";
    }

    public void createNode(final AsyncCallback<GWTJahiaNode> callback) {
        if (node == null) {
            String areaType = this.areaType;
            if (mainModule.getConfig().getName().contains("layout")) {
                // move this config property
                areaType = "jnt:layoutContentList";
            }
            JahiaContentManagementService.App.getInstance().createNode(path.substring(0, path.lastIndexOf('/')), path.substring(path.lastIndexOf('/') + 1),
                    areaType, null,null,null,null, null, null, true, new AsyncCallback<GWTJahiaNode>() {
                public void onSuccess(GWTJahiaNode result) {
                    node = result;
                    callback.onSuccess(result);
                }

                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }
            });
        } else {
            callback.onSuccess(node);
        }
    }
}