package org.jahia.ajax.gwt.client.widget.toolbar.action;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:56 PM
* To change this template use File | Settings | File Templates.
*/
public class PublishActionItem extends BaseActionItem {
    public void onComponentSelection() {
        EditActions.publish(linker);
    }

    public void handleNewLinkerSelection() {
        final GWTJahiaNode gwtJahiaNode = linker.getSelectedNode();               
        if (gwtJahiaNode != null) {
            GWTJahiaPublicationInfo info = gwtJahiaNode.getPublicationInfo();
            setEnabled(info.isCanPublish() && (info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED || info.getStatus() == GWTJahiaPublicationInfo.MODIFIED));
        }
    }
}
