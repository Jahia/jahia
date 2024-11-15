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

import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:58:06 PM
 */
@SuppressWarnings("serial")
public class ZipActionItem extends NodeTypeAwareBaseActionItem  {
    public void onComponentSelection() {
        final List<GWTJahiaNode> selectedItems = linker.getSelectionContext().getMultipleSelection();
        if (selectedItems != null && selectedItems.size() > 0) {
            final GWTJahiaNode selection = selectedItems.get(0);
            if (selection != null) {
                linker.loading(Messages.get("statusbar.zipping.label"));
                String defaultArchName;
                if (selectedItems.size() == 1) {
                    defaultArchName = selection.getName() + ".zip";
                } else {
                    defaultArchName = "archive.zip";
                }
                final String archName = Window.prompt(Messages.get("confirm.archiveName.label"), defaultArchName);
                if (archName != null && archName.length() > 0) {
                    final String parentPath = selection.getPath().substring(0, selection.getPath().lastIndexOf('/'));
                    JahiaContentManagementService
                            .App.getInstance().checkExistence(parentPath + "/" + archName, new BaseAsyncCallback<Boolean>() {
                        public void onApplicationFailure(Throwable throwable) {
                            if (throwable instanceof ExistingFileException) {
                                if (Window.confirm(Messages.get("alreadyExists.label") + "\n" + Messages.get("confirm.overwrite.label"))) {
                                    forceZip(selectedItems, archName, linker);
                                }
                            } else {
                                Window.alert(Messages.get("failure.zip.label") + "\n" + throwable.getLocalizedMessage());
                                linker.loaded();
                            }
                        }

                        public void onSuccess(Boolean aBoolean) {
                            forceZip(selectedItems, archName, linker);
                        }
                    });
                }
            }
        }
    }

    private static void forceZip(final List<GWTJahiaNode> selectedItems, final String archName, final Linker linker) {
        List<String> selectedPaths = new ArrayList<String>(selectedItems.size());
        for (GWTJahiaNode node : selectedItems) {
            selectedPaths.add(node.getPath());
        }
        JahiaContentManagementService.App.getInstance().zip(selectedPaths, archName, new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable throwable) {
                Window.alert(Messages.get("failure.zip.label") + "\n" + throwable.getLocalizedMessage());
                linker.loaded();
            }

            public void onSuccess(Object o) {
                linker.loaded();
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(Linker.REFRESH_MAIN, true);
                linker.refresh(data);
            }
        });
    }


    public void handleNewLinkerSelection(){
        LinkerSelectionContext lh = linker.getSelectionContext();
        Boolean isContentType = lh.getSingleSelection() != null && isNodeTypeAllowed(lh.getSingleSelection());
        setEnabled(lh.getMultipleSelection().size() > 0 && hasPermission(lh.getSelectionPermissions()) && lh.isParentWriteable() && !lh.isSecondarySelection() && isContentType );
    }
}
