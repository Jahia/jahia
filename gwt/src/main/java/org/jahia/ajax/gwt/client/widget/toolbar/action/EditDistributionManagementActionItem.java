package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTModuleReleaseInfo;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

public class EditDistributionManagementActionItem extends BaseActionItem {

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
        // if no distribution server defined and we have module sources ask for the server info
        DistributionServerWindow distributionServerDialog = new DistributionServerWindow(result != null ? result.getRepositoryId() : null, result != null ? result.getRepositoryUrl() : null) {
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
                                }
                            });
                }
            }
        };
        distributionServerDialog.show();
    }

}
