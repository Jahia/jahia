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
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.toolbar.action.SiteSwitcherActionItem;

import java.util.*;


/**
 * User: david
 * Date: 11/5/12
 * Time: 9:59 AM
 * Defines a button that opens a popup and allows to save a view ( see ModuleDataSource ) with custom parameters
 */
public class SaveAsViewButtonItem extends SaveButtonItem {

    public static final int INDEX_OF_FILE_TYPE = 5;
    public static final int INDEX_OF_FILE_NAME = 7;
    public static final int INDEX_OF_TEMPLATE_TYPE = 6;

    public Button create(final AbstractContentEngine engine) {
        Button button = new Button(Messages.get("label.saveAs", "Save as ..."));
        button.setHeight(BUTTON_HEIGHT);
        button.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                GWTJahiaNode node = engine.getNode();
                if (node == null) {
                    node = engine.getTargetNode();
                }
                final String[] filePath = node.getPath().split("/");

                String moduleVersionTmp;
                String fileTypeTmp= "";
                String fileTemplateTypeTmp= "html";
                String fileNameTmp = "";
                if ("modules".equals(filePath[1])) {
                    moduleVersionTmp = (String) JahiaGWTParameters.getSiteNode().getProperties().get("j:versionInfo");
                    if (engine instanceof CreateContentEngine) {
                        fileTypeTmp = ((CreateContentEngine) engine).getTargetName();
                        fileNameTmp = fileTypeTmp.indexOf("_")>1?fileTypeTmp.substring(fileTypeTmp.indexOf("_") + 1) + ".jsp":fileTypeTmp+".jsp";
                    } else {
                        fileTypeTmp = filePath[INDEX_OF_FILE_TYPE];
                        fileNameTmp = filePath[INDEX_OF_FILE_NAME];
                        fileTemplateTypeTmp=filePath[INDEX_OF_TEMPLATE_TYPE];
                    }

                } else {
                    MessageBox.alert("save not work as expected", "An issue occurs when trying to resolve " + node.getPath(), null);
                    return;
                }
                final String modulePath = "/modules/" + filePath[2];
                final String moduleName = filePath[2];
                final String moduleVersion = moduleVersionTmp;
                final String fileName = fileNameTmp;
                final String fileView = fileName.indexOf(".") == fileName.lastIndexOf(".") ? "default" : fileName.substring(fileName.indexOf(".") + 1, fileName.lastIndexOf("."));
                final String fileType = fileTypeTmp;
                final String fileTemplateType = fileTemplateTypeTmp;


                // Open popup to select module

                final Window popup = new Window();
                popup.setHeading(Messages.get("label.saveAsView", "Save as view"));
                popup.setHeight(200);
                popup.setWidth(350);
                popup.setModal(true);
                FormPanel f = new FormPanel();
                f.setHeaderVisible(false);
                final SimpleComboBox<String> dependenciesCombo = new SimpleComboBox<String>();
                if (JahiaGWTParameters.getSiteNode() != null) {
                    dependenciesCombo.setStore(new ListStore<SimpleComboValue<String>>());
                    dependenciesCombo.setFieldLabel(Messages.get("label.module", "Module"));
                    dependenciesCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
                    dependenciesCombo.add(moduleName);
                    for (GWTJahiaNode n : JahiaGWTParameters.getSitesMap().values()) {
                        dependenciesCombo.add(n.getName());
                    }
                    dependenciesCombo.setSimpleValue(moduleName);
                    f.add(dependenciesCombo);
                }
                final TextField<String> templateType = new TextField<String>();
                templateType.setFieldLabel(Messages.get("label.templateType", "template Type"));
                templateType.setValue(fileTemplateType);
                f.add(templateType);

                final TextField<String> viewName = new TextField<String>();
                viewName.setFieldLabel(Messages.get("label.viewName", "View name"));
                viewName.setValue(fileView);
                f.add(viewName);

                Button b = new Button(Messages.get("label.submit", "submit"));
                f.addButton(b);
                b.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        String newModuleName = moduleName;
                        String newModulePath = modulePath;
                        String newModuleVersion = moduleVersion;
                        GWTJahiaNode newModuleNode = null;
                        if (!newModuleName.equals(dependenciesCombo.getSimpleValue())) {
                            for (GWTJahiaNode n : JahiaGWTParameters.getSitesMap().values()) {
                                if (n.getName().equals(dependenciesCombo.getSimpleValue())) {
                                    newModuleNode = n;
                                    newModuleName = dependenciesCombo.getSimpleValue();
                                    newModulePath = n.getPath();
                                    newModuleVersion = (String) n.getProperties().get("j:versionInfo");
                                    break;
                                }
                            }
                        }
                        String newfileTemplateType = !"".equals(templateType.getValue()) ? templateType.getValue() : fileTemplateType;
                        String newfileView;
                        String viewNameValue = viewName.getValue();
                        if (viewNameValue == null || viewNameValue.equals("default") || viewNameValue.trim().equals("")) {
                            newfileView = "";
                        } else {
                            newfileView = "." + viewNameValue;
                        }
                        newModulePath = newModulePath + "/" +
                                newModuleVersion + "/sources/" +
                                fileType + "/" +
                                newfileTemplateType;
                        String ft = fileType.indexOf("_") > 1?fileType.substring(fileType.indexOf("_") + 1):fileName;
                        String newViewName = ft + newfileView + fileName.substring(fileName.lastIndexOf("."));
                        Map<String, String> parentNodesType = new LinkedHashMap<java.lang.String, java.lang.String>();

                        parentNodesType.put("/modules/"+newModuleName+"/"+newModuleVersion+"/sources/"+fileType, "jnt:folder");
                        parentNodesType.put("/modules/"+newModuleName+"/"+newModuleVersion+"/sources/"+fileType+"/"+newfileTemplateType, "jnt:folder");
                        parentNodesType.put(newfileTemplateType, "jnt:folder");
                        prepareAndSave(newModulePath, newViewName, parentNodesType, engine, newModuleNode);
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

    protected void prepareAndSave(final String modulePath, String viewName, Map<String, String> parentNodesType, final AbstractContentEngine engine, final GWTJahiaNode newModuleNode) {

        final Set<String> addedTypes = new HashSet<String>();
        final Set<String> removedTypes = new HashSet<String>();

        List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();
        properties.addAll(engine.getProperties().values());
        for (TabItem tab : engine.getTabs().getItems()) {
            EditEngineTabItem item = tab.getData("item");
            item.doSave(null, engine.getChangedProperties(), engine.getChangedI18NProperties(), addedTypes, removedTypes, null, engine.getAcl());
        }
        List<GWTJahiaNodeProperty> changedProperties = engine.getChangedProperties();
        if (properties.size() > 0) {
            // Edit
            for (int i=0; i < properties.size(); i++ ) {
                for (int j=0; j < changedProperties.size(); j++) {
                    if (properties.get(i).getName().equals(changedProperties.get(j).getName())) {
                        properties.get(i).setValues(changedProperties.get(j).getValues());
                    }
                }
            }
        } else {
            // Create
            properties = changedProperties;
        }
        JahiaContentManagementService.App.getInstance().createNode(modulePath, viewName, "jnt:viewFile", null, engine.getAcl(), properties, engine.changedI18NProperties, null, parentNodesType, false, new AsyncCallback<GWTJahiaNode>() {
            public void onFailure(Throwable throwable) {
                MessageBox.alert(Messages.get("label.error.processingRequestError","An error occurred while processing your request"), throwable.getMessage(), null);
            }
            public void onSuccess(GWTJahiaNode gwtJahiaNode) {
                Linker linker = engine.getLinker();
                engine.close();
                if (newModuleNode == null) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("node", gwtJahiaNode);
                    linker.refresh(data);
                } else {
                    JahiaGWTParameters.setSiteFromNode(gwtJahiaNode, linker);
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put(Linker.REFRESH_ALL, true);
                    linker.refresh(data);
                    MainModule.staticGoTo(gwtJahiaNode.getPath(), null);
                    SiteSwitcherActionItem.refreshAllSitesList(linker);
                }
            }
        });


    }

    @Override
    protected void prepareAndSave(final AbstractContentEngine engine, boolean closeAfterSave) {
        //Nothing to do here
    }
}
