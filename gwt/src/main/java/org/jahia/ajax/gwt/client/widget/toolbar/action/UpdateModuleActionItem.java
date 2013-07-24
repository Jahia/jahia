/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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

import java.util.HashMap;
import java.util.Map;

/**
 * Action item to create a new templates set
 */
public class UpdateModuleActionItem extends BaseActionItem {

    @Override public void onComponentSelection() {

        GWTJahiaNode siteNode = JahiaGWTParameters.getSiteNode();
        String s = siteNode.get("j:versionInfo");

        if (siteNode.get("j:sourcesFolder") != null) {
            if (s.endsWith("-SNAPSHOT") && siteNode.get("j:scmURI") != null) {
                linker.loading("Updating module...");
                JahiaContentManagementService.App.getInstance().updateModule(JahiaGWTParameters.getSiteKey(), new BaseAsyncCallback() {
                    public void onSuccess(Object result) {
                        linker.loaded();
                        Info.display(Messages.get("label.information", "Information"), "Module updated");
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("event","update");
                        linker.refresh(data);
                    }

                    public void onApplicationFailure(Throwable caught) {
                        linker.loaded();
                        Info.display(Messages.get("label.error", "Error"), caught.getMessage());
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("event","update");
                        linker.refresh(data);
                    }
                });
            } else {
                final SourceControlDialog dialog = new SourceControlDialog(Messages.get("label.sendToSourceControl", "Send to source control"), false, false);
                dialog.addCallback(new Listener<WindowEvent>() {
                    @Override
                    public void handleEvent(WindowEvent be) {
                        linker.loading("Sending sources...");
                        JahiaContentManagementService.App.getInstance().sendToSourceControl(JahiaGWTParameters.getSiteKey(), dialog.getUri(), dialog.getScmType(), new BaseAsyncCallback<GWTJahiaNode>() {
                            @Override
                            public void onSuccess(GWTJahiaNode result) {
                                JahiaGWTParameters.getSitesMap().put(result.getUUID(), result);
                                JahiaGWTParameters.setSiteNode(result);
                                ((EditLinker) linker).handleNewMainSelection();
                                linker.loaded();
                            }

                            public void onApplicationFailure(Throwable caught) {
                                linker.loaded();
                                Info.display(Messages.get("label.error", "Error"), caught.getMessage());
                            }
                        });
                    }
                });
                dialog.show();
            }
        } else {
            final SourceControlDialog dialog = new SourceControlDialog(Messages.get("label.sourceControlDialog.header", "Get sources from source control"), false, true);

            if (siteNode.get("j:scmURI") != null) {
                String value = (String) siteNode.get("j:scmURI");
                if (value.startsWith("scm:")) {
                    value = value.substring(4);
                    String type = value.substring(0, value.indexOf(":"));
                    dialog.setScmType(type);
                    value = value.substring(value.indexOf(":")+1);
                }
                dialog.setUri(value);
            }
            dialog.addCallback(new Listener<WindowEvent>() {
                @Override
                public void handleEvent(WindowEvent be) {
                    linker.loading("Getting sources...");

                    JahiaContentManagementService.App.getInstance().checkoutModule(JahiaGWTParameters.getSiteKey(), dialog.getUri(), dialog.getScmType(), dialog.getBranchOrTag(), new BaseAsyncCallback<GWTJahiaNode>() {
                        public void onSuccess(GWTJahiaNode result) {
                            linker.loaded();
                            JahiaGWTParameters.getSitesMap().put(result.getUUID(), result);
                            JahiaGWTParameters.setSiteFromNode(result, linker);
                            if (((EditLinker) linker).getSidePanel() != null) {
                                Map<String, Object> data = new HashMap<String, Object>();
                                data.put(Linker.REFRESH_ALL, true);
                                ((EditLinker) linker).getSidePanel().refresh(data);
                            }
                            SiteSwitcherActionItem.refreshAllSitesList(linker);
                            ((EditLinker) linker).handleNewMainSelection();
                            Info.display(Messages.get("label.information", "Information"), "Sources downloaded");
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

    @Override
    public void handleNewLinkerSelection() {
        GWTJahiaNode siteNode = JahiaGWTParameters.getSiteNode();
        String s = siteNode.get("j:versionInfo");
        setEnabled(true);
        if (siteNode.get("j:sourcesFolder") != null) {
            if (siteNode.get("j:scmURI") != null) {
                updateTitle(Messages.get("label.updateModule", "Update module"));
                if (!s.endsWith("-SNAPSHOT")) {
                    setEnabled(false);
                }
            } else {
                updateTitle(Messages.get("label.sendToSourceControl", "Send to source control"));
            }
        } else {
            setEnabled(false);
        }
    }
 }
