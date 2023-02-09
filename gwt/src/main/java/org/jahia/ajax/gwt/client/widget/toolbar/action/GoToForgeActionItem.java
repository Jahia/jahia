/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.ajax.gwt.client.data.GWTModuleReleaseInfo;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

public class GoToForgeActionItem extends BaseActionItem {

    private static final long serialVersionUID = 4466321584782980102L;

    @Override
    public void handleNewLinkerSelection() {
        GWTJahiaNode siteNode = JahiaGWTParameters.getSiteNode();
        setEnabled(siteNode.getProperties().containsKey("j:forgeUrl"));
    }

    @Override
    public void onComponentSelection() {
        linker.loading(Messages.get("label.releaseModule.loadingInfo", "Loading release information for module..."));
        JahiaContentManagementService.App.getInstance().getInfoForModuleRelease(JahiaGWTParameters.getSiteKey(),
                new BaseAsyncCallback<GWTModuleReleaseInfo>() {
                    public void onApplicationFailure(Throwable caught) {
                        linker.loaded();
                        MessageBox.alert(
                                Messages.get("label.error", "Error"),
                                Messages.get("label.releaseModule.loadingInfo.failure",
                                        "Cannot load module information for release") + ":\n" + caught.getMessage(), null);
                    }

                    public void onSuccess(GWTModuleReleaseInfo result) {
                        linker.loaded();
                        onInfoLoaded(result);
                    }
                });
    }

    protected void onInfoLoaded(final GWTModuleReleaseInfo result) {
        if (result != null) {
            final ForgeLoginWindow w = new ForgeLoginWindow();
            w.setCallback(new ForgeLoginWindow.Callback() {
                @Override
                public void handle(String username, String password) {
                    String forgeModulePageUrl = result.getForgeModulePageUrl();

                    if (username != null && password != null) {
                        forgeModulePageUrl +="?username="+username+"&password="+password+"&doLogin=true";
                    }

                    final String finalUrl = forgeModulePageUrl;

                    MainModule.getInstance().goToExternalUrl(finalUrl);

                    w.hide();
                }
            });
            w.show();
        } else {
            MessageBox.alert(Messages.get("label.error", "Error"), Messages.get("label.releaseModule.distributionServer.notProvided",
                    "No target distribution server configured for this module yet."), null);
        }
    }

}
