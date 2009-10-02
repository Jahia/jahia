package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.edit.EditActions;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:59:06 PM
*/
public class DeleteActionItem extends BaseActionItem {
    public DeleteActionItem() {
    }

    public void onComponentSelection() {
        EditActions.delete(linker);
    }

    public void handleNewLinkerSelection() {
        final GWTJahiaNode gwtJahiaNode = linker.getSelectedNode();
        if (gwtJahiaNode != null) {
            setEnabled(gwtJahiaNode.isWriteable());
        }
    }
}
