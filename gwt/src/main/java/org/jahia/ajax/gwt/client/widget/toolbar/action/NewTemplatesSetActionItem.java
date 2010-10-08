package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

/**
 * Action item to create a new templates set
 */
public class NewTemplatesSetActionItem extends BaseActionItem {
    @Override public void onComponentSelection() {
        String name = Window.prompt(Messages.get("newDirName.label"), "New package name");
        if (name != null) {
            JahiaContentManagementService.App.getInstance().createTemplateSet(name, new BaseAsyncCallback<GWTJahiaNode>() {
                public void onSuccess(GWTJahiaNode result) {
                    Info.display("Templates set created","Templates set created");
                    JahiaGWTParameters.setSiteUUID(result.getUUID());
                    JahiaGWTParameters.setSiteKey(result.getName());
                    ((EditLinker) linker).getSidePanel().refresh(EditLinker.REFRESH_ALL);
                    ((EditLinker) linker).onMainSelection(result.getPath()+"/home", null, null);
                    SiteSwitcherActionItem.refreshAllSitesList(linker);
                }

                public void onApplicationFailure(Throwable caught) {
                    Info.display("Templates set creation failed","Templates set creation failed");
                }
            });
        }
    }
}
