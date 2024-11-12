/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

/**
 * UI action item that "navigates" to the currently selected content (the displayable node, which corresponds to it) in main area of edit
 * mode.
 *
 * @author Sergiy Shyrkov
 */
public class GoToContentActionItem extends BaseActionItem {

    private static final long serialVersionUID = 3945583396729003576L;

    @Override
    public void handleNewLinkerSelection() {
        setEnabled(isActionAvailable());
    }

    private boolean isActionAvailable() {
        if (!isMainModuleAvailable()) {
            return false;
        }
        GWTJahiaNode selectedNode = linker.getSelectionContext().getSingleSelection();
        return (selectedNode != null && !isOutOfContextContent(selectedNode));
    }

    private boolean isMainModuleAvailable() {
        MainModule mainModule = MainModule.getInstance();
        return (mainModule != null && mainModule.getEditLinker() != null);
    }

    private boolean isOutOfContextContent(GWTJahiaNode selectedNode) {
        return selectedNode.getPath().startsWith(JahiaGWTParameters.getSiteNode().getPath() + "/contents/") || selectedNode.isFile();
    }

    @Override
    public void onComponentSelection() {
        if (!isActionAvailable()) {
            return;
        }
        GWTJahiaNode selectedNode = linker.getSelectionContext().getSingleSelection();
        JahiaContentManagementService.App.getInstance().getDisplayableNodePath(selectedNode.getPath(), false,
                new BaseAsyncCallback<String>() {
                    @Override
                    public void onSuccess(String path) {
                        if (path != null) {
                            MainModule.staticGoTo(path, null);
                        }
                    }
                });
    }
}
