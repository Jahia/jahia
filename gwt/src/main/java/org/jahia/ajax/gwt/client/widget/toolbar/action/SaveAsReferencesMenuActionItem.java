/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

    @Override
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

                    @Override
                    public void onSuccess(List<GWTJahiaNode> result) {
                        if (result == null || result.size() == 0) {
                            MessageBox.alert(Messages.get("label.saveAsPortalComponent"), Messages.get("label.saveAsPortalComponent.portalComponents.nonedeclared", "There is no Portal Components folder declared. The component can not be saved"), null);
                        } else if (result.size() == 1) {
                            saveInPortalNode(result.get(0));
                        } else {
                            final Window popup = new Window();
                            popup.addStyleName("save-as-reference-modal");
                            popup.setHeadingHtml(Messages.get("label.saveAsPortalComponent.portalComponents.select", "Select a Portal Components folder"));
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

                    @Override
                    public void onApplicationFailure(Throwable caught) {
                        Info.display(Messages.get("label.saveAsPortalComponent"),
                                Messages.get("label.saveAsPortalComponent.cannotGetPortalNodes"));
                    }
                });
    }

    @Override
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
                        new BaseAsyncCallback<Object>() {

                            @Override
                            public void onApplicationFailure(Throwable caught) {
                                Info.display(Messages.get("label.saveAsPortalComponent"),
                                        Messages.get("label.saveAsPortalComponent.failure"));
                            }

                            @Override
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

