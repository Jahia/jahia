package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.http.client.*;
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

                    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(finalUrl));
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
