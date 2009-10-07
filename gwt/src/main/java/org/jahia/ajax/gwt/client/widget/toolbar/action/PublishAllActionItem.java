package org.jahia.ajax.gwt.client.widget.toolbar.action;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:56 PM
* To change this template use File | Settings | File Templates.
*/
public class PublishAllActionItem extends BaseActionItem {
    public void onComponentSelection() {
        EditActions.publishAll(linker);
    }

    /**
     * Init the action item.
     *
     * @param gwtToolbarItem
     * @param linker
     */
    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);    //To change body of overridden methods use File | Settings | File Templates.
        setEnabled(false);
    }

    public void handleNewLinkerSelection() {
        GWTJahiaNode gwtJahiaNode = linker.getSelectedNode();
        if (gwtJahiaNode == null) {
            gwtJahiaNode = linker.getMainNode();
        }
        if (gwtJahiaNode != null) {
            GWTJahiaPublicationInfo info = gwtJahiaNode.getPublicationInfo();
            setEnabled(info.isCanPublish() && (info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED || info.getStatus() == GWTJahiaPublicationInfo.MODIFIED));
        }
    }
}