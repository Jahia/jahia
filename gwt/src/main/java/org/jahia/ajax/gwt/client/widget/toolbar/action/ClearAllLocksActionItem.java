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

import com.extjs.gxt.ui.client.widget.MessageBox;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Action item for cleaning all the locks on the node and if specified on its children.
 * @author david
 * Date: 4/28/11
 * Time: 3:10 PM
 */


public class ClearAllLocksActionItem extends BaseActionItem {

    private boolean doSubNodes = false;

    public void setDoSubNodes(boolean doSubNodes) {
        this.doSubNodes = doSubNodes;
    }

    public void onComponentSelection() {
        String selectedPaths = linker.getSelectionContext().getSingleSelection().getPath();
        JahiaContentManagementService.App.getInstance().clearAllLocks(selectedPaths, doSubNodes, new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable throwable) {
                MessageBox.alert(Messages.get("label.error", "Error"), throwable.getLocalizedMessage(), null);
                linker.loaded();
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(Linker.REFRESH_MAIN, true);
                linker.refresh(data);
            }

            public void onSuccess(Object o) {
                linker.loaded();
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(Linker.REFRESH_MAIN, true);
                linker.refresh(data);
            }
        });


    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        GWTJahiaNode singleSelection = lh.getSingleSelection();
        setEnabled(singleSelection!=null && singleSelection.isLockable() && hasPermission(lh.getSelectionPermissions()) &&
                PermissionsUtils.isPermitted("jcr:lockManagement", lh.getSelectionPermissions()) && singleSelection.getLockInfos() != null &&
                !lh.getSingleSelection().getLockInfos().isEmpty() &&
                (PermissionsUtils.isPermitted("clearLock", lh.getSelectionPermissions()) || checkLockInfos(singleSelection.getLockInfos())));
    }

    private boolean checkLockInfos(Map<String,List<String>> lockInfos) {
        for (List<String> list : lockInfos.values()) {
            for (String s : list) {
                if (!s.equals(JahiaGWTParameters.getCurrentUser())) {
                    return false;
                }
            }
        }
        return true;
    }
}
