package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.Arrays;
import java.util.List;

public class PublicationStatusActionItem extends BaseActionItem {

    private transient HorizontalPanel panel;

    @Override
    public void handleNewMainNodeLoaded(final GWTJahiaNode node) {
        final GWTJahiaPublicationInfo info = node.getAggregatedPublicationInfo();
        if (info != null) {
            displayInfo(info);
        } else {
            JahiaContentManagementService.App.getInstance().getNodes(Arrays.asList(node.getPath()), Arrays.asList(GWTJahiaNode.PUBLICATION_INFO), new BaseAsyncCallback<List<GWTJahiaNode>>() {
                public void onSuccess(List<GWTJahiaNode> result) {
                    final GWTJahiaPublicationInfo info = result.get(0).getAggregatedPublicationInfo();
                    if (info != null) {
                        displayInfo(info);
                    }
                }
            });
        }
    }

    private void displayInfo(GWTJahiaPublicationInfo info) {
        Image res = GWTJahiaPublicationInfo.renderPublicationStatusImage(info.getStatus());
        panel.removeAll();
        panel.add(res);
        panel.layout();
    }

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        panel = new HorizontalPanel();
    }

    @Override
    public Component getCustomItem() {
        return panel;
    }

}
