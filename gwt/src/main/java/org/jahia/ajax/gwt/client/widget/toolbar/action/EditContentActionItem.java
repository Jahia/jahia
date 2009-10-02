package org.jahia.ajax.gwt.client.widget.toolbar.action;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:59:03 PM
*/
public class EditContentActionItem extends BaseActionItem {
    public void onComponentSelection() {
        EditActions.edit(linker);
    }

    public void handleNewLinkerSelection() {
        final GWTJahiaNode gwtJahiaNode = linker.getSelectedNode();
        if (gwtJahiaNode != null) {
            setEnabled(gwtJahiaNode.isWriteable());
        }
    }
}
