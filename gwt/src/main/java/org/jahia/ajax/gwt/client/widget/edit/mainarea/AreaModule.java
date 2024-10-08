/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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
import java.util.Collections;
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

    private String mockupStyle;
    private String areaType = "jnt:contentList";
    private String areaHolder;
    private boolean showAreaButton;
    private boolean missingList;
    private final LayoutContainer content;
    private String conflictsWith = null;

    private static int MIN_WIDTH = 100;

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
        this.showAreaButton = "true".equals(DOM.getElementAttribute(divElement, "showAreaButton"));
        this.areaHolder =  DOM.getElementAttribute(divElement, "areaHolder");
        this.conflictsWith = DOM.getElementAttribute(divElement, "conflictsWith");
        String areaType = DOM.getElementAttribute(divElement, "areaType");
        if (areaType != null && areaType.length() > 0) {
            this.areaType = areaType;
        }

        if (this.conflictsWith.length()==0) {
            this.conflictsWith = null;
        }

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

            Text label = new Text(Messages.getWithArgs("label.areaConflicts", "Area conflicts with same name node {0}. Rename the area or the node", new String[] { conflictsWith }));
            if (getWidth() >= MIN_WIDTH) {
                p.add(label);
            } else {
                p.setTitle(label.getText());
            }
            head.addTool(p);
            layout();
        } else if (missingList && editable && showAreaButton) {
            //If We are on edit mode and not in the studio remove the enable/disable button
                LayoutContainer p = new HorizontalPanel();

                Image icon = ToolbarIconProvider.getInstance().getIcon("enableArea").createImage();
                icon.setTitle(Messages.get("label.areaEnable", "Enable area"));


                p.add(icon);
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
            head.addTool(p);
            addStyleName(mainModule.getConfig().getName() + "DisableArea");
            content.addStyleName(mainModule.getConfig().getName() + "DisableAreaContent");
            layout();
        }
    }

    public void setEnabledEmptyArea() {
        //If We are on edit mode and not in the studio remove the enable/disable button
        if(showAreaButton)
        {
            HorizontalPanel p = new HorizontalPanel();
            Image icon =  ToolbarIconProvider.getInstance().getIcon("disableArea").createImage();
            icon.setTitle(Messages.get("label.areaDisable", "Disable area"));
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
                });
            }
            });
            head.addTool(p);
            addStyleName(mainModule.getConfig().getName() + "EnabledEmptyArea");
            content.addStyleName(mainModule.getConfig().getName()+"EnabledEmptyAreaContent");
            layout();
        }
    }

    public String getAreaHolder() {
        return areaHolder;
    }

    @Override public String getPath() {
        return "*";
    }

    public void createNode(final AsyncCallback<GWTJahiaNode> callback) {
        if (node == null) {
            JahiaContentManagementService.App.getInstance().createNode(path.substring(0, path.lastIndexOf('/')), path.substring(path.lastIndexOf('/') + 1),
                    areaType, Collections.singletonList("jmix:isAreaList"),null,null,null, null, null, true, new AsyncCallback<GWTJahiaNode>() {
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
