package org.jahia.ajax.gwt.client.widget.toolbar.action;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
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
public class PublishAllActionItem extends PublishActionItem {
    public void onComponentSelection() {
        EditActions.publish(linker, true);
    }

    public void handleNewLinkerSelection() {
        GWTJahiaNode gwtJahiaNode = linker.getSelectedNode();
        if (gwtJahiaNode == null) {
            gwtJahiaNode = linker.getMainNode();
        }
        if (gwtJahiaNode != null) {
            GWTJahiaPublicationInfo info = gwtJahiaNode.getPublicationInfo();
            updateTitle(getGwtToolbarItem().getTitle() + " " + gwtJahiaNode.getName());
        }
    }

}