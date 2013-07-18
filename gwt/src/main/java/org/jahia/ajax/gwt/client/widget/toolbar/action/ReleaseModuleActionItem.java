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

import java.util.HashMap;
import java.util.Map;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTModuleReleaseInfo;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.RpcMap;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;

/**
 * Action item to export and download the current templates set as a war
 */
public class ReleaseModuleActionItem extends BaseActionItem {

    private abstract class DistributionServerWindow extends Window {
        protected abstract void callback(String id, String url);

        @Override
        protected void onRender(Element parent, int pos) {
            super.onRender(parent, pos);

            setLayout(new FitLayout());
            setHeading(Messages.get("label.releaseModule.distributionServer", "Distribution server (Maven)"));
            setModal(true);
            setWidth(500);
            setHeight(270);

            VerticalPanel p = new VerticalPanel();
            p.add(new Label(Messages.get("label.releaseModule.distributionServer.notProvided",
                    "No target distribution server configured for this module yet.")));
            p.add(new HTML("<br/>"));
            p.add(new Label(Messages.get("label.releaseModule.distributionServer.purpose",
                    "A target distribution server is a Maven repository,"
                            + " where built module artifacts (module JAR file)"
                            + " are pushed to during module release process.")));
            p.add(new Label(Messages.get("label.releaseModule.distributionServer.authentication",
                    "If your distribution server requires authentication, please, provide the corresponding"
                            + " <server/> section in your Maven's settings.xml file.")));
            p.add(new HTML("<br/>"));
            p.add(new Label(Messages.get("label.releaseModule.distributionServer.provideNow",
                    "Would you like to configure the distribution server now?")));

            final FormPanel formPanel = new FormPanel();
            formPanel.setHeaderVisible(false);
            formPanel.setLabelWidth(50);
            formPanel.setFieldWidth(380);
            formPanel.setButtonAlign(HorizontalAlignment.CENTER);
            formPanel.setBorders(false);

            final TextField<String> tfRepoId = new TextField<String>();
            tfRepoId.setFieldLabel(Messages.get("label.id", "ID"));
            tfRepoId.setAllowBlank(false);
            formPanel.add(tfRepoId);

            final TextField<String> tfRepoUrl = new TextField<String>();
            tfRepoUrl.setFieldLabel(Messages.get("label.url", "URL"));
            tfRepoUrl.setAllowBlank(false);
            formPanel.add(tfRepoUrl);

            final Window w = this;
            formPanel.addButton(new Button(Messages.get("label.save", "Save"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    w.hide();
                    callback(tfRepoId.getValue(), tfRepoUrl.getValue());
                }
            }));
            formPanel.addButton(new Button(Messages.get("label.skip", "Skip"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    w.hide();
                    callback(null, null);
                }
            }));

            p.add(formPanel);

