/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.BoxComponent;
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

    public static final int INDEX_OF_MODULE_NAME = 2;
    public static final int INDEX_OF_FILE_TYPE = 8;
    public static final int INDEX_OF_FILE_NAME = 10;
    public static final int INDEX_OF_TEMPLATE_TYPE = 9;
    public static final String VIEWS_SOURCE_PATH = "/sources/src/main/resources";

    @Override
    public BoxComponent create(final AbstractContentEngine engine) {
        Button button = new Button(Messages.get("label.saveAs", "Save as ..."));
        button.addStyleName("button-saveas");
        button.setHeight(BUTTON_HEIGHT);
        button.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent event) {
                GWTJahiaNode node = engine.getNode();
                if (node == null) {
                    node = engine.getTargetNode();
                }
                final String[] filePath = node.getPath().split("/");

                final String moduleVersion;
                final String fileType;
                final String fileTemplateType;
                final String fileName;
                final String fileView;
                if ("modules".equals(filePath[1])) {
                    moduleVersion = (String) JahiaGWTParameters.getSiteNode().getProperties().get("j:versionInfo");
                    if (engine instanceof CreateContentEngine) {
                        // extract view name from target name
                        final String targetName = ((CreateContentEngine) engine).getTargetName();
                        final int periodIndex = targetName.indexOf('.');
                        if (periodIndex > 0) {
                            fileType = targetName.substring(0, periodIndex);
                            fileView = targetName.substring(periodIndex + 1);
                        } else {
                            fileType = targetName;
                            fileView = "default";
                        }

                        final int nsIndex = fileType.indexOf('_');
                        fileName = nsIndex > 1 ? fileType.substring(nsIndex + 1) + ".jsp" : fileType + ".jsp";
                        fileTemplateType = "html";
                    } else {
                        fileType = filePath[INDEX_OF_FILE_TYPE];
                        fileName = filePath[INDEX_OF_FILE_NAME];
                        fileTemplateType = filePath[INDEX_OF_TEMPLATE_TYPE];
                        fileView = "default";
                    }

                } else {
                    MessageBox.alert(Messages.get("label.error", "Error"), Messages.getWithArgs("label.issueOccursTryingResolve", "An issue occurred when trying to resolve {0}", new Object[]{node.getPath()}), null).getDialog().addStyleName("engine-save-error");
                    return;
                }
                final String modulePath = "/modules/" + filePath[INDEX_OF_MODULE_NAME];
                final String moduleName = filePath[INDEX_OF_MODULE_NAME];

                // Open popup to select module

                final Window popup = new Window();
                popup.addStyleName("save-as-view-modal");
                popup.setHeadingHtml(Messages.get("label.saveAsView", "Save as view"));
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
                    dependenciesCombo.getStore().sort("value", Style.SortDir.ASC);
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
                b.addStyleName("button-submit");
                f.addButton(b);
                b.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        String newModuleName = moduleName;
                        String newModulePath = modulePath;
                        String newModuleVersion = moduleVersion;
                        GWTJahiaNode newModuleNode = null;
                        final String dependenciesValue = dependenciesCombo.getSimpleValue();
                        if (!newModuleName.equals(dependenciesValue)) {
                            for (GWTJahiaNode n : JahiaGWTParameters.getSitesMap().values()) {
                                if (n.getName().equals(dependenciesValue)) {
                                    newModuleNode = n;
                                    newModuleName = dependenciesValue;
                                    newModulePath = n.getPath();
                                    newModuleVersion = (String) n.getProperties().get("j:versionInfo");
                                    break;
                                }
                            }
                        }
                        final String templateTypeValue = templateType.getValue();
                        String newfileTemplateType = !"".equals(templateTypeValue) ? templateTypeValue : fileTemplateType;
                        String newfileView;
                        String viewNameValue = viewName.getValue();
                        if (viewNameValue == null || viewNameValue.equals("default") || viewNameValue.trim().equals("")) {
                            newfileView = "";
                        } else {
                            newfileView = "." + viewNameValue;
                        }
                        final String versionSourceTypePath = "/" + newModuleVersion + VIEWS_SOURCE_PATH + "/" + fileType;
                        final String templatePath = versionSourceTypePath + "/" + newfileTemplateType;
                        newModulePath = newModulePath + templatePath;

                        final int nsIndex = fileType.indexOf('_');
                        String ft = nsIndex > 1 ? fileType.substring(nsIndex + 1) : fileName;
                        String newViewName = ft + newfileView + fileName.substring(fileName.lastIndexOf('.'));

                        Map<String, String> parentNodesType = new LinkedHashMap<java.lang.String, java.lang.String>();

                        final String modulePathStart = "/modules/" + newModuleName;
                        parentNodesType.put(modulePathStart + versionSourceTypePath, "jnt:folder");
                        parentNodesType.put(modulePathStart + templatePath, "jnt:folder");
                        parentNodesType.put(newfileTemplateType, "jnt:folder");
                        prepareAndSave(newModulePath, newViewName, parentNodesType, engine, newModuleNode);
                        popup.hide();
                    }
                });
                Button c = new Button(Messages.get("label.cancel", "Cancel"));
                c.addStyleName("button-cancel");
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
                popup.setFocusWidget(viewName);
                popup.show();
                viewName.setCursorPos(viewName.getValue().length());
            }
        });
        return button;
    }

    protected void prepareAndSave(final String modulePath, String viewName, Map<String, String> parentNodesType, final AbstractContentEngine engine, final GWTJahiaNode newModuleNode) {

        final Set<String> addedTypes = new HashSet<String>();
        final Set<String> removedTypes = new HashSet<String>();

        for (TabItem tab : engine.getTabs().getItems()) {
            EditEngineTabItem item = tab.getData("item");
            item.doSave(null, engine.getChangedProperties(), engine.getChangedI18NProperties(), addedTypes, removedTypes, null, engine.getAcl());
        }
        List<GWTJahiaNodeProperty> changedProperties = engine.getChangedProperties();
        List<GWTJahiaNodeProperty> properties = null;
        // this property has been added to create the engine with the right type, but has to be removed before really save it.
        engine.getProperties().remove("nodeTypeName");
        // Remove scm status from the engine as the file is new
        engine.getProperties().remove("scmStatus");
        if (engine.getProperties().size() > 0) {
            // Edit
            Map<String, GWTJahiaNodeProperty> engineProperties = new HashMap<String, GWTJahiaNodeProperty>(engine.getProperties());
            for (GWTJahiaNodeProperty changed : changedProperties) {
                GWTJahiaNodeProperty prop = engineProperties.get(changed.getName());
                if (prop != null) {
                    prop.setValues(changed.getValues());
                }
            }
            properties = new ArrayList<GWTJahiaNodeProperty>(engineProperties.values());
        } else {
            // Create
            properties = changedProperties;
        }

        JahiaContentManagementService.App.getInstance().createNode(modulePath, viewName, "jnt:viewFile", null, engine.getAcl(), properties, engine.changedI18NProperties, null, parentNodesType, false, new AsyncCallback<GWTJahiaNode>() {

            @Override
            public void onFailure(Throwable throwable) {
                MessageBox.alert(Messages.get("label.error.processingRequestError", "An error occurred while processing your request"), throwable.getMessage(), null).getDialog().addStyleName("engine-save-error");
            }

            @Override
            public void onSuccess(GWTJahiaNode gwtJahiaNode) {
                Linker linker = engine.getLinker();
                engine.close();
                if (newModuleNode == null) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("node", gwtJahiaNode);
                    linker.refresh(data);
                } else {
                    JahiaGWTParameters.setSiteNode(newModuleNode);
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
        // Nothing to do here
    }
}
