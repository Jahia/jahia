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
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;

import java.util.Arrays;
import java.util.List;


/**
 * 
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:03:48 PM
 *
 */
public class PlaceholderModule extends Module {
    private LayoutContainer panel;

    public PlaceholderModule(String id, String path, Element divElement, MainModule mainModule) {
        super(id, path, divElement, mainModule, new FlowLayout());

        if (path.endsWith("*")) {
            setBorders(false);
        } else {
            setBorders(true);
        }
        panel = new LayoutContainer();
        //panel.setHorizontalAlign(Style.HorizontalAlignment.CENTER);
        panel.addStyleName("x-small-editor");
        panel.addStyleName("x-panel-header");
        panel.addStyleName("x-panel-placeholder");

        html = new HTML(Messages.get("label.add") + " : &nbsp;");
        html.setStyleName("label-placeholder");
        panel.add(html);
        add(panel);
    }

    @Override
    public void onParsed() {
    }

    public void onNodeTypesLoaded() {
        DropTarget target = new ModuleDropTarget(this, EditModeDNDListener.PLACEHOLDER_TYPE);
        target.setOperation(DND.Operation.COPY);
        target.setFeedback(DND.Feedback.INSERT);

        target.addDNDListener(mainModule.getEditLinker().getDndListener());

        if (getParentModule().getChildCount() >= getParentModule().getListLimit() && getParentModule().getListLimit() != -1) {
            return;
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
                filter = Arrays.asList(nodeTypes);
            }
            for (final String s : nodeTypesArray) {
                if (filter != null && !filter.contains(s)) {
                    continue;
                }
                Button button = new Button(ModuleHelper.getNodeType(s) != null ? ModuleHelper.getNodeType(
                        s).getLabel() : s);
                button.setStyleName("button-placeholder");
                button.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        final GWTJahiaNode parentNode = getParentModule().getNode();
                        if (parentNode != null && PermissionsUtils.isPermitted("jcr:addChildNodes", parentNode) && !parentNode.isLocked()) {
                            String nodeName = null;
                            if ((path != null) && !"*".equals(path) && !path.startsWith("/")) {
                                nodeName = path;
                            }
                            ContentActions.showContentWizard(mainModule.getEditLinker(), s, parentNode, nodeName, true);
                        }
                    }
                });
                panel.add(button);
                panel.layout();
            }
        }

    }

    public boolean isDraggable() {
        return false;
    }

    public void setParentModule(Module parentModule) {
        this.parentModule = parentModule;
        String headerText;
        if (parentModule.path.contains("/")) {
            headerText =  parentModule.path.substring(parentModule.path.lastIndexOf('/') + 1);
        } else {
            headerText =   parentModule.path;
        }

        html.setHTML(Messages.get("label.addTo") + headerText + " : &nbsp;");
    }
}
