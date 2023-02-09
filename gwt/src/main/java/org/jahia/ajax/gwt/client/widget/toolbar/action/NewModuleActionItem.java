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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

import java.util.HashMap;
import java.util.Map;

/**
 * Action item to create a new module
 */
@SuppressWarnings("serial")
public class NewModuleActionItem extends BaseActionItem {
    private String siteType = null;

    public void setSiteType(String siteType) {
        this.siteType = siteType;
    }

    @Override public void onComponentSelection() {
        final Dialog dialog = new Dialog();
        dialog.setHeadingHtml(Messages.get("label.moduleCreate", "Create module"));
        dialog.setButtons(Dialog.OKCANCEL);
        dialog.setModal(true);
        dialog.setHideOnButtonClick(true);
        dialog.setWidth(500);
        dialog.setHeight(300);

        dialog.setLayout(new FitLayout());

        final FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setFrame(false);
        form.setLabelWidth(125);

        final SimpleComboBox<String> moduleTypeCombo = new SimpleComboBox<String>();
        if (siteType == null) {
            moduleTypeCombo.setStore(new ListStore<SimpleComboValue<String>>());
            moduleTypeCombo.setFieldLabel(Messages.get("label.moduleType", "Module type"));

            moduleTypeCombo.add("module");
            moduleTypeCombo.add("templatesSet");
            moduleTypeCombo.setSimpleValue("module");
            moduleTypeCombo.setTriggerAction(ComboBox.TriggerAction.ALL);

            form.add(moduleTypeCombo);
        }

        final TextField<String> moduleName = new TextField<String>();
        moduleName.setName("moduleName");
        moduleName.setAllowBlank(false);
        moduleName.setFieldLabel(Messages.get("label.moduleName", "Module name"));
        form.add(moduleName);
        final TextField<String> artifactId = new TextField<String>();
        artifactId.setName("artifactId");
        artifactId.setFieldLabel(Messages.get("label.moduleId", "Module ID (artifactId)"));
        artifactId.setEmptyText(Messages.get("label.moduleId.empty", "Generated from module name"));
        form.add(artifactId);
        final TextField<String> groupId = new TextField<String>();
        groupId.setName("groupId");
        groupId.setFieldLabel(Messages.get("label.groupId", "groupId"));
        groupId.setEmptyText(Messages.get("label.groupId.empty", "org.foo.modules"));
        form.add(groupId);

        final TextField<String> sources = new TextField<String>();
        sources.setName("sources");
        sources.setFieldLabel(Messages.get("label.sources.folder", "Sources folder (optional - will be created with new sources)"));
        sources.setEmptyText(JahiaGWTParameters.getModulesSourcesDiskPath());
        sources.addListener(Events.OnClick, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                if (!sources.isDirty()) {
                    sources.setRawValue(JahiaGWTParameters.getModulesSourcesDiskPath());
                }
            }
        });
        form.add(sources);
        dialog.add(form);

        dialog.addListener(Events.Hide, new Listener<WindowEvent>() {
            @Override
            public void handleEvent(WindowEvent be) {
                if (be.getButtonClicked().getItemId().equalsIgnoreCase(Dialog.OK)) {
                    if (form.isValid()) {
                        linker.loading(Messages.get("statusbar.creatingModule.label", "Creating module..."));
                        JahiaContentManagementService.App.getInstance().createModule(moduleName.getValue(), artifactId.getValue(), groupId.getValue(), siteType != null ? siteType : moduleTypeCombo.getSimpleValue(), sources.getValue(), new BaseAsyncCallback<GWTJahiaNode>() {
                            public void onSuccess(GWTJahiaNode result) {
                                linker.loaded();
                                Info.display(Messages.get("label.information", "Information"), Messages.get("message.moduleCreated", "Module successfully created"));
                                JahiaGWTParameters.getSitesMap().put(result.getUUID(), result);
                                JahiaGWTParameters.setSiteNode(result);
                                if (((EditLinker) linker).getSidePanel() != null) {
                                    Map<String, Object> data = new HashMap<String, Object>();
                                    data.put(Linker.REFRESH_ALL, true);
                                    ((EditLinker) linker).getSidePanel().refresh(data);
                                }
                                MainModule.staticGoTo(result.getPath(), null);
                                SiteSwitcherActionItem.refreshAllSitesList(linker);
                            }

                            public void onApplicationFailure(Throwable caught) {
                                linker.loaded();
                                final InfoConfig config = new InfoConfig(Messages.get("label.error", "Error"), caught.getLocalizedMessage());
                                config.display = 5000;
                                config.height = 100;
                                config.width = 250;
                                Info.display(config);
                            }
                        });
                    } else {
                        dialog.show();
                    }
                }
            }
        });

        dialog.show();
    }
}
