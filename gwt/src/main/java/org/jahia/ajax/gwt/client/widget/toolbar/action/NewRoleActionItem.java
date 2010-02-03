package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Feb 3, 2010
 * Time: 4:13:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewRoleActionItem extends BaseActionItem  {
    public void onComponentSelection() {
        ContentActions.createNode(linker, Messages.get("fm_newrole", "New role"), "jnt:role");
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isMainSelection() && lh.isParentWriteable() || lh.isTableSelection() && lh.isSingleFolder() && lh.isWriteable());
    }
}
