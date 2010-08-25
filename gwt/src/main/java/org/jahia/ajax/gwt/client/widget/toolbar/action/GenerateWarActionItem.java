package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

/**
 * Action item to export and download the current templates set as a war
 */
public class GenerateWarActionItem extends BaseActionItem {
    @Override public void onComponentSelection() {
        final com.extjs.gxt.ui.client.widget.Window dl = new com.extjs.gxt.ui.client.widget.Window();
        HTML html = new HTML("Generating war, please wait ...");
        dl.setModal(true);
        dl.setHeading(Messages.get("label.export"));
        dl.setLayout(new FlowLayout());
        dl.setScrollMode(Style.Scroll.AUTO);
        dl.add(html);
        dl.setHeight(120);
        dl.show();

        JahiaContentManagementService.App.getInstance().generateWar(JahiaGWTParameters.getSiteKey(), new BaseAsyncCallback<GWTJahiaNode>() {
            public void onSuccess(GWTJahiaNode result) {
                dl.removeAll();
                HTML link = new HTML(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.downloadMessage.label") + "<br /><br /><a href=\"" + result.getUrl() + "\" target=\"_new\">" + result.getName() + "</a>");
                dl.add(link);
                dl.layout();
            }

            public void onApplicationFailure(Throwable caught) {
                dl.hide();;
                Info.display("War creation failed","War creation failed");
            }
        });
    }
}
