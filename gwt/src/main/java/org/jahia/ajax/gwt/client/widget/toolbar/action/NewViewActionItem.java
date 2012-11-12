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

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;

import java.util.*;

public class NewViewActionItem extends BaseActionItem  {

    protected List<String> parentTypesAsList;

    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        parentTypesAsList = Arrays.asList("jnt:moduleVersionFolder", "jnt:nodeTypeFolder", "jnt:templateTypeFolder");
    }

    public void onComponentSelection() {
        final GWTJahiaNode parent = linker.getSelectionContext().getSingleSelection();
        String parentType = parent.getNodeTypes().get(0);

        final String[] filePath = parent.getPath().split("/");
        final String basePath = "/" + filePath[1] + "/" + filePath[2] + "/" + filePath[3];
        final String nodeType;
        if (parentType.equals("jnt:nodeTypeFolder") || parentType.equals("jnt:templateTypeFolder")) {
            nodeType = filePath[4].replaceFirst("_", ":");
        } else {
            nodeType = "";
        }
        final String templateType;
        if (parentType.equals("jnt:templateTypeFolder")) {
            templateType = filePath[5];
        } else {
            templateType = "";
        }


        // Open popup to select module

        final com.extjs.gxt.ui.client.widget.Window popup = new com.extjs.gxt.ui.client.widget.Window();
        popup.setHeading(Messages.get("label.addView", "Add view"));
        popup.setHeight(200);
        popup.setWidth(350);
        popup.setModal(true);
        FormPanel f = new FormPanel();
        f.setHeaderVisible(false);

        final TextField<String> nodeTypeField = new TextField<String>();
        nodeTypeField.setFieldLabel(Messages.get("label.nodetype", "Nodetype"));
        nodeTypeField.setValue(nodeType);
        f.add(nodeTypeField);

        final TextField<String> templateTypeField = new TextField<String>();
        templateTypeField.setFieldLabel(Messages.get("label.templateType", "Template type"));
        templateTypeField.setValue(templateType);
        f.add(templateTypeField);

        final TextField<String> viewNameField = new TextField<String>();
        viewNameField.setFieldLabel(Messages.get("label.viewName", "View name"));
        f.add(viewNameField);

        Button b = new Button(Messages.get("label.submit", "submit"));
        f.addButton(b);
        b.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                String newNodeType = nodeTypeField.getValue();
                if (newNodeType == null || newNodeType.trim().equals("") || newNodeType.contains("/")) {
                    Window.alert(Messages.get("label.nodetype.wrong", "Nodetype is not well formed."));
                    return;
                }
                newNodeType = newNodeType.replaceAll(":", "_");
                String newTemplateType = templateTypeField.getValue();
                if (newTemplateType == null || newTemplateType.trim().equals("") || newTemplateType.contains("/")) {
                    Window.alert(Messages.get("label.templateType.wrong", "Template type is not well formed."));
                    return;
                }
                String viewName = viewNameField.getValue();
                if (viewName == null || viewName.trim().equals("") || viewName.contains("/")) {
                    Window.alert(Messages.get("label.viewName.wrong", "View name is not well formed."));
                    return;
                }
                String parentPath = basePath + "/" + newNodeType + "/" + newTemplateType;
//                parent.setPath(parentPath);
                final String targetName = newNodeType.substring(newNodeType.indexOf(':') + 1) + "." + viewName + ".jsp";

                JahiaContentManagementService.App.getInstance().getNodeType("jnt:viewFile", new BaseAsyncCallback<GWTJahiaNodeType>() {
                    @Override
                    public void onSuccess(GWTJahiaNodeType nodeType) {
                        EngineLoader.showCreateEngine(linker, parent, nodeType, new HashMap<String, GWTJahiaNodeProperty>(), targetName, false);
                    }
                });

                popup.hide();
            }
        });
        Button c = new Button(Messages.get("label.cancel", "Cancel"));
        c.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                popup.hide();
            }
        });
        f.addButton(c);
        f.setButtonAlign(Style.HorizontalAlignment.CENTER);

        FormButtonBinding binding = new FormButtonBinding(f);
        binding.addButton(b);
        popup.add(f);
        popup.show();
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        GWTJahiaNode n = lh.getSingleSelection();
        if (n != null) {
            boolean isValidParent = false;
            for (String s : parentTypesAsList) {
                isValidParent = n.getNodeTypes().contains(s) || n.getInheritedNodeTypes().contains(s);
                if (isValidParent) {
                    break;
                }
            }
            setEnabled(isValidParent && !"".equals(n.getChildConstraints().trim())
                    && !lh.isLocked()
                    && hasPermission(lh.getSelectionPermissions())
                    && PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getSelectionPermissions()));
        } else {
            setEnabled(false);
        }
    }
}
