/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
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
 * Action item to create a new templates set
 */
public class FetchModuleActionItem extends BaseActionItem {

    @Override public void onComponentSelection() {

        final SourceControlDialog dialog = new SourceControlDialog(Messages.get("label.sourceControlDialog.header", "Get sources from source control"), true, true);
        dialog.addCallback(new Listener<WindowEvent>() {
            @Override
            public void handleEvent(WindowEvent be) {
                linker.loading(Messages.get("statusbar.creatingModule.label", "Creating module..."));
                JahiaContentManagementService.App.getInstance().checkoutModule(dialog.getModuleId(), dialog.getUri(), dialog.getScmType(), dialog.getBranchOrTag(), dialog.getSources(), new BaseAsyncCallback<GWTJahiaNode>() {
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
                        MessageBox.alert(Messages.get("label.error", "Error"), caught.getMessage(), null);
                    }
                });
            }
        });
        dialog.show();
    }
}
