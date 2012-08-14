package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

public class PublicationStatusActionItem extends BaseActionItem {

    private transient HorizontalPanel panel;

    public void handleNewLinkerSelection() {
        LinkerSelectionContext ctx = linker.getSelectionContext();

        final GWTJahiaPublicationInfo info = ctx.getMainNode().getAggregatedPublicationInfo();
        if (info != null) {
            Image res = GWTJahiaPublicationInfo.renderPublicationStatusImage(info.getStatus());
            panel.removeAll();
            panel.add(res);
            panel.layout();
        }
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
