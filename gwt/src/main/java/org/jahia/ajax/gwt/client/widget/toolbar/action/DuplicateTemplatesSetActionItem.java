package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

/**
 * Action item to create a new templates set
 */
public class DuplicateTemplatesSetActionItem extends BaseActionItem {
    @Override public void onComponentSelection() {
        String name = Window.prompt(Messages.get("newPackageName.label"), "New package name");
        linker.loading("Duplicating template set...");
        if (name != null) {
            JahiaContentManagementService.App.getInstance().createTemplateSet(name, JahiaGWTParameters.getSiteKey(), null, new BaseAsyncCallback<GWTJahiaNode>() {
                public void onSuccess(GWTJahiaNode result) {
                    linker.loaded();
                    Info.display(Messages.get("label.information", "Information"), Messages.get("message.templateSetCreated", "Templates set successfully created"));
                    JahiaGWTParameters.getSitesMap().put(result.getUUID(), result);
                    JahiaGWTParameters.setSite(result, linker);
                    ((EditLinker) linker).getSidePanel().refresh(EditLinker.REFRESH_ALL);
                    ((EditLinker) linker).onMainSelection(result.getPath()+"/home", null, null);
                    SiteSwitcherActionItem.refreshAllSitesList(linker);
                }

                public void onApplicationFailure(Throwable caught) {
                    linker.loaded();
                    Info.display(Messages.get("label.error", "Error"), Messages.get("message.templateSetCreationFailed", "Templates set creation failed"));
                }
            });
        }
    }
}
