/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
