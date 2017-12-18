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
                ("root".equals(JahiaGWTParameters.getCurrentUser()) || checkLockInfos(singleSelection.getLockInfos())));
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
