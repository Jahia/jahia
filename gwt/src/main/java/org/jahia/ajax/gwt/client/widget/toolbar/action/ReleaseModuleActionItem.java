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
