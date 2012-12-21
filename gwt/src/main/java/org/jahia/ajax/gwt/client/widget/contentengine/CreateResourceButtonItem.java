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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.*;


public class CreateResourceButtonItem extends SaveButtonItem {

    private String defaultContainerFolder;
    private String fileExtension;

    public Button create(final AbstractContentEngine engine) {
        Button button = new Button(Messages.get("label.save", "Save"));
        button.setHeight(BUTTON_HEIGHT);
        button.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                GWTJahiaNode node = engine.getNode();
                if (node == null) {
                    node = engine.getTargetNode();
                }
                final String nodePath = node.getPath();
                final String[] filePath = nodePath.split("/");

                final Window popup = new Window();
                popup.setHeading(Messages.get("label.saveAs", "Save as ..."));
                popup.setHeight(120);
                popup.setWidth(350);
                popup.setModal(true);
                FormPanel f = new FormPanel();
                f.setHeaderVisible(false);
                f.setBorders(false);
                final TextField<String> name = new TextField<String>();
                name.setFieldLabel(Messages.get("label.name", "Name"));
                name.setMinLength(1);
                f.add(name);

                Button b = new Button(Messages.get("label.submit", "submit"));
                f.addButton(b);
                b.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        String path;

                        Map<String, String> parentNodesType = new LinkedHashMap<String, String>();
                        if (defaultContainerFolder.equals(filePath[4])) {
                            path = nodePath;
                        } else {
                            path = "/modulesFileSystem/" + filePath[2] + "/" + filePath[3] + "/" + defaultContainerFolder;
                            parentNodesType.put("modulesFileSystem", "jnt:folder");
                            parentNodesType.put(filePath[2], "jnt:folder");
                            parentNodesType.put(filePath[3], "jnt:folder");
                            parentNodesType.put(defaultContainerFolder, "jnt:folder");
                        }
                        String finalName = name.getValue();
                        if (!finalName.endsWith("." + fileExtension)) {
                            finalName += "." + fileExtension;
                        }
                        prepareAndSave(path, finalName, parentNodesType, (CreateContentEngine) engine);
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
        });
        return button;
    }

    protected void prepareAndSave(final String parentPath, String nodeName, Map<String, String> parentNodesType, final CreateContentEngine engine) {

        final Set<String> addedTypes = new HashSet<String>();
        final Set<String> removedTypes = new HashSet<String>();

        List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();
        properties.addAll(engine.getProperties().values());
        for (TabItem tab : engine.getTabs().getItems()) {
            EditEngineTabItem item = tab.getData("item");
            item.doSave(null, engine.getChangedProperties(), engine.getChangedI18NProperties(), addedTypes, removedTypes, null, engine.getAcl());
        }
        if (properties.size() > 0) {
            // Edit
            for (GWTJahiaNodeProperty p : properties) {
                for (GWTJahiaNodeProperty p1 : engine.getChangedProperties()) {
                    if (p.getName().equals(p1.getName())) {
                        properties.get(properties.indexOf(p)).setValues(p1.getValues());
                    }
                }
            }
        } else {
            // Create
            properties = engine.getChangedProperties();
        }
        JahiaContentManagementService.App.getInstance().createNode(parentPath, nodeName, engine.getType().getName(), null, engine.getAcl(), properties, engine.changedI18NProperties, null, parentNodesType, false, new AsyncCallback<GWTJahiaNode>() {
            public void onFailure(Throwable throwable) {
                MessageBox.alert("save not work as expected", throwable.getMessage(), null);
            }
            public void onSuccess(GWTJahiaNode gwtJahiaNode) {
                Linker linker = engine.getLinker();
                engine.close();
                linker.refresh(Linker.REFRESH_SOURCES, null);
            }
        });


    }

    @Override
    protected void prepareAndSave(final AbstractContentEngine engine, boolean closeAfterSave) {
    }

    public void setDefaultContainerFolder(String defaultContainerFolder) {
        this.defaultContainerFolder = defaultContainerFolder;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
}
