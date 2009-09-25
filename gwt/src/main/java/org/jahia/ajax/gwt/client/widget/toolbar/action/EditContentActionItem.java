package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.toolbar.handler.ModuleSelectionHandler;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;
import org.jahia.ajax.gwt.client.widget.edit.Module;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:59:03 PM
* To change this template use File | Settings | File Templates.
*/
public class EditContentActionItem extends BaseActionItem implements ModuleSelectionHandler {
    public void onSelection() {
        EditActions.edit(linker);
    }

    public void handleNewModuleSelection(Module selectedModule) {
        if (selectedModule != null) {
            setEnabled(selectedModule.getNode().isWriteable());
        }
    }
}
