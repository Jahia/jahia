/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:57:51 PM
 */
public class RenameActionItem extends BaseActionItem {
    public void onComponentSelection() {
        final GWTJahiaNode selection = linker.getSelectionContext().getSingleSelection();
        if (selection != null) {
            if (selection.isLocked()) {
                Window.alert(selection.getName() + " is locked");
                return;
            }
            linker.loading(Messages.get("statusbar.renaming.label"));
            String newName = Window.prompt(Messages.get("confirm.newName.label") + " " + selection.getName(),
                    selection.getName());
            if (newName != null && newName.length() > 0 && !newName.equals(selection.getName())) {
                final boolean folder = !selection.isFile();
                JahiaContentManagementService.App.getInstance()
                        .rename(selection.getPath(), newName, new BaseAsyncCallback() {
                            public void onApplicationFailure(Throwable throwable) {
                                Window.alert(
                                        Messages.get("failure.rename.label") + "\n" + throwable.getLocalizedMessage());
                                linker.loaded();
                            }

                            public void onSuccess(Object o) {
                                linker.loaded();
                                if (folder) {
                                    linker.refresh(EditLinker.REFRESH_ALL);
                                } else {
                                    linker.refresh(Linker.REFRESH_MAIN);
                                }
                            }
                        });
            } else {
                linker.loaded();
            }
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.getSingleSelection() != null && PermissionsUtils.isPermitted("jcr:write", lh.getSelectionPermissions()) && !lh.isSecondarySelection() && !lh.getSingleSelection().getPath().equals("/sites/"+lh.getSingleSelection().getSiteKey()+"/"+lh.getSingleSelection().getName()) && !lh.getSingleSelection().getPath().equals("/"+lh.getSingleSelection().getName()));
    }
}
