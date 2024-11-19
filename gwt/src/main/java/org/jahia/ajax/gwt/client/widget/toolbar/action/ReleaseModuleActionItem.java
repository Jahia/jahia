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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.*;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTModuleReleaseInfo;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

import com.extjs.gxt.ui.client.data.RpcMap;
import com.google.gwt.user.client.ui.HTML;

/**
 * Action item to export and download the current templates set as a war
 */
public class ReleaseModuleActionItem extends BaseActionItem {

    private static final long serialVersionUID = 4466321584782980102L;

    protected boolean areModuleSourcesAvailable() {
        return JahiaGWTParameters.getSiteNode().get("j:sourcesFolder") != null;
    }

    protected void doRelease(final GWTModuleReleaseInfo info, final ReleaseModuleWindow window) {
        JahiaContentManagementService.App.getInstance().releaseModule(JahiaGWTParameters.getSiteKey(), info,
                new BaseAsyncCallback<RpcMap>() {
                    public void onApplicationFailure(Throwable caught) {
                        linker.loaded();
                        MessageBox.alert(
                                Messages.get("label.releaseModule.failure", "Module release failed") ,
                                Messages.get(caught.getMessage()), null);
                    }

                    public void onSuccess(RpcMap result) {
                        linker.loaded();
                        GWTJahiaNode newModule = (GWTJahiaNode) result.get("newModule");
                        String filename = (String) result.get("filename");
                        String artifactUrl = (String) result.get("artifactUrl");
                        String url = (String) result.get("downloadUrl");
                        String catalogModulePageUrl = (String) result.get("catalogModulePageUrl");

                        JahiaGWTParameters.getSitesMap().remove(JahiaGWTParameters.getSiteNode().getUUID());
                        JahiaGWTParameters.getSitesMap().put(newModule.getUUID(), newModule);
                        JahiaGWTParameters.setSiteNode(newModule);
                        if (((EditLinker) linker).getSidePanel() != null) {
                            Map<String, Object> data = new HashMap<String, Object>();
                            data.put(Linker.REFRESH_ALL, true);
                            ((EditLinker) linker).getSidePanel().refresh(data);
                        }
                        MainModule.staticGoTo(newModule.getPath(), null);
                        SiteSwitcherActionItem.refreshAllSitesList(linker);

                        window.removeAll();
                        window.setHeight(150);

                        if (artifactUrl != null) {
                            window.add(new HTML(Messages.get("downloadMessage.label") + "<br /><br /><a href=\"" + artifactUrl
                                    + "\" target=\"_new\">" + artifactUrl + "</a>"));
                        } else {
                            window.add(new HTML(Messages.get("downloadMessage.label") + "<br /><br /><a href=\"" + url
                                    + "\" target=\"_new\">" + filename + "</a>"));
                        }

                        window.layout();
                        window.show();
                        if (catalogModulePageUrl != null && catalogModulePageUrl.length() > 0) {
                            if (info.getUsername() != null && info.getPassword() != null) {
                                catalogModulePageUrl +="?username="+info.getUsername()+"&password="+info.getPassword()+"&doLogin=true";
                            }

                            MainModule.getInstance().goToExternalUrl(catalogModulePageUrl);
                        }
                    }
                });
    }

    @Override
    public void handleNewLinkerSelection() {
        GWTJahiaNode siteNode = JahiaGWTParameters.getSiteNode();
        String s = siteNode.get("j:versionInfo");
        if (s != null && s.endsWith("-SNAPSHOT") && siteNode.get("j:sourcesFolder") != null) {
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
                protected void callback(GWTModuleReleaseInfo info) {
                    if (info != null) {
                        linker.loading(Messages.get("label.releaseModule.distributionServer.updating",
                                "Updating distribution management server information for module..."));
                        JahiaContentManagementService.App.getInstance().setDistributionServerForModule(
                                JahiaGWTParameters.getSiteKey(), info,
                                new BaseAsyncCallback<GWTModuleReleaseInfo>() {
                                    public void onApplicationFailure(Throwable caught) {
                                        linker.loaded();
                                        MessageBox.alert(
                                                Messages.get("label.error", "Error"),
                                                Messages.get("label.releaseModule.distributionServer.updating.failure",
                                                        "Cannot update distribution server information")
                                                        + ":\n"
                                                        + caught.getMessage(), null);
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
