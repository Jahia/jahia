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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The module that displays areas in edit mode.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 7:25:48 PM
 *
 */
public class AreaModule extends SimpleModule {

    private String moduleType;
    private String mockupStyle;
    private String areaType = "jnt:contentList";  // todo set the areatype
    private String areaHolder;
    private boolean missingList;
    private final LayoutContainer content;
    private String conflictsWith = null;

    private int MIN_WIDTH = 100;
    
    public AreaModule(String id, String path, Element divElement, String moduleType, MainModule mainModule) {
        super(id, path, divElement, mainModule);
        hasDragDrop = false;
        addIconInHeader = false;

        head = new Header();

        if (editable) {
            add(head);
        }

        this.mockupStyle = DOM.getElementAttribute(divElement, "mockupStyle");
        this.missingList = "true".equals(DOM.getElementAttribute(divElement, "missingList"));
        this.areaHolder =  DOM.getElementAttribute(divElement, "areaHolder");
        this.conflictsWith = DOM.getElementAttribute(divElement, "conflictsWith");
        if (this.conflictsWith.length()==0) {
            this.conflictsWith = null;
        }

        this.moduleType = moduleType;
        String areaTitle;
        if (path.contains("/")) {
            areaTitle = path.substring(path.lastIndexOf('/') + 1);
        } else {
            areaTitle = path;
        }
        setHeaderText(areaTitle);

        head.setId("JahiaGxtArea__" + areaTitle);
        head.setTextStyle("x-panel-header-text-"+moduleType+"module");
        head.addStyleName("x-panel-header");
        head.addStyleName("x-panel-header-"+moduleType+"module");

        html = new HTML(divElement.getInnerHTML());

        content = new LayoutContainer();
        content.add(html);
        add(content);
    }

    @Override public void onParsed() {
        super.onParsed();

        addStyleName(mainModule.getConfig().getName()+"Area");
        setBorders(true);

        if (missingList && editable) {
            if (mockupStyle != null) {
                addStyleName(mockupStyle);
            }
            canHover = false;
        }
    }

    @Override public void onNodeTypesLoaded() {
        if (conflictsWith != null) {
            LayoutContainer p = new HorizontalPanel();
            
<<<<<<< .working
            Text label = new Text(Messages.getWithArgs("label.areaConflicts", "Area conflicts with same name node {0}. Rename the area or the node", new String[] { conflictsWith })); 
            if (getWidth() >= MIN_WIDTH) {
=======
            Text label = new Text(Messages.getWithArgs("label.areaConflicts", "Area conflicts with same name node {0}. Rename the area or the node", new String[] { conflictsWith }));
            if (getWidth() >= MIN_WIDTH) {
>>>>>>> .merge-right.r46397
                p.add(label);
            } else {
                p.setTitle(label.getText());
            }
            head.addTool(p);
            layout();
        } else if (missingList && editable) {
            Image icon =  ToolbarIconProvider.getInstance().getIcon("enableArea").createImage();
            icon.setTitle(Messages.get("label.areaEnable", "Enable area"));
            LayoutContainer p = new HorizontalPanel();
<<<<<<< .working

            p.add(icon);
=======
            p.add(icon.createImage());
            
            Text label = new Text(Messages.get("label.areaEnable", "Enable area"));
            if (getWidth() >= MIN_WIDTH) {
                p.add(label);
            } else {
            	p.setTitle(label.getText());
            }
>>>>>>> .merge-right.r46397
            p.sinkEvents(Event.ONCLICK);
            p.addStyleName("button-enable");
            p.addListener(Events.OnClick, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent be) {
                    createNode(new BaseAsyncCallback<GWTJahiaNode>() {
                        public void onSuccess(GWTJahiaNode result) {
                            Map<String, Object> data = new HashMap<String, Object>();
                            data.put(Linker.REFRESH_MAIN, true);
                            mainModule.getEditLinker().refresh(data);
                        }
                    });
                }
            });
<<<<<<< .working
            head.addTool(p);
=======
            ctn.add(p);
            ctn.layout();
        } else if (childCount == 0 && editable) {
            AbstractImagePrototype icon =  ToolbarIconProvider.getInstance().getIcon("disableArea");
            LayoutContainer p = new HorizontalPanel();
            p.add(icon.createImage());
            
            Text disableLabel = new Text(Messages.get("label.areaDisable", "Disable area"));
            if (getWidth() >= MIN_WIDTH) {
                p.add(disableLabel);
            } else {
            	p.setTitle(disableLabel.getText());
            }
            p.sinkEvents(Event.ONCLICK);
            p.addStyleName("button-placeholder");
            p.addListener(Events.OnClick, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent be) {
                    JahiaContentManagementService.App.getInstance().deletePaths(Arrays.asList(path), new BaseAsyncCallback<GWTJahiaNode>() {
                        public void onSuccess(GWTJahiaNode result) {
                            mainModule.getEditLinker().refresh(EditLinker.REFRESH_MAIN);
                        }
                    });
                }
            });
>>>>>>> .merge-right.r46397

            addStyleName(mainModule.getConfig().getName() + "DisableArea");
            content.addStyleName(mainModule.getConfig().getName() + "DisableAreaContent");
            layout();
        }
    }

    public void setEnabledEmptyArea() {
        Image icon =  ToolbarIconProvider.getInstance().getIcon("disableArea").createImage();
        icon.setTitle(Messages.get("label.areaDisable", "Disable area"));
        final HorizontalPanel p = new HorizontalPanel();
        p.add(icon);
        p.sinkEvents(Event.ONCLICK);
        p.addStyleName("button-disable");
        p.addListener(Events.OnClick, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                JahiaContentManagementService.App.getInstance().deletePaths(Arrays.asList(path), new BaseAsyncCallback<GWTJahiaNode>() {
                    public void onSuccess(GWTJahiaNode result) {
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put(Linker.REFRESH_MAIN, true);
                        mainModule.getEditLinker().refresh(data);
                    }
<<<<<<< .working
                });
=======
                    icon = ContentModelIconProvider.getInstance().getIcon(nodeType);
                    p = new HorizontalPanel();
                    p.add(icon.createImage());
                    
                    Text nodeTypeLabel = new Text(nodeType != null ? nodeType.getLabel() : s);
                    if (getWidth() >= MIN_WIDTH) {
                        p.add(nodeTypeLabel);
                    } else {
                    	p.setTitle(nodeTypeLabel.getText());
                    }
                    
                    p.sinkEvents(Event.ONCLICK);
                    p.addStyleName("button-placeholder");
                    p.addListener(Events.OnClick, new Listener<ComponentEvent>() {
                        public void handleEvent(ComponentEvent be) {
                            createNode(new BaseAsyncCallback<GWTJahiaNode>() {
                                public void onSuccess(GWTJahiaNode result) {
                                    if (node != null && PermissionsUtils.isPermitted("jcr:addChildNodes", node) && !node.isLocked()) {
                                        ContentActions.showContentWizard(mainModule.getEditLinker(), s, node, true);
                                    }
                                }
                            });
                        }
                    });

                    ctn.add(p);
                }
>>>>>>> .merge-right.r46397
            }
        });
        head.addTool(p);
        addStyleName(mainModule.getConfig().getName()+"EnabledEmptyArea");
        content.addStyleName(mainModule.getConfig().getName()+"EnabledEmptyAreaContent");
        layout();
    }

    public String getAreaHolder() {
        return areaHolder;
    }

    @Override public String getPath() {
        return "*";
    }

    public void createNode(final AsyncCallback<GWTJahiaNode> callback) {
        if (node == null) {
            String areaType = this.areaType;
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