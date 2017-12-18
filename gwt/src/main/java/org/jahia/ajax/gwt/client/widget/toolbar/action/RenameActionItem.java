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
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.content.FileUploader;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:57:51 PM
 */
@SuppressWarnings("serial")
public class RenameActionItem extends NodeTypeAwareBaseActionItem {

    private int maxNameSize = -1;

    public void onComponentSelection() {
        final GWTJahiaNode selection = linker.getSelectionContext().getSingleSelection();
        if (selection != null) {
            if (selection.isLocked()) {
                Window.alert(selection.getName() + " is locked");
                return;
            }
            MainModule mainModule = MainModule.getInstance();
            final boolean goTo;
            if (mainModule != null) {
                goTo = selection.getPath().equals(mainModule.getNode().getPath());
            } else {
                goTo = false;
            }
            linker.loading(Messages.get("statusbar.renaming.label"));
            String newName = Window.prompt(Messages.get("confirm.newName.label") + " " + selection.getName(),
                    selection.getName());
            if (selection.isNodeType("nt:hierarchyNode") && newName != null && FileUploader.filenameHasInvalidCharacters(newName)) {
                MessageBox.alert(Messages.get("label.error"), Messages.getWithArgs("failure.upload.invalid.filename", "{0} is an invalid name", new String[]{newName}), null);
            } else if (maxNameSize > 0 && newName != null && newName.length() > 0 && newName.length() > maxNameSize) {
                MessageBox.alert(Messages.get("label.error"), Messages.getWithArgs("failure.upload.invalid.filename.toolong", "The name is too long. The maximum length is {0}", new String[]{String.valueOf(maxNameSize)}), null);
            } else if (newName != null && newName.length() > 0 && !newName.equals(selection.getName())) {
                JahiaContentManagementService.App.getInstance()
                        .rename(selection.getPath(), newName, new BaseAsyncCallback<GWTJahiaNode>() {
                            public void onApplicationFailure(Throwable throwable) {
                                MessageBox.alert(Messages.get("failure.rename.label"), throwable.getLocalizedMessage(), null);
                                linker.loaded();
                            }

                            public void onSuccess(GWTJahiaNode node) {
                                if (goTo) {
                                    linker.setSelectPathAfterDataUpdate(Arrays.asList(node.getPath()));
                                }
                                linker.loaded();
                                Map<String, Object> data = new HashMap<String, Object>();
                                data.put("node", node);
                                if (linker instanceof ManagerLinker) {
                                    data.put(Linker.REFRESH_ALL,true);
                                }
                                linker.refresh(data);
                            }
                        });
            } else {
                linker.loaded();
            }
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.getSingleSelection() != null
                && hasPermission(lh.getSelectionPermissions())
                && PermissionsUtils.isPermitted("jcr:write", lh.getSelectionPermissions())
                && !lh.isLocked()
                && !lh.isRootNode()
                && !lh.getSingleSelection().getPath().equals("/sites/"+lh.getSingleSelection().getSiteKey()+"/"+lh.getSingleSelection().getName())
                && !lh.getSingleSelection().getPath().equals("/"+lh.getSingleSelection().getName())
                && isNodeTypeAllowed(lh.getSingleSelection()));
    }

    public void setMaxNameSize(int maxNameSize) {
        this.maxNameSize = maxNameSize;
    }
}