            add(p, new MarginData(5));
        }
    }

    private static final long serialVersionUID = 4466321584782980102L;

    protected boolean areModuleSourcesAvailable() {
        return JahiaGWTParameters.getSiteNode().get("j:sourcesFolder") != null;
    }

    protected void doRelease(GWTModuleReleaseInfo info, final ReleaseModuleWindow window) {
        JahiaContentManagementService.App.getInstance().releaseModule(JahiaGWTParameters.getSiteKey(), info,
                new BaseAsyncCallback<RpcMap>() {
                    public void onApplicationFailure(Throwable caught) {
                        linker.loaded();
                        Info.display(
                                Messages.get("label.error", "Error"),
                                Messages.get("label.releaseModule.failure", "Module release failed") + ":\n"
                                        + caught.getMessage());
                    }

                    public void onSuccess(RpcMap result) {
                        linker.loaded();
                        GWTJahiaNode newModule = (GWTJahiaNode) result.get("newModule");
                        String filename = (String) result.get("filename");
                        String url = (String) result.get("downloadUrl");
                        String catalogModulePageUrl = (String) result.get("catalogModulePageUrl");

                        JahiaGWTParameters.getSitesMap().remove(JahiaGWTParameters.getSiteNode().getUUID());
                        JahiaGWTParameters.getSitesMap().put(newModule.getUUID(), newModule);
                        JahiaGWTParameters.setSiteFromNode(newModule, linker);
                        if (((EditLinker) linker).getSidePanel() != null) {
                            Map<String, Object> data = new HashMap<String, Object>();
                            data.put(Linker.REFRESH_ALL, true);
                            ((EditLinker) linker).getSidePanel().refresh(data);
                        }
                        MainModule.staticGoTo(newModule.getPath(), null);
                        SiteSwitcherActionItem.refreshAllSitesList(linker);

                        window.removeAll();
                        window.setHeight(150);
                        HTML link = new HTML(Messages.get("downloadMessage.label") + "<br /><br /><a href=\"" + url
                                + "\" target=\"_new\">" + filename + "</a>");
                        window.add(link);
                        window.layout();
                        window.show();
                        if (catalogModulePageUrl != null && catalogModulePageUrl.length() > 0) {
                            com.google.gwt.user.client.Window.open(catalogModulePageUrl, "CatalogModulePage", "");
                        }
                    }
                });
    }

    @Override
    public void handleNewLinkerSelection() {
        GWTJahiaNode siteNode = JahiaGWTParameters.getSiteNode();
        String s = siteNode.get("j:versionInfo");
        if (s.endsWith("-SNAPSHOT") && siteNode.get("j:sourcesFolder") != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }

    @Override
    public void onComponentSelection() {
        linker.loading(Messages.get("label.releaseModule.loadingInfo", "Loading release information for module..."));
        JahiaContentManagementService.App.getInstance().getInfoForModuleRelease(JahiaGWTParameters.getSiteKey(),
                new BaseAsyncCallback<GWTModuleReleaseInfo>() {
                    public void onApplicationFailure(Throwable caught) {
                        linker.loaded();
                        Info.display(
                                Messages.get("label.error", "Error"),
                                Messages.get("label.releaseModule.loadingInfo.failure",
                                        "Cannot load module information for release") + ":\n" + caught.getMessage());
                    }

                    public void onSuccess(GWTModuleReleaseInfo result) {
                        linker.loaded();
                        onInfoLoaded(result);
                    }
                });
    }

    protected void onInfoLoaded(final GWTModuleReleaseInfo result) {
        if ((result == null || result.getRepositoryUrl() == null) && areModuleSourcesAvailable()) {
            // if no distribution server defined and we have module sources ask for the server info
            DistributionServerWindow distributionServerDialog = new DistributionServerWindow() {
                @Override
                protected void callback(String id, String url) {
                    if (id != null && url != null) {
                        linker.loading(Messages.get("label.releaseModule.distributionServer.updating",
                                "Updating distribution management server information for module..."));
                        JahiaContentManagementService.App.getInstance().setDistributionServerForModule(
                                JahiaGWTParameters.getSiteKey(), id, url,
                                new BaseAsyncCallback<GWTModuleReleaseInfo>() {
                                    public void onApplicationFailure(Throwable caught) {
                                        linker.loaded();
                                        Info.display(
                                                Messages.get("label.error", "Error"),
                                                Messages.get("label.releaseModule.distributionServer.updating.failure",
                                                        "Cannot update distribution server information")
                                                        + ":\n"
                                                        + caught.getMessage());
                                    }

                                    @Override
                                    public void onSuccess(GWTModuleReleaseInfo newInfo) {
                                        linker.loaded();
                                        showReleaseModuleWindo(newInfo);
                                    }
                                });
                    } else {
                        showReleaseModuleWindo(result);
                    }
                }
            };
            distributionServerDialog.show();
        } else {
            showReleaseModuleWindo(result);
        }
    }

    private void showReleaseModuleWindo(GWTModuleReleaseInfo result) {
        final ReleaseModuleWindow window = new ReleaseModuleWindow(result);
        window.setCallback(new ReleaseModuleWindow.Callback() {
            @Override
            public void handle(GWTModuleReleaseInfo info) {
                window.hide();
                linker.loading(Messages.get("label.releaseModule.releasing", "Releasing module..."));
                doRelease(info, window);
            }
        });

        window.show();
    }
}
