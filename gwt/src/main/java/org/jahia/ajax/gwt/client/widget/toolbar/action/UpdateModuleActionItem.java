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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

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
@SuppressWarnings("serial")
public class UpdateModuleActionItem extends BaseActionItem {

    @Override
    public void onComponentSelection() {

        GWTJahiaNode siteNode = JahiaGWTParameters.getSiteNode();
        String s = siteNode.get("j:versionInfo");

        if (siteNode.get("j:sourcesFolder") != null) {
            if (s.endsWith("-SNAPSHOT") && siteNode.get("j:scmURI") != null) {
                linker.loading(Messages.get("label.sourceControl.update.module", "Updating module..."));
                JahiaContentManagementService.App.getInstance().updateModule(JahiaGWTParameters.getSiteKey(), new BaseAsyncCallback<String>() {

                    @Override
                    public void onSuccess(String result) {
                        linker.loaded();
                        showUpdateResult(result, false);
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("event", "update");
                        data.put(Linker.REFRESH_ALL, "true");
                        linker.refresh(data);
                    }

                    @Override
                    public void onApplicationFailure(Throwable caught) {
                        linker.loaded();
                        showUpdateResult(caught.getMessage(), true);
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("event", "update");
                        linker.refresh(data);
                    }
                });
            } else {
                final SourceControlDialog dialog = new SourceControlDialog(Messages.get("label.sendToSourceControl", "Send to source control"), false, false);
                dialog.addCallback(new Listener<WindowEvent>() {
                    @Override
                    public void handleEvent(WindowEvent be) {
                        linker.loading(Messages.get("label.sourceControl.sending.sources", "Sending sources..."));
                        JahiaContentManagementService.App.getInstance().sendToSourceControl(JahiaGWTParameters.getSiteKey(), dialog.getUri(), dialog.getScmType(), new BaseAsyncCallback<GWTJahiaNode>() {
                            @Override
                            public void onSuccess(GWTJahiaNode result) {
                                JahiaGWTParameters.getSitesMap().put(result.getUUID(), result);
                                JahiaGWTParameters.setSiteNode(result);
                                ((EditLinker) linker).handleNewMainSelection();
                                linker.loaded();
                            }

                            @Override
                            public void onApplicationFailure(Throwable caught) {
                                linker.loaded();
                                MessageBox.alert(Messages.get("label.error", "Error"), caught.getMessage(), null);
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
                    value = value.substring(value.indexOf(":") + 1);
                }
                dialog.setUri(value);
            }
            dialog.addCallback(new Listener<WindowEvent>() {
                @Override
                public void handleEvent(WindowEvent be) {
                    linker.loading(Messages.get("label.sourceControl.getting.sources", "Getting sources..."));

                    JahiaContentManagementService.App.getInstance().checkoutModule(JahiaGWTParameters.getSiteKey(), dialog.getUri(), dialog.getScmType(), dialog.getBranchOrTag(), null, new BaseAsyncCallback<GWTJahiaNode>() {

                        @Override
                        public void onSuccess(GWTJahiaNode result) {
                            linker.loaded();
                            JahiaGWTParameters.getSitesMap().put(result.getUUID(), result);
                            JahiaGWTParameters.setSiteNode(result);
                            if (((EditLinker) linker).getSidePanel() != null) {
                                Map<String, Object> data = new HashMap<String, Object>();
                                data.put(Linker.REFRESH_ALL, true);
                                ((EditLinker) linker).getSidePanel().refresh(data);
                            }
                            SiteSwitcherActionItem.refreshAllSitesList(linker);
                            ((EditLinker) linker).handleNewMainSelection();
                            Info.display(Messages.get("label.information", "Information"), Messages.get("label.sourceControl.source.downloaded", "Sources downloaded"));
                        }

                        @Override
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

    private void showUpdateResult(String output, boolean isError) {
        final Window wnd = new Window();
        wnd.addStyleName("update-results-modal");
        wnd.setWidth(450);
        wnd.setHeight(250);
        wnd.setModal(true);
        wnd.setBlinkModal(true);
        wnd.setHeadingHtml(Messages.get("label.updateModule", "Update module"));
        wnd.setLayout(new FitLayout());

        final FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setFrame(false);
        form.setLabelAlign(LabelAlign.TOP);
        form.setFieldWidth(415);

        final TextArea message = new TextArea();
        message.setName("status");
        message.setFieldLabel(Messages.get("label.status", "Status")
                + " - "
                + (isError ? Messages.get("label.error", "Error") : Messages.get("label.sourceControl.module.updated",
                        "Module updated")));
        message.setHeight(120);
        message.setValue(output);
        message.setReadOnly(true);

        form.add(message);

        Button btnClose = new Button(Messages.get("label.close", "Close"), new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent event) {
                wnd.hide();
            }
        });
        btnClose.addStyleName("button-close");
        form.addButton(btnClose);
        form.setButtonAlign(Style.HorizontalAlignment.CENTER);

        wnd.add(form);
        wnd.layout();
        wnd.setFocusWidget(message);

        wnd.show();

    }
 }
