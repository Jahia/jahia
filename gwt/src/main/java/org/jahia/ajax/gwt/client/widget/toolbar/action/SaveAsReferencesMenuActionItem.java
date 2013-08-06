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

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.Arrays;
import java.util.List;

/**
 *
 * User: ktlili
 * Date: Jan 20, 2010
 * Time: 1:51:18 PM
 */
public class SaveAsReferencesMenuActionItem extends BaseActionItem {
    private GWTJahiaProperty targetName;
    private GWTJahiaProperty allowedNodeType;

    public void init(GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        super.init(gwtToolbarItem, linker);
        setEnabled(false);
        targetName = gwtToolbarItem.getProperties().get("targetName");
        allowedNodeType = gwtToolbarItem.getProperties().get("allowedNodeType");
    }

    @Override
    public void onComponentSelection() {
        JahiaContentManagementService.App.getInstance().getPortalNodes(targetName.getValue(),
                new BaseAsyncCallback<List<GWTJahiaNode>>() {
                    public void onSuccess(List<GWTJahiaNode> result) {
                        if (result == null || result.size() == 0) {
                            MessageBox.alert(Messages.get("label.saveAsPortalComponent"), Messages.get("label.saveAsPortalComponent.portal-components.nonedeclared", "There is no Portal Components folder declared. The component can not be saved"), null);
                        } else if (result.size() == 1) {
                            saveInPortalNode(result.get(0));
                        } else {
                            final Window popup = new Window();
                            popup.setHeading(Messages.get("label.saveAsPortalComponent.portal-components.select", "Select a Portal Components folder"));
                            popup.setWidth(500);
                            popup.setAutoHeight(true);
                            popup.setModal(true);
                            FormPanel f = new FormPanel();
                            f.setHeaderVisible(false);
                            final ComboBox<GWTJahiaNode> portalNodesCombo = new ComboBox<GWTJahiaNode>();
                            portalNodesCombo.setStore(new ListStore<GWTJahiaNode>());
                            portalNodesCombo.setDisplayField(GWTJahiaNode.PATH);
                            portalNodesCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
                            portalNodesCombo.setForceSelection(true);
                            portalNodesCombo.getStore().add(result);
                            ContentPanel p = new ContentPanel();
                            p.setLayout(new FitLayout());
                            p.setCollapsible(false);
                            p.setFrame(false);
                            p.setAnimCollapse(false);
                            p.setBorders(false);
                            p.setBodyBorder(false);
                            p.setHeaderVisible(false);
                            p.add(portalNodesCombo);
                            f.add(p);

                            Button b = new Button(Messages.get("label.save", "Save"));
                            f.addButton(b);
                            b.addSelectionListener(new SelectionListener<ButtonEvent>() {
                                @Override
                                public void componentSelected(ButtonEvent buttonEvent) {
                                    GWTJahiaNode portalNode = portalNodesCombo.getValue();
                                    if (portalNode != null) {
                                        saveInPortalNode(portalNode);
                                    }
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
                    }

                    public void onApplicationFailure(Throwable caught) {
                        Info.display("Portal Components",
                                "Error while getting My Portal nodes.");
                    }
                });
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.getSingleSelection() != null && hasPermission(lh.getSingleSelection()) && lh.getSingleSelection().getInheritedNodeTypes().contains(allowedNodeType.getValue()));
    }

    private void saveInPortalNode(final GWTJahiaNode portalNode) {
        if (PermissionsUtils.isPermitted("jcr:write", portalNode.getPermissions())) {
            LinkerSelectionContext lh = linker.getSelectionContext();
            GWTJahiaNode target = lh.getSingleSelection();
            if (target != null) {
                JahiaContentManagementService.App.getInstance().pasteReferences(
                        Arrays.asList(target.getPath()), portalNode.getPath(), null,
                        new BaseAsyncCallback() {
                            public void onApplicationFailure(Throwable caught) {
                                Info.display("Portal Components",
                                        "Error while making your component available for users in their portal page.");
                            }

                            public void onSuccess(Object result) {
                                com.google.gwt.user.client.Window.alert(Messages.getWithArgs("label.saveAsPortalComponent.success",
                                        "Component saved in {0}. It will be available for My Portal users only after publication. To proceed, go in the Content Tab to the selected Portal Components folder, select the component you want to publish in the lower tab then publish it (use right click).",
                                        new Object[] {portalNode.getPath()}));
                            }
                        });
            }
        } else {
            MessageBox.alert(Messages.get("label.saveAsPortalComponent"), Messages.get("label.saveAsPortalComponent.denied"), null);
        }
    }
}

