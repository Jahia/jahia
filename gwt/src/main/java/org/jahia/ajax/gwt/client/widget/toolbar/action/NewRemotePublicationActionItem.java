package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Apr 26, 2010
 * Time: 4:54:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewRemotePublicationActionItem extends BaseActionItem  {
    public void onComponentSelection() {
        ContentActions.createNode(linker, Messages.get("fm_newremotepublication", "New Remote Publication"), "jnt:remotePublication");
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isMainSelection() && lh.isParentWriteable() || lh.isTableSelection() && lh.isSingleFolder() && lh.isWriteable());
    }
}
